package graduate.model.logmodulemodel;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
 *	客户端指令 deviceId ：collectingConfig 
 */
@Getter
@Setter
public class Command implements Serializable 
{
	/** 指令的key deviceId */
	private String key;
	
	/** 指令的value collectingConfig.toString() */
	private String value;

	@Override
    public String toString() {
        return "Command{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
	
	public Command(String key, String value) {
        this.key = key;
        this.value = value;
    }

    private Command(Builder builder) {
        setKey(builder.key);
        setValue(builder.value);
    }

    public static Builder newBuilder() {
        return new Builder();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Command command = (Command) o;
        return Objects.equals(key, command.key) &&  Objects.equals(value, command.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    public static final class Builder {

        private String key;
        private String value;

        private Builder() {
        }

        public Builder key(String val) {
            key = val;
            return this;
        }

        public Builder value(String val) {
            value = val;
            return this;
        }

        public Command build() {
            return new Command(this);
        }
    }
}
