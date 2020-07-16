package graduate.rpc.userprocessor;

import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.protocol.SyncUserProcessor;
import graduate.model.clientmodel.ClientKVReq;
import graduate.model.consensusmodel.aentry.AentryParam;
import graduate.model.consensusmodel.rvote.RvoteParam;
import graduate.model.monitor.JVMInfo;
import graduate.model.rpcmodel.Request;
import graduate.model.rpcmodel.Response;
import graduate.node.impl.NodeImpl;

public class RaftUserProcessor extends SyncUserProcessor<Request>{
	private NodeImpl nodeImpl;
	
	public RaftUserProcessor( NodeImpl nodeImpl ) {
		this.nodeImpl = nodeImpl;
	}
	
	@Override
	public Object handleRequest(BizContext bizContext,Request request) throws Exception {
		// 接收到请求投票RPC
		if (request.getCmd() == Request.R_VOTE)	{
			RvoteParam rvoteParam = (RvoteParam)request.getObj();
			return new Response(nodeImpl.handlerRequestVote((RvoteParam) request.getObj()));
		}
		// 接收到附加日志RPC
		else if (request.getCmd() == Request.A_ENTRIES)	{
			AentryParam aentryParam = (AentryParam)request.getObj();
			return new Response(nodeImpl.handlerAppendEntries((AentryParam)request.getObj()));
		}
		// 客户端请求RPC
		else if (request.getCmd() == Request.CLIENT_REQ) {
			// 客户端请求RPC 边缘服务器->Raspberry
			return new Response(nodeImpl.handlerClientRequest((ClientKVReq) request.getObj()));
		}
		// 增加节点请求RPC
		else if (request.getCmd() == Request.CHANGE_CONFIG_ADD)	{
		}
		// 移除节点请求RPC
		else if (request.getCmd() == Request.CHANGE_CONFIG_REMOVE) {
		}
		// 获取节点性能RPC
		else if (request.getCmd() == Request.CAPABILITY_REQ) {
			System.out.println("接收到来自节点 Leader的获取性能请求参数");
			JVMInfo jvmInfo = nodeImpl.handlerCapabilityRequest();
			System.out.println("当前节点的性能参数" + jvmInfo);
			return new Response(jvmInfo);
		}
		// 采集初始化请求rpc
		else if(request.getCmd() == Request.TRY_COLLECT) {
			System.out.println("接收到Leader的try collect请求rpc");
			Boolean tryCollectRes = nodeImpl.handlerTryCollectRequest();
			String str = tryCollectRes?"成功":"失败";
			System.out.println("当前节点trycollect " + str);
			return new Response(tryCollectRes);
		}
		// 正式采集RPC
		else if(request.getCmd() == Request.START_COLLECT) {
			System.out.println("接收到Leader的start collect请求rpc");
			Boolean startCollectRes = nodeImpl.handlerStartCollectRequest();
			String str = startCollectRes?"成功":"失败";
			System.out.println("当前节点startCollectRes " + str);
			return new Response(startCollectRes);
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
