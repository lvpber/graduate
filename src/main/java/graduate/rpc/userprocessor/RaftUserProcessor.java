package graduate.rpc.userprocessor;

import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.SyncUserProcessor;
import graduate.model.clientmodel.ClientKVReq;
import graduate.model.consensusmodel.aentry.AentryParam;
import graduate.model.consensusmodel.rvote.RvoteParam;
import graduate.model.rpcmodel.Request;
import graduate.model.rpcmodel.Response;
import graduate.node.impl.NodeImpl;

public class RaftUserProcessor extends SyncUserProcessor<Request>{
	private NodeImpl nodeImpl;
	
	public RaftUserProcessor( NodeImpl nodeImpl )
	{
		// TODO Auto-generated constructor stub
		this.nodeImpl = nodeImpl;
	}
	
	@Override
	public Object handleRequest(BizContext bizContext,Request request) throws Exception
	{
		if (request.getCmd() == Request.R_VOTE)
		{
			// 请求投票RPC
			RvoteParam rvoteParam = (RvoteParam)request.getObj();
			System.out.println("收到用户" + rvoteParam.getCandidateId() + "的请求投票请求");
			return new Response(nodeImpl.handlerRequestVote((RvoteParam) request.getObj()));
		} 
		else if (request.getCmd() == Request.A_ENTRIES)
		{
			// 附加日志RPC
			AentryParam aentryParam = (AentryParam)request.getObj();
			
//			if( aentryParam.getEntries() == null || aentryParam.getEntries().length == 0 )
//				System.out.println("收到用户"+ aentryParam.getLeaderId() +"的心跳");
//			else
//				System.out.println("收到用户"+ aentryParam.getLeaderId() +"的附加日志请求");
			return new Response(nodeImpl.handlerAppendEntries((AentryParam)request.getObj()));
		} 
		else if (request.getCmd() == Request.CLIENT_REQ)
		{
			// 客户端请求RPC 边缘服务器->Raspberry
			return new Response(nodeImpl.handlerClientRequest((ClientKVReq) request.getObj()));
		} 
		else if (request.getCmd() == Request.CHANGE_CONFIG_ADD)
		{
			// 增加节点请求RPC

		} 
		else if (request.getCmd() == Request.CHANGE_CONFIG_REMOVE)
		{
			// 移除节点请求RPC

		}
		return null;
	}
	
	
	/**
     * 指定感兴趣的请求数据类型，该 UserProcessor 只对感兴趣的请求类型的数据进行处理；
     * 假设 除了需要处理 MyRequest 类型的数据，还要处理 java.lang.String 类型，有两种方式：
     * 1、再提供一个 UserProcessor 实现类，其 interest() 返回 java.lang.String.class.getName()
     * 2、使用 MultiInterestUserProcessor 实现类，可以为一个 UserProcessor 指定 List<String> multiInterest()
     */
	@Override
	public String interest() {
		return Request.class.getName();
	}
}
