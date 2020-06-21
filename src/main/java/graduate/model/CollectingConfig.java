package graduate.model;

import java.io.Serializable;

/**
 *	客户端发送的采集配置
 *	对指定设备进行采集
 *	先实现只能tcp采集 
 */
public class CollectingConfig implements Serializable 
{
	/** 标注指令是开始采集还是停止采集 */
	private boolean ifStart;
	
	/** 操作的设备 */
	private String deviceId;
	
	/** 采集的间隔 */
	private int collectInterval;
	
	/** 设备ip地址 */
	private String ipAddr;
	
	/** 设备端口号 */
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
