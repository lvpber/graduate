package graduate.model;

import java.io.Serializable;

/**
 *	�ͻ��˷��͵Ĳɼ�����
 *	��ָ���豸���вɼ�
 *	��ʵ��ֻ��tcp�ɼ� 
 */
public class CollectingConfig implements Serializable 
{
	/** ��עָ���ǿ�ʼ�ɼ�����ֹͣ�ɼ� */
	private boolean ifStart;
	
	/** �������豸 */
	private String deviceId;
	
	/** �ɼ��ļ�� */
	private int collectInterval;
	
	/** �豸ip��ַ */
	private String ipAddr;
	
	/** �豸�˿ں� */
	private int port;

	public boolean isIfStart() {
		return ifStart;
	}

	public void setIfStart(boolean ifStart) {
		this.ifStart = ifStart;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public int getCollectInterval() {
		return collectInterval;
	}

	public void setCollectInterval(int collectInterval) {
		this.collectInterval = collectInterval;
	}

	public String getIpAddr() {
		return ipAddr;
	}

	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
