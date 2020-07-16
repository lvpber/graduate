package graduate.collect.driver;

import graduate.model.collectingconfig.CollectingConfig;

/** 设备采集驱动接口 */
public interface IDeviceDriver {
    /** 采集初始化 */
    boolean init(CollectingConfig collectingConfig);

    /** 采集结束后释放资源，若被动链接，不做处理，如果主动连接被采集设备，需要释放资源，比如socket */
    default void release() {
        System.out.println("this is deviceDriver#release");
    }
}
