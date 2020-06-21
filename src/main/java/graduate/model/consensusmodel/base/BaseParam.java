package graduate.model.consensusmodel.base;

import java.io.Serializable;

public class BaseParam implements Serializable 
{
	/** 发送者任期号 */
	private long term;
	
	/** 被请求者id (IP:Port) */
	private String serverId;

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
	
	@Override
    public String toString() {
        return "BaseParam{" +
                "term=" + getTerm() +
                ", serverId='" + getServerId() + '\'' +
                '}';
    }
	
}
