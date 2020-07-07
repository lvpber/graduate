package graduate.model.clientmodel;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 *  客户端对一致性日志的kv请求
 *  1. 建立采集
 *  2. 停止采集
 */
@Getter
@Setter
public class ClientKVReq implements Serializable
{
    public static int PUT = 0;
    public static int GET = 1;

    /** 指令类型，读取状态还是设置状态 */
    private int type;
    /** 指令的key deviceId */
    private String key;
    /** 指令的内容，具体的CollectingConfig */
    private String value;

    private ClientKVReq(Builder builder)
    {
        setType(builder.type);
        setKey(builder.key);
        setValue(builder.value);
    }

    public static Builder newBuilder(){return new Builder();}

    public enum Type
    {
        PUT(0),GET(1),
        ;
        int code;
        Type(int code){this.code = code;}

        public static Type value(int code)
        {
            for(Type type : values())
            {
                if(type.code == code)
                    return type;
            }
            return null;
        }
    }

    public static final class Builder
    {
        private int type;
        private String key;
        private String value;

        private Builder(){}

        public Builder type(int val)
        {
            this.type = val;
            return this;
        }

        public Builder key(String val)
        {
            key = val;
            return this;
        }

        public Builder value(String val)
        {
            value = val;
            return this;
        }

        public ClientKVReq build(){return new ClientKVReq(this);}
    }
}
