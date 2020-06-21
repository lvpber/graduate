package graduate.model.consensusmodel.rvote;

import graduate.model.consensusmodel.base.BaseParam;
public class RvoteParam extends BaseParam
{
	/** 候选人id 方便投票 */
	private String candidateId;
	
	/** 候选人最后日志条目的索引值 */
	private long lastLogIndex;
	
	/** 候选人最后日志条目的任期号 */
	private long lastLogTerm;

	public String getCandidateId() {
		return candidateId;
	}

	public void setCandidateId(String candidateId) {
		this.candidateId = candidateId;
	}

	public long getLastLogIndex() {
		return lastLogIndex;
	}

	public void setLastLogIndex(long lastLogIndex) {
		this.lastLogIndex = lastLogIndex;
	}

	public long getLastLogTerm() {
		return lastLogTerm;
	}

	public void setLastLogTerm(long lastLogTerm) {
		this.lastLogTerm = lastLogTerm;
	}
	
	@Override
    public String toString() {
        return "RvoteParam{" +
                "candidateId='" + candidateId + '\'' +
                ", lastLogIndex=" + lastLogIndex +
                ", lastLogTerm=" + lastLogTerm +
                ", term=" + super.getTerm() +
                ", serverId='" + super.getServerId() + '\'' +
                '}';
    }
	
	private RvoteParam(Builder builder)
	{
		setTerm(builder.term);
		setServerId(builder.serverId);
        setCandidateId(builder.candidateId);
        setLastLogIndex(builder.lastLogIndex);
        setLastLogTerm(builder.lastLogTerm);
	}
	
	public static Builder newBuilder() {return new Builder();}
	
	public static final class Builder
	{
		private long term;
		private String serverId;
		private String candidateId;
		private long lastLogIndex;
		private long lastLogTerm;
		
		private Builder() {
        }

        public Builder term(long val) {
            term = val;
            return this;
        }

        public Builder serverId(String val) {
            serverId = val;
            return this;
        }

        public Builder candidateId(String val) {
            candidateId = val;
            return this;
        }

        public Builder lastLogIndex(long val) {
            lastLogIndex = val;
            return this;
        }

        public Builder lastLogTerm(long val) {
            lastLogTerm = val;
            return this;
        }
        
        public RvoteParam build() {return new RvoteParam(this);}
	}
}
