/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.repository.model.NodeCpuInfo;
import com.clustercontrol.repository.model.NodeDeviceInfo;
import com.clustercontrol.repository.model.NodeDiskInfo;
import com.clustercontrol.repository.model.NodeFilesystemInfo;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.NodeMemoryInfo;
import com.clustercontrol.repository.model.NodeNetworkInterfaceInfo;
import com.clustercontrol.util.MessageConstant;

/**
 * このクラスはDeviceSearchの更新情報を持つクラスです。
 * DeviceSearchでノード情報を取得した場合に使用します。
 *
 * @since 5.0
 */
@XmlType(namespace = "http://repository.ws.clustercontrol.com")
public class NodeInfoDeviceSearch implements Serializable
{
	private static Log m_log = LogFactory.getLog( NodeInfoDeviceSearch.class );
	private static final long serialVersionUID = -6677435738596727809L;

	private NodeInfo nodeInfo = null;
	private NodeInfo newNodeInfo = null;
	private ArrayList<DeviceSearchMessageInfo> deviceSearchMessageInfo = new ArrayList<DeviceSearchMessageInfo>();
	private static String itemCpu = MessageConstant.CPU_LIST.getMessage();
	private static String itemMem = MessageConstant.MEMORY_LIST.getMessage();
	private static String itemNic = MessageConstant.NETWORK_INTERFACE_LIST.getMessage();
	private static String itemDisk = MessageConstant.DISK_LIST.getMessage();
	private static String itemFileSys = MessageConstant.FILE_SYSTEM_LIST.getMessage();
	private static String itemGenDev = MessageConstant.GENERAL_DEVICE_LIST.getMessage();
	private String errorMessage = null;

	/**
	 * 登録時エラーのあったノード詳細情報のsetter
	 * @param message
	 */
	public void setErrorMessage(String message) {
		this.errorMessage = message;
	}

	/**
	 * 登録時エラーのあったノード詳細情報のgetter
	 * @return message
	 */
	public String getErrorMessage() {
		return this.errorMessage;
	}

	/**
	 * ノード詳細情報のsetter
	 * @param nodeInfo
	 */
	public void setNodeInfo(NodeInfo nodeInfo) {
		nodeInfo.setDefaultInfo();
		this.nodeInfo = nodeInfo;
	}

	/**
	 * ノード詳細情報のgetter
	 * @return nodeInfo
	 */
	public NodeInfo getNodeInfo() {
		return this.nodeInfo;
	}

	/**
	 * 取得後のノード詳細情報のgetter
	 * @return newNodeInfo
	 */
	public NodeInfo getNewNodeInfo() {
		return this.newNodeInfo;
	}

	/**
	 * メッセージのsetter
	 */
	public void setDeviceSearchMessageInfo(ArrayList<DeviceSearchMessageInfo> deviceSearchMessageInfo) {
		this.deviceSearchMessageInfo = deviceSearchMessageInfo;
	}

	/**
	 * メッセージのgetter
	 */
	public ArrayList<DeviceSearchMessageInfo> getDeviceSearchMessageInfo() {
		return this.deviceSearchMessageInfo;
	}

	private void setMessage(String item, String lastVal, String thisVal) {
		DeviceSearchMessageInfo msgInfo = new DeviceSearchMessageInfo();
		msgInfo.setItemName(item);
		msgInfo.setLastVal(lastVal);
		msgInfo.setThisVal(thisVal);
		deviceSearchMessageInfo.add(msgInfo);
	}

