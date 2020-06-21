package graduate.statemachine;

import graduate.model.logmodulemodel.LogEntry;

public interface IStateMachine 
{
	/**
	 *	日志应用到状态机上 
	 */
	void apply(LogEntry loeEntry);
	
	/**
	 * 	获取状态机中key的value
	 */
	LogEntry get(String key);
	
	/**
	 * 获取value
	 */
	String getString(String key);
	
	/**
	 *  设置value
	 */
	void setString(String key,String value);
	
	/**
	 * 	删除
	 * @param key
	 */
	void delString(String... keys);
}
