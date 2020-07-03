package graduate.consensus.impl;

import java.util.concurrent.locks.ReentrantLock;

import graduate.model.logmodulemodel.LogEntry;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import graduate.consensus.IConsensus;
import graduate.model.consensusmodel.aentry.AentryParam;
import graduate.model.consensusmodel.aentry.AentryResult;
import graduate.model.consensusmodel.rvote.RvoteParam;
import graduate.model.consensusmodel.rvote.RvoteResult;
import graduate.model.node.NodeStatus;
import graduate.model.peer.Peer;
import graduate.node.impl.NodeImpl;
import sun.rmi.runtime.Log;

/**
 *	������������ᷢ������ͶƱRPC
 *	�����������һ�����߳��������Դ�����������ﴦ�����񣬶Դ˶���һЩ����
 *	��Ҫ��֤�̰߳�ȫ 
 */
public class ConsensusImpl implements IConsensus
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ConsensusImpl.class);
	
	private final NodeImpl node;
	
	private final ReentrantLock voteLock = new ReentrantLock();
	private final ReentrantLock appendLock = new ReentrantLock();
	
	public ConsensusImpl(NodeImpl nodeImpl)
	{
		// TODO Auto-generated constructor stub
		this.node = nodeImpl;
	}
	
	/**
	 *
	 *  if param.term < self.currentTerm									�Է�����û���Լ��ĸ�
	 *  	return false
	 *  else
	 *  	if self.votedFor == null || self.votedFor == param				�Լ���û��ѡ�񣬻���ѡ��ľ��ǶԷ�
	 *  		if self.getLastLogEntry.getTerm > param.getLastLogEntry		�Է������һ����־�����ںű��Լ������һ����־�����ں�С
	 *  			return false
	 *  	    if self.getLastIndex > param.getLastIndex					�Է������һ����־�������ű��Լ������һ��������С
	 *  	    	return false
	 *  	    return true
	 *  	else															�Լ��Ѿ�ѡ����ѡ��Ĳ��ǶԷ�
	 *  		return false
	 */
	@Override
	public RvoteResult requestVote(RvoteParam param)
	{
		// TODO Auto-generated method stub
		try
		{		
			RvoteResult.Builder builder = RvoteResult.newBuilder();
			/** �÷������������ȴ������̷��ؽ�� */
			if(!voteLock.tryLock())
			{
				// û�л����ֱ�ӷ���false ��ԭ��Ŀǰ�������߳���ʹ��
				return builder.term(node.getCurrentTerm()).voteGranted(false).build();
			}


			/** ����Է�����û�Լ��£��������Ϊ����־һ���� */
			if(param.getTerm() < node.getCurrentTerm())
			{
				return builder.term(node.getCurrentTerm()).voteGranted(false).build();
			}

//			LOGGER.info("node {} current vote for [{}], param candidateId : {}", node.getPeerSet().getSelf(),
//					node.getVotedFor(),
//					param.getCandidateId());
//			LOGGER.info("node {} current term {}, peer term : {}", node.getPeerSet().getSelf(),
//					node.getCurrentTerm(),
//					param.getTerm());

			System.out.println("��ǰ�ڵ� " + node.getPeerSet().getSelf() + " �Ѿ�Ͷ" +
							node.getVotedFor() + "һƱ,�����յ� " + param.getCandidateId() + "������ͶƱ����");

			/** ��ǰûѡ ����ѡ�˵Ľڵ��������ڵ� */
			String nowVotedFor = node.getVotedFor(); 
			if( StringUtil.isNullOrEmpty(nowVotedFor) || nowVotedFor.equals(param.getCandidateId()) )
			{
				/** �жϵ�ǰ�ڵ������ڵ�����һ����־˭����һ�� */
				LogEntry logEntry;
				if((logEntry = node.getLogModuleImpl().getLast()) != null)
				{
					// �ȱȽ�term term������ȼ���
					if(logEntry.getTerm() > param.getLastLogTerm())
					{
						return builder.term(node.getCurrentTerm()).voteGranted(false).build();
					}
					// ��� param.term >= �Լ��ģ��ڱȽ�lastLogIndex
					if(node.getLogModuleImpl().getLastIndex() > param.getLastLogIndex())
					{
						return builder.term(node.getCurrentTerm()).voteGranted(false).build();
					}
				}

				/** ���FOLLOWER������LEADER�����������ڡ�����ͶƱ�Ķ�������ѡ��ʱ�� */
				node.setStatus(NodeStatus.FOLLOWER);								
				node.getPeerSet().setLeader( new Peer(param.getCandidateId()) );
				node.setCurrentTerm(param.getTerm());
				node.setVotedFor(param.getCandidateId());
//				LOGGER.info(node.getPeerSet().getSelf() + " voted for " + node.getVotedFor());
				System.out.println("��ǰ�ڵ� " + node.getPeerSet().getSelf() + " ��Ϊ����������Ͷ " + node.getVotedFor() + "һƱ");
				node.setPreElectionTime(System.currentTimeMillis());
				return builder.term(node.getCurrentTerm()).voteGranted(true).build();
			}
			
			return builder.term(node.getCurrentTerm()).voteGranted(false).build();
		} 
		catch (Exception e)
		{
		}
		finally
		{
			/** �п��ܵ�ǰ�߳�û��������Ͼ�ǰ��ʹ�õ�trylock */
			if(voteLock.isHeldByCurrentThread())
			{
				voteLock.unlock();
			}
		}
		return null;
	}

	/**
	 *  ������־�������־��Ϊ�����Ч�ʣ� RPC
	 *
	 *  Leader���͹����������� logEntries[...],prevLogIndex,prevLogTerm,leaderCommit
	 *
	 *  ������ʵ��
	 *  	���term < currentTerm ����false��5.1�ڣ�
	 *  	�����־��prevLogIndexλ�ô�����־��Ŀ�����ںź�prevLogTerm��ƥ�䣬����false��5.3�ڣ�
	 *  	����Ѿ����ڵ���־��Ŀ���µĲ�����ͻ������ֵ��ͬ�������ںŲ�ͬ����ɾ����һ��֮�����еģ�5.3�ڣ�
	 *  	�����κ������е���־�в����ڵ���Ŀ
	 *
	 *  	���leaderCommit > commitIndex,��commitIndex ����LeaderCommit �� �µ���־��Ŀ����ֵ��С��һ��
	 */
	@Override
	public AentryResult appendEntries(AentryParam param)
	{
		try
		{
			AentryResult result = AentryResult.fail();
			result.setTerm(node.getCurrentTerm());

			if(!appendLock.tryLock())
			{
				return result;
			}
			
			/** ���������־����Ľڵ�����ں�С�ڵ�ǰ�ڵ�����ֱ�ӷ���false */
			if(param.getTerm() < node.getCurrentTerm())
			{
				return result;
			}

			/** �ж��������� */


			// ��������϶Է�����Ч�ԣ���һ�Է���term ���� ��ǰ���ں�,�ڶ�����Ҫ��
			node.setPreElectionTime(System.currentTimeMillis());
			node.setPreHeartBeatTime(System.currentTimeMillis());
			node.getPeerSet().setLeader(new Peer(param.getLeaderId()));
			
			node.setStatus(NodeStatus.FOLLOWER);
			node.setCurrentTerm(param.getTerm());
			
			/** ������ */
			if(param.getEntries() == null || param.getEntries().length == 0)
			{
//				LOGGER.info("node {} append heartbeat success , he's term : {}, my term : {}",
//						param.getLeaderId(), param.getTerm(), node.getCurrentTerm());
				System.out.println("�յ� " + param.getLeaderId()
								+ " ��������, ��ǰLeader���� : " + param.getTerm() + ", �ҵ����� "
						+ node.getCurrentTerm());
				return AentryResult.newBuilder().term(node.getCurrentTerm()).success(true).build();
			}

			/** Leader�ĸ�����־���� */
			// ��ǰ�ڵ������־���ҷ��͹����ĸ�����־�����к��в�����һ����־���
			if(node.getLogModuleImpl().getLastIndex() != 0 && param.getPrevLogIndex() != 0)
			{
				LogEntry logEntry;
				// Follower.logEntry[prevLogIndex] != null
				if((logEntry = node.getLogModuleImpl().read(param.getPrevLogIndex())) != null)
				{
					// �����prevLogIndexλ�ô�����־��Ŀ�����ںź�prevLogTerm��ƥ�䣬����false��leader��Ҫ��СnextIndex���³���
					if(logEntry.getTerm() != param.getPrevLogTerm())
					{
						return result;
					}
				}
				// Follower.logEntry[prevLogIndex] == null ��������־������ɾ��
				else
				{
					return result;
				}
			}
			// ����Ѿ����ڵ���־��Ŀ���µ���־��Ŀ������ͻ������ֵ��ͬ�������ںŲ�ͬ����ɾ����һ����֮�����е�
			// Follower.logEntry[prevLogIndex+1] Ҫ�������־
			LogEntry existLog = node.getLogModuleImpl().read(param.getPrevLogIndex() + 1);
			if(existLog != null && existLog.getTerm() != param.getEntries()[0].getTerm())
			{
				// ɾ����һ����֮������еģ�Ȼ��д����־��״̬��
				node.getLogModuleImpl().removeOnStartIndex(param.getPrevLogIndex() + 1);
			}
			else if(existLog != null)
			{
				// �Ѿ�����־�ˣ�����Ҫ�ظ�д��
				result.setSuccess(true);
				return result;
			}

			// д��־��Ӧ�õ�״̬��
			for(LogEntry logEntry : param.getEntries())
			{
				node.getLogModuleImpl().write(logEntry);
				node.getStateMachine().apply(logEntry);
				result.setSuccess(true);
			}

			// ���leaderCommit > commitIndex �� commitIndex ���� leaderCommit �� ����־��Ŀ����ֵ�н�С��һ��
			if(param.getLeaderCommit() > node.getCommitIndex())
			{
				int commitIndex = (int)Math.min(param.getLeaderCommit(),node.getLogModuleImpl().getLastIndex());
				node.setCommitIndex(commitIndex);
				node.setLastApplied(commitIndex);
			}

			result.setTerm(node.getCurrentTerm());
			node.setStatus(NodeStatus.FOLLOWER);
			return result;
		} 
		catch (Exception e)
		{
			// TODO: handle exception
			
		}
		finally
		{
			// �п��ܵ�ǰ�߳�û��������Ͼ�ǰ��ʹ�õ�trylock
			if(appendLock.isHeldByCurrentThread())
			{
				appendLock.unlock();
			}
		}
		return null;
	}
	
}
