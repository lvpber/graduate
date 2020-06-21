package graduate.model.consensusmodel.aentry;

import java.util.Arrays;

import graduate.model.consensusmodel.base.BaseParam;
import graduate.model.logmodulemodel.LogEntry;

/**
 * Leader 发送
 *	附加日志RPC 
 *	作用 日志一致性实现、心跳
 */
public class AentryParam extends BaseParam 
{
	/** 领导人Id 便于跟随者实现重定向 */
	private String leaderId;
	
	/** 新的日志条目紧随之前的索引值（就是上一条日志条目） */
	private long prevLogIndex;
	
	/** prevLogIndex日志条目的任期号 */
	private long prevLogTerm;
	
	/** 准备存储的日志条目（表示心跳时为空；一次性发送多个是为了提高效率） */
	private LogEntry[] entries;

    /** 领导人已经提交的日志的索引值  */
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
