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
 * �����ڵ� ָ��raspberry
 */
public class NodeImpl implements INode, ILifeCycle
{
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeImpl.class);

	/** ѡ�ٳ�ʱʱ�� 10s ����Ҫ�ģ����ʱ��̫���� */
	private volatile long electionTime = 5 * 1000;
	/** ��һ��ѡ��ʱ��� */
	private volatile long preElectionTime;
	/** ��һ������ʱ��� */
	private volatile long preHeartBeatTime;
	/** �������ʱ�� 5sʱ��̫�� */
	private final long heartBeatTick = 5 * 100;

	/** �������� ������ѡ������ */
	private HeartBeatTask heartBeatTask = new HeartBeatTask();
	private ElectionTask electionTask = new ElectionTask();
	
	private volatile int status = NodeStatus.FOLLOWER;

	private PeerSet peerSet;

	/** ===========���з������ϳ־ô��ڵ�========== */
	private volatile long currentTerm = 0;
	private volatile String votedFor;

	/** ===========���з������Ͼ������ =========== */
	private volatile long commitIndex;
	private volatile long lastApplied = 0;

	/** ===========�쵼���Ͼ������============== */
	private Map<Peer, Long> nextIndexs;
	private Map<Peer, Long> matchIndexs;
	
	/** ==================================== */
	private NodeConfig nodeConfig;
	private static RpcServerImpl rpcServerImpl;
	private RpcClientImpl rpcClientImpl = new RpcClientImpl();
	private volatile boolean started; // ��¼��ǰ�ڵ��Ƿ��Ѿ�����

	private ConsensusImpl consensusImpl;		//һ����ģ��
	private LogModuleImpl logModuleImpl;		//��־ģ��
	
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

	/** ========��̬�ڲ���ʵ�ֵ���ģʽ���̰߳�ȫ======= */
	private NodeImpl(){}

	private static class NodeLazyHolder
	{
		private static final NodeImpl instance = new NodeImpl();
	}

	public static NodeImpl getInstance()
	{
		return NodeLazyHolder.instance;
	}

	/** ===========LifeCycle����============= */
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
			/** ����RPC Server ����RPC���󣨿ͻ��ˡ������ڵ㣩 */
			rpcServerImpl.start(); // ����rpc server����rpc����
			
			/** ��ʼ��һ���Դ���ģ�顢��־�洢ģ�� */
			consensusImpl = new ConsensusImpl(this);	
			logModuleImpl = LogModuleImpl.getLogModuleInstance();
			
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
	 * ѡ���߳� ��ɫ�Ӹ�����תΪ��ѡ���Ժ󣬿�ʼѡ�ٹ��� 
	 * 1. �����Լ���ǰ�����ں� 
	 * 		���Լ�ͶƱ 
	 * 		����ѡ�ٳ�ʱ��ʱ�� 
	 * 		��������ͶƱRPC�������ķ����� 
	 * 2. ������յ��������������ѡƱ���ͱ���쵼�� 
	 * 3. ������յ��µ��쵼�˵ĸ�����־RPC ת���ɸ����� (�Ȳ�ʵ��) 
	 * 4. ���ѡ�ٹ��̳�ʱ���ٷ���һ��ѡ��
	 */
	class ElectionTask implements Runnable
	{

		@Override
		public void run()
		{
			// TODO Auto-generated method stub
			/** �����ǰ�Ѿ���leader �Ͳ���Ҫִ��ѡ�ٲ��� */
			if (status == NodeStatus.LEADER)
			{
				return;
			}

			/** ��ȡ��ǰʱ�� */
			long current = System.currentTimeMillis();

			/** �����һ��ʱ����û�н��յ��κ����󣬾ͻᷢ��ѡ�٣������յ������󣬻�����ʱ�� */
			long currentElectionTime  = electionTime + ThreadLocalRandom.current().nextInt(50);
			System.out.println("������һ��ѡ�������Ѿ���ȥʱ�� " + (current - preElectionTime));
			if (current - preElectionTime < currentElectionTime)
			{
				System.out.println("�ȴ�ʱ�䲻���������ȴ�");
				return;
			}

			/** FOLLOWER -> CANDIDATE */
			status = NodeStatus.CANDIDATE;
//			LOGGER.error("node {} will become CANDIDATE and start election leader,current term : [{}] ",	peerSet.getSelf(), currentTerm);
			System.out.println("node" +  peerSet.getSelf() + " will become CANDIDATE and "
					+ "start election leader,current term : [" + currentTerm +"]");
			/** ����ǰʱ����Ϊ��һ��ѡ��ʱ�� */
			preElectionTime = System.currentTimeMillis() + 
					ThreadLocalRandom.current().nextInt(200) + 150;

			/** �������ںţ����Լ�ͶһƱ */
			currentTerm = currentTerm + 1;
			votedFor = peerSet.getSelf().getAddr();

			/** �����������еĻ�鷢��ͶƱ����RPC */
			List<Peer> peers = peerSet.getPeersWithoutSelf();
			ArrayList<Future> futures = new ArrayList<>();

			/**
			 * ������ͶƱ�����ݽ����̳߳ش���Ȼ��õ�һ��future����
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
						 * 	��������ͶƱRPC���������� 
						 *	�Ȳ���������־���ֶ��� 
						 */
						RvoteParam param = RvoteParam.newBuilder()
								.term(currentTerm)
								.candidateId(peerSet.getSelf().getAddr())
								.build();

						/** ����rpc���� */
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
			
			/** ��Ϊ�����ж������ͶƱ�̣߳�����Ҫ�̰߳�ȫ */
			AtomicInteger successCount = new AtomicInteger(0);
			
			/**
			 * CountDownLatch ������������ǰ�̣߳��ȴ������̸߳���ִ�����֮��
			 * ��ִ�е�ǰ�߳�
			 * �������һ���̳߳� pool���������10���߳� t1,t2,t3...,t10
			 * CountDownLatch latch = new CountDownLatch(10);
			 * 
			 * foreach t:
			 * 		t.run()
			 * 		{
			 * 			do thread;
			 * 			countDownLatch.countDown
			 * 		}
			 * 
			 * latch.await()	����������ȴ������߳����
			 * t11.run  �ص�֮ǰ���̼߳���ִ��
			 */
			CountDownLatch latch = new CountDownLatch(futures.size());
			
			/**
			 * ��ͶƱ���ֳɵ�future�����ö��߳̽��к����Ĵ���
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
							/** ���0.1����û�еõ���Ϣ */
							if (response == null)
							{
								return -1;
							}
							
							/** �жϷ��ؽ���ǲ���ѡ�Լ� */
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
							/** �����ǲ��ǳɹ� ��Ҫ��finally��ִ�и���䣬��Ȼ���������  */
							latch.countDown();
						}
					}
				});
			}
			
			try
			{
				/** ��3.5sʱ�䣬�����ʱ����ûȷ��leader��ǿ����ֹ */
				latch.await(150,TimeUnit.MILLISECONDS);
			} 
			catch (Exception e)
			{
				// TODO: handle exception
				LOGGER.error("Interrupted by master election task");
			}
			
			int success = successCount.get();
			/** 
			 * ���е�ͶƱ�������ˣ������ֿ��� 
			 *	1. ��ѡ
			 *	2. ͶƱ�Ϸ�
			 *	3. �����˳�ΪLeader�����ʱ�򱾽ڵ���Զ����Follower
			 *		��ʱ���Ҫֹͣ���� 
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
				/** ������Ҫ������������  */
				// todo sth when become Leader
			}
			else
			{
				/** �������û�Լ���Ϊleader��Ҳ�������µ�leader ��˵��ѡƱ���Ϸ��ˣ�����ѡ�� */
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
	public void becomeLeaderTodo()
	{
		/** �ȶ����� */
		RaftThreadPool.execute(new HeartBeatTask(), true);	
		System.out.println("become Leader and run the heartBeatTask immiediately");
		//��leader������־��Ȼ�󿴿��ܲ��ܸ㵽�����Ľڵ���

		/** �ڵ�ѡȡ��ΪLeader�󣬵�һ���³�ʼ��ÿһ���ڵ��ƥ����־ */
		nextIndexs = new ConcurrentHashMap<>();
		matchIndexs = new ConcurrentHashMap<>();
		for(Peer peer : peerSet.getPeersWithoutSelf())
		{
			nextIndexs.put(peer,logModuleImpl.getLastIndex()+1);
			matchIndexs.put(peer,0L);
		}
	}
	
	/**
	 *	ֻ��Leader����ִ������̵߳�Ȩ�� 
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

//			System.out.println("ִ����������");
			/** �ѵ�ǰ��ʱ����Ϊ��һ�ε�����ʱ�� */
			preHeartBeatTime = System.currentTimeMillis();
			
			/** ��ÿһ��С���ѷ��������� */
			for(Peer peer : peerSet.getPeersWithoutSelf())
			{
				/**
				 * ��������RPC����
				 */
				AentryParam param = AentryParam.newBuilder()
						.entries(null)
						.leaderId(peerSet.getSelf().getAddr())	//�쵼��id�����ض���
						.serverId(peer.getAddr())				//��������ip��ַ
						.term(currentTerm)
						.build();
				/**
				 * ����RPC����
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
						
						/** ���������Ϣ�е�term ���ڵ�ǰleader��term˵����ǰ�ڵ��Ѿ�out */
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
	 *	����ͻ�������
	 *	����ΪʲôҪ��synchronized
	 */
	@Override
	public synchronized ClientKVAck handlerClientRequest(ClientKVReq request) {
		/** �����ǰ�ڵ㲻��Leader */
		if(status != NodeStatus.LEADER)
		{
			return redirect(request);
		}

		/** �ͻ�������ֻ�Ǽ򵥵Ļ�ȡ�����˵�״̬ */
		if(request.getType() == ClientKVReq.GET)
		{
			if(request.getKey()==null || request.getKey().equals(""))
			{
				//�������е�״̬
			}

			// ��ȡ״̬����һ��
		}

		/** ��ǰ������PUT������������־ */
		LogEntry logEntry = LogEntry.newBuilder()
				.command(Command.newBuilder()
						.key(request.getKey())
						.value(request.getValue())
						.build())
				.term(currentTerm)
				.build();

		// �Ȱ����µ���־д��logDB��
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
			 	/** ��һ��ʱ����ʧ������ */
			 	while(end - start < 1 * 1000)
				{
					AentryParam aentryParam = new AentryParam.Builder()
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
