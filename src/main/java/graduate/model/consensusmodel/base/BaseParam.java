package graduate.model.consensusmodel.base;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class BaseParam implements Serializable 
{
	private long term;				/** ���������ں� */
	private String serverId;		/** ��������id (IP:Port) */

	@Override
    public String toString() {
        return "BaseParam{" +
                "term=" + getTerm() +
                ", serverId='" + getServerId() + '\'' +
                '}';
    }
}
