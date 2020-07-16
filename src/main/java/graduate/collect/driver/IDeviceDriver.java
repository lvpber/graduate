package graduate.collect.driver;

import graduate.model.collectingconfig.CollectingConfig;

/** �豸�ɼ������ӿ� */
public interface IDeviceDriver {
    /** �ɼ���ʼ�� */
    boolean init(CollectingConfig collectingConfig);

    /** �ɼ��������ͷ���Դ�����������ӣ�������������������ӱ��ɼ��豸����Ҫ�ͷ���Դ������socket */
    default void release() {
        System.out.println("this is deviceDriver#release");
    }
}
