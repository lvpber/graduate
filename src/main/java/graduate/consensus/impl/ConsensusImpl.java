package graduate.consensus.impl;

import java.util.concurrent.locks.ReentrantLock;
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
	 *  ��������ͶƱRPC
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
			/** ������������ */
			/** ����Է�����û�Լ��£��������Ϊ����־һ���� */
			if(param.getTerm() < node.getCurrentTerm())
			{
				return builder.term(node.getCurrentTerm()).voteGranted(false).build();
			}
			
			/** ��ǰûѡ ����ѡ�˵Ľڵ��������ڵ� */
			String nowVotedFor = node.getVotedFor(); 
			if( nowVotedFor==null || nowVotedFor.length()==0 
					||	nowVotedFor.equals(param.getCandidateId()) )
			{
				/** ���FOLLOWER������LEADER�����������ڡ�����ͶƱ�Ķ�������ѡ��ʱ�� */
				node.setStatus(NodeStatus.FOLLOWER);								
				node.getPeerSet().setLeader( new Peer(param.getCandidateId()) );
				node.setCurrentTerm(param.getTerm());
				node.setVotedFor(param.getCandidateId());
				System.out.println(node.getPeerSet().getSelf() + " votedefor : "  + node.getVotedFor());
				node.setPreElectionTime(System.currentTimeMillis());
				return builder.term(node.getCurrentTerm()).voteGranted(true).build();
			}
			
			return builder.term(node.getCurrentTerm()).voteGranted(false).build();
		} 
		catch (Exception e)
		{
			// TODO: handle exception
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
	 *  ��������־RPC
	 */
	@Override
	public AentryResult appendEntries(AentryParam param)
	{
		// TODO Auto-generated method stub
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
			
			/** ��������϶Է�����Ч�ԣ���һ�Է���term ���� ��ǰ���ں�,�ڶ�����Ҫ�� */
			node.setPreElectionTime(System.currentTimeMillis());
			node.setPreHeartBeatTime(System.currentTimeMillis());
			node.getPeerSet().setLeader(new Peer(param.getLeaderId()));
			
			node.setStatus(NodeStatus.FOLLOWER);
			node.setCurrentTerm(param.getTerm());
			
			/** ������ */
			if(param.getEntries() == null || param.getEntries().length == 0)
			{
				return AentryResult.newBuilder().term(node.getCurrentTerm()).success(true).build();
			}

			/** Leader�ĸ�����־���� */
			
		} 
		catch (Exception e)
		{
			// TODO: handle exception
			
		}
		finally
		{
			/** �п��ܵ�ǰ�߳�û��������Ͼ�ǰ��ʹ�õ�trylock */		
			if(appendLock.isHeldByCurrentThread())
			{
				appendLock.unlock();
			}
		}
		return null;
	}
	
}
