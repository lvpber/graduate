package graduate.model.monitor;

import graduate.model.peer.Peer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Setter
@Getter
@ToString
public class JVMInfo implements Serializable {
    private Peer peer;          // ��ѯ�Ľڵ������
    private long freeMemory;    // jvmʣ���ڴ� ��λMB
    private long totalMemory;   // jvm�������ڴ��ʣ���� ��λMB
    private long maxMemory;     // jvm�����ԴӲ���ϵͳ��ȡ��ʣ���� ��λMB

    private JVMInfo() {}

    private static class SingleTonHolder {
        private static JVMInfo jvmInfo = new JVMInfo();
    }

    public static JVMInfo getInstance() {
        return SingleTonHolder.jvmInfo;
    }

    public static JVMInfo getInstanceNoSin() {
        return new JVMInfo();
    }
}
