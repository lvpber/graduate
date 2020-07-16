package graduate.collect.driver.robotdriver;

import graduate.collect.driver.IDeviceDriver;
import graduate.exception.rpcexception.collectingexception.ConnectInterruptedException;
import graduate.model.kafkamsg.axlemsg.AxleDynamicMsg;
import graduate.model.kafkamsg.robotmsg.RobotDynamicMsg;

public interface IRobotDriver extends IDeviceDriver {
    /**
     * @apiNote	    获取机器人动态信息
     * @return     机器人动态信息，按照 key:value 形式组成字符串 或者直接返回json字符串
     */
    RobotDynamicMsg getRobotDynamicData() throws ConnectInterruptedException;

    /**
     * @apiNote	    获取机器人静态信息
     * @return     机器人静态信息，按照 key:value 形式组成字符串 或者直接返回json字符串
     */
    String getRobotStaticData() throws ConnectInterruptedException;

    /**
     * @apiNote	    获取机器人轴动态信息
     * @return     机器人轴动态信息，按照 key:value 形式组成字符串 或者直接返回json字符串
     */
    AxleDynamicMsg getAxleDynameData() throws ConnectInterruptedException;

    /**
     * @apiNote	    获取机器人轴静态信息
     * @return     机器人轴静态信息，按照 key:value 形式组成字符串 或者直接返回json字符串
     */
    String getAxleStaticData() throws ConnectInterruptedException;
}
