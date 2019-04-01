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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.SnmpProtocolConstant;
import com.clustercontrol.bean.SnmpSecurityLevelConstant;
import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.bean.FacilityIdConstant;
import com.clustercontrol.repository.bean.NodeConfigFilterComparisonMethod;
import com.clustercontrol.repository.bean.NodeConfigFilterDataType;
import com.clustercontrol.repository.bean.NodeConfigFilterInfo;
import com.clustercontrol.repository.bean.NodeConfigFilterItem;
import com.clustercontrol.repository.bean.NodeConfigFilterItemInfo;
import com.clustercontrol.repository.bean.NodeConfigRunInterval;
import com.clustercontrol.repository.bean.NodeConfigSettingItem;
import com.clustercontrol.repository.factory.NodeProperty;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.NodeConfigCustomInfo;
import com.clustercontrol.repository.model.NodeConfigSettingInfo;
import com.clustercontrol.repository.model.NodeConfigSettingItemInfo;
import com.clustercontrol.repository.model.NodeCpuInfo;
import com.clustercontrol.repository.model.NodeCustomInfo;
import com.clustercontrol.repository.model.NodeDeviceInfo;
import com.clustercontrol.repository.model.NodeDeviceInfoPK;
import com.clustercontrol.repository.model.NodeDiskInfo;
import com.clustercontrol.repository.model.NodeFilesystemInfo;
import com.clustercontrol.repository.model.NodeHostnameInfo;
import com.clustercontrol.repository.model.NodeHostnameInfoPK;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.NodeLicenseInfo;
import com.clustercontrol.repository.model.NodeLicenseInfoPK;
import com.clustercontrol.repository.model.NodeMemoryInfo;
import com.clustercontrol.repository.model.NodeProductInfo;
import com.clustercontrol.repository.model.NodeProductInfoPK;
import com.clustercontrol.repository.model.NodeNetstatInfo;
import com.clustercontrol.repository.model.NodeNetstatInfoPK;
import com.clustercontrol.repository.model.NodeNetworkInterfaceInfo;
import com.clustercontrol.repository.model.NodeOsInfo;
import com.clustercontrol.repository.model.NodePackageInfo;
import com.clustercontrol.repository.model.NodePackageInfoPK;
import com.clustercontrol.repository.model.NodeProcessInfo;
import com.clustercontrol.repository.model.NodeProcessInfoPK;
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
		validateNodeInfo(nodeInfo, false);
	}

	public static void validateNodeInfo(NodeInfo nodeInfo, boolean auto) throws InvalidSetting{

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
		else if(!auto){
			InvalidSetting e = new InvalidSetting("IpAddressVersion is not 4 / 6.");
			m_log.info("validateNodeInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		//ノード名の入力チェック
		if(auto){
			CommonValidator.validateString(MessageConstant.NODE_NAME.getMessage(), nodeInfo.getNodeName(), false, 0, 128);
		} else{
			CommonValidator.validateString(MessageConstant.NODE_NAME.getMessage(), nodeInfo.getNodeName(), true, 1, 128);
		}

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

		// 入力チェック (OS情報)
		validateNodeOsInfo(nodeInfo.getNodeOsInfo());
		
		//デバイスの入力チェック
		if(nodeInfo.getNodeCpuInfo() != null){
			List<NodeDeviceInfoPK> pkList = new ArrayList<NodeDeviceInfoPK>();
			for(NodeCpuInfo info : nodeInfo.getNodeCpuInfo()){
				// 入力チェック (CPU情報)
				validateNodeCpuInfo(info);

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
			List<NodeDeviceInfoPK> pkList = new ArrayList<NodeDeviceInfoPK>();
			for(NodeMemoryInfo info : nodeInfo.getNodeMemoryInfo()){
				// 入力チェック (メモリ情報)
				validateNodeMemoryInfo(info);

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
			List<NodeDeviceInfoPK> pkList = new ArrayList<NodeDeviceInfoPK>();
			for(NodeDiskInfo info : nodeInfo.getNodeDiskInfo()){
				// 入力チェック (ディスク情報)
				validateNodeDiskInfo(info);

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
			List<NodeDeviceInfoPK> pkList = new ArrayList<NodeDeviceInfoPK>();
			for(NodeNetworkInterfaceInfo info : nodeInfo.getNodeNetworkInterfaceInfo()){
				// 入力チェック (NIC情報)
				validateNodeNetworkInterfaceInfo(info);

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
			List<NodeDeviceInfoPK> pkList = new ArrayList<NodeDeviceInfoPK>();
			for(NodeFilesystemInfo info : nodeInfo.getNodeFilesystemInfo()){
				// 入力チェック (ファイルシステム情報)
				validateNodeFilesystemInfo(info);

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
				CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_NAME.getMessage() + "]", info.getDeviceName(), true, 1, 256);
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
				if (info.getHostname() == null || info.getHostname().equals("")) {
					continue;
				}

				// 入力チェック (ホスト名情報)
				validateNodeHostnameInfo(info);

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
				if (variable.getNodeVariableName() == null || variable.getNodeVariableName().equals("")) {
					continue;
				}

				// 入力チェック (ノード変数情報)
				validateNodeVariableInfo(variable);

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

		if(nodeInfo.getNodeNetstatInfo() != null){
			// 比較用Map (PK, status)
			HashMap<NodeNetstatInfoPK, String> infoMap = new HashMap<>();
			Iterator<NodeNetstatInfo> iter = nodeInfo.getNodeNetstatInfo().iterator();
			while(iter.hasNext()){
				NodeNetstatInfo info = iter.next();
				// 入力チェック (ネットワーク接続)
				validateNodeNetstatInfo(info);

				// 重複チェック
				// JPAリレーションでのDeleteInsertがうまくいかないので、
				// インデックスを持っていないデバイス情報は手動で重複チェックする。
				NodeNetstatInfoPK entityPk = new NodeNetstatInfoPK(
						nodeInfo.getFacilityId(), info.getProtocol(), info.getLocalIpAddress(), info.getLocalPort(), info.getForeignIpAddress(), info.getForeignPort(),
						info.getProcessName(), info.getPid());
				if (infoMap.containsKey(entityPk)) {
					if (infoMap.get(entityPk).equals(info.getStatus())) {
						// 重複レコードを削除する
						iter.remove();
						continue;
					} else {
						// ステータスが異なる場合はエラー
						String[] args = { MessageConstant.NODE_NETSTAT.getMessage(), 
								String.format("%s, %s:%s", info.getProtocol(), info.getLocalIpAddress(), info.getLocalPort())};
						InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(args));
						m_log.info("validateNodeInfo() : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage());
						throw e;
					}
				}
				infoMap.put(entityPk, info.getStatus());
			}
		}

		if(nodeInfo.getNodeProcessInfo() != null){
			List<NodeProcessInfoPK> pkList = new ArrayList<>();
			for(NodeProcessInfo info : nodeInfo.getNodeProcessInfo()){
				// 入力チェック (プロセス情報)
				validateNodeProcessInfo(info);

				// 重複チェック
				// JPAリレーションでのDeleteInsertがうまくいかないので、
				// インデックスを持っていないデバイス情報は手動で重複チェックする。
				NodeProcessInfoPK entityPk = new NodeProcessInfoPK(nodeInfo.getFacilityId(), info.getProcessName(), info.getPid());
				if (pkList.contains(entityPk)) {
					String[] args = { MessageConstant.NODE_PROCESS.getMessage(), info.getProcessName()};
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(args));
					m_log.info("validateNodeInfo() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				pkList.add(entityPk);
			}
		}

		if(nodeInfo.getNodePackageInfo() != null){
			List<NodePackageInfoPK> pkList = new ArrayList<>();
			for(NodePackageInfo info : nodeInfo.getNodePackageInfo()){
				// 入力チェック (パッケージ情報)
				validateNodePackageInfo(info);

				// 重複チェック
				// JPAリレーションでのDeleteInsertがうまくいかないので、
				// インデックスを持っていないデバイス情報は手動で重複チェックする。
				NodePackageInfoPK entityPk = new NodePackageInfoPK(nodeInfo.getFacilityId(), info.getPackageId());
				if (pkList.contains(entityPk)) {
					String[] args = { MessageConstant.NODE_PACKAGE.getMessage(), info.getPackageId()};
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(args));
					m_log.info("validateNodeInfo() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				pkList.add(entityPk);
			}
		}

		if(nodeInfo.getNodeProductInfo() != null){
			List<NodeProductInfoPK> pkList = new ArrayList<>();
			for(NodeProductInfo info : nodeInfo.getNodeProductInfo()){
				// 入力チェック (個別導入製品情報)
				validateNodeProductInfo(info);

				// 重複チェック
				// JPAリレーションでのDeleteInsertがうまくいかないので、
				// インデックスを持っていないデバイス情報は手動で重複チェックする。
				NodeProductInfoPK entityPk = new NodeProductInfoPK(nodeInfo.getFacilityId(), info.getProductName());
				if (pkList.contains(entityPk)) {
					String[] args = { MessageConstant.NODE_PRODUCT.getMessage(), info.getProductName()};
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(args));
					m_log.info("validateNodeInfo() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				pkList.add(entityPk);
			}
		}

		if(nodeInfo.getNodeLicenseInfo() != null){
			List<NodeLicenseInfoPK> pkList = new ArrayList<>();
			for(NodeLicenseInfo info : nodeInfo.getNodeLicenseInfo()){
				// 入力チェック (個別導入製品情報)
				validateNodeLicenseInfo(info);

				// 重複チェック
				// JPAリレーションでのDeleteInsertがうまくいかないので、
				// インデックスを持っていないデバイス情報は手動で重複チェックする。
				NodeLicenseInfoPK entityPk = new NodeLicenseInfoPK(nodeInfo.getFacilityId(), info.getProductName());
				if (pkList.contains(entityPk)) {
					String[] args = { MessageConstant.NODE_LICENSE.getMessage(), info.getProductName()};
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
		if( nodeInfo.getSnmpVersion() == null || (
				!auto &&
				(SnmpVersionConstant.TYPE_V1 != nodeInfo.getSnmpVersion()
				&& SnmpVersionConstant.TYPE_V2 != nodeInfo.getSnmpVersion()
				&& SnmpVersionConstant.TYPE_V3 != nodeInfo.getSnmpVersion()))
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
	 * ノードフィルタのバリデートチェック
	 * 
	 * @param nodeInfo
	 * @throws InvalidSetting
	 */
	public static void validateFilterNodeInfo(NodeInfo nodeInfo) throws InvalidSetting {

		if (nodeInfo.getNodeConfigFilterList() == null
				|| nodeInfo.getNodeConfigFilterList().size() == 0) {
			// チェック不要
			return;
		}

		// 構成情報のチェック
		Iterator<NodeConfigFilterInfo> iter = nodeInfo.getNodeConfigFilterList().iterator();
		while (iter.hasNext()) {
			NodeConfigFilterInfo filterInfo = iter.next();

			if (filterInfo.getNodeConfigSettingItemName() == null 
					|| filterInfo.getNodeConfigSettingItemName().isEmpty()) {
				// 構成情報種別名が未設定の場合は対象外
				iter.remove();
			}

			// 構成情報種別名存在チェック
			if (filterInfo.getNodeConfigSettingItem() == null) {
				String nodeConfigSettingItemName = "";
				if (filterInfo.getNodeConfigSettingItemName() != null) {
					nodeConfigSettingItemName = filterInfo.getNodeConfigSettingItemName();
				}
				InvalidSetting e = new InvalidSetting(
						String.format("The specified node config setting name does not exist. NodeConfigSettingItem=%s", 
								nodeConfigSettingItemName));
				m_log.warn("validateFilterNodeInfo() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				continue;
			}

			Iterator<NodeConfigFilterItemInfo> itemIter = filterInfo.getItemList().iterator();
			while (itemIter.hasNext()) {
				NodeConfigFilterItemInfo itemInfo = itemIter.next();

				// 項目名
				if (itemInfo.getItemName() == null || itemInfo.getItemName().isEmpty()) {
					// 項目名が設定されていない場合は対象外
					itemIter.remove();
					continue;
				}

				if (itemInfo.getItem() == null) {
					// 項目名が構成情報項目でない場合はエラー
					InvalidSetting e = new InvalidSetting(
							String.format("Incorrect value is set in the item name. NodeConfigSettingItem=%s, ItemName=%s, ComparisonMethod=%s, Value=%s",
							filterInfo.getNodeConfigSettingItemName(), itemInfo.getItemName(), itemInfo.getMethod(), itemInfo.getItemValue().toString()));
					m_log.warn("validateFilterNodeInfo() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				if (itemInfo.getItem().nodeConfigSettingItem() != filterInfo.getNodeConfigSettingItem()) {
					// 項目名と構成情報がアンマッチの場合はエラー
					InvalidSetting e = new InvalidSetting(
							String.format("The specified node config setting name does not exist. NodeConfigSettingItem=%s", 
							filterInfo.getNodeConfigSettingItemName()));
					m_log.warn("validateFilterNodeInfo() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}

				// 比較演算子
				if (itemInfo.getMethod() == null || itemInfo.getMethod().isEmpty()) {
					// 演算子が設定されていない場合は対象外
					itemIter.remove();
					continue;
				}

				// 値
				if (itemInfo.getItemValue() == null
						|| (itemInfo.getItemValue() instanceof String && ((String)itemInfo.getItemValue()).isEmpty())) {
					// 値が設定されていない場合は対象外
					itemIter.remove();
					continue;
				}

				if (itemInfo.getMethodType() == null) {
					// 該当する比較演算子が存在しない場合はエラー
					InvalidSetting e = new InvalidSetting(
							String.format("Incorrect value is set in the comparison method. NodeConfigSettingItem=%s, ItemName=%s, ComparisonMethod=%s, Value=%s",
							filterInfo.getNodeConfigSettingItemName(), itemInfo.getItemName(), itemInfo.getMethod(), itemInfo.getItemValue().toString()));
					m_log.warn("validateFilterNodeInfo() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}

				if (itemInfo.getItem().dataType() == NodeConfigFilterDataType.STRING
						|| itemInfo.getItem().dataType() == NodeConfigFilterDataType.STRING_ONLYEQUAL
						|| itemInfo.getItem().dataType() == NodeConfigFilterDataType.STRING_VERSION) {
					if (!(itemInfo.getItemValue() instanceof String)) {
						// データ型が異なる場合はエラー
						InvalidSetting e = new InvalidSetting(
								String.format("Incorrect value is set in the node config setting value. NodeConfigSettingItem=%s, ItemName=%s, ComparisonMethod=%s, Value=%s",
								filterInfo.getNodeConfigSettingItemName(), itemInfo.getItemName(), itemInfo.getMethod(), itemInfo.getItemValue().toString()));
						m_log.warn("validateFilterNodeInfo() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
						throw e;
					}
					if (((String)itemInfo.getItemValue()).isEmpty()) {
						// 空文字の場合は対象外
						itemIter.remove();
						continue;
					}
				} else if (itemInfo.getItem().dataType() == NodeConfigFilterDataType.INTEGER
						|| itemInfo.getItem().dataType() == NodeConfigFilterDataType.INTEGER_ONLYEQUAL) {
					if (!(itemInfo.getItemValue() instanceof Integer)) {
						// データ型が異なる場合はエラー
						InvalidSetting e = new InvalidSetting(
								String.format("Incorrect value is set in the node config setting value. NodeConfigSettingItem=%s, ItemName=%s, ComparisonMethod=%s, Value=%s",
								filterInfo.getNodeConfigSettingItemName(), itemInfo.getItemName(), itemInfo.getMethod(), itemInfo.getItemValue().toString()));
						m_log.warn("validateFilterNodeInfo() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
						throw e;
					}
				} else if (itemInfo.getItem().dataType() == NodeConfigFilterDataType.DATETIME) {
					if (!(itemInfo.getItemValue() instanceof Long)) {
						// データ型が異なる場合はエラー
						InvalidSetting e = new InvalidSetting(
								String.format("Incorrect value is set in the node config setting value. NodeConfigSettingItem=%s, ItemName=%s, ComparisonMethod=%s, Value=%s",
								filterInfo.getNodeConfigSettingItemName(), itemInfo.getItemName(), itemInfo.getMethod(), itemInfo.getItemValue().toString()));
						m_log.warn("validateFilterNodeInfo() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
						throw e;
					}
					if ((Long)itemInfo.getItemValue() <= 0L) {
						// 0未満の場合は対象外
						itemIter.remove();
						continue;
					}
				}
				if ((itemInfo.getItem().dataType() == NodeConfigFilterDataType.STRING_ONLYEQUAL
						|| itemInfo.getItem().dataType() == NodeConfigFilterDataType.INTEGER_ONLYEQUAL)
						&& itemInfo.getMethodType() != NodeConfigFilterComparisonMethod.EQ
						&& itemInfo.getMethodType() != NodeConfigFilterComparisonMethod.NE) {
					// 該当する演算子でない場合はエラー
					InvalidSetting e = new InvalidSetting(
							String.format("Incorrect value is set in the comparison method. NodeConfigSettingItem=%s, ItemName=%s, ComparisonMethod=%s, Value=%s",
							filterInfo.getNodeConfigSettingItemName(), itemInfo.getItemName(), itemInfo.getMethod(), itemInfo.getItemValue().toString()));
					m_log.warn("validateFilterNodeInfo() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}

			if (filterInfo.getNodeConfigSettingItem() == NodeConfigSettingItem.PACKAGE) {
				boolean isName = false;
				boolean isVersion = false;
				for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
					if (itemInfo.getItem() == NodeConfigFilterItem.PACKAGE_NAME) {
						isName = true;
					} else if (itemInfo.getItem() == NodeConfigFilterItem.PACKAGE_VERSION
							|| itemInfo.getItem() == NodeConfigFilterItem.PACKAGE_RELEASE) {
						isVersion = true;
					}
				}
				if (isVersion && !isName) {
					// バージョン、リリース番号が指定されており、パッケージ名が指定されていない場合はエラー
					String[] args = {MessageConstant.NODE_PACKAGE_NAME.getMessage() + "("  + MessageConstant.NODE_PACKAGE.getMessage() + ")"};
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(args));
					m_log.warn("validateFilterNodeInfo() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			} else if (filterInfo.getNodeConfigSettingItem() == NodeConfigSettingItem.PRODUCT) {
				boolean isName = false;
				boolean isVersion = false;
				for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
					if (itemInfo.getItem() == NodeConfigFilterItem.PRODUCT_NAME) {
						isName = true;
					} else if (itemInfo.getItem() == NodeConfigFilterItem.PRODUCT_VERSION) {
						isVersion = true;
					}
				}
				if (isVersion && !isName) {
					// バージョンが指定されており、製品名が指定されていない場合はエラー
					String[] args = {MessageConstant.NODE_PRODUCT_NAME.getMessage() + "("  + MessageConstant.NODE_PRODUCT.getMessage() + ")"};
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(args));
					m_log.warn("validateFilterNodeInfo() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			} else if (filterInfo.getNodeConfigSettingItem() == NodeConfigSettingItem.CUSTOM) {
				boolean isName = false;
				boolean isValue = false;
				for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
					if (itemInfo.getItem() == NodeConfigFilterItem.CUSTOM_DISPLAY_NAME) {
						isName = true;
					} else if (itemInfo.getItem() == NodeConfigFilterItem.CUSTOM_VALUE) {
						isValue = true;
					}
				}
				if (isValue && !isName) {
					// 値が指定されており、表示名が指定されていない場合はエラー
					String[] args = {MessageConstant.SETTING_CUSTOM_NAME.getMessage() + "("  + MessageConstant.NODE_CONFIG_SETTING_CUSTOM.getMessage() + ")"};
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(args));
					m_log.warn("validateFilterNodeInfo() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
			}

			if (filterInfo.getItemList() == null || filterInfo.getItemList().size() == 0) {
				// 項目がひとつも存在しない場合は対象外
				iter.remove();
			}
		}
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

		// iconImage
		CommonValidator.validateString(MessageConstant.ICON_IMAGE.getMessage(), facilityInfo.getIconImage(), false, 0, 256);
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
	
	/**
	 * 対象構成情報のValidateチェック
	 * 
	 * @param info 対象構成情報
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public static void validateNodeConfigSettingInfo(NodeConfigSettingInfo info)
			throws InvalidSetting, InvalidRole {

		// settingId
		CommonValidator.validateId(MessageConstant.SETTING_ID.getMessage(), info.getSettingId(), 64);

		// settingName
		CommonValidator.validateString(MessageConstant.SETTING_NAME.getMessage(), info.getSettingName(), true, 1, 128);

		// description
		CommonValidator.validateString(MessageConstant.DESCRIPTION.getMessage(),
				info.getDescription(), false, 0, 256);

		// facilityId
		if(info.getFacilityId() == null || "".equals(info.getFacilityId())){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SCOPE.getMessage());
			m_log.info("validateNodeConfigSettingInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}else{
			try {
				FacilityTreeCache.validateFacilityId(info.getFacilityId(), info.getOwnerRoleId(), false);
			} catch (FacilityNotFound e) {
				throw new InvalidSetting(e.getMessage(), e);
			}
		}

		// runInterval
		if(info.getRunInterval() != NodeConfigRunInterval.TYPE_HOUR_6.toSec()
				&& info.getRunInterval() != NodeConfigRunInterval.TYPE_HOUR_12.toSec()
				&& info.getRunInterval() != NodeConfigRunInterval.TYPE_HOUR_24.toSec()){
				InvalidSetting e = new InvalidSetting("RunInterval is not 6 hour / 12 hour / 24 hour.");
			m_log.info("validateNodeConfigSettingInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// calendarId
		if (info.getCalendarId() != null && !info.getCalendarId().equals("")) {
			CommonValidator.validateCalenderId(info.getCalendarId(), false, info.getOwnerRoleId());
		}

		// notifyId
		if(info.getNotifyRelationList() != null
				&& info.getNotifyRelationList().size() > 0){
			for(NotifyRelationInfo notifyInfo : info.getNotifyRelationList()){
				CommonValidator.validateNotifyId(notifyInfo.getNotifyId(), true, info.getOwnerRoleId());
			}
		}

		// NodeConfigSettingItem
		boolean customFlag = false;
		if(info.getNodeConfigSettingItemList() != null
				&& info.getNodeConfigSettingItemList().size() > 0){
			for(NodeConfigSettingItemInfo itemInfo : info.getNodeConfigSettingItemList()){
				if (itemInfo.getSettingItemId() == null
						|| !(itemInfo.getSettingItemId().equals(NodeConfigSettingItem.OS.name())
						|| itemInfo.getSettingItemId().equals(NodeConfigSettingItem.HW_CPU.name())
						|| itemInfo.getSettingItemId().equals(NodeConfigSettingItem.HW_MEMORY.name())
						|| itemInfo.getSettingItemId().equals(NodeConfigSettingItem.HW_NIC.name())
						|| itemInfo.getSettingItemId().equals(NodeConfigSettingItem.HW_DISK.name())
						|| itemInfo.getSettingItemId().equals(NodeConfigSettingItem.HW_FILESYSTEM.name())
						|| itemInfo.getSettingItemId().equals(NodeConfigSettingItem.HOSTNAME.name())
						|| itemInfo.getSettingItemId().equals(NodeConfigSettingItem.NETSTAT.name())
						|| itemInfo.getSettingItemId().equals(NodeConfigSettingItem.PROCESS.name())
						|| itemInfo.getSettingItemId().equals(NodeConfigSettingItem.PACKAGE.name())
						|| itemInfo.getSettingItemId().equals(NodeConfigSettingItem.CUSTOM.name()))) {
					InvalidSetting e = new InvalidSetting("NodeConfigSettingItem is incorrect.");
					m_log.info("validateNodeConfigSettingInfo() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				if (itemInfo.getSettingItemId() != null
						&& itemInfo.getSettingItemId().equals(NodeConfigSettingItem.CUSTOM.name())){
					customFlag = true;
				}
			}
		}
		
		// NodeCustomInfo
		if(customFlag){
			if(info.getNodeConfigCustomList() == null || info.getNodeConfigCustomList().isEmpty()){
				InvalidSetting e = new InvalidSetting("NodeCustomInfo is empty.");
				m_log.info("validateNodeConfigSettingInfo() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			
			List<String> duplicatedCustomIdList = new ArrayList<String>();
			for(NodeConfigCustomInfo customInfo : info.getNodeConfigCustomList()){
				// custom ID.
				CommonValidator.validateId(MessageConstant.SETTING_CUSTOM_ID.getMessage(), customInfo.getSettingCustomId(), 64);
				duplicatedCustomIdList.add(customInfo.getSettingCustomId());
				
				// display name.
				CommonValidator.validateString(MessageConstant.SETTING_CUSTOM_NAME.getMessage(), customInfo.getDisplayName(), true, 1, 128);
				
				// description.
				CommonValidator.validateString(MessageConstant.DESCRIPTION.getMessage(),customInfo.getDescription(), false, 0, 256);
				
				// command.
				CommonValidator.validateString(MessageConstant.COMMAND.getMessage(), customInfo.getCommand(), true, 1, 1024);
				
				// effective user.
				if(customInfo.isSpecifyUser() != null && customInfo.isSpecifyUser().booleanValue()){
					CommonValidator.validateString(MessageConstant.EFFECTIVE_USER.getMessage(), customInfo.getEffectiveUser(), true, 1, 64);
				}
			}
			
			// check "custom ID" not to be duplicate
			List<String> singleCustomIdList = new ArrayList<String>(new HashSet<>(duplicatedCustomIdList));
			if(duplicatedCustomIdList.size() != singleCustomIdList.size()){
				InvalidSetting e = new InvalidSetting( //
						MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage("", MessageConstant.SETTING_CUSTOM_ID.getMessage()));
				m_log.info("validateNodeConfigSettingInfo() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			
		}

		// ownerRoleId
		CommonValidator.validateOwnerRoleId(info.getOwnerRoleId(), true,
				info.getSettingId(), HinemosModuleConstant.NODE_CONFIG_SETTING);
	}

	/**
	 * 構成情報(OS)の入力チェック
	 * 
	 * @param info 構成情報(OS)
	 * @return エラーメッセージリスト
	 */
	public static List<String> validateNodeOsConfigInfo(NodeOsInfo info) {
		List<String> messageList = new ArrayList<>();
		try {
			// 入力チェック (OS情報)
			validateNodeOsInfo(info);
		} catch (InvalidSetting e) {
			messageList.add(e.getMessage());
		}
		return messageList;
	}

	/**
	 * 構成情報(CPU)の入力チェック
	 * 
	 * @param facilityId ファシリティID
	 * @param list 構成情報リスト(CPU)
	 * @return エラーメッセージリスト
	 */
	public static List<String> validateNodeCpuConfigInfo(String facilityId, List<NodeCpuInfo> list) {
		List<String> messageList = new ArrayList<>();
		if (list == null) {
			return messageList;
		}
		Iterator<NodeCpuInfo> iter = list.iterator();
		List<NodeDeviceInfoPK> pkList = new ArrayList<>();
		while(iter.hasNext()) {
			NodeCpuInfo info = iter.next();
			try {
				// 入力チェック (CPU情報)
				validateNodeCpuInfo(info);
			} catch (InvalidSetting e) {
				messageList.add(e.getMessage());
				iter.remove();
				continue;
			}
			// 重複チェック
			NodeDeviceInfoPK entityPk = new NodeDeviceInfoPK(
					facilityId,
					info.getDeviceIndex(),
					info.getDeviceType(),
					info.getDeviceName());
			if (pkList.contains(entityPk)) {
				String[] args = { MessageConstant.CPU.getMessage(), info.getDeviceName()};
				messageList.add(MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(args));
				iter.remove();
				continue;
			}
			pkList.add(entityPk);
		}
		return messageList;
	}

	/**
	 * 構成情報(ディスク)の入力チェック
	 * 
	 * @param facilityId ファシリティID
	 * @param list 構成情報リスト(ディスク)
	 * @return エラーメッセージリスト
	 */
	public static List<String> validateNodeDiskConfigInfo(String facilityId, List<NodeDiskInfo> list) {
		List<String> messageList = new ArrayList<>();
		if (list == null) {
			return messageList;
		}
		Iterator<NodeDiskInfo> iter = list.iterator();
		List<NodeDeviceInfoPK> pkList = new ArrayList<>();
		while(iter.hasNext()) {
			NodeDiskInfo info = iter.next();
			try {
				// 入力チェック (ディスク情報)
				validateNodeDiskInfo(info);
			} catch (InvalidSetting e) {
				messageList.add(e.getMessage());
				iter.remove();
				continue;
			}
			// 重複チェック
			NodeDeviceInfoPK entityPk = new NodeDeviceInfoPK(
					facilityId,
					info.getDeviceIndex(),
					info.getDeviceType(),
					info.getDeviceName());
			if (pkList.contains(entityPk)) {
				String[] args = { MessageConstant.DISK.getMessage(), info.getDeviceName()};
				messageList.add(MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(args));
				iter.remove();
				continue;
			}
			pkList.add(entityPk);
		}
		return messageList;
	}

	/**
	 * 構成情報(ファイルシステム)の入力チェック
	 * 
	 * @param facilityId ファシリティID
	 * @param list 構成情報リスト(ファイルシステム)
	 * @return エラーメッセージリスト
	 */
	public static List<String> validateNodeFilesystemConfigInfo(String facilityId, List<NodeFilesystemInfo> list) {
		List<String> messageList = new ArrayList<>();
		if (list == null) {
			return messageList;
		}
		Iterator<NodeFilesystemInfo> iter = list.iterator();
		List<NodeDeviceInfoPK> pkList = new ArrayList<>();
		while(iter.hasNext()) {
			NodeFilesystemInfo info = iter.next();
			try {
				// 入力チェック (ファイルシステム情報)
				validateNodeFilesystemInfo(info);
			} catch (InvalidSetting e) {
				messageList.add(e.getMessage());
				iter.remove();
				continue;
			}
			// 重複チェック
			NodeDeviceInfoPK entityPk = new NodeDeviceInfoPK(
					facilityId,
					info.getDeviceIndex(),
					info.getDeviceType(),
					info.getDeviceName());
			if (pkList.contains(entityPk)) {
				String[] args = { MessageConstant.FILE_SYSTEM.getMessage(), info.getDeviceName()};
				messageList.add(MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(args));
				iter.remove();
				continue;
			}
			pkList.add(entityPk);
		}
		return messageList;
	}

	/**
	 * 構成情報(ノード変数)の入力チェック
	 * 
	 * @param facilityId ファシリティID
	 * @param list 構成情報リスト(ノード変数)
	 * @return エラーメッセージリスト
	 */
	public static List<String> validateNodeVariableConfigInfo(String facilityId, List<NodeVariableInfo> list) {
		List<String> messageList = new ArrayList<>();
		if (list == null) {
			return messageList;
		}
		Iterator<NodeVariableInfo> iter = list.iterator();
		List<NodeVariableInfoPK> pkList = new ArrayList<>();
		while(iter.hasNext()) {
			NodeVariableInfo info = iter.next();
			try {
				if (info.getNodeVariableName() == null || info.getNodeVariableName().equals("")) {
					iter.remove();
				} else {
					// 入力チェック (ノード変数情報)
					validateNodeVariableInfo(info);
				}
			} catch (InvalidSetting e) {
				messageList.add(e.getMessage());
				iter.remove();
				continue;
			}
			// 重複チェック
			NodeVariableInfoPK entityPk = new NodeVariableInfoPK(facilityId, info.getNodeVariableName());
			if (pkList.contains(entityPk)) {
				String[] args = { MessageConstant.NODE_VARIABLE_NAME.getMessage(), info.getNodeVariableName()};
				messageList.add(MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(args));
				iter.remove();
				continue;
			}
			pkList.add(entityPk);
		}
		return messageList;
	}

	/**
	 * 構成情報(ホスト名)の入力チェック
	 * 
	 * @param facilityId ファシリティID
	 * @param list 構成情報リスト(ホスト名)
	 * @return エラーメッセージリスト
	 */
	public static List<String> validateNodeHostnameConfigInfo(String facilityId, List<NodeHostnameInfo> list) {
		List<String> messageList = new ArrayList<>();
		if (list == null) {
			return messageList;
		}
		Iterator<NodeHostnameInfo> iter = list.iterator();
		List<NodeHostnameInfoPK> pkList = new ArrayList<>();
		while(iter.hasNext()) {
			NodeHostnameInfo info = iter.next();
			try {
				if (info.getHostname() == null || info.getHostname().equals("")) {
					iter.remove();
				} else {
					// 入力チェック (ホスト名情報)
					validateNodeHostnameInfo(info);
				}
			} catch (InvalidSetting e) {
				messageList.add(e.getMessage());
				iter.remove();
				continue;
			}
			// 重複チェック
			NodeHostnameInfoPK entityPk = new NodeHostnameInfoPK(facilityId, info.getHostname());
			if (pkList.contains(entityPk)) {
				String[] args = { MessageConstant.HOST_NAME.getMessage(), info.getHostname()};
				messageList.add(MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(args));
				iter.remove();
				continue;
			}
			pkList.add(entityPk);
		}
		return messageList;
	}

	/**
	 * 構成情報(メモリ)の入力チェック
	 * 
	 * @param facilityId ファシリティID
	 * @param list 構成情報リスト(メモリ)
	 * @return エラーメッセージリスト
	 */
	public static List<String> validateNodeMemoryConfigInfo(String facilityId, List<NodeMemoryInfo> list) {
		List<String> messageList = new ArrayList<>();
		if (list == null) {
			return messageList;
		}
		Iterator<NodeMemoryInfo> iter = list.iterator();
		List<NodeDeviceInfoPK> pkList = new ArrayList<>();
		while(iter.hasNext()) {
			NodeMemoryInfo info = iter.next();
			try {
				// 入力チェック (メモリ情報)
				validateNodeMemoryInfo(info);
			} catch (InvalidSetting e) {
				messageList.add(e.getMessage());
				iter.remove();
				continue;
			}
			// 重複チェック
			NodeDeviceInfoPK entityPk = new NodeDeviceInfoPK(
					facilityId,
					info.getDeviceIndex(),
					info.getDeviceType(),
					info.getDeviceName());
			if (pkList.contains(entityPk)) {
				String[] args = { MessageConstant.MEMORY.getMessage(), info.getDeviceName()};
				messageList.add(MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(args));
				iter.remove();
				continue;
			}
			pkList.add(entityPk);
		}
		return messageList;
	}

	/**
	 * 構成情報(NIC)の入力チェック
	 * 
	 * @param facilityId ファシリティID
	 * @param list 構成情報リスト(NIC)
	 * @return エラーメッセージリスト
	 */
	public static List<String> validateNodeNicConfigInfo(String facilityId, List<NodeNetworkInterfaceInfo> list) {
		List<String> messageList = new ArrayList<>();
		if (list == null) {
			return messageList;
		}
		Iterator<NodeNetworkInterfaceInfo> iter = list.iterator();
		List<NodeDeviceInfoPK> pkList = new ArrayList<>();
		while(iter.hasNext()) {
			NodeNetworkInterfaceInfo info = iter.next();
			try {
				// 入力チェック (NIC情報)
				validateNodeNetworkInterfaceInfo(info);
			} catch (InvalidSetting e) {
				messageList.add(e.getMessage());
				iter.remove();
				continue;
			}
			// 重複チェック
			NodeDeviceInfoPK entityPk = new NodeDeviceInfoPK(
					facilityId,
					info.getDeviceIndex(),
					info.getDeviceType(),
					info.getDeviceName());
			if (pkList.contains(entityPk)) {
				String[] args = { MessageConstant.NETWORK_INTERFACE.getMessage(), info.getDeviceName()};
				messageList.add(MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(args));
				iter.remove();
				continue;
			}
			pkList.add(entityPk);
		}
		return messageList;
	}

	/**
	 * 構成情報(ネットワーク接続)の入力チェック
	 * 
	 * @param facilityId ファシリティID
	 * @param list 構成情報リスト(ネットワーク接続)
	 * @return エラーメッセージリスト
	 */
	public static List<String> validateNodeNetstatConfigInfo(String facilityId, List<NodeNetstatInfo> list) {
		List<String> messageList = new ArrayList<>();
		if (list == null) {
			return messageList;
		}
		// 比較用Map (PK, status)
		HashMap<NodeNetstatInfoPK, String> infoMap = new HashMap<>();
		Iterator<NodeNetstatInfo> iter = list.iterator();
		while(iter.hasNext()) {
			NodeNetstatInfo info = iter.next();
			try {
				// 入力チェック (ネットワーク接続)
				validateNodeNetstatInfo(info);
			} catch (InvalidSetting e) {
				messageList.add(e.getMessage());
				iter.remove();
				continue;
			}
			// 重複チェック
			NodeNetstatInfoPK entityPk = new NodeNetstatInfoPK(
					facilityId, info.getProtocol(), info.getLocalIpAddress(), info.getLocalPort(), info.getForeignIpAddress(), info.getForeignPort(),
					info.getProcessName(), info.getPid());
			if (infoMap.containsKey(entityPk)) {
				if (infoMap.get(entityPk).equals(info.getStatus())) {
					// 重複レコードを削除する
					iter.remove();
					continue;
				} else {
					// ステータスが異なる場合はエラー
					String[] args = { MessageConstant.NODE_NETSTAT.getMessage(), 
							String.format("%s, %s:%s", info.getProtocol(), info.getLocalIpAddress(), info.getLocalPort())};
					messageList.add(MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(args));
					iter.remove();
					continue;
				}
			}
			infoMap.put(entityPk, info.getStatus());
		}
		return messageList;
	}

	/**
	 * 構成情報(プロセス)の入力チェック
	 * 
	 * @param facilityId ファシリティID
	 * @param list 構成情報リスト(プロセス)
	 * @return エラーメッセージリスト
	 */
	public static List<String> validateNodeProcessConfigInfo(String facilityId, List<NodeProcessInfo> list) {
		List<String> messageList = new ArrayList<>();
		if (list == null) {
			return messageList;
		}
		Iterator<NodeProcessInfo> iter = list.iterator();
		List<NodeProcessInfoPK> pkList = new ArrayList<>();
		while(iter.hasNext()) {
			NodeProcessInfo info = iter.next();
			try {
				// 入力チェック (プロセス情報)
				validateNodeProcessInfo(info);
			} catch (InvalidSetting e) {
				messageList.add(e.getMessage());
				iter.remove();
				continue;
			}
			// 重複チェック
			NodeProcessInfoPK entityPk = new NodeProcessInfoPK(facilityId, info.getProcessName(), info.getPid());
			if (pkList.contains(entityPk)) {
				String[] args = { MessageConstant.NODE_PROCESS.getMessage(), info.getProcessName()};
				messageList.add(MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(args));
				iter.remove();
				continue;
			}
			pkList.add(entityPk);
		}
		return messageList;
	}

	/**
	 * 構成情報(パッケージ)の入力チェック
	 * 
	 * @param facilityId ファシリティID
	 * @param list 構成情報リスト(パッケージ)
	 * @return エラーメッセージリスト
	 */
	public static List<String> validateNodePackageConfigInfo(String facilityId, List<NodePackageInfo> list) {
		List<String> messageList = new ArrayList<>();
		if (list == null) {
			return messageList;
		}
		Iterator<NodePackageInfo> iter = list.iterator();
		List<NodePackageInfoPK> pkList = new ArrayList<>();
		while(iter.hasNext()) {
			NodePackageInfo info = iter.next();
			try {
				// 入力チェック (パッケージ情報)
				validateNodePackageInfo(info);
			} catch (InvalidSetting e) {
				messageList.add(e.getMessage());
				iter.remove();
				continue;
			}
			// 重複チェック
			NodePackageInfoPK entityPk = new NodePackageInfoPK(facilityId, info.getPackageId());
			if (pkList.contains(entityPk)) {
				String[] args = { MessageConstant.NODE_PACKAGE.getMessage(), info.getPackageId()};
				messageList.add(MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(args));
				iter.remove();
				continue;
			}
			pkList.add(entityPk);
		}
		return messageList;
	}

	/**
	 * ノード情報 (OS情報)の入力チェック
	 * 
	 * @param info ノード情報 (OS情報)
	 */
	private static void validateNodeOsInfo(NodeOsInfo info) throws InvalidSetting {
		if (info == null) {
			return;
		}
		// OS名
		if (info.getOsName() != null) {
			CommonValidator.validateString(MessageConstant.OS_NAME.getMessage(), info.getOsName(), false, 0, 1024);
		}
		// OSリリース
		if (info.getOsRelease() != null) {
			CommonValidator.validateString(MessageConstant.OS_RELEASE.getMessage(), info.getOsRelease(), false, 0, 1024);
		}
		// OSバージョン
		if (info.getOsVersion() != null) {
			CommonValidator.validateString(MessageConstant.OS_VERSION.getMessage(), info.getOsVersion(), false, 0, 1024);
		}
		// 文字セット
		if (info.getCharacterSet() != null) {
			CommonValidator.validateString(MessageConstant.CHARACTER_SET.getMessage(), info.getCharacterSet(), false, 0, 1024);
		}
		// 起動日時
		if (info.getStartupDateTime() != null) {
			CommonValidator.validateLong(MessageConstant.NODE_OS_STARTUP_DATE_TIME.getMessage(), info.getStartupDateTime(), 0, Long.MAX_VALUE);
		}
	}

	/**
	 * ノード情報 (CPU情報)の入力チェック
	 * 
	 * @param info ノード情報 (CPU情報)
	 */
	private static void validateNodeCpuInfo(NodeCpuInfo info) throws InvalidSetting {
		String DeviceTypeName = MessageConstant.CPU.getMessage();
		// キー情報
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_NAME.getMessage() + "]", info.getDeviceName(), true, 1, 256);
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_TYPE.getMessage() + "]", info.getDeviceType(), true, 1, 32);
		CommonValidator.validateInt(DeviceTypeName + "[" + MessageConstant.DEVICE_INDEX.getMessage() + "]", info.getDeviceIndex(), 0, Integer.MAX_VALUE);
		// 付属情報
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_DISPLAY_NAME.getMessage() + "]", info.getDeviceDisplayName(), true, 1, 256);
		CommonValidator.validateInt(DeviceTypeName + "[" + MessageConstant.DEVICE_SIZE.getMessage() + "]", info.getDeviceSize(), 0, Integer.MAX_VALUE);
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_SIZE_UNIT.getMessage() + "]", info.getDeviceSizeUnit(), false, 0, 64);
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DESCRIPTION.getMessage() + "]", info.getDeviceDescription(), false, 0, 1024);
		if (info.getCoreCount() != null) {
			CommonValidator.validateInt(MessageConstant.CPU_CORE_COUNT.getMessage(), info.getCoreCount(), 0, Integer.MAX_VALUE);
		}
		if (info.getThreadCount() != null) {
			CommonValidator.validateInt(MessageConstant.CPU_THREAD_COUNT.getMessage(), info.getThreadCount(), 0, Integer.MAX_VALUE);
		}
		if (info.getClockCount() != null) {
			CommonValidator.validateInt(MessageConstant.CPU_CLOCK_COUNT.getMessage(), info.getClockCount(), 0, Integer.MAX_VALUE);
		}
	}

	/**
	 * ノード情報 (ディスク情報)の入力チェック
	 * 
	 * @param info ノード情報 (ディスク情報)
	 */
	private static void validateNodeDiskInfo(NodeDiskInfo info) throws InvalidSetting {
		String DeviceTypeName = MessageConstant.DISK.getMessage();
		// キー情報
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_NAME.getMessage() + "]", info.getDeviceName(), true, 1, 256);
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_TYPE.getMessage() + "]", info.getDeviceType(), true, 1, 32);
		CommonValidator.validateInt(DeviceTypeName + "[" + MessageConstant.DEVICE_INDEX.getMessage() + "]", info.getDeviceIndex(), 0, Integer.MAX_VALUE);
		// 付属情報
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_DISPLAY_NAME.getMessage() + "]", info.getDeviceDisplayName(), true, 1, 256);
		CommonValidator.validateInt(DeviceTypeName + "[" + MessageConstant.DEVICE_SIZE.getMessage() + "]", info.getDeviceSize(), 0, Integer.MAX_VALUE);
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_SIZE_UNIT.getMessage() + "]", info.getDeviceSizeUnit(), false, 0, 64);
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DESCRIPTION.getMessage() + "]", info.getDeviceDescription(), false, 0, 1024);
	}

	/**
	 * ノード情報 (ファイルシステム情報)の入力チェック
	 * 
	 * @param info ノード情報 (ファイルシステム情報)
	 */
	private static void validateNodeFilesystemInfo(NodeFilesystemInfo info) throws InvalidSetting {
		String DeviceTypeName = MessageConstant.FILE_SYSTEM.getMessage();
		// キー情報
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_NAME.getMessage() + "]", info.getDeviceName(), true, 1, 256);
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_TYPE.getMessage() + "]", info.getDeviceType(), true, 1, 32);
		CommonValidator.validateInt(DeviceTypeName + "[" + MessageConstant.DEVICE_INDEX.getMessage() + "]", info.getDeviceIndex(), 0, Integer.MAX_VALUE);
		// 付属情報
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_DISPLAY_NAME.getMessage() + "]", info.getDeviceDisplayName(), true, 1, 256);
		CommonValidator.validateInt(DeviceTypeName + "[" + MessageConstant.DEVICE_SIZE.getMessage() + "]", info.getDeviceSize(), 0, Integer.MAX_VALUE);
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_SIZE_UNIT.getMessage() + "]", info.getDeviceSizeUnit(), false, 0, 64);
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DESCRIPTION.getMessage() + "]", info.getDeviceDescription(), false, 0, 1024);
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.FILE_SYSTEM_TYPE.getMessage() + "]", info.getFilesystemType(), false, 0, 1024);
	}

	/**
	 * ノード情報 (ノード変数情報)の入力チェック
	 * 
	 * @param info ノード情報 (ノード変数情報)
	 */
	private static void validateNodeVariableInfo(NodeVariableInfo info) throws InvalidSetting {
		// キー情報
		CommonValidator.validateString(MessageConstant.NODE_VARIABLE_NAME.getMessage(), info.getNodeVariableName(), true, 1, 128);
		// 付属情報
		CommonValidator.validateString(MessageConstant.NODE_VARIABLE_VALUE.getMessage(), info.getNodeVariableValue(), false, 1, 1024);
	}

	/**
	 * ノード情報 (ホスト名情報)の入力チェック
	 * 
	 * @param info ノード情報 (ホスト名情報)
	 */
	private static void validateNodeHostnameInfo(NodeHostnameInfo info) throws InvalidSetting {
		// キー情報
		CommonValidator.validateString(MessageConstant.HOST_NAME.getMessage(), info.getHostname(), true, 1, 1024);
	}

	/**
	 * ノード情報 (メモリ情報)の入力チェック
	 * 
	 * @param info ノード情報 (メモリ情報)
	 */
	private static void validateNodeMemoryInfo(NodeMemoryInfo info) throws InvalidSetting {
		String DeviceTypeName = MessageConstant.MEMORY.getMessage();
		// キー情報
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_NAME.getMessage() + "]", info.getDeviceName(), true, 1, 256);
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_TYPE.getMessage() + "]", info.getDeviceType(), true, 1, 32);
		CommonValidator.validateInt(DeviceTypeName + "[" + MessageConstant.DEVICE_INDEX.getMessage() + "]", info.getDeviceIndex(), 0, Integer.MAX_VALUE);
		// 付属情報
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_DISPLAY_NAME.getMessage() + "]", info.getDeviceDisplayName(), true, 1, 256);
		CommonValidator.validateInt(DeviceTypeName + "[" + MessageConstant.DEVICE_SIZE.getMessage() + "]", info.getDeviceSize(), 0, Integer.MAX_VALUE);
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_SIZE_UNIT.getMessage() + "]", info.getDeviceSizeUnit(), false, 0, 64);
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DESCRIPTION.getMessage() + "]", info.getDeviceDescription(), false, 0, 1024);
	}

	/**
	 * ノード情報 (NIC情報)の入力チェック
	 * 
	 * @param info ノード情報 (NIC情報)
	 */
	private static void validateNodeNetworkInterfaceInfo(NodeNetworkInterfaceInfo info) throws InvalidSetting {
		String DeviceTypeName = MessageConstant.NETWORK_INTERFACE.getMessage();
		// キー情報
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_NAME.getMessage() + "]", info.getDeviceName(), true, 1, 256);
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_TYPE.getMessage() + "]", info.getDeviceType(), true, 1, 32);
		CommonValidator.validateInt(DeviceTypeName + "[" + MessageConstant.DEVICE_INDEX.getMessage() + "]", info.getDeviceIndex(), 0, Integer.MAX_VALUE);
		// 付属情報
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_DISPLAY_NAME.getMessage() + "]", info.getDeviceDisplayName(), true, 1, 256);
		CommonValidator.validateInt(DeviceTypeName + "[" + MessageConstant.DEVICE_SIZE.getMessage() + "]", info.getDeviceSize(), 0, Integer.MAX_VALUE);
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DEVICE_SIZE_UNIT.getMessage() + "]", info.getDeviceSizeUnit(), false, 0, 64);
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.DESCRIPTION.getMessage() + "]", info.getDeviceDescription(), false, 0, 1024);
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.NIC_IP_ADDRESS.getMessage() + "]", info.getNicIpAddress(), false, 0, 1024);
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.NIC_MAC_ADDRESS.getMessage() + "]", info.getNicMacAddress(), false, 0, 1024);
	}

	/**
	 * ノード情報 (ネットワーク接続)の入力チェック
	 * 
	 * @param info ノード情報 (ネットワーク接続)
	 */
	private static void validateNodeNetstatInfo(NodeNetstatInfo info) throws InvalidSetting {
		String DeviceTypeName = MessageConstant.NODE_NETSTAT.getMessage();
		// キー情報
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.NODE_NETSTAT_PROTOCOL.getMessage() + "]", info.getProtocol(), true, 1, 1024);
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.NODE_NETSTAT_LOCAL_IP_ADDRESS.getMessage() + "]", info.getLocalIpAddress(), true, 1, 1024);
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.NODE_NETSTAT_LOCAL_PORT.getMessage() + "]", info.getLocalPort(), true, 1, 1024);
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.NODE_NETSTAT_FOREIGN_IP_ADDRESS.getMessage() + "]", info.getForeignIpAddress(), true, 0, 1024);
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.NODE_NETSTAT_FOREIGN_PORT.getMessage() + "]", info.getForeignPort(), true, 0, 1024);
		
		// 付属情報
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.NODE_NETSTAT_PROCESS_NAME.getMessage() + "]", info.getProcessName(), false, 0, 1024);
		if (info.getPid() != null) {
			CommonValidator.validateInt(DeviceTypeName + "[" + MessageConstant.NODE_NETSTAT_PID.getMessage() + "]", info.getPid(), -1, Integer.MAX_VALUE);
		}
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.NODE_NETSTAT_STATUS.getMessage() + "]", info.getStatus(), false, 0, 1024);
	}

	/**
	 * ノード情報 (プロセス情報)の入力チェック
	 * 
	 * @param info ノード情報 (プロセス情報)
	 */
	private static void validateNodeProcessInfo(NodeProcessInfo info) throws InvalidSetting {
		String DeviceTypeName = MessageConstant.NODE_PROCESS.getMessage();
		// キー情報
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.NODE_PROCESS_NAME.getMessage() + "]", info.getProcessName(), true, 1, 1024);
		CommonValidator.validateInt(DeviceTypeName + "[" + MessageConstant.NODE_PROCESS_PID.getMessage() + "]", info.getPid(), -1, Integer.MAX_VALUE);
		// 付属情報
		// 引数付きパスは最大サイズをHinemosプロパティから取得
		int pathMaxSize = HinemosPropertyCommon.node_process_path_maxsize.getIntegerValue();
		if (pathMaxSize < 0) {
			pathMaxSize = 0;
		}
		if (info.getPath() != null && info.getPath().length() > pathMaxSize) {
			info.setPath(info.getPath().substring(0, pathMaxSize));
		}
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.NODE_PROCESS_EXEC_USER.getMessage() + "]", info.getExecUser(), false, 0, 1024);
		CommonValidator.validateLong(DeviceTypeName + "[" + MessageConstant.NODE_PROCESS_STARTUP_DATE_TIME.getMessage() + "]", info.getStartupDateTime(), 0L, Long.MAX_VALUE);
	}

	/**
	 * ノード情報 (パッケージ情報)の入力チェック
	 * 
	 * @param info ノード情報 (パッケージ情報)
	 */
	private static void validateNodePackageInfo(NodePackageInfo info) throws InvalidSetting {
		String DeviceTypeName = MessageConstant.NODE_PACKAGE.getMessage();
		// キー情報
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.NODE_PACKAGE_ID.getMessage() + "]", info.getPackageId(), true, 1, 1024);
		// 付属情報
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.NODE_PACKAGE_NAME.getMessage() + "]", info.getPackageName(), true, 0, 1024);
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.NODE_PACKAGE_VERSION.getMessage() + "]", info.getVersion(), false, 0, 1024);
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.NODE_PACKAGE_RELEASE.getMessage() + "]", info.getRelease(), false, 0, 1024);
		if (info.getInstallDate() != null) {
			CommonValidator.validateLong(DeviceTypeName + "[" + MessageConstant.NODE_PACKAGE_INSTALL_DATE.getMessage() + "]", info.getInstallDate(), 0, Long.MAX_VALUE);
		}
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.NODE_PACKAGE_VENDOR.getMessage() + "]", info.getVendor(), false, 0, 1024);
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.NODE_PACKAGE_ARCHITECTURE.getMessage() + "]", info.getArchitecture(), false, 0, 1024);
	}

	/**
	 * ノード情報 (個別導入支援情報)の入力チェック
	 * 
	 * @param info ノード情報 (個別導入支援情報)
	 */
	private static void validateNodeProductInfo(NodeProductInfo info) throws InvalidSetting {
		String DeviceTypeName = MessageConstant.NODE_PRODUCT.getMessage();
		// キー情報
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.NODE_PRODUCT_NAME.getMessage() + "]", info.getProductName(), true, 1, 1024);
		// 付属情報
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.NODE_PRODUCT_VERSION.getMessage() + "]", info.getVersion(), false, 0, 1024);
		// インストールパスは最大サイズをHinemosプロパティから取得
		int pathMaxSize = HinemosPropertyCommon.node_product_path_maxsize.getIntegerValue();
		if (pathMaxSize < 0) {
			pathMaxSize = 0;
		}
		if (info.getPath() != null && info.getPath().length() > pathMaxSize) {
			info.setPath(info.getPath().substring(0, pathMaxSize));
		}
	}

	/**
	 * ノード情報 (ライセンス情報)の入力チェック
	 * 
	 * @param info ノード情報 (ライセンス情報)
	 */
	private static void validateNodeLicenseInfo(NodeLicenseInfo info) throws InvalidSetting {
		String DeviceTypeName = MessageConstant.NODE_LICENSE.getMessage();
		// キー情報
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.NODE_LICENSE_PRODUCT_NAME.getMessage() + "]", info.getProductName(), true, 1, 1024);
		// 付属情報
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.NODE_LICENSE_VENDOR.getMessage() + "]", info.getVendor(), false, 0, 1024);
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.NODE_LICENSE_VENDOR_CONTACT.getMessage() + "]", info.getVendorContact(), false, 0, 1024);
		CommonValidator.validateString(DeviceTypeName + "[" + MessageConstant.NODE_LICENSE_SERIAL_NUMBER.getMessage() + "]", info.getSerialNumber(), false, 0, 1024);
		CommonValidator.validateInt(DeviceTypeName + "[" + MessageConstant.NODE_LICENSE_COUNT.getMessage() + "]", info.getCount(), -1, Integer.MAX_VALUE);
		if (info.getExpirationDate() != null) {
			CommonValidator.validateLong(DeviceTypeName + "[" + MessageConstant.NODE_LICENSE_EXPIRATION_DATE.getMessage() + "]", info.getExpirationDate(), 0, Long.MAX_VALUE);
		}
	}

	/**
	 * ノード情報 (ユーザ任意情報)の入力チェック
	 * 
	 * @param info ノード情報 (ユーザ任意情報)
	 */
	public static void validateNodeCustomInfo(NodeCustomInfo info) throws InvalidSetting {
		CommonValidator.validateId(MessageConstant.SETTING_CUSTOM_ID.getMessage(), info.getSettingCustomId(), 64);
		CommonValidator.validateString(MessageConstant.SETTING_CUSTOM_NAME.getMessage(), info.getDisplayName(), true, 1, 128);
		CommonValidator.validateString(MessageConstant.COMMAND.getMessage(), info.getCommand(), true, 1, 1024);
		CommonValidator.validateString(MessageConstant.VALUE.getMessage(), info.getValue(), true, 0, Integer.MAX_VALUE);
	}
	
}