	/**
	 * ノード詳細情報が等しいかを返す
	 * 検出対象でない場合は前回情報に置き換え
	 * @param lastNode 前回のノード詳細情報
	 * @return 検出対象の場合、前回と同一か
	 */
	public boolean equalsNodeInfo(NodeInfo lastNode) {
		boolean equalsBasic = true;
		boolean equalsHostName = true;
		boolean equalsCpu = true;
		boolean equalsMem = true;
		boolean equalsNic = true;
		boolean equalsFs = true;
		boolean equalsDisk = true;
		boolean equalsDevice = true;

		//サーバ基本情報
		equalsBasic = equalsNodeBasicInfo(lastNode);

		//ホスト名
		if (HinemosPropertyCommon.repository_device_search_prop_node_config_hostname.getBooleanValue()) {
			if( nodeInfo.getNodeHostnameInfo() == null || nodeInfo.getNodeHostnameInfo().isEmpty() )
			{
				equalsHostName = lastNode.getNodeHostnameInfo() == null || lastNode.getNodeHostnameInfo().isEmpty();
			} else if (lastNode.getNodeHostnameInfo() == null || lastNode.getNodeHostnameInfo().isEmpty()) {
				// 前回値が空で、今回のデバイスサーチで値がある場合（必ず同一ではない）
				equalsHostName = false;
				setMessage(MessageConstant.HOST_NAME.getMessage(),
						MessageConstant.NONEXISTENT.getMessage(),
						nodeInfo.getNodeHostnameInfo().get(0).getHostname());
			} else if(lastNode.getNodeHostnameInfo().size()>1){
				//複数ホスト名を登録された場合（更新されるようにする）
				equalsHostName = false;
				String lastNodeHostnames = lastNode.getNodeHostnameInfo().get(0).getHostname();
				for(int iter = 1; iter < lastNode.getNodeHostnameInfo().size(); iter++){//setMessage()のためすべてのホスト名を取得
					lastNodeHostnames = lastNodeHostnames.concat("\n" + lastNode.getNodeHostnameInfo().get(iter).getHostname());
				}
				setMessage(MessageConstant.HOST_NAME.getMessage(),
						lastNodeHostnames,
						nodeInfo.getNodeHostnameInfo().get(0).getHostname());
			} else {
				equalsHostName = nodeInfo
									.getNodeHostnameInfo()
									.get(0)
									.getHostname()
									.equals(lastNode.getNodeHostnameInfo().get(0)
											.getHostname());
				if (nodeInfo
						.getNodeHostnameInfo()
						.get(0)
						.getHostname()
						.equals(lastNode.getNodeHostnameInfo().get(0)
								.getHostname()) == false) {
					setMessage(MessageConstant.HOST_NAME.getMessage(),
							lastNode.getNodeHostnameInfo().get(0).getHostname(),
							nodeInfo.getNodeHostnameInfo().get(0).getHostname());
				}
			}
		}

		//CPU情報
		if (HinemosPropertyCommon.repository_device_search_prop_device_cpu.getBooleanValue()) {
			Set<NodeCpuInfo> last = new HashSet<NodeCpuInfo>();
			if (lastNode.getNodeCpuInfo() != null) {
				last.addAll(lastNode.getNodeCpuInfo());
			}
			
			Set<NodeCpuInfo> current = new HashSet<NodeCpuInfo>();
			if (nodeInfo.getNodeCpuInfo() != null) {
				current.addAll(nodeInfo.getNodeCpuInfo());
				for (NodeCpuInfo cpu : last) {
					if (cpu != null && ! "cpu".equals(cpu.getDeviceType())) {
						// cpu以外のものはSearchされないため、非更新対象として扱う
						current.add(cpu);
					}
				}
			}
			
			if (last.size() == 0) {
				equalsCpu = current.size() == 0 ? true : false;
				if(equalsCpu == false) {
					//前回ノード情報:なし 今回ノード情報:あり
					setMessage(itemCpu, MessageConstant.NONEXISTENT.getMessage(), MessageConstant.EXISTENT.getMessage());
				}
			} else if (current.size() == 0) {
				equalsCpu = false;
				//前回ノード情報:あり 今回ノード情報:なし
				setMessage(itemCpu, MessageConstant.EXISTENT.getMessage(), MessageConstant.NONEXISTENT.getMessage());
			} else if (last.equals(current)) {
				equalsCpu = true;
			} else {
				equalsCpu = equalsWithMap(itemCpu, new ArrayList<NodeDeviceInfo>(last), new ArrayList<NodeDeviceInfo>(current));
			}
			
			nodeInfo.getNodeCpuInfo().clear();
			nodeInfo.getNodeCpuInfo().addAll(current);
		}

		//メモリ情報
		if (HinemosPropertyCommon.repository_device_search_prop_device_memory.getBooleanValue()) {
			Set<NodeMemoryInfo> last = new HashSet<NodeMemoryInfo>();
			if (lastNode.getNodeMemoryInfo() != null) {
				last.addAll(lastNode.getNodeMemoryInfo());
			}
			
			Set<NodeMemoryInfo> current = new HashSet<NodeMemoryInfo>();
			if (nodeInfo.getNodeMemoryInfo() != null) {
				current.addAll(nodeInfo.getNodeMemoryInfo());
				for (NodeMemoryInfo mem : last) {
					if (mem != null && ! "mem".equals(mem.getDeviceType())) {
						// mem以外のものはSearchされないため、非更新対象として扱う
						current.add(mem);
					}
				}
			}
			
			if (last.size() == 0) {
				equalsMem = current.size() == 0 ? true : false;
				if(!equalsMem) {
					//前回ノード情報:なし 今回ノード情報:あり
					setMessage(itemMem, MessageConstant.NONEXISTENT.getMessage(), MessageConstant.EXISTENT.getMessage());
				}
			} else if (current.size() == 0) {
				equalsMem = false;
				//前回ノード情報:あり 今回ノード情報:なし
				setMessage(itemMem, MessageConstant.EXISTENT.getMessage(), MessageConstant.NONEXISTENT.getMessage());
			} else if (last.equals(current)) {
				equalsMem = true;
			} else {
				equalsMem = equalsWithMap(itemMem, new ArrayList<NodeDeviceInfo>(last), new ArrayList<NodeDeviceInfo>(current));
			}
			
			nodeInfo.getNodeMemoryInfo().clear();
			nodeInfo.getNodeMemoryInfo().addAll(current);
		}

		//NIC情報
		if (HinemosPropertyCommon.repository_device_search_prop_device_nic.getBooleanValue()) {
			Set<NodeNetworkInterfaceInfo> last = new HashSet<NodeNetworkInterfaceInfo>();
			if (lastNode.getNodeNetworkInterfaceInfo() != null) {
				last.addAll(lastNode.getNodeNetworkInterfaceInfo());
			}
			
			Set<NodeNetworkInterfaceInfo> current = new HashSet<NodeNetworkInterfaceInfo>();
			if (nodeInfo.getNodeNetworkInterfaceInfo() != null) {
				current.addAll(nodeInfo.getNodeNetworkInterfaceInfo());
				for (NodeNetworkInterfaceInfo nic : last) {
					if (nic != null && ! "nic".equals(nic.getDeviceType())) {
						// nic以外のものはSearchされないため、非更新対象として扱う
						current.add(nic);
					}
				}
			}
			
			//NIC情報
			if (last.size() == 0) {
				equalsNic = current.size() == 0 ? true : false;
				if(! equalsNic) {
					//前回ノード情報:なし 今回ノード情報:あり
					setMessage(itemNic, MessageConstant.NONEXISTENT.getMessage(), MessageConstant.EXISTENT.getMessage());
				}
			} else if (current.size() == 0) {
				equalsNic = false;
				//前回ノード情報:あり 今回ノード情報:なし
				setMessage(itemNic, MessageConstant.EXISTENT.getMessage(), MessageConstant.NONEXISTENT.getMessage());
			} else if (last.equals(current)) {
				equalsNic = true;
			} else {
				equalsNic = equalsWithMap(itemNic, new ArrayList<NodeDeviceInfo>(last), new ArrayList<NodeDeviceInfo>(current));
			}
			
			nodeInfo.getNodeNetworkInterfaceInfo().clear();
			nodeInfo.getNodeNetworkInterfaceInfo().addAll(current);
		}

		//ディスク情報
		if (HinemosPropertyCommon.repository_device_search_prop_device_disk.getBooleanValue()) {
			Set<NodeDiskInfo> last = new HashSet<NodeDiskInfo>();
			if (lastNode.getNodeDiskInfo() != null) {
				last.addAll(lastNode.getNodeDiskInfo());
			}
			
			Set<NodeDiskInfo> current = new HashSet<NodeDiskInfo>();
			if (nodeInfo.getNodeDiskInfo() != null) {
				current.addAll(nodeInfo.getNodeDiskInfo());
				for (NodeDiskInfo disk : last) {
					if (disk != null && ! "disk".equals(disk.getDeviceType())) {
						// disk以外のものはSearchされないため、非更新対象として扱う
						current.add(disk);
					}
				}
			}
			
			if (last.size() == 0) {
				equalsDisk = current.size() == 0 ? true : false;
				if(! equalsDisk) {
					//前回ノード情報:なし 今回ノード情報:あり
					setMessage(itemDisk, MessageConstant.NONEXISTENT.getMessage(), MessageConstant.EXISTENT.getMessage());
				}
			} else if (current.size() == 0) {
				equalsDisk = false;
				//前回ノード情報:あり 今回ノード情報:なし
				setMessage(itemDisk, MessageConstant.EXISTENT.getMessage(), MessageConstant.NONEXISTENT.getMessage());
			} else if (last.equals(current)) {
				equalsDisk = true;
			} else {
				equalsDisk = equalsWithMap(itemDisk, new ArrayList<NodeDeviceInfo>(last), new ArrayList<NodeDeviceInfo>(current));
			}
			
			nodeInfo.getNodeDiskInfo().clear();
			nodeInfo.getNodeDiskInfo().addAll(current);
		}

		//ファイルシステム情報
		if (HinemosPropertyCommon.repository_device_search_prop_device_filesystem.getBooleanValue()) {
			Set<NodeFilesystemInfo> last = new HashSet<NodeFilesystemInfo>();
			if (lastNode.getNodeFilesystemInfo() != null) {
				last.addAll(lastNode.getNodeFilesystemInfo());
			}
			
			Set<NodeFilesystemInfo> current = new HashSet<NodeFilesystemInfo>();
			if (nodeInfo.getNodeFilesystemInfo() != null) {
				current.addAll(nodeInfo.getNodeFilesystemInfo());
				for (NodeFilesystemInfo fs : last) {
					if (fs != null && ! "filesystem".equals(fs.getDeviceType())) {
						// filesystem以外のものはSearchされないため、非更新対象として扱う
						current.add(fs);
					}
				}
			}
			
			if (last.size() == 0) {
				equalsFs = current.size() == 0 ? true : false;
				if(!equalsFs) {
					//前回ノード情報:なし 今回ノード情報:あり
					setMessage(itemFileSys, MessageConstant.NONEXISTENT.getMessage(), MessageConstant.EXISTENT.getMessage());
				}
			} else if (current.size() == 0) {
				equalsFs = false;
				//前回ノード情報:あり 今回ノード情報:なし
				setMessage(itemFileSys, MessageConstant.EXISTENT.getMessage(), MessageConstant.NONEXISTENT.getMessage());
			} else if (last.equals(current)) {
				equalsFs = true;
			} else {
				equalsFs = equalsWithMap(itemFileSys, new ArrayList<NodeDeviceInfo>(last), new ArrayList<NodeDeviceInfo>(current));
			}
			
			nodeInfo.getNodeFilesystemInfo().clear();
			nodeInfo.getNodeFilesystemInfo().addAll(current);
		}

		//汎用デバイス情報(自動検出されないため、trueにすると必ず削除されることを注意せよ）
		if (HinemosPropertyCommon.repository_device_search_prop_device_general.getBooleanValue()) {
			Set<NodeDeviceInfo> last = new HashSet<NodeDeviceInfo>();
			if (lastNode.getNodeDeviceInfo() != null) {
				last.addAll(lastNode.getNodeDeviceInfo());
			}
			
			Set<NodeDeviceInfo> current = new HashSet<NodeDeviceInfo>();
			if (nodeInfo.getNodeDeviceInfo() != null) {
				current.addAll(nodeInfo.getNodeDeviceInfo());
			}
			
			if (last.size() == 0) {
				equalsDevice = current.size() == 0 ? true : false;
				if(!equalsDevice) {
					//前回ノード情報:なし 今回ノード情報:あり
					setMessage(itemGenDev, MessageConstant.NONEXISTENT.getMessage(), MessageConstant.EXISTENT.getMessage());
				}
			} else if (current.size() == 0) {
				equalsDevice = false;
				//前回ノード情報:あり 今回ノード情報:なし
				setMessage(itemGenDev, MessageConstant.EXISTENT.getMessage(), MessageConstant.NONEXISTENT.getMessage());
			} else if (last.equals(current)) {
				// do nothing.
			} else {
				equalsDevice = equalsWithMap(itemGenDev, new ArrayList<NodeDeviceInfo>(last), new ArrayList<NodeDeviceInfo>(current));
			}
		}

		boolean equals = equalsBasic && equalsHostName && equalsCpu && equalsMem && equalsNic && equalsDisk
				&& equalsFs && equalsDevice;

		if (equals == false) {
			//前回情報をベースとする
			this.newNodeInfo = lastNode.clone();
			//取得した情報で更新
			if (equalsBasic == false) {
				// サーバ基本情報->ハードウェア
				if (HinemosPropertyCommon.repository_device_search_prop_basic_hardware.getBooleanValue()) {
					this.newNodeInfo.setPlatformFamily(this.nodeInfo.getPlatformFamily());
					//SNMPで取得される情報ではないためサブプラットフォームは置き換えない
					//(置き換えてしまうとクラウド仮想化オプション利用時にリソース監視(メトリクス)が動作しなくなる)
					this.newNodeInfo.setHardwareType(this.nodeInfo.getHardwareType());
					this.newNodeInfo.setIconImage(this.nodeInfo.getIconImage());
				}
				// サーバ基本情報->ネットワーク
				if (HinemosPropertyCommon.repository_device_search_prop_basic_network.getBooleanValue()) {
					this.newNodeInfo.setIpAddressVersion(this.nodeInfo.getIpAddressVersion());
					this.newNodeInfo.setIpAddressV4(this.nodeInfo.getIpAddressV4());
					this.newNodeInfo.setIpAddressV6(this.nodeInfo.getIpAddressV6());
					
					// ノード名更新の要否を判定(自動デバイスサーチが優先、または対象ノードのクラウド自動検知が無効の場合に更新する)
					boolean isPropertyUpdate = false;
					String pirorityName = HinemosPropertyCommon.repository_node_config_nodename_update_priority.getStringValue();
					switch(pirorityName){
					case "device_search":
						isPropertyUpdate = true;
						break;
					case "xcloud_auto_detection":
						isPropertyUpdate = false;
						break;
					default:
						isPropertyUpdate = true;
						m_log.info("invalid property value. name = repository.node.config.nodename.update.priority, value = " + pirorityName);
						break;
					}
					
					if (isPropertyUpdate || // 自動デバイスサーチ優先
							"off".equals(HinemosPropertyCommon.xcloud_autoupdate_interval.getStringValue()) || // クラウド自動検知が無効
							!HinemosPropertyCommon.xcloud_node_property_nodename_update.getBooleanValue() || // クラウド自動検知のホスト名の更新が無効
							this.newNodeInfo.getCloudScope() == null || this.newNodeInfo.getCloudScope().length() == 0){ // 対象ノードがクラウド自動検知対象外
						
						this.newNodeInfo.setNodeName(this.nodeInfo.getNodeName());

					}
				}
				// サーバ基本情報->OS
				if (HinemosPropertyCommon.repository_device_search_prop_basic_os.getBooleanValue()) {
					this.newNodeInfo.getNodeOsInfo().setOsName(this.nodeInfo.getNodeOsInfo().getOsName());
					this.newNodeInfo.getNodeOsInfo().setOsRelease(this.nodeInfo.getNodeOsInfo().getOsRelease());
					this.newNodeInfo.getNodeOsInfo().setOsVersion(this.nodeInfo.getNodeOsInfo().getOsVersion());
					this.newNodeInfo.getNodeOsInfo().setCharacterSet(this.nodeInfo.getNodeOsInfo().getCharacterSet());
				}
				// サーバ基本情報->Hinemosエージェント
				if (HinemosPropertyCommon.repository_device_search_prop_basic_agent.getBooleanValue()) {
					this.newNodeInfo.setAgentAwakePort(this.nodeInfo.getAgentAwakePort());
				}
			}
			if (equalsHostName == false) {
				this.newNodeInfo.setNodeHostnameInfo(this.nodeInfo.getNodeHostnameInfo());
			}
			if (equalsCpu == false) {
				this.newNodeInfo.setNodeCpuInfo(this.nodeInfo.getNodeCpuInfo());
			}
			if (equalsMem == false) {
				this.newNodeInfo.setNodeMemoryInfo(this.nodeInfo.getNodeMemoryInfo());
			}
			if (equalsNic == false) {
				this.newNodeInfo.setNodeNetworkInterfaceInfo(this.nodeInfo.getNodeNetworkInterfaceInfo());
			}
			if (equalsDisk == false) {
				this.newNodeInfo.setNodeDiskInfo(this.nodeInfo.getNodeDiskInfo());
			}
			if (equalsFs == false) {
				this.newNodeInfo.setNodeFilesystemInfo(this.nodeInfo.getNodeFilesystemInfo());
			}
			if (equalsDevice == false) {
				this.newNodeInfo.setNodeDeviceInfo(this.nodeInfo.getNodeDeviceInfo());
			}
		}

		return equals;
	}

