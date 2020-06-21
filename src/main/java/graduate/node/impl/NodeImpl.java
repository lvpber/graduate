package graduate.node.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.security.ntlm.Client;
import graduate.model.clientmodel.ClientKVAck;
import graduate.model.clientmodel.ClientKVReq;
import graduate.model.logmodulemodel.Command;
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
import sun.rmi.runtime.Log;

/**
 * 机器节点 指代raspberry
 */
public class NodeImpl implements INode, ILifeCycle
{
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeImpl.class);

	/** 选举超时时间 10s 后面要改，这个时间太长了 */
	private volatile long electionTime = 5 * 1000;
	/** 上一次选举时间戳 */
	private volatile long preElectionTime;
	/** 上一次心跳时间戳 */
	private volatile long preHeartBeatTime;
	/** 心跳间隔时间 5s时间太长 */
	private final long heartBeatTick = 5 * 100;

	/** 两个任务 心跳和选举任务 */
	private HeartBeatTask heartBeatTask = new HeartBeatTask();
	private ElectionTask electionTask = new ElectionTask();
	
	private volatile int status = NodeStatus.FOLLOWER;

	private PeerSet peerSet;

	/** ===========所有服务器上持久存在的========== */
	private volatile long currentTerm = 0;
	private volatile String votedFor;

	/** ===========所有服务器上经常变的 =========== */
	private volatile long commitIndex;
	private volatile long lastApplied = 0;

	/** ===========领导人上经常变的============== */
	private Map<Peer, Long> nextIndexs;
	private Map<Peer, Long> matchIndexs;
	
	/** ==================================== */
	private NodeConfig nodeConfig;
	private static RpcServerImpl rpcServerImpl;
	private RpcClientImpl rpcClientImpl = new RpcClientImpl();
	private volatile boolean started; // 记录当前节点是否已经启动

	private ConsensusImpl consensusImpl;		//一致性模块
	private LogModuleImpl logModuleImpl;		//日志模块
	
	public long getPreElectionTime()
	{
		return preElectionTime;
	}

	public void setPreElectionTime(long preElectionTime)
	{
		this.preElectionTime = preElectionTime;
	}

	public long getPreHeartBeatTime()
	{
		return preHeartBeatTime;
	}

	public void setPreHeartBeatTime(long preHeartBeatTime)
	{
		this.preHeartBeatTime = preHeartBeatTime;
	}

	public int getStatus()
	{
		return status;
	}

	public void setStatus(int status)
	{
		this.status = status;
	}

	public PeerSet getPeerSet()
	{
		return peerSet;
	}

	public void setPeerSet(PeerSet peerSet)
	{
		this.peerSet = peerSet;
	}

	public long getCurrentTerm()
	{
		return currentTerm;
	}

	public void setCurrentTerm(long currentTerm)
	{
		this.currentTerm = currentTerm;
	}

	public String getVotedFor()
	{
		return votedFor;
	}

	public void setVotedFor(String votedFor)
	{
		this.votedFor = votedFor;
	}

	/** ========静态内部类实现单例模式多线程安全======= */
	private NodeImpl(){}

	private static class NodeLazyHolder
	{
		private static final NodeImpl instance = new NodeImpl();
	}

	public static NodeImpl getInstance()
	{
		return NodeLazyHolder.instance;
	}

	/** ===========LifeCycle方法============= */
	@Override
	public void init() throws Throwable
	{
		// TODO Auto-generated method stub
		if (started)
		{
			return;
		}
		synchronized (this)
		{
			if (started)
			{
				return;
			}
			/** 启用RPC Server 监听RPC请求（客户端、其他节点） */
			rpcServerImpl.start(); // 启动rpc server监听rpc请求
			
			/** 初始化一致性处理模块、日志存储模块 */
			consensusImpl = new ConsensusImpl(this);	
			logModuleImpl = LogModuleImpl.getLogModuleInstance();
			
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
	public void destroy() throws Throwable
	{
		// TODO Auto-generated method stub
		rpcServerImpl.stop();
	}

	@Override
	public void setConfig(NodeConfig config)
	{
		// TODO Auto-generated method stub
		this.nodeConfig = config;

		peerSet = PeerSet.getInstance();
		for (String s : config.getPeerAddrs())
		{
			Peer peer = new Peer(s);
			peerSet.addPeer(peer);
			if (s.equals("localhost:" + config.getSelfPort()))
			{
				peerSet.setSelf(peer);
			}
		}

		rpcServerImpl = new RpcServerImpl(config.selfPort, this);
	}

	@Override
	public RvoteResult handlerRequestVote(RvoteParam param)
	{
		// TODO Auto-generated method stub
//		LOGGER.warn("handlerRequestVote will be invoke , param info : {}" ,param);
		return consensusImpl.requestVote(param);
	}

	@Override
	public AentryResult handlerAppendEntries(AentryParam param)
	{
		// TODO Auto-generated method stub
		return consensusImpl.appendEntries(param);
	}

	/**
	 * 选举线程 角色从跟随者转为候选人以后，开始选举过程 
	 * 1. 自增自己当前的任期号 
	 * 		给自己投票 
	 * 		重置选举超时计时器 
	 * 		发送请求投票RPC给其他的服务器 
	 * 2. 如果接收到大多数服务器的选票，就变成领导人 
	 * 3. 如果接收到新的领导人的附加日志RPC 转换成跟随者 (先不实现) 
	 * 4. 如果选举过程超时，再发起一次选举
	 */
	class ElectionTask implements Runnable
	{

		@Override
		public void run()
		{
			// TODO Auto-generated method stub
			/** 如果当前已经是leader 就不需要执行选举操作 */
			if (status == NodeStatus.LEADER)
			{
				return;
			}

			/** 获取当前时间 */
			long current = System.currentTimeMillis();

			/** 如果在一段时间内没有接收到任何请求，就会发起选举，当接收到心跳后，会重置时间 */
			long currentElectionTime  = electionTime + ThreadLocalRandom.current().nextInt(50);
			System.out.println("距离上一次选举任务已经过去时间 " + (current - preElectionTime));
			if (current - preElectionTime < currentElectionTime)
			{
				System.out.println("等待时间不够，继续等待");
				return;
			}

			/** FOLLOWER -> CANDIDATE */
			status = NodeStatus.CANDIDATE;
//			LOGGER.error("node {} will become CANDIDATE and start election leader,current term : [{}] ",	peerSet.getSelf(), currentTerm);
			System.out.println("node" +  peerSet.getSelf() + " will become CANDIDATE and "
					+ "start election leader,current term : [" + currentTerm +"]");
			/** 将当前时间置为上一次选举时间 */
			preElectionTime = System.currentTimeMillis() + 
					ThreadLocalRandom.current().nextInt(200) + 150;

			/** 更新任期号，给自己投一票 */
			currentTerm = currentTerm + 1;
			votedFor = peerSet.getSelf().getAddr();

			/** 接下来向所有的伙伴发送投票请求RPC */
			List<Peer> peers = peerSet.getPeersWithoutSelf();
			ArrayList<Future> futures = new ArrayList<>();

			/**
			 * 将请求投票的内容交给线程池处理，然后得到一个future对象
			 */
			for (Peer peer : peers)
			{
				futures.add(RaftThreadPool.submit(new Callable()
				{

					@Override
					public Object call() throws Exception
					{
						// TODO Auto-generated method stub

						/** 
						 * 	创建请求投票RPC的请求内容 
						 *	先不做最后的日志这种东西 
						 */
						RvoteParam param = RvoteParam.newBuilder()
								.term(currentTerm)
								.candidateId(peerSet.getSelf().getAddr())
								.build();

						/** 创建rpc请求 */
						Request request = Request.newBuilder()
								.cmd(Request.R_VOTE)
								.obj(param)
								.url(peer.getAddr())
								.build();

						try
						{
							Response<RvoteResult> response = rpcClientImpl.send(request);
							return response;
						} 
						catch (Exception e)
						{
							// TODO: handle exception
//							LOGGER.error("ElectionTask RPC Failed , URL : " + request.getUrl());
							System.out.println("ElectionTask RPC Failed URL : " + request.getUrl());
							return null;
						}
					}
				}));
			}
			
			/** 因为这里有多个请求投票线程，所以要线程安全 */
			AtomicInteger successCount = new AtomicInteger(0);
			
			/**
			 * CountDownLatch 这个类会阻塞当前线程，等待其他线程各自执行完毕之后
			 * 再执行当前线程
			 * 假设存在一个线程池 pool，里面存在10个线程 t1,t2,t3...,t10
			 * CountDownLatch latch = new CountDownLatch(10);
			 * 
			 * foreach t:
			 * 		t.run()
			 * 		{
			 * 			do thread;
			 * 			countDownLatch.countDown
			 * 		}
			 * 
			 * latch.await()	这里会阻塞等待所有线程完成
			 * t11.run  回到之前的线程继续执行
			 */
			CountDownLatch latch = new CountDownLatch(futures.size());
			
			/**
			 * 对投票的现成的future对象用多线程进行后续的处理
			 */
			for(Future future : futures) 
			{
				RaftThreadPool.submit(new Callable()
				{
					@Override
					public Object call() throws Exception
					{
						// TODO Auto-generated method stub
						try
						{
							Response<RvoteResult> response = 
									(Response<RvoteResult>)future.get(100, TimeUnit.MILLISECONDS);
							/** 如果0.1秒内没有得到消息 */
							if (response == null)
							{
								return -1;
							}
							
							/** 判断返回结果是不是选自己 */
							boolean isVoteGranted = response.getResult().isVoteGranted();
							if(isVoteGranted)
							{
								successCount.incrementAndGet();
							}
							else 
							{
								long resTerm = response.getResult().getTerm();
								if(resTerm > currentTerm)
								{
									currentTerm = resTerm;
								}
							}
							return 0;
						} 
						catch (Exception e)
						{
							// TODO: handle exception
							LOGGER.error("future.get Exception , e : " ,e);
							return -1;
						}
						finally 
						{
							/** 不管是不是成功 都要在finally中执行该语句，不然会出现阻塞  */
							latch.countDown();
						}
					}
				});
			}
			
			try
			{
				/** 给3.5s时间，如果改时间内没确定leader就强行终止 */
				latch.await(150,TimeUnit.MILLISECONDS);
			} 
			catch (Exception e)
			{
				// TODO: handle exception
				LOGGER.error("Interrupted by master election task");
			}
			
			int success = successCount.get();
			/** 
			 * 所有的投票都结束了，有三种可能 
			 *	1. 当选
			 *	2. 投票瓜分
			 *	3. 其他人成为Leader，这个时候本节点会自动变成Follower
			 *		这时候就要停止过程 
			 */
			
			if(status == NodeStatus.FOLLOWER)
			{
				return ;
			}
			
			if(success > peers.size() / 2)
			{
				LOGGER.warn("node {} become Leader" ,peerSet.getSelf());
				System.out.println("node " + peerSet.getSelf() + "become Leader");
				status = NodeStatus.LEADER;
				peerSet.setLeader(peerSet.getSelf());
				votedFor = "";
				/** 这里需要发送心跳任务  */
				// todo sth when become Leader
			}
			else
			{
				/** 到这里既没自己成为leader，也不存在新的leader 就说明选票被瓜分了，重新选举 */
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
	public void becomeLeaderTodo()
	{
		/** 稳定军心 */
		RaftThreadPool.execute(new HeartBeatTask(), true);	
		System.out.println("become Leader and run the heartBeatTask immiediately");
		//在leader里搞点日志，然后看看能不能搞到其他的节点中

		/** 节点选取成为Leader后，第一件事初始化每一个节点的匹配日志 */
		nextIndexs = new ConcurrentHashMap<>();
		matchIndexs = new ConcurrentHashMap<>();
		for(Peer peer : peerSet.getPeersWithoutSelf())
		{
			nextIndexs.put(peer,logModuleImpl.getLastIndex()+1);
			matchIndexs.put(peer,0L);
		}
	}
	
	/**
	 *	只有Leader才有执行这个线程的权力 
	 */
	class HeartBeatTask implements Runnable 
	{
		@Override
		public void run()
		{
			if(status != NodeStatus.LEADER)
			{
				return ;
			}
			
			long current = System.currentTimeMillis();
			if(current - preHeartBeatTime < heartBeatTick)
			{
				return;
			}

//			System.out.println("执行心跳任务");
			/** 把当前的时间作为上一次的心跳时间 */
			preHeartBeatTime = System.currentTimeMillis();
			
			/** 给每一个小朋友发送心跳包 */
			for(Peer peer : peerSet.getPeersWithoutSelf())
			{
				/**
				 * 构造心跳RPC参数
				 */
				AentryParam param = AentryParam.newBuilder()
						.entries(null)
						.leaderId(peerSet.getSelf().getAddr())	//领导人id用于重定向
						.serverId(peer.getAddr())				//被请求者ip地址
						.term(currentTerm)
						.build();
				/**
				 * 构造RPC请求
				 */
				Request<AentryParam> request = new Request<AentryParam>(
						Request.A_ENTRIES,
						param,
						peer.getAddr()
						);
				
				RaftThreadPool.execute( () ->
				{
					try
					{
						Response response = rpcClientImpl.send(request);
						AentryResult result = (AentryResult) response.getResult();
						long term = result.getTerm();
						
						/** 如果返回消息中的term 大于当前leader的term说明当前节点已经out */
						if(term > currentTerm)
						{
							currentTerm = term;
							votedFor = "";
							status = NodeStatus.FOLLOWER;
						}
					} 
					catch (Exception e)
					{
						// TODO: handle exception
					}
				},false);
			}
		}	
	}


	/**
	 *	处理客户端请求
	 *	这里为什么要加synchronized
	 */
	@Override
	public synchronized ClientKVAck handlerClientRequest(ClientKVReq request) {
		/** 如果当前节点不是Leader */
		if(status != NodeStatus.LEADER)
		{
			return redirect(request);
		}

		/** 客户端请求只是简单的获取机器人的状态 */
		if(request.getType() == ClientKVReq.GET)
		{
			if(request.getKey()==null || request.getKey().equals(""))
			{
				//返回所有的状态
			}

			// 获取状态机的一条
		}

		/** 当前请求是PUT，生成请求日志 */
		LogEntry logEntry = LogEntry.newBuilder()
				.command(Command.newBuilder()
						.key(request.getKey())
						.value(request.getValue())
						.build())
				.term(currentTerm)
				.build();

		// 先把最新的日志写进logDB中
		logModuleImpl.write(logEntry);

		final AtomicInteger success = new AtomicInteger(0);
		List<Future<Boolean>> futureList = new CopyOnWriteArrayList<>();

		int count = 0;
		for(Peer peer : peerSet.getPeersWithoutSelf())
		{
			count++;
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
			 	/** 在一段时间内失败重试 */
			 	while(end - start < 1 * 1000)
				{
					AentryParam aentryParam = new AentryParam.Builder()
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

							}
						}
					}


				}
			 	return null;
			 }
		}

		);
	}
}
