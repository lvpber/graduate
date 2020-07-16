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
    private Peer peer;          // 查询的节点的内容
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

    public static JVMInfo getInstanceNoSin() {
        return new JVMInfo();
    }
}
