package graduate.model.consensusmodel.aentry;

import java.util.Arrays;

import graduate.model.consensusmodel.base.BaseParam;
import graduate.model.logmodulemodel.LogEntry;
import lombok.Getter;
import lombok.Setter;

/**
 * Leader 发送
 *	附加日志RPC 
 *	作用 日志一致性实现、心跳
 */

@Getter
@Setter
public class AentryParam extends BaseParam 
{
	private String      leaderId;            /** 领导人Id 便于跟随者实现重定向 */
	private long        prevLogIndex;        /** 新的日志条目紧随之前的索引值（就是上一条日志条目） */
	private long        prevLogTerm;         /** prevLogIndex日志条目的任期号 */
	private LogEntry[]  entries;             /** 准备存储的日志条目（表示心跳时为空；一次性发送多个是为了提高效率） */
	private long        leaderCommit;        /** 领导人已经提交的日志的索引值  */

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
