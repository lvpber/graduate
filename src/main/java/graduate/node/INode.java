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
	void 			setConfig				(NodeConfig config);	/** 设置配置文件 */
	RvoteResult 	handlerRequestVote		(RvoteParam param);		/** 处理请求投票 */
	AentryResult 	handlerAppendEntries	(AentryParam param);	/** 处理附加日志请求 */
	ClientKVAck 	handlerClientRequest	(ClientKVReq request);	/** 处理客户端请求 */
	ClientKVAck 	redirect				(ClientKVReq request);	/** 当客户端发送请求的对象不是Leader时 */
	JVMInfo			handlerCapabilityRequest();						/** 处理Leader的性能请求包 */
}
