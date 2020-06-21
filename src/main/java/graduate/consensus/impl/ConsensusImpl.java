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
 *	多个服务器都会发送请求投票RPC
 *	所以这个类是一个多线程争夺的资源，都会在这里处理任务，对此对于一些变量
 *	需要保证线程安全 
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
	 *  处理请求投票RPC
	 */
	@Override
	public RvoteResult requestVote(RvoteParam param)
	{
		// TODO Auto-generated method stub
		try
		{		
			RvoteResult.Builder builder = RvoteResult.newBuilder();
			/** 该方法不会阻塞等待，立刻返回结果 */
			if(!voteLock.tryLock())
			{
				// 没有获得锁直接返回false ，原因目前有其他线程在使用
				return builder.term(node.getCurrentTerm()).voteGranted(false).build();
			}			
			/** 到这里获得锁了 */
			/** 如果对方任期没自己新，后面添加为了日志一致性 */
			if(param.getTerm() < node.getCurrentTerm())
			{
				return builder.term(node.getCurrentTerm()).voteGranted(false).build();
			}
			
			/** 当前没选 或者选了的节点就是请求节点 */
			String nowVotedFor = node.getVotedFor(); 
			if( nowVotedFor==null || nowVotedFor.length()==0 
					||	nowVotedFor.equals(param.getCandidateId()) )
			{
				/** 变回FOLLOWER、设置LEADER、设置新任期、设置投票的对象、设置选举时间 */
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
			/** 有可能当前线程没获得锁，毕竟前面使用的trylock */
			if(voteLock.isHeldByCurrentThread())
			{
				voteLock.unlock();
			}
		}
		return null;
	}

	/**
	 *  处理附加日志RPC
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
			
			/** 如果附加日志请求的节点的任期号小于当前节点任期直接返回false */
			if(param.getTerm() < node.getCurrentTerm())
			{
				return result;
			}
			/** 判断其他条件 */
			
			/** 到这里承认对方的有效性，第一对方的term 大于 当前任期号,第二将来要改 */
			node.setPreElectionTime(System.currentTimeMillis());
			node.setPreHeartBeatTime(System.currentTimeMillis());
			node.getPeerSet().setLeader(new Peer(param.getLeaderId()));
			
			node.setStatus(NodeStatus.FOLLOWER);
			node.setCurrentTerm(param.getTerm());
			
			/** 是心跳 */
			if(param.getEntries() == null || param.getEntries().length == 0)
			{
				return AentryResult.newBuilder().term(node.getCurrentTerm()).success(true).build();
			}

			/** Leader的附加日志处理 */
			
		} 
		catch (Exception e)
		{
			// TODO: handle exception
			
		}
		finally
		{
			/** 有可能当前线程没获得锁，毕竟前面使用的trylock */		
			if(appendLock.isHeldByCurrentThread())
			{
				appendLock.unlock();
			}
		}
		return null;
	}
	
}
