package graduate.model.collectingconfig;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 *	客户端发送的采集配置
 *	对指定设备进行采集
 *	先实现只能tcp采集 
 */
@Setter
@Getter
public class CollectingConfig implements Serializable 
{
	private int frequency;				/** 采集的频率 */
	private String deviceId;			/** 操作的设备 */
	private String number;				/** 采集编号 */
	private String operatorNumber;		/** 采集人员编号 */
	private String operatorName;		/** 采集人员姓名 */
	private String description;			/** 采集描述 */
	private String driverFilePath;		/** 采集驱动文件本地路径 */
	/**
	 * 针对采集设备的种类分类设备配置
	 * 1. 基于tcp协议设备：
	 * 		1.1 角色 （client、server）
	 * 		1.2 采集设备ip地址
	 * 		1.3 采集设备端口号
	 * 		1.4 other
	 * 2. 基于OPC UA协议设备配置
	 * 3. 基于CAN总线协议设备配置
	 * 4. 基于Modbus RTC总线协议设备配置
	 * 5. other 协议
	 */
	private String protocol;			/** 通信协议名称 */
}
