package graduate.model.consensusmodel.rvote;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class RvoteResult implements Serializable
{
	/** 消息接收者的term 用于发送者更新 */
	private long term;
	/** 是否支持该候选人 */
	private boolean voteGranted;
	
	@Override
    public String toString() {
        return "RvoteResult{" +
                "voteGranted=" + voteGranted +
                ", term=" + getTerm()   +
                '}';
    }
	
	public RvoteResult(boolean voteGranted) {this.voteGranted = voteGranted;}
	
	public static RvoteResult fail() {return new RvoteResult(false);}
	
	public static RvoteResult ok() {return new RvoteResult(true);}
	
	public static Builder newBuilder() {return new Builder();} 
	
	private RvoteResult(Builder builder)
	{
		setTerm(builder.term);
		setVoteGranted(builder.voteGranted);
	}
	
	/** 建造者模式 */
	public static final class Builder 
	{

        private long term;
        private boolean voteGranted;

        private Builder() {
        }

        public Builder term(long term) {
            this.term = term;
            return this;
        }

        public Builder voteGranted(boolean voteGranted) {
            this.voteGranted = voteGranted;
            return this;
        }

        public RvoteResult build() {
            return new RvoteResult(this);
        }
    }
}
