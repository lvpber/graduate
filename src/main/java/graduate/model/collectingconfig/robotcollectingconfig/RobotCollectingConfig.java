package graduate.model.collectingconfig.robotcollectingconfig;

import graduate.model.collectingconfig.CollectingConfig;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RobotCollectingConfig extends CollectingConfig {
    private Long workTypeId;        /** �ɼ�����Id */
    private String connectRole;     /** ���ӽ�ɫ */
    private String ipAddr;          /** ����ip��ַ */
    private int port;               /** ����port */
}
