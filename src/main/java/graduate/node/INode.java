package graduate.node;

import graduate.lifecycle.ILifeCycle;
import graduate.model.clientmodel.ClientKVAck;
import graduate.model.clientmodel.ClientKVReq;
import graduate.model.consensusmodel.aentry.AentryParam;
import graduate.model.consensusmodel.aentry.AentryResult;
import graduate.model.consensusmodel.rvote.RvoteParam;
import graduate.model.consensusmodel.rvote.RvoteResult;
import graduate.model.node.NodeConfig;

public interface INode<T> extends ILifeCycle 
{
	/** ���������ļ� */
	void setConfig(NodeConfig config);

	/** ��������ͶƱ */
	RvoteResult handlerRequestVote(RvoteParam param);

	/** ��������־���� */
	AentryResult handlerAppendEntries(AentryParam param);

	/** ����ͻ������� */
	ClientKVAck handlerClientRequest(ClientKVReq request);

	/** ���ͻ��˷�������Ķ�����Leaderʱ */
	ClientKVAck redirect(ClientKVReq request);
}
