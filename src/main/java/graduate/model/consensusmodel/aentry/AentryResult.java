package graduate.model.consensusmodel.aentry;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class AentryResult implements Serializable
{

	private long    term;               /** ���������ڣ������ѡ�˸����Լ� */
	private boolean success;            /** �Ƿ�ͶƱ�ɹ� */

	public AentryResult(long term) {
        this.term = term;
    }

    public AentryResult(boolean success) {
        this.success = success;
    }

    public AentryResult(long term, boolean success) {
        this.term = term;
        this.success = success;
    }

    private AentryResult(Builder builder) {
        setTerm(builder.term);
        setSuccess(builder.success);
    }
    
    public static Builder newBuilder() {
        return new Builder();
    }

    public static AentryResult fail() {
        return new AentryResult(false);
    }

    public static AentryResult ok() 
    {
        return new AentryResult(true);
    }

    public static final class Builder {

        private long term;
        private boolean success;

        private Builder() {
        }

        public Builder term(long val) {
            term = val;
            return this;
        }

        public Builder success(boolean val) {
            success = val;
            return this;
        }

        public AentryResult build() {
            return new AentryResult(this);
        }
    }
}
