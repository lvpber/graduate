package graduate.Main;

import java.util.Arrays;
import graduate.model.node.NodeConfig;
import graduate.node.INode;
import graduate.node.impl.NodeImpl;
import graduate.rpc.impl.RpcServerImpl;


public class NodeBootStrap
{
	public static void main(String args[]) throws Throwable
	{
		String ipAddr = "localhost";
		int port = 8775;
		if(args.length == 2)
		{
			ipAddr = args[0];
			port = Integer.parseInt(args[1]);
		}
		main0(ipAddr, port);
		main1(port);	
	}
	
	public static void main0(String ipAddr,int port) throws Throwable
	{
		String []peerAddrs = {
				"localhost:8775","localhost:8776",
		        "localhost:8777", "localhost:8778", "localhost:8779"
		}; 
		
		/** 第一步配置相关参数 */
		NodeConfig nodeConfig = new NodeConfig();
		nodeConfig.setSelfPort(port);
		nodeConfig.setPeerAddrs(Arrays.asList(peerAddrs));	
		INode node = NodeImpl.getInstance();
		node.setConfig(nodeConfig);

		/**
		 * 第二步执行init操作
		 *	init执行相应操作操作：
		 * 1. 开启rpcserver服务
		 * 2. 开启一个心跳线程，不断轮询，每次执行判断自己是不是leader 不是就不做了
		 * 3. 开启一个选举线程，不断轮询，每次执行判断自己是不是candidate 不是就不做了 
		 */
		node.init();
		
		/**
		 * 当jvm关闭的时候会执行系统中已经设置的所有通过方法addShutdownHook添加的钩子
		 * 当系统执行完这些任务（比如下面的node.destroy())后，jvm才会关闭，
		 * 所以这个钩子函数可以用来进行内存的清理，对象销毁的操作
		 * 多适用于内存清理和对象销毁
		 */
		Runtime.getRuntime().addShutdownHook(
				new Thread( () -> 
				{
					try
					{
						node.destroy();
					} 
					catch (Throwable e)
					{
						// TODO: handle exception
						e.printStackTrace();
					}
				}
		));
	}
	
	
	/** 测试rpcserver */
	public static void main1(int port)
	{
		RpcServerImpl rpcServerImpl = new RpcServerImpl(port, null);
		rpcServerImpl.start();
	}
}
