package graduate.node.impl;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import graduate.model.clientmodel.ClientKVAck;
import graduate.model.clientmodel.ClientKVReq;
import graduate.model.logmodulemodel.Command;
import graduate.statemachine.impl.StateMachineImpl;
import io.netty.util.internal.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import graduate.concurrent.RaftThreadPool;
import graduate.consensus.impl.ConsensusImpl;
import graduate.lifecycle.ILifeCycle;
import graduate.logmodule.impl.LogModuleImpl;
import graduate.model.consensusmodel.aentry.AentryParam;
import graduate.model.consensusmodel.aentry.AentryResult;
import graduate.model.consensusmodel.rvote.RvoteParam;
import graduate.model.consensusmodel.rvote.RvoteResult;
import graduate.model.logmodulemodel.LogEntry;
import graduate.model.node.NodeConfig;
import graduate.model.node.NodeStatus;
import graduate.model.peer.Peer;
import graduate.model.peer.PeerSet;
import graduate.model.rpcmodel.Request;
import graduate.model.rpcmodel.Response;
import graduate.node.INode;
import graduate.rpc.impl.RpcClientImpl;
import graduate.rpc.impl.RpcServerImpl;
import io.netty.util.internal.ThreadLocalRandom;

/**
 * 机器节点 指代raspberry
 */
