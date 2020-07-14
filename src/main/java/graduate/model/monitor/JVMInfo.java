package graduate.model.monitor;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class JVMInfo {
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
}
