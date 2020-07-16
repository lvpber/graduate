package graduate.model.kafkamsg.axlemsg;

import graduate.model.kafkamsg.DeviceMsg;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AxleDynamicMsg extends DeviceMsg {
    private String jnt_pos;     /** 当前轴角度 */
    private String jnt_vel;     /** 当前轴速度 */
    private String jnt_trq;     /** 当前轴力矩千分比系数 */
    private long happenTime;    /** 实际采集时间 */
}
