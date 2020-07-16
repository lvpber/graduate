package graduate.node;

import graduate.lifecycle.ILifeCycle;
import graduate.model.clientmodel.ClientKVAck;
import graduate.model.clientmodel.ClientKVReq;
import graduate.model.consensusmodel.aentry.AentryParam;
import graduate.model.consensusmodel.aentry.AentryResult;
import graduate.model.consensusmodel.rvote.RvoteParam;
import graduate.model.consensusmodel.rvote.RvoteResult;
import graduate.model.monitor.JVMInfo;
import graduate.model.node.NodeConfig;

public interface INode<T> extends ILifeCycle {
	void 			setConfig				(NodeConfig config);	/** ���������ļ� */
	RvoteResult 	handlerRequestVote		(RvoteParam param);		/** ��������ͶƱ */
	AentryResult 	handlerAppendEntries	(AentryParam param);	/** ��������־���� */
	ClientKVAck 	handlerClientRequest	(ClientKVReq request);	/** ����ͻ������� */
	ClientKVAck 	redirect				(ClientKVReq request);	/** ���ͻ��˷�������Ķ�����Leaderʱ */
	JVMInfo			handlerCapabilityRequest();						/** ����Leader����������� */
}
