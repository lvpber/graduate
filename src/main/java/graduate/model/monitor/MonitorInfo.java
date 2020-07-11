package graduate.model.monitor;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonitorInfo
{
    /** ��ʹ���ڴ�. */
    private long totalMemory;
    /** ʣ���ڴ�. */
    private long freeMemory;
    /** ����ʹ���ڴ�. */
    private long maxMemory;
    /** ����ϵͳ. */
    private String osName;
    /** �ܵ������ڴ�. */
    private long totalMemorySize;
    /** ʣ��������ڴ�. */
    private long freePhysicalMemorySize;
    /** ��ʹ�õ������ڴ�. */
    private long usedMemory;
    /** �߳�����. */
    private int totalThread;
    /** cpuʹ����. */
    private double cpuRatio;
}
