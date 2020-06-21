package graduate.model.consensusmodel.aentry;

import java.util.Arrays;

import graduate.model.consensusmodel.base.BaseParam;
import graduate.model.logmodulemodel.LogEntry;

/**
 * Leader ����
 *	������־RPC 
 *	���� ��־һ����ʵ�֡�����
 */
public class AentryParam extends BaseParam 
{
	/** �쵼��Id ���ڸ�����ʵ���ض��� */
	private String leaderId;
	
	/** �µ���־��Ŀ����֮ǰ������ֵ��������һ����־��Ŀ�� */
	private long prevLogIndex;
	
	/** prevLogIndex��־��Ŀ�����ں� */
	private long prevLogTerm;
	
	/** ׼���洢����־��Ŀ����ʾ����ʱΪ�գ�һ���Է��Ͷ����Ϊ�����Ч�ʣ� */
	private LogEntry[] entries;

    /** �쵼���Ѿ��ύ����־������ֵ  */
	private long leaderCommit;
	
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
	
	public static final class Builder {

        private long term;
        private String serverId;
        private String leaderId;
        private long prevLogIndex;
        private long prevLogTerm;
        private LogEntry[] entries;
        private long leaderCommit;

        public long getTerm() {
			return term;
		}

		public void setTerm(long term) {
			this.term = term;
		}

		public String getServerId() {
			return serverId;
		}

		public void setServerId(String serverId) {
			this.serverId = serverId;
		}

		public String getLeaderId() {
			return leaderId;
		}

		public void setLeaderId(String leaderId) {
			this.leaderId = leaderId;
		}

		public long getPrevLogIndex() {
			return prevLogIndex;
		}

		public void setPrevLogIndex(long prevLogIndex) {
			this.prevLogIndex = prevLogIndex;
		}

		public long getPrevLogTerm() {
			return prevLogTerm;
		}

		public void setPrevLogTerm(long prevLogTerm) {
			this.prevLogTerm = prevLogTerm;
		}

		public LogEntry[] getEntries() {
			return entries;
		}

		public void setEntries(LogEntry[] entries) {
			this.entries = entries;
		}

		public long getLeaderCommit() {
			return leaderCommit;
		}

		public void setLeaderCommit(long leaderCommit) {
			this.leaderCommit = leaderCommit;
		}

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

	public String getLeaderId() {
		return leaderId;
	}

	public void setLeaderId(String leaderId) {
		this.leaderId = leaderId;
	}

	public long getPrevLogIndex() {
		return prevLogIndex;
	}

	public void setPrevLogIndex(long prevLogIndex) {
		this.prevLogIndex = prevLogIndex;
	}

	public long getPrevLogTerm() {
		return prevLogTerm;
	}

	public void setPrevLogTerm(long prevLogTerm) {
		this.prevLogTerm = prevLogTerm;
	}

	public LogEntry[] getEntries() {
		return entries;
	}

	public void setEntries(LogEntry[] entries) {
		this.entries = entries;
	}

	public long getLeaderCommit() {
		return leaderCommit;
	}

	public void setLeaderCommit(long leaderCommit) {
		this.leaderCommit = leaderCommit;
	}
}
