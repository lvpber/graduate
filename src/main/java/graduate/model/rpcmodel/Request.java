package graduate.model.rpcmodel;

import java.io.Serializable;

/**
 *  RPC Request
 *  ȫ��RPC����Ĳ������������֣�
 *  1. ����ͶƱRPC    CANDIDATE -> FOLLOWERS
 *  2. ������־RPC(��������־һ���Ը���)  LEADER -> FOLLOWERs
 *  3. �ͻ�������RPC  Client -> LEADER
 */
public class Request<T> implements Serializable 
{
	/** ����ͶƱ */
    public static final int R_VOTE = 0;
    /** ������־ */
    public static final int A_ENTRIES = 1;
    /** �ͻ��� */
    public static final int CLIENT_REQ = 2;
    /** ���ñ��. add*/
    public static final int CHANGE_CONFIG_ADD = 3;
    /** ���ñ��. remove*/
    public static final int CHANGE_CONFIG_REMOVE = 4;

    /**
     * ��������
     */
    private int cmd = -1;
    
    /**
     * RPC ���� [param]
     * @see AentryParam ������־RPC
     * @see RvoteParam  ����ͶƱRPC
     * @see ClientKVReq �ͻ�������RPC
     */
    private T obj;

    /**
     *  ����RPC��ָ��Ŀ���ַ
     */
    private String url;

	public T getObj() {
		return obj;
	}

	public void setObj(T obj) {
		this.obj = obj;
	}

	public int getCmd() {
		return cmd;
	}

	public void setCmd(int cmd) {
		this.cmd = cmd;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

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
