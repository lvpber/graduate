package graduate.model.rpcmodel;

import java.io.Serializable;

public class Response<T> implements Serializable
{
	private T result;

	public T getResult() {
		return result;
	}

	public void setResult(T result) {
		this.result = result;
	}
	
	private Response(Builder builder) {
        setResult((T) builder.result);
    }

    public static Response ok() {
        return new Response<>("ok");
    }

    public static Response fail() {
        return new Response<>("fail");
    }

    public static Builder newBuilder() {
        return new Builder();
    }
	
	public Response(T result){this.result = result;}
	
	 @Override
	    public String toString() {
	        return "Response{" +
	            "result=" + result +
	            '}';
	    }

	    public static final class Builder {

	        private Object result;

	        private Builder() {
	        }

	        public Builder result(Object val) {
	            result = val;
	            return this;
	        }

	        public Response build() {
	            return new Response(this);
	        }
	    }
}
