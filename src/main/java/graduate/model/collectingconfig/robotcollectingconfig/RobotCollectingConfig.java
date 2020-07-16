package graduate.model.collectingconfig.robotcollectingconfig;

import graduate.model.collectingconfig.CollectingConfig;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RobotCollectingConfig extends CollectingConfig {
    private Long workTypeId;        /** 采集工艺Id */
    private String connectRole;     /** 连接角色 */
    private String ipAddr;          /** 连接ip地址 */
    private int port;               /** 连接port */
}
