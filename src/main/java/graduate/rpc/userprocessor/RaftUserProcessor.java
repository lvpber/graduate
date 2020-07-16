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
		// ���յ�����ͶƱRPC
		if (request.getCmd() == Request.R_VOTE)	{
			RvoteParam rvoteParam = (RvoteParam)request.getObj();
			return new Response(nodeImpl.handlerRequestVote((RvoteParam) request.getObj()));
		}
		// ���յ�������־RPC
		else if (request.getCmd() == Request.A_ENTRIES)	{
			AentryParam aentryParam = (AentryParam)request.getObj();
			return new Response(nodeImpl.handlerAppendEntries((AentryParam)request.getObj()));
		}
		// �ͻ�������RPC
		else if (request.getCmd() == Request.CLIENT_REQ) {
			// �ͻ�������RPC ��Ե������->Raspberry
			return new Response(nodeImpl.handlerClientRequest((ClientKVReq) request.getObj()));
		}
		// ���ӽڵ�����RPC
		else if (request.getCmd() == Request.CHANGE_CONFIG_ADD)	{
		}
		// �Ƴ��ڵ�����RPC
		else if (request.getCmd() == Request.CHANGE_CONFIG_REMOVE) {
		}
		else if (request.getCmd() == Request.CAPABILITY_REQ) {
			System.out.println("���յ����Խڵ� Leader�Ļ�ȡ�����������");
			JVMInfo jvmInfo = nodeImpl.handlerCapabilityRequest();
			System.out.println("��ǰ�ڵ�����ܲ���" + jvmInfo);
			return new Response<JVMInfo>(jvmInfo);
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
