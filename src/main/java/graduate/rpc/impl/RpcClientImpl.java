package graduate.rpc.impl;

import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;

import graduate.exception.rpcexception.RaftRemotingException;
import graduate.model.rpcmodel.Request;
import graduate.model.rpcmodel.Response;
import graduate.rpc.IRpcClient;

public class RpcClientImpl implements IRpcClient
{
	private final static RpcClient rpcClient = new RpcClient();

	static {
		rpcClient.startup();
	}

	@Override
	public Response send(Request request) {
		Response result = null;
		try {
			result = (Response) rpcClient.invokeSync(request.getUrl(), request, 200000);
		} 
		catch (RemotingException e) {
			e.printStackTrace();
			throw new RaftRemotingException();
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		} 
		finally {
			return result;
		}
	}

}
