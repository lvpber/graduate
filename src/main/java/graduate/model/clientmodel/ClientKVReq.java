package graduate.model.clientmodel;

import java.io.Serializable;

/**
 *  �ͻ��˶�һ������־��kv����
 *  1. �����ɼ�
 *  2. ֹͣ�ɼ�
 */
public class ClientKVReq implements Serializable
{
    public static int PUT = 0;
    public static int GET = 1;

    /** ָ�����ͣ���ȡ״̬��������״̬ */
    private int type;
    /** ָ���key deviceId */
    private String key;
    /** ָ������ݣ������CollectingConfig */
    private String value;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

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
