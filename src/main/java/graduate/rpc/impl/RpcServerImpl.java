package graduate.rpc.impl;

import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.RpcServer;

import graduate.model.consensusmodel.rvote.RvoteParam;
import graduate.model.rpcmodel.Request;
import graduate.model.rpcmodel.Response;
import graduate.node.impl.NodeImpl;
import graduate.rpc.IRpcServer;
import graduate.rpc.userprocessor.RaftUserProcessor;

public class RpcServerImpl implements IRpcServer
{
	private RpcServer rpcServer;

	/** 节点，用来处理请求 */
	private NodeImpl nodeImpl;

	public RpcServerImpl(){}

	public RpcServerImpl(int port, NodeImpl nodeImpl)
	{
			rpcServer = new RpcServer(port);

			/**
			 * 注册业务逻辑处理器 UserProcessor UserProcessor的任务是监听指定的请求类型，
			 * 比如本例子中的request 然后处理这种请求
			 */
			rpcServer.registerUserProcessor(new RaftUserProcessor(nodeImpl));
			
			this.nodeImpl = nodeImpl;
	}

	@Override
	public void start()
	{
		// TODO Auto-generated method stub
		rpcServer.startup();
	}

	@Override
	public void stop()
	{
		// TODO Auto-generated method stub
		rpcServer.shutdown();
	}

}
