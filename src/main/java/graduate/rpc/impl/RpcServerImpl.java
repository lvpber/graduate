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

	/** �ڵ㣬������������ */
	private NodeImpl nodeImpl;

	public RpcServerImpl(){}

	public RpcServerImpl(int port, NodeImpl nodeImpl)
	{
			rpcServer = new RpcServer(port);

			/**
			 * ע��ҵ���߼������� UserProcessor UserProcessor�������Ǽ���ָ�����������ͣ�
			 * ���籾�����е�request Ȼ������������
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
