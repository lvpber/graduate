package graduate.rpc;

import graduate.model.rpcmodel.Request;
import graduate.model.rpcmodel.Response;

/**
 * RPCServer ÿ���ڵ�������������
 * 					�����ͻ�������
 * 					���������ڵ�����
 * @author 13299
 *
 */
public interface IRpcServer 
{
	void start();
	
	void stop();
}
