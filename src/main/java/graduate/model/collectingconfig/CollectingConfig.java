package graduate.model.collectingconfig;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 *	�ͻ��˷��͵Ĳɼ�����
 *	��ָ���豸���вɼ�
 *	��ʵ��ֻ��tcp�ɼ� 
 */
@Setter
@Getter
public class CollectingConfig implements Serializable 
{
	private int frequency;				/** �ɼ���Ƶ�� */
	private String deviceId;			/** �������豸 */
	private String number;				/** �ɼ���� */
	private String operatorNumber;		/** �ɼ���Ա��� */
	private String operatorName;		/** �ɼ���Ա���� */
	private String description;			/** �ɼ����� */
	private String driverFilePath;		/** �ɼ������ļ�����·�� */
	/**
	 * ��Բɼ��豸����������豸����
	 * 1. ����tcpЭ���豸��
	 * 		1.1 ��ɫ ��client��server��
	 * 		1.2 �ɼ��豸ip��ַ
	 * 		1.3 �ɼ��豸�˿ں�
	 * 		1.4 other
	 * 2. ����OPC UAЭ���豸����
	 * 3. ����CAN����Э���豸����
	 * 4. ����Modbus RTC����Э���豸����
	 * 5. other Э��
	 */
	private String protocol;			/** ͨ��Э������ */
}
