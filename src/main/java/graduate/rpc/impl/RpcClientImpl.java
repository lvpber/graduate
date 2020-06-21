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
	static
	{
		rpcClient.startup();
	}

	@Override
	public Response send(Request request)
	{
		// TODO Auto-generated method stub
		Response result = null;
		try
		{
//			System.out.println("œÚ " + request.getUrl() + "∑¢ÀÕ«Î«Û " + request.getCmd());
			result = (Response) rpcClient.invokeSync(request.getUrl(), request, 200000);
		} 
		catch (RemotingException e)
		{
			// TODO: handle exception
			throw new RaftRemotingException();
		} 
		catch (InterruptedException e)
		{
			// TODO: handle exception
			e.printStackTrace();
		} 
		finally
		{
			return result;
		}
	}

}
