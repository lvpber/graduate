package graduate.model.kafkamsg.robotmsg;

import graduate.model.kafkamsg.DeviceMsg;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RobotDynamicMsg extends DeviceMsg {
    private String cart_pos;            /** ��ǰ�ѿ���λ�� */
    private String alarm_info;          /** ��ǰ������ */
    private String state;               /** ��ǰ����ϵͳ״̬ */
    private long happenTime;            /** ʵ�ʲɼ�ʱ�� */
}
