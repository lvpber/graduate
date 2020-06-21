package graduate.model.rpcmodel;

import java.io.Serializable;

/**
 *  RPC Request
 *  全局RPC请求的参数，包括三种：
 *  1. 请求投票RPC    CANDIDATE -> FOLLOWERS
 *  2. 附加日志RPC(心跳、日志一致性更新)  LEADER -> FOLLOWERs
 *  3. 客户端请求RPC  Client -> LEADER
 */
public class Request<T> implements Serializable 
{
	/** 请求投票 */
    public static final int R_VOTE = 0;
    /** 附加日志 */
    public static final int A_ENTRIES = 1;
    /** 客户端 */
    public static final int CLIENT_REQ = 2;
    /** 配置变更. add*/
    public static final int CHANGE_CONFIG_ADD = 3;
    /** 配置变更. remove*/
    public static final int CHANGE_CONFIG_REMOVE = 4;

    /**
     * 请求类型
     */
    private int cmd = -1;
    
    /**
     * RPC 内容 [param]
     * @see AentryParam 附加日志RPC
     * @see RvoteParam  请求投票RPC
     * @see ClientKVReq 客户端请求RPC
     */
    private T obj;

    /**
     *  发送RPC到指定目标地址
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
