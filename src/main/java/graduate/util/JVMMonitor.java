package graduate.util;

import graduate.model.monitor.JVMInfo;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class JVMMonitor {
    private static JVMInfo jvmInfo;

    /** 获取jvm当前的内存信息 */
    public static JVMInfo getJvmInfo() {
        jvmInfo = JVMInfo.getInstance();
        jvmInfo.setFreeMemory(Runtime.getRuntime().freeMemory()/1024/1024);
        jvmInfo.setMaxMemory(Runtime.getRuntime().maxMemory()/1024/1024);
        jvmInfo.setTotalMemory(Runtime.getRuntime().totalMemory()/1024/1024);
        return jvmInfo;
    }

    public static void main(String[] args) {
        System.out.println(JVMMonitor.getJvmInfo());
    }

}
