/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rap.rwt.SingletonUtil;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.SnmpProtocolConstant;
import com.clustercontrol.bean.SnmpSecurityLevelConstant;
import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.repository.bean.DeviceTypeConstant;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.bean.NodeConstant;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.ws.repository.InvalidRole_Exception;
import com.clustercontrol.ws.repository.NodeCpuInfo;
import com.clustercontrol.ws.repository.NodeDeviceInfo;
import com.clustercontrol.ws.repository.NodeDiskInfo;
import com.clustercontrol.ws.repository.NodeFilesystemInfo;
import com.clustercontrol.ws.repository.NodeGeneralDeviceInfo;
import com.clustercontrol.ws.repository.NodeHostnameInfo;
import com.clustercontrol.ws.repository.NodeInfo;
import com.clustercontrol.ws.repository.NodeMemoryInfo;
import com.clustercontrol.ws.repository.NodeNetworkInterfaceInfo;
import com.clustercontrol.ws.repository.NodeNoteInfo;
import com.clustercontrol.ws.repository.NodeVariableInfo;
import com.clustercontrol.ws.repository.RepositoryTableInfo;

public class NodePropertyUtil {

	// ログ
	private static Log m_log = LogFactory.getLog( NodePropertyUtil.class );

	/** ----- 初期値キャッシュ ----- */
	private Object[][] platformCache = null;
	private Object[][] subPlatformCache = null;

	private static NodePropertyUtil getInstance() {
		return SingletonUtil.getSessionInstance(NodePropertyUtil.class);
	}
	
