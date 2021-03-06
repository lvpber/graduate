package graduate.model.logmodulemodel;

import java.io.Serializable;
import java.util.Objects;

public class LogEntry implements Serializable ,Comparable
{
	/** 这里有点疑问 */
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		if (o == null) {
            return -1;
        }
        if (this.getIndex() > ((LogEntry) o).getIndex()) {
            return 1;
        }
        return -1;
	}
	
	/** 日志在索引库中的下标 */
	private Long index;

	/** 日志的任期号 */
    private long term;

    /** 日志中包含的指令 */
    private Command command;
    
    public Long getIndex() {
		return index;
	}

	public void setIndex(Long index) {
		this.index = index;
	}

	public long getTerm() {
		return term;
	}

	public void setTerm(long term) {
		this.term = term;
	}

	public Command getCommand() {
		return command;
	}

	public void setCommand(Command command) {
		this.command = command;
	}

	public LogEntry() { }
    
    public LogEntry(long term, Command command) {
        this.term = term;
        this.command = command;
    }

    public LogEntry(Long index, long term, Command command) {
        this.index = index;
        this.term = term;
        this.command = command;
    }
    
    private LogEntry(Builder builder) {
        setIndex(builder.index);
        setTerm(builder.term);
        setCommand(builder.command);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "{" +
            "index=" + index +
            ", term=" + term +
            ", command=" + command +
            '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LogEntry logEntry = (LogEntry) o;
        return term == logEntry.term &&
            Objects.equals(index, logEntry.index) &&
            Objects.equals(command, logEntry.command);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, term, command);
    }

    public static final class Builder {

        private Long index;
        private long term;
        private Command command;

        private Builder() {
        }

        public Builder index(Long val) {
            index = val;
            return this;
        }

        public Builder term(long val) {
            term = val;
            return this;
        }

        public Builder command(Command val) {
            command = val;
            return this;
        }

        public LogEntry build() {
            return new LogEntry(this);
        }
    }
}
