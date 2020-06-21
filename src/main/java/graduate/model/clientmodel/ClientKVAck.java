package graduate.model.clientmodel;

import java.io.Serializable;

public class ClientKVAck implements Serializable
{
    Object result;

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public ClientKVAck(Object obj){result = obj;}

    private ClientKVAck(Builder builder)
    {
        setResult(builder.result);
    }

    public static ClientKVAck ok(){return new ClientKVAck("ok");}

    public static ClientKVAck fail(){return new ClientKVAck("fail");}

    public static Builder newBuilder(){return new Builder();}

    public static final class Builder
    {
        private Object result;

        private Builder(){}

        public Builder result(Object val)
        {
            this.result = val;
            return this;
        }

        public ClientKVAck build(){return new ClientKVAck(this);}
    }
}
