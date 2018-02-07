/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.util;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.SnmpProtocolConstant;
import com.clustercontrol.bean.SnmpSecurityLevelConstant;
import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.bean.FacilityIdConstant;
import com.clustercontrol.repository.factory.NodeProperty;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.NodeCpuInfo;
import com.clustercontrol.repository.model.NodeDeviceInfo;
import com.clustercontrol.repository.model.NodeDeviceInfoPK;
import com.clustercontrol.repository.model.NodeDiskInfo;
import com.clustercontrol.repository.model.NodeFilesystemInfo;
import com.clustercontrol.repository.model.NodeHostnameInfo;
import com.clustercontrol.repository.model.NodeHostnameInfoPK;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.NodeMemoryInfo;
import com.clustercontrol.repository.model.NodeNetworkInterfaceInfo;
import com.clustercontrol.repository.model.NodeVariableInfo;
import com.clustercontrol.repository.model.NodeVariableInfoPK;
import com.clustercontrol.repository.model.ScopeInfo;
import com.clustercontrol.util.MessageConstant;

/**
 * リポジトリ管理の入力チェッククラス
 *
 *
 */
public class RepositoryValidator {

	private static Log m_log = LogFactory.getLog(RepositoryValidator.class);

	public static void validateIpv4(String ipv4) throws InvalidSetting {
		
		if (!ipv4.matches("\\d{1,3}?\\.\\d{1,3}?\\.\\d{1,3}?\\.\\d{1,3}?")){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_IPV4_CORRECT_FORMAT.getMessage() + "(" + ipv4 + ")");
			m_log.info("validateNodeInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		String[] ipv4Array = ipv4.split("\\.");
		if (ipv4Array.length != 4) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_IPV4_CORRECT_FORMAT.getMessage() + "(" + ipv4 + ")");
			m_log.info("validateNodeInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		for (int i = 0; i < 4; i ++) {
			int j = Integer.parseInt(ipv4Array[i]);
			if (j < 0 || 255 < j) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_IPV4_CORRECT_FORMAT.getMessage() + "(" + ipv4 + ")");
				m_log.info("validateNodeInfo() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}
		
	}
	
	public static void validateNodeInfo(NodeInfo nodeInfo) throws InvalidSetting{

		validateFacilityInfo(nodeInfo);

		// hardware
		CommonValidator.validateString(MessageConstant.HARDWARE_TYPE.getMessage(), nodeInfo.getHardwareType(), false, 0, 128);
		// platformFamily
		CommonValidator.validateString(MessageConstant.PLATFORM_FAMILY_NAME.getMessage(), nodeInfo.getPlatformFamily(), true, 1, 128);
		// subPlatformFamily
		CommonValidator.validateString(MessageConstant.SUB_PLATFORM_FAMILY_NAME.getMessage(), nodeInfo.getSubPlatformFamily(), true, 0, 128);

		try {
			QueryUtil.getCollectorPlatformMstPK(nodeInfo.getPlatformFamily());
		} catch (FacilityNotFound e) {
			throw new InvalidSetting("platform " + nodeInfo.getPlatformFamily() + " does not exist!");
		}

		// facilityType
		if(nodeInfo.getFacilityType() != FacilityConstant.TYPE_NODE){
			InvalidSetting e = new InvalidSetting("Node FacilityType is  " + FacilityConstant.TYPE_NODE);
			m_log.info("validateNodeInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		//IPアドレスの入力チェック
		if(nodeInfo.getIpAddressVersion() == null){
			InvalidSetting e = new InvalidSetting("IpAddressVersion is null.");
			m_log.info("validateNodeInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		int ipaddressVersion = nodeInfo.getIpAddressVersion().intValue();
		if(ipaddressVersion == 4){
			//versionが空か4の場合には、
			if(nodeInfo.getIpAddressV4() == null || "".equals(nodeInfo.getIpAddressV4())){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_IPV4_CORRECT_FORMAT.getMessage());
				m_log.info("validateNodeInfo() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}

			// ipv4形式チェック
			try{
				InetAddress address = InetAddress.getByName(nodeInfo.getIpAddressV4());
				if (address instanceof Inet4Address){
					//IPv4の場合はさらにStringをチェック
					validateIpv4(nodeInfo.getIpAddressV4());
				} else{
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_IPV4_CORRECT_FORMAT.getMessage() + "(" + nodeInfo.getIpAddressV4() + ")");
					m_log.info("validateNodeInfo() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}catch (UnknownHostException e) {
				InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_IPV4_CORRECT_FORMAT.getMessage() + "(" + nodeInfo.getIpAddressV4() + ")");
				m_log.info("validateNodeInfo() : "
						+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
				throw e1;
			}
		}
		else if(ipaddressVersion == 6){
			//	versionが6の場合には、
			if(nodeInfo.getIpAddressV6() == null || "".equals(nodeInfo.getIpAddressV6())){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_IPV6_CORRECT_FORMAT.getMessage());
				m_log.info("validateNodeInfo() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}

			// ipv6形式チェック
			try{
				InetAddress address = InetAddress.getByName(nodeInfo.getIpAddressV6());
				if (address instanceof Inet6Address){
				} else{
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_IPV6_CORRECT_FORMAT.getMessage() + "(" + nodeInfo.getIpAddressV6() + ")");
					m_log.info("validateNodeInfo() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			} catch (UnknownHostException e) {
				InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_IPV6_CORRECT_FORMAT.getMessage() + "(" + nodeInfo.getIpAddressV6() + ")");
				m_log.info("validateNodeInfo() : "
						+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
				throw e1;
			}
		}
		else{
			InvalidSetting e = new InvalidSetting("IpAddressVersion is not 4 / 6.");
			m_log.info("validateNodeInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		//ノード名の入力チェック
		CommonValidator.validateString(MessageConstant.NODE_NAME.getMessage(), nodeInfo.getNodeName(), true, 1, 128);

		// クラウド管理->クラウドサービス
		CommonValidator.validateString(MessageConstant.CLOUD_SERVICE.getMessage(), nodeInfo.getCloudService(), false, 0, 64);
		// クラウド管理->クラウドアカウントリソース
		CommonValidator.validateString(MessageConstant.CLOUD_SCOPE.getMessage(), nodeInfo.getCloudScope(), false, 0, 64);
		// クラウド管理->クラウドリソースタイプ
		CommonValidator.validateString(MessageConstant.CLOUD_RESOURCE_TYPE.getMessage(), nodeInfo.getCloudResourceType(), false, 0, 64);
		// クラウド管理->クラウドリソースID
		CommonValidator.validateString(MessageConstant.CLOUD_RESOURCE_ID.getMessage(), nodeInfo.getCloudResourceId(), false, 0, 64);
		// クラウド管理->クラウドリソース名
		CommonValidator.validateString(MessageConstant.CLOUD_RESOURCE_NAME.getMessage(), nodeInfo.getCloudResourceName(), false, 0, 64);
		// クラウド管理->クラウドロケーション
		CommonValidator.validateString(MessageConstant.CLOUD_LOCATION.getMessage(), nodeInfo.getCloudLocation(), false, 0, 64);

		// OS名
		CommonValidator.validateString(MessageConstant.OS_NAME.getMessage(), nodeInfo.getOsName(), false, 0, 256);
		// OSリリース
		CommonValidator.validateString(MessageConstant.OS_RELEASE.getMessage(), nodeInfo.getOsRelease(), false, 0, 256);
		// OSバージョン
		CommonValidator.validateString(MessageConstant.OS_VERSION.getMessage(), nodeInfo.getOsVersion(), false, 0, 256);
		// 文字セット
		CommonValidator.validateString(MessageConstant.CHARACTER_SET.getMessage(), nodeInfo.getCharacterSet(), false, 0, 16);

		//デバイスの入力チェック
		if(nodeInfo.getNodeCpuInfo() != null){
			String DeviceTypeName = MessageConstant.CPU.getMessage();
			List<NodeDeviceInfoPK> pkList = new ArrayList<NodeDeviceInfoPK>();
			for(NodeCpuInfo info : nodeInfo.getNodeCpuInfo()){
				CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_NAME.getMessage() + "]", info.getDeviceName(), true, 1, 128);
				CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_TYPE.getMessage() + "]", info.getDeviceType(), true, 1, 32);
				CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_DISPLAY_NAME.getMessage() + "]", info.getDeviceDisplayName(), true, 1, 256);
				CommonValidator.validateInt(DeviceTypeName + "[" + MessageConstant.DEVICE_INDEX.getMessage() + "]", info.getDeviceIndex(), 0, Integer.MAX_VALUE);

				// 重複チェック
				// JPAリレーションでのDeleteInsertがうまくいかないので、
				// インデックスを持っていないデバイス情報は手動で重複チェックする。
				NodeDeviceInfoPK entityPk = new NodeDeviceInfoPK(
						nodeInfo.getFacilityId(),
						info.getDeviceIndex(),
						info.getDeviceType(),
						info.getDeviceName());
				if (pkList.contains(entityPk)) {
					String[] args = { MessageConstant.CPU.getMessage(), info.getDeviceName()};
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(args));
					m_log.info("validateNodeInfo() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				pkList.add(entityPk);
			}
		}
		if(nodeInfo.getNodeMemoryInfo() != null){
			String DeviceTypeName = MessageConstant.MEMORY.getMessage();
			List<NodeDeviceInfoPK> pkList = new ArrayList<NodeDeviceInfoPK>();
			for(NodeMemoryInfo info : nodeInfo.getNodeMemoryInfo()){
				CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_NAME.getMessage() + "]", info.getDeviceName(), true, 1, 128);
				CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_TYPE.getMessage() + "]", info.getDeviceType(), true, 1, 32);
				CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_DISPLAY_NAME.getMessage() + "]", info.getDeviceDisplayName(), true, 1, 256);
				CommonValidator.validateInt(DeviceTypeName + "[" + MessageConstant.DEVICE_INDEX.getMessage() + "]", info.getDeviceIndex(), 0, Integer.MAX_VALUE);

				// 重複チェック
				// JPAリレーションでのDeleteInsertがうまくいかないので、
				// インデックスを持っていないデバイス情報は手動で重複チェックする。
				NodeDeviceInfoPK entityPk = new NodeDeviceInfoPK(
						nodeInfo.getFacilityId(),
						info.getDeviceIndex(),
						info.getDeviceType(),
						info.getDeviceName());
				if (pkList.contains(entityPk)) {
					String[] args = { MessageConstant.MEMORY.getMessage(), info.getDeviceName()};
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(args));
					m_log.info("validateNodeInfo() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				pkList.add(entityPk);
			}
		}
		if(nodeInfo.getNodeDiskInfo() != null){
			String DeviceTypeName = MessageConstant.DISK.getMessage();
			List<NodeDeviceInfoPK> pkList = new ArrayList<NodeDeviceInfoPK>();
			for(NodeDiskInfo info : nodeInfo.getNodeDiskInfo()){
				CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_NAME.getMessage() + "]", info.getDeviceName(), true, 1, 128);
				CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_TYPE.getMessage() + "]", info.getDeviceType(), true, 1, 32);
				CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_DISPLAY_NAME.getMessage() + "]", info.getDeviceDisplayName(), true, 1, 256);
				CommonValidator.validateInt(DeviceTypeName + "[" + MessageConstant.DEVICE_INDEX.getMessage() + "]", info.getDeviceIndex(), 0, Integer.MAX_VALUE);

				// 重複チェック
				// JPAリレーションでのDeleteInsertがうまくいかないので、
				// インデックスを持っていないデバイス情報は手動で重複チェックする。
				NodeDeviceInfoPK entityPk = new NodeDeviceInfoPK(
						nodeInfo.getFacilityId(),
						info.getDeviceIndex(),
						info.getDeviceType(),
						info.getDeviceName());
				if (pkList.contains(entityPk)) {
					String[] args = { MessageConstant.DISK.getMessage(), info.getDeviceName()};
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(args));
					m_log.info("validateNodeInfo() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				pkList.add(entityPk);
			}
		}
		if(nodeInfo.getNodeNetworkInterfaceInfo() != null){
			String DeviceTypeName = MessageConstant.NETWORK_INTERFACE.getMessage();
			List<NodeDeviceInfoPK> pkList = new ArrayList<NodeDeviceInfoPK>();
			for(NodeNetworkInterfaceInfo info : nodeInfo.getNodeNetworkInterfaceInfo()){
				CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_NAME.getMessage() + "]", info.getDeviceName(), true, 1, 128);
				CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_TYPE.getMessage() + "]", info.getDeviceType(), true, 1, 32);
				CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_DISPLAY_NAME.getMessage() + "]", info.getDeviceDisplayName(), true, 1, 256);
				CommonValidator.validateInt(DeviceTypeName + "[" + MessageConstant.DEVICE_INDEX.getMessage() + "]", info.getDeviceIndex(), 0, Integer.MAX_VALUE);

				// 重複チェック
				// JPAリレーションでのDeleteInsertがうまくいかないので、
				// インデックスを持っていないデバイス情報は手動で重複チェックする。
				NodeDeviceInfoPK entityPk = new NodeDeviceInfoPK(
						nodeInfo.getFacilityId(),
						info.getDeviceIndex(),
						info.getDeviceType(),
						info.getDeviceName());
				if (pkList.contains(entityPk)) {
					String[] args = { MessageConstant.NETWORK_INTERFACE.getMessage(), info.getDeviceName()};
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(args));
					m_log.info("validateNodeInfo() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				pkList.add(entityPk);
			}
		}
		if(nodeInfo.getNodeFilesystemInfo() != null){
			String DeviceTypeName = MessageConstant.FILE_SYSTEM.getMessage();
			List<NodeDeviceInfoPK> pkList = new ArrayList<NodeDeviceInfoPK>();
			for(NodeFilesystemInfo info : nodeInfo.getNodeFilesystemInfo()){
				CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_NAME.getMessage() + "]", info.getDeviceName(), true, 1, 128);
				CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_TYPE.getMessage() + "]", info.getDeviceType(), true, 1, 32);
				CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_DISPLAY_NAME.getMessage() + "]", info.getDeviceDisplayName(), true, 1, 256);
				CommonValidator.validateInt(DeviceTypeName + "[" + MessageConstant.DEVICE_INDEX.getMessage() + "]", info.getDeviceIndex(), 0, Integer.MAX_VALUE);

				// 重複チェック
				// JPAリレーションでのDeleteInsertがうまくいかないので、
				// インデックスを持っていないデバイス情報は手動で重複チェックする。
				NodeDeviceInfoPK entityPk = new NodeDeviceInfoPK(
						nodeInfo.getFacilityId(),
						info.getDeviceIndex(),
						info.getDeviceType(),
						info.getDeviceName());
				if (pkList.contains(entityPk)) {
					String[] args = { MessageConstant.FILE_SYSTEM.getMessage(), info.getDeviceName()};
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(args));
					m_log.info("validateNodeInfo() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				pkList.add(entityPk);
			}
		}
		if(nodeInfo.getNodeDeviceInfo() != null){
			String DeviceTypeName = MessageConstant.GENERAL_DEVICE.getMessage();
			List<NodeDeviceInfoPK> pkList = new ArrayList<NodeDeviceInfoPK>();
			for(NodeDeviceInfo info : nodeInfo.getNodeDeviceInfo()){
				CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_NAME.getMessage() + "]", info.getDeviceName(), true, 1, 128);
				CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_TYPE.getMessage() + "]", info.getDeviceType(), true, 1, 32);
				CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_DISPLAY_NAME.getMessage() + "]", info.getDeviceDisplayName(), true, 1, 256);
				CommonValidator.validateInt(DeviceTypeName + "[" + MessageConstant.DEVICE_INDEX.getMessage() + "]", info.getDeviceIndex(), 0, Integer.MAX_VALUE);

				// 重複チェック
				// JPAリレーションでのDeleteInsertがうまくいかないので、
				// インデックスを持っていないデバイス情報は手動で重複チェックする。
				NodeDeviceInfoPK entityPk = new NodeDeviceInfoPK(
						nodeInfo.getFacilityId(),
						info.getDeviceIndex(),
						info.getDeviceType(),
						info.getDeviceName());
				if (pkList.contains(entityPk)) {
					String[] args = { MessageConstant.GENERAL_DEVICE.getMessage(), info.getDeviceName()};
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(args));
					m_log.info("validateNodeInfo() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				pkList.add(entityPk);
			}
		}
		if(nodeInfo.getNodeHostnameInfo() != null){
			List<NodeHostnameInfoPK> pkList = new ArrayList<NodeHostnameInfoPK>();
			for(NodeHostnameInfo info : nodeInfo.getNodeHostnameInfo()){
				CommonValidator.validateString(MessageConstant.HOST_NAME.getMessage(), info.getHostname(), false, 0, 128);

				// 重複チェック
				// JPAリレーションでのDeleteInsertがうまくいかないので、
				// インデックスを持っていないデバイス情報は手動で重複チェックする。
				NodeHostnameInfoPK entityPk = new NodeHostnameInfoPK(nodeInfo.getFacilityId(), info.getHostname());
				if (pkList.contains(entityPk)) {
					String[] args = { MessageConstant.HOST_NAME.getMessage(), info.getHostname()};
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(args));
					m_log.info("validateNodeInfo() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				pkList.add(entityPk);
			}
		}
		if(nodeInfo.getNodeVariableInfo() != null){
			List<NodeVariableInfoPK> pkList = new ArrayList<NodeVariableInfoPK>();
			for(NodeVariableInfo variable : nodeInfo.getNodeVariableInfo()){
				// 重複チェック
				// JPAリレーションでのDeleteInsertがうまくいかないので、
				// インデックスを持っていないデバイス情報は手動で重複チェックする。
				NodeVariableInfoPK entityPk = new NodeVariableInfoPK(nodeInfo.getFacilityId(), variable.getNodeVariableName());
				if (pkList.contains(entityPk)) {
					String[] args = { MessageConstant.NODE_VARIABLE.getMessage(), variable.getNodeVariableName()};
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(args));
					m_log.info("validateNodeInfo() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				pkList.add(entityPk);
			}
		}

		//サービスのチェック(SNMP)
		CommonValidator.validateString(MessageConstant.COMMUNITY_NAME.getMessage(), nodeInfo.getSnmpCommunity(), false, 0, 64);
		if(nodeInfo.getSnmpVersion() == null || 
				(SnmpVersionConstant.TYPE_V1 != nodeInfo.getSnmpVersion()
				&& SnmpVersionConstant.TYPE_V2 != nodeInfo.getSnmpVersion()
				&& SnmpVersionConstant.TYPE_V3 != nodeInfo.getSnmpVersion())
			){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SNMP_VERSION.getMessage());
			m_log.info("validateNodeInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		if(SnmpVersionConstant.TYPE_V3 == nodeInfo.getSnmpVersion() &&
				(SnmpSecurityLevelConstant.AUTH_NOPRIV.equals(nodeInfo.getSnmpSecurityLevel())
					|| SnmpSecurityLevelConstant.AUTH_PRIV.equals(nodeInfo.getSnmpSecurityLevel()))) {
			CommonValidator.validateString(MessageConstant.SNMP_AUTH_PASSWORD.getMessage(), nodeInfo.getSnmpAuthPassword(), true, 8, 64);
			String auth = nodeInfo.getSnmpAuthProtocol();
			if (auth == null || !SnmpProtocolConstant.getAuthProtocol().contains(auth)) {
				String[] args = { MessageConstant.SNMP_AUTH_PROTOCOL.getMessage() };
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(args));
				m_log.info("validateNodeInfo() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} else {
			CommonValidator.validateString(MessageConstant.SNMP_AUTH_PASSWORD.getMessage(), nodeInfo.getSnmpAuthPassword(), false, 0, 64);
		}
		if(SnmpVersionConstant.TYPE_V3 == nodeInfo.getSnmpVersion() &&
				(SnmpSecurityLevelConstant.AUTH_PRIV.equals(nodeInfo.getSnmpSecurityLevel()))) {
			CommonValidator.validateString(MessageConstant.SNMP_PRIV_PASSWORD.getMessage(), nodeInfo.getSnmpPrivPassword(), true, 8, 64);
			String priv = nodeInfo.getSnmpPrivProtocol();
			if (priv == null || !SnmpProtocolConstant.getPrivProtocol().contains(priv)) {
				String[] args = { MessageConstant.SNMP_PRIV_PROTOCOL.getMessage() };
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(args));
				m_log.info("validateNodeInfo() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} else {
			CommonValidator.validateString(MessageConstant.SNMP_PRIV_PASSWORD.getMessage(), nodeInfo.getSnmpPrivPassword(), false, 0, 64);
		}

		CommonValidator.validateString(MessageConstant.SNMP_USER.getMessage(),
				nodeInfo.getSnmpUser(), false, 0, 64);
		CommonValidator.validateInt(MessageConstant.SNMP_RETRIES.getMessage(),
				nodeInfo.getSnmpRetryCount(), 1, 10);
		CommonValidator.validateInt(MessageConstant.SNMP_TIMEOUT.getMessage(),
				nodeInfo.getSnmpTimeout(), 1, Integer.MAX_VALUE);

		//サービスのチェック(WBEM)
		if(nodeInfo.getWbemProtocol() == null
				|| (!"".equals(nodeInfo.getWbemProtocol())
						&& !"http".equals(nodeInfo.getWbemProtocol())
						&& !"https".equals(nodeInfo.getWbemProtocol()))){
			InvalidSetting e = new InvalidSetting("WBEM Protocol is http or https");
			m_log.info("validateNodeInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateString(MessageConstant.WBEM_USER.getMessage(),
				nodeInfo.getWbemUser(), false, 0, 64);
		CommonValidator.validateString(MessageConstant.WBEM_USER_PASSWORD.getMessage(),
				nodeInfo.getWbemUserPassword(), false, 0, 64);
		CommonValidator.validateInt(MessageConstant.WBEM_RETRIES.getMessage(),
				nodeInfo.getWbemRetryCount(), 1, 10);
		CommonValidator.validateInt(MessageConstant.WBEM_TIMEOUT.getMessage(),
				nodeInfo.getWbemTimeout(), 1, Integer.MAX_VALUE);

		//サービスのチェック(IPMI)
		CommonValidator.validateString(MessageConstant.IPMI_PROTOCOL.getMessage(), nodeInfo.getIpmiProtocol(), false, 0, 32);
		CommonValidator.validateString(MessageConstant.IPMI_LEVEL.getMessage(), nodeInfo.getIpmiLevel(), false, 0, 32);
		CommonValidator.validateString(MessageConstant.IPMI_USER.getMessage(),
				nodeInfo.getIpmiUser(), false, 0, 64);
		CommonValidator.validateString(MessageConstant.IPMI_USER_PASSWORD.getMessage(),
				nodeInfo.getIpmiUserPassword(), false, 0, 64);
		CommonValidator.validateInt(MessageConstant.IPMI_RETRIES.getMessage(),
				nodeInfo.getIpmiRetries(), 1, 10);
		CommonValidator.validateInt(MessageConstant.IPMI_TIMEOUT.getMessage(),
				nodeInfo.getIpmiTimeout(), 1, Integer.MAX_VALUE);

		//サービスのチェック(WinRM)
		if(nodeInfo.getWinrmProtocol() == null
				|| (!"".equals(nodeInfo.getWinrmProtocol())
						&& !"http".equals(nodeInfo.getWinrmProtocol())
						&& !"https".equals(nodeInfo.getWinrmProtocol()))){
			InvalidSetting e = new InvalidSetting("WinRM Protocol is http or https");
			m_log.info("validateNodeInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateString(MessageConstant.WINRM_USER.getMessage(),
				nodeInfo.getWinrmUser(), false, 0, 64);
		CommonValidator.validateString(MessageConstant.WINRM_USER_PASSWORD.getMessage(),
				nodeInfo.getWinrmUserPassword(), false, 0, 64);
		CommonValidator.validateInt(MessageConstant.WINRM_RETRIES.getMessage(),
				nodeInfo.getWinrmRetries(), 1, 10);
		CommonValidator.validateInt(MessageConstant.WINRM_TIMEOUT.getMessage(),
				nodeInfo.getWinrmTimeout(), 1, Integer.MAX_VALUE);

		// administrator
		CommonValidator.validateString(MessageConstant.ADMINISTRATOR.getMessage(), nodeInfo.getAdministrator(), false, 0, 256);

	}

	/**
	 * スコープ情報の妥当性チェック
	 *
	 * @param scopeInfo
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public static void validateScopeInfo(String parentFacilityId, ScopeInfo scopeInfo, boolean parentCheck)
			throws InvalidSetting, InvalidRole {

		validateFacilityInfo(scopeInfo);

		// parentFacilityId
		if(parentCheck){
			if(parentFacilityId != null && parentFacilityId.compareTo("") != 0){
				try{
					QueryUtil.getFacilityPK(parentFacilityId, ObjectPrivilegeMode.MODIFY);
				} catch (FacilityNotFound e) {
					InvalidSetting e1 = new InvalidSetting("Scope does not exist! facilityId = " + parentFacilityId);
					m_log.info("validateScopeInfo() : "
							+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
					throw e1;
				} catch (InvalidRole e) {
					throw e;
				}
			}
		}

		// facilityType
		if(scopeInfo.getFacilityType() != FacilityConstant.TYPE_SCOPE){
			InvalidSetting e = new InvalidSetting("Scope FacilityType is  " + scopeInfo.getFacilityType());
			m_log.info("validateScopeInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
	}

	public static void validateFacilityInfo (FacilityInfo facilityInfo) throws InvalidSetting {
		// facilityId
		CommonValidator.validateId(MessageConstant.FACILITY_ID.getMessage(), facilityInfo.getFacilityId(), 512);
		// 最上位スコープ（_ROOT_）は登録不可
		if (facilityInfo.getFacilityId().equals(FacilityIdConstant.ROOT)) {
			String[] args = { FacilityIdConstant.ROOT };
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_NOT_ALLOWED_IN_REPOSITORY.getMessage(args));
			m_log.info("validateFacilityInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		// facilityName
		CommonValidator.validateString(MessageConstant.FACILITY_NAME.getMessage(), facilityInfo.getFacilityName(), true, 1, 128);

		// description
		CommonValidator.validateString(MessageConstant.DESCRIPTION.getMessage(), facilityInfo.getDescription(), false, 0, 256);

		// ownerRoleId
		CommonValidator.validateOwnerRoleId(facilityInfo.getOwnerRoleId(), true,
				facilityInfo.getFacilityId(), HinemosModuleConstant.PLATFORM_REPOSITORY);
	}

	/**
	 * ノード割り当て時のチェック
	 *
	 * @param parentFacilityId
	 * @param facilityIds
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public static void validateaAssignNodeScope(String parentFacilityId, String[] facilityIds)
			throws InvalidSetting, InvalidRole {
		// parentFacilityId
		try{
			QueryUtil.getFacilityPK(parentFacilityId, ObjectPrivilegeMode.MODIFY);
		} catch (FacilityNotFound e) {
			InvalidSetting e1 = new InvalidSetting("Scope does not exist! facilityId = " + parentFacilityId);
			m_log.info("validateaAssignNodeScope() : "
					+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
			throw e1;
		} catch (InvalidRole e) {
			throw e;
		}

		// facilityIds
		if(facilityIds == null || facilityIds.length == 0){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_NODE_REPOSITORY.getMessage());
			m_log.info("validateaAssignNodeScope() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		for (int i = 0; i < facilityIds.length; i++) {
			try{
				NodeProperty.getProperty(facilityIds[i]);
			} catch (FacilityNotFound e) {
				InvalidSetting e1 = new InvalidSetting("Node does not exist! facilityId = " + facilityIds[i]);
				m_log.info("validateaAssignNodeScope() : "
						+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
				throw e1;
			}
		}
	}

}
