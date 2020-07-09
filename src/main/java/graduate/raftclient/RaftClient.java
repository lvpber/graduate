package graduate.raftclient;

import graduate.model.clientmodel.ClientKVReq;
import graduate.model.logmodulemodel.LogEntry;
import graduate.model.rpcmodel.Request;
import graduate.model.rpcmodel.Response;
import graduate.rpc.impl.RpcClientImpl;

import java.util.ArrayList;
import java.util.List;

public class RaftClient
{
    private final static RpcClientImpl rpcClient = new RpcClientImpl();

    static String addr = "10.1.1.44:8778";
    static List<String> list = new ArrayList<>();
    static
    {
        list.add("10.1.1.44:8777");
        list.add("10.1.1.44:8778");
        list.add("10.1.1.44:8779");
    }

    public static void main(String[] args)
    {
        int i;
        ClientKVReq clientKVReq;
        Request<ClientKVReq> reqRequest;

        for(i=0;i<3;i++)
        {
            addr = list.get(i);
            clientKVReq = ClientKVReq.newBuilder()
                    .key("hello:" + i)
                    .value("world:" + i)
                    .type(ClientKVReq.PUT)
                    .build();

            reqRequest = new Request<>();
            reqRequest.setObj(clientKVReq);
            reqRequest.setCmd(Request.CLIENT_REQ);
            reqRequest.setUrl(addr);

            Response<String> response;
            try
            {
                response = rpcClient.send(reqRequest);
                System.out.println(response.getResult().toString());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        try
        {
            Thread.sleep(5000);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }


        for(i = 0; i < 3 ;i++)
        {
            clientKVReq = ClientKVReq.newBuilder()
                    .key("hello:" + i)
                    .type(ClientKVReq.GET)
                    .build();

            reqRequest = new Request<>();
            reqRequest.setObj(clientKVReq);
            reqRequest.setCmd(Request.CLIENT_REQ);
            reqRequest.setUrl(addr);

            Response<LogEntry> response1 ;
            try
            {
                response1 = rpcClient.send(reqRequest);
                System.out.println(response1.getResult().toString());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
