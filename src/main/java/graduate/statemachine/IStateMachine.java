package graduate.statemachine;

import graduate.model.logmodulemodel.LogEntry;

public interface IStateMachine 
{
	/**
	 *	��־Ӧ�õ�״̬���� 
	 */
	void apply(LogEntry loeEntry);
	
	/**
	 * 	��ȡ״̬����key��value
	 */
	LogEntry get(String key);
	
	/**
	 * ��ȡvalue
	 */
	String getString(String key);
	
	/**
	 *  ����value
	 */
	void setString(String key,String value);
	
	/**
	 * 	ɾ��
	 * @param key
	 */
	void delString(String... keys);
}
