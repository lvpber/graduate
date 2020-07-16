package graduate.model.rpcmodel;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 *  RPC Request
 *  ȫ��RPC����Ĳ������������֣�
 *  1. ����ͶƱRPC    CANDIDATE -> FOLLOWERS
 *  2. ������־RPC(��������־һ���Ը���)  LEADER -> FOLLOWERs
 *  3. �ͻ�������RPC  Client -> LEADER
 */
@Getter
@Setter
public class Request<T> implements Serializable 
{

    public static final int R_VOTE = 0;                 /** ����ͶƱ */
    public static final int A_ENTRIES = 1;              /** ������־ */
    public static final int CLIENT_REQ = 2;             /** �ͻ��� */
    public static final int CHANGE_CONFIG_ADD = 3;      /** ���ñ��. add*/
    public static final int CHANGE_CONFIG_REMOVE = 4;   /** ���ñ��. remove*/
    public static final int CAPABILITY_REQ = 5;         /** ����̽�� */

    private int cmd = -1;                               /** ��������  */
    
    /**
     * RPC ���� [param]
     * @see AentryParam ������־RPC
     * @see RvoteParam  ����ͶƱRPC
     * @see ClientKVReq �ͻ�������RPC
     */
    private T obj;                                      /** ����RPC��ָ��Ŀ���ַ */

    private String url;                                 /** Ҫ���͵�Ŀ��URL */

	public Request() {
    }
	
	public Request(T obj) {
        this.obj = obj;
    }
	
	public Request(int cmd, T obj, String url) {
        this.cmd = cmd;
        this.obj = obj;
        this.url = url;
    }
	
	private Request(Builder builder) {
        setCmd(builder.cmd);
        setObj((T) builder.obj);
        setUrl(builder.url);
    }

    public static  Builder newBuilder() {
        return new Builder<>();
    }


    public final static class Builder<T> {

        private int cmd;
        private Object obj;
        private String url;

        private Builder() {
        }

        public Builder cmd(int val) {
            cmd = val;
            return this;
        }

        public Builder obj(Object val) {
            obj = val;
            return this;
        }

        public Builder url(String val) {
            url = val;
            return this;
        }

        public Request<T> build() {
            return new Request<T>(this);
        }
    }
}
