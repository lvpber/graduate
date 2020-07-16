package graduate.model.kafkamsg;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class DeviceMsg implements Serializable {
    private String messageTypeName;     /** ��Ϣ�������� [robotdynamic,robotstatic,axledynamic,axlestatic] */
    private String deviceId;            /** �豸Uuid ����ʹ��*/
    private long timestamp;             /** �豸�ɼ�ʱ��� */
}
