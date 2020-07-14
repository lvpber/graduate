package graduate.model.monitor;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonitorInfo
{
    private long    totalMemory;                /** 可使用内存. */
    private long    freeMemory;                 /** 剩余内存. */
    private long    maxMemory;                  /** 最大可使用内存. */
    private String  osName;                     /** 操作系统. */
    private long    totalMemorySize;            /** 总的物理内存. */
    private long    freePhysicalMemorySize;     /** 剩余的物理内存. */
    private long    usedMemory;                 /** 已使用的物理内存. */
    private int     totalThread;                /** 线程总数. */
    private double  cpuRatio;                   /** cpu使用率. */
}
