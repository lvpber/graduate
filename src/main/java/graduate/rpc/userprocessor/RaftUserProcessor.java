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
			// ����ͶƱRPC
			RvoteParam rvoteParam = (RvoteParam)request.getObj();
			System.out.println("�յ��û�" + rvoteParam.getCandidateId() + "������ͶƱ����");
			return new Response(nodeImpl.handlerRequestVote((RvoteParam) request.getObj()));
		} 
		else if (request.getCmd() == Request.A_ENTRIES)
		{
			// ������־RPC
			AentryParam aentryParam = (AentryParam)request.getObj();
			
//			if( aentryParam.getEntries() == null || aentryParam.getEntries().length == 0 )
//				System.out.println("�յ��û�"+ aentryParam.getLeaderId() +"������");
//			else
//				System.out.println("�յ��û�"+ aentryParam.getLeaderId() +"�ĸ�����־����");
			return new Response(nodeImpl.handlerAppendEntries((AentryParam)request.getObj()));
		} 
		else if (request.getCmd() == Request.CLIENT_REQ)
		{
			// �ͻ�������RPC ��Ե������->Raspberry
			return new Response(nodeImpl.handlerClientRequest((ClientKVReq) request.getObj()));
		} 
		else if (request.getCmd() == Request.CHANGE_CONFIG_ADD)
		{
			// ���ӽڵ�����RPC

		} 
		else if (request.getCmd() == Request.CHANGE_CONFIG_REMOVE)
		{
			// �Ƴ��ڵ�����RPC

		}
		return null;
	}
	
	
	/**
     * ָ������Ȥ�������������ͣ��� UserProcessor ֻ�Ը���Ȥ���������͵����ݽ��д���
     * ���� ������Ҫ���� MyRequest ���͵����ݣ���Ҫ���� java.lang.String ���ͣ������ַ�ʽ��
     * 1�����ṩһ�� UserProcessor ʵ���࣬�� interest() ���� java.lang.String.class.getName()
     * 2��ʹ�� MultiInterestUserProcessor ʵ���࣬����Ϊһ�� UserProcessor ָ�� List<String> multiInterest()
     */
	@Override
	public String interest() {
		return Request.class.getName();
	}
}
