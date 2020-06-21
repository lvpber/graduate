package graduate.rpc;

import graduate.model.rpcmodel.Request;
import graduate.model.rpcmodel.Response;

/**
 * 负责处理发送任务，将请求发送给其他节点
 * @author 13299
 *
 */
public interface IRpcClient 
{
	Response send(Request request);
}
