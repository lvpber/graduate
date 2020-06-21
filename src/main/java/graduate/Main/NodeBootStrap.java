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
		
		/** ��һ��������ز��� */
		NodeConfig nodeConfig = new NodeConfig();
		nodeConfig.setSelfPort(port);
		nodeConfig.setPeerAddrs(Arrays.asList(peerAddrs));	
		INode node = NodeImpl.getInstance();
		node.setConfig(nodeConfig);

		/**
		 * �ڶ���ִ��init����
		 *	initִ����Ӧ����������
		 * 1. ����rpcserver����
		 * 2. ����һ�������̣߳�������ѯ��ÿ��ִ���ж��Լ��ǲ���leader ���ǾͲ�����
		 * 3. ����һ��ѡ���̣߳�������ѯ��ÿ��ִ���ж��Լ��ǲ���candidate ���ǾͲ����� 
		 */
		node.init();
		
		/**
		 * ��jvm�رյ�ʱ���ִ��ϵͳ���Ѿ����õ�����ͨ������addShutdownHook��ӵĹ���
		 * ��ϵͳִ������Щ���񣨱��������node.destroy())��jvm�Ż�رգ�
		 * ����������Ӻ����������������ڴ�������������ٵĲ���
		 * ���������ڴ�����Ͷ�������
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
	
	
	/** ����rpcserver */
	public static void main1(int port)
	{
		RpcServerImpl rpcServerImpl = new RpcServerImpl(port, null);
		rpcServerImpl.start();
	}
}
