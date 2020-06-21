package graduate.rpc;

import graduate.model.rpcmodel.Request;
import graduate.model.rpcmodel.Response;

/**
 * RPCServer 每个节点用来接收请求
 * 					包括客户端请求
 * 					包括其他节点请求
 * @author 13299
 *
 */
public interface IRpcServer 
{
	void start();
	
	void stop();
}
