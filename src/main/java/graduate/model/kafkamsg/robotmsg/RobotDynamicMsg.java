package graduate.model.kafkamsg.robotmsg;

import graduate.model.kafkamsg.DeviceMsg;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RobotDynamicMsg extends DeviceMsg {
    private String cart_pos;            /** 当前笛卡尔位置 */
    private String alarm_info;          /** 当前错误码 */
    private String state;               /** 当前控制系统状态 */
    private long happenTime;            /** 实际采集时间 */
}
