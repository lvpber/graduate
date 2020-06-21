package graduate.model.consensusmodel.base;

import java.io.Serializable;

public class BaseParam implements Serializable 
{
	/** ���������ں� */
	private long term;
	
	/** ��������id (IP:Port) */
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
