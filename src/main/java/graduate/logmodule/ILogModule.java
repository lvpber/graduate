package graduate.logmodule;

import graduate.model.logmodulemodel.LogEntry;

/**
 * �������֮һ����־����ģ��
 * �����ļ�ʵ�֣�������ݮ����Ƕ��ʽϵͳ����������
 * ��������load���ڴ���
 * д�������ȶ��ļ������޸ģ����޸��ڴ�����
 * @author 13299
 */
public interface ILogModule 
{
	/** ����־��дһ����־�������л���д���ڴ� */
	void write(LogEntry logEntry);	
	
	/** ɾ����startIndexλ�ÿ�ʼ����־�������л���д���ڴ� */
	void removeOnStartIndex(Long startIndex);
	
	/** ��ȡָ��indexλ�ô�����־ */
	LogEntry read(Long index);
	
	/** ��ȡ���һ����־ */
	LogEntry getLast();
	
	/** ��ȡ���һ����־��index */
	Long getLastIndex();
}
