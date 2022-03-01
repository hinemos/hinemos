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
import org.openapitools.client.model.NodeCpuInfoResponse;
import org.openapitools.client.model.NodeCustomInfoResponse;
import org.openapitools.client.model.NodeDiskInfoResponse;
import org.openapitools.client.model.NodeFilesystemInfoResponse;
import org.openapitools.client.model.NodeGeneralDeviceInfoResponse;
import org.openapitools.client.model.NodeHostnameInfoResponse;
import org.openapitools.client.model.NodeInfoResponse;
import org.openapitools.client.model.NodeLicenseInfoResponse;
import org.openapitools.client.model.NodeMemoryInfoResponse;
import org.openapitools.client.model.NodeNetstatInfoResponse;
import org.openapitools.client.model.NodeNetworkInterfaceInfoResponse;
import org.openapitools.client.model.NodeNoteInfoResponse;
import org.openapitools.client.model.NodeOsInfoResponse;
import org.openapitools.client.model.NodePackageInfoResponse;
import org.openapitools.client.model.NodeProcessInfoResponse;
import org.openapitools.client.model.NodeProductInfoResponse;
import org.openapitools.client.model.NodeVariableInfoResponse;
import org.openapitools.client.model.RepositoryTableInfoResponse;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.bean.Property;
import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.SnmpProtocolConstant;
import com.clustercontrol.bean.SnmpSecurityLevelConstant;
import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.bean.WbemProtocolConstant;
import com.clustercontrol.bean.WinrmProtocolConstant;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.repository.bean.DeviceTypeConstant;
import com.clustercontrol.repository.bean.NodeConstant;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.util.TimezoneUtil;

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
		ArrayList<Property> object10 = null;

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
			object8 = PropertyUtil.getProperty(deviceProperty, NodeConstant.CPU_CORE_COUNT);
			object9 = PropertyUtil.getProperty(deviceProperty, NodeConstant.CPU_THREAD_COUNT);
			object10 = PropertyUtil.getProperty(deviceProperty, NodeConstant.CPU_CLOCK_COUNT);

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
					((Property)object10.get(i)).setModify(modify);
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

		// ----- ネットワーク接続関連 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.NETSTAT);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getProperty(deviceProperty, NodeConstant.NETSTAT_PROTOCOL);
			object2 = PropertyUtil.getProperty(deviceProperty, NodeConstant.NETSTAT_LOCAL_IP_ADDRESS);
			object3 = PropertyUtil.getProperty(deviceProperty, NodeConstant.NETSTAT_LOCAL_PORT);
			object4 = PropertyUtil.getProperty(deviceProperty, NodeConstant.NETSTAT_FOREIGN_IP_ADDRESS);
			object5 = PropertyUtil.getProperty(deviceProperty, NodeConstant.NETSTAT_FOREIGN_PORT);
			object6 = PropertyUtil.getProperty(deviceProperty, NodeConstant.NETSTAT_PROCESS_NAME);
			object7 = PropertyUtil.getProperty(deviceProperty, NodeConstant.NETSTAT_PID);
			object8 = PropertyUtil.getProperty(deviceProperty, NodeConstant.NETSTAT_STATUS);

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

		// ----- プロセス関連 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.PROCESS);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getProperty(deviceProperty, NodeConstant.PROCESS_NAME);
			object2 = PropertyUtil.getProperty(deviceProperty, NodeConstant.PROCESS_PID);
			object3 = PropertyUtil.getProperty(deviceProperty, NodeConstant.PROCESS_PATH);
			object4 = PropertyUtil.getProperty(deviceProperty, NodeConstant.PROCESS_EXEC_USER);
			object5 = PropertyUtil.getProperty(deviceProperty, NodeConstant.PROCESS_STARTUP_DATE_TIME);

			if(object1 != null){
				for (int i = 0; i < object1.size(); i++){
					((Property)object1.get(i)).setModify(modify);
					((Property)object2.get(i)).setModify(modify);
					((Property)object3.get(i)).setModify(modify);
					((Property)object4.get(i)).setModify(modify);
					((Property)object5.get(i)).setModify(modify);
				}
			}
		}

		// ----- パッケージ関連 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.PACKAGE);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getProperty(deviceProperty, NodeConstant.PACKAGE_ID);
			object2 = PropertyUtil.getProperty(deviceProperty, NodeConstant.PACKAGE_NAME);
			object3 = PropertyUtil.getProperty(deviceProperty, NodeConstant.PACKAGE_VERSION);
			object4 = PropertyUtil.getProperty(deviceProperty, NodeConstant.PACKAGE_RELEASE);
			object5 = PropertyUtil.getProperty(deviceProperty, NodeConstant.PACKAGE_INSTALL_DATE);
			object6 = PropertyUtil.getProperty(deviceProperty, NodeConstant.PACKAGE_VENDOR);
			object7 = PropertyUtil.getProperty(deviceProperty, NodeConstant.PACKAGE_ARCHITECTURE);

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

		// ----- 個別導入製品関連 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.PRODUCT);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getProperty(deviceProperty, NodeConstant.PRODUCT_NAME);
			object2 = PropertyUtil.getProperty(deviceProperty, NodeConstant.PRODUCT_VERSION);
			object3 = PropertyUtil.getProperty(deviceProperty, NodeConstant.PRODUCT_PATH);

			if(object1 != null){
				for (int i = 0; i < object1.size(); i++){
					((Property)object1.get(i)).setModify(modify);
					((Property)object2.get(i)).setModify(modify);
					((Property)object3.get(i)).setModify(modify);
				}
			}
		}

		// ----- ライセンス関連 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.LICENSE);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getProperty(deviceProperty, NodeConstant.LICENSE_PRODUCT_NAME);
			object2 = PropertyUtil.getProperty(deviceProperty, NodeConstant.LICENSE_VENDOR);
			object3 = PropertyUtil.getProperty(deviceProperty, NodeConstant.LICENSE_VENDOR_CONTACT);
			object4 = PropertyUtil.getProperty(deviceProperty, NodeConstant.LICENSE_SERIAL_NUMBER);
			object5 = PropertyUtil.getProperty(deviceProperty, NodeConstant.LICENSE_COUNT);
			object6 = PropertyUtil.getProperty(deviceProperty, NodeConstant.LICENSE_EXPIRATION_DATE);
			if(object1 != null){
				for (int i = 0; i < object1.size(); i++){
					((Property)object1.get(i)).setModify(modify);
					((Property)object2.get(i)).setModify(modify);
					((Property)object3.get(i)).setModify(modify);
					((Property)object4.get(i)).setModify(modify);
					((Property)object5.get(i)).setModify(modify);
					((Property)object6.get(i)).setModify(modify);
				}
			}
		}
		
		// ----- ユーザ任意情報 -----
		// 常時編集不可.

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
	public static NodeInfoResponse property2node (Property property) {
		NodeInfoResponse nodeInfo = new NodeInfoResponse();

		ArrayList<?> object1 = null;
		ArrayList<?> object2 = null;
		ArrayList<?> object3 = null;
		ArrayList<?> object4 = null;
		ArrayList<?> object5 = null;
		ArrayList<?> object6 = null;
		ArrayList<?> object7 = null;
		ArrayList<?> object8 = null;
		ArrayList<?> object9 = null;
		ArrayList<?> object10 = null;

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
			nodeInfo.setCreateDatetime(TimezoneUtil.getSimpleDateFormat().format((Date) object1.get(0)));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.MODIFIER_NAME);
		if (object1.size() > 0) {
			nodeInfo.setModifyUserId((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.MODIFY_TIME);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setModifyDatetime(TimezoneUtil.getSimpleDateFormat().format((Date)object1.get(0)));
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
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.NODE_NAME);
		if (object1.size() > 0) {
			nodeInfo.setNodeName((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.ICONIMAGE);
		if (object1.size() > 0) {
			nodeInfo.setIconImage((String)object1.get(0));
		}


		// ----- IPアドレス関連 -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.IP_ADDRESS_VERSION);
		if (object1.size() > 0 && object1.get(0) != null && object1.get(0).toString().equals("6")) {
			nodeInfo.setIpAddressVersion(NodeInfoResponse.IpAddressVersionEnum.IPV6);
		} else {
			nodeInfo.setIpAddressVersion(NodeInfoResponse.IpAddressVersionEnum.IPV4);
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.IP_ADDRESS_V4);
		if (object1.size() > 0) {
			nodeInfo.setIpAddressV4((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.IP_ADDRESS_V6);
		if (object1.size() > 0) {
			nodeInfo.setIpAddressV6((String)object1.get(0));
		}

		// ----- Hinemosエージェント関連  -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.AGENT_AWAKE_PORT);
		if (object1.size() > 0  && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setAgentAwakePort((Integer)object1.get(0));
		}

		// ----- 構成情報関連 -----
		// ----- ホスト名 -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.HOST_NAME);
		ArrayList<NodeHostnameInfoResponse> nodeHostnameInfo = new ArrayList<NodeHostnameInfoResponse>();
		for (Object o : object1) {
			NodeHostnameInfoResponse item = new NodeHostnameInfoResponse();
			item.setHostname((String)o);
			nodeHostnameInfo.add(item);
		}
		List<NodeHostnameInfoResponse> nodeHostnameInfo_orig = nodeInfo.getNodeHostnameInfo();
		nodeHostnameInfo_orig.clear();
		nodeHostnameInfo_orig.addAll(nodeHostnameInfo);
		// ----- OS関連 -----
		if (nodeInfo.getNodeOsInfo() == null) {
			nodeInfo.setNodeOsInfo(new NodeOsInfoResponse());
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.OS_NAME);
		if (object1.size() > 0) {
			nodeInfo.getNodeOsInfo().setOsName((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.OS_RELEASE);
		if (object1.size() > 0) {
			nodeInfo.getNodeOsInfo().setOsRelease((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.OS_VERSION);
		if (object1.size() > 0) {
			nodeInfo.getNodeOsInfo().setOsVersion((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.CHARACTER_SET);
		if (object1.size() > 0) {
			nodeInfo.getNodeOsInfo().setCharacterSet((String)object1.get(0));
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.OS_STARTUP_DATE_TIME);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.getNodeOsInfo().setStartupDateTime(TimezoneUtil.getSimpleDateFormat().format((Date)object1.get(0)));
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

			ArrayList<NodeGeneralDeviceInfoResponse> nodeDeviceInfo = new ArrayList<NodeGeneralDeviceInfoResponse>();
			for (int i = 0; i < object1.size(); i++) {
				NodeGeneralDeviceInfoResponse item = new NodeGeneralDeviceInfoResponse();
				item.setDeviceType((String)object1.get(i));
				if(object2.get(i) != null && !"".equals(object2.get(i)))
					item.setDeviceIndex((Integer)object2.get(i));
				item.setDeviceName((String)object3.get(i));
				item.setDeviceDisplayName((String)object4.get(i));
				if(object5.get(i) != null && !"".equals(object5.get(i)))
					item.setDeviceSize((Long)object5.get(i));
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
			List<NodeGeneralDeviceInfoResponse> nodeDeviceInfo_orig = nodeInfo.getNodeDeviceInfo();
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
			object8 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.CPU_CORE_COUNT);
			object9 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.CPU_THREAD_COUNT);
			object10 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.CPU_CLOCK_COUNT);

			ArrayList<NodeCpuInfoResponse> nodeCpuInfo = new ArrayList<NodeCpuInfoResponse>();

			for (int i = 0; i < object1.size(); i++) {
				NodeCpuInfoResponse item = new NodeCpuInfoResponse();
				item.setDeviceType((String)object1.get(i));
				if(object2.get(i) != null && !"".equals(object2.get(i)))
					item.setDeviceIndex((Integer)object2.get(i));
				item.setDeviceName((String)object3.get(i));
				item.setDeviceDisplayName((String)object4.get(i));
				if(object5.get(i) != null && !"".equals(object5.get(i)))
					item.setDeviceSize((Long)object5.get(i));
				item.setDeviceSizeUnit((String)object6.get(i));
				item.setDeviceDescription((String)object7.get(i));
				if (object8.get(i) != null && !"".equals(object8.get(i)))
					item.setCoreCount((Integer) object8.get(i));
				if (object9.get(i) != null && !"".equals(object9.get(i)))
					item.setThreadCount((Integer) object9.get(i));
				if (object10.get(i) != null && !"".equals(object10.get(i)))
					item.setClockCount((Integer) object10.get(i));
				
				if((item.getDeviceIndex() == null || -1 == item.getDeviceIndex())
						&& (item.getDeviceName() == null || "".equals(item.getDeviceName()))
						&& (item.getDeviceDisplayName()== null || "".equals(item.getDeviceDisplayName()))
						){
					m_log.debug("CPU Device is null");
				}else{
					nodeCpuInfo.add(item);
				}
			}
			List<NodeCpuInfoResponse> nodeCpuInfo_orig = nodeInfo.getNodeCpuInfo();
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

			ArrayList<NodeMemoryInfoResponse> nodeMemoryInfo = new ArrayList<NodeMemoryInfoResponse>();
			for (int i = 0; i < object1.size(); i++) {
				NodeMemoryInfoResponse item = new NodeMemoryInfoResponse();
				item.setDeviceType((String)object1.get(i));
				if(object2.get(i) != null && !"".equals(object2.get(i)))
					item.setDeviceIndex((Integer)object2.get(i));
				item.setDeviceName((String)object3.get(i));
				item.setDeviceDisplayName((String)object4.get(i));
				if(object5.get(i) != null && !"".equals(object5.get(i)))
					item.setDeviceSize((Long)object5.get(i));
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
			List<NodeMemoryInfoResponse> nodeMemoryInfo_orig = nodeInfo.getNodeMemoryInfo();
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

			ArrayList<NodeNetworkInterfaceInfoResponse> nodeNetworkInterfaceInfo = new ArrayList<NodeNetworkInterfaceInfoResponse>();
			for (int i = 0; i < object1.size(); i++) {
				NodeNetworkInterfaceInfoResponse item = new NodeNetworkInterfaceInfoResponse();
				item.setDeviceType((String)object1.get(i));
				if(object2.get(i) != null && !"".equals(object2.get(i)))
					item.setDeviceIndex((Integer)object2.get(i));
				item.setDeviceName((String)object3.get(i));
				item.setDeviceDisplayName((String)object4.get(i));
				if(object5.get(i) != null && !"".equals(object5.get(i)))
					item.setDeviceSize((Long)object5.get(i));
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
			List<NodeNetworkInterfaceInfoResponse> nodeNetworkInterfaceInfo_orig
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

			ArrayList<NodeDiskInfoResponse> nodeDiskInfo = new ArrayList<NodeDiskInfoResponse>();
			for (int i = 0; i < object1.size(); i++) {
				NodeDiskInfoResponse item = new NodeDiskInfoResponse();
				item.setDeviceType((String)object1.get(i));
				if(object2.get(i) != null && !"".equals(object2.get(i)))
					item.setDeviceIndex((Integer)object2.get(i));
				item.setDeviceName((String)object3.get(i));
				item.setDeviceDisplayName((String)object4.get(i));
				if(object5.get(i) != null && !"".equals(object5.get(i)))
					item.setDeviceSize((Long)object5.get(i));
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
			List<NodeDiskInfoResponse> nodeDiskInfo_orig = nodeInfo.getNodeDiskInfo();
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

			ArrayList<NodeFilesystemInfoResponse> nodeFilesystemInfo = new ArrayList<NodeFilesystemInfoResponse>();
			for (int i = 0; i < object1.size(); i++) {
				NodeFilesystemInfoResponse item = new NodeFilesystemInfoResponse();
				item.setDeviceType((String)object1.get(i));
				if(object2.get(i) != null && !"".equals(object2.get(i)))
					item.setDeviceIndex((Integer)object2.get(i));
				item.setDeviceName((String)object3.get(i));
				item.setDeviceDisplayName((String)object4.get(i));
				if(object5.get(i) != null && !"".equals(object5.get(i)))
					item.setDeviceSize((Long)object5.get(i));
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
			List<NodeFilesystemInfoResponse> nodeFilesystemInfo_orig
			= nodeInfo.getNodeFilesystemInfo();
			nodeFilesystemInfo_orig.clear();
			nodeFilesystemInfo_orig.addAll(nodeFilesystemInfo);
		}

		// ----- ネットワーク接続関連 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.NETSTAT_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.NETSTAT_PROTOCOL);
			object2 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.NETSTAT_LOCAL_IP_ADDRESS);
			object3 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.NETSTAT_LOCAL_PORT);
			object4 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.NETSTAT_FOREIGN_IP_ADDRESS);
			object5 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.NETSTAT_FOREIGN_PORT);
			object6 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.NETSTAT_PROCESS_NAME);
			object7 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.NETSTAT_PID);
			object8 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.NETSTAT_STATUS);

			ArrayList<NodeNetstatInfoResponse> nodeNetstatInfo = new ArrayList<>();
			for (int i = 0; i < object1.size(); i++) {
				NodeNetstatInfoResponse item = new NodeNetstatInfoResponse();
				item.setProtocol((String)object1.get(i));
				item.setLocalIpAddress((String)object2.get(i));
				item.setLocalPort((String)object3.get(i));
				item.setForeignIpAddress((String)object4.get(i));
				item.setForeignPort((String)object5.get(i));
				item.setProcessName((String)object6.get(i));
				if(object7.get(i) != null && !"".equals(object7.get(i))) {
					item.setPid((Integer)object7.get(i));
				}
				item.setStatus((String)object8.get(i));
				if((item.getProtocol() == null || "".equals(item.getProtocol())
						&& (item.getLocalIpAddress() == null || "".equals(item.getLocalIpAddress()))
						&& (item.getLocalPort() == null || "".equals(item.getLocalPort())))){
					m_log.debug("Netstat is null");
				}else{
					nodeNetstatInfo.add(item);
				}
			}
			List<NodeNetstatInfoResponse> nodeNetstatInfo_orig = nodeInfo.getNodeNetstatInfo();
			nodeNetstatInfo_orig.clear();
			nodeNetstatInfo_orig.addAll(nodeNetstatInfo);
		}

		// ----- プロセス関連 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.PROCESS_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.PROCESS_NAME);
			object2 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.PROCESS_PID);
			object3 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.PROCESS_PATH);
			object4 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.PROCESS_EXEC_USER);
			object5 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.PROCESS_STARTUP_DATE_TIME);

			ArrayList<NodeProcessInfoResponse> nodeProcessInfo = new ArrayList<>();
			for (int i = 0; i < object1.size(); i++) {
				NodeProcessInfoResponse item = new NodeProcessInfoResponse();
				item.setProcessName((String)object1.get(i));
				if(object2.get(i) != null && !"".equals(object2.get(i))) {
					item.setPid((Integer)object2.get(i));
				}
				item.setPath((String)object3.get(i));
				item.setExecUser((String)object4.get(i));
				if(object5.get(i) != null && !"".equals(object5.get(i))) {
					item.setStartupDateTime(TimezoneUtil.getSimpleDateFormat().format((Date)object5.get(i)));
				}
				if((item.getProcessName() == null || "".equals(item.getProcessName())
						&& (item.getPid() == null || item.getPid() <= -1))){
					m_log.debug("Process is null");
				}else{
					nodeProcessInfo.add(item);
				}
			}
			List<NodeProcessInfoResponse> nodeProcessInfo_orig = nodeInfo.getNodeProcessInfo();
			nodeProcessInfo_orig.clear();
			nodeProcessInfo_orig.addAll(nodeProcessInfo);
		}

		// ----- パッケージ関連 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.PACKAGE_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.PACKAGE_ID);
			object2 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.PACKAGE_NAME);
			object3 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.PACKAGE_VERSION);
			object4 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.PACKAGE_RELEASE);
			object5 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.PACKAGE_INSTALL_DATE);
			object6 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.PACKAGE_VENDOR);
			object7 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.PACKAGE_ARCHITECTURE);

			ArrayList<NodePackageInfoResponse> nodePackageInfo = new ArrayList<>();
			for (int i = 0; i < object1.size(); i++) {
				NodePackageInfoResponse item = new NodePackageInfoResponse();
				item.setPackageId((String)object1.get(i));
				item.setPackageName((String)object2.get(i));
				item.setVersion((String)object3.get(i));
				item.setRelease((String)object4.get(i));
				if (object5.size() > 0 && object5.get(i) != null &&
						!object5.get(i).toString().equals("")) {
					item.setInstallDate(TimezoneUtil.getSimpleDateFormat().format((Date)object5.get(i)));
				}
				item.setVendor((String)object6.get(i));
				item.setArchitecture((String)object7.get(i));

				if(item.getPackageId() == null || "".equals(item.getPackageId())){
					m_log.debug("Package is null");
				}else{
					nodePackageInfo.add(item);
				}
			}
			List<NodePackageInfoResponse> nodePackageInfo_orig = nodeInfo.getNodePackageInfo();
			nodePackageInfo_orig.clear();
			nodePackageInfo_orig.addAll(nodePackageInfo);
		}

		// ----- 個別導入製品関連 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.PRODUCT_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.PRODUCT_NAME);
			object2 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.PRODUCT_VERSION);
			object3 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.PRODUCT_PATH);

			ArrayList<NodeProductInfoResponse> nodeProductInfo = new ArrayList<>();
			for (int i = 0; i < object1.size(); i++) {
				NodeProductInfoResponse item = new NodeProductInfoResponse();
				item.setProductName((String)object1.get(i));
				item.setVersion((String)object2.get(i));
				item.setPath((String)object3.get(i));

				if(item.getProductName() == null || "".equals(item.getProductName())){
					m_log.debug("Product is null");
				}else{
					nodeProductInfo.add(item);
				}
			}
			List<NodeProductInfoResponse> nodeProductInfo_orig = nodeInfo.getNodeProductInfo();
			nodeProductInfo_orig.clear();
			nodeProductInfo_orig.addAll(nodeProductInfo);
		}

		// ----- ライセンス関連 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.LICENSE_LIST);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.LICENSE_PRODUCT_NAME);
			object2 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.LICENSE_VENDOR);
			object3 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.LICENSE_VENDOR_CONTACT);
			object4 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.LICENSE_SERIAL_NUMBER);
			object5 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.LICENSE_COUNT);
			object6 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.LICENSE_EXPIRATION_DATE);

			ArrayList<NodeLicenseInfoResponse> nodeLicenseInfo = new ArrayList<>();
			for (int i = 0; i < object1.size(); i++) {
				NodeLicenseInfoResponse item = new NodeLicenseInfoResponse();
				item.setProductName((String)object1.get(i));
				item.setVendor((String)object2.get(i));
				item.setVendorContact((String)object3.get(i));
				item.setSerialNumber((String)object4.get(i));
				if(object5.get(i) != null && !"".equals(object5.get(i))) {
					item.setCount((Integer)object5.get(i));
				}
				if(object6.get(i) != null && !"".equals(object6.get(i))) {
					item.setExpirationDate(TimezoneUtil.getSimpleDateFormat().format((Date)object6.get(i)));
				}
				if(item.getProductName() == null || "".equals(item.getProductName())){
					m_log.debug("License is null");
				}else{
					nodeLicenseInfo.add(item);
				}

			}
			List<NodeLicenseInfoResponse> nodeLicenseInfo_orig = nodeInfo.getNodeLicenseInfo();
			nodeLicenseInfo_orig.clear();
			nodeLicenseInfo_orig.addAll(nodeLicenseInfo);
		}

		// ----- ノード変数関連 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.NODE_VARIABLE);
		if (propertyList != null && propertyList.size() != 0) {
			deviceProperty = propertyList.get(0);

			object1 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.NODE_VARIABLE_NAME);
			object2 = PropertyUtil.getPropertyValue(deviceProperty, NodeConstant.NODE_VARIABLE_VALUE);

			ArrayList<NodeVariableInfoResponse> nodeVariableInfo = new ArrayList<NodeVariableInfoResponse>();
			for (int i = 0; i < object1.size(); i++) {
				if ((object1.get(i) != null && ! object1.get(i).toString().equals(""))
						|| (object2.get(i) != null && ! object2.get(i).toString().equals(""))
						) {
					NodeVariableInfoResponse item = new NodeVariableInfoResponse();
					item.setNodeVariableName((String)object1.get(i));
					item.setNodeVariableValue((String)object2.get(i));
					nodeVariableInfo.add(item);
				}
			}
			List<NodeVariableInfoResponse> nodeVariableInfo_orig = nodeInfo.getNodeVariableInfo();
			nodeVariableInfo_orig.clear();
			nodeVariableInfo_orig.addAll(nodeVariableInfo);
		}

		// ----- ユーザ任意情報 -----
		// 編集不可.

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
		if (object1.size() > 0 && object1.get(0) != null) {
			switch ((String) object1.get(0)) {
			case SnmpVersionConstant.STRING_V1:
				nodeInfo.setSnmpVersion(NodeInfoResponse.SnmpVersionEnum.V1);
				break;
			case SnmpVersionConstant.STRING_V2:
				nodeInfo.setSnmpVersion(NodeInfoResponse.SnmpVersionEnum.V2);
				break;
			case SnmpVersionConstant.STRING_V3:
				nodeInfo.setSnmpVersion(NodeInfoResponse.SnmpVersionEnum.V3);
				break;
			}
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SNMP_SECURITY_LEVEL);
		if (object1.size() > 0 && object1.get(0) != null) {
			switch ((String) object1.get(0)) {
			case SnmpSecurityLevelConstant.NOAUTH_NOPRIV:
				nodeInfo.setSnmpSecurityLevel(NodeInfoResponse.SnmpSecurityLevelEnum.NOAUTH_NOPRIV);
				break;
			case SnmpSecurityLevelConstant.AUTH_NOPRIV:
				nodeInfo.setSnmpSecurityLevel(NodeInfoResponse.SnmpSecurityLevelEnum.AUTH_NOPRIV);
				break;
			case SnmpSecurityLevelConstant.AUTH_PRIV:
				nodeInfo.setSnmpSecurityLevel(NodeInfoResponse.SnmpSecurityLevelEnum.AUTH_PRIV);
				break;
			default:
				nodeInfo.setSnmpSecurityLevel(NodeInfoResponse.SnmpSecurityLevelEnum.NONE);
			}
		} else {
			nodeInfo.setSnmpSecurityLevel(NodeInfoResponse.SnmpSecurityLevelEnum.NONE);
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SNMP_AUTH_PROTOCOL);
		if (object1.size() > 0 && object1.get(0) != null) {
			switch ((String) object1.get(0)) {
			case SnmpProtocolConstant.MD5:
				nodeInfo.setSnmpAuthProtocol(NodeInfoResponse.SnmpAuthProtocolEnum.MD5);
				break;
			case SnmpProtocolConstant.SHA:
				nodeInfo.setSnmpAuthProtocol(NodeInfoResponse.SnmpAuthProtocolEnum.SHA);
				break;
			case SnmpProtocolConstant.SHA224:
				nodeInfo.setSnmpAuthProtocol(NodeInfoResponse.SnmpAuthProtocolEnum.SHA224);
				break;
			case SnmpProtocolConstant.SHA256:
				nodeInfo.setSnmpAuthProtocol(NodeInfoResponse.SnmpAuthProtocolEnum.SHA256);
				break;
			case SnmpProtocolConstant.SHA384:
				nodeInfo.setSnmpAuthProtocol(NodeInfoResponse.SnmpAuthProtocolEnum.SHA384);
				break;
			case SnmpProtocolConstant.SHA512:
				nodeInfo.setSnmpAuthProtocol(NodeInfoResponse.SnmpAuthProtocolEnum.SHA512);
				break;
			default:
				nodeInfo.setSnmpAuthProtocol(NodeInfoResponse.SnmpAuthProtocolEnum.NONE);
			}
		} else {
			nodeInfo.setSnmpAuthProtocol(NodeInfoResponse.SnmpAuthProtocolEnum.NONE);
		}
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.SNMP_PRIV_PROTOCOL);
		if (object1.size() > 0 && object1.get(0) != null) {
			switch ((String) object1.get(0)) {
			case SnmpProtocolConstant.AES:
				nodeInfo.setSnmpPrivProtocol(NodeInfoResponse.SnmpPrivProtocolEnum.AES);
				break;
			case SnmpProtocolConstant.DES:
				nodeInfo.setSnmpPrivProtocol(NodeInfoResponse.SnmpPrivProtocolEnum.DES);
				break;
			case SnmpProtocolConstant.AES192:
				nodeInfo.setSnmpPrivProtocol(NodeInfoResponse.SnmpPrivProtocolEnum.AES192);
				break;
			case SnmpProtocolConstant.AES256:
				nodeInfo.setSnmpPrivProtocol(NodeInfoResponse.SnmpPrivProtocolEnum.AES256);
				break;
			default:
				nodeInfo.setSnmpPrivProtocol(NodeInfoResponse.SnmpPrivProtocolEnum.NONE);
			}
		} else {
			nodeInfo.setSnmpPrivProtocol(NodeInfoResponse.SnmpPrivProtocolEnum.NONE);
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
		if (object1.size() > 0 && object1.get(0) != null) {
			switch ((String) object1.get(0)) {
			case WbemProtocolConstant.HTTP:
				nodeInfo.setWbemProtocol(NodeInfoResponse.WbemProtocolEnum.HTTP);
				break;
			case WbemProtocolConstant.HTTPS:
				nodeInfo.setWbemProtocol(NodeInfoResponse.WbemProtocolEnum.HTTPS);
				break;
			default:
				nodeInfo.setWbemProtocol(NodeInfoResponse.WbemProtocolEnum.NONE);
			}
		} else {
			nodeInfo.setWbemProtocol(NodeInfoResponse.WbemProtocolEnum.NONE);
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
		if (object1.size() > 0 && object1.get(0) != null) {
			switch ((String) object1.get(0)) {
			case WinrmProtocolConstant.HTTP:
				nodeInfo.setWinrmProtocol(NodeInfoResponse.WinrmProtocolEnum.HTTP);
				break;
			case WinrmProtocolConstant.HTTPS:
				nodeInfo.setWinrmProtocol(NodeInfoResponse.WinrmProtocolEnum.HTTPS);
				break;
			default:
				nodeInfo.setWinrmProtocol(NodeInfoResponse.WinrmProtocolEnum.NONE);
			}
		} else {
			nodeInfo.setWinrmProtocol(NodeInfoResponse.WinrmProtocolEnum.NONE);
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
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setSshPrivateKeyPassphrase((String)object1.get(0));
		} else {
			nodeInfo.setSshPrivateKeyPassphrase("");
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
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.CLOUDLOGPRIORITY);
		if (object1.size() > 0 && object1.get(0) != null && !object1.get(0).toString().equals("")) {
			nodeInfo.setCloudLogPriority((Integer) object1.get(0));
		}

		// ---- RPAツール情報 -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.RPA_LOG_DIRECTORY);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setRpaLogDir((String)object1.get(0));
		}
		
		// ---- RPA管理ツール情報 -----
		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.RPA_MANAGEMENT_TOOL_TYPE);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setRpaManagementToolType((String)object1.get(0));
		}

		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.RPA_RESOURCE_ID);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setRpaResourceId((String)object1.get(0));
		}

		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.RPA_USER);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setRpaUser((String)object1.get(0));
		}

		object1 = PropertyUtil.getPropertyValue(property, NodeConstant.RPA_EXECUTION_ENVIRONMENT_ID);
		if (object1.size() > 0 && object1.get(0) != null &&
				!object1.get(0).toString().equals("")) {
			nodeInfo.setRpaExecEnvId((String)object1.get(0));
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
		ArrayList<NodeNoteInfoResponse> nodeNoteInfo = new ArrayList<NodeNoteInfoResponse>();
		for (int i = 0; i < object1.size(); i++) {
			NodeNoteInfoResponse item = new NodeNoteInfoResponse();
			item.setNoteId(i);
			item.setNote((String) object1.get(i));
			nodeNoteInfo.add(item);
		}
		List<NodeNoteInfoResponse> nodeNoteInfo_orig = nodeInfo.getNodeNoteInfo();
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
	 * @param isNodeMap true:ノードマップ（構成情報検索）表示
	 * @return
	 */
	public static Property node2property(String managerName, NodeInfoResponse node, int mode, Locale locale, boolean isNodeMap) {
		/** ローカル変数 */
		Property property = null;
		ArrayList<Property> propertyList = null;
		Property childProperty = null;

		/** メイン処理 */
		property = getProperty(managerName, mode, locale, isNodeMap);
		if (node == null) {
			return property;
		}

		if (!isNodeMap) {
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
			((Property)propertyList.get(0)).setValue(node.getValid());
			// 自動デバイスサーチ
			propertyList = PropertyUtil.getProperty(property, NodeConstant.AUTO_DEVICE_SEARCH);
			((Property)propertyList.get(0)).setValue(node.getAutoDeviceSearch());
			// 登録ユーザID
			propertyList = PropertyUtil.getProperty(property, NodeConstant.CREATOR_NAME);
			((Property)propertyList.get(0)).setValue(node.getCreateUserId());
			// 登録日時
			Long createDatetime = null;
			try {
				createDatetime = TimezoneUtil.getSimpleDateFormat().parse(node.getCreateDatetime()).getTime();
			} catch (Exception e) {
				//findbugs対応 エラーは発生しない想定なので本来不要だが Exception無視と思われないようtraceログの出力を追加
				m_log.trace("node2property : exception occuered",e);
			}
			if (createDatetime != null && createDatetime != 0L) {
				propertyList = PropertyUtil.getProperty(property, NodeConstant.CREATE_TIME);
				((Property)propertyList.get(0)).setValue(new Date(createDatetime));
			}
			// 最終更新ユーザID
			propertyList = PropertyUtil.getProperty(property, NodeConstant.MODIFIER_NAME);
			((Property)propertyList.get(0)).setValue(node.getModifyUserId());
			// 最終更新日時
			Long modifyDatetime = null;
			try {
				modifyDatetime = TimezoneUtil.getSimpleDateFormat().parse(node.getModifyDatetime()).getTime();
			} catch (Exception e) {
				//findbugs対応 エラーは発生しない想定なので本来不要だが Exception無視と思われないようtraceログの出力を追加
				m_log.trace("node2property : exception occuered",e);
			}
			if (modifyDatetime != null && modifyDatetime != 0L) {
				propertyList = PropertyUtil.getProperty(property, NodeConstant.MODIFY_TIME);
				((Property)propertyList.get(0)).setValue(new Date(modifyDatetime));
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
			// ノード名
			propertyList = PropertyUtil.getProperty(property, NodeConstant.NODE_NAME);
			((Property)propertyList.get(0)).setValue(node.getNodeName());
			// 画面アイコンイメージ
			propertyList = PropertyUtil.getProperty(property, NodeConstant.ICONIMAGE);
			((Property)propertyList.get(0)).setValue(node.getIconImage());
	
	
			// ----- IPアドレス関連 -----
			// IPバージョン
			propertyList = PropertyUtil.getProperty(property, NodeConstant.IP_ADDRESS_VERSION);
			
			if (node.getIpAddressVersion() != null) {
				switch (node.getIpAddressVersion()) {
				case IPV4:
					((Property)propertyList.get(0)).setValue(4);
					break;
				case IPV6:
					((Property)propertyList.get(0)).setValue(6);
					break;
				default:
					((Property)propertyList.get(0)).setValue(4);
				}
			} else {
				((Property)propertyList.get(0)).setValue(4);
			}
			// IPアドレスV4
			propertyList = PropertyUtil.getProperty(property, NodeConstant.IP_ADDRESS_V4);
			((Property)propertyList.get(0)).setValue(node.getIpAddressV4());
			// IPアドレスV6
			propertyList = PropertyUtil.getProperty(property, NodeConstant.IP_ADDRESS_V6);
			((Property)propertyList.get(0)).setValue(node.getIpAddressV6());

			// ----- Hinemosエージェント -----
			// 即時反映用ポート番号
			propertyList = PropertyUtil.getProperty(property, NodeConstant.AGENT_AWAKE_PORT);
			((Property)propertyList.get(0)).setValue(node.getAgentAwakePort());
		}

		// ----- 構成情報関連 -----
		Boolean isSearchTarget = null;
		// ホスト名
		propertyList = PropertyUtil.getProperty(property, NodeConstant.HOST_NAME);
		Property parentProperty = (Property)((Property)propertyList.get(0)).getParent();

		childProperty = (Property)propertyList.get(0);
		if (node.getNodeHostnameInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(parentProperty, childProperty);
			for (int i = 0; i < node.getNodeHostnameInfo().size(); i++) {
				Property target = null;
				if (i == (node.getNodeHostnameInfo().size() - 1)) {
					target = childProperty;
				} else {
					target = PropertyUtil.copy(childProperty);
					parentProperty.addChildren(target, index + i + 1);
				}
				NodeHostnameInfoResponse hostname = node.getNodeHostnameInfo().get(i);

				if(hostname.getSearchTarget() == null){
					isSearchTarget = false;
				} else{
					isSearchTarget = hostname.getSearchTarget();
				}

				// ホスト名
				target.setValue(hostname.getHostname());
				target.setStringHighlight(isSearchTarget);
			}
		}
		// ----- OS関連 -----
		String osName = null;
		String osRelease = null;
		String osVersion = null;
		String characterSet = null;
		Long startDateTime = null;
		if (node.getNodeOsInfo() != null) {
			osName = node.getNodeOsInfo().getOsName();
			osRelease = node.getNodeOsInfo().getOsRelease();
			osVersion = node.getNodeOsInfo().getOsVersion();
			characterSet = node.getNodeOsInfo().getCharacterSet();
			try {
				startDateTime = TimezoneUtil.getSimpleDateFormat().parse(node.getNodeOsInfo().getStartupDateTime()).getTime();
			} catch (Exception e) {
				//findbugs対応 エラーは発生しない想定なので本来不要だが Exception無視と思われないようtraceログの出力を追加
				m_log.trace("node2property : exception occuered",e);
			}
			if (node.getNodeOsInfo().getSearchTarget() == null) {
				isSearchTarget = false;
			} else {
				isSearchTarget = node.getNodeOsInfo().getSearchTarget();
			}
		}
		// OS名
		propertyList = PropertyUtil.getProperty(property, NodeConstant.OS_NAME);
		((Property)propertyList.get(0)).setValue(osName);
		// 強調表示
		((Property)propertyList.get(0)).setStringHighlight(isSearchTarget);
		// OSリリース
		propertyList = PropertyUtil.getProperty(property, NodeConstant.OS_RELEASE);
		((Property)propertyList.get(0)).setValue(osRelease);
		// OSバージョン
		propertyList = PropertyUtil.getProperty(property, NodeConstant.OS_VERSION);
		((Property)propertyList.get(0)).setValue(osVersion);
		// 文字セット
		propertyList = PropertyUtil.getProperty(property, NodeConstant.CHARACTER_SET);
		((Property)propertyList.get(0)).setValue(characterSet);
		// 起動日時
		if (startDateTime != null && startDateTime != 0) {
			propertyList = PropertyUtil.getProperty(property, NodeConstant.OS_STARTUP_DATE_TIME);
			((Property)propertyList.get(0)).setValue(new Date(startDateTime));
		}

		// ----- デバイス関連-----
		// ----- 汎用デバイス情報 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.GENERAL_DEVICE);
		parentProperty = (Property)((Property)propertyList.get(0)).getParent();

		childProperty = (Property)propertyList.get(0);
		if (node.getNodeDeviceInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, childProperty);
			for (int i = 0; i < node.getNodeDeviceInfo().size(); i++) {
				Property target = null;
				if (i == (node.getNodeDeviceInfo().size() - 1)) {
					target = childProperty;
				} else {
					target = PropertyUtil.copy(childProperty);
					parentProperty.addChildren(target, index + i + 1);
				}
				NodeGeneralDeviceInfoResponse device = node.getNodeDeviceInfo().get(i);

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
		parentProperty = (Property)((Property)propertyList.get(0)).getParent();

		childProperty = (Property)propertyList.get(0);
		if (node.getNodeCpuInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, childProperty);
			for (int i = 0; i < node.getNodeCpuInfo().size(); i++) {
				Property target = null;
				if (i == (node.getNodeCpuInfo().size() - 1)) {
					target = childProperty;
				} else {
					target = PropertyUtil.copy(childProperty);
					parentProperty.addChildren(target, index + i + 1);
				}
				NodeCpuInfoResponse cpu = node.getNodeCpuInfo().get(i);

				// トップ表示情報（デバイス表示名）
				target.setValue(cpu.getDeviceDisplayName());
				// 強調表示
				target.setStringHighlight(cpu.getSearchTarget());

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
				// コア数
				propertyList = PropertyUtil.getProperty(target, NodeConstant.CPU_CORE_COUNT);
				((Property)propertyList.get(0)).setValue(cpu.getCoreCount());
				// スレッド数
				propertyList = PropertyUtil.getProperty(target, NodeConstant.CPU_THREAD_COUNT);
				((Property)propertyList.get(0)).setValue(cpu.getThreadCount());
				// クロック数
				propertyList = PropertyUtil.getProperty(target, NodeConstant.CPU_CLOCK_COUNT);
				((Property)propertyList.get(0)).setValue(cpu.getClockCount());

			}
		}

		// ----- MEMデバイス情報 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.MEMORY);
		parentProperty = (Property)((Property)propertyList.get(0)).getParent();

		childProperty = (Property)propertyList.get(0);
		if (node.getNodeMemoryInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, childProperty);
			for (int i = 0; i < node.getNodeMemoryInfo().size(); i++) {
				Property target = null;
				if (i == (node.getNodeMemoryInfo().size() - 1)) {
					target = childProperty;
				} else {
					target = PropertyUtil.copy(childProperty);
					parentProperty.addChildren(target, index + i + 1);
				}
				NodeMemoryInfoResponse memory = node.getNodeMemoryInfo().get(i);

				// トップ表示情報（デバイス表示名）
				target.setValue(memory.getDeviceDisplayName());
				// 強調表示
				target.setStringHighlight(memory.getSearchTarget());

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
		parentProperty = (Property)((Property)propertyList.get(0)).getParent();

		childProperty = (Property)propertyList.get(0);
		if (node.getNodeNetworkInterfaceInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, childProperty);
			for (int i = 0; i < node.getNodeNetworkInterfaceInfo().size(); i++) {
				Property target = null;
				if (i == (node.getNodeNetworkInterfaceInfo().size() - 1)) {
					target = childProperty;
				} else {
					target = PropertyUtil.copy(childProperty);
					parentProperty.addChildren(target, index + i + 1);
				}
				NodeNetworkInterfaceInfoResponse nic = node.getNodeNetworkInterfaceInfo().get(i);

				// トップ表示情報（デバイス表示名）
				target.setValue(nic.getDeviceDisplayName());
				// 強調表示
				target.setStringHighlight(nic.getSearchTarget());

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
		parentProperty = (Property)((Property)propertyList.get(0)).getParent();

		childProperty = (Property)propertyList.get(0);
		if (node.getNodeDiskInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, childProperty);
			for (int i = 0; i < node.getNodeDiskInfo().size(); i++) {
				Property target = null;
				if (i == (node.getNodeDiskInfo().size() - 1)) {
					target = childProperty;
				} else {
					target = PropertyUtil.copy(childProperty);
					parentProperty.addChildren(target, index + i + 1);
				}
				NodeDiskInfoResponse disk = node.getNodeDiskInfo().get(i);

				// トップ表示情報（デバイス表示名）
				target.setValue(disk.getDeviceDisplayName());
				// 強調表示
				target.setStringHighlight(disk.getSearchTarget());

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
		parentProperty = (Property)((Property)propertyList.get(0)).getParent();

		childProperty = (Property)propertyList.get(0);
		if (node.getNodeFilesystemInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, childProperty);
			for (int i = 0; i < node.getNodeFilesystemInfo().size(); i++) {
				Property target = null;
				if (i == (node.getNodeFilesystemInfo().size() - 1)) {
					target = childProperty;
				} else {
					target = PropertyUtil.copy(childProperty);
					parentProperty.addChildren(target, index + i + 1);
				}
				NodeFilesystemInfoResponse filesystem = node.getNodeFilesystemInfo().get(i);

				// トップ表示情報（デバイス表示名）
				target.setValue(filesystem.getDeviceDisplayName());
				// 強調表示
				target.setStringHighlight(filesystem.getSearchTarget());

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

		// ---- ネットワーク接続 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.NETSTAT);
		parentProperty = (Property)((Property)propertyList.get(0)).getParent();

		childProperty = (Property)propertyList.get(0);
		if (node.getNodeNetstatInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, childProperty);
			for (int i = 0; i < node.getNodeNetstatInfo().size(); i++) {
				Property target = null;
				if (i == (node.getNodeNetstatInfo().size() - 1)) {
					target = childProperty;
				} else {
					target = PropertyUtil.copy(childProperty);
					parentProperty.addChildren(target, index + i + 1);
				}
				NodeNetstatInfoResponse netstatInfo = node.getNodeNetstatInfo().get(i);

				// トップ表示情報（プロトコル、ローカルIPアドレス、ローカルポート）
				target.setValue(String.format(
						"%s %s:%s", netstatInfo.getProtocol(), netstatInfo.getLocalIpAddress(), netstatInfo.getLocalPort()));
				// 強調表示
				target.setStringHighlight(netstatInfo.getSearchTarget());

				// プロトコル
				propertyList = PropertyUtil.getProperty(target, NodeConstant.NETSTAT_PROTOCOL);
				((Property)propertyList.get(0)).setValue(netstatInfo.getProtocol());
				// ローカルIPアドレス
				propertyList = PropertyUtil.getProperty(target, NodeConstant.NETSTAT_LOCAL_IP_ADDRESS);
				((Property)propertyList.get(0)).setValue(netstatInfo.getLocalIpAddress());
				// ローカルポート
				propertyList = PropertyUtil.getProperty(target, NodeConstant.NETSTAT_LOCAL_PORT);
				((Property)propertyList.get(0)).setValue(netstatInfo.getLocalPort());
				// 外部IPアドレス
				propertyList = PropertyUtil.getProperty(target, NodeConstant.NETSTAT_FOREIGN_IP_ADDRESS);
				((Property)propertyList.get(0)).setValue(netstatInfo.getForeignIpAddress());
				// 外部ポート
				propertyList = PropertyUtil.getProperty(target, NodeConstant.NETSTAT_FOREIGN_PORT);
				((Property)propertyList.get(0)).setValue(netstatInfo.getForeignPort());
				// プロセス名
				propertyList = PropertyUtil.getProperty(target, NodeConstant.NETSTAT_PROCESS_NAME);
				((Property)propertyList.get(0)).setValue(netstatInfo.getProcessName());
				// PID
				if (netstatInfo.getPid() != -1) {
					propertyList = PropertyUtil.getProperty(target, NodeConstant.NETSTAT_PID);
					((Property)propertyList.get(0)).setValue(netstatInfo.getPid());
				}
				// 状態
				propertyList = PropertyUtil.getProperty(target, NodeConstant.NETSTAT_STATUS);
				((Property)propertyList.get(0)).setValue(netstatInfo.getStatus());
			}
		}

		// ---- プロセス情報 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.PROCESS);
		parentProperty = (Property)((Property)propertyList.get(0)).getParent();

		childProperty = (Property)propertyList.get(0);
		if (node.getNodeProcessInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, childProperty);
			for (int i = 0; i < node.getNodeProcessInfo().size(); i++) {
				Property target = null;
				if (i == (node.getNodeProcessInfo().size() - 1)) {
					target = childProperty;
				} else {
					target = PropertyUtil.copy(childProperty);
					parentProperty.addChildren(target, index + i + 1);
				}
				NodeProcessInfoResponse processInfo = node.getNodeProcessInfo().get(i);

				// トップ表示情報（プロセス名）
				target.setValue(processInfo.getProcessName());
				// 強調表示
				target.setStringHighlight(processInfo.getSearchTarget());

				// プロセス名
				propertyList = PropertyUtil.getProperty(target, NodeConstant.PROCESS_NAME);
				((Property)propertyList.get(0)).setValue(processInfo.getProcessName());
				// PID
				if (processInfo.getPid() != -1) {
					propertyList = PropertyUtil.getProperty(target, NodeConstant.PROCESS_PID);
					((Property)propertyList.get(0)).setValue(processInfo.getPid());
				}
				// 引数付フルパス
				propertyList = PropertyUtil.getProperty(target, NodeConstant.PROCESS_PATH);
				((Property)propertyList.get(0)).setValue(processInfo.getPath());
				// 実行ユーザ
				propertyList = PropertyUtil.getProperty(target, NodeConstant.PROCESS_EXEC_USER);
				((Property)propertyList.get(0)).setValue(processInfo.getExecUser());
				// 起動日時
				Long startupDateTime = null;
				try {
					startupDateTime = TimezoneUtil.getSimpleDateFormat().parse(processInfo.getStartupDateTime()).getTime();
				} catch (Exception e) {
					//findbugs対応 エラーは発生しない想定なので本来不要だが Exception無視と思われないようtraceログの出力を追加
					m_log.trace("node2property : exception occuered",e);
				}
				if (startupDateTime != null && startupDateTime != 0L) {
					propertyList = PropertyUtil.getProperty(target, NodeConstant.PROCESS_STARTUP_DATE_TIME);
					((Property)propertyList.get(0)).setValue(new Date(startupDateTime));
				}
			}
		}

		// ---- パッケージ情報 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.PACKAGE);
		parentProperty = (Property)((Property)propertyList.get(0)).getParent();

		childProperty = (Property)propertyList.get(0);
		if (node.getNodePackageInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, childProperty);
			for (int i = 0; i < node.getNodePackageInfo().size(); i++) {
				Property target = null;
				if (i == (node.getNodePackageInfo().size() - 1)) {
					target = childProperty;
				} else {
					target = PropertyUtil.copy(childProperty);
					parentProperty.addChildren(target, index + i + 1);
				}
				NodePackageInfoResponse packageInfo = node.getNodePackageInfo().get(i);

				// トップ表示情報（パッケージID）
				target.setValue(packageInfo.getPackageName());
				// 強調表示
				target.setStringHighlight(packageInfo.getSearchTarget());

				// パッケージID
				propertyList = PropertyUtil.getProperty(target, NodeConstant.PACKAGE_ID);
				((Property)propertyList.get(0)).setValue(packageInfo.getPackageId());
				// パッケージ名
				propertyList = PropertyUtil.getProperty(target, NodeConstant.PACKAGE_NAME);
				((Property)propertyList.get(0)).setValue(packageInfo.getPackageName());
				// バージョン
				propertyList = PropertyUtil.getProperty(target, NodeConstant.PACKAGE_VERSION);
				((Property)propertyList.get(0)).setValue(packageInfo.getVersion());
				// リリース番号
				propertyList = PropertyUtil.getProperty(target, NodeConstant.PACKAGE_RELEASE);
				((Property)propertyList.get(0)).setValue(packageInfo.getRelease());
				// インストール日時
				Long installDate = null;
				try {
					installDate = TimezoneUtil.getSimpleDateFormat().parse(packageInfo.getInstallDate()).getTime();
				} catch (Exception e) {
					//findbugs対応 エラーは発生しない想定なので本来不要だが Exception無視と思われないようtraceログの出力を追加
					m_log.trace("node2property : exception occuered",e);
				}
				if (installDate != null && installDate != 0L) {
					propertyList = PropertyUtil.getProperty(target, NodeConstant.PACKAGE_INSTALL_DATE);
					((Property)propertyList.get(0)).setValue(new Date(installDate));
				}
				// ベンダ
				propertyList = PropertyUtil.getProperty(target, NodeConstant.PACKAGE_VENDOR);
				((Property)propertyList.get(0)).setValue(packageInfo.getVendor());
				// アーキテクチャ
				propertyList = PropertyUtil.getProperty(target, NodeConstant.PACKAGE_ARCHITECTURE);
				((Property)propertyList.get(0)).setValue(packageInfo.getArchitecture());
			}
		}

		// ---- 個別導入製品情報 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.PRODUCT);
		parentProperty = (Property)((Property)propertyList.get(0)).getParent();

		childProperty = (Property)propertyList.get(0);
		if (node.getNodeProductInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, childProperty);
			for (int i = 0; i < node.getNodeProductInfo().size(); i++) {
				Property target = null;
				if (i == (node.getNodeProductInfo().size() - 1)) {
					target = childProperty;
				} else {
					target = PropertyUtil.copy(childProperty);
					parentProperty.addChildren(target, index + i + 1);
				}
				NodeProductInfoResponse productInfo = node.getNodeProductInfo().get(i);

				// トップ表示情報（名前）
				target.setValue(productInfo.getProductName());
				// 強調表示
				target.setStringHighlight(productInfo.getSearchTarget());

				// 名前
				propertyList = PropertyUtil.getProperty(target, NodeConstant.PRODUCT_NAME);
				((Property)propertyList.get(0)).setValue(productInfo.getProductName());
				// バージョン
				propertyList = PropertyUtil.getProperty(target, NodeConstant.PRODUCT_VERSION);
				((Property)propertyList.get(0)).setValue(productInfo.getVersion());
				// インストールパス
				propertyList = PropertyUtil.getProperty(target, NodeConstant.PRODUCT_PATH);
				((Property)propertyList.get(0)).setValue(productInfo.getPath());
			}
		}

		// ---- ライセンス情報 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.LICENSE);
		parentProperty = (Property)((Property)propertyList.get(0)).getParent();

		childProperty = (Property)propertyList.get(0);
		if (node.getNodeLicenseInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, childProperty);
			for (int i = 0; i < node.getNodeLicenseInfo().size(); i++) {
				Property target = null;
				if (i == (node.getNodeLicenseInfo().size() - 1)) {
					target = childProperty;
				} else {
					target = PropertyUtil.copy(childProperty);
					parentProperty.addChildren(target, index + i + 1);
				}
				NodeLicenseInfoResponse licenseInfo = node.getNodeLicenseInfo().get(i);

				// トップ表示情報（製品名）
				target.setValue(licenseInfo.getProductName());
				// 強調表示
				target.setStringHighlight(licenseInfo.getSearchTarget());

				// 製品名
				propertyList = PropertyUtil.getProperty(target, NodeConstant.LICENSE_PRODUCT_NAME);
				((Property)propertyList.get(0)).setValue(licenseInfo.getProductName());
				// ベンダ
				propertyList = PropertyUtil.getProperty(target, NodeConstant.LICENSE_VENDOR);
				((Property)propertyList.get(0)).setValue(licenseInfo.getVendor());
				// ベンダ連絡先
				propertyList = PropertyUtil.getProperty(target, NodeConstant.LICENSE_VENDOR_CONTACT);
				((Property)propertyList.get(0)).setValue(licenseInfo.getVendorContact());
				// シリアルナンバー
				propertyList = PropertyUtil.getProperty(target, NodeConstant.LICENSE_SERIAL_NUMBER);
				((Property)propertyList.get(0)).setValue(licenseInfo.getSerialNumber());
				// 数量
				if (licenseInfo.getCount()!= null && licenseInfo.getCount() != -1) {
					propertyList = PropertyUtil.getProperty(target, NodeConstant.LICENSE_COUNT);
					((Property)propertyList.get(0)).setValue(licenseInfo.getCount());
				}
				// 有効期限
				Long expirationDate = null;
				try {
					expirationDate = TimezoneUtil.getSimpleDateFormat().parse(licenseInfo.getExpirationDate()).getTime();
				} catch (Exception e) {
					//findbugs対応 エラーは発生しない想定なので本来不要だが Exception無視と思われないようtraceログの出力を追加
					m_log.trace("node2property : exception occuered",e);
				}
				if (expirationDate != null && expirationDate != 0L) {
					propertyList = PropertyUtil.getProperty(target, NodeConstant.LICENSE_EXPIRATION_DATE);
					((Property)propertyList.get(0)).setValue(new Date(expirationDate));
				}
			}
		}

		// ----- ノード変数 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.GENERAL_NODE_VARIABLE);
		parentProperty = (Property)((Property)propertyList.get(0)).getParent();

		childProperty = (Property)propertyList.get(0);
		if (node.getNodeVariableInfo() != null) {
			int index = PropertyUtil.getPropertyIndex(property, childProperty);
			for (int i = 0; i < node.getNodeVariableInfo().size(); i++) {
				Property target = null;
				if (i == (node.getNodeVariableInfo().size() - 1)) {
					target = childProperty;
				} else {
					target = PropertyUtil.copy(childProperty);
					parentProperty.addChildren(target, index + i + 1);
				}
				NodeVariableInfoResponse variable = node.getNodeVariableInfo().get(i);

				// トップ表示情報（ノード変数名）
				target.setValue(variable.getNodeVariableName());
				// 強調表示
				target.setStringHighlight(variable.getSearchTarget());

				// ノード変数名
				propertyList = PropertyUtil.getProperty(target, NodeConstant.NODE_VARIABLE_NAME);
				((Property)propertyList.get(0)).setValue(variable.getNodeVariableName());
				// ノード変数値
				propertyList = PropertyUtil.getProperty(target, NodeConstant.NODE_VARIABLE_VALUE);
				((Property)propertyList.get(0)).setValue(variable.getNodeVariableValue());
			}
		}

		// ---- ユーザ任意情報 -----
		propertyList = PropertyUtil.getProperty(property, NodeConstant.CUSTOM);
		Property parent = (Property)((Property)propertyList.get(0)).getParent();

		Property custom = (Property)propertyList.get(0);
		if (node.getNodeCustomInfo() != null) {
			parent.removeChildren();
			Property target = null;
			if(node.getNodeCustomInfo().size() <= 0){
				target = custom;
				parent.addChildren(target, 0);
				target.setName(Messages.getString("node.custom", locale));
				target.setValue("");
			}
			
			for (int i = 0; i < node.getNodeCustomInfo().size(); i++) {
				NodeCustomInfoResponse customInfo = node.getNodeCustomInfo().get(i);
				target = PropertyUtil.copy(custom);
				parent.addChildren(target, i);
				// 名前
				target.setName(customInfo.getDisplayName());
				// 値(コマンド名 \n コマンド取得結果）
				target.setValue(customInfo.getValue());
				// 強調表示
				target.setStringHighlight(customInfo.getSearchTarget());
			}
		}

		if (!isNodeMap) {
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
			if (node.getSnmpVersion() != null) {
				switch (node.getSnmpVersion()) {
				case V1:
					((Property)propertyList.get(0)).setValue(SnmpVersionConstant.STRING_V1);
					break;
				case V2:
					((Property)propertyList.get(0)).setValue(SnmpVersionConstant.STRING_V2);
					break;
				case V3:
					((Property)propertyList.get(0)).setValue(SnmpVersionConstant.STRING_V3);
					break;
				}
			} else {
				((Property)propertyList.get(0)).setValue("");
			}
			// SNMPセキュリティレベル
			propertyList = PropertyUtil.getProperty(property, NodeConstant.SNMP_SECURITY_LEVEL);
			if (node.getSnmpSecurityLevel() != null) {
				switch (node.getSnmpSecurityLevel()) {
				case NOAUTH_NOPRIV:
					((Property)propertyList.get(0)).setValue(SnmpSecurityLevelConstant.NOAUTH_NOPRIV);
					break;
				case AUTH_NOPRIV:
					((Property)propertyList.get(0)).setValue(SnmpSecurityLevelConstant.AUTH_NOPRIV);
					break;
				case AUTH_PRIV:
					((Property)propertyList.get(0)).setValue(SnmpSecurityLevelConstant.AUTH_PRIV);
					break;
				default:
					((Property)propertyList.get(0)).setValue("");
				}
			} else {
				((Property)propertyList.get(0)).setValue("");
			}
			// SNMP接続認証パスワード
			propertyList = PropertyUtil.getProperty(property, NodeConstant.SNMP_AUTH_PASSWORD);
			((Property)propertyList.get(0)).setValue(node.getSnmpAuthPassword());
			// SNMP接続暗号化パスワード
			propertyList = PropertyUtil.getProperty(property, NodeConstant.SNMP_PRIV_PASSWORD);
			((Property)propertyList.get(0)).setValue(node.getSnmpPrivPassword());
			// SNMP認証プロトコル
			propertyList = PropertyUtil.getProperty(property, NodeConstant.SNMP_AUTH_PROTOCOL);
			if (node.getSnmpAuthProtocol() != null) {
				switch (node.getSnmpAuthProtocol()) {
				case MD5:
					((Property)propertyList.get(0)).setValue(SnmpProtocolConstant.MD5);
					break;
				case SHA:
					((Property)propertyList.get(0)).setValue(SnmpProtocolConstant.SHA);
					break;
				case SHA224:
					((Property)propertyList.get(0)).setValue(SnmpProtocolConstant.SHA224);
					break;
				case SHA256:
					((Property)propertyList.get(0)).setValue(SnmpProtocolConstant.SHA256);
					break;
				case SHA384:
					((Property)propertyList.get(0)).setValue(SnmpProtocolConstant.SHA384);
					break;
				case SHA512:
					((Property)propertyList.get(0)).setValue(SnmpProtocolConstant.SHA512);
					break;
				default:
					((Property)propertyList.get(0)).setValue("");
					break;
				}
			} else {
				((Property)propertyList.get(0)).setValue("");
			}
			// SNMP暗号化プロトコル
			propertyList = PropertyUtil.getProperty(property, NodeConstant.SNMP_PRIV_PROTOCOL);
			if (node.getSnmpPrivProtocol() != null) {
				switch (node.getSnmpPrivProtocol()) {
				case AES:
					((Property)propertyList.get(0)).setValue(SnmpProtocolConstant.AES);
					break;
				case DES:
					((Property)propertyList.get(0)).setValue(SnmpProtocolConstant.DES);
					break;
				case AES192:
					((Property)propertyList.get(0)).setValue(SnmpProtocolConstant.AES192);
					break;
				case AES256:
					((Property)propertyList.get(0)).setValue(SnmpProtocolConstant.AES256);
					break;
				default:
					((Property)propertyList.get(0)).setValue("");
					break;
				}
			} else {
				((Property)propertyList.get(0)).setValue("");
			}
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
			if (node.getWbemProtocol() != null) {
				switch (node.getWbemProtocol()) {
				case HTTP:
					((Property)propertyList.get(0)).setValue(WbemProtocolConstant.HTTP);
					break;
				case HTTPS:
					((Property)propertyList.get(0)).setValue(WbemProtocolConstant.HTTPS);
					break;
				default:
					((Property)propertyList.get(0)).setValue("");
					break;
				}
			} else {
				((Property)propertyList.get(0)).setValue("");
			}
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
			if (node.getWinrmProtocol() != null) {
				switch (node.getWinrmProtocol()) {
				case HTTP:
					((Property)propertyList.get(0)).setValue(WinrmProtocolConstant.HTTP);
					break;
				case HTTPS:
					((Property)propertyList.get(0)).setValue(WinrmProtocolConstant.HTTPS);
					break;
				default:
					((Property)propertyList.get(0)).setValue("");
					break;
				}
			} else {
				((Property)propertyList.get(0)).setValue("");
			}
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
			// クラウドログ優先度
			propertyList = PropertyUtil.getProperty(property, NodeConstant.CLOUDLOGPRIORITY);
			((Property)propertyList.get(0)).setValue(node.getCloudLogPriority());
	
			// ---- RPAツール情報 -----
			// ログ出力先ディレクトリ
			propertyList = PropertyUtil.getProperty(property, NodeConstant.RPA_LOG_DIRECTORY);
			((Property)propertyList.get(0)).setValue(node.getRpaLogDir());

			// ---- RPA管理ツール情報 -----
			// RPA管理ツールタイプ
			propertyList = PropertyUtil.getProperty(property, NodeConstant.RPA_MANAGEMENT_TOOL_TYPE);
			((Property)propertyList.get(0)).setValue(node.getRpaManagementToolType());
			// RPAリソースID
			propertyList = PropertyUtil.getProperty(property, NodeConstant.RPA_RESOURCE_ID);
			((Property)propertyList.get(0)).setValue(node.getRpaResourceId());
			// ユーザ名
			propertyList = PropertyUtil.getProperty(property, NodeConstant.RPA_USER);
			((Property)propertyList.get(0)).setValue(node.getRpaUser());
			// RPA実行環境ID
			propertyList = PropertyUtil.getProperty(property, NodeConstant.RPA_EXECUTION_ENVIRONMENT_ID);
			((Property)propertyList.get(0)).setValue(node.getRpaExecEnvId());

	
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
				for (NodeNoteInfoResponse note : node.getNodeNoteInfo()) {
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
		}

		return property;
	}


	/**
	 * ノード用プロパティを返します。
	 *
	 * @param mode
	 * @param isNodeMap true:ノードマップ（構成情報検索）表示
	 * @return ノード用プロパティ
	 */
	public static Property getProperty(String managerName, int mode, Locale locale, boolean isNodeMap) {

		// ------------------------
		// ---- 変数定義-----
		// ------------------------

		// ---- ファシリティ情報 -----
		//ファシリティID
		Property facilityId =
				new Property(NodeConstant.FACILITY_ID, Messages.getString("facility.id", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_512);
		//ファシリティ名
		Property facilityName =
				new Property(NodeConstant.FACILITY_NAME, Messages.getString("facility.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
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
		//ノード名
		Property nodeName =
				new Property(NodeConstant.NODE_NAME, Messages.getString("node.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
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

		// ----- Hinemosエージェント情報-----
		//Hinemosエージェント
		Property agent =
				new Property(NodeConstant.AGENT, Messages.getString("agent", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//即時反映用ポート番号
		Property agentAwakePort =
				new Property(NodeConstant.AGENT_AWAKE_PORT, Messages.getString("agent.awake.port", locale), PropertyDefineConstant.EDITOR_NUM, DataRangeConstant.PORT_NUMBER_MAX, 0);

		// ---- 構成情報 -----
		Property nodeConfigInformation =
				new Property(NodeConstant.NODE_CONFIG_INFORMATION, Messages.getString("node.config", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
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
		//OSリリース
		Property osRelease =
				new Property(NodeConstant.OS_RELEASE, Messages.getString("os.release", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//OSバージョン
		Property osVersion =
				new Property(NodeConstant.OS_VERSION, Messages.getString("os.version", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//文字セット
		Property characterSet =
				new Property(NodeConstant.CHARACTER_SET, Messages.getString("character.set", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_16);
		//起動日時
		Property osStartupDateTime =
				new Property(NodeConstant.OS_STARTUP_DATE_TIME, Messages.getString("os.startup.date.time", locale), PropertyDefineConstant.EDITOR_DATETIME);

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
				new Property(NodeConstant.DEVICE_NAME, Messages.getString("device.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//デバイスINDEX
		Property deviceIndex =
				new Property(NodeConstant.DEVICE_INDEX, Messages.getString("device.index", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//デバイス種別
		Property deviceType =
				new Property(NodeConstant.DEVICE_TYPE, Messages.getString("device.type", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_32);
		//デバイスサイズ
		Property deviceSize =
				new Property(NodeConstant.DEVICE_SIZE, Messages.getString("device.size", locale), PropertyDefineConstant.EDITOR_NUM_LONG, Long.MAX_VALUE, 0L);
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
				new Property(NodeConstant.DEVICE_NAME, Messages.getString("device.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//デバイスINDEX
		Property cpuDeviceIndex =
				new Property(NodeConstant.DEVICE_INDEX, Messages.getString("device.index", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//デバイス種別
		Property cpuDeviceType =
				new Property(NodeConstant.DEVICE_TYPE, Messages.getString("device.type", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_32);
		//デバイスサイズ
		Property cpuDeviceSize =
				new Property(NodeConstant.DEVICE_SIZE, Messages.getString("device.size", locale), PropertyDefineConstant.EDITOR_NUM_LONG, Long.MAX_VALUE, 0L);
		//デバイスサイズ単位
		Property cpuDeviceSizeUnit =
				new Property(NodeConstant.DEVICE_SIZE_UNIT, Messages.getString("device.size.unit", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//説明
		Property cpuDeviceDescription =
				new Property(NodeConstant.DEVICE_DESCRIPTION, Messages.getString("description", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//コア数
		Property cpuCoreCount =
				new Property(NodeConstant.CPU_CORE_COUNT, Messages.getString("cpu.core.count", locale), PropertyDefineConstant.EDITOR_NUM, DataRangeConstant.INTEGER_HIGH, 0);
		//スレッド数
		Property cpuThreadCount =
				new Property(NodeConstant.CPU_THREAD_COUNT, Messages.getString("cpu.thread.count", locale), PropertyDefineConstant.EDITOR_NUM, DataRangeConstant.INTEGER_HIGH, 0);
		//クロック数
		Property cpuClockCount =
				new Property(NodeConstant.CPU_CLOCK_COUNT, Messages.getString("cpu.clock.count", locale), PropertyDefineConstant.EDITOR_NUM, DataRangeConstant.INTEGER_HIGH, 0);


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
				new Property(NodeConstant.DEVICE_NAME, Messages.getString("device.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//デバイスINDEX
		Property memoryDeviceIndex =
				new Property(NodeConstant.DEVICE_INDEX, Messages.getString("device.index", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//デバイス種別
		Property memoryDeviceType =
				new Property(NodeConstant.DEVICE_TYPE, Messages.getString("device.type", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_32);
		//デバイスサイズ
		Property memoryDeviceSize =
				new Property(NodeConstant.DEVICE_SIZE, Messages.getString("device.size", locale), PropertyDefineConstant.EDITOR_NUM_LONG, Long.MAX_VALUE, 0L);
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
				new Property(NodeConstant.DEVICE_NAME, Messages.getString("device.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//デバイスINDEX
		Property networkInterfaceDeviceIndex =
				new Property(NodeConstant.DEVICE_INDEX, Messages.getString("device.index", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//デバイス種別
		Property networkInterfaceDeviceType =
				new Property(NodeConstant.DEVICE_TYPE, Messages.getString("device.type", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_32);
		//デバイスサイズ
		Property networkInterfaceDeviceSize =
				new Property(NodeConstant.DEVICE_SIZE, Messages.getString("device.size", locale), PropertyDefineConstant.EDITOR_NUM_LONG, Long.MAX_VALUE, 0L);
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
				new Property(NodeConstant.DEVICE_NAME, Messages.getString("device.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//デバイスINDEX
		Property diskDeviceIndex =
				new Property(NodeConstant.DEVICE_INDEX, Messages.getString("device.index", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//デバイス種別
		Property diskDeviceType =
				new Property(NodeConstant.DEVICE_TYPE, Messages.getString("device.type", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_32);
		//デバイスサイズ
		Property diskDeviceSize =
				new Property(NodeConstant.DEVICE_SIZE, Messages.getString("device.size", locale), PropertyDefineConstant.EDITOR_NUM_LONG, Long.MAX_VALUE, 0L);
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
				new Property(NodeConstant.DEVICE_NAME, Messages.getString("device.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//デバイスINDEX
		Property fileSystemDeviceIndex =
				new Property(NodeConstant.DEVICE_INDEX, Messages.getString("device.index", locale), PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);
		//デバイス種別
		Property fileSystemDeviceType =
				new Property(NodeConstant.DEVICE_TYPE, Messages.getString("device.type", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_32);
		//デバイスサイズ
		Property fileSystemDeviceSize =
				new Property(NodeConstant.DEVICE_SIZE, Messages.getString("device.size", locale), PropertyDefineConstant.EDITOR_NUM_LONG, Long.MAX_VALUE, 0L);
		//デバイスサイズ単位
		Property fileSystemDeviceSizeUnit =
				new Property(NodeConstant.DEVICE_SIZE_UNIT, Messages.getString("device.size.unit", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//説明
		Property fileSystemDeviceDescription =
				new Property(NodeConstant.DEVICE_DESCRIPTION, Messages.getString("description", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		//ファイルシステム種別
		Property fileSystemType =
				new Property(NodeConstant.FILE_SYSTEM_TYPE, Messages.getString("file.system.type", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);

		// ---- ネットワーク接続 -----
		Property nodeNetstat =
				new Property(NodeConstant.NETSTAT, Messages.getString("node.netstat", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property nodeNetstatList =
				new Property(NodeConstant.NETSTAT_LIST, Messages.getString("node.netstat.list", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property netstatProtocol =
				new Property(NodeConstant.NETSTAT_PROTOCOL, Messages.getString("node.netstat.protocol", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property netstatLocalIpAddress =
				new Property(NodeConstant.NETSTAT_LOCAL_IP_ADDRESS, Messages.getString("node.netstat.local.ip.address", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property netstatLocalPort =
				new Property(NodeConstant.NETSTAT_LOCAL_PORT, Messages.getString("node.netstat.local.port", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property netstatForeignIpAddress =
				new Property(NodeConstant.NETSTAT_FOREIGN_IP_ADDRESS, Messages.getString("node.netstat.foreign.ip.address", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property netstatForeignPort =
				new Property(NodeConstant.NETSTAT_FOREIGN_PORT, Messages.getString("node.netstat.foreign.port", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property netstatProcessName =
				new Property(NodeConstant.NETSTAT_PROCESS_NAME, Messages.getString("node.netstat.process.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property netstatPid =
				new Property(NodeConstant.NETSTAT_PID, Messages.getString("node.netstat.pid", locale), PropertyDefineConstant.EDITOR_NUM, DataRangeConstant.INTEGER_HIGH, 0);
		Property netstatStatus =
				new Property(NodeConstant.NETSTAT_STATUS, Messages.getString("node.netstat.status", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);

		// ---- プロセス情報 -----
		Property process =
				new Property(NodeConstant.PROCESS, Messages.getString("node.process", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property processList =
				new Property(NodeConstant.PROCESS_LIST, Messages.getString("node.process.list", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property processName =
				new Property(NodeConstant.PROCESS_NAME, Messages.getString("node.process.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property processPid =
				new Property(NodeConstant.PROCESS_PID, Messages.getString("node.process.pid", locale), PropertyDefineConstant.EDITOR_NUM, DataRangeConstant.INTEGER_HIGH, 0);
		Property processPath =
				new Property(NodeConstant.PROCESS_PATH, Messages.getString("node.process.path", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.TEXT);
		Property processExecUser =
				new Property(NodeConstant.PROCESS_EXEC_USER, Messages.getString("node.process.exec.user", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property processStartupDateTime =
				new Property(NodeConstant.PROCESS_STARTUP_DATE_TIME, Messages.getString("node.process.startup.date.time", locale), PropertyDefineConstant.EDITOR_DATETIME);

		// ---- パッケージ情報 -----
		Property nodePackage =
				new Property(NodeConstant.PACKAGE, Messages.getString("node.package", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property nodePackageList =
				new Property(NodeConstant.PACKAGE_LIST, Messages.getString("node.package.list", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property packageId =
				new Property(NodeConstant.PACKAGE_ID, Messages.getString("node.package.id", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property packageName =
				new Property(NodeConstant.PACKAGE_NAME, Messages.getString("node.package.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property packageVersion =
				new Property(NodeConstant.PACKAGE_VERSION, Messages.getString("node.package.version", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property packageRelease =
				new Property(NodeConstant.PACKAGE_RELEASE, Messages.getString("node.package.release", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property packageInstallDate =
				new Property(NodeConstant.PACKAGE_INSTALL_DATE, Messages.getString("node.package.install.date", locale), PropertyDefineConstant.EDITOR_DATETIME);
		Property packageVendor =
				new Property(NodeConstant.PACKAGE_VENDOR, Messages.getString("node.package.vendor", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property packageArchitecture =
				new Property(NodeConstant.PACKAGE_ARCHITECTURE, Messages.getString("node.package.architecture", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);

		// ---- 個別導入製品情報 -----
		Property nodeProduct =
				new Property(NodeConstant.PRODUCT, Messages.getString("node.product", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property nodeProductList =
				new Property(NodeConstant.PRODUCT_LIST, Messages.getString("node.product.list", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property productName =
				new Property(NodeConstant.PRODUCT_NAME, Messages.getString("node.product.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property productVersion =
				new Property(NodeConstant.PRODUCT_VERSION, Messages.getString("node.product.version", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property productPath =
				new Property(NodeConstant.PRODUCT_PATH, Messages.getString("node.product.path", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.TEXT);

		// ---- ライセンス -----
		Property nodeLicense =
				new Property(NodeConstant.LICENSE, Messages.getString("node.license", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property nodeLicenseList =
				new Property(NodeConstant.LICENSE_LIST, Messages.getString("node.license.list", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property licenseProductName =
				new Property(NodeConstant.LICENSE_PRODUCT_NAME, Messages.getString("node.license.product.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property licenseVendor =
				new Property(NodeConstant.LICENSE_VENDOR, Messages.getString("node.license.vendor", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property licenseVendorContact =
				new Property(NodeConstant.LICENSE_VENDOR_CONTACT, Messages.getString("node.license.vendor.contact", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property licenseSerialNumber =
				new Property(NodeConstant.LICENSE_SERIAL_NUMBER, Messages.getString("node.license.serial.number", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property licenseCount =
				new Property(NodeConstant.LICENSE_COUNT, Messages.getString("node.license.count", locale), PropertyDefineConstant.EDITOR_NUM, DataRangeConstant.INTEGER_HIGH, 0);
		Property licenseExpirationDate =
				new Property(NodeConstant.LICENSE_EXPIRATION_DATE, Messages.getString("node.license.expiration.date", locale), PropertyDefineConstant.EDITOR_DATETIME);

		// ---- ノード変数 -----
		Property nodeVariable =
				new Property(NodeConstant.NODE_VARIABLE, Messages.getString("node.variable", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		Property generalNodeVariable =
				new Property(NodeConstant.GENERAL_NODE_VARIABLE, Messages.getString("node.variable", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		Property nodeVariableName =
				new Property(NodeConstant.NODE_VARIABLE_NAME, Messages.getString("node.variable.name", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_128);
		Property nodeVariableValues =
				new Property(NodeConstant.NODE_VARIABLE_VALUE, Messages.getString("node.variable.value", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);

		// ---- ユーザ任意情報 -----
		Property nodeCustomList =
				new Property(NodeConstant.CUSTOM_LIST, Messages.getString("node.custom.list", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);
		Property nodeCustom =
				new Property(NodeConstant.CUSTOM, Messages.getString("node.custom", locale), PropertyDefineConstant.EDITOR_TEXTAREA, DataRangeConstant.TEXT);


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
				new Property(NodeConstant.CLOUDRESOURCEID, Messages.getString("cloud.resource.id", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_256);
		//クラウドリソース名
		Property cloudResourceName =
				new Property(NodeConstant.CLOUDRESOURCENAME, Messages.getString("cloud.resource.name", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_128);
		//クラウドロケーション
		Property cloudLocation =
				new Property(NodeConstant.CLOUDLOCATION, Messages.getString("cloud.location", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		//クラウドログ優先度
		Property cloudLogPriority =
				new Property(NodeConstant.CLOUDLOGPRIORITY, Messages.getString("cloud.log.priority", locale),  PropertyDefineConstant.EDITOR_NUM, Integer.MAX_VALUE, 0);

		// ---- RPAツール情報 -----
		// RPAツール情報
		Property rpaTool =
				new Property(NodeConstant.RPA_TOOL, Messages.getString("rpa.tool.info", locale), PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		// ログ出力ディレクトリ
		Property rpaLogDir =
				new Property(NodeConstant.RPA_LOG_DIRECTORY, Messages.getString("rpa.log.directory", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_1024);


		// ---- RPA管理ツール情報 -----
		// RPA管理ツール情報
		Property rpaManagementTool =
				new Property(NodeConstant.RPA_MANAGEMENT_TOOL, Messages.getString("rpa.management.tool.info", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		// RPA管理ツールタイプ
		Property rpaManagementToolType =
				new Property(NodeConstant.RPA_MANAGEMENT_TOOL_TYPE, Messages.getString("rpa.management.tool.type", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_128);
		// RPAリソースID
		Property rpaResourceId =
				new Property(NodeConstant.RPA_RESOURCE_ID, Messages.getString("rpa.resource.id", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		// ユーザ名
		Property rpaUser =
				new Property(NodeConstant.RPA_USER, Messages.getString("rpa.user", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);
		// RPA実行環境ID
		Property rpaExecEnvId =
				new Property(NodeConstant.RPA_EXECUTION_ENVIRONMENT_ID, Messages.getString("rpa.exec.env.id", locale),  PropertyDefineConstant.EDITOR_TEXT, DataRangeConstant.VARCHAR_64);

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
		nodeName.setValue("");
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

		// ----- Hinemosエージェント情報 -----
		agent.setValue("");
		agentAwakePort.setValue("");

		// ---- 構成情報 -----
		nodeConfigInformation.setValue("");
		
		// ---- ホスト名 -----
		hostName.setValue("");

		// ---- OS情報 -----
		os.setValue("");
		osName.setValue("");
		osRelease.setValue("");
		osVersion.setValue("");
		characterSet.setValue("");
		osStartupDateTime.setValue("");
		
		// ---- デバイス情報 -----
		device.setValue("");

		// ---- 汎用デバイス情報 -----
		generalDevice.setValue("");
		generalDeviceList.setValue("");
		deviceDisplayName.setValue("");
		deviceName.setValue("");
		deviceIndex.setValue("");
		deviceType.setValue("");
		deviceSize.setValue(0L);
		deviceSizeUnit.setValue("");
		deviceDescription.setValue("");

		// ---- CPU情報 -----
		cpu.setValue("");
		cpuList.setValue("");
		cpuDeviceDisplayName.setValue("");
		cpuDeviceName.setValue("");
		cpuDeviceIndex.setValue("");
		cpuDeviceType.setValue(DeviceTypeConstant.DEVICE_CPU);
		cpuDeviceSize.setValue(0L);
		cpuDeviceSizeUnit.setValue("");
		cpuDeviceDescription.setValue("");
		cpuClockCount.setValue(0);
		cpuThreadCount.setValue(0);
		cpuCoreCount.setValue(0);

		// ---- MEM情報 -----
		memory.setValue("");
		memoryList.setValue("");
		memoryDeviceDisplayName.setValue("");
		memoryDeviceName.setValue("");
		memoryDeviceIndex.setValue("");
		memoryDeviceType.setValue(DeviceTypeConstant.DEVICE_MEM);
		memoryDeviceSize.setValue(0L);
		memoryDeviceSizeUnit.setValue("");
		memoryDeviceDescription.setValue("");

		// ---- NIC情報 -----
		networkInterface.setValue("");
		networkInterfaceList.setValue("");
		networkInterfaceDeviceDisplayName.setValue("");
		networkInterfaceDeviceName.setValue("");
		networkInterfaceDeviceIndex.setValue("");
		networkInterfaceDeviceType.setValue(DeviceTypeConstant.DEVICE_NIC);
		networkInterfaceDeviceSize.setValue(0L);
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
		diskDeviceSize.setValue(0L);
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
		fileSystemDeviceSize.setValue(0L);
		fileSystemDeviceSizeUnit.setValue("");
		fileSystemDeviceDescription.setValue("");
		fileSystemType.setValue("");


		// ---- ネットワーク接続 -----
		nodeNetstat.setValue("");
		nodeNetstatList.setValue("");
		netstatProtocol.setValue("");
		netstatLocalIpAddress.setValue("");
		netstatLocalPort.setValue("");
		netstatForeignIpAddress.setValue("");
		netstatForeignPort.setValue("");
		netstatProcessName.setValue("");
		netstatPid.setValue("");
		netstatStatus.setValue("");


		// ---- プロセス情報 -----
		process.setValue("");
		processList.setValue("");
		processName.setValue("");
		processPid.setValue("");
		processPath.setValue("");
		processExecUser.setValue("");
		processStartupDateTime.setValue("");


		// ---- パッケージ情報 -----
		nodePackage.setValue("");
		nodePackageList.setValue("");
		packageId.setValue("");
		packageName.setValue("");
		packageVersion.setValue("");
		packageRelease.setValue("");
		packageInstallDate.setValue("");
		packageVendor.setValue("");
		packageArchitecture.setValue("");


		// ---- 個別導入製品情報 -----
		nodeProduct.setValue("");
		nodeProductList.setValue("");
		productName.setValue("");
		productVersion.setValue("");
		productPath.setValue("");

		// ---- ライセンス情報 -----
		nodeLicense.setValue("");
		nodeLicenseList.setValue("");
		licenseProductName.setValue("");
		licenseVendor.setValue("");
		licenseVendorContact.setValue("");
		licenseSerialNumber.setValue("");
		licenseCount.setValue("");
		licenseExpirationDate.setValue("");
		
		// ---- ノード変数情報 -----
		nodeVariable.setValue("");
		generalNodeVariable.setValue("");
		nodeVariableName.setValue("");
		nodeVariableValues.setValue("");

		// ---- ユーザ任意情報 -----
		nodeCustomList.setValue("");
		nodeCustom.setValue("");

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
				{ "", SnmpProtocolConstant.MD5, SnmpProtocolConstant.SHA, SnmpProtocolConstant.SHA224, SnmpProtocolConstant.SHA256, SnmpProtocolConstant.SHA384, SnmpProtocolConstant.SHA512 },
				{ "", SnmpProtocolConstant.MD5, SnmpProtocolConstant.SHA, SnmpProtocolConstant.SHA224, SnmpProtocolConstant.SHA256, SnmpProtocolConstant.SHA384, SnmpProtocolConstant.SHA512 },
		};
		Object snmpPrivProtocolValue[][] = {
				{ "", SnmpProtocolConstant.DES, SnmpProtocolConstant.AES, SnmpProtocolConstant.AES192, SnmpProtocolConstant.AES256 },
				{ "", SnmpProtocolConstant.DES, SnmpProtocolConstant.AES, SnmpProtocolConstant.AES192, SnmpProtocolConstant.AES256 },
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
				{"", WbemProtocolConstant.HTTP, WbemProtocolConstant.HTTPS},
				{"", WbemProtocolConstant.HTTP, WbemProtocolConstant.HTTPS},
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
				{"", WinrmProtocolConstant.HTTP, WinrmProtocolConstant.HTTPS},
				{"", WinrmProtocolConstant.HTTP, WinrmProtocolConstant.HTTPS},
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

		// ---- クラウド・仮想化管理情報 -----
		cloudManagement.setValue("");
		cloudService.setValue("");
		cloudScope.setValue("");
		cloudResourceType.setValue("");
		cloudResourceId.setValue("");
		cloudResourceName.setValue("");
		cloudLocation.setValue("");
		cloudLogPriority.setValue(16);

		// ---- RPAツール情報 -----
		rpaTool.setValue("");
		rpaLogDir.setValue("");

		// ---- RPA管理ツール情報 -----
		rpaManagementTool.setValue("");
		rpaManagementToolType.setValue("");
		rpaResourceId.setValue("");
		rpaUser.setValue("");
		rpaExecEnvId.setValue("");

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
			nodeName.setModify(PropertyDefineConstant.MODIFY_OK);
			iconImage.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- IPアドレス情報 -----
			ipAddressVersion.setModify(PropertyDefineConstant.MODIFY_OK);
			ipAddressV4.setModify(PropertyDefineConstant.MODIFY_OK);
			ipAddressV6.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- 構成情報 -----
			
			// ---- ホスト名 -----
			hostName.setModify(PropertyDefineConstant.MODIFY_OK);
			hostName.setCopy(PropertyDefineConstant.COPY_OK);

			// ---- OS情報 -----
			osName.setModify(PropertyDefineConstant.MODIFY_OK);
			osRelease.setModify(PropertyDefineConstant.MODIFY_OK);
			osVersion.setModify(PropertyDefineConstant.MODIFY_OK);
			characterSet.setModify(PropertyDefineConstant.MODIFY_OK);
			osStartupDateTime.setModify(PropertyDefineConstant.MODIFY_OK);
			
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
			cpuCoreCount.setModify(PropertyDefineConstant.MODIFY_OK);
			cpuThreadCount.setModify(PropertyDefineConstant.MODIFY_OK);
			cpuClockCount.setModify(PropertyDefineConstant.MODIFY_OK);

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
			fileSystemDeviceSize.setModify(PropertyDefineConstant.MODIFY_OK);
			fileSystemDeviceSizeUnit.setModify(PropertyDefineConstant.MODIFY_OK);
			fileSystemDeviceDescription.setModify(PropertyDefineConstant.MODIFY_OK);
			fileSystemType.setModify(PropertyDefineConstant.MODIFY_OK);


			// ---- ネットワーク接続 -----
			nodeNetstat.setCopy(PropertyDefineConstant.COPY_OK);
			netstatProtocol.setModify(PropertyDefineConstant.MODIFY_OK);
			netstatLocalIpAddress.setModify(PropertyDefineConstant.MODIFY_OK);
			netstatLocalPort.setModify(PropertyDefineConstant.MODIFY_OK);
			netstatForeignIpAddress.setModify(PropertyDefineConstant.MODIFY_OK);
			netstatForeignPort.setModify(PropertyDefineConstant.MODIFY_OK);
			netstatProcessName.setModify(PropertyDefineConstant.MODIFY_OK);
			netstatPid.setModify(PropertyDefineConstant.MODIFY_OK);
			netstatStatus.setModify(PropertyDefineConstant.MODIFY_OK);


			// ---- プロセス情報 -----
			process.setCopy(PropertyDefineConstant.COPY_OK);
			processName.setModify(PropertyDefineConstant.MODIFY_OK);
			processPid.setModify(PropertyDefineConstant.MODIFY_OK);
			processPath.setModify(PropertyDefineConstant.MODIFY_OK);
			processExecUser.setModify(PropertyDefineConstant.MODIFY_OK);
			processStartupDateTime.setModify(PropertyDefineConstant.MODIFY_OK);


			// ---- パッケージ情報 -----
			nodePackage.setCopy(PropertyDefineConstant.COPY_OK);
			packageId.setModify(PropertyDefineConstant.MODIFY_OK);
			packageName.setModify(PropertyDefineConstant.MODIFY_OK);
			packageVersion.setModify(PropertyDefineConstant.MODIFY_OK);
			packageRelease.setModify(PropertyDefineConstant.MODIFY_OK);
			packageInstallDate.setModify(PropertyDefineConstant.MODIFY_OK);
			packageVendor.setModify(PropertyDefineConstant.MODIFY_OK);
			packageArchitecture.setModify(PropertyDefineConstant.MODIFY_OK);


			// ---- 個別導入支援情報 -----
			nodeProduct.setCopy(PropertyDefineConstant.COPY_OK);
			productName.setModify(PropertyDefineConstant.MODIFY_OK);
			productVersion.setModify(PropertyDefineConstant.MODIFY_OK);
			productPath.setModify(PropertyDefineConstant.MODIFY_OK);


			// ---- ライセンス情報 -----
			nodeLicense.setCopy(PropertyDefineConstant.COPY_OK);
			licenseProductName.setModify(PropertyDefineConstant.MODIFY_OK);
			licenseVendor.setModify(PropertyDefineConstant.MODIFY_OK);
			licenseVendorContact.setModify(PropertyDefineConstant.MODIFY_OK);
			licenseSerialNumber.setModify(PropertyDefineConstant.MODIFY_OK);
			licenseCount.setModify(PropertyDefineConstant.MODIFY_OK);
			licenseExpirationDate.setModify(PropertyDefineConstant.MODIFY_OK);


			// ---- ノード変数情報 -----
			generalNodeVariable.setCopy(PropertyDefineConstant.COPY_OK);
			nodeVariableName.setModify(PropertyDefineConstant.MODIFY_OK);
			nodeVariableValues.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- ユーザ任意情報 -----
			nodeCustom.setModify(PropertyDefineConstant.MODIFY_NG);
			nodeCustom.setCopy(PropertyDefineConstant.COPY_NG);

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


			// ---- クラウド管理情報 -----
			cloudService.setModify(PropertyDefineConstant.MODIFY_OK);
			cloudScope.setModify(PropertyDefineConstant.MODIFY_OK);
			cloudResourceType.setModify(PropertyDefineConstant.MODIFY_OK);
			cloudResourceId.setModify(PropertyDefineConstant.MODIFY_OK);
			cloudResourceName.setModify(PropertyDefineConstant.MODIFY_OK);
			cloudLocation.setModify(PropertyDefineConstant.MODIFY_OK);
			cloudLogPriority.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- RPAツール情報 -----
			rpaLogDir.setModify(PropertyDefineConstant.MODIFY_OK);

			// ---- RPA管理ツール情報 -----
			rpaManagementToolType.setModify(PropertyDefineConstant.MODIFY_OK);
			rpaResourceId.setModify(PropertyDefineConstant.MODIFY_OK);
			rpaUser.setModify(PropertyDefineConstant.MODIFY_OK);
			rpaExecEnvId.setModify(PropertyDefineConstant.MODIFY_OK);


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

		if (!isNodeMap) {
			//ファシリティ情報
			property.addChildren(facilityId);
			property.addChildren(facilityName);
			property.addChildren(description);
			property.addChildren(valid);
			property.addChildren(autoDeviceSearch);
	
			//基本情報
			property.addChildren(basicInformation);
		}

		if (!isNodeMap) {

			//構成情報
			property.addChildren(nodeConfigInformation);

			//ジョブ
			property.addChildren(job);
			//サービス
			property.addChildren(service);
			//クラウド・仮想化管理
			property.addChildren(cloudManagement);
			//RPAツール情報
			property.addChildren(rpaTool);
			//RPA管理ツール情報
			property.addChildren(rpaManagementTool);
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
			network.addChildren(nodeName);
	
			// Hinemosエージェントツリー
			agent.removeChildren();
			agent.addChildren(agentAwakePort);

			// 構成情報ツリー
			nodeConfigInformation.removeChildren();
			nodeConfigInformation.addChildren(hostName);
			nodeConfigInformation.addChildren(os);
			nodeConfigInformation.addChildren(device);
			nodeConfigInformation.addChildren(nodeNetstatList);
			nodeConfigInformation.addChildren(processList);
			nodeConfigInformation.addChildren(nodePackageList);
			nodeConfigInformation.addChildren(nodeProductList);
			nodeConfigInformation.addChildren(nodeLicenseList);
			nodeConfigInformation.addChildren(nodeCustomList);
			nodeConfigInformation.addChildren(nodeVariable);
		} else {

			// 構成情報ツリー
			property.addChildren(hostName);
			property.addChildren(os);
			property.addChildren(device);
			property.addChildren(nodeNetstatList);
			property.addChildren(processList);
			property.addChildren(nodePackageList);
			property.addChildren(nodeProductList);
			property.addChildren(nodeLicenseList);
			property.addChildren(nodeCustomList);
			property.addChildren(nodeVariable);			
		}

		// OSツリー
		os.removeChildren();
		os.addChildren(osName);
		os.addChildren(osRelease);
		os.addChildren(osVersion);
		os.addChildren(characterSet);
		os.addChildren(osStartupDateTime);

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
		cpu.addChildren(cpuCoreCount);
		cpu.addChildren(cpuThreadCount);
		cpu.addChildren(cpuClockCount);

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

		// ---- ネットワーク接続ツリー
		nodeNetstatList.removeChildren();
		nodeNetstatList.addChildren(nodeNetstat);
		nodeNetstat.removeChildren();
		nodeNetstat.addChildren(netstatProtocol);
		nodeNetstat.addChildren(netstatLocalIpAddress);
		nodeNetstat.addChildren(netstatLocalPort);
		nodeNetstat.addChildren(netstatForeignIpAddress);
		nodeNetstat.addChildren(netstatForeignPort);
		nodeNetstat.addChildren(netstatProcessName);
		nodeNetstat.addChildren(netstatPid);
		nodeNetstat.addChildren(netstatStatus);

		// ---- プロセス情報ツリー
		processList.removeChildren();
		processList.addChildren(process);
		process.removeChildren();
		process.addChildren(processName);
		process.addChildren(processPid);
		process.addChildren(processPath);
		process.addChildren(processExecUser);
		process.addChildren(processStartupDateTime);

		// ---- パッケージ情報ツリー
		nodePackageList.removeChildren();
		nodePackageList.addChildren(nodePackage);
		nodePackage.removeChildren();
		nodePackage.addChildren(packageId);
		nodePackage.addChildren(packageName);
		nodePackage.addChildren(packageVersion);
		nodePackage.addChildren(packageRelease);
		nodePackage.addChildren(packageInstallDate);
		nodePackage.addChildren(packageVendor);
		nodePackage.addChildren(packageArchitecture);

		// ---- 個別導入支援情報ツリー
		nodeProductList.removeChildren();
		nodeProductList.addChildren(nodeProduct);
		nodeProduct.removeChildren();
		nodeProduct.addChildren(productName);
		nodeProduct.addChildren(productVersion);
		nodeProduct.addChildren(productPath);

		// ---- ライセンス情報ツリー
		nodeLicenseList.removeChildren();
		nodeLicenseList.addChildren(nodeLicense);
		nodeLicense.removeChildren();
		nodeLicense.addChildren(licenseProductName);
		nodeLicense.addChildren(licenseVendor);
		nodeLicense.addChildren(licenseVendorContact);
		nodeLicense.addChildren(licenseSerialNumber);
		nodeLicense.addChildren(licenseCount);
		nodeLicense.addChildren(licenseExpirationDate);

		// ---- ノード変数情報ツリー
		nodeVariable.removeChildren();
		nodeVariable.addChildren(generalNodeVariable);
		generalNodeVariable.addChildren(nodeVariableName);
		generalNodeVariable.addChildren(nodeVariableValues);

		// ---- ユーザ任意情報ツリー
		nodeCustomList.removeChildren();
		nodeCustomList.addChildren(nodeCustom);

		if (!isNodeMap) {
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

			// RPAツール情報ツリー
			rpaTool.removeChildren();
			rpaTool.addChildren(rpaLogDir);
			
			// RPA管理ツール情報ツリー
			rpaManagementTool.removeChildren();
			rpaManagementTool.addChildren(rpaManagementToolType);
			rpaManagementTool.addChildren(rpaResourceId);
			rpaManagementTool.addChildren(rpaUser);
			rpaManagementTool.addChildren(rpaExecEnvId);
			
	
			// ---- クラウド・仮想化管理情報ツリー
			cloudManagement.removeChildren();
			cloudManagement.addChildren(cloudService);
			cloudManagement.addChildren(cloudScope);
			cloudManagement.addChildren(cloudResourceType);
			cloudManagement.addChildren(cloudResourceId);
			cloudManagement.addChildren(cloudResourceName);
			cloudManagement.addChildren(cloudLocation);
			cloudManagement.addChildren(cloudLogPriority);
			
			// ---- 保守情報ツリー
			maintenance.removeChildren();
			maintenance.addChildren(administrator);
			maintenance.addChildren(contact);
		}

		return property;
	}

	/**
	 * TODO HinemosManagerのNodeInfoの値と揃えておくこと。!
	 * @param nodeInfo
	 */
	public static void setDefaultNode(NodeInfoResponse nodeInfo) {
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
		if (nodeInfo.getValid() == null) {
			nodeInfo.setValid(Boolean.TRUE);
		}
		if (nodeInfo.getAutoDeviceSearch() == null) {
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
			nodeInfo.setIpAddressVersion(NodeInfoResponse.IpAddressVersionEnum.IPV4);
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
		if (nodeInfo.getNodeOsInfo() == null) {
			NodeOsInfoResponse nodeOsInfo = new NodeOsInfoResponse();
			nodeInfo.setNodeOsInfo(nodeOsInfo);
		}
		if (nodeInfo.getNodeOsInfo().getOsName() == null) {
			nodeInfo.getNodeOsInfo().setOsName("");
		}
		if (nodeInfo.getNodeOsInfo().getOsRelease() == null) {
			nodeInfo.getNodeOsInfo().setOsRelease("");
		}
		if (nodeInfo.getNodeOsInfo().getOsVersion() == null) {
			nodeInfo.getNodeOsInfo().setOsVersion("");
		}
		if (nodeInfo.getNodeOsInfo().getCharacterSet() == null) {
			nodeInfo.getNodeOsInfo().setCharacterSet("");
		}
		Long longDate = null;
		try {
			longDate = TimezoneUtil.getSimpleDateFormat().parse(nodeInfo.getNodeOsInfo().getStartupDateTime()).getTime();
		} catch (Exception e) {
			//findbugs対応 エラーは発生しない想定なので本来不要だが Exception無視と思われないようtraceログの出力を追加
			m_log.trace("setDefaultNode : exception occuered",e);
		}
		if (nodeInfo.getNodeOsInfo().getStartupDateTime() == null || longDate <= 0) {
			nodeInfo.getNodeOsInfo().setStartupDateTime(null);
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

		// RPA
		if (nodeInfo.getRpaLogDir() == null) {
			nodeInfo.setRpaLogDir("");
		}
		
		if (nodeInfo.getRpaManagementToolType() == null) {
			nodeInfo.setRpaManagementToolType("");
		}
		
		if (nodeInfo.getRpaResourceId() == null) {
			nodeInfo.setRpaResourceId("");
		}
		
		if (nodeInfo.getRpaUser() == null) {
			nodeInfo.setRpaUser("");
		}
		
		if (nodeInfo.getRpaExecEnvId() == null) {
			nodeInfo.setRpaExecEnvId("");
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
			nodeInfo.setSnmpVersion(NodeInfoResponse.SnmpVersionEnum.V2);
		}
		if (nodeInfo.getSnmpSecurityLevel() == null) {
			nodeInfo.setSnmpSecurityLevel(NodeInfoResponse.SnmpSecurityLevelEnum.NOAUTH_NOPRIV);
		}
		if (nodeInfo.getSnmpAuthProtocol() == null) {
			nodeInfo.setSnmpAuthProtocol(null);
		}
		if (nodeInfo.getSnmpPrivProtocol() == null) {
			nodeInfo.setSnmpPrivProtocol(null);
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
		if (nodeInfo.getWbemProtocol() == null) {
			nodeInfo.setWbemProtocol(NodeInfoResponse.WbemProtocolEnum.HTTP);
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
		if (nodeInfo.getWinrmProtocol() == null) {
			nodeInfo.setWinrmProtocol(NodeInfoResponse.WinrmProtocolEnum.HTTP);
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
		if (nodeInfo.getCloudLogPriority() == null) {
			nodeInfo.setCloudLogPriority(16);
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
		List<RepositoryTableInfoResponse> platforms = null;
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
			RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(managerName);
			platforms = wrapper.getPlatformList();

			if (platforms != null) {
				for (RepositoryTableInfoResponse platform : platforms) {
					platformIdList.add(platform.getId());
					platformNameList.add(platform.getName() + "(" + platform.getId() + ")");
				}
			}

			table[PropertyDefineConstant.SELECT_VALUE] = platformIdList.toArray();
			table[PropertyDefineConstant.SELECT_DISP_TEXT] = platformNameList.toArray();
		} catch (InvalidRole e) {
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
	private static Object[][] getSubPlatformNames(String managerName) {
		// キャッシュが存在する場合はキャッシュを返す
		if(getInstance().subPlatformCache != null){
			return getInstance().subPlatformCache;
		}
		/** ローカル変数 */
		Object[][] table = null;
		List<RepositoryTableInfoResponse> subPlatforms = null;
		ArrayList<String> subPlatformIdList = new ArrayList<String>();
		ArrayList<String> subPlatformNameList = new ArrayList<String>();

		/** メイン処理 */
		try {
			table = new Object[2][subPlatformIdList.size()];
			if (managerName == null) {
				return table;
			}
			
			RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(managerName);
			subPlatforms = wrapper.getCollectorSubPlatformTableInfoList();

			subPlatformIdList.add("");
			subPlatformNameList.add("");
			if (subPlatforms != null) {
				for (RepositoryTableInfoResponse subPlatform : subPlatforms) {
					subPlatformIdList.add(subPlatform.getId());
					subPlatformNameList.add(subPlatform.getName() + "(" + subPlatform.getId() + ")");
				}
			}

			table[PropertyDefineConstant.SELECT_VALUE] = subPlatformIdList.toArray();
			table[PropertyDefineConstant.SELECT_DISP_TEXT] = subPlatformNameList.toArray();
		} catch (InvalidRole e) {
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