@Setter
@Getter
public class NodeImpl implements INode, ILifeCycle
{
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeImpl.class);

	private volatile long electionTime  = 5 * 1000;		/** 选举超时时间 10s 后面要改，这个时间太长了 */
	private final 	 long heartBeatTick = 5 * 100;		/** 心跳间隔时间 5s时间太长 */
	private volatile long preElectionTime;				/** 上一次选举时间戳 */
	private volatile long preHeartBeatTime;				/** 上一次心跳时间戳 */

	/** 两个任务 心跳和选举任务 */
	private HeartBeatTask heartBeatTask = new HeartBeatTask();
	private ElectionTask  electionTask  = new ElectionTask();
	
	private volatile int status = NodeStatus.FOLLOWER;
	private PeerSet peerSet;

	/** ===========所有服务器上持久存在的========== */
	private volatile long 	currentTerm = 0;
	private volatile String votedFor;

	/** ===========所有服务器上经常变的 =========== */
	private volatile long commitIndex;
	private volatile long lastApplied = 0;

	/** ===========领导人上经常变的============== */
	private Map<Peer, Long> nextIndexs;
	private Map<Peer, Long> matchIndexs;
	
	/** ==================================== */
	private 			NodeConfig 			nodeConfig;
	private static  	RpcServerImpl   	rpcServerImpl;
	private 			RpcClientImpl 		rpcClientImpl = new RpcClientImpl();
	private volatile 	boolean 			started; 							/** 记录当前节点是否已经启动 */
	private 			StateMachineImpl 	stateMachine;						/** 状态机				 */

	private ConsensusImpl consensusImpl;		//一致性模块
	private LogModuleImpl logModuleImpl;		//日志模块

	/** ========静态内部类实现单例模式多线程安全======= */
	private NodeImpl(){}
	private static class NodeLazyHolder {
		private static final NodeImpl instance = new NodeImpl();
	}
	public static NodeImpl getInstance()
	{
		return NodeLazyHolder.instance;
	}


	/** ===========LifeCycle方法============= */	// NodeBootStrap#main0() -> {setConfig(),init()}
	@Override
	public void setConfig(NodeConfig config) {
		this.nodeConfig = config;

		peerSet = PeerSet.getInstance();
		for (String s : config.getPeerAddrs()) {
			Peer peer = new Peer(s);
			peerSet.addPeer(peer);
			if (s.equals("localhost:" + config.getSelfPort())) {
				peerSet.setSelf(peer);
			}
		}

		rpcServerImpl = new RpcServerImpl(config.selfPort, this);
	}
	@Override
	public void init() throws Throwable {
		if (started) {
			return;
		}
		synchronized (this)	{
			if (started) {
				return;
			}
			/** 启用RPC Server 监听RPC请求（客户端、其他节点） */
			rpcServerImpl.start();
			
			/** 初始化一致性处理模块、日志存储模块 */
			consensusImpl = new ConsensusImpl(this);
			logModuleImpl = new LogModuleImpl(this.getPeerSet().getSelf().getAddr());
			stateMachine = new StateMachineImpl(this.getPeerSet().getSelf().getAddr());
			// 采用文件实现
			//logModuleImpl = LogModuleImpl.getLogModuleInstance();
			//stateMachine = ...
			// 采用redis实现

			/** 初始化相关时间参数 */
			preElectionTime = System.currentTimeMillis();
			preHeartBeatTime = System.currentTimeMillis();
			
			/** 交由线程池定期执行心跳任务、选举任务 */
			RaftThreadPool.scheduleWithFixedDelay(heartBeatTask, 500);		
			RaftThreadPool.scheduleAtFixedRate(electionTask, 1000, 2000);
			
			/** 如果是宕机恢复，当前任期为之前的最后一条日志的任期号 */
			LogEntry logEntry = logModuleImpl.getLast();
			if(logEntry != null)
				this.setCurrentTerm(logEntry.getTerm());
			
			started = true;	//表示已经开始了
		}
	}
	@Override
	public void destroy() throws Throwable {
		// TODO Auto-generated method stub
		rpcServerImpl.stop();
	}


	/** ==============INode方法============== */
	@Override
	public RvoteResult handlerRequestVote(RvoteParam param) {
		return consensusImpl.requestVote(param);
	}
	@Override
	public AentryResult handlerAppendEntries(AentryParam param)	{
		return consensusImpl.appendEntries(param);
	}

	/**
	 * 选举线程 角色从跟随者转为候选人以后，开始选举过程 
	 * 1. 自增自己当前的任期号 
	 * 		给自己投票 
	 * 		重置选举超时计时器 
	 * 		发送请求投票RPC给其他的服务器 
	 * 2. 如果接收到大多数服务器的选票，就变成领导人 
	 * 3. 如果接收到新的领导人的附加日志RPC 转换成跟随者
	 * 4. 如果选举过程超时，再发起一次选举
	 */
	class ElectionTask implements Runnable {
		@Override
		public void run() {
			/** 如果当前已经是leader 就不需要执行选举操作 */
			if (status == NodeStatus.LEADER) {
				return;
			}

			/** 等待一个随机超时时间 */
			long current = System.currentTimeMillis();
			long currentElectionTime  = electionTime + ThreadLocalRandom.current().nextInt(500);
			if (current - preElectionTime < currentElectionTime) {
				return;
			}

			/** 开始选举 */
			// 改变当前节点角色为候选人 FOLLOWER -> CANDIDATE
			status = NodeStatus.CANDIDATE;
//			LOGGER.info("node {} will become CANDIDATE and start election leader,current term : [{}] ",
//					peerSet.getSelf(),
//					currentTerm);
			System.out.println("当前节点 " + peerSet.getSelf() +	" 满足成为候选人条件，成为候选人，当前任期是: " + currentTerm);

			// 更新上一次选举时间 重置超时时间
			preElectionTime = currentElectionTime;

			// 更新任期号，给自己投一票
			currentTerm = currentTerm + 1;
			votedFor = peerSet.getSelf().getAddr();

			// 向所有的节点发送投票请求RPC
			List<Peer> peers = peerSet.getPeersWithoutSelf();
			ArrayList<Future> futures = new ArrayList<>();
			for (Peer peer : peers)	{
				futures.add(RaftThreadPool.submit(new Callable() {
					@Override
					public Object call() throws Exception {
						Long lastTerm = 0L;
						LogEntry lastLogEntry = logModuleImpl.getLast();

						if(lastLogEntry != null) {
							lastTerm = lastLogEntry.getTerm();
						}

						// 请求投票RPC参数
						RvoteParam param = RvoteParam.newBuilder()
								.term(currentTerm)
								.candidateId(peerSet.getSelf().getAddr())
								.lastLogIndex(logModuleImpl.getLastIndex())
								.lastLogTerm(lastTerm)
								.build();

						// 创建rpc请求
						Request request = Request.newBuilder()
								.cmd(Request.R_VOTE)	// 类型
								.obj(param)				// 内容
								.url(peer.getAddr())	// 发送的对象
								.build();
						try	{
							Response<RvoteResult> response = rpcClientImpl.send(request);
							return response;
						} 
						catch (Exception e)	{
//							LOGGER.error("ElectionTask RPC Failed , URL : " + request.getUrl());
							System.out.println("远程投票RPC失败 , 失败的节点URL : " + request.getUrl());
							return null;
						}
					}
				}));
			}

			AtomicInteger successCount = new AtomicInteger(0);	// 记录成功数目
			CountDownLatch latch = new CountDownLatch(futures.size());

			// 处理结果
			for(Future future : futures) {
				RaftThreadPool.submit(new Callable() {
					@Override
					public Object call() throws Exception {
						try	{
							Response<RvoteResult> response = (Response<RvoteResult>)future.get(150, TimeUnit.MILLISECONDS);

							// 0.1s内没有收到结果
							if (response == null) {
								return -1;
							}
							
							// 判断投票结果是否是自己
							/**
							 * 如果没有投给自己判断任期，如果任期比自己大，说明自己不配，变为FOLLOWER
							 * 否则就说明对方的日志比自己新或者对方已经被别人预约了或者对方已经投票了
							 */
							boolean isVoteGranted = response.getResult().isVoteGranted();
							if(isVoteGranted) {
								successCount.incrementAndGet();
							}
							else {
								// 发现对方的任期比自己大，立刻变回FOLLOWER
								long resTerm = response.getResult().getTerm();
								if(resTerm > currentTerm) {
									currentTerm = resTerm;
									status = NodeStatus.FOLLOWER;
								}
							}
							return 0;
						} 
						catch (Exception e)	{
//							LOGGER.error("future.get Exception , e : " ,e);
							System.out.println("选举任务结果获取失败，错误原因 : " + e.getMessage());
							return -1;
						}
						finally {
							// 不管是不是成功 都要在finally中执行该语句，不然会出现阻塞
							latch.countDown();
						}
					}
				});
			}
			
			try {
				// 如果0.15s之内没有处理完所有节点就直接强制中止
				latch.await(150,TimeUnit.MILLISECONDS);
			}
			catch (Exception e)	{
//				LOGGER.error("Interrupted by master election task");
				System.out.println("Interrupted by master election task");
			}
			
			int success = successCount.get();
//			LOGGER.info("node {} maybe become leader , success count = {} , status : {}",
//					peerSet.getSelf(),
//					success,
//					NodeStatus.Enum.value(status));

			System.out.println("当前节点 [" + peerSet.getSelf() +"] 获得的票数 = " + success + " , 当前节点状态 : " +
							NodeStatus.Enum.value(status));

			/** 
			 * 所有的投票都结束了，有三种可能 
			 *	1. 当选
			 *	2. 投票瓜分
			 *	3. 其他人成为Leader，这个时候本节点会自动变成Follower
			 *		这时候就要停止过程 
			 */
			// 如果投票期间,有其他合法的Leader(在RPCServer.hangdlerRequest()中处理的req) 发送appendEntry, 就可能变成 follower
			if(status == NodeStatus.FOLLOWER) {
				return;
			}
			
			if(success >= peers.size() / 2)	{
//				LOGGER.warn("node {} become Leader" ,peerSet.getSelf());
				System.out.println("当前节点 [" + peerSet.getSelf() + "] 成为 Leader" );
				status = NodeStatus.LEADER;
				peerSet.setLeader(peerSet.getSelf());
				votedFor = "";
				// 成为Leader后立刻执行一些任务
				becomeLeaderTodo();
			}
			else {
				// 到这里既没自己成为leader，也不存在新的leader 就说明选票被瓜分了，重新选举
				votedFor = "";
			}
		}
	}
	
	/**
	 * 成为Leader后要做的事
	 * 1. 立刻执行一次心跳任务，结束其他节点无用的选举任务
	 * 2. 初始化所有的nextIndex值为 （自己的最后一条日志的index） + 1
	 * Leader尝试递减nextIndex，使得与Follower达成一致
	 */
	public void becomeLeaderTodo() {
		/** 立刻执行心跳命令来稳定军心 */
		RaftThreadPool.execute(new HeartBeatTask(), true);	
//		LOGGER.info(getPeerSet().getSelf().getAddr() + " become the leader and run the heartBeat task Immediately");
		System.out.println(getPeerSet().getSelf().getAddr() + " 成为Leader，并且立刻执行心跳任务");

		/** 节点选取成为Leader后，第一件事初始化每一个节点的匹配日志 */
		nextIndexs = new ConcurrentHashMap<>();
		matchIndexs = new ConcurrentHashMap<>();
		for(Peer peer : peerSet.getPeersWithoutSelf()) {
			nextIndexs.put(peer,logModuleImpl.getLastIndex()+1);
			matchIndexs.put(peer,0L);
		}
	}
	
	/** 心跳线程 */
	class HeartBeatTask implements Runnable {
		@Override
		public void run() {
			if(status != NodeStatus.LEADER)	{
				return ;
			}
			long current = System.currentTimeMillis();
			if(current - preHeartBeatTime < heartBeatTick) {
				return;
			}

			preHeartBeatTime = current;
			LOGGER.info("开始执行心跳任务");

			/** 给每一个小朋友发送心跳包 */
			for(Peer peer : peerSet.getPeersWithoutSelf()) {
				// 心跳包参数
				AentryParam param = AentryParam.newBuilder()
						.entries(null)
						.leaderId(peerSet.getSelf().getAddr())	// 领导人id用于重定向
						.serverId(peer.getAddr())				// 被请求者ip地址
						.term(currentTerm)						// 领导人的term
						.build();

				// request 参数构造
				Request<AentryParam> request = Request.newBuilder()
						.cmd(Request.A_ENTRIES)
						.obj(param)
						.url(peer.getAddr())
						.build();
				
				RaftThreadPool.execute( () -> {
					try	{
						Response response = rpcClientImpl.send(request);
						AentryResult result = (AentryResult) response.getResult();
						long term = result.getTerm();
						
						// 如果返回消息中的term 大于当前leader的term说明当前节点已经out
						if(term > currentTerm) {
							currentTerm = term;
							votedFor = "";
							status = NodeStatus.FOLLOWER;
						}
					} 
					catch (Exception e)	{
						e.printStackTrace();
					}
				},false);
			}
		}	
	}


	/**
	 * 处理客户端请求
	 * 每个节点都可能会接收到客户端的请求，但只有 leader 能处理，所以如果自身不是 leader，则需要转发给 leader。
	 * 然后将用户的 KV 数据封装成日志结构，包括 term，index，command，预提交到本地。
	 * 并行的向其他节点发送数据，也就是日志复制。
	 * 如果在指定的时间内，过半节点返回成功，那么就提交这条日志。
	 * 最后，更新自己的 commitIndex，lastApplied 等信息。
	 * @param
	 * @return
	 */
	@Override
	public synchronized ClientKVAck handlerClientRequest(ClientKVReq request) {
//		LOGGER.warn("handlerClientRequest handler {} operation,  and key : [{}], value : [{}]",
//				ClientKVReq.Type.value(request.getType()), request.getKey(), request.getValue());

		System.out.println("------------------------------------------------------------------------");
		String cmd = request.getType() == ClientKVReq.GET ? "读取内容" : "写入内容";
		System.out.println("接收到客户端[" + cmd + "]请求");

		// 如果当前节点不是Leader 将请求重定向到Leader
		if(status != NodeStatus.LEADER)
		{
			System.out.println("当前节点[" + this.getPeerSet().getSelf().getAddr() + "" +
					"] 不是leader，将任务转发至leader [ " + getPeerSet().getLeader().getAddr() + "] ");
			System.out.println("########################################################################");
			return redirect(request);
		}

		/** 客户端请求只是简单的获取机器人的状态 */
		if(request.getType() == ClientKVReq.GET)
		{
			String key = request.getKey();

			System.out.println("要读取的key 是 [ " + key + "] ");
			if(StringUtil.isNullOrEmpty(key))
			{
				//返回所有的状态
				System.out.println("########################################################################");
				return new ClientKVAck(null);
			}

			// 获取状态机的一条
			LogEntry logEntry = stateMachine.get(key);
			if(logEntry != null)
			{
				System.out.println("########################################################################");
				return new ClientKVAck(logEntry.getCommand());
			}
			System.out.println("########################################################################");
			return new ClientKVAck(null);
		}

		/** 当前请求是PUT，生成请求日志 */
		LogEntry logEntry = LogEntry.newBuilder()
				.command(Command.newBuilder()
						.key(request.getKey())
						.value(request.getValue())
						.build())
				.term(currentTerm)
				.build();

		// 先把最新的日志写进logDB中 预提交 这个方法会修改logEntry的索引号
		logModuleImpl.write(logEntry);
//		LOGGER.info("write logModule success, logEntry info : {}, log index : {}", logEntry, logEntry.getIndex());
		System.out.println("write logModule success, logEntry info : " + logEntry + ", log index : " + logEntry.getIndex());

		final AtomicInteger success = new AtomicInteger(0);
		List<Future<Boolean>> futureList = new CopyOnWriteArrayList<>();

		int count = 0;
		for(Peer peer : peerSet.getPeersWithoutSelf())
		{
			count++;
			futureList.add(replication(peer,logEntry));
		}

		CountDownLatch countDownLatch = new CountDownLatch(futureList.size());
		List<Boolean> resultList = new CopyOnWriteArrayList<>();
		getRPCAppendResult(futureList,countDownLatch,resultList);

		// 等待所有结果，最多等待200ms
		try {
			countDownLatch.await(2000,TimeUnit.MILLISECONDS);
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}

		for(Boolean flag : resultList)
		{
			if(flag)
			{
				success.incrementAndGet();
			}
		}

		/**
		 * 如果存在一个N，使得N > commitIndex，并且大多数的matchIndex[i] >= N 成立
		 * 并且log[N].term == currentTerm 成立，那么令commitIndex 等于这个N
		 */
		List<Long> matchIndexList = new ArrayList<>(matchIndexs.values());
		// 小于2 没有意义
		int median = 0;
		if(matchIndexList.size() >= 2)
		{
			Collections.sort(matchIndexList);
			median = matchIndexList.size() / 2;
		}
		Long N = matchIndexList.get(median);
		if(N > commitIndex)
		{
			LogEntry entry = logModuleImpl.read(N);
			if(entry != null && entry.getTerm() == currentTerm)
			{
				commitIndex = N;
			}
		}

		if(success.get() >= (count / 2))
		{
			// 更新
			commitIndex = logEntry.getIndex();
			// 应用到状态机
			stateMachine.apply(logEntry);
			lastApplied = commitIndex;

//			LOGGER.info("success apply local state machine,  logEntry info : {}", logEntry);
			System.out.println("success apply local state machine , logEntry info : " + logEntry);
			// 返回成功.
			System.out.println("########################################################################");
			return ClientKVAck.ok();
		}
		else
		{
			logModuleImpl.removeOnStartIndex(logEntry.getIndex());
			LOGGER.warn("fail apply local state  machine,  logEntry info : {}", logEntry);
			// TODO 不应用到状态机,但已经记录到日志中.由定时任务从重试队列取出,然后重复尝试,当达到条件时,应用到状态机.
			// 这里应该返回错误, 因为没有成功复制过半机器.
			System.out.println("########################################################################");
			return ClientKVAck.fail();
		}
	}

	/**
	 * 	如果当前节点不是Leader 就 重定向交由Leader处理
	 */
	@Override
	public ClientKVAck redirect(ClientKVReq request) {
		Request<ClientKVReq> r = Request.newBuilder()
				.obj(request)
				.url(peerSet.getLeader().getAddr())
				.cmd(Request.CLIENT_REQ)
				.build();
		Response response = rpcClientImpl.send(r);
		return (ClientKVAck)response.getResult();
	}

	/**
	 * 将日志复制给peer
	 * @param peer
	 * @param logEntry
	 * @return
	 */
	public Future<Boolean> replication(Peer peer ,LogEntry logEntry)
	{
		return RaftThreadPool.submit(new Callable()
		{
			 @Override
			 public Object call() throws Exception
			 {
			 	long start = System.currentTimeMillis(),end = start;
			 	// 失败重试1s
			 	while(end - start < 500)
				{
					// 构建附加日志参数
					AentryParam aentryParam = AentryParam.newBuilder()
							.term(currentTerm)
							.serverId(peer.getAddr())
							.leaderId(peerSet.getSelf().getAddr())
							.leaderCommit(commitIndex)
							.build();

					// 以我这边为准，通常成为Leader后首次进行RPC才有意义
					Long nextIndex = nextIndexs.get(peer);
					LinkedList<LogEntry> logEntries = new LinkedList<>();
					if(logEntry.getIndex() >= nextIndex)
					{
						//要发送的最新的日志的索引大于当前该设备所支持的最后一条，即设备最后一条不够新
						for(long i=nextIndex; i<=logEntry.getIndex();i++)
						{
							LogEntry tempLogEntry = logModuleImpl.read(i);
							if(tempLogEntry != null)
							{
								if(tempLogEntry != null)
								{
									logEntries.add(tempLogEntry);
								}
							}
						}
					}
					else // logEntry.index < nextIndex	只添加当前一条
					{
						logEntries.add(logEntry);
					}
					// 找到要发送的最小的日志的前一条日志
					LogEntry prevLog = getPreLog(logEntries.getFirst());

					aentryParam.setPrevLogTerm(prevLog.getTerm());
					aentryParam.setPrevLogIndex(prevLog.getIndex());
					aentryParam.setEntries(logEntries.toArray(new LogEntry[0]));

					Request request = Request.newBuilder()
							.cmd(Request.A_ENTRIES)
							.obj(aentryParam)
							.url(peer.getAddr())
							.build();

					try
					{
						Response response = rpcClientImpl.send(request);
						if(response == null)
						{
							return false;
						}
						AentryResult result = (AentryResult) response.getResult();
						if(result != null && result.isSuccess())
						{
//							LOGGER.info("append follower entry success , follower = [{}],entry = [{}]",
//							peer,aentryParam.getEntries());
							System.out.println("append follower entry success , the follower is [" + peer +
									"], the entry is "+
									aentryParam.getEntries());
							nextIndexs.put(peer,logEntry.getIndex() + 1);
							matchIndexs.put(peer,logEntry.getIndex());
							return true;
						}

						else if(result != null)
						{
							// 对方比我大
							if(result.getTerm() > currentTerm)
							{
//								LOGGER.warn("follower [{}] term [{}] is more than self,and my term is [{}] , " +
//												"so, I will become follower",
//										peer,result.getTerm(),currentTerm);
								System.out.println("the follower [" + peer + "]'s term " + result.getTerm() +
										" is more than myself," +
										",my term is " + currentTerm + " ,so I will become follower");
								currentTerm = result.getTerm();
								// 认怂变成follower
								status = NodeStatus.FOLLOWER;
								return false;
							}
							// term 没有我大却失败了，说明index不对或者term不对
							else
							{
								// 递减
								if(nextIndex == 0)
								{
									nextIndex = 1L;
								}
								nextIndexs.put(peer,nextIndex - 1);
								LOGGER.warn("follower {} nextIndex not match, will reduce nextIndex and retry RPC append, " +
												"nextIndex : [{}]", peer.getAddr(),
										nextIndex);
								System.out.println("follower [" + peer.getAddr() + "] nextIndex not match,will reduce nextIndex" +
										"and retry RPC append,nextIndex : [" + nextIndex + " ]");
								// 重新再来，直到成功为止
							}
						}
						end = System.currentTimeMillis();
					}
					catch (Exception e)
					{
//						LOGGER.warn(e.getMessage());
						e.printStackTrace();
						return false;
					}
				}
			 	// 超时了没办法了
			 	return false;
			 }
		}
		);
	}

	private LogEntry getPreLog(LogEntry logEntry) {
		LogEntry entry = logModuleImpl.read(logEntry.getIndex() - 1);

		if(entry == null) {
			LOGGER.warn("get preLog is null , parameter logEntry : {}",logEntry);
			entry = LogEntry.newBuilder()
					.index(0L)
					.term(0)
					.command(null)
					.build();
		}

		return entry;
	}

	/**
	 * 	被handlerClientRequest 调用
	 *	处理附加日志的结果，leader将客户端的kv请求搞成一个日志分发到各个节点上
	 *	处理的结果由这个函数处理
	 */
	private void getRPCAppendResult(List<Future<Boolean>> futureList,CountDownLatch countDownLatch,List<Boolean> resultList) {
		for(Future<Boolean> future : futureList) {
			RaftThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					try	{
						resultList.add(future.get(500,TimeUnit.MILLISECONDS));	// 等待结果
					}
					catch (CancellationException | TimeoutException | ExecutionException | InterruptedException e) {
						e.printStackTrace();
						resultList.add(false);
					}
					finally {
						countDownLatch.countDown();
					}
				}
			});
		}
	}
}
