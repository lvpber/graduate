package graduate.rpc;

import graduate.model.rpcmodel.Request;
import graduate.model.rpcmodel.Response;

/**
 * �����������񣬽������͸������ڵ�
 * @author 13299
 *
 */
public interface IRpcClient 
{
	Response send(Request request);
}
