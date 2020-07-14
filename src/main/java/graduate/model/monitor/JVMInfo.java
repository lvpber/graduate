package graduate.model.monitor;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class JVMInfo {
    private long freeMemory;    // jvm剩余内存 单位MB
    private long totalMemory;   // jvm所分配内存的剩余量 单位MB
    private long maxMemory;     // jvm最大可以从操作系统获取的剩余量 单位MB

    private JVMInfo() {}

    private static class SingleTonHolder {
        private static JVMInfo jvmInfo = new JVMInfo();
    }

    public static JVMInfo getInstance() {
        return SingleTonHolder.jvmInfo;
    }
}
