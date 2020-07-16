package graduate.model.clientmodel;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ClientKVAck implements Serializable
{
    Object result;

    public ClientKVAck(Object obj){result = obj;}

    private ClientKVAck(Builder builder)
    {
        setResult(builder.result);
    }

    public static ClientKVAck ok(){return new ClientKVAck("ok");}

    public static ClientKVAck fail(){return new ClientKVAck("fail");}

    public static Builder newBuilder(){return new Builder();}

    public static final class Builder {
        private Object result;

        private Builder(){}

        public Builder result(Object val) {
            this.result = val;
            return this;
        }

        public ClientKVAck build(){return new ClientKVAck(this);}
    }
}
