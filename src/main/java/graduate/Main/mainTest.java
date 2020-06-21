package graduate.Main;

import graduate.model.consensusmodel.rvote.RvoteParam;
import graduate.model.rpcmodel.Request;
import graduate.model.rpcmodel.Response;
import graduate.rpc.impl.RpcClientImpl;

public class mainTest
{
	public static void main1(String args[])
	{
		RpcClientImpl rpcClientImpl = new RpcClientImpl();
		RvoteParam param = RvoteParam.newBuilder()
				.term(0)
				.candidateId("localhost:8776")
				.build();

		/** ¥¥Ω®rpc«Î«Û */
		Request request = Request.newBuilder()
				.cmd(Request.R_VOTE)
				.obj(param)
				.url("localhost:8775")
				.build();
		Response response = (Response) rpcClientImpl.send(request);
		System.out.println(response);
	}
}
