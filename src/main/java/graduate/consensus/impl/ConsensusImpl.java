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
	
	public ConsensusImpl(NodeImpl nodeImpl) {
		this.node = nodeImpl;
	}
	
	/**
	 *
	 *  if param.term < self.currentTerm									对方任期没有自己的高
	 *  	return false
	 *  else
	 *  	if self.votedFor == null || self.votedFor == param				自己还没有选择，或者选择的就是对方
	 *  		if self.getLastLogEntry.getTerm > param.getLastLogEntry		对方的最后一条日志的任期号比自己的最后一条日志的任期号小
	 *  			return false
	 *  	    if self.getLastIndex > param.getLastIndex					对方的最后一条日志的索引号比自己的最后一条索引号小
	 *  	    	return false
	 *  	    return true
	 *  	else															自己已经选择，且选择的不是对方
	 *  		return false
	 */
	@Override
	public RvoteResult requestVote(RvoteParam param) {
		try	{
			System.out.println("------------------------------------------------------------------------");

			System.out.println("当前节点 [" + node.getPeerSet().getSelf() + "] 收到了节点 [" +	param.getCandidateId() + "] 的请求投票请求");
			System.out.println("对方的任期是 " + param.getTerm() + ",自己的任期是 " + node.getCurrentTerm());

			RvoteResult.Builder builder = RvoteResult.newBuilder();
			/** 该方法不会阻塞等待，立刻返回结果 */
			if(!voteLock.tryLock())	{
				// 没有获得锁直接返回false ，原因目前有其他线程在使用，即目前已经接收到其他节点的请求投票
				System.out.println("########################################################################");
				return builder.term(node.getCurrentTerm()).voteGranted(false).build();
			}


			/** 如果对方任期没自己新，后面添加为了日志一致性 */
			if(param.getTerm() < node.getCurrentTerm())	{
				System.out.println("对方的任期没有自己的大，所以拒绝本次投票");
				System.out.println("########################################################################");
				return builder.term(node.getCurrentTerm()).voteGranted(false).build();
			}

//			LOGGER.info("node {} current vote for [{}], param candidateId : {}", node.getPeerSet().getSelf(),node.getVotedFor(),param.getCandidateId());
//			LOGGER.info("node {} current term {}, peer term : {}", node.getPeerSet().getSelf(),	node.getCurrentTerm(),param.getTerm());

			/** 用于输出注释 */
			if(node.getVotedFor() == null || node.getVotedFor().length() == 0) {
				System.out.println("当前节点还没有投票给任何节点");
			}
			else {
				System.out.println("当前节点已经投票给[" + node.getVotedFor() + "],所以拒绝");
			}

			/** 当前没选 或者选了的节点就是请求节点 */
			String nowVotedFor = node.getVotedFor(); 
			if( StringUtil.isNullOrEmpty(nowVotedFor) || nowVotedFor.equals(param.getCandidateId()) ) {
				/** 判断当前节点和请求节点的最后一条日志谁更新一点 */
				LogEntry logEntry;
				if((logEntry = node.getLogModuleImpl().getLast()) != null) {
					// 先比较term term大的优先级大
					if(logEntry.getTerm() > param.getLastLogTerm())	{
						System.out.println("对方的最后一条日志的任期 [" + param.getLastLogTerm() + "] 比自己最后一条日志的"+"任期 [ " + logEntry.getTerm() + " ] 小，拒绝本次投票");
						System.out.println("########################################################################");
						return builder.term(node.getCurrentTerm()).voteGranted(false).build();
					}
					// 如果 param.term >= 自己的，在比较lastLogIndex
					if(node.getLogModuleImpl().getLastIndex() > param.getLastLogIndex()) {
						System.out.println("对方的最后一条日志下标 [" + param.getLastLogIndex() + "] 比自己最后一条日志的" +"下标 [ " + node.getLogModuleImpl().getLastIndex() + " ] 小，拒绝本次投票");
						System.out.println("########################################################################");
						return builder.term(node.getCurrentTerm()).voteGranted(false).build();
					}
				}

				/** 变回FOLLOWER、设置LEADER、设置新任期、设置投票的对象、设置选举时间 */
				node.setStatus(NodeStatus.FOLLOWER);								
				// node.getPeerSet().setLeader( new Peer(param.getCandidateId()) );
				node.setCurrentTerm(param.getTerm());
				node.setVotedFor(param.getCandidateId());
				node.setPreElectionTime(System.currentTimeMillis());
//				LOGGER.info(node.getPeerSet().getSelf() + " voted for " + node.getVotedFor());
				System.out.println("当前节点 [" + node.getPeerSet().getSelf() + "] 认为符合条件，投 [" + node.getVotedFor() + "]一票");
				System.out.println("########################################################################");
				return builder.term(node.getCurrentTerm()).voteGranted(true).build();
			}
			/** 当前已经选择了候选人，返回失败 */
			System.out.println("########################################################################");
			return builder.term(node.getCurrentTerm()).voteGranted(false).build();
		} 
		catch (Exception e)	{
			System.out.println("this node is [" + node.getPeerSet().getSelf() + "] and the election task exists Error : " +	e.getMessage());
		}
		finally {
			/** 有可能当前线程没获得锁，毕竟前面使用的trylock */
			if(voteLock.isHeldByCurrentThread()) {
				voteLock.unlock();
			}
		}
		System.out.println("########################################################################");
		return null;
	}

	/**
	 *  附加日志（多个日志，为了提高效率） RPC
	 *
	 *  Leader发送过来几个参数 logEntries[...],prevLogIndex,prevLogTerm,leaderCommit
	 *
	 *  接收者实现
	 *  	如果term < currentTerm 返回false（5.1节）
	 *  	如果日志再prevLogIndex位置处的日志条目的任期号和prevLogTerm不匹配，返回false（5.3节）
	 *  	如果已经存在的日志条目和新的产生冲突（索引值相同但是任期号不同），删除这一条之后所有的（5.3节）
	 *  	附加任何在已有的日志中不存在的条目
	 *
	 *  	如果leaderCommit > commitIndex,令commitIndex 等与LeaderCommit 和 新的日志条目索引值较小的一个
	 */
	@Override
	public AentryResult appendEntries(AentryParam param) {
		try {
			AentryResult result = AentryResult.fail();
			result.setTerm(node.getCurrentTerm());		// result = {node.currentTerm,false}

			/** 当前正在处理其他的任务，有可能正在处理心跳，也可能正在处理附加日志，这里加锁对吗？ */
			if(!appendLock.tryLock()) {
				System.out.println();
				return result;
			}
			
			/** 如果附加日志请求的节点的任期号小于当前节点任期直接返回false */
			if(param.getTerm() < node.getCurrentTerm())	{
				System.out.println();
				return result;
			}


			/** 判断其他条件 */

			// 到这里承认对方的有效性，第一对方的term 大于 当前任期号,第二将来要改
			node.setPreElectionTime(System.currentTimeMillis());
			// node.setPreHeartBeatTime(System.currentTimeMillis());
			node.getPeerSet().setLeader(new Peer(param.getLeaderId()));
			node.setStatus(NodeStatus.FOLLOWER);
			node.setCurrentTerm(param.getTerm());
			
			/** 是心跳 */
			if(param.getEntries() == null || param.getEntries().length == 0) {
				// 获取当前节点的任务执行情况 返回 未实现
				return AentryResult.newBuilder().term(node.getCurrentTerm()).success(true).build();
			}
//***************************************************************下面的逻辑有问题***********************************************************************
			/** Leader的附加日志处理 */
			// 当前节点存在日志，且发送过来的附加日志请求中含有参数上一条日志编号
			if(node.getLogModuleImpl().getLastIndex() != 0 && param.getPrevLogIndex() != 0) {
				System.out.println();
				System.out.println("收到 [" + param.getLeaderId() + "] 的附加日志请求, 当前Leader周期 : " + param.getTerm() + ", 我的周期 " + node.getCurrentTerm());

				LogEntry logEntry;
				// Follower.logEntry[prevLogIndex] != null
				if((logEntry = node.getLogModuleImpl().read(param.getPrevLogIndex())) != null) {
					// 如果在prevLogIndex位置处的日志条目的任期号和prevLogTerm不匹配，返回false，leader需要减小nextIndex重新尝试
					if(logEntry.getTerm() != param.getPrevLogTerm()) {
						System.out.println();
						return result;
					}
				}
				// Follower.logEntry[prevLogIndex] == null 不存在日志，可以删除
				else {
					System.out.println();
					return result;
				}
			}

			// 如果已经存在的日志条目和新的日志条目发生冲突（索引值相同但是任期号不同），删除这一条和之后所有的
			// Follower.logEntry[prevLogIndex+1] 要插入的日志
			LogEntry existLog = node.getLogModuleImpl().read(param.getPrevLogIndex() + 1);
			if(existLog != null && existLog.getTerm() != param.getEntries()[0].getTerm()) {
				// 删除这一条和之后的所有的，然后写入日志和状态机
				node.getLogModuleImpl().removeOnStartIndex(param.getPrevLogIndex() + 1);
			}
			else if(existLog != null) {
				// 已经有日志了，不需要重复写入
				result.setSuccess(true);
				System.out.println();
				return result;
			}

			// 写日志并应用到状态机
			for(LogEntry logEntry : param.getEntries())	{
				node.getLogModuleImpl().write(logEntry);
				node.getStateMachine().apply(logEntry);
			}
			result.setSuccess(true);

			// 如果leaderCommit > commitIndex 令 commitIndex 等于 leaderCommit 和 新日志条目索引值中较小的一个
			if(param.getLeaderCommit() > node.getCommitIndex()) {
				int commitIndex = (int)Math.min(param.getLeaderCommit(),node.getLogModuleImpl().getLastIndex());
				node.setCommitIndex(commitIndex);
				node.setLastApplied(commitIndex);
			}

			result.setTerm(node.getCurrentTerm());
			node.setStatus(NodeStatus.FOLLOWER);
			System.out.println();
			return result;
		} 
		catch (Exception e)	{
			System.out.println("this node is [" + node.getPeerSet().getSelf() + "] solve the append entry task exists" + " Error : " + e.getMessage());
		}
		finally	{
			// 有可能当前线程没获得锁，毕竟前面使用的trylock
			if(appendLock.isHeldByCurrentThread()) {
				appendLock.unlock();
			}
		}
		System.out.println();
		return null;
	}
	
}
