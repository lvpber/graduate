package graduate.model.consensusmodel.rvote;

import graduate.model.consensusmodel.base.BaseParam;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RvoteParam extends BaseParam
{
	private String candidateId;		/** ��ѡ��id */
	private long lastLogIndex;		/** ��ѡ�������־��Ŀ������ֵ */
	private long lastLogTerm;		/** ��ѡ�������־��Ŀ�����ں� */

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
	
	private RvoteParam(Builder builder){
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
