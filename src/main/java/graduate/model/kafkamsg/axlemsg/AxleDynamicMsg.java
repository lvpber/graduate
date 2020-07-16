package graduate.model.kafkamsg.axlemsg;

import graduate.model.kafkamsg.DeviceMsg;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AxleDynamicMsg extends DeviceMsg {
    private String jnt_pos;     /** ��ǰ��Ƕ� */
    private String jnt_vel;     /** ��ǰ���ٶ� */
    private String jnt_trq;     /** ��ǰ������ǧ�ֱ�ϵ�� */
    private long happenTime;    /** ʵ�ʲɼ�ʱ�� */
}