	/**
	 * 基本情報が検出対象の場合は前回情報と比較
	 * 検出対象でない場合は前回情報に置き換え
	 * @param lastNode
	 * @return 検出対象の場合、前回と同一か
	 */
	private boolean equalsNodeBasicInfo(NodeInfo lastNode) {
		boolean lEquals = true;

		if (HinemosPropertyCommon.repository_device_search_prop_basic_hardware.getBooleanValue()) {
			// HW
			if( nodeInfo.getPlatformFamily() == null ) {
				lEquals = lEquals && ( lastNode.getPlatformFamily() == null );
				if (lastNode.getPlatformFamily() != null) {
					setMessage(MessageConstant.BASIC_INFORMATION.getMessage() + "." + MessageConstant.HARDWARE.getMessage() + "." + MessageConstant.PLATFORM_FAMILY_NAME.getMessage(),
							MessageConstant.EXISTENT.getMessage(),
							MessageConstant.NONEXISTENT.getMessage());
				}
			} else {
				lEquals = lEquals && nodeInfo.getPlatformFamily().equals( lastNode.getPlatformFamily() );
				if (nodeInfo.getPlatformFamily().equals( lastNode.getPlatformFamily()) == false) {
					setMessage(MessageConstant.BASIC_INFORMATION.getMessage() + "." + MessageConstant.HARDWARE.getMessage() + "." + MessageConstant.PLATFORM_FAMILY_NAME.getMessage(),
							lastNode.getPlatformFamily(),
							nodeInfo.getPlatformFamily());
				}
			}
			//SNMPで取得される情報ではないためサブプラットフォームは置き換えない
			//(置き換えてしまうとクラウド仮想化オプション利用時にリソース監視(メトリクス)が動作しなくなる)
			if( nodeInfo.getHardwareType() == null ) {
				lEquals = lEquals && ( lastNode.getHardwareType() == null );
				if (lastNode.getHardwareType() != null) {
					setMessage(MessageConstant.BASIC_INFORMATION.getMessage() + "." + MessageConstant.HARDWARE.getMessage() + "." + MessageConstant.HARDWARE_TYPE.getMessage(),
							MessageConstant.EXISTENT.getMessage(),
							MessageConstant.NONEXISTENT.getMessage());
				}
			} else {
				lEquals = lEquals && nodeInfo.getHardwareType().equals( lastNode.getHardwareType() );
				if (nodeInfo.getHardwareType().equals( lastNode.getHardwareType()) == false) {
					setMessage(MessageConstant.BASIC_INFORMATION.getMessage() + "." + MessageConstant.HARDWARE.getMessage() + "." + MessageConstant.HARDWARE_TYPE.getMessage(),
							lastNode.getHardwareType(),
							nodeInfo.getHardwareType());
				}
			}

			if( nodeInfo.getIconImage() == null ) {
				lEquals = lEquals && ( lastNode.getIconImage() == null );
				if (lastNode.getIconImage() != null) {
					setMessage(MessageConstant.BASIC_INFORMATION.getMessage() + "." + MessageConstant.HARDWARE.getMessage() + "." + MessageConstant.ICON_IMAGE.getMessage(),
							MessageConstant.EXISTENT.getMessage(),
							MessageConstant.NONEXISTENT.getMessage());
				}
			} else {
				lEquals = lEquals && nodeInfo.getIconImage().equals( lastNode.getIconImage() );
				if (nodeInfo.getIconImage().equals( lastNode.getIconImage()) == false) {
					setMessage(MessageConstant.BASIC_INFORMATION.getMessage() + "." + MessageConstant.HARDWARE.getMessage() + "." + MessageConstant.ICON_IMAGE.getMessage(),
							lastNode.getIconImage(),
							nodeInfo.getIconImage());
				}
			}
		}

		// IP アドレス
		if (HinemosPropertyCommon.repository_device_search_prop_basic_network.getBooleanValue()) {
			if( nodeInfo.getIpAddressVersion() == null )
			{
				lEquals = lEquals && ( lastNode.getIpAddressVersion() == null );
				if (lastNode.getIpAddressVersion() != null) {
					setMessage(MessageConstant.BASIC_INFORMATION.getMessage() + "." + MessageConstant.NETWORK.getMessage() + "." + MessageConstant.IP_ADDRESS_VERSION.getMessage(),
							MessageConstant.EXISTENT.getMessage(),
							MessageConstant.NONEXISTENT.getMessage());
				}
			} else {
				lEquals = lEquals && nodeInfo.getIpAddressVersion().equals( lastNode.getIpAddressVersion() );
				if (nodeInfo.getIpAddressVersion().equals( lastNode.getIpAddressVersion() ) == false) {
					setMessage(MessageConstant.BASIC_INFORMATION.getMessage() + "." + MessageConstant.NETWORK.getMessage() + "." + MessageConstant.IP_ADDRESS_VERSION.getMessage(),
							lastNode.getIpAddressVersion().toString(),
							nodeInfo.getIpAddressVersion().toString());
				}
			}
			if( nodeInfo.getIpAddressV4() == null )
			{
				lEquals = lEquals && ( lastNode.getIpAddressV4() == null );
			} else {
				lEquals = lEquals && nodeInfo.getIpAddressV4().equals( lastNode.getIpAddressV4() );
				if (nodeInfo.getIpAddressV4().equals( lastNode.getIpAddressV4() ) == false) {
					setMessage(MessageConstant.BASIC_INFORMATION.getMessage() + "." + MessageConstant.NETWORK.getMessage() + "." + MessageConstant.IP_ADDRESS_V4.getMessage(),
							lastNode.getIpAddressV4(),
							nodeInfo.getIpAddressV4());
				}
			}
			if( nodeInfo.getIpAddressV6() == null )
			{
				lEquals = lEquals && ( lastNode.getIpAddressV6() == null );
			} else {
				lEquals = lEquals && nodeInfo.getIpAddressV6().equals( lastNode.getIpAddressV6() );
				if (nodeInfo.getIpAddressV6().equals( lastNode.getIpAddressV6() ) == false) {
					setMessage(MessageConstant.BASIC_INFORMATION.getMessage() + "." + MessageConstant.NETWORK.getMessage() + "." + MessageConstant.IP_ADDRESS_V6.getMessage(),
							lastNode.getIpAddressV6(),
							nodeInfo.getIpAddressV6());
				}
			}
			if( nodeInfo.getNodeName() == null )
			{
				lEquals = lEquals && ( lastNode.getNodeName() == null );
			} else {
				lEquals = lEquals && nodeInfo.getNodeName().equals( lastNode.getNodeName() );
				if (nodeInfo.getNodeName().equals( lastNode.getNodeName() ) == false) {
					setMessage(MessageConstant.BASIC_INFORMATION.getMessage() + "." + MessageConstant.NETWORK.getMessage() + "." + MessageConstant.NODE_NAME.getMessage(),
							lastNode.getNodeName(),
							nodeInfo.getNodeName());
				}
			}
		}

		// OS
		if (HinemosPropertyCommon.repository_device_search_prop_basic_os.getBooleanValue()) {
			if( nodeInfo.getNodeOsInfo().getOsName() == null )
			{
				lEquals = lEquals && ( lastNode.getNodeOsInfo().getOsName() == null );
				if (lastNode.getNodeOsInfo().getOsName() != null) {
					setMessage(MessageConstant.BASIC_INFORMATION.getMessage() + "." + MessageConstant.OS.getMessage() + "." + MessageConstant.OS_NAME.getMessage(),
							MessageConstant.EXISTENT.getMessage(),
							MessageConstant.NONEXISTENT.getMessage());
				}
			} else {
				lEquals = lEquals && nodeInfo.getNodeOsInfo().getOsName().equals( lastNode.getNodeOsInfo().getOsName() );
				if (nodeInfo.getNodeOsInfo().getOsName().equals( lastNode.getNodeOsInfo().getOsName() ) == false ) {
					setMessage(MessageConstant.BASIC_INFORMATION.getMessage() + "." + MessageConstant.OS.getMessage() + "." + MessageConstant.OS_NAME.getMessage(),
							lastNode.getNodeOsInfo().getOsName(),
							nodeInfo.getNodeOsInfo().getOsName());
				}
			}
			if( nodeInfo.getNodeOsInfo().getOsRelease() == null )
			{
				lEquals = lEquals && ( lastNode.getNodeOsInfo().getOsRelease() == null );
				if (lastNode.getNodeOsInfo().getOsRelease() != null) {
					setMessage(MessageConstant.BASIC_INFORMATION.getMessage() + "." + MessageConstant.OS.getMessage() + "." + MessageConstant.OS_RELEASE.getMessage(),
							MessageConstant.EXISTENT.getMessage(),
							MessageConstant.NONEXISTENT.getMessage());
				}
			} else {
				lEquals = lEquals && nodeInfo.getNodeOsInfo().getOsRelease().equals( lastNode.getNodeOsInfo().getOsRelease() );
				if (nodeInfo.getNodeOsInfo().getOsRelease().equals( lastNode.getNodeOsInfo().getOsRelease() ) == false ) {
					setMessage(MessageConstant.BASIC_INFORMATION.getMessage() + "." + MessageConstant.OS.getMessage() + "." + MessageConstant.OS_RELEASE.getMessage(),
							lastNode.getNodeOsInfo().getOsRelease(),
							nodeInfo.getNodeOsInfo().getOsRelease());
				}
			}
			if( nodeInfo.getNodeOsInfo().getOsVersion() == null )
			{
				lEquals = lEquals && ( lastNode.getNodeOsInfo().getOsVersion() == null );
				if (lastNode.getNodeOsInfo().getOsVersion() != null) {
					setMessage(MessageConstant.BASIC_INFORMATION.getMessage() + "." + MessageConstant.OS.getMessage() + "." + MessageConstant.OS_VERSION.getMessage(),
							MessageConstant.EXISTENT.getMessage(),
							MessageConstant.NONEXISTENT.getMessage());
				}
			} else {
				lEquals = lEquals && nodeInfo.getNodeOsInfo().getOsVersion().equals( lastNode.getNodeOsInfo().getOsVersion() );
				if (nodeInfo.getNodeOsInfo().getOsVersion().equals( lastNode.getNodeOsInfo().getOsVersion() ) == false ) {
					setMessage(MessageConstant.BASIC_INFORMATION.getMessage() + "." + MessageConstant.OS.getMessage() + "." + MessageConstant.OS_VERSION.getMessage(),
							lastNode.getNodeOsInfo().getOsVersion(),
							nodeInfo.getNodeOsInfo().getOsVersion());
				}
			}
			if( nodeInfo.getNodeOsInfo().getCharacterSet() == null )
			{
				lEquals = lEquals && ( lastNode.getNodeOsInfo().getCharacterSet() == null );
				if (lastNode.getNodeOsInfo().getCharacterSet() != null) {
					setMessage(MessageConstant.BASIC_INFORMATION.getMessage() + "." + MessageConstant.OS.getMessage() + "." + MessageConstant.CHARACTER_SET.getMessage(),
							MessageConstant.EXISTENT.getMessage(),
							MessageConstant.NONEXISTENT.getMessage());
				}
			} else {
				lEquals = lEquals && nodeInfo.getNodeOsInfo().getCharacterSet().equals( lastNode.getNodeOsInfo().getCharacterSet() );
				if (nodeInfo.getNodeOsInfo().getCharacterSet().equals( lastNode.getNodeOsInfo().getCharacterSet() ) == false ) {
					setMessage(MessageConstant.BASIC_INFORMATION.getMessage() + "." + MessageConstant.OS.getMessage() + "." + MessageConstant.CHARACTER_SET.getMessage(),
							lastNode.getNodeOsInfo().getCharacterSet(),
							nodeInfo.getNodeOsInfo().getCharacterSet());
				}
			}
		}

		// Hinemosエージェント
		if (HinemosPropertyCommon.repository_device_search_prop_basic_agent.getBooleanValue()) {
			if( nodeInfo.getAgentAwakePort() == null )
			{
				lEquals = lEquals && ( lastNode.getAgentAwakePort() == null );
				if (lastNode.getAgentAwakePort() != null) {
					setMessage(MessageConstant.BASIC_INFORMATION.getMessage() + "." + MessageConstant.AGENT.getMessage() + "." + MessageConstant.AGENT_AWAKE_PORT.getMessage(),
							MessageConstant.EXISTENT.getMessage(),
							MessageConstant.NONEXISTENT.getMessage());
				}
			} else {
				lEquals = lEquals && nodeInfo.getAgentAwakePort().equals( lastNode.getAgentAwakePort() );
				if (nodeInfo.getAgentAwakePort().equals( lastNode.getAgentAwakePort() ) == false ) {
					setMessage(MessageConstant.BASIC_INFORMATION.getMessage() + "." + MessageConstant.AGENT.getMessage() + "." + MessageConstant.AGENT_AWAKE_PORT.getMessage(),
									lastNode.getAgentAwakePort().toString(),
									nodeInfo.getAgentAwakePort().toString());
				}
			}
		}

		return lEquals;
	}

