package graduate.consensus;

import graduate.model.consensusmodel.aentry.AentryParam;
import graduate.model.consensusmodel.aentry.AentryResult;
import graduate.model.consensusmodel.rvote.RvoteParam;
import graduate.model.consensusmodel.rvote.RvoteResult;

/**
 *		author lvpb
 *		note    һ����ģ��ӿ� ��ÿһ���ڵ㶼��һ��һ����ģ��
 *		����ʵ��ͶƱ�͸�����־����
 */
public interface IConsensus 
{
	/**
	 * 	 ��������ͶƱRPC
	 *  ������ʵ�֣�
	 *  		1. ��� param.term < currentTerm ����false
	 *  		2. ���votedForΪ�ջ��ߵ�ǰ����candidateId��
	 *  			���Һ�ѡ�˵���־���ٺ��Լ�һ���£�ͶƱ����
	 */
	RvoteResult requestVote(RvoteParam param);
	
	/**
	 * 	��������־RPC
	 * ������ʵ�֣�
	 * 			1. ���param.term < currentTerm ����false
	 * 			2. �����prevLogIndexλ�ô�����־��Ŀ�����ںź�prevLogTerm��ƥ�� ����false
	 * 			3. ����Ѵ�����־��Ŀ���µĲ����˳�ͻ��ɾ����һ��֮���������־
	 * 			4. ���յ��ĸ�����־�б��ز����ڵĸ��ӵ�������־����
	 * 			5. ���param.LeaderCommit > commitIndex��
	 * 				comminIndex = leaderCommit���µ���־��Ŀ����ֵ�н�С��һ��
	 */
	AentryResult appendEntries(AentryParam param);
}
