package graduate.model.monitor;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonitorInfo
{
    private long    totalMemory;                /** ��ʹ���ڴ�. */
    private long    freeMemory;                 /** ʣ���ڴ�. */
    private long    maxMemory;                  /** ����ʹ���ڴ�. */
    private String  osName;                     /** ����ϵͳ. */
    private long    totalMemorySize;            /** �ܵ������ڴ�. */
    private long    freePhysicalMemorySize;     /** ʣ��������ڴ�. */
    private long    usedMemory;                 /** ��ʹ�õ������ڴ�. */
    private int     totalThread;                /** �߳�����. */
    private double  cpuRatio;                   /** cpuʹ����. */
}