	private boolean equalsNodeDeviceInfo(String itemNm,
			NodeDeviceInfo lastDI, NodeDeviceInfo thisDI) {
		boolean equalsDevice = true;

		if (!lastDI.getDeviceDescription().equals(thisDI.getDeviceDescription())) {
			equalsDevice = false;
			String item = itemNm + "." + MessageConstant.DESCRIPTION.getMessage();
			setMessage(item, lastDI.getDeviceDescription(), thisDI.getDeviceDescription());
		}
		if (!lastDI.getDeviceDisplayName().equals(thisDI.getDeviceDisplayName())) {
			equalsDevice = false;
			String item = itemNm + "." + MessageConstant.DEVICE_DISPLAY_NAME.getMessage();
			setMessage(item, lastDI.getDeviceDisplayName(), thisDI.getDeviceDisplayName());
		}
		if (!lastDI.getDeviceIndex().equals(thisDI.getDeviceIndex())) {
			equalsDevice = false;
			String item = itemNm + "." + MessageConstant.DEVICE_INDEX.getMessage();
			setMessage(
							item,
							lastDI.getDeviceIndex() == null ? "" : lastDI.getDeviceIndex().toString(),
							thisDI.getDeviceIndex() == null ? "" : thisDI.getDeviceIndex().toString());
		}
		if (!lastDI.getDeviceName().equals(thisDI.getDeviceName())) {
			equalsDevice = false;
			String item = itemNm + "." + MessageConstant.DEVICE_NAME.getMessage();
			setMessage(item, lastDI.getDeviceName(), thisDI.getDeviceName());
		}
		if (!lastDI.getDeviceSize().equals(thisDI.getDeviceSize())) {
			equalsDevice = false;
			String item = itemNm + "." + MessageConstant.DEVICE_SIZE.getMessage();
			setMessage(
							item,
							lastDI.getDeviceSize() == null ? "" : lastDI.getDeviceSize().toString(),
							thisDI.getDeviceSize() == null ? "" : thisDI.getDeviceSize().toString());
		}
		if (!lastDI.getDeviceSizeUnit().equals(thisDI.getDeviceSizeUnit())) {
			equalsDevice = false;
			String item = itemNm + "." + MessageConstant.DEVICE_SIZE_UNIT.getMessage();
			setMessage(item, lastDI.getDeviceIndex().toString(), thisDI.getDeviceIndex().toString());
		}
		if (!lastDI.getDeviceType().equals(thisDI.getDeviceType())) {
			equalsDevice = false;
			String item = itemNm + "." + MessageConstant.DEVICE_TYPE.getMessage();
			setMessage(item, lastDI.getDeviceType(), thisDI.getDeviceType());
		}
		return equalsDevice;
	}

