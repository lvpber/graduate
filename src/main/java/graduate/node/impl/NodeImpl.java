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
 * �����ڵ� ָ��raspberry
 */
@Setter
@Getter
public class NodeImpl implements INode, ILifeCycle
{
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeImpl.class);

	private volatile long electionTime  = 5 * 1000;		/** ѡ�ٳ�ʱʱ�� 10s ����Ҫ�ģ����ʱ��̫���� */
	private final 	 long heartBeatTick = 5 * 100;		/** �������ʱ�� 5sʱ��̫�� */
	private volatile long preElectionTime;				/** ��һ��ѡ��ʱ��� */
	private volatile long preHeartBeatTime;				/** ��һ������ʱ��� */

	/** �������� ������ѡ������ */
	private HeartBeatTask heartBeatTask = new HeartBeatTask();
	private ElectionTask  electionTask  = new ElectionTask();
	
	private volatile int status = NodeStatus.FOLLOWER;
	private PeerSet peerSet;

	/** ===========���з������ϳ־ô��ڵ�========== */
	private volatile long 	currentTerm = 0;
	private volatile String votedFor;

	/** ===========���з������Ͼ������ =========== */
	private volatile long commitIndex;
	private volatile long lastApplied = 0;

	/** ===========�쵼���Ͼ������============== */
	private Map<Peer, Long> nextIndexs;
	private Map<Peer, Long> matchIndexs;
	
	/** ==================================== */
	private 			NodeConfig 			nodeConfig;
	private static  	RpcServerImpl   	rpcServerImpl;
	private 			RpcClientImpl 		rpcClientImpl = new RpcClientImpl();
	private volatile 	boolean 			started; 							/** ��¼��ǰ�ڵ��Ƿ��Ѿ����� */
	private 			StateMachineImpl 	stateMachine;						/** ״̬��				 */

	private ConsensusImpl consensusImpl;		//һ����ģ��
	private LogModuleImpl logModuleImpl;		//��־ģ��

	/** ========��̬�ڲ���ʵ�ֵ���ģʽ���̰߳�ȫ======= */
	private NodeImpl(){}
	private static class NodeLazyHolder {
		private static final NodeImpl instance = new NodeImpl();
	}
	public static NodeImpl getInstance()
	{
		return NodeLazyHolder.instance;
	}


	/** ===========LifeCycle����============= */	// NodeBootStrap#main0() -> {setConfig(),init()}
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
			/** ����RPC Server ����RPC���󣨿ͻ��ˡ������ڵ㣩 */
			rpcServerImpl.start();
			
			/** ��ʼ��һ���Դ���ģ�顢��־�洢ģ�� */
			consensusImpl = new ConsensusImpl(this);
			logModuleImpl = new LogModuleImpl(this.getPeerSet().getSelf().getAddr());
			stateMachine = new StateMachineImpl(this.getPeerSet().getSelf().getAddr());
			// �����ļ�ʵ��
			//logModuleImpl = LogModuleImpl.getLogModuleInstance();
			//stateMachine = ...
			// ����redisʵ��

			/** ��ʼ�����ʱ����� */
			preElectionTime = System.currentTimeMillis();
			preHeartBeatTime = System.currentTimeMillis();
			
			/** �����̳߳ض���ִ����������ѡ������ */
			RaftThreadPool.scheduleWithFixedDelay(heartBeatTask, 500);		
			RaftThreadPool.scheduleAtFixedRate(electionTask, 1000, 2000);
			
			/** �����崻��ָ�����ǰ����Ϊ֮ǰ�����һ����־�����ں� */
			LogEntry logEntry = logModuleImpl.getLast();
			if(logEntry != null)
				this.setCurrentTerm(logEntry.getTerm());
			
			started = true;	//��ʾ�Ѿ���ʼ��
		}
	}
	@Override
	public void destroy() throws Throwable {
		// TODO Auto-generated method stub
		rpcServerImpl.stop();
	}


	/** ==============INode����============== */
	@Override
	public RvoteResult handlerRequestVote(RvoteParam param) {
		return consensusImpl.requestVote(param);
	}
	@Override
	public AentryResult handlerAppendEntries(AentryParam param)	{
		return consensusImpl.appendEntries(param);
	}

	/**
	 * ѡ���߳� ��ɫ�Ӹ�����תΪ��ѡ���Ժ󣬿�ʼѡ�ٹ��� 
	 * 1. �����Լ���ǰ�����ں� 
	 * 		���Լ�ͶƱ 
	 * 		����ѡ�ٳ�ʱ��ʱ�� 
	 * 		��������ͶƱRPC�������ķ����� 
	 * 2. ������յ��������������ѡƱ���ͱ���쵼�� 
	 * 3. ������յ��µ��쵼�˵ĸ�����־RPC ת���ɸ�����
	 * 4. ���ѡ�ٹ��̳�ʱ���ٷ���һ��ѡ��
	 */
	class ElectionTask implements Runnable {
		@Override
		public void run() {
			/** �����ǰ�Ѿ���leader �Ͳ���Ҫִ��ѡ�ٲ��� */
			if (status == NodeStatus.LEADER) {
				return;
			}

			/** �ȴ�һ�������ʱʱ�� */
			long current = System.currentTimeMillis();
			long currentElectionTime  = electionTime + ThreadLocalRandom.current().nextInt(500);
			if (current - preElectionTime < currentElectionTime) {
				return;
			}

			/** ��ʼѡ�� */
			// �ı䵱ǰ�ڵ��ɫΪ��ѡ�� FOLLOWER -> CANDIDATE
			status = NodeStatus.CANDIDATE;
//			LOGGER.info("node {} will become CANDIDATE and start election leader,current term : [{}] ",
//					peerSet.getSelf(),
//					currentTerm);
			System.out.println("��ǰ�ڵ� " + peerSet.getSelf() +	" �����Ϊ��ѡ����������Ϊ��ѡ�ˣ���ǰ������: " + currentTerm);

			// ������һ��ѡ��ʱ�� ���ó�ʱʱ��
			preElectionTime = currentElectionTime;

			// �������ںţ����Լ�ͶһƱ
			currentTerm = currentTerm + 1;
			votedFor = peerSet.getSelf().getAddr();

			// �����еĽڵ㷢��ͶƱ����RPC
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

						// ����ͶƱRPC����
						RvoteParam param = RvoteParam.newBuilder()
								.term(currentTerm)
								.candidateId(peerSet.getSelf().getAddr())
								.lastLogIndex(logModuleImpl.getLastIndex())
								.lastLogTerm(lastTerm)
								.build();

						// ����rpc����
						Request request = Request.newBuilder()
								.cmd(Request.R_VOTE)	// ����
								.obj(param)				// ����
								.url(peer.getAddr())	// ���͵Ķ���
								.build();
						try	{
							Response<RvoteResult> response = rpcClientImpl.send(request);
							return response;
						} 
						catch (Exception e)	{
//							LOGGER.error("ElectionTask RPC Failed , URL : " + request.getUrl());
							System.out.println("Զ��ͶƱRPCʧ�� , ʧ�ܵĽڵ�URL : " + request.getUrl());
							return null;
						}
					}
				}));
			}

			AtomicInteger successCount = new AtomicInteger(0);	// ��¼�ɹ���Ŀ
			CountDownLatch latch = new CountDownLatch(futures.size());

			// ������
			for(Future future : futures) {
				RaftThreadPool.submit(new Callable() {
					@Override
					public Object call() throws Exception {
						try	{
							Response<RvoteResult> response = (Response<RvoteResult>)future.get(150, TimeUnit.MILLISECONDS);

							// 0.1s��û���յ����
							if (response == null) {
								return -1;
							}
							
							// �ж�ͶƱ����Ƿ����Լ�
							/**
							 * ���û��Ͷ���Լ��ж����ڣ�������ڱ��Լ���˵���Լ����䣬��ΪFOLLOWER
							 * �����˵���Է�����־���Լ��»��߶Է��Ѿ�������ԤԼ�˻��߶Է��Ѿ�ͶƱ��
							 */
							boolean isVoteGranted = response.getResult().isVoteGranted();
							if(isVoteGranted) {
								successCount.incrementAndGet();
							}
							else {
								// ���ֶԷ������ڱ��Լ������̱��FOLLOWER
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
							System.out.println("ѡ����������ȡʧ�ܣ�����ԭ�� : " + e.getMessage());
							return -1;
						}
						finally {
							// �����ǲ��ǳɹ� ��Ҫ��finally��ִ�и���䣬��Ȼ���������
							latch.countDown();
						}
					}
				});
			}
			
			try {
				// ���0.15s֮��û�д��������нڵ��ֱ��ǿ����ֹ
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

			System.out.println("��ǰ�ڵ� [" + peerSet.getSelf() +"] ��õ�Ʊ�� = " + success + " , ��ǰ�ڵ�״̬ : " +
							NodeStatus.Enum.value(status));

			/** 
			 * ���е�ͶƱ�������ˣ������ֿ��� 
			 *	1. ��ѡ
			 *	2. ͶƱ�Ϸ�
			 *	3. �����˳�ΪLeader�����ʱ�򱾽ڵ���Զ����Follower
			 *		��ʱ���Ҫֹͣ���� 
			 */
			// ���ͶƱ�ڼ�,�������Ϸ���Leader(��RPCServer.hangdlerRequest()�д����req) ����appendEntry, �Ϳ��ܱ�� follower
			if(status == NodeStatus.FOLLOWER) {
				return;
			}
			
			if(success >= peers.size() / 2)	{
//				LOGGER.warn("node {} become Leader" ,peerSet.getSelf());
				System.out.println("��ǰ�ڵ� [" + peerSet.getSelf() + "] ��Ϊ Leader" );
				status = NodeStatus.LEADER;
				peerSet.setLeader(peerSet.getSelf());
				votedFor = "";
				// ��ΪLeader������ִ��һЩ����
				becomeLeaderTodo();
			}
			else {
				// �������û�Լ���Ϊleader��Ҳ�������µ�leader ��˵��ѡƱ���Ϸ��ˣ�����ѡ��
				votedFor = "";
			}
		}
	}
	
	/**
	 * ��ΪLeader��Ҫ������
	 * 1. ����ִ��һ���������񣬽��������ڵ����õ�ѡ������
	 * 2. ��ʼ�����е�nextIndexֵΪ ���Լ������һ����־��index�� + 1
	 * Leader���Եݼ�nextIndex��ʹ����Follower���һ��
	 */
	public void becomeLeaderTodo() {
		/** ����ִ�������������ȶ����� */
		RaftThreadPool.execute(new HeartBeatTask(), true);	
//		LOGGER.info(getPeerSet().getSelf().getAddr() + " become the leader and run the heartBeat task Immediately");
		System.out.println(getPeerSet().getSelf().getAddr() + " ��ΪLeader����������ִ����������");

		/** �ڵ�ѡȡ��ΪLeader�󣬵�һ���³�ʼ��ÿһ���ڵ��ƥ����־ */
		nextIndexs = new ConcurrentHashMap<>();
		matchIndexs = new ConcurrentHashMap<>();
		for(Peer peer : peerSet.getPeersWithoutSelf()) {
			nextIndexs.put(peer,logModuleImpl.getLastIndex()+1);
			matchIndexs.put(peer,0L);
		}
	}
	
	/** �����߳� */
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
			LOGGER.info("��ʼִ����������");

			/** ��ÿһ��С���ѷ��������� */
			for(Peer peer : peerSet.getPeersWithoutSelf()) {
				// ����������
				AentryParam param = AentryParam.newBuilder()
						.entries(null)
						.leaderId(peerSet.getSelf().getAddr())	// �쵼��id�����ض���
						.serverId(peer.getAddr())				// ��������ip��ַ
						.term(currentTerm)						// �쵼�˵�term
						.build();

				// request ��������
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
						
						// ���������Ϣ�е�term ���ڵ�ǰleader��term˵����ǰ�ڵ��Ѿ�out
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
	 * ����ͻ�������
	 * ÿ���ڵ㶼���ܻ���յ��ͻ��˵����󣬵�ֻ�� leader �ܴ���������������� leader������Ҫת���� leader��
	 * Ȼ���û��� KV ���ݷ�װ����־�ṹ������ term��index��command��Ԥ�ύ�����ء�
	 * ���е��������ڵ㷢�����ݣ�Ҳ������־���ơ�
	 * �����ָ����ʱ���ڣ�����ڵ㷵�سɹ�����ô���ύ������־��
	 * ��󣬸����Լ��� commitIndex��lastApplied ����Ϣ��
	 * @param
	 * @return
	 */
	@Override
	public synchronized ClientKVAck handlerClientRequest(ClientKVReq request) {
//		LOGGER.warn("handlerClientRequest handler {} operation,  and key : [{}], value : [{}]",
//				ClientKVReq.Type.value(request.getType()), request.getKey(), request.getValue());

		System.out.println("------------------------------------------------------------------------");
		String cmd = request.getType() == ClientKVReq.GET ? "��ȡ����" : "д������";
		System.out.println("���յ��ͻ���[" + cmd + "]����");

		// �����ǰ�ڵ㲻��Leader �������ض���Leader
		if(status != NodeStatus.LEADER)
		{
			System.out.println("��ǰ�ڵ�[" + this.getPeerSet().getSelf().getAddr() + "" +
					"] ����leader��������ת����leader [ " + getPeerSet().getLeader().getAddr() + "] ");
			System.out.println("########################################################################");
			return redirect(request);
		}

		/** �ͻ�������ֻ�Ǽ򵥵Ļ�ȡ�����˵�״̬ */
		if(request.getType() == ClientKVReq.GET)
		{
			String key = request.getKey();

			System.out.println("Ҫ��ȡ��key �� [ " + key + "] ");
			if(StringUtil.isNullOrEmpty(key))
			{
				//�������е�״̬
				System.out.println("########################################################################");
				return new ClientKVAck(null);
			}

			// ��ȡ״̬����һ��
			LogEntry logEntry = stateMachine.get(key);
			if(logEntry != null)
			{
				System.out.println("########################################################################");
				return new ClientKVAck(logEntry.getCommand());
			}
			System.out.println("########################################################################");
			return new ClientKVAck(null);
		}

		/** ��ǰ������PUT������������־ */
		LogEntry logEntry = LogEntry.newBuilder()
				.command(Command.newBuilder()
						.key(request.getKey())
						.value(request.getValue())
						.build())
				.term(currentTerm)
				.build();

		// �Ȱ����µ���־д��logDB�� Ԥ�ύ ����������޸�logEntry��������
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

		// �ȴ����н�������ȴ�200ms
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
		 * �������һ��N��ʹ��N > commitIndex�����Ҵ������matchIndex[i] >= N ����
		 * ����log[N].term == currentTerm ��������ô��commitIndex �������N
		 */
		List<Long> matchIndexList = new ArrayList<>(matchIndexs.values());
		// С��2 û������
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
			// ����
			commitIndex = logEntry.getIndex();
			// Ӧ�õ�״̬��
			stateMachine.apply(logEntry);
			lastApplied = commitIndex;

//			LOGGER.info("success apply local state machine,  logEntry info : {}", logEntry);
			System.out.println("success apply local state machine , logEntry info : " + logEntry);
			// ���سɹ�.
			System.out.println("########################################################################");
			return ClientKVAck.ok();
		}
		else
		{
			logModuleImpl.removeOnStartIndex(logEntry.getIndex());
			LOGGER.warn("fail apply local state  machine,  logEntry info : {}", logEntry);
			// TODO ��Ӧ�õ�״̬��,���Ѿ���¼����־��.�ɶ�ʱ��������Զ���ȡ��,Ȼ���ظ�����,���ﵽ����ʱ,Ӧ�õ�״̬��.
			// ����Ӧ�÷��ش���, ��Ϊû�гɹ����ƹ������.
			System.out.println("########################################################################");
			return ClientKVAck.fail();
		}
	}

	/**
	 * 	�����ǰ�ڵ㲻��Leader �� �ض�����Leader����
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
	 * ����־���Ƹ�peer
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
			 	// ʧ������1s
			 	while(end - start < 500)
				{
					// ����������־����
					AentryParam aentryParam = AentryParam.newBuilder()
							.term(currentTerm)
							.serverId(peer.getAddr())
							.leaderId(peerSet.getSelf().getAddr())
							.leaderCommit(commitIndex)
							.build();

					// �������Ϊ׼��ͨ����ΪLeader���״ν���RPC��������
					Long nextIndex = nextIndexs.get(peer);
					LinkedList<LogEntry> logEntries = new LinkedList<>();
					if(logEntry.getIndex() >= nextIndex)
					{
						//Ҫ���͵����µ���־���������ڵ�ǰ���豸��֧�ֵ����һ�������豸���һ��������
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
					else // logEntry.index < nextIndex	ֻ��ӵ�ǰһ��
					{
						logEntries.add(logEntry);
					}
					// �ҵ�Ҫ���͵���С����־��ǰһ����־
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
							// �Է����Ҵ�
							if(result.getTerm() > currentTerm)
							{
//								LOGGER.warn("follower [{}] term [{}] is more than self,and my term is [{}] , " +
//												"so, I will become follower",
//										peer,result.getTerm(),currentTerm);
								System.out.println("the follower [" + peer + "]'s term " + result.getTerm() +
										" is more than myself," +
										",my term is " + currentTerm + " ,so I will become follower");
								currentTerm = result.getTerm();
								// ���˱��follower
								status = NodeStatus.FOLLOWER;
								return false;
							}
							// term û���Ҵ�ȴʧ���ˣ�˵��index���Ի���term����
							else
							{
								// �ݼ�
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
								// ����������ֱ���ɹ�Ϊֹ
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
			 	// ��ʱ��û�취��
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
	 * 	��handlerClientRequest ����
	 *	��������־�Ľ����leader���ͻ��˵�kv������һ����־�ַ��������ڵ���
	 *	����Ľ���������������
	 */
	private void getRPCAppendResult(List<Future<Boolean>> futureList,CountDownLatch countDownLatch,List<Boolean> resultList) {
		for(Future<Boolean> future : futureList) {
			RaftThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					try	{
						resultList.add(future.get(500,TimeUnit.MILLISECONDS));	// �ȴ����
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
