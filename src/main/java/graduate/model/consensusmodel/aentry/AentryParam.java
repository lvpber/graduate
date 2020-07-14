package graduate.model.consensusmodel.aentry;

import java.util.Arrays;

import graduate.model.consensusmodel.base.BaseParam;
import graduate.model.logmodulemodel.LogEntry;
import lombok.Getter;
import lombok.Setter;

/**
 * Leader ����
 *	������־RPC 
 *	���� ��־һ����ʵ�֡�����
 */

@Getter
@Setter
public class AentryParam extends BaseParam 
{
	private String      leaderId;            /** �쵼��Id ���ڸ�����ʵ���ض��� */
	private long        prevLogIndex;        /** �µ���־��Ŀ����֮ǰ������ֵ��������һ����־��Ŀ�� */
	private long        prevLogTerm;         /** prevLogIndex��־��Ŀ�����ں� */
	private LogEntry[]  entries;             /** ׼���洢����־��Ŀ����ʾ����ʱΪ�գ�һ���Է��Ͷ����Ϊ�����Ч�ʣ� */
	private long        leaderCommit;        /** �쵼���Ѿ��ύ����־������ֵ  */

	public AentryParam() {
    }

	private AentryParam(Builder builder) {
        setTerm(builder.term);
        setServerId(builder.serverId);
        setLeaderId(builder.leaderId);
        setPrevLogIndex(builder.prevLogIndex);
        setPrevLogTerm(builder.prevLogTerm);
        setEntries(builder.entries);
        setLeaderCommit(builder.leaderCommit);
    }

	@Override
    public String toString() {
        return "AentryParam{" +
            "leaderId='" + leaderId + '\'' +
            ", prevLogIndex=" + prevLogIndex +
            ", prevLogTerm=" + prevLogTerm +
            ", entries=" + Arrays.toString(entries) +
            ", leaderCommit=" + leaderCommit +
            ", term=" + super.getTerm() +
            ", serverId='" + super.getServerId() + '\'' +
            '}';
    }
	
	public static Builder newBuilder() {
        return new Builder();
    }

    @Setter
	@Getter
	public static final class Builder {

        private long term;
        private String serverId;
        private String leaderId;
        private long prevLogIndex;
        private long prevLogTerm;
        private LogEntry[] entries;
        private long leaderCommit;

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

        public Builder leaderId(String val) {
            leaderId = val;
            return this;
        }

        public Builder prevLogIndex(long val) {
            prevLogIndex = val;
            return this;
        }

        public Builder prevLogTerm(long val) {
            prevLogTerm = val;
            return this;
        }

        public Builder entries(LogEntry[] val) {
            entries = val;
            return this;
        }

        public Builder leaderCommit(long val) {
            leaderCommit = val;
            return this;
        }

        public AentryParam build() {
            return new AentryParam(this);
        }
    }
}
