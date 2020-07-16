package graduate.collect.driver.robotdriver.impl;

import graduate.collect.driver.robotdriver.IRobotDriver;
import graduate.exception.rpcexception.collectingexception.ConnectInterruptedException;
import graduate.model.collectingconfig.CollectingConfig;
import graduate.model.collectingconfig.robotcollectingconfig.RobotCollectingConfig;
import graduate.model.kafkamsg.axlemsg.AxleDynamicMsg;
import graduate.model.kafkamsg.robotmsg.RobotDynamicMsg;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class RokaeRobotDriverImpl implements IRobotDriver {
    /** 通信使用的socket */
    private Socket socket;

    /** socket通信需要的输入输出流 */
    PrintWriter printWriter ;
    BufferedReader bufferedReader ;

    /** 采集配置参数 */
    private RobotCollectingConfig robotCollectingConfig;

    @Override
    public boolean init(CollectingConfig collectingConfig) {
        /** init communication param */
        try {
            robotCollectingConfig = (RobotCollectingConfig) collectingConfig;
            if(robotCollectingConfig.getConnectRole().equals("client")) {
                socket = new Socket(robotCollectingConfig.getIpAddr(), robotCollectingConfig.getPort());
            }
            else if(robotCollectingConfig.getConnectRole().equals("server")) {

            }
            if(socket != null) {
                printWriter = new PrintWriter(socket.getOutputStream());
                bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            }
            else {
                System.out.println("未与机器人建立连接");
                return false;
            }
            System.out.println(" hello this is rokae robot driver ");
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("与机器人主动连接失败");
            return false;
        }
        return true;
    }

    @Override
    public RobotDynamicMsg getRobotDynamicData() throws ConnectInterruptedException {
        /** 利用init的socket采集数据 */
        RobotDynamicMsg robotDynamicMsg = new RobotDynamicMsg();
        robotDynamicMsg.setMessageTypeName("robotdynamic");
        robotDynamicMsg.setDeviceId(robotCollectingConfig.getDeviceId());
        robotDynamicMsg.setAlarm_info(this.getAlarmInfo());
        robotDynamicMsg.setState(this.getState());
        robotDynamicMsg.setCart_pos(this.getCartPos());
        robotDynamicMsg.setHappenTime(System.currentTimeMillis());
        return robotDynamicMsg;
    }


    public String getRobotStaticData() throws ConnectInterruptedException {
        String robotStaticDataString = "robotStaticData";
        return robotStaticDataString;
    }

    @Override
    public AxleDynamicMsg getAxleDynameData() throws ConnectInterruptedException {
        AxleDynamicMsg axleDynamicMsg = new AxleDynamicMsg();
        axleDynamicMsg.setMessageTypeName("axledynamic");
        axleDynamicMsg.setDeviceId(this.robotCollectingConfig.getDeviceId());
        axleDynamicMsg.setJnt_pos(this.getJntPos());
        axleDynamicMsg.setJnt_trq(this.getJntTrq());
        axleDynamicMsg.setJnt_vel(this.getJntVel());
        axleDynamicMsg.setHappenTime(System.currentTimeMillis());
        return axleDynamicMsg;
    }

    @Override
    public String getAxleStaticData() throws ConnectInterruptedException {
        String axleStaticDataString = "axleStaticDataString";
        return axleStaticDataString;
    }

    /**
     * @apiNote		    查询机器人笛卡尔位置
     * @return			笛卡尔位置
     */
    private String getCartPos() {
        return this.sendCommand("cart_pos\r");
    }

    /**
     * @apiNote		    查询机器人当前错误码
     * @return			机器人错误码
     */
    private String getAlarmInfo() {
        return this.sendCommand("alarm_info\r");
    }

    /**
     * @apiNote		    查询机器人当前控制系统状态
     * @return			机器人控制系统状态
     */
    private String getState() {
        return this.sendCommand("state\r");
    }

    /**
     * @apiNote		    查询机器人当前空间速度参数
     * @return			机器人空间速度参数
     */
    private String getSpacePara() {
        return this.sendCommand("query_space_para\r");
    }

    /**
     * @apiNote		    查询机器人当前轴角度信息
     * @return			机器人轴角度信息
     */
    private String getJntPos() {
        return this.sendCommand("jnt_pos\r");
    }

    /**
     * @apiNote		    查询机器人当前轴速度
     * @return			机器人轴速度
     */
    private String getJntVel() {
        return this.sendCommand("jnt_vel\r");
    }

    /**
     * @apiNote		    查询机器人当前轴力矩千分比系数
     * @return			机器人轴力矩千分比系数
     */
    private String getJntTrq() {
        return this.sendCommand("jnt_trq\r");
    }

    /**
     * @apiNote		    向指定机器人发送指令
     * @command	        指令
     * @return			执行结果 机器人数据
     */
    private String sendCommand(String command) {
        try {
            printWriter.print(command);
            printWriter.flush();
            String result;
            if( (result = bufferedReader.readLine()) == null )
                throw new ConnectInterruptedException();//return null
            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            // 发生连接中断了
            throw new ConnectInterruptedException();
        }
    }
}
