package graduate.logmodule;

import graduate.model.logmodulemodel.LogEntry;

/**
 * 三大组件之一，日志管理模块
 * 采用文件实现，利用树莓派是嵌入式系统，性能受限
 * 读操作，load到内存中
 * 写操作，先对文件进行修改，再修改内存内容
 * @author 13299
 */
public interface ILogModule 
{
	/** 向日志库写一条日志，先序列化再写进内存 */
	void write(LogEntry logEntry);	
	
	/** 删除从startIndex位置开始的日志，先序列化再写到内存 */
	void removeOnStartIndex(Long startIndex);
	
	/** 读取指定index位置处的日志 */
	LogEntry read(Long index);
	
	/** 获取最后一条日志 */
	LogEntry getLast();
	
	/** 获取最后一条日志的index */
	Long getLastIndex();
}
