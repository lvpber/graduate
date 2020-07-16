package graduate.rpc.impl;

import com.alipay.remoting.rpc.RpcServer;

import graduate.node.impl.NodeImpl;
import graduate.rpc.IRpcServer;
import graduate.rpc.userprocessor.RaftUserProcessor;

public class RpcServerImpl implements IRpcServer
{
	private RpcServer rpcServer;
	private NodeImpl nodeImpl;			/** �ڵ㣬������������ */

	public RpcServerImpl(){}
	public RpcServerImpl(int port, NodeImpl nodeImpl) {
		rpcServer = new RpcServer(port);

		/** ע��ҵ���߼������� UserProcessor UserProcessor�������Ǽ���ָ�����������ͣ����籾�����е�request Ȼ������������ */
		rpcServer.registerUserProcessor(new RaftUserProcessor(nodeImpl));
			
		this.nodeImpl = nodeImpl;
	}

	@Override
	public void start() {
		rpcServer.startup();
	}

	@Override
	public void stop() {
		rpcServer.shutdown();
	}

}
