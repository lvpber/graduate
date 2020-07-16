package graduate.collect.driver.robotdriver;

import graduate.collect.driver.IDeviceDriver;
import graduate.exception.rpcexception.collectingexception.ConnectInterruptedException;
import graduate.model.kafkamsg.axlemsg.AxleDynamicMsg;
import graduate.model.kafkamsg.robotmsg.RobotDynamicMsg;

public interface IRobotDriver extends IDeviceDriver {
    /**
     * @apiNote	    ��ȡ�����˶�̬��Ϣ
     * @return     �����˶�̬��Ϣ������ key:value ��ʽ����ַ��� ����ֱ�ӷ���json�ַ���
     */
    RobotDynamicMsg getRobotDynamicData() throws ConnectInterruptedException;

    /**
     * @apiNote	    ��ȡ�����˾�̬��Ϣ
     * @return     �����˾�̬��Ϣ������ key:value ��ʽ����ַ��� ����ֱ�ӷ���json�ַ���
     */
    String getRobotStaticData() throws ConnectInterruptedException;

    /**
     * @apiNote	    ��ȡ�������ᶯ̬��Ϣ
     * @return     �������ᶯ̬��Ϣ������ key:value ��ʽ����ַ��� ����ֱ�ӷ���json�ַ���
     */
    AxleDynamicMsg getAxleDynameData() throws ConnectInterruptedException;

    /**
     * @apiNote	    ��ȡ�������ᾲ̬��Ϣ
     * @return     �������ᾲ̬��Ϣ������ key:value ��ʽ����ַ��� ����ֱ�ӷ���json�ַ���
     */
    String getAxleStaticData() throws ConnectInterruptedException;
}