	/**
	 * ノード[作成・変更]ダイアログのスコープ単位の変更において変更不可項目を変更不可/可能に切り替える
	 *
	 * @param property
	 * @param modifyNode
	 */
	public static void modifyPropertySetting (Property property, boolean modifyNode) {

		/** ローカル変数 */
		ArrayList<Property> propertyList = null;
		Property deviceProperty = null;
		ArrayList<Property> object1 = null;
		ArrayList<Property> object2 = null;
		ArrayList<Property> object3 = null;
		ArrayList<Property> object4 = null;
		ArrayList<Property> object5 = null;
		ArrayList<Property> object6 = null;
		ArrayList<Property> object7 = null;
		ArrayList<Property> object8 = null;
		ArrayList<Property> object9 = null;

		int modify;//編集可否
		if(modifyNode){
			modify = PropertyDefineConstant.MODIFY_OK;
		}else{
			modify = PropertyDefineConstant.MODIFY_NG;
		}

		//
		// 変更不可項目(繰り返し項目)のみmodify可能/不可の再設定をする
		//

		// ----- デバイス関連 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.HOST_NAME);
		if (propertyList != null && propertyList.size() != 0) {
			for (int i = 0; i < propertyList.size(); i++){
				(propertyList.get(i)).setModify(modify);
			}
		}

		// ----- デバイス関連 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.GENERAL_DEVICE_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_TYPE);
			object2 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_INDEX);
			object3 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_NAME);
			object4 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_DISPLAY_NAME);
			object5 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_SIZE);
			object6 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_SIZE_UNIT);
			object7 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_DESCRIPTION);

			if(object1 != null){
				for (int i = 0; i < object1.size(); i++){
					((Property)object1.get(i)).setModify(modify);
					((Property)object2.get(i)).setModify(modify);
					((Property)object3.get(i)).setModify(modify);
					((Property)object4.get(i)).setModify(modify);
					((Property)object5.get(i)).setModify(modify);
					((Property)object6.get(i)).setModify(modify);
					((Property)object7.get(i)).setModify(modify);
				}
			}
		}

		// ----- CPU関連 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.CPU_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_TYPE);
			object2 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_INDEX);
			object3 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_NAME);
			object4 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_DISPLAY_NAME);
			object5 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_SIZE);
			object6 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_SIZE_UNIT);
			object7 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_DESCRIPTION);

			if(object1 != null){
				for (int i = 0; i < object1.size(); i++){
					((Property)object1.get(i)).setModify(modify);
					((Property)object2.get(i)).setModify(modify);
					((Property)object3.get(i)).setModify(modify);
					((Property)object4.get(i)).setModify(modify);
					((Property)object5.get(i)).setModify(modify);
					((Property)object6.get(i)).setModify(modify);
					((Property)object7.get(i)).setModify(modify);
				}
			}
		}

		// ----- MEM関連 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.MEMORY_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_TYPE);
			object2 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_INDEX);
			object3 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_NAME);
			object4 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_DISPLAY_NAME);
			object5 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_SIZE);
			object6 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_SIZE_UNIT);
			object7 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_DESCRIPTION);

			if(object1 != null){
				for (int i = 0; i < object1.size(); i++){
					((Property)object1.get(i)).setModify(modify);
					((Property)object2.get(i)).setModify(modify);
					((Property)object3.get(i)).setModify(modify);
					((Property)object4.get(i)).setModify(modify);
					((Property)object5.get(i)).setModify(modify);
					((Property)object6.get(i)).setModify(modify);
					((Property)object7.get(i)).setModify(modify);
				}
			}
		}

		// ----- NIC関連 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.NETWORK_INTERFACE_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_TYPE);
			object2 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_INDEX);
			object3 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_NAME);
			object4 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_DISPLAY_NAME);
			object5 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_SIZE);
			object6 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_SIZE_UNIT);
			object7 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_DESCRIPTION);
			object8 = PropertyUtil.getProperty(deviceProperty, NodeConstant.NIC_IP_ADDRESS);
			object9 = PropertyUtil.getProperty(deviceProperty, NodeConstant.NIC_MAC_ADDRESS);

			if(object1 != null){
				for (int i = 0; i < object1.size(); i++){
					((Property)object1.get(i)).setModify(modify);
					((Property)object2.get(i)).setModify(modify);
					((Property)object3.get(i)).setModify(modify);
					((Property)object4.get(i)).setModify(modify);
					((Property)object5.get(i)).setModify(modify);
					((Property)object6.get(i)).setModify(modify);
					((Property)object7.get(i)).setModify(modify);
					((Property)object8.get(i)).setModify(modify);
					((Property)object9.get(i)).setModify(modify);
				}
			}
		}

		// ----- DISK関連 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.DISK_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_TYPE);
			object2 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_INDEX);
			object3 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_NAME);
			object4 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_DISPLAY_NAME);
			object5 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_SIZE);
			object6 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_SIZE_UNIT);
			object7 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_DESCRIPTION);
			object8 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DISK_RPM);

			if(object1 != null){
				for (int i = 0; i < object1.size(); i++){
					((Property)object1.get(i)).setModify(modify);
					((Property)object2.get(i)).setModify(modify);
					((Property)object3.get(i)).setModify(modify);
					((Property)object4.get(i)).setModify(modify);
					((Property)object5.get(i)).setModify(modify);
					((Property)object6.get(i)).setModify(modify);
					((Property)object7.get(i)).setModify(modify);
					((Property)object8.get(i)).setModify(modify);
				}
			}
		}

		// ----- FS関連 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.FILE_SYSTEM_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_TYPE);
			object2 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_INDEX);
			object3 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_NAME);
			object4 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_DISPLAY_NAME);
			object5 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_SIZE);
			object6 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_SIZE_UNIT);
			object7 = PropertyUtil.getProperty(deviceProperty, NodeConstant.DEVICE_DESCRIPTION);
			object8 = PropertyUtil.getProperty(deviceProperty, NodeConstant.FILE_SYSTEM_TYPE);

			if(object1 != null){
				for (int i = 0; i < object1.size(); i++){
					((Property)object1.get(i)).setModify(modify);
					((Property)object2.get(i)).setModify(modify);
					((Property)object3.get(i)).setModify(modify);
					((Property)object4.get(i)).setModify(modify);
					((Property)object5.get(i)).setModify(modify);
					((Property)object6.get(i)).setModify(modify);
					((Property)object7.get(i)).setModify(modify);
					((Property)object8.get(i)).setModify(modify);
				}
			}
		}

		// ----- ノード変数関連 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.NODE_VARIABLE);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getProperty(deviceProperty, NodeConstant.NODE_VARIABLE_NAME);
			object2 = PropertyUtil.getProperty(deviceProperty, NodeConstant.NODE_VARIABLE_VALUE);

			if(object1 != null){
				for (int i = 0; i < object1.size(); i++){
					((Property)object1.get(i)).setModify(modify);
					((Property)object2.get(i)).setModify(modify);
				}
			}
		}

		// ----- 備考関連 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.NOTE);
		if (propertyList != null && propertyList.size() != 0) {
			for (int i = 0; i < propertyList.size(); i++){
				(propertyList.get(i)).setModify(modify);
			}
		}
	}

	/**
	 * PropertyクラスをNodeInfoクラスに変換するメソッド。
	 * この処理は不可逆なので注意すること。。
	 */
	public static NodeInfo property2node (Property property) {
		NodeInfo nodeInfo = new NodeInfo();

		ArrayList<?> object1 = null;
		ArrayList<?> object2 = null;
		ArrayList<?> object3 = null;
		ArrayList<?> object4 = null;
		ArrayList<?> object5 = null;
		ArrayList<?> object6 = null;
		ArrayList<?> object7 = null;
		ArrayList<?> object8 = null;
		ArrayList<?> object9 = null;

		ArrayList<Property> propertyList = null;
		Property deviceProperty = null;

		// ----- ファシリティ関連 -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.FACILITY_ID);
		if (object1.size() > 0) {
			nodeInfo.setFacilityId((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.FACILITY_NAME);
		if (object1.size() > 0) {
			nodeInfo.setFacilityName((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.DESCRIPTION);
		if (object1.size() > 0) {
			nodeInfo.setDescription((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.VALID);
		if (object1.size() > 0) {
			nodeInfo.setValid((Boolean)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.AUTO_DEVICE_SEARCH);
		if (object1.size() > 0) {
			nodeInfo.setAutoDeviceSearch((Boolean)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.CREATOR_NAME);
		if (object1.size() > 0) {
			nodeInfo.setCreateUserId((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.CREATE_TIME);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setCreateDatetime(((Date)object1.get(0)).getTime());
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.MODIFIER_NAME);
		if (object1.size() > 0) {
			nodeInfo.setModifyUserId((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.MODIFY_TIME);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setModifyDatetime(((Date)object1.get(0)).getTime());
		}

		// ----- ノード基本情報関連 -----

		// ----- HW関連 -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.PLATFORM_FAMILY_NAME);
		if (object1.size() > 0) {
			nodeInfo.setPlatformFamily((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SUB_PLATFORM_FAMILY_NAME);
		if (object1.size() > 0) {
			nodeInfo.setSubPlatformFamily((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.HARDWARE_TYPE);
		if (object1.size() > 0) {
			nodeInfo.setHardwareType((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.ICONIMAGE);
		if (object1.size() > 0) {
			nodeInfo.setIconImage((String)object1.get(0));
		}


		// ----- IPアドレス関連 -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.IP_ADDRESS_VERSION);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setIpAddressVersion((Integer)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.IP_ADDRESS_V4);
		if (object1.size() > 0) {
			nodeInfo.setIpAddressV4((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.IP_ADDRESS_V6);
		if (object1.size() > 0) {
			nodeInfo.setIpAddressV6((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.HOST_NAME);
		ArrayList<NodeHostnameInfo> nodeHostnameInfo = new ArrayList<NodeHostnameInfo>();
		for (Object o : object1) {
			NodeHostnameInfo item = new NodeHostnameInfo();
			item.setHostname((String)o);
			nodeHostnameInfo.add(item);
		}
		List<NodeHostnameInfo> nodeHostnameInfo_orig = nodeInfo.getNodeHostnameInfo();
		nodeHostnameInfo_orig.clear();
		nodeHostnameInfo_orig.addAll(nodeHostnameInfo);

		// ----- OS関連 -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.NODE_NAME);
		if (object1.size() > 0) {
			nodeInfo.setNodeName((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.OS_NAME);
		if (object1.size() > 0) {
			nodeInfo.setOsName((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.OS_RELEASE);
		if (object1.size() > 0) {
			nodeInfo.setOsRelease((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.OS_VERSION);
		if (object1.size() > 0) {
			nodeInfo.setOsVersion((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.CHARACTER_SET);
		if (object1.size() > 0) {
			nodeInfo.setCharacterSet((String)object1.get(0));
		}

		// ----- Hinemosエージェント関連  -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.AGENT_AWAKE_PORT);
		if (object1.size() > 0  && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setAgentAwakePort((Integer)object1.get(0));
		}

		// ----- ジョブ -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.JOB_PRIORITY);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setJobPriority((Integer)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.JOB_MULTIPLICITY);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setJobMultiplicity((Integer)object1.get(0));
		}


		// ----- サービス情報関連 -----

		// ----- SNMP関連 -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SNMP_USER);
		if (object1.size() > 0) {
			nodeInfo.setSnmpUser((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SNMP_AUTH_PASSWORD);
		if (object1.size() > 0) {
			nodeInfo.setSnmpAuthPassword((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SNMP_PRIV_PASSWORD);
		if (object1.size() > 0) {
			nodeInfo.setSnmpPrivPassword((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SNMP_PORT);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setSnmpPort((Integer)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SNMP_COMMUNITY);
		if (object1.size() > 0) {
			nodeInfo.setSnmpCommunity((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SNMP_VERSION);
		if (object1.size() > 0) {
			nodeInfo.setSnmpVersion(SnmpVersionConstant.stringToType((String)object1.get(0)));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SNMP_SECURITY_LEVEL);
		if (object1.size() > 0) {
			nodeInfo.setSnmpSecurityLevel((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SNMP_AUTH_PROTOCOL);
		if (object1.size() > 0) {
			nodeInfo.setSnmpAuthProtocol((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SNMP_PRIV_PROTOCOL);
		if (object1.size() > 0) {
			nodeInfo.setSnmpPrivProtocol((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SNMPTIMEOUT);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setSnmpTimeout((Integer)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SNMPRETRIES);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setSnmpRetryCount((Integer)object1.get(0));
		}


		// ----- WBEM関連 -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.WBEM_USER);
		if (object1.size() > 0) {
			nodeInfo.setWbemUser((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.WBEM_USER_PASSWORD);
		if (object1.size() > 0) {
			nodeInfo.setWbemUserPassword((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.WBEM_PORT);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setWbemPort((Integer)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.WBEM_PROTOCOL);
		if (object1.size() > 0) {
			nodeInfo.setWbemProtocol((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.WBEM_TIMEOUT);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setWbemTimeout((Integer)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.WBEM_RETRIES);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setWbemRetryCount((Integer)object1.get(0));
		}

		// ----- IPMI関連 -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.IPMI_IP_ADDRESS);
		if (object1.size() > 0) {
			nodeInfo.setIpmiIpAddress((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.IPMI_PORT);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setIpmiPort((Integer)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.IPMI_USER);
		if (object1.size() > 0) {
			nodeInfo.setIpmiUser((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.IPMI_USER_PASSWORD);
		if (object1.size() > 0) {
			nodeInfo.setIpmiUserPassword((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.IPMI_TIMEOUT);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setIpmiTimeout((Integer)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.IPMI_RETRIES);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setIpmiRetries((Integer)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.IPMI_PROTOCOL);
		if (object1.size() > 0) {
			nodeInfo.setIpmiProtocol((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.IPMI_LEVEL);
		if (object1.size() > 0) {
			nodeInfo.setIpmiLevel((String)object1.get(0));
		}

		// ----- WinRM関連 -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.WINRM_USER);
		if (object1.size() > 0) {
			nodeInfo.setWinrmUser((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.WINRM_USER_PASSWORD);
		if (object1.size() > 0) {
			nodeInfo.setWinrmUserPassword((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.WINRM_VERSION);
		if (object1.size() > 0) {
			nodeInfo.setWinrmVersion((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.WINRM_PORT);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setWinrmPort((Integer)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.WINRM_PROTOCOL);
		if (object1.size() > 0) {
			nodeInfo.setWinrmProtocol((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.WINRM_TIMEOUT);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setWinrmTimeout((Integer)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.WINRM_RETRIES);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setWinrmRetries((Integer)object1.get(0));
		}

		// ----- SSH関連 -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SSH_USER);
		if (object1.size() > 0) {
			nodeInfo.setSshUser((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SSH_USER_PASSWORD);
		if (object1.size() > 0) {
			nodeInfo.setSshUserPassword((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SSH_PRIVATE_KEY_FILEPATH);
		if (object1.size() > 0) {
			nodeInfo.setSshPrivateKeyFilepath((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SSH_PRIVATE_KEY_PASSPHRASE);
		if (object1.size() > 0) {
			nodeInfo.setSshPrivateKeyPassphrase((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SSH_PORT);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setSshPort((Integer)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SSH_TIMEOUT);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setSshTimeout((Integer)object1.get(0));
		}

		// ----- デバイス関連 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.GENERAL_DEVICE_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_TYPE);
			object2 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_INDEX);
			object3 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_NAME);
			object4 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_DISPLAY_NAME);
			object5 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_SIZE);
			object6 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_SIZE_UNIT);
			object7 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_DESCRIPTION);

			ArrayList<NodeGeneralDeviceInfo> nodeDeviceInfo = new ArrayList<NodeGeneralDeviceInfo>();
			for (int i = 0; i < object1.size(); i++) {
				NodeGeneralDeviceInfo item = new NodeGeneralDeviceInfo();
				item.setDeviceType((String)object1.get(i));
				if(object2.get(i) != null && !"".equals(object2.get(i)))
					item.setDeviceIndex((Integer)object2.get(i));
				item.setDeviceName((String)object3.get(i));
				item.setDeviceDisplayName((String)object4.get(i));
				if(object5.get(i) != null && !"".equals(object5.get(i)))
					item.setDeviceSize((Integer)object5.get(i));
				item.setDeviceSizeUnit((String)object6.get(i));
				item.setDeviceDescription((String)object7.get(i));

				if((item.getDeviceType() == null || "".equals(item.getDeviceType()))
						&& (item.getDeviceIndex() == null || -1 == item.getDeviceIndex())
						&& (item.getDeviceName() == null || "".equals(item.getDeviceName()))
						&& (item.getDeviceDisplayName()== null || "".equals(item.getDeviceDisplayName()))
						){
					m_log.debug("General Device is null");
				}else{
					nodeDeviceInfo.add(item);
				}
			}
			List<NodeGeneralDeviceInfo> nodeDeviceInfo_orig = nodeInfo.getNodeDeviceInfo();
			nodeDeviceInfo_orig.clear();
			nodeDeviceInfo_orig.addAll(nodeDeviceInfo);
		}

		// ----- CPU関連 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.CPU_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_TYPE);
			object2 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_INDEX);
			object3 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_NAME);
			object4 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_DISPLAY_NAME);
			object5 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_SIZE);
			object6 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_SIZE_UNIT);
			object7 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_DESCRIPTION);

			ArrayList<NodeCpuInfo> nodeCpuInfo = new ArrayList<NodeCpuInfo>();

			for (int i = 0; i < object1.size(); i++) {
				NodeCpuInfo item = new NodeCpuInfo();
				item.setDeviceType((String)object1.get(i));
				if(object2.get(i) != null && !"".equals(object2.get(i)))
					item.setDeviceIndex((Integer)object2.get(i));
				item.setDeviceName((String)object3.get(i));
				item.setDeviceDisplayName((String)object4.get(i));
				if(object5.get(i) != null && !"".equals(object5.get(i)))
					item.setDeviceSize((Integer)object5.get(i));
				item.setDeviceSizeUnit((String)object6.get(i));
				item.setDeviceDescription((String)object7.get(i));

				if((item.getDeviceIndex() == null || -1 == item.getDeviceIndex())
						&& (item.getDeviceName() == null || "".equals(item.getDeviceName()))
						&& (item.getDeviceDisplayName()== null || "".equals(item.getDeviceDisplayName()))
						){
					m_log.debug("CPU Device is null");
				}else{
					nodeCpuInfo.add(item);
				}
			}
			List<NodeCpuInfo> nodeCpuInfo_orig = nodeInfo.getNodeCpuInfo();
			nodeCpuInfo_orig.clear();
			nodeCpuInfo_orig.addAll(nodeCpuInfo);
		}

		// ----- MEM関連 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.MEMORY_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_TYPE);
			object2 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_INDEX);
			object3 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_NAME);
			object4 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_DISPLAY_NAME);
			object5 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_SIZE);
			object6 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_SIZE_UNIT);
			object7 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_DESCRIPTION);

			ArrayList<NodeMemoryInfo> nodeMemoryInfo = new ArrayList<NodeMemoryInfo>();
			for (int i = 0; i < object1.size(); i++) {
				NodeMemoryInfo item = new NodeMemoryInfo();
				item.setDeviceType((String)object1.get(i));
				if(object2.get(i) != null && !"".equals(object2.get(i)))
					item.setDeviceIndex((Integer)object2.get(i));
				item.setDeviceName((String)object3.get(i));
				item.setDeviceDisplayName((String)object4.get(i));
				if(object5.get(i) != null && !"".equals(object5.get(i)))
					item.setDeviceSize((Integer)object5.get(i));
				item.setDeviceSizeUnit((String)object6.get(i));
				item.setDeviceDescription((String)object7.get(i));

				if((item.getDeviceIndex() == null || -1 == item.getDeviceIndex())
						&& (item.getDeviceName() == null || "".equals(item.getDeviceName()))
						&& (item.getDeviceDisplayName()== null || "".equals(item.getDeviceDisplayName()))
						){
					m_log.debug("Memory Device is null");
				}else{
					nodeMemoryInfo.add(item);
				}
			}
			List<NodeMemoryInfo> nodeMemoryInfo_orig = nodeInfo.getNodeMemoryInfo();
			nodeMemoryInfo_orig.clear();
			nodeMemoryInfo_orig.addAll(nodeMemoryInfo);
		}

		// ----- NIC関連 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.NETWORK_INTERFACE_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_TYPE);
			object2 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_INDEX);
			object3 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_NAME);
			object4 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_DISPLAY_NAME);
			object5 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_SIZE);
			object6 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_SIZE_UNIT);
			object7 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_DESCRIPTION);
			object8 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.NIC_IP_ADDRESS);
			object9 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.NIC_MAC_ADDRESS);

			ArrayList<NodeNetworkInterfaceInfo> nodeNetworkInterfaceInfo = new ArrayList<NodeNetworkInterfaceInfo>();
			for (int i = 0; i < object1.size(); i++) {
				NodeNetworkInterfaceInfo item = new NodeNetworkInterfaceInfo();
				item.setDeviceType((String)object1.get(i));
				if(object2.get(i) != null && !"".equals(object2.get(i)))
					item.setDeviceIndex((Integer)object2.get(i));
				item.setDeviceName((String)object3.get(i));
				item.setDeviceDisplayName((String)object4.get(i));
				if(object5.get(i) != null && !"".equals(object5.get(i)))
					item.setDeviceSize((Integer)object5.get(i));
				item.setDeviceSizeUnit((String)object6.get(i));
				item.setDeviceDescription((String)object7.get(i));
				item.setNicIpAddress((String)object8.get(i));
				item.setNicMacAddress((String)object9.get(i));

				if((item.getDeviceIndex() == null || -1 == item.getDeviceIndex())
						&& (item.getDeviceName() == null || "".equals(item.getDeviceName()))
						&& (item.getDeviceDisplayName()== null || "".equals(item.getDeviceDisplayName()))
						){
					m_log.debug("NIC Device is null");
				}else{
					nodeNetworkInterfaceInfo.add(item);
				}
			}
			List<NodeNetworkInterfaceInfo> nodeNetworkInterfaceInfo_orig
			= nodeInfo.getNodeNetworkInterfaceInfo();
			nodeNetworkInterfaceInfo_orig.clear();
			nodeNetworkInterfaceInfo_orig.addAll(nodeNetworkInterfaceInfo);
		}

		// ----- DISK関連 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.DISK_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_TYPE);
			object2 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_INDEX);
			object3 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_NAME);
			object4 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_DISPLAY_NAME);
			object5 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_SIZE);
			object6 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_SIZE_UNIT);
			object7 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_DESCRIPTION);
			object8 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DISK_RPM);

			ArrayList<NodeDiskInfo> nodeDiskInfo = new ArrayList<NodeDiskInfo>();
			for (int i = 0; i < object1.size(); i++) {
				NodeDiskInfo item = new NodeDiskInfo();
				item.setDeviceType((String)object1.get(i));
				if(object2.get(i) != null && !"".equals(object2.get(i)))
					item.setDeviceIndex((Integer)object2.get(i));
				item.setDeviceName((String)object3.get(i));
				item.setDeviceDisplayName((String)object4.get(i));
				if(object5.get(i) != null && !"".equals(object5.get(i)))
					item.setDeviceSize((Integer)object5.get(i));
				item.setDeviceSizeUnit((String)object6.get(i));
				item.setDeviceDescription((String)object7.get(i));
				if(object8.get(i) != null && !"".equals(object8.get(i)))
					item.setDiskRpm((Integer)object8.get(i));

				if((item.getDeviceIndex() == null || -1 == item.getDeviceIndex())
						&& (item.getDeviceName() == null || "".equals(item.getDeviceName()))
						&& (item.getDeviceDisplayName()== null || "".equals(item.getDeviceDisplayName()))
						){
					m_log.debug("Disk Device is null");
				}else{
					nodeDiskInfo.add(item);
				}
			}
			List<NodeDiskInfo> nodeDiskInfo_orig = nodeInfo.getNodeDiskInfo();
			nodeDiskInfo_orig.clear();
			nodeDiskInfo_orig.addAll(nodeDiskInfo);
		}

		// ----- ファイルシステム関連 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.FILE_SYSTEM_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_TYPE);
			object2 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_INDEX);
			object3 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_NAME);
			object4 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_DISPLAY_NAME);
			object5 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_SIZE);
			object6 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_SIZE_UNIT);
			object7 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.DEVICE_DESCRIPTION);
			object8 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.FILE_SYSTEM_TYPE);

			ArrayList<NodeFilesystemInfo> nodeFilesystemInfo = new ArrayList<NodeFilesystemInfo>();
			for (int i = 0; i < object1.size(); i++) {
				NodeFilesystemInfo item = new NodeFilesystemInfo();
				item.setDeviceType((String)object1.get(i));
				if(object2.get(i) != null && !"".equals(object2.get(i)))
					item.setDeviceIndex((Integer)object2.get(i));
				item.setDeviceName((String)object3.get(i));
				item.setDeviceDisplayName((String)object4.get(i));
				if(object5.get(i) != null && !"".equals(object5.get(i)))
					item.setDeviceSize((Integer)object5.get(i));
				item.setDeviceSizeUnit((String)object6.get(i));
				item.setDeviceDescription((String)object7.get(i));
				item.setFilesystemType((String)object8.get(i));

				if((item.getDeviceIndex() == null || -1 == item.getDeviceIndex())
						&& (item.getDeviceName() == null || "".equals(item.getDeviceName()))
						&& (item.getDeviceDisplayName()== null || "".equals(item.getDeviceDisplayName()))
						){
					m_log.debug("File System Device is null");
				}else{
					nodeFilesystemInfo.add(item);
				}
			}
			List<NodeFilesystemInfo> nodeFilesystemInfo_orig
			= nodeInfo.getNodeFilesystemInfo();
			nodeFilesystemInfo_orig.clear();
			nodeFilesystemInfo_orig.addAll(nodeFilesystemInfo);
		}

		// ----- クラウド管理関連 -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.CLOUDSERVICE);
		if (object1.size() > 0) {
			nodeInfo.setCloudService((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.CLOUDSCOPE);
		if (object1.size() > 0) {
			nodeInfo.setCloudScope((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.CLOUDRESOURCETYPE);
		if (object1.size() > 0) {
			nodeInfo.setCloudResourceType((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.CLOUDRESOURCEID);
		if (object1.size() > 0) {
			nodeInfo.setCloudResourceId((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.CLOUDRESOURCENAME);
		if (object1.size() > 0) {
			nodeInfo.setCloudResourceName((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.CLOUDLOCATION);
		if (object1.size() > 0) {
			nodeInfo.setCloudLocation((String)object1.get(0));
		}

		// ----- ノード変数関連 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.NODE_VARIABLE);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.NODE_VARIABLE_NAME);
			object2 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.NODE_VARIABLE_VALUE);

			ArrayList<NodeVariableInfo> nodeVariableInfo = new ArrayList<NodeVariableInfo>();
			for (int i = 0; i < object1.size(); i++) {
				if ((object1.get(i) != null || ! object1.get(i).toString().equals(""))
						&& (object2.get(i) != null && ! object2.get(i).toString().equals(""))
						) {
					NodeVariableInfo item = new NodeVariableInfo();
					item.setNodeVariableName((String)object1.get(i));
					item.setNodeVariableValue((String)object2.get(i));
					nodeVariableInfo.add(item);
				}
			}
			List<NodeVariableInfo> nodeVariableInfo_orig = nodeInfo.getNodeVariableInfo();
			nodeVariableInfo_orig.clear();
			nodeVariableInfo_orig.addAll(nodeVariableInfo);
		}

		// ----- 保守関連 -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.ADMINISTRATOR);
		if (object1.size() > 0) {
			nodeInfo.setAdministrator((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.CONTACT);
		if (object1.size() > 0) {
			nodeInfo.setContact((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.NOTE);
		ArrayList<NodeNoteInfo> nodeNoteInfo = new ArrayList<NodeNoteInfo>();
		for (int i = 0; i < object1.size(); i++) {
			NodeNoteInfo item = new NodeNoteInfo();
			item.setNoteId(i);
			item.setNote((String) object1.get(i));
			nodeNoteInfo.add(item);
		}
		List<NodeNoteInfo> nodeNoteInfo_orig = nodeInfo.getNodeNoteInfo();
		nodeNoteInfo_orig.clear();
		nodeNoteInfo_orig.addAll(nodeNoteInfo);

		return nodeInfo;
	}

	/**
	 * NodeInfoをPropertyに変換します。
	 * @param managerName
	 * @param node
	 * @param mode
	 * @param locale
	 * @return
	 */
	public static Property node2property(String managerName, NodeInfo node, int mode, Locale locale) {
		/** ローカル変数 */
		Property property = null;
		ArrayList<Property> propertyList = null;
		Property deviceProperty = null;

		/** メイン処理 */
		property = getProperty(managerName, mode, locale);

		// ----- ファシリティ関連 -----
		// ファシリティID
		propertyList = PropertyUtil.getProperty(property, NodeConstant.FACILITY_ID);
		((Property)propertyList.get(0)).setValue(node.getFacilityId());
		// ファシリティ名
		propertyList = PropertyUtil.getProperty(property, NodeConstant.FACILITY_NAME);
		((Property)propertyList.get(0)).setValue(node.getFacilityName());
		// 説明
		propertyList = PropertyUtil.getProperty(property, NodeConstant.DESCRIPTION);
		((Property)propertyList.get(0)).setValue(node.getDescription());
		// 有効/無効
		propertyList = PropertyUtil.getProperty(property, NodeConstant.VALID);
		((Property)propertyList.get(0)).setValue(node.isValid());
		// 自動デバイスサーチ
		propertyList = PropertyUtil.getProperty(property, NodeConstant.AUTO_DEVICE_SEARCH);
		((Property)propertyList.get(0)).setValue(node.isAutoDeviceSearch());
		// 登録ユーザID
		propertyList = PropertyUtil.getProperty(property, NodeConstant.CREATOR_NAME);
		((Property)propertyList.get(0)).setValue(node.getCreateUserId());
		// 登録日時
		if (node.getCreateDatetime() != null && node.getCreateDatetime() != 0) {
			propertyList = PropertyUtil.getProperty(property, NodeConstant.CREATE_TIME);
			((Property)propertyList.get(0)).setValue(new Date(node.getCreateDatetime()));
		}
		// 最終更新ユーザID
		propertyList = PropertyUtil.getProperty(property, NodeConstant.MODIFIER_NAME);
		((Property)propertyList.get(0)).setValue(node.getModifyUserId());
		// 最終更新日時
		if (node.getModifyDatetime() != null && node.getModifyDatetime() != 0) {
			propertyList = PropertyUtil.getProperty(property, NodeConstant.MODIFY_TIME);
			((Property)propertyList.get(0)).setValue(new Date(node.getModifyDatetime()));
		}

		// ----- ノード関連 -----

		// ----- HW関連 -----
		// プラットフォーム
		propertyList = PropertyUtil.getProperty(property, NodeConstant.PLATFORM_FAMILY_NAME);
		((Property)propertyList.get(0)).setValue(node.getPlatformFamily());
		// サブプラットフォーム
		propertyList = PropertyUtil.getProperty(property, NodeConstant.SUB_PLATFORM_FAMILY_NAME);
		((Property)propertyList.get(0)).setValue(node.getSubPlatformFamily());
		// H/Wタイプ
		propertyList = PropertyUtil.getProperty(property, NodeConstant.HARDWARE_TYPE);
		((Property)propertyList.get(0)).setValue(node.getHardwareType());
		// 画面アイコンイメージ
		propertyList = PropertyUtil.getProperty(property, NodeConstant.ICONIMAGE);
		((Property)propertyList.get(0)).setValue(node.getIconImage());


		// ----- IPアドレス関連 -----
		// IPバージョン
		propertyList = PropertyUtil.getProperty(property, NodeConstant.IP_ADDRESS_VERSION);
		((Property)propertyList.get(0)).setValue(node.getIpAddressVersion());
		// IPアドレスV4
		propertyList = PropertyUtil.getProperty(property, NodeConstant.IP_ADDRESS_V4);
		((Property)propertyList.get(0)).setValue(node.getIpAddressV4());
		// IPアドレスV6
		propertyList = PropertyUtil.getProperty(property, NodeConstant.IP_ADDRESS_V6);
		((Property)propertyList.get(0)).setValue(node.getIpAddressV6());
		// ホスト名
		propertyList = PropertyUtil.getProperty(property, NodeConstant.HOST_NAME);
		Property parentHost = (Property)((Property)propertyList.get(0)).getParent();
		if (node.getNodeHostnameInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(parentHost, (Property)propertyList.get(0));
			int cnt = 0;
			for (NodeHostnameInfo hostname : node.getNodeHostnameInfo()) {
				Property target = null;
				if (cnt == 0) {
					target = (Property)propertyList.get(0);
				} else {
					target = PropertyUtil.copy((Property)propertyList.get(0));
					parentHost.addChildren(target, index + cnt);
				}
				// ホスト名
				target.setValue(hostname.getHostname());
				cnt++;
			}
		}

		// ----- OS関連 -----
		// ノード名
		propertyList = PropertyUtil.getProperty(property, NodeConstant.NODE_NAME);
		((Property)propertyList.get(0)).setValue(node.getNodeName());
		// OS名
		propertyList = PropertyUtil.getProperty(property, NodeConstant.OS_NAME);
		((Property)propertyList.get(0)).setValue(node.getOsName());
		// OSリリース
		propertyList = PropertyUtil.getProperty(property, NodeConstant.OS_RELEASE);
		((Property)propertyList.get(0)).setValue(node.getOsRelease());
		// OSバージョン
		propertyList = PropertyUtil.getProperty(property, NodeConstant.OS_VERSION);
		((Property)propertyList.get(0)).setValue(node.getOsVersion());
		// 文字セット
		propertyList = PropertyUtil.getProperty(property, NodeConstant.CHARACTER_SET);
		((Property)propertyList.get(0)).setValue(node.getCharacterSet());

		// ----- Hinemosエージェント -----
		// 即時反映用ポート番号
		propertyList = PropertyUtil.getProperty(property, NodeConstant.AGENT_AWAKE_PORT);
		((Property)propertyList.get(0)).setValue(node.getAgentAwakePort());

		// ----- ジョブ -----
		// ジョブ優先度
		if (node.getJobPriority() != null) {
			propertyList = PropertyUtil.getProperty(property, NodeConstant.JOB_PRIORITY);
			((Property)propertyList.get(0)).setValue(node.getJobPriority());
		}
		// ジョブ多重度
		if (node.getJobMultiplicity() != null) {
			propertyList = PropertyUtil.getProperty(property, NodeConstant.JOB_MULTIPLICITY);
			((Property)propertyList.get(0)).setValue(node.getJobMultiplicity());
		}


		// ----- サービス関連 -----

		// ----- SNMP関連 -----
		// SNMP接続ユーザ
		propertyList = PropertyUtil.getProperty(property, NodeConstant.SNMP_USER);
		((Property)propertyList.get(0)).setValue(node.getSnmpUser());
		// SNMPポート番号
		if (node.getSnmpPort() != null) {
			propertyList = PropertyUtil.getProperty(property, NodeConstant.SNMP_PORT);
			((Property)propertyList.get(0)).setValue(node.getSnmpPort());
		}
		// SNMPコミュニティ名
		propertyList = PropertyUtil.getProperty(property, NodeConstant.SNMP_COMMUNITY);
		((Property)propertyList.get(0)).setValue(node.getSnmpCommunity());
		// SNMPバージョン
		propertyList = PropertyUtil.getProperty(property, NodeConstant.SNMP_VERSION);
		((Property)propertyList.get(0)).setValue(SnmpVersionConstant.typeToString(node.getSnmpVersion()));
		// SNMPセキュリティレベル
		propertyList = PropertyUtil.getProperty(property, NodeConstant.SNMP_SECURITY_LEVEL);
		((Property)propertyList.get(0)).setValue(node.getSnmpSecurityLevel());
		// SNMP接続認証パスワード
		propertyList = PropertyUtil.getProperty(property, NodeConstant.SNMP_AUTH_PASSWORD);
		((Property)propertyList.get(0)).setValue(node.getSnmpAuthPassword());
		// SNMP接続暗号化パスワード
		propertyList = PropertyUtil.getProperty(property, NodeConstant.SNMP_PRIV_PASSWORD);
		((Property)propertyList.get(0)).setValue(node.getSnmpPrivPassword());
		// SNMP認証プロトコル
		propertyList = PropertyUtil.getProperty(property, NodeConstant.SNMP_AUTH_PROTOCOL);
		((Property)propertyList.get(0)).setValue(node.getSnmpAuthProtocol());
		// SNMP暗号化プロトコル
		propertyList = PropertyUtil.getProperty(property, NodeConstant.SNMP_PRIV_PROTOCOL);
		((Property)propertyList.get(0)).setValue(node.getSnmpPrivProtocol());
		// SNMPタイムアウト
		propertyList = PropertyUtil.getProperty(property, NodeConstant.SNMPTIMEOUT);
		((Property)propertyList.get(0)).setValue(node.getSnmpTimeout());
		// SNMPリトライ回数
		propertyList = PropertyUtil.getProperty(property, NodeConstant.SNMPRETRIES);
		((Property)propertyList.get(0)).setValue(node.getSnmpRetryCount());

		// ----- WBEM関連 -----
		// WBEM接続ポート番号
		propertyList = PropertyUtil.getProperty(property, NodeConstant.WBEM_PORT);
		((Property)propertyList.get(0)).setValue(node.getWbemPort());
		// WBEM接続ユーザ
		propertyList = PropertyUtil.getProperty(property, NodeConstant.WBEM_USER);
		((Property)propertyList.get(0)).setValue(node.getWbemUser());
		// WBEM接続ユーザパスワード
		propertyList = PropertyUtil.getProperty(property, NodeConstant.WBEM_USER_PASSWORD);
		((Property)propertyList.get(0)).setValue(node.getWbemUserPassword());
		// WBEM接続プロトコル
		propertyList = PropertyUtil.getProperty(property, NodeConstant.WBEM_PROTOCOL);
		((Property)propertyList.get(0)).setValue(node.getWbemProtocol());
		// WBEM接続タイムアウト
		propertyList = PropertyUtil.getProperty(property, NodeConstant.WBEM_TIMEOUT);
		((Property)propertyList.get(0)).setValue(node.getWbemTimeout());
		// WBEM接続リトライ回数
		propertyList = PropertyUtil.getProperty(property, NodeConstant.WBEM_RETRIES);
		((Property)propertyList.get(0)).setValue(node.getWbemRetryCount());

		// ----- IPMI関連 -----
		// IPMI接続IPアドレス
		propertyList = PropertyUtil.getProperty(property, NodeConstant.IPMI_IP_ADDRESS);
		((Property)propertyList.get(0)).setValue(node.getIpmiIpAddress());
		// IPMI接続ポート番号
		propertyList = PropertyUtil.getProperty(property, NodeConstant.IPMI_PORT);
		((Property)propertyList.get(0)).setValue(node.getIpmiPort());
		// IPMI接続ユーザ
		propertyList = PropertyUtil.getProperty(property, NodeConstant.IPMI_USER);
		((Property)propertyList.get(0)).setValue(node.getIpmiUser());
		// IPMI接続ユーザパスワード
		propertyList = PropertyUtil.getProperty(property, NodeConstant.IPMI_USER_PASSWORD);
		((Property)propertyList.get(0)).setValue(node.getIpmiUserPassword());
		// IPMI接続タイムアウト
		propertyList = PropertyUtil.getProperty(property, NodeConstant.IPMI_TIMEOUT);
		((Property)propertyList.get(0)).setValue(node.getIpmiTimeout());
		// IPMI接続リトライ回数
		propertyList = PropertyUtil.getProperty(property, NodeConstant.IPMI_RETRIES);
		((Property)propertyList.get(0)).setValue(node.getIpmiRetries());
		// IPMI接続プロトコル
		propertyList = PropertyUtil.getProperty(property, NodeConstant.IPMI_PROTOCOL);
		((Property)propertyList.get(0)).setValue(node.getIpmiProtocol());
		// IPMI接続レベル
		propertyList = PropertyUtil.getProperty(property, NodeConstant.IPMI_LEVEL);
		((Property)propertyList.get(0)).setValue(node.getIpmiLevel());

		// ----- WinRM関連 -----
		// WinRM接続ユーザ
		propertyList = PropertyUtil.getProperty(property, NodeConstant.WINRM_USER);
		((Property)propertyList.get(0)).setValue(node.getWinrmUser());
		// WinRM接続ユーザパスワード
		propertyList = PropertyUtil.getProperty(property, NodeConstant.WINRM_USER_PASSWORD);
		((Property)propertyList.get(0)).setValue(node.getWinrmUserPassword());
		// WinRMバージョン
		propertyList = PropertyUtil.getProperty(property, NodeConstant.WINRM_VERSION);
		((Property)propertyList.get(0)).setValue(node.getWinrmVersion());
		// WinRM接続ポート番号
		propertyList = PropertyUtil.getProperty(property, NodeConstant.WINRM_PORT);
		((Property)propertyList.get(0)).setValue(node.getWinrmPort());
		// WinRM接続プロトコル
		propertyList = PropertyUtil.getProperty(property, NodeConstant.WINRM_PROTOCOL);
		((Property)propertyList.get(0)).setValue(node.getWinrmProtocol());
		// WinRM接続タイムアウト
		propertyList = PropertyUtil.getProperty(property, NodeConstant.WINRM_TIMEOUT);
		((Property)propertyList.get(0)).setValue(node.getWinrmTimeout());
		// WinRM接続リトライ回数
		propertyList = PropertyUtil.getProperty(property, NodeConstant.WINRM_RETRIES);
		((Property)propertyList.get(0)).setValue(node.getWinrmRetries());

		// ----- SSH関連 -----
		// SSH接続ユーザ
		propertyList = PropertyUtil.getProperty(property, NodeConstant.SSH_USER);
		((Property)propertyList.get(0)).setValue(node.getSshUser());
		// SSH接続ユーザパスワード
		propertyList = PropertyUtil.getProperty(property, NodeConstant.SSH_USER_PASSWORD);
		((Property)propertyList.get(0)).setValue(node.getSshUserPassword());
		// SSH秘密鍵ファイル名
		propertyList = PropertyUtil.getProperty(property, NodeConstant.SSH_PRIVATE_KEY_FILEPATH);
		((Property)propertyList.get(0)).setValue(node.getSshPrivateKeyFilepath());
		// SSH秘密鍵パスフレーズ
		propertyList = PropertyUtil.getProperty(property, NodeConstant.SSH_PRIVATE_KEY_PASSPHRASE);
		((Property)propertyList.get(0)).setValue(node.getSshPrivateKeyPassphrase());
		// SSHポート番号
		propertyList = PropertyUtil.getProperty(property, NodeConstant.SSH_PORT);
		((Property)propertyList.get(0)).setValue(node.getSshPort());
		// SSHタイムアウト
		propertyList = PropertyUtil.getProperty(property, NodeConstant.SSH_TIMEOUT);
		((Property)propertyList.get(0)).setValue(node.getSshTimeout());

		// ----- デバイス関連-----

		// ----- 汎用デバイス情報 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.GENERAL_DEVICE);
		parentHost = (Property)((Property)propertyList.get(0)).getParent();

		deviceProperty = (Property)propertyList.get(0);
		if (node.getNodeDeviceInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, deviceProperty);
			int cnt = 0;
			for (NodeDeviceInfo device : node.getNodeDeviceInfo()) {
				Property target = null;
				if (cnt == 0) {
					target = deviceProperty;
				} else {
					target = PropertyUtil.copy(deviceProperty);
					parentHost.addChildren(target, index + cnt);
				}
				cnt++;

				// トップ表示情報（デバイス表示名）
				target.setValue(device.getDeviceDisplayName());
				// デバイス種別
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_TYPE);
				((Property)propertyList.get(0)).setValue(device.getDeviceType());
				// デバイスINDEX
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_INDEX);
				((Property)propertyList.get(0)).setValue(device.getDeviceIndex());
				// デバイス名
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_NAME);
				((Property)propertyList.get(0)).setValue(device.getDeviceName());
				// デバイス表示名
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_DISPLAY_NAME);
				((Property)propertyList.get(0)).setValue(device.getDeviceDisplayName());
				// デバイスサイズ
				if (device.getDeviceSize() != -1) {
					propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_SIZE);
					((Property)propertyList.get(0)).setValue(device.getDeviceSize());
				}
				// デバイスサイズ単位
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_SIZE_UNIT);
				((Property)propertyList.get(0)).setValue(device.getDeviceSizeUnit());
				// 説明
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_DESCRIPTION);
				((Property)propertyList.get(0)).setValue(device.getDeviceDescription());
			}
		}

		// ----- CPUデバイス情報 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.CPU);
		parentHost = (Property)((Property)propertyList.get(0)).getParent();

		deviceProperty = (Property)propertyList.get(0);
		if (node.getNodeCpuInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, deviceProperty);
			int cnt = 0;
			for (NodeCpuInfo cpu : node.getNodeCpuInfo()) {
				Property target = null;
				if (cnt == 0) {
					target = deviceProperty;
				} else {
					target = PropertyUtil.copy(deviceProperty);
					parentHost.addChildren(target, index + cnt);
				}
				cnt++;

				// トップ表示情報（デバイス表示名）
				target.setValue(cpu.getDeviceDisplayName());

				// デバイス種別
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_TYPE);
				((Property)propertyList.get(0)).setValue(cpu.getDeviceType());
				// デバイスINDEX
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_INDEX);
				((Property)propertyList.get(0)).setValue(cpu.getDeviceIndex());
				// デバイス名
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_NAME);
				((Property)propertyList.get(0)).setValue(cpu.getDeviceName());
				// デバイス表示名
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_DISPLAY_NAME);
				((Property)propertyList.get(0)).setValue(cpu.getDeviceDisplayName());
				// デバイスサイズ
				if (cpu.getDeviceSize() != -1) {
					propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_SIZE);
					((Property)propertyList.get(0)).setValue(cpu.getDeviceSize());
				}
				// デバイスサイズ単位
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_SIZE_UNIT);
				((Property)propertyList.get(0)).setValue(cpu.getDeviceSizeUnit());
				// 説明
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_DESCRIPTION);
				((Property)propertyList.get(0)).setValue(cpu.getDeviceDescription());
			}
		}

		// ----- MEMデバイス情報 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.MEMORY);
		parentHost = (Property)((Property)propertyList.get(0)).getParent();

		deviceProperty = (Property)propertyList.get(0);
		if (node.getNodeMemoryInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, deviceProperty);
			int cnt = 0;
			for (NodeMemoryInfo memory : node.getNodeMemoryInfo()) {
				Property target = null;
				if (cnt == 0) {
					target = deviceProperty;
				} else {
					target = PropertyUtil.copy(deviceProperty);
					parentHost.addChildren(target, index + cnt);
				}
				cnt++;

				// トップ表示情報（デバイス表示名）
				target.setValue(memory.getDeviceDisplayName());

				// デバイス種別
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_TYPE);
				((Property)propertyList.get(0)).setValue(memory.getDeviceType());
				// デバイスINDEX
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_INDEX);
				((Property)propertyList.get(0)).setValue(memory.getDeviceIndex());
				// デバイス名
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_NAME);
				((Property)propertyList.get(0)).setValue(memory.getDeviceName());
				// デバイス表示名
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_DISPLAY_NAME);
				((Property)propertyList.get(0)).setValue(memory.getDeviceDisplayName());
				// デバイスサイズ
				if (memory.getDeviceSize() != -1) {
					propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_SIZE);
					((Property)propertyList.get(0)).setValue(memory.getDeviceSize());
				}
				// デバイスサイズ単位
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_SIZE_UNIT);
				((Property)propertyList.get(0)).setValue(memory.getDeviceSizeUnit());
				// 説明
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_DESCRIPTION);
				((Property)propertyList.get(0)).setValue(memory.getDeviceDescription());
			}
		}

		// ----- NICデバイス情報 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.NETWORK_INTERFACE);
		parentHost = (Property)((Property)propertyList.get(0)).getParent();

		deviceProperty = (Property)propertyList.get(0);
		if (node.getNodeNetworkInterfaceInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, deviceProperty);
			int cnt = 0;
			for (NodeNetworkInterfaceInfo nic : node.getNodeNetworkInterfaceInfo()) {
				Property target = null;
				if (cnt == 0) {
					target = deviceProperty;
				} else {
					target = PropertyUtil.copy(deviceProperty);
					parentHost.addChildren(target, index + cnt);
				}
				cnt++;

				// トップ表示情報（デバイス表示名）
				target.setValue(nic.getDeviceDisplayName());

				// デバイス種別
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_TYPE);
				((Property)propertyList.get(0)).setValue(nic.getDeviceType());
				// デバイスINDEX
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_INDEX);
				((Property)propertyList.get(0)).setValue(nic.getDeviceIndex());
				// デバイス名
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_NAME);
				((Property)propertyList.get(0)).setValue(nic.getDeviceName());
				// デバイス表示名
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_DISPLAY_NAME);
				((Property)propertyList.get(0)).setValue(nic.getDeviceDisplayName());
				// デバイスサイズ
				if (nic.getDeviceSize() != -1) {
					propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_SIZE);
					((Property)propertyList.get(0)).setValue(nic.getDeviceSize());
				}
				// デバイスサイズ単位
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_SIZE_UNIT);
				((Property)propertyList.get(0)).setValue(nic.getDeviceSizeUnit());
				// NIC IP アドレス
				propertyList = PropertyUtil.getProperty(target, NodeConstant.NIC_IP_ADDRESS);
				((Property)propertyList.get(0)).setValue(nic.getNicIpAddress());
				// NIC MAC アドレス
				propertyList = PropertyUtil.getProperty(target, NodeConstant.NIC_MAC_ADDRESS);
				((Property)propertyList.get(0)).setValue(nic.getNicMacAddress());
				// 説明
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_DESCRIPTION);
				((Property)propertyList.get(0)).setValue(nic.getDeviceDescription());
			}
		}

		// ----- DISKデバイス情報 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.DISK);
		parentHost = (Property)((Property)propertyList.get(0)).getParent();

		deviceProperty = (Property)propertyList.get(0);
		if (node.getNodeDiskInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, deviceProperty);
			int cnt = 0;
			for (NodeDiskInfo disk : node.getNodeDiskInfo()) {
				Property target = null;
				if (cnt == 0) {
					target = deviceProperty;
				} else {
					target = PropertyUtil.copy(deviceProperty);
					parentHost.addChildren(target, index + cnt);
				}
				cnt++;

				// トップ表示情報（デバイス表示名）
				target.setValue(disk.getDeviceDisplayName());

				// デバイス種別
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_TYPE);
				((Property)propertyList.get(0)).setValue(disk.getDeviceType());
				// デバイスINDEX
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_INDEX);
				((Property)propertyList.get(0)).setValue(disk.getDeviceIndex());
				// デバイス名
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_NAME);
				((Property)propertyList.get(0)).setValue(disk.getDeviceName());
				// デバイス表示名
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_DISPLAY_NAME);
				((Property)propertyList.get(0)).setValue(disk.getDeviceDisplayName());
				// デバイスサイズ
				if (disk.getDeviceSize() != -1) {
					propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_SIZE);
					((Property)propertyList.get(0)).setValue(disk.getDeviceSize());
				}
				// デバイスサイズ単位
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_SIZE_UNIT);
				((Property)propertyList.get(0)).setValue(disk.getDeviceSizeUnit());
				// ディスク回転数
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DISK_RPM);
				((Property)propertyList.get(0)).setValue(disk.getDiskRpm());
				// 説明
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_DESCRIPTION);
				((Property)propertyList.get(0)).setValue(disk.getDeviceDescription());
			}
		}

		// ---- ファイルシステム情報 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.FILE_SYSTEM);
		parentHost = (Property)((Property)propertyList.get(0)).getParent();

		deviceProperty = (Property)propertyList.get(0);
		if (node.getNodeFilesystemInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, deviceProperty);
			int cnt = 0;
			for (NodeFilesystemInfo filesystem : node.getNodeFilesystemInfo()) {
				Property target = null;
				if (cnt == 0) {
					target = deviceProperty;
				} else {
					target = PropertyUtil.copy(deviceProperty);
					parentHost.addChildren(target, index + cnt);
				}
				cnt++;

				// トップ表示情報（デバイス表示名）
				target.setValue(filesystem.getDeviceDisplayName());

				// デバイス種別
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_TYPE);
				((Property)propertyList.get(0)).setValue(filesystem.getDeviceType());
				// デバイスINDEX
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_INDEX);
				((Property)propertyList.get(0)).setValue(filesystem.getDeviceIndex());
				// デバイス名
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_NAME);
				((Property)propertyList.get(0)).setValue(filesystem.getDeviceName());
				// デバイス表示名
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_DISPLAY_NAME);
				((Property)propertyList.get(0)).setValue(filesystem.getDeviceDisplayName());
				// デバイスサイズ
				if (filesystem.getDeviceSize() != -1) {
					propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_SIZE);
					((Property)propertyList.get(0)).setValue(filesystem.getDeviceSize());
				}
				// デバイスサイズ単位
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_SIZE_UNIT);
				((Property)propertyList.get(0)).setValue(filesystem.getDeviceSizeUnit());
				// ファイルシステム種別
				propertyList = PropertyUtil.getProperty(target, NodeConstant.FILE_SYSTEM_TYPE);
				((Property)propertyList.get(0)).setValue(filesystem.getFilesystemType());
				// 説明
				propertyList = PropertyUtil.getProperty(target, NodeConstant.DEVICE_DESCRIPTION);
				((Property)propertyList.get(0)).setValue(filesystem.getDeviceDescription());
			}
		}

		// ----- クラウド管理関連 -----
		// クラウドサービス
		propertyList = PropertyUtil.getProperty(property, NodeConstant.CLOUDSERVICE);
		((Property)propertyList.get(0)).setValue(node.getCloudService());
		// クラウドアカウントリソース
		propertyList = PropertyUtil.getProperty(property, NodeConstant.CLOUDSCOPE);
		((Property)propertyList.get(0)).setValue(node.getCloudScope());
		// クラウドリソースタイプ
		propertyList = PropertyUtil.getProperty(property, NodeConstant.CLOUDRESOURCETYPE);
		((Property)propertyList.get(0)).setValue(node.getCloudResourceType());
		// クラウドリソースID
		propertyList = PropertyUtil.getProperty(property, NodeConstant.CLOUDRESOURCEID);
		((Property)propertyList.get(0)).setValue(node.getCloudResourceId());
		// クラウドリージョン
		propertyList = PropertyUtil.getProperty(property, NodeConstant.CLOUDRESOURCENAME);
		((Property)propertyList.get(0)).setValue(node.getCloudResourceName());
		// クラウドゾーン
		propertyList = PropertyUtil.getProperty(property, NodeConstant.CLOUDLOCATION);
		((Property)propertyList.get(0)).setValue(node.getCloudLocation());

		// ----- ノード変数 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.GENERAL_NODE_VARIABLE);
		parentHost = (Property)((Property)propertyList.get(0)).getParent();

		deviceProperty = (Property)propertyList.get(0);
		if (node.getNodeVariableInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, deviceProperty);
			int cnt = 0;
			for (NodeVariableInfo variable : node.getNodeVariableInfo()) {
				Property target = null;
				if (cnt == 0) {
					target = deviceProperty;
				} else {
					target = PropertyUtil.copy(deviceProperty);
					parentHost.addChildren(target, index + cnt);
				}
				cnt++;

				// トップ表示情報（ノード変数名）
				target.setValue(variable.getNodeVariableName());

				// ノード変数名
				propertyList = PropertyUtil.getProperty(target, NodeConstant.NODE_VARIABLE_NAME);
				((Property)propertyList.get(0)).setValue(variable.getNodeVariableName());
				// ノード変数値
				propertyList = PropertyUtil.getProperty(target, NodeConstant.NODE_VARIABLE_VALUE);
				((Property)propertyList.get(0)).setValue(variable.getNodeVariableValue());
			}
		}


		// ----- 保守関連 -----
		// 連絡先
		propertyList = PropertyUtil.getProperty(property, NodeConstant.CONTACT);
		((Property)propertyList.get(0)).setValue(node.getContact());
		// 管理者
		propertyList = PropertyUtil.getProperty(property, NodeConstant.ADMINISTRATOR);
		((Property)propertyList.get(0)).setValue(node.getAdministrator());


		// ----- 備考 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.NOTE);
		Property noteProperty = (Property)propertyList.get(0);
		if (node.getNodeNoteInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, noteProperty);
			int cnt = 0;
			for (NodeNoteInfo note : node.getNodeNoteInfo()) {
				Property target = null;
				if (cnt == 0) {
					target = noteProperty;
				} else {
					target = PropertyUtil.copy(noteProperty);
					property.addChildren(target, index + cnt);
				}
				// 備考
				target.setValue(note.getNote());
				cnt++;
			}
		}

		return property;
	}


	/**
	 * ノード用プロパティを返します。
	 *
	 * @param mode
	 * @return ノード用プロパティ
	 */
	public static Property getProperty(String managerName, int mode, Locale locale) {

		// ------------------------
		// ---- 変数定義-----
		// ------------------------

		// ---- ファシリティ情報 -----
		//ファシリティID
		Property facilityId =
				new Property(NodeConstant.FACILITY_ID, Messages.getString("facility.id", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_512);
		//ファシリティ名
		Property facilityName =
				new Property(NodeConstant.FACILITY_NAME, Messages.getString("facility.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_128);
		//説明
		Property description =
				new Property(NodeConstant.DESCRIPTION, Messages.getString("description", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//有効/無効
		Property valid =
				new Property(NodeConstant.VALID, Messages.getString("management.object", locale), PropertyDefineConstant.EDITOR_BOOL);
		//自動デバイスサーチ
		Property autoDeviceSearch =
				new Property(NodeConstant.AUTO_DEVICE_SEARCH, Messages.getString("auto.device.search", locale), PropertyDefineConstant.EDITOR_BOOL);
		//登録者
		Property createTime =
				new Property(NodeConstant.CREATE_TIME, Messages.getString("create.time", locale), PropertyDefineConstant.EDITOR_DATETIME);
		//登録日時
		Property creatorName =
				new Property(NodeConstant.CREATOR_NAME, Messages.getString("creator.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//更新者
		Property modifyTime =
				new Property(NodeConstant.MODIFY_TIME, Messages.getString("update.time", locale), PropertyDefineConstant.EDITOR_DATETIME);
		//更新日時
		Property modifierName =
				new Property(NodeConstant.MODIFIER_NAME, Messages.getString("modifier.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);

		// ---- 基本情報 -----
		//基本情報
		Property basicInformation =
				new Property(NodeConstant.BASIC_INFORMATION, Messages.getString("basic.information", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);

		// ---- HW情報 -----
		//H/W
		Property hardware =
				new Property(NodeConstant.HARDWARE, Messages.getString("hardware", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_128);
		//H/Wタイプ
		Property hardwareType =
				new Property(NodeConstant.HARDWARE_TYPE, Messages.getString("hardware.type", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_128);
		//プラットフォーム
		Property platformFamilyName =
				new Property(NodeConstant.PLATFORM_FAMILY_NAME, Messages.getString("platform.family.name", locale), PropertyDefineConstant.EDITOR_SELECT);
		//サブプラットフォーム
		Property subPlatformFamilyName =
				new Property(NodeConstant.SUB_PLATFORM_FAMILY_NAME, Messages.getString("sub.platform.family.name", locale), PropertyDefineConstant.EDITOR_SELECT);
		//画面アイコンイメージ
		Property iconImage =
				new Property(NodeConstant.ICONIMAGE, Messages.getString("icon.image", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);


		// ---- IPアドレス情報 -----
		//ネットワーク
		Property network =
				new Property(NodeConstant.NETWORK, Messages.getString("network", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//IPバージョン
		Property ipAddressVersion =
				new Property(NodeConstant.IP_ADDRESS_VERSION, Messages.getString("ip.address.version", locale), PropertyDefineConstant.EDITOR_SELECT);
		//IPアドレスV4
		Property ipAddressV4 =
				new Property(NodeConstant.IP_ADDRESS_V4, Messages.getString("ip.address.v4", locale), PropertyDefineConstant.EDITOR_IPV4);
		//IPアドレスV6
		Property ipAddressV6 =
				new Property(NodeConstant.IP_ADDRESS_V6, Messages.getString("ip.address.v6", locale), PropertyDefineConstant.EDITOR_IPV6);
		//ホスト名
		Property hostName =
				new Property(NodeConstant.HOST_NAME, Messages.getString("host.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_128);


		// ---- OS情報 -----
		//OS
		Property os =
				new Property(NodeConstant.OS, Messages.getString("os", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//OS名
		Property osName =
				new Property(NodeConstant.OS_NAME, Messages.getString("os.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//ノード名
		Property nodeName =
				new Property(NodeConstant.NODE_NAME, Messages.getString("node.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_128);
		//OSリリース
		Property osRelease =
				new Property(NodeConstant.OS_RELEASE, Messages.getString("os.release", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//OSバージョン
		Property osVersion =
				new Property(NodeConstant.OS_VERSION, Messages.getString("os.version", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//文字セット
		Property characterSet =
				new Property(NodeConstant.CHARACTER_SET, Messages.getString("character.set", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_16);

		// ----- Hinemosエージェント情報-----
		//Hinemosエージェント
		Property agent =
				new Property(NodeConstant.AGENT, Messages.getString("agent", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//即時反映用ポート番号
		Property agentAwakePort =
				new Property(NodeConstant.AGENT_AWAKE_PORT, Messages.getString("agent.awake.port", locale), PropertyDefineConstant.EDITOR_NUM, DataRangeConstant.PORT_NUMBER_MAX, 0);

		//ジョブ
		Property job =
				new Property(NodeConstant.JOB, Messages.getString("job", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//ジョブ優先度
		Property jobPriority =
				new Property(NodeConstant.JOB_PRIORITY, Messages.getString("job.priority", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//ジョブ多重度
		Property jobMultiplicity =
				new Property(NodeConstant.JOB_MULTIPLICITY, Messages.getString("job.multiplicity", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);


		// ---- 基本情報 -----
		//サービス
		Property service =
				new Property(NodeConstant.SERVICE, Messages.getString("service", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);

		// ---- SNMP情報 -----
		//SNMP
		Property snmp =
				new Property(NodeConstant.SNMP, Messages.getString("snmp", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//SNMP接続ユーザ
		Property snmpUser =
				new Property(NodeConstant.SNMP_USER, Messages.getString("snmp.user", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//SNMPポート番号
		Property snmpPort =
				new Property(NodeConstant.SNMP_PORT, Messages.getString("snmp.port.number", locale), PropertyDefineConstant.EDITOR_NUM, DataRangeConstant.PORT_NUMBER_MAX, 0);
		//SNMPコミュニティ名
		Property snmpCommunity =
				new Property(NodeConstant.SNMP_COMMUNITY, Messages.getString("community.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//SNMPバージョン
		Property snmpVersion =
				new Property(NodeConstant.SNMP_VERSION, Messages.getString("snmp.version", locale), PropertyDefineConstant.EDITOR_SELECT);
		//SNMPセキュリティレベル
		Property snmpSecurityLevel =
				new Property(NodeConstant.SNMP_SECURITY_LEVEL, Messages.getString("snmp.security.level", locale), PropertyDefineConstant.EDITOR_SELECT);
		//SNMP接続認証パスワード
		Property snmpAuthPassword =
				new Property(NodeConstant.SNMP_AUTH_PASSWORD, Messages.getString("snmp.auth.password", locale),  PropertyDefineConstant.EDITOR_PASSWORD, DataRangeConstant.VARCHAR_64);
		//SNMP接続暗号化パスワード
		Property snmpPrivPassword =
				new Property(NodeConstant.SNMP_PRIV_PASSWORD, Messages.getString("snmp.priv.password", locale),  PropertyDefineConstant.EDITOR_PASSWORD, DataRangeConstant.VARCHAR_64);
		//SNMP認証プロトコル
		Property snmpAuthProtocol =
				new Property(NodeConstant.SNMP_AUTH_PROTOCOL, Messages.getString("snmp.auth.protocol", locale), PropertyDefineConstant.EDITOR_SELECT);
		//SNMP暗号化プロトコル
		Property snmpPrivProtocol =
				new Property(NodeConstant.SNMP_PRIV_PROTOCOL, Messages.getString("snmp.priv.protocol", locale), PropertyDefineConstant.EDITOR_SELECT);
		//SNMPタイムアウト
		Property snmpTimeout =
				new Property(NodeConstant.SNMPTIMEOUT, Messages.getString("snmp.timeout", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//SNMPリトライ回数
		Property snmpRetries =
				new Property(NodeConstant.SNMPRETRIES, Messages.getString("snmp.retries", locale), PropertyDefineConstant.EDITOR_NUM, 10, 0);


		// ---- WBEM情報 -----
		//WBEM
		Property wbem =
				new Property(NodeConstant.WBEM, Messages.getString("wbem", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//WBEM接続ユーザ
		Property wbemUser =
				new Property(NodeConstant.WBEM_USER, Messages.getString("wbem.user", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//WBEM接続ユーザパスワード
		Property wbemUserPassword =
				new Property(NodeConstant.WBEM_USER_PASSWORD, Messages.getString("wbem.user.password", locale),  PropertyDefineConstant.EDITOR_PASSWORD, DataRangeConstant.VARCHAR_64);
		//WBEM接続ポート番号
		Property wbemPort =
				new Property(NodeConstant.WBEM_PORT, Messages.getString("wbem.port.number", locale), PropertyDefineConstant.EDITOR_NUM, DataRangeConstant.PORT_NUMBER_MAX, 0);
		//WBEM接続プロトコル
		Property wbemProtocol =
				new Property(NodeConstant.WBEM_PROTOCOL, Messages.getString("wbem.protocol", locale),  PropertyDefineConstant.EDITOR_SELECT);
		//WBEM接続タイムアウト
		Property wbemTimeout =
				new Property(NodeConstant.WBEM_TIMEOUT, Messages.getString("wbem.timeout", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//WBEM接続リトライ回数
		Property wbemRetries =
				new Property(NodeConstant.WBEM_RETRIES, Messages.getString("wbem.retries", locale), PropertyDefineConstant.EDITOR_NUM, 10, 0);


		// ---- IPMI情報 -----
		//IPMI
		Property ipmi =
				new Property(NodeConstant.IPMI, Messages.getString("ipmi", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//IPMI接続IPアドレス
		Property ipmiIpAddress =
				new Property(NodeConstant.IPMI_IP_ADDRESS, Messages.getString("ipmi.ip.address", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//IPMI接続ポート番号
		Property ipmiPort =
				new Property(NodeConstant.IPMI_PORT, Messages.getString("ipmi.port.number", locale), PropertyDefineConstant.EDITOR_NUM, DataRangeConstant.PORT_NUMBER_MAX, 0);
		//IPMI接続ユーザ
		Property ipmiUser =
				new Property(NodeConstant.IPMI_USER, Messages.getString("ipmi.user", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//IPMI接続ユーザパスワード
		Property ipmiUserPassword =
				new Property(NodeConstant.IPMI_USER_PASSWORD, Messages.getString("ipmi.user.password", locale),  PropertyDefineConstant.EDITOR_PASSWORD, DataRangeConstant.VARCHAR_64);
		//IPMI接続タイムアウト
		Property ipmiTimeout =
				new Property(NodeConstant.IPMI_TIMEOUT, Messages.getString("ipmi.timeout", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//IPMI接続リトライ回数
		Property ipmiRetries =
				new Property(NodeConstant.IPMI_RETRIES, Messages.getString("ipmi.retries", locale), PropertyDefineConstant.EDITOR_NUM, 10, 0);
		//IPMI接続プロトコル
		Property ipmiProtocol =
				new Property(NodeConstant.IPMI_PROTOCOL, Messages.getString("ipmi.protocol", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_32);
		//IPMI特権レベル
		Property ipmiLevel =
				new Property(NodeConstant.IPMI_LEVEL, Messages.getString("ipmi.level", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_32);

		// ---- WinRM情報 -----
		Property winrm =
				new Property(NodeConstant.WINRM, Messages.getString("winrm", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//WinRM接続ユーザ
		Property winrmUser =
				new Property(NodeConstant.WINRM_USER, Messages.getString("winrm.user", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//WinRM接続ユーザパスワード
		Property winrmUserPassword =
				new Property(NodeConstant.WINRM_USER_PASSWORD, Messages.getString("winrm.user.password", locale),  PropertyDefineConstant.EDITOR_PASSWORD, DataRangeConstant.VARCHAR_64);
		//WinRMバージョン
		Property winrmVersion =
				new Property(NodeConstant.WINRM_VERSION, Messages.getString("winrm.version", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//WinRM接続ポート番号
		Property winrmPort =
				new Property(NodeConstant.WINRM_PORT, Messages.getString("winrm.port.number", locale), PropertyDefineConstant.EDITOR_NUM, DataRangeConstant.PORT_NUMBER_MAX, 0);
		//WinRM接続プロトコル
		Property winrmProtocol =
				new Property(NodeConstant.WINRM_PROTOCOL, Messages.getString("winrm.protocol", locale),  PropertyDefineConstant.EDITOR_SELECT);
		//WinRM接続タイムアウト
		Property winrmTimeout =
				new Property(NodeConstant.WINRM_TIMEOUT, Messages.getString("winrm.timeout", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//WinRM接続リトライ回数
		Property winrmRetries =
				new Property(NodeConstant.WINRM_RETRIES, Messages.getString("winrm.retries", locale), PropertyDefineConstant.EDITOR_NUM, 10, 0);

		// ---- SSH関連 ----
		Property ssh =
				new Property(NodeConstant.SSH, Messages.getString("ssh", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//SSH接続ユーザ
		Property sshUser =
				new Property(NodeConstant.SSH_USER, Messages.getString("ssh.user", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//SSH接続ユーザパスワード
		Property sshUserPassword =
				new Property(NodeConstant.SSH_USER_PASSWORD, Messages.getString("ssh.user.password", locale),  PropertyDefineConstant.EDITOR_PASSWORD, DataRangeConstant.VARCHAR_64);
		//SSH秘密鍵ファイル名
		Property sshPrivateKeyFilepath =
				new Property(NodeConstant.SSH_PRIVATE_KEY_FILEPATH, Messages.getString("ssh.private.key.filepath", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//SSH秘密鍵パスフレーズ
		Property sshPrivateKeyPassphrase =
				new Property(NodeConstant.SSH_PRIVATE_KEY_PASSPHRASE, Messages.getString("ssh.private.key.passphrase", locale),  PropertyDefineConstant.EDITOR_PASSWORD, DataRangeConstant.VARCHAR_1024);
		//SSHポート番号
		Property sshPort =
				new Property(NodeConstant.SSH_PORT, Messages.getString("ssh.port", locale),  PropertyDefineConstant.EDITOR_NUM, DataRangeConstant.PORT_NUMBER_MAX, 0);
		//SSHタイムアウト
		Property sshTimeout =
				new Property(NodeConstant.SSH_TIMEOUT, Messages.getString("ssh.timeout", locale),  PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);

		// ---- 汎用デバイス情報 -----
		//デバイス
		Property device =
				new Property(NodeConstant.DEVICE, Messages.getString("device", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//汎用デバイス
		Property generalDevice =
				new Property(NodeConstant.GENERAL_DEVICE, Messages.getString("general.device", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//汎用デバイス情報
		Property generalDeviceList =
				new Property(NodeConstant.GENERAL_DEVICE_LIST, Messages.getString("general.device.list", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//デバイス表示名
		Property deviceDisplayName =
				new Property(NodeConstant.DEVICE_DISPLAY_NAME, Messages.getString("device.display.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//デバイス名
		Property deviceName =
				new Property(NodeConstant.DEVICE_NAME, Messages.getString("device.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_128);
		//デバイスINDEX
		Property deviceIndex =
				new Property(NodeConstant.DEVICE_INDEX, Messages.getString("device.index", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//デバイス種別
		Property deviceType =
				new Property(NodeConstant.DEVICE_TYPE, Messages.getString("device.type", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_32);
		//デバイスサイズ
		Property deviceSize =
				new Property(NodeConstant.DEVICE_SIZE, Messages.getString("device.size", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//デバイスサイズ単位
		Property deviceSizeUnit =
				new Property(NodeConstant.DEVICE_SIZE_UNIT, Messages.getString("device.size.unit", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//説明
		Property deviceDescription =
				new Property(NodeConstant.DEVICE_DESCRIPTION, Messages.getString("description", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);


		// ---- CPUデバイス情報 -----
		//CPU
		Property cpu =
				new Property(NodeConstant.CPU, Messages.getString("cpu", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//CPU情報
		Property cpuList =
				new Property(NodeConstant.CPU_LIST, Messages.getString("cpu.list", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//デバイス表示名
		Property cpuDeviceDisplayName =
				new Property(NodeConstant.DEVICE_DISPLAY_NAME, Messages.getString("device.display.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//デバイス名
		Property cpuDeviceName =
				new Property(NodeConstant.DEVICE_NAME, Messages.getString("device.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_128);
		//デバイスINDEX
		Property cpuDeviceIndex =
				new Property(NodeConstant.DEVICE_INDEX, Messages.getString("device.index", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//デバイス種別
		Property cpuDeviceType =
				new Property(NodeConstant.DEVICE_TYPE, Messages.getString("device.type", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_32);
		//デバイスサイズ
		Property cpuDeviceSize =
				new Property(NodeConstant.DEVICE_SIZE, Messages.getString("device.size", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//デバイスサイズ単位
		Property cpuDeviceSizeUnit =
				new Property(NodeConstant.DEVICE_SIZE_UNIT, Messages.getString("device.size.unit", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//説明
		Property cpuDeviceDescription =
				new Property(NodeConstant.DEVICE_DESCRIPTION, Messages.getString("description", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);


		// ---- MEMデバイス情報 -----
		//MEM
		Property memory =
				new Property(NodeConstant.MEMORY, Messages.getString("memory", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//MEM
		Property memoryList =
				new Property(NodeConstant.MEMORY_LIST, Messages.getString("memory.list", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//デバイス表示名
		Property memoryDeviceDisplayName =
				new Property(NodeConstant.DEVICE_DISPLAY_NAME, Messages.getString("device.display.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//デバイス名
		Property memoryDeviceName =
				new Property(NodeConstant.DEVICE_NAME, Messages.getString("device.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_128);
		//デバイスINDEX
		Property memoryDeviceIndex =
				new Property(NodeConstant.DEVICE_INDEX, Messages.getString("device.index", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//デバイス種別
		Property memoryDeviceType =
				new Property(NodeConstant.DEVICE_TYPE, Messages.getString("device.type", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_32);
		//デバイスサイズ
		Property memoryDeviceSize =
				new Property(NodeConstant.DEVICE_SIZE, Messages.getString("device.size", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//デバイスサイズ単位
		Property memoryDeviceSizeUnit =
				new Property(NodeConstant.DEVICE_SIZE_UNIT, Messages.getString("device.size.unit", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//説明
		Property memoryDeviceDescription =
				new Property(NodeConstant.DEVICE_DESCRIPTION, Messages.getString("description", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);


		// ---- NICデバイス情報 -----
		//NIC
		Property networkInterface =
				new Property(NodeConstant.NETWORK_INTERFACE, Messages.getString("network.interface", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//NIC情報
		Property networkInterfaceList =
				new Property(NodeConstant.NETWORK_INTERFACE_LIST, Messages.getString("network.interface.list", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//デバイス表示名
		Property networkInterfaceDeviceDisplayName =
				new Property(NodeConstant.DEVICE_DISPLAY_NAME, Messages.getString("device.display.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//デバイス名
		Property networkInterfaceDeviceName =
				new Property(NodeConstant.DEVICE_NAME, Messages.getString("device.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_128);
		//デバイスINDEX
		Property networkInterfaceDeviceIndex =
				new Property(NodeConstant.DEVICE_INDEX, Messages.getString("device.index", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//デバイス種別
		Property networkInterfaceDeviceType =
				new Property(NodeConstant.DEVICE_TYPE, Messages.getString("device.type", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_32);
		//デバイスサイズ
		Property networkInterfaceDeviceSize =
				new Property(NodeConstant.DEVICE_SIZE, Messages.getString("device.size", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//デバイスサイズ単位
		Property networkInterfaceDeviceSizeUnit =
				new Property(NodeConstant.DEVICE_SIZE_UNIT, Messages.getString("device.size.unit", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//説明
		Property networkInterfaceDeviceDescription =
				new Property(NodeConstant.DEVICE_DESCRIPTION, Messages.getString("description", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//NIC IPアドレス
		Property nicIpAddress =
				new Property(NodeConstant.NIC_IP_ADDRESS, Messages.getString("nic.ip.address", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//NIC MACアドレス
		Property nicMacAddress =
				new Property(NodeConstant.NIC_MAC_ADDRESS, Messages.getString("nic.mac.address", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);


		// ---- DISKデバイス情報 -----
		//DISK
		Property disk =
				new Property(NodeConstant.DISK, Messages.getString("disk", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//DISK情報
		Property diskList =
				new Property(NodeConstant.DISK_LIST, Messages.getString("disk.list", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//デバイス表示名
		Property diskDeviceDisplayName =
				new Property(NodeConstant.DEVICE_DISPLAY_NAME, Messages.getString("device.display.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//デバイス名
		Property diskDeviceName =
				new Property(NodeConstant.DEVICE_NAME, Messages.getString("device.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_128);
		//デバイスINDEX
		Property diskDeviceIndex =
				new Property(NodeConstant.DEVICE_INDEX, Messages.getString("device.index", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//デバイス種別
		Property diskDeviceType =
				new Property(NodeConstant.DEVICE_TYPE, Messages.getString("device.type", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_32);
		//デバイスサイズ
		Property diskDeviceSize =
				new Property(NodeConstant.DEVICE_SIZE, Messages.getString("device.size", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//デバイスサイズ単位
		Property diskDeviceSizeUnit =
				new Property(NodeConstant.DEVICE_SIZE_UNIT, Messages.getString("device.size.unit", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//説明
		Property diskDeviceDescription =
				new Property(NodeConstant.DEVICE_DESCRIPTION, Messages.getString("description", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//DISK回転数
		Property diskRpm =
				new Property(NodeConstant.DISK_RPM, Messages.getString("disk.rpm", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);


		// ---- ファイルシステムデバイス情報 -----
		//ファイルシステム
		Property fileSystem =
				new Property(NodeConstant.FILE_SYSTEM, Messages.getString("file.system", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//ファイルシステム
		Property fileSystemList =
				new Property(NodeConstant.FILE_SYSTEM_LIST, Messages.getString("file.system.list", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//デバイス表示名
		Property fileSystemDeviceDisplayName =
				new Property(NodeConstant.DEVICE_DISPLAY_NAME, Messages.getString("device.display.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//デバイス名
		Property fileSystemDeviceName =
				new Property(NodeConstant.DEVICE_NAME, Messages.getString("device.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_128);
		//デバイスINDEX
		Property fileSystemDeviceIndex =
				new Property(NodeConstant.DEVICE_INDEX, Messages.getString("device.index", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//デバイス種別
		Property fileSystemDeviceType =
				new Property(NodeConstant.DEVICE_TYPE, Messages.getString("device.type", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_32);
		//デバイスサイズ
		Property fileSystemDeviceSize =
				new Property(NodeConstant.DEVICE_SIZE, Messages.getString("device.size", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//デバイスサイズ単位
		Property fileSystemDeviceSizeUnit =
				new Property(NodeConstant.DEVICE_SIZE_UNIT, Messages.getString("device.size.unit", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//説明
		Property fileSystemDeviceDescription =
				new Property(NodeConstant.DEVICE_DESCRIPTION, Messages.getString("description", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//ファイルシステム種別
		Property fileSystemType =
				new Property(NodeConstant.FILE_SYSTEM_TYPE, Messages.getString("file.system.type", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);


		// ---- クラウド・仮想化管理情報 -----
		//クラウド管理
		Property cloudManagement =
				new Property(NodeConstant.CLOUD_MANAGEMENT, Messages.getString("cloud.management", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//クラウドサービス
		Property cloudService =
				new Property(NodeConstant.CLOUDSERVICE, Messages.getString("cloud.service", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//クラウドスコープ
		Property cloudScope =
				new Property(NodeConstant.CLOUDSCOPE, Messages.getString("cloud.scope", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//クラウドリソースタイプ
		Property cloudResourceType =
				new Property(NodeConstant.CLOUDRESOURCETYPE, Messages.getString("cloud.resource.type", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//クラウドリソースID
		Property cloudResourceId =
				new Property(NodeConstant.CLOUDRESOURCEID, Messages.getString("cloud.resource.id", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//クラウドリソース名
		Property cloudResourceName =
				new Property(NodeConstant.CLOUDRESOURCENAME, Messages.getString("cloud.resource.name", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//クラウドロケーション
		Property cloudLocation =
				new Property(NodeConstant.CLOUDLOCATION, Messages.getString("cloud.location", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);

		// ---- ノード変数 -----
		Property nodeVariable =
				new Property(NodeConstant.NODE_VARIABLE, Messages.getString("node.variable", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		Property generalNodeVariable =
				new Property(NodeConstant.GENERAL_NODE_VARIABLE, Messages.getString("node.variable", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		Property nodeVariableName =
				new Property(NodeConstant.NODE_VARIABLE_NAME, Messages.getString("node.variable.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_128);
		Property nodeVariableValues =
				new Property(NodeConstant.NODE_VARIABLE_VALUE, Messages.getString("node.variable.value", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);

		// ---- 保守情報 -----
		//保守
		Property maintenance =
				new Property(NodeConstant.MAINTENANCE, Messages.getString("maintenance", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//管理者
		Property administrator =
				new Property(NodeConstant.ADMINISTRATOR, Messages.getString("administrator", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//連絡先
		Property contact =
				new Property(NodeConstant.CONTACT, Messages.getString("contact", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//備考
		Property note =
				new Property(NodeConstant.NOTE, Messages.getString("note", locale), PropertyDefineConstant.EDITOR_TEXTAREA, DataRangeConstant.VARCHAR_1024);


		// ------------------------
		// ---- 初期化 -----
		// ------------------------

		// ---- ファシリティ情報 -----
		facilityId.setValue("");
		facilityName.setValue("");
		description.setValue("");
		valid.setValue(true);
		autoDeviceSearch.setValue(true);
		createTime.setValue("");
		creatorName.setValue("");
		modifyTime.setValue("");
		modifierName.setValue("");

		// ---- 基本情報 -----
		basicInformation.setValue("");

		// ---- HW情報 -----
		hardware.setValue("");
		hardwareType.setValue("");
		platformFamilyName.setSelectValues(getPlatformNames(managerName));
		platformFamilyName.setValue("");
		subPlatformFamilyName.setSelectValues(getSubPlatformNames(managerName));
		subPlatformFamilyName.setValue("");
		iconImage.setValue("");

		// ---- IPアドレス情報 -----
		network.setValue("");
		Object ipVersionValue[][] = {
				{ "4", "6" },
				{ 4, 6 }
		};
		ipAddressVersion.setSelectValues(ipVersionValue);
		ipAddressVersion.setValue(4);
		ipAddressV4.setValue("");
		ipAddressV6.setValue("");
		hostName.setValue("");

		// ---- OS情報 -----
		os.setValue("");
		osName.setValue("");
		nodeName.setValue("");
		osRelease.setValue("");
		osVersion.setValue("");
		characterSet.setValue("");

		// ----- Hinemosエージェント情報 -----
		agent.setValue("");
		agentAwakePort.setValue("");

		// ---- ジョブ -----
		job.setValue("");
		jobPriority.setValue(16);
		jobMultiplicity.setValue(0);

		// ---- サービス情報 -----
		service.setValue("");

		// ---- SNMP情報 -----
		snmp.setValue("");
		Object snmpVersionValue[][] = {
				{ "", SnmpVersionConstant.STRING_V1, SnmpVersionConstant.STRING_V2, SnmpVersionConstant.STRING_V3 },
				{ "", SnmpVersionConstant.STRING_V1, SnmpVersionConstant.STRING_V2, SnmpVersionConstant.STRING_V3 }
		};
		Object snmpSecurityLevelValue[][] = {
				{ "", SnmpSecurityLevelConstant.NOAUTH_NOPRIV, SnmpSecurityLevelConstant.AUTH_NOPRIV, SnmpSecurityLevelConstant.AUTH_PRIV },
				{ "", SnmpSecurityLevelConstant.NOAUTH_NOPRIV, SnmpSecurityLevelConstant.AUTH_NOPRIV, SnmpSecurityLevelConstant.AUTH_PRIV },
		};
		Object snmpAuthProtocolValue[][] = {
				{ "", SnmpProtocolConstant.MD5, SnmpProtocolConstant.SHA },
				{ "", SnmpProtocolConstant.MD5, SnmpProtocolConstant.SHA },
		};
		Object snmpPrivProtocolValue[][] = {
				{ "", SnmpProtocolConstant.DES, SnmpProtocolConstant.AES },
				{ "", SnmpProtocolConstant.DES, SnmpProtocolConstant.AES },
		};
		snmpUser.setValue("");
		snmpPort.setValue("");
		snmpCommunity.setValue("");
		snmpVersion.setSelectValues(snmpVersionValue);
		snmpVersion.setValue("");
		snmpSecurityLevel.setSelectValues(snmpSecurityLevelValue);
		snmpSecurityLevel.setValue("");
		snmpAuthPassword.setValue("");
		snmpPrivPassword.setValue("");
		snmpAuthProtocol.setSelectValues(snmpAuthProtocolValue);
		snmpAuthProtocol.setValue("");
		snmpPrivProtocol.setSelectValues(snmpPrivProtocolValue);
		snmpPrivProtocol.setValue("");
		snmpTimeout.setValue("");
		snmpRetries.setValue("");
		snmpPort.setValue("");


		// ---- WBEM情報 -----
		wbem.setValue("");
		Object wbemProtocolValue[][] = {
				{"", "http", "https"},
				{"", "http", "https"}
		};
		wbemUser.setValue("");
		wbemUserPassword.setValue("");
		wbemPort.setValue("");
		wbemProtocol.setSelectValues(wbemProtocolValue);
		wbemProtocol.setValue("");
		wbemTimeout.setValue("");
		wbemRetries.setValue("");


		// ---- IPMI情報 -----
		ipmi.setValue("");
		ipmiIpAddress.setValue("");
		ipmiPort.setValue("");
		ipmiUser.setValue("");
		ipmiUserPassword.setValue("");
		ipmiTimeout.setValue("");
		ipmiRetries.setValue("");
		ipmiProtocol.setValue("");
		ipmiLevel.setValue("");

		// ---- WinRM情報 -----
		Object winrmProtocolValue[][] = {
				{"", "http", "https"},
				{"", "http", "https"}
		};
		winrm.setValue("");
		winrmUser.setValue("");
		winrmUserPassword.setValue("");
		winrmVersion.setValue("");
		winrmPort.setValue("");
		winrmProtocol.setSelectValues(winrmProtocolValue);
		winrmProtocol.setValue("");
		winrmTimeout.setValue("");
		winrmRetries.setValue("");

		// ---- SSH情報 -----
		ssh.setValue("");
		sshUser.setValue("");
		sshUserPassword.setValue("");
		sshPrivateKeyFilepath.setValue("");
		sshPrivateKeyPassphrase.setValue("");
		sshPort.setValue("");
		sshTimeout.setValue("");
		
		// ---- デバイス情報 -----
		device.setValue("");

		// ---- 汎用デバイス情報 -----
		generalDevice.setValue("");
		generalDeviceList.setValue("");
		deviceDisplayName.setValue("");
		deviceName.setValue("");
		deviceIndex.setValue("");
		deviceType.setValue("");
		deviceSize.setValue(0);
		deviceSizeUnit.setValue("");
		deviceDescription.setValue("");

		// ---- CPU情報 -----
		cpu.setValue("");
		cpuList.setValue("");
		cpuDeviceDisplayName.setValue("");
		cpuDeviceName.setValue("");
		cpuDeviceIndex.setValue("");
		cpuDeviceType.setValue(DeviceTypeConstant.DEVICE_CPU);
		cpuDeviceSize.setValue(0);
		cpuDeviceSizeUnit.setValue("");
		cpuDeviceDescription.setValue("");

		// ---- MEM情報 -----
		memory.setValue("");
		memoryList.setValue("");
		memoryDeviceDisplayName.setValue("");
		memoryDeviceName.setValue("");
		memoryDeviceIndex.setValue("");
		memoryDeviceType.setValue(DeviceTypeConstant.DEVICE_MEM);
		memoryDeviceSize.setValue(0);
		memoryDeviceSizeUnit.setValue("");
		memoryDeviceDescription.setValue("");

		// ---- NIC情報 -----
		networkInterface.setValue("");
		networkInterfaceList.setValue("");
		networkInterfaceDeviceDisplayName.setValue("");
		networkInterfaceDeviceName.setValue("");
		networkInterfaceDeviceIndex.setValue("");
		networkInterfaceDeviceType.setValue(DeviceTypeConstant.DEVICE_NIC);
		networkInterfaceDeviceSize.setValue(0);
		networkInterfaceDeviceSizeUnit.setValue("");
		networkInterfaceDeviceDescription.setValue("");
		nicIpAddress.setValue("");
		nicMacAddress.setValue("");

		// ---- DISK情報 -----
		disk.setValue("");
		diskList.setValue("");
		diskDeviceDisplayName.setValue("");
		diskDeviceName.setValue("");
		diskDeviceIndex.setValue("");
		diskDeviceType.setValue(DeviceTypeConstant.DEVICE_DISK);
		diskDeviceSize.setValue(0);
		diskDeviceSizeUnit.setValue("");
		diskDeviceDescription.setValue("");
		diskRpm.setValue("");

		// ---- ファイルシステム情報 -----
		fileSystem.setValue("");
		fileSystemList.setValue("");
		fileSystemDeviceDisplayName.setValue("");
		fileSystemDeviceName.setValue("");
		fileSystemDeviceIndex.setValue("");
		fileSystemDeviceType.setValue(DeviceTypeConstant.DEVICE_FILESYSTEM);
		fileSystemDeviceSize.setValue(0);
		fileSystemDeviceSizeUnit.setValue("");
		fileSystemDeviceDescription.setValue("");
		fileSystemType.setValue("");


		// ---- クラウド・仮想化管理情報 -----
		cloudManagement.setValue("");
		cloudService.setValue("");
		cloudScope.setValue("");
		cloudResourceType.setValue("");
		cloudResourceId.setValue("");
		cloudResourceName.setValue("");
		cloudLocation.setValue("");


		// ---- ノード変数情報 -----
		nodeVariable.setValue("");
		generalNodeVariable.setValue("");
		nodeVariableName.setValue("");
		nodeVariableValues.setValue("");


		// ---- 保守情報 -----
		maintenance.setValue("");
		administrator.setValue("");
		contact.setValue("");
		note.setValue("");


		// ------------------------
		// ---- 変更可・コピーモード設定 -----
		// ------------------------
		//モードにより、変更可及びコピー可を設定
		if(mode == PropertyDefineConstant.MODE_ADD ||
				mode == PropertyDefineConstant.MODE_MODIFY){

			// ---- ファシリティ情報 ----
			if(mode == PropertyDefineConstant.MODE_ADD){
				facilityId.setModify(PropertyDefineConstant.MODIFY_OK); // ファシリティIDのみ変更不可
			}
			facilityName.setModify(PropertyDefineConstant.MODIFY_OK);
			description.setModify(PropertyDefineConstant.MODIFY_OK);
			valid.setModify(PropertyDefineConstant.MODIFY_OK);
			autoDeviceSearch.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- 基本情報 -----

			// ---- HW情報 -----
			hardwareType.setModify(PropertyDefineConstant.MODIFY_OK);
			platformFamilyName.setModify(PropertyDefineConstant.MODIFY_OK);
			subPlatformFamilyName.setModify(PropertyDefineConstant.MODIFY_OK);
			iconImage.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- IPアドレス情報 -----
			ipAddressVersion.setModify(PropertyDefineConstant.MODIFY_OK);
			ipAddressV4.setModify(PropertyDefineConstant.MODIFY_OK);
			ipAddressV6.setModify(PropertyDefineConstant.MODIFY_OK);
			hostName.setModify(PropertyDefineConstant.MODIFY_OK);
			hostName.setCopy(PropertyDefineConstant.COPY_OK);

			// ---- OS情報 -----
			osName.setModify(PropertyDefineConstant.MODIFY_OK);
			nodeName.setModify(PropertyDefineConstant.MODIFY_OK);
			osRelease.setModify(PropertyDefineConstant.MODIFY_OK);
			osVersion.setModify(PropertyDefineConstant.MODIFY_OK);
			characterSet.setModify(PropertyDefineConstant.MODIFY_OK);

			// ----- Hinemosエージェント情報 -----
			agentAwakePort.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- ジョブ -----
			jobPriority.setModify(PropertyDefineConstant.MODIFY_OK);
			jobMultiplicity.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- サービス情報 -----

			// ---- SNMP情報 -----
			snmpUser.setModify(PropertyDefineConstant.MODIFY_OK);
			snmpPort.setModify(PropertyDefineConstant.MODIFY_OK);
			snmpCommunity.setModify(PropertyDefineConstant.MODIFY_OK);
			snmpVersion.setModify(PropertyDefineConstant.MODIFY_OK);
			snmpSecurityLevel.setModify(PropertyDefineConstant.MODIFY_OK);
			snmpAuthPassword.setModify(PropertyDefineConstant.MODIFY_OK);
			snmpPrivPassword.setModify(PropertyDefineConstant.MODIFY_OK);
			snmpAuthProtocol.setModify(PropertyDefineConstant.MODIFY_OK);
			snmpPrivProtocol.setModify(PropertyDefineConstant.MODIFY_OK);
			snmpTimeout.setModify(PropertyDefineConstant.MODIFY_OK);
			snmpRetries.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- WBEM情報 -----
			wbemUser.setModify(PropertyDefineConstant.MODIFY_OK);
			wbemUserPassword.setModify(PropertyDefineConstant.MODIFY_OK);
			wbemPort.setModify(PropertyDefineConstant.MODIFY_OK);
			wbemProtocol.setModify(PropertyDefineConstant.MODIFY_OK);
			wbemTimeout.setModify(PropertyDefineConstant.MODIFY_OK);
			wbemRetries.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- IPMI情報 -----
			ipmiIpAddress.setModify(PropertyDefineConstant.MODIFY_OK);
			ipmiPort.setModify(PropertyDefineConstant.MODIFY_OK);
			ipmiUser.setModify(PropertyDefineConstant.MODIFY_OK);
			ipmiUserPassword.setModify(PropertyDefineConstant.MODIFY_OK);
			ipmiTimeout.setModify(PropertyDefineConstant.MODIFY_OK);
			ipmiRetries.setModify(PropertyDefineConstant.MODIFY_OK);
			ipmiProtocol.setModify(PropertyDefineConstant.MODIFY_OK);
			ipmiLevel.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- WinRM情報 -----
			winrmUser.setModify(PropertyDefineConstant.MODIFY_OK);
			winrmUserPassword.setModify(PropertyDefineConstant.MODIFY_OK);
			winrmVersion.setModify(PropertyDefineConstant.MODIFY_OK);
			winrmPort.setModify(PropertyDefineConstant.MODIFY_OK);
			winrmProtocol.setModify(PropertyDefineConstant.MODIFY_OK);
			winrmTimeout.setModify(PropertyDefineConstant.MODIFY_OK);
			winrmRetries.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- SSH情報 -----
			sshUser.setModify(PropertyDefineConstant.MODIFY_OK);
			sshUserPassword.setModify(PropertyDefineConstant.MODIFY_OK);
			sshPrivateKeyFilepath.setModify(PropertyDefineConstant.MODIFY_OK);
			sshPrivateKeyPassphrase.setModify(PropertyDefineConstant.MODIFY_OK);
			sshPort.setModify(PropertyDefineConstant.MODIFY_OK);
			sshTimeout.setModify(PropertyDefineConstant.MODIFY_OK);
			
			// ---- デバイス情報 -----

			// ---- 汎用デバイス情報 -----
			generalDevice.setCopy(PropertyDefineConstant.COPY_OK);
			deviceDisplayName.setModify(PropertyDefineConstant.MODIFY_OK);
			deviceName.setModify(PropertyDefineConstant.MODIFY_OK);
			deviceIndex.setModify(PropertyDefineConstant.MODIFY_OK);
			deviceType.setModify(PropertyDefineConstant.MODIFY_OK);
			deviceSize.setModify(PropertyDefineConstant.MODIFY_OK);
			deviceSizeUnit.setModify(PropertyDefineConstant.MODIFY_OK);
			deviceDescription.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- CPUデバイス情報 -----
			cpu.setCopy(PropertyDefineConstant.COPY_OK);
			cpuDeviceDisplayName.setModify(PropertyDefineConstant.MODIFY_OK);
			cpuDeviceName.setModify(PropertyDefineConstant.MODIFY_OK);
			cpuDeviceIndex.setModify(PropertyDefineConstant.MODIFY_OK);
			cpuDeviceType.setModify(PropertyDefineConstant.MODIFY_OK);
			cpuDeviceSize.setModify(PropertyDefineConstant.MODIFY_OK);
			cpuDeviceSizeUnit.setModify(PropertyDefineConstant.MODIFY_OK);
			cpuDeviceDescription.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- MEMデバイス情報 -----
			memoryDeviceDisplayName.setModify(PropertyDefineConstant.MODIFY_OK);
			memoryDeviceName.setModify(PropertyDefineConstant.MODIFY_OK);
			memoryDeviceIndex.setModify(PropertyDefineConstant.MODIFY_OK);
			memoryDeviceType.setModify(PropertyDefineConstant.MODIFY_OK);
			memoryDeviceSize.setModify(PropertyDefineConstant.MODIFY_OK);
			memoryDeviceSizeUnit.setModify(PropertyDefineConstant.MODIFY_OK);
			memoryDeviceDescription.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- NICデバイス情報 -----
			networkInterface.setCopy(PropertyDefineConstant.COPY_OK);
			networkInterfaceDeviceDisplayName.setModify(PropertyDefineConstant.MODIFY_OK);
			networkInterfaceDeviceName.setModify(PropertyDefineConstant.MODIFY_OK);
			networkInterfaceDeviceIndex.setModify(PropertyDefineConstant.MODIFY_OK);
			networkInterfaceDeviceType.setModify(PropertyDefineConstant.MODIFY_OK);
			networkInterfaceDeviceSize.setModify(PropertyDefineConstant.MODIFY_OK);
			networkInterfaceDeviceSizeUnit.setModify(PropertyDefineConstant.MODIFY_OK);
			networkInterfaceDeviceDescription.setModify(PropertyDefineConstant.MODIFY_OK);
			nicIpAddress.setModify(PropertyDefineConstant.MODIFY_OK);
			nicMacAddress.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- DISKデバイス情報 -----
			disk.setCopy(PropertyDefineConstant.COPY_OK);
			diskDeviceDisplayName.setModify(PropertyDefineConstant.MODIFY_OK);
			diskDeviceName.setModify(PropertyDefineConstant.MODIFY_OK);
			diskDeviceIndex.setModify(PropertyDefineConstant.MODIFY_OK);
			diskDeviceType.setModify(PropertyDefineConstant.MODIFY_OK);
			diskDeviceSize.setModify(PropertyDefineConstant.MODIFY_OK);
			diskDeviceSizeUnit.setModify(PropertyDefineConstant.MODIFY_OK);
			diskDeviceDescription.setModify(PropertyDefineConstant.MODIFY_OK);
			diskRpm.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- ファイルシステムデバイス情報 -----
			fileSystem.setCopy(PropertyDefineConstant.COPY_OK);
			fileSystemDeviceDisplayName.setModify(PropertyDefineConstant.MODIFY_OK);
			fileSystemDeviceName.setModify(PropertyDefineConstant.MODIFY_OK);
			fileSystemDeviceIndex.setModify(PropertyDefineConstant.MODIFY_OK);
			fileSystemDeviceType.setModify(PropertyDefineConstant.MODIFY_OK);
			fileSystem.setModify(PropertyDefineConstant.MODIFY_OK);
			fileSystemDeviceSize.setModify(PropertyDefineConstant.MODIFY_OK);
			fileSystemDeviceSizeUnit.setModify(PropertyDefineConstant.MODIFY_OK);
			fileSystemDeviceDescription.setModify(PropertyDefineConstant.MODIFY_OK);
			fileSystemType.setModify(PropertyDefineConstant.MODIFY_OK);


			// ---- クラウド管理情報 -----
			cloudService.setModify(PropertyDefineConstant.MODIFY_OK);
			cloudScope.setModify(PropertyDefineConstant.MODIFY_OK);
			cloudResourceType.setModify(PropertyDefineConstant.MODIFY_OK);
			cloudResourceId.setModify(PropertyDefineConstant.MODIFY_OK);
			cloudResourceName.setModify(PropertyDefineConstant.MODIFY_OK);
			cloudLocation.setModify(PropertyDefineConstant.MODIFY_OK);


			// ---- ノード変数情報 -----
			generalNodeVariable.setCopy(PropertyDefineConstant.COPY_OK);
			nodeVariableName.setModify(PropertyDefineConstant.MODIFY_OK);
			nodeVariableValues.setModify(PropertyDefineConstant.MODIFY_OK);


			// ---- 保守情報 -----
			administrator.setModify(PropertyDefineConstant.MODIFY_OK);
			contact.setModify(PropertyDefineConstant.MODIFY_OK);
			note.setCopy(PropertyDefineConstant.COPY_OK);
			note.setModify(PropertyDefineConstant.MODIFY_OK);
			note.setModify(PropertyDefineConstant.MODIFY_OK);

		}


		// ------------------------
		// ---- ツリを構成。 -----
		// ------------------------

		// ---- 初期表示ツリーを構成。
		Property property = new Property(null, null, "");
		property.removeChildren();
		//ファシリティ情報
		property.addChildren(facilityId);
		property.addChildren(facilityName);
		property.addChildren(description);
		property.addChildren(valid);
		property.addChildren(autoDeviceSearch);

		//基本情報
		property.addChildren(basicInformation);
		//ジョブ
		property.addChildren(job);
		//サービス
		property.addChildren(service);
		//デバイス
		property.addChildren(device);
		//クラウド・仮想化管理
		property.addChildren(cloudManagement);
		//ノード変数
		property.addChildren(nodeVariable);
		//保守
		property.addChildren(maintenance);
		//ファシリティ情報(後編)
		property.addChildren(createTime);
		property.addChildren(creatorName);
		property.addChildren(modifyTime);
		property.addChildren(modifierName);
		//ファシリティ情報(備考)
		property.addChildren(note);


		// ---- 基本情報ツリー
		basicInformation.removeChildren();
		basicInformation.addChildren(hardware);
		basicInformation.addChildren(network);
		basicInformation.addChildren(os);
		basicInformation.addChildren(agent);

		// HWツリー
		hardware.removeChildren();
		hardware.addChildren(platformFamilyName);
		hardware.addChildren(subPlatformFamilyName);
		hardware.addChildren(hardwareType);
		hardware.addChildren(iconImage);

		// ネットワークツリー
		network.removeChildren();
		network.addChildren(ipAddressVersion);
		network.addChildren(ipAddressV4);
		network.addChildren(ipAddressV6);
		network.addChildren(hostName);

		// OSツリー
		os.removeChildren();
		os.addChildren(nodeName);
		os.addChildren(osName);
		os.addChildren(osRelease);
		os.addChildren(osVersion);
		os.addChildren(characterSet);

		// Hinemosエージェントツリー
		agent.removeChildren();
		agent.addChildren(agentAwakePort);

		// ジョブツリー
		job.removeChildren();
		job.addChildren(jobPriority);
		job.addChildren(jobMultiplicity);

		// ---- サービス情報ツリー
		service.removeChildren();
		service.addChildren(snmp);
		service.addChildren(wbem);
		service.addChildren(ipmi);
		service.addChildren(winrm);
		service.addChildren(ssh);

		// SNMPツリー
		snmp.removeChildren();
		snmp.addChildren(snmpUser);
		snmp.addChildren(snmpPort);
		snmp.addChildren(snmpCommunity);
		snmp.addChildren(snmpVersion);
		snmp.addChildren(snmpSecurityLevel);
		snmp.addChildren(snmpAuthPassword);
		snmp.addChildren(snmpPrivPassword);
		snmp.addChildren(snmpAuthProtocol);
		snmp.addChildren(snmpPrivProtocol);
		snmp.addChildren(snmpTimeout);
		snmp.addChildren(snmpRetries);

		// WBEMツリー
		wbem.removeChildren();
		wbem.addChildren(wbemUser);
		wbem.addChildren(wbemUserPassword);
		wbem.addChildren(wbemPort);
		wbem.addChildren(wbemProtocol);
		wbem.addChildren(wbemTimeout);
		wbem.addChildren(wbemRetries);

		// IPMIツリー
		ipmi.removeChildren();
		ipmi.addChildren(ipmiIpAddress);
		ipmi.addChildren(ipmiPort);
		ipmi.addChildren(ipmiUser);
		ipmi.addChildren(ipmiUserPassword);
		ipmi.addChildren(ipmiTimeout);
		ipmi.addChildren(ipmiRetries);
		ipmi.addChildren(ipmiProtocol);
		ipmi.addChildren(ipmiLevel);

		// WinRMツリー
		winrm.removeChildren();
		winrm.addChildren(winrmUser);
		winrm.addChildren(winrmUserPassword);
		winrm.addChildren(winrmVersion);
		winrm.addChildren(winrmPort);
		winrm.addChildren(winrmProtocol);
		winrm.addChildren(winrmTimeout);
		winrm.addChildren(winrmRetries);

		// SSHツリー
		ssh.removeChildren();
		ssh.addChildren(sshUser);
		ssh.addChildren(sshUserPassword);
		ssh.addChildren(sshPrivateKeyFilepath);
		ssh.addChildren(sshPrivateKeyPassphrase);
		ssh.addChildren(sshPort);
		ssh.addChildren(sshTimeout);

		// ---- デバイス情報ツリー
		device.removeChildren();
		device.addChildren(cpuList);
		device.addChildren(memoryList);
		device.addChildren(networkInterfaceList);
		device.addChildren(diskList);
		device.addChildren(fileSystemList);
		device.addChildren(generalDeviceList);

		// 汎用デバイスツリー
		generalDeviceList.removeChildren();
		generalDeviceList.addChildren(generalDevice);

		generalDevice.removeChildren();
		generalDevice.addChildren(deviceDisplayName);
		generalDevice.addChildren(deviceName);
		generalDevice.addChildren(deviceIndex);
		generalDevice.addChildren(deviceType);
		generalDevice.addChildren(deviceSize);
		generalDevice.addChildren(deviceSizeUnit);
		generalDevice.addChildren(deviceDescription);

		// CPUツリー
		cpuList.removeChildren();
		cpuList.addChildren(cpu);

		cpu.removeChildren();
		cpu.addChildren(cpuDeviceDisplayName);
		cpu.addChildren(cpuDeviceName);
		cpu.addChildren(cpuDeviceIndex);
		cpu.addChildren(cpuDeviceType);
		cpu.addChildren(cpuDeviceSize);
		cpu.addChildren(cpuDeviceSizeUnit);
		cpu.addChildren(cpuDeviceDescription);

		// MEMツリー
		memoryList.removeChildren();
		memoryList.addChildren(memory);

		memory.removeChildren();
		memory.addChildren(memoryDeviceDisplayName);
		memory.addChildren(memoryDeviceName);
		memory.addChildren(memoryDeviceIndex);
		memory.addChildren(memoryDeviceType);
		memory.addChildren(memoryDeviceSize);
		memory.addChildren(memoryDeviceSizeUnit);
		memory.addChildren(memoryDeviceDescription);

		// NICツリー
		networkInterfaceList.removeChildren();
		networkInterfaceList.addChildren(networkInterface);

		networkInterface.removeChildren();
		networkInterface.addChildren(networkInterfaceDeviceDisplayName);
		networkInterface.addChildren(networkInterfaceDeviceName);
		networkInterface.addChildren(networkInterfaceDeviceIndex);
		networkInterface.addChildren(networkInterfaceDeviceType);
		networkInterface.addChildren(networkInterfaceDeviceSize);
		networkInterface.addChildren(networkInterfaceDeviceSizeUnit);
		networkInterface.addChildren(networkInterfaceDeviceDescription);
		networkInterface.addChildren(nicIpAddress);
		networkInterface.addChildren(nicMacAddress);

		// DISKツリー
		diskList.removeChildren();
		diskList.addChildren(disk);

		disk.removeChildren();
		disk.addChildren(diskDeviceDisplayName);
		disk.addChildren(diskDeviceName);
		disk.addChildren(diskDeviceIndex);
		disk.addChildren(diskDeviceType);
		disk.addChildren(diskDeviceSize);
		disk.addChildren(diskDeviceSizeUnit);
		disk.addChildren(diskDeviceDescription);
		disk.addChildren(diskRpm);

		// ファイルシステムツリー
		fileSystemList.removeChildren();
		fileSystemList.addChildren(fileSystem);

		fileSystem.removeChildren();
		fileSystem.addChildren(fileSystemDeviceDisplayName);
		fileSystem.addChildren(fileSystemDeviceName);
		fileSystem.addChildren(fileSystemDeviceIndex);
		fileSystem.addChildren(fileSystemDeviceType);
		fileSystem.addChildren(fileSystemDeviceSize);
		fileSystem.addChildren(fileSystemDeviceSizeUnit);
		fileSystem.addChildren(fileSystemDeviceDescription);
		fileSystem.addChildren(fileSystemType);

		// ---- クラウド・仮想化管理情報ツリー
		cloudManagement.removeChildren();
		cloudManagement.addChildren(cloudService);
		cloudManagement.addChildren(cloudScope);
		cloudManagement.addChildren(cloudResourceType);
		cloudManagement.addChildren(cloudResourceId);
		cloudManagement.addChildren(cloudResourceName);
		cloudManagement.addChildren(cloudLocation);

		// ---- ノード変数情報ツリー
		nodeVariable.removeChildren();
		nodeVariable.addChildren(generalNodeVariable);
		generalNodeVariable.addChildren(nodeVariableName);
		generalNodeVariable.addChildren(nodeVariableValues);

		// ---- 保守情報ツリー
		maintenance.removeChildren();
		maintenance.addChildren(administrator);
		maintenance.addChildren(contact);

		return property;
	}

	/**
	 * TODO HinemosManagerのNodeInfoの値と揃えておくこと。!
	 * @param nodeInfo
	 */
	public static void setDefaultNode(NodeInfo nodeInfo) {
		nodeInfo.setFacilityType(FacilityConstant.TYPE_NODE);
		nodeInfo.setDisplaySortOrder(100);
		if (nodeInfo.getFacilityId() == null) {
			nodeInfo.setFacilityId("");
		}
		if (nodeInfo.getFacilityName() == null) {
			nodeInfo.setFacilityName("");
		}
		if (nodeInfo.getDescription() == null) {
			nodeInfo.setDescription("");
		}
		if (nodeInfo.isValid() == null) {
			nodeInfo.setValid(Boolean.TRUE);
		}
		if (nodeInfo.isAutoDeviceSearch() == null) {
			nodeInfo.setAutoDeviceSearch(Boolean.TRUE);
		}
		if (nodeInfo.getCreateUserId() == null) {
			nodeInfo.setCreateUserId("");
		}
		if (nodeInfo.getCreateDatetime() == null) {
			nodeInfo.setCreateDatetime(null);
		}
		if (nodeInfo.getModifyUserId() == null) {
			nodeInfo.setModifyUserId("");
		}
		if (nodeInfo.getModifyDatetime() == null) {
			nodeInfo.setModifyDatetime(null);
		}

		// HW
		if (nodeInfo.getPlatformFamily() == null) {
			nodeInfo.setPlatformFamily("");
		}
		if (nodeInfo.getSubPlatformFamily() == null) {
			nodeInfo.setSubPlatformFamily("");
		}
		if (nodeInfo.getHardwareType() == null) {
			nodeInfo.setHardwareType("");
		}
		if (nodeInfo.getIconImage() == null) {
			nodeInfo.setIconImage("");
		}

		// IPアドレス
		if (nodeInfo.getIpAddressVersion() == null) {
			nodeInfo.setIpAddressVersion(4);
		}
		if (nodeInfo.getIpAddressV4() == null) {
			nodeInfo.setIpAddressV4("");
		}
		if (nodeInfo.getIpAddressV6() == null) {
			nodeInfo.setIpAddressV6("");
		}

		// OS
		if (nodeInfo.getNodeName() == null) {
			nodeInfo.setNodeName("");
		}
		if (nodeInfo.getOsName() == null) {
			nodeInfo.setOsName("");
		}
		if (nodeInfo.getOsRelease() == null) {
			nodeInfo.setOsRelease("");
		}
		if (nodeInfo.getOsVersion() == null) {
			nodeInfo.setOsVersion("");
		}
		if (nodeInfo.getCharacterSet() == null) {
			nodeInfo.setCharacterSet("");
		}

		// Hinemosエージェント
		if (nodeInfo.getAgentAwakePort() == null || nodeInfo.getAgentAwakePort() == -1) {
			nodeInfo.setAgentAwakePort(24005);
		}

		// JOB
		if (nodeInfo.getJobPriority() == null) {
			nodeInfo.setJobPriority(16);
		}
		if (nodeInfo.getJobMultiplicity() == null) {
			nodeInfo.setJobMultiplicity(0);
		}

		// SNMP
		if (nodeInfo.getSnmpUser() == null || "".equals(nodeInfo.getSnmpUser())) {
			nodeInfo.setSnmpUser("root");
		}
		if (nodeInfo.getSnmpAuthPassword() == null) {
			nodeInfo.setSnmpAuthPassword("");
		}
		if (nodeInfo.getSnmpPrivPassword() == null) {
			nodeInfo.setSnmpPrivPassword("");
		}
		if (nodeInfo.getSnmpPort() == null) {
			nodeInfo.setSnmpPort(161);
		}
		if (nodeInfo.getSnmpCommunity() == null) {
			nodeInfo.setSnmpCommunity("public");
		}
		if (nodeInfo.getSnmpVersion() == null) {
			nodeInfo.setSnmpVersion(SnmpVersionConstant.TYPE_V2);
		}
		if (nodeInfo.getSnmpSecurityLevel() == null) {
			nodeInfo.setSnmpSecurityLevel(SnmpSecurityLevelConstant.NOAUTH_NOPRIV);
		}
		if (nodeInfo.getSnmpAuthProtocol() == null) {
			nodeInfo.setSnmpAuthProtocol("");
		}
		if (nodeInfo.getSnmpPrivProtocol() == null) {
			nodeInfo.setSnmpPrivProtocol("");
		}
		if (nodeInfo.getSnmpTimeout() == null || nodeInfo.getSnmpTimeout() == -1) {
			nodeInfo.setSnmpTimeout(5000);
		}
		if (nodeInfo.getSnmpRetryCount() == null || nodeInfo.getSnmpRetryCount() == -1) {
			nodeInfo.setSnmpRetryCount(3);
		}

		// WBEM
		if (nodeInfo.getWbemUser() == null || "".equals(nodeInfo.getWbemUser())) {
			nodeInfo.setWbemUser("root");
		}
		if (nodeInfo.getWbemUserPassword() == null) {
			nodeInfo.setWbemUserPassword("");
		}
		if (nodeInfo.getWbemPort() == null || nodeInfo.getWbemPort() == -1) {
			nodeInfo.setWbemPort(5988);
		}
		if (nodeInfo.getWbemProtocol() == null || "".equals(nodeInfo.getWbemProtocol())) {
			nodeInfo.setWbemProtocol("http");
		}
		if (nodeInfo.getWbemTimeout() == null || nodeInfo.getWbemTimeout() == -1) {
			nodeInfo.setWbemTimeout(5000);
		}
		if (nodeInfo.getWbemRetryCount() == null || nodeInfo.getWbemRetryCount() == -1) {
			nodeInfo.setWbemRetryCount(3);
		}

		// IPMI
		if (nodeInfo.getIpmiIpAddress() == null) {
			nodeInfo.setIpmiIpAddress("");
		}
		if (nodeInfo.getIpmiPort() == null || nodeInfo.getIpmiPort() == -1) {
			nodeInfo.setIpmiPort(0);
		}
		if (nodeInfo.getIpmiUser() == null) {
			nodeInfo.setIpmiUser("");
		}
		if (nodeInfo.getIpmiUserPassword() == null) {
			nodeInfo.setIpmiUserPassword("");
		}
		if (nodeInfo.getIpmiTimeout() == null || nodeInfo.getIpmiTimeout() == -1) {
			nodeInfo.setIpmiTimeout(5000);
		}
		if (nodeInfo.getIpmiRetries() == null || nodeInfo.getIpmiRetries() == -1) {
			nodeInfo.setIpmiRetries(3);
		}
		if (nodeInfo.getIpmiProtocol() == null || "".equals(nodeInfo.getIpmiProtocol())) {
			nodeInfo.setIpmiProtocol("RMCP+");
		}
		if (nodeInfo.getIpmiLevel() == null) {
			nodeInfo.setIpmiLevel("");
		}

		// WinRM
		if (nodeInfo.getWinrmUser() == null) {
			nodeInfo.setWinrmUser("");
		}
		if (nodeInfo.getWinrmUserPassword() == null) {
			nodeInfo.setWinrmUserPassword("");
		}
		if (nodeInfo.getWinrmVersion() == null || "".equals(nodeInfo.getWinrmVersion())) {
			nodeInfo.setWinrmVersion("2.0");
		}
		if (nodeInfo.getWinrmPort() == null || nodeInfo.getWinrmPort() == -1) {
			nodeInfo.setWinrmPort(5985);
		}
		if (nodeInfo.getWinrmProtocol() == null || "".equals(nodeInfo.getWinrmProtocol())) {
			nodeInfo.setWinrmProtocol("http");
		}
		if (nodeInfo.getWinrmTimeout() == null || nodeInfo.getWinrmTimeout() == -1) {
			nodeInfo.setWinrmTimeout(5000);
		}
		if (nodeInfo.getWinrmRetries() == null || nodeInfo.getWinrmRetries() == -1) {
			nodeInfo.setWinrmRetries(3);
		}

		// SSH
		if (nodeInfo.getSshUser() == null || "".equals(nodeInfo.getSshUser())) {
			nodeInfo.setSshUser("root");
		}
		if (nodeInfo.getSshUserPassword() == null) {
			nodeInfo.setSshUserPassword("");
		}
		if (nodeInfo.getSshPrivateKeyFilepath() == null) {
			nodeInfo.setSshPrivateKeyFilepath("");
		}
		if (nodeInfo.getSshPrivateKeyPassphrase() == null) {
			nodeInfo.setSshPrivateKeyPassphrase("");
		}
		if (nodeInfo.getSshPort() == null) {
			nodeInfo.setSshPort(22);
		}
		if (nodeInfo.getSshTimeout() == null) {
			nodeInfo.setSshTimeout(50000);
		}

		// クラウド管理
		if (nodeInfo.getCloudService() == null) {
			nodeInfo.setCloudService("");
		}
		if (nodeInfo.getCloudScope() == null) {
			nodeInfo.setCloudScope("");
		}
		if (nodeInfo.getCloudResourceType() == null) {
			nodeInfo.setCloudResourceType("");
		}
		if (nodeInfo.getCloudResourceId() == null) {
			nodeInfo.setCloudResourceId("");
		}
		if (nodeInfo.getCloudResourceName() == null) {
			nodeInfo.setCloudResourceName("");
		}
		if (nodeInfo.getCloudLocation() == null) {
			nodeInfo.setCloudLocation("");
		}

		// 保守
		if (nodeInfo.getAdministrator() == null) {
			nodeInfo.setAdministrator("");
		}
		if (nodeInfo.getContact() == null) {
			nodeInfo.setContact("");
		}
	}

	/**
	 * プラットフォーム情報（プラットフォームIDおよびプラットフォーム名）を返す。<BR>
	 * 戻り値は、下記の構成の2次元配列（Object[][]）である。<BR>
	 * <PRE>
	 * {
	 *    {platformId1, platformId2, ...},
	 *    {platformName1, platformName2, ...}
	 * }
	 * </PRE>
	 *
	 * @return プラットフォーム情報の2次元配列
	 */
	private static Object[][] getPlatformNames(String managerName) {
		// キャッシュが存在する場合はキャッシュを返す
		if(getInstance().platformCache != null){
			return getInstance().platformCache;
		}
		/** ローカル変数 */
		Object[][] table = null;
		//Collection platforms = null;
		List<RepositoryTableInfo> platforms = null;
		ArrayList<String> platformIdList = null;
		ArrayList<String> platformNameList = null;

		/** メイン処理 */
		try {
			platformIdList = new ArrayList<String>();
			platformNameList = new ArrayList<String>();
			table = new Object[2][platformIdList.size()];
			if (managerName == null) {
				return table;
			}
			RepositoryEndpointWrapper wrapper = RepositoryEndpointWrapper.getWrapper(managerName);
			platforms = wrapper.getPlatformList();

			if (platforms != null) {
				for (RepositoryTableInfo platform : platforms) {
					platformIdList.add(platform.getId());
					platformNameList.add(platform.getName() + "(" + platform.getId() + ")");
				}
			}

			table[PropertyDefineConstant.SELECT_VALUE] = platformIdList.toArray();
			table[PropertyDefineConstant.SELECT_DISP_TEXT] = platformNameList.toArray();
		} catch (InvalidRole_Exception e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			m_log.warn("getPlatformNames(), " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}

		m_log.debug("getPlatformNames : cache created");
		getInstance().platformCache = table;
		return table;
	}

	/**
	 * サブプラットフォーム情報（サブプラットフォームIDおよびサブプラットフォーム名）を返す。<BR>
	 * 戻り値は、下記の構成の2次元配列（Object[][]）である。<BR>
	 * <PRE>
	 * {
	 *    {subPlatformId1, subPlatformId2, ...},
	 *    {subPlatformName1(subPlatformName), subPlatformName2(subPlatformId2), ...}
	 * }
	 * </PRE>
	 *
	 * @return サブプラットフォーム情報の2次元配列
	 */
	//TODO サブプラットフォームのテーブルより一覧を参照するように修正する
	private static Object[][] getSubPlatformNames(String managerName) {
		// キャッシュが存在する場合はキャッシュを返す
		if(getInstance().subPlatformCache != null){
			return getInstance().subPlatformCache;
		}
		/** ローカル変数 */
		Object[][] table = null;
		List<RepositoryTableInfo> subPlatforms = null;
		ArrayList<String> subPlatformIdList = new ArrayList<String>();
		ArrayList<String> subPlatformNameList = new ArrayList<String>();

		/** メイン処理 */
		try {
			table = new Object[2][subPlatformIdList.size()];
			if (managerName == null) {
				return table;
			}
			
			RepositoryEndpointWrapper wrapper = RepositoryEndpointWrapper.getWrapper(managerName);
			subPlatforms = wrapper.getCollectorSubPlatformTableInfoList();

			subPlatformIdList.add("");
			subPlatformNameList.add("");
			if (subPlatforms != null) {
				for (RepositoryTableInfo subPlatform : subPlatforms) {
					subPlatformIdList.add(subPlatform.getId());
					subPlatformNameList.add(subPlatform.getName() + "(" + subPlatform.getId() + ")");
				}
			}

			table[PropertyDefineConstant.SELECT_VALUE] = subPlatformIdList.toArray();
			table[PropertyDefineConstant.SELECT_DISP_TEXT] = subPlatformNameList.toArray();
		} catch (InvalidRole_Exception e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			m_log.warn("getSubPlatformNames(), " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}

		m_log.debug("getSubPlatformNames : cache created");
		getInstance().subPlatformCache = table;
		return table;
	}


}
