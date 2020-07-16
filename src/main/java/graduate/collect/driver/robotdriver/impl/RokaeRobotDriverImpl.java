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
    /** ͨ��ʹ�õ�socket */
    private Socket socket;

    /** socketͨ����Ҫ����������� */
    PrintWriter printWriter ;
    BufferedReader bufferedReader ;

    /** �ɼ����ò��� */
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
                System.out.println("δ������˽�������");
                return false;
            }
            System.out.println(" hello this is rokae robot driver ");
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("���������������ʧ��");
            return false;
        }
        return true;
    }

    @Override
    public RobotDynamicMsg getRobotDynamicData() throws ConnectInterruptedException {
        /** ����init��socket�ɼ����� */
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
     * @apiNote		    ��ѯ�����˵ѿ���λ��
     * @return			�ѿ���λ��
     */
    private String getCartPos() {
        return this.sendCommand("cart_pos\r");
    }

    /**
     * @apiNote		    ��ѯ�����˵�ǰ������
     * @return			�����˴�����
     */
    private String getAlarmInfo() {
        return this.sendCommand("alarm_info\r");
    }

    /**
     * @apiNote		    ��ѯ�����˵�ǰ����ϵͳ״̬
     * @return			�����˿���ϵͳ״̬
     */
    private String getState() {
        return this.sendCommand("state\r");
    }

    /**
     * @apiNote		    ��ѯ�����˵�ǰ�ռ��ٶȲ���
     * @return			�����˿ռ��ٶȲ���
     */
    private String getSpacePara() {
        return this.sendCommand("query_space_para\r");
    }

    /**
     * @apiNote		    ��ѯ�����˵�ǰ��Ƕ���Ϣ
     * @return			��������Ƕ���Ϣ
     */
    private String getJntPos() {
        return this.sendCommand("jnt_pos\r");
    }

    /**
     * @apiNote		    ��ѯ�����˵�ǰ���ٶ�
     * @return			���������ٶ�
     */
    private String getJntVel() {
        return this.sendCommand("jnt_vel\r");
    }

    /**
     * @apiNote		    ��ѯ�����˵�ǰ������ǧ�ֱ�ϵ��
     * @return			������������ǧ�ֱ�ϵ��
     */
    private String getJntTrq() {
        return this.sendCommand("jnt_trq\r");
    }

    /**
     * @apiNote		    ��ָ�������˷���ָ��
     * @command	        ָ��
     * @return			ִ�н�� ����������
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
            // ���������ж���
            throw new ConnectInterruptedException();
        }
    }
}