	private boolean equalsWithMap(String itemName, ArrayList<NodeDeviceInfo> lastList, ArrayList<NodeDeviceInfo> thisList) {
		boolean equalsInfo = true;

		HashMap<String, NodeDeviceInfo> lastMap = new HashMap<String, NodeDeviceInfo>();
		HashMap<String, NodeDeviceInfo> thisMap = new HashMap<String, NodeDeviceInfo>();

		//比較のためMapに詰め替える
		for (int i=0; lastList.size() > i; i++) {
			NodeDeviceInfo lastDI = lastList.get(i);
			String key = lastDI.getDeviceIndex().toString() + "."  + lastDI.getDeviceType() + "."  + lastDI.getDeviceName();
			lastMap.put(key, lastDI);
		}

		for (int i=0; thisList.size() > i; i++) {
			NodeDeviceInfo thisDI =  thisList.get(i);
			String key = thisDI.getDeviceIndex().toString() + "."  + thisDI.getDeviceType() + "."  + thisDI.getDeviceName();
			thisMap.put(key, thisDI);
		}

		//比較
		for (int i=0; thisList.size() > i; i++) {
			NodeDeviceInfo thisDI = (NodeDeviceInfo)thisList.get(i);
			String key = thisDI.getDeviceIndex().toString() + "."  + thisDI.getDeviceType() + "."  + thisDI.getDeviceName();
			if (lastMap.containsKey(key)) {
				equalsInfo = equalsInfo && equalsNodeDeviceInfo(itemName, lastMap.get(key), thisDI);
			} else {
				equalsInfo = false;
				setMessage(itemName + "." + MessageConstant.DEVICE_NAME.getMessage() + "." + thisDI.getDeviceIndex(), MessageConstant.NONEXISTENT.getMessage(), thisDI.getDeviceName());
				setMessage(itemName + "." + MessageConstant.DEVICE_INDEX.getMessage()  + "." + thisDI.getDeviceIndex(), MessageConstant.NONEXISTENT.getMessage(), thisDI.getDeviceIndex().toString());
				setMessage(itemName + "." + MessageConstant.DEVICE_TYPE.getMessage()  + "." + thisDI.getDeviceIndex(), MessageConstant.NONEXISTENT.getMessage(), thisDI.getDeviceType());
			}
		}
		for (int i=0; lastList.size() > i; i++) {
			NodeDeviceInfo lastDI = (NodeDeviceInfo)lastList.get(i);
			String key = lastDI.getDeviceIndex().toString() + "."  + lastDI.getDeviceType() + "."  + lastDI.getDeviceName();
			if (thisMap.containsKey(key)) {
				equalsInfo = equalsInfo && equalsNodeDeviceInfo(itemName, lastDI, thisMap.get(key));
			} else {
				equalsInfo = false;
				setMessage(itemName + "." + MessageConstant.DEVICE_NAME.getMessage() + "." + lastDI.getDeviceIndex(), lastDI.getDeviceName(), MessageConstant.NONEXISTENT.getMessage());
				setMessage(itemName + "." + MessageConstant.DEVICE_INDEX.getMessage() + "." + lastDI.getDeviceIndex(), lastDI.getDeviceIndex().toString(), MessageConstant.NONEXISTENT.getMessage());
				setMessage(itemName + "." + MessageConstant.DEVICE_TYPE.getMessage() + "." + lastDI.getDeviceIndex(), lastDI.getDeviceType(), MessageConstant.NONEXISTENT.getMessage());
			}
		}

		return equalsInfo;

	}
}
