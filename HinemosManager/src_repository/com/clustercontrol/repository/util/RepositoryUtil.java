/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.util;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.SnmpVersionConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.jobmanagement.bean.SystemParameterConstant;
import com.clustercontrol.repository.model.NodeCpuInfo;
import com.clustercontrol.repository.model.NodeCustomInfo;
import com.clustercontrol.repository.model.NodeDiskInfo;
import com.clustercontrol.repository.model.NodeFilesystemInfo;
import com.clustercontrol.repository.model.NodeGeneralDeviceInfo;
import com.clustercontrol.repository.model.NodeHostnameInfo;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.NodeLicenseInfo;
import com.clustercontrol.repository.model.NodeMemoryInfo;
import com.clustercontrol.repository.model.NodeNetstatInfo;
import com.clustercontrol.repository.model.NodeNetworkInterfaceInfo;
import com.clustercontrol.repository.model.NodeNoteInfo;
import com.clustercontrol.repository.model.NodeOsInfo;
import com.clustercontrol.repository.model.NodePackageInfo;
import com.clustercontrol.repository.model.NodeProcessInfo;
import com.clustercontrol.repository.model.NodeProductInfo;
import com.clustercontrol.repository.model.NodeVariableInfo;
import com.clustercontrol.util.DateUtil;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.StringBinder;

/**
 * リポジトリに関するUtilityクラス<br/>
 *
 *
 */
public class RepositoryUtil {

	private static Log log = LogFactory.getLog(RepositoryUtil.class);
	private static final String DELIMITER = "() : ";
	private static final String DEFAULT_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
	/** DB上の最大値 */
	public static final int NODE_NODE_NAME_MAX_BYTE = 256;
	public static final int NODE_FACILITY_NAME_MAX_BYTE = 256;
	public static final int NODE_CLOUD_RESOURCE_NAME_MAX_BYTE = 128;

	/**
	 * ノードの基本情報をハッシュとして返す
	 * 
	 * @param nodeInfo
	 * @param keyList
	 *            変換対象のキー(余分な情報をメモリに持たせないため)
	 * @retur
	 */
	public static Map<String, String> createNodeParameter(NodeInfo nodeInfo, ArrayList<String> keyList) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		HashMap<String, String> nodeVariableMap = new HashMap<String, String>();
		// 引数チェック.
		if (nodeInfo == null) {
			log.warn(methodName + DELIMITER + "'nodeInfo' is null.");
			return nodeVariableMap;
		}
		if (keyList == null || keyList.isEmpty()) {
			// 置換キーを含まない文字列の場合はここに入る、のでdebug.
			log.debug(methodName + DELIMITER + "'keyList' is null.");
			return nodeVariableMap;
		}

		// いくつかのキーで共通で使う値を取得.
		String facilityId = nodeInfo.getFacilityId();
		NodeOsInfo osEntity = nodeInfo.getNodeOsInfo();
		boolean accessedOs = false;

		// 変動的なキーのチェック用のリスト作成
		List<NodeVariableInfo> nodeVariableList = nodeInfo.getNodeVariableInfo();
		if (nodeVariableList == null || nodeVariableList.isEmpty()) {
			log.debug(methodName + DELIMITER + "to get nodeVariableList from DB.");
			// 渡されたnodeInfoに詳細情報(cc_cfg_nodeテーブル以外の情報)が含まれていない場合があるので直接取得.
			nodeVariableList = QueryUtil.getNodeVariableInfoByFacilityId(facilityId);
		}
		if (nodeVariableList != null && !nodeVariableList.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			boolean isTop = true;
			for (NodeVariableInfo info : nodeVariableList) {
				if (info.getNodeVariableName() == null || "".equals(info.getNodeVariableName())) {
					log.warn(methodName + DELIMITER + "name of NodeVariable is empty.");
					continue;
				}
				nodeVariableMap.put(info.getNodeVariableName(), info.getNodeVariableValue());
				if(!isTop){
					sb.append(" ,");
				}
				sb.append("<" + info.getNodeVariableName() + "," + info.getNodeVariableValue() + ">");
			}
			log.debug(methodName + DELIMITER + "get nodeVariableList. list=[" + sb.toString() + "]");
		} else {
			log.debug(methodName + DELIMITER + "nodeVariableList is empty.");
		}

		// 置換処理に関するHinemosプロパティを取得.
		int maxChar = HinemosPropertyCommon.repository_node_replace_max_by_key.getIntegerValue();
		log.trace(methodName + DELIMITER + "repository.node.replace.max.by.key=[" + maxChar + "]");
		String columnSeparator = HinemosPropertyCommon.repository_node_replace_separator.getStringValue();
		log.trace(methodName + DELIMITER + "repository.node.replace.column.delimiter=[" + columnSeparator + "]");
		String dataSeparator = HinemosPropertyCommon.repository_node_replace_line_separator.getStringValue();
		log.trace(methodName + DELIMITER + "repository.node.replace.data.delimiter=[" + dataSeparator + "]");
		dataSeparator = dataSeparator.replace("CR", "\r");
		dataSeparator = dataSeparator.replace("LF", "\n");

		// 置換対象のキーの分だけ変換用の値をMapにセットする.
		Map<String, String> param = new HashMap<String, String>();
		for (String key : keyList) {
			if (key == null || key.isEmpty()) {
				log.warn(methodName + DELIMITER + "key is empty.");
				continue;
			}
			log.trace(methodName + DELIMITER + "key=[" + key + "]");
			String value = null;
			
			String[] splitedkey = StringBinder.splitPostfix(key);
			// :original以外(:quoteSh, :escapeCmd)が付与されていた場合は、元の置換キーで判定する。
			if (splitedkey[1] != null && !splitedkey[1].equals(SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				key = splitedkey[0];
			}
			
			if (key.equals(SystemParameterConstant.FACILITY_ID) || key
					.equals(SystemParameterConstant.FACILITY_ID + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = facilityId;

			} else if (key.equals(SystemParameterConstant.FACILITY_NAME) || key
					.equals(SystemParameterConstant.FACILITY_NAME + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getFacilityName();

			} else if (key.equals(SystemParameterConstant.DESCRIPTION) || key
					.equals(SystemParameterConstant.DESCRIPTION + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getDescription();

			} else if (key.equals(SystemParameterConstant.AUTO_DEVICE_SEARCH) || key.equals(
					SystemParameterConstant.AUTO_DEVICE_SEARCH + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = parseParam(nodeInfo.getAutoDeviceSearch(), true);

			} else if (key.equals(SystemParameterConstant.PLATFORM_FAMILY) || key
					.equals(SystemParameterConstant.PLATFORM_FAMILY + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getPlatformFamily();

			} else if (key.equals(SystemParameterConstant.SUB_PLATFORM_FAMILY) || key.equals(
					SystemParameterConstant.SUB_PLATFORM_FAMILY + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getSubPlatformFamily();

			} else if (key.equals(SystemParameterConstant.HARDWARE_TYPE) || key
					.equals(SystemParameterConstant.HARDWARE_TYPE + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getHardwareType();

			} else if (key.equals(SystemParameterConstant.IP_ADDRESS_VERSION) || key.equals(
					SystemParameterConstant.IP_ADDRESS_VERSION + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = parseParam(nodeInfo.getIpAddressVersion(), true);

			} else if (key.equals(SystemParameterConstant.IP_ADDRESS)
					|| key.equals(SystemParameterConstant.IP_ADDRESS + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getAvailableIpAddress();

			} else if (key.equals(SystemParameterConstant.IP_ADDRESS_V4) || key
					.equals(SystemParameterConstant.IP_ADDRESS_V4 + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getIpAddressV4();

			} else if (key.equals(SystemParameterConstant.IP_ADDRESS_V6) || key
					.equals(SystemParameterConstant.IP_ADDRESS_V6 + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getIpAddressV6();

			} else if (key.equals(SystemParameterConstant.NODE_NAME)
					|| key.equals(SystemParameterConstant.NODE_NAME + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getNodeName();

			} else if (key.equals(SystemParameterConstant.AGENT_AWAKE_PORT) || key
					.equals(SystemParameterConstant.AGENT_AWAKE_PORT + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = parseParam(nodeInfo.getAgentAwakePort(), true);

			} else if (key.equals(SystemParameterConstant.HOSTNAME)
					|| key.equals(SystemParameterConstant.HOSTNAME + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				List<NodeHostnameInfo> list = nodeInfo.getNodeHostnameInfo();
				if (list == null || list.isEmpty()) {
					// 渡されたnodeInfoに詳細情報(cc_cfg_nodeテーブル以外の情報)が含まれていない場合があるので、再取得.
					list = QueryUtil.getNodeHostnameInfoByFacilityId(facilityId);
				}
				if (list == null || list.isEmpty()) {
					log.debug(methodName + DELIMITER + "list is empty. (key = " + key + ")");
					value = null;
				} else {
					StringBuilder sb = new StringBuilder();
					boolean isTop = true;
					for (NodeHostnameInfo info : list) {
						if (!isTop) {
							sb.append(dataSeparator);
						}
						sb.append(info.getHostname());
						isTop = false;
					}
					value = sb.toString();

				}

			} else if (key.equals(SystemParameterConstant.OS_NAME)
					|| key.equals(SystemParameterConstant.OS_NAME + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				if (osEntity == null) {
					osEntity = getOsInfo(facilityId, accessedOs);
					accessedOs = true;
				}
				if (osEntity == null) {
					log.debug(methodName + DELIMITER + "osEntity is null. (key = " + key + ")");
					value = null;
				} else {
					value = nodeInfo.getNodeOsInfo().getOsName();
				}

			} else if (key.equals(SystemParameterConstant.OS_RELEASE)
					|| key.equals(SystemParameterConstant.OS_RELEASE + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				if (osEntity == null) {
					osEntity = getOsInfo(facilityId, accessedOs);
					accessedOs = true;
				}
				if (osEntity == null) {
					log.debug(methodName + DELIMITER + "osEntity is null. (key = " + key + ")");
					value = null;
				} else {
					value = nodeInfo.getNodeOsInfo().getOsRelease();
				}

			} else if (key.equals(SystemParameterConstant.OS_VERSION)
					|| key.equals(SystemParameterConstant.OS_VERSION + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				if (osEntity == null) {
					osEntity = getOsInfo(facilityId, accessedOs);
					accessedOs = true;
				}
				if (osEntity == null) {
					log.debug(methodName + DELIMITER + "osEntity is null. (key = " + key + ")");
					value = null;
				} else {
					value = nodeInfo.getNodeOsInfo().getOsVersion();
				}

			} else if (key.equals(SystemParameterConstant.CHARSET)
					|| key.equals(SystemParameterConstant.CHARSET + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				if (osEntity == null) {
					osEntity = getOsInfo(facilityId, accessedOs);
					accessedOs = true;
				}
				if (osEntity == null) {
					log.debug(methodName + DELIMITER + "osEntity is null. (key = " + key + ")");
					value = null;
				} else {
					value = nodeInfo.getNodeOsInfo().getCharacterSet();
				}

			} else if (key.equals(SystemParameterConstant.OS_STARTUP)
					|| key.equals(SystemParameterConstant.OS_STARTUP + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				if (osEntity == null) {
					osEntity = getOsInfo(facilityId, accessedOs);
					accessedOs = true;
				}
				if (osEntity == null) {
					log.debug(methodName + DELIMITER + "osEntity is null. (key = " + key + ")");
					value = null;
				} else {
					if (nodeInfo.getNodeOsInfo().getStartupDateTime() == null) {
						value = null;
					} else if (nodeInfo.getNodeOsInfo().getStartupDateTime() == 0) {
						// クライアントの画面表示に合わせ、値が0の場合は空白を表示
						value = "";
					} else {
						String date = getFormattedDate(nodeInfo.getNodeOsInfo().getStartupDateTime());
						value = date;
					}
				}

			} else if (key.equals(SystemParameterConstant.CPU)
					|| key.equals(SystemParameterConstant.CPU + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				List<NodeCpuInfo> list = nodeInfo.getNodeCpuInfo();
				if (list == null || list.isEmpty()) {
					// 渡されたnodeInfoに詳細情報(cc_cfg_nodeテーブル以外の情報)が含まれていない場合があるので、再取得.
					list = QueryUtil.getNodeCpuInfoByFacilityId(facilityId);
				}
				if (list == null || list.isEmpty()) {
					log.debug(methodName + DELIMITER + "list is empty. (key = " + key + ")");
					value = null;
				} else {
					StringBuilder sb = new StringBuilder();
					boolean isTop = true;
					for (NodeCpuInfo info : list) {
						if (!isTop) {
							sb.append(dataSeparator);
						}
						sb.append(info.getDeviceDisplayName());
						sb.append(columnSeparator + info.getDeviceName());
						sb.append(columnSeparator + parseParam(info.getDeviceIndex(), false));
						sb.append(columnSeparator + info.getDeviceType());
						if (info.getDeviceSize() == -1) {
							// クライアントの画面表示に合わせ、値が-1の場合は0を表示
							sb.append(columnSeparator + 0);
						} else {
							sb.append(columnSeparator + parseParam(info.getDeviceSize(), false));
						}
						sb.append(columnSeparator + info.getDeviceSizeUnit());
						sb.append(columnSeparator + info.getDeviceDescription());
						sb.append(columnSeparator + parseParam(info.getCoreCount(), false));
						sb.append(columnSeparator + parseParam(info.getThreadCount(), false));
						sb.append(columnSeparator + parseParam(info.getClockCount(), false));
						isTop = false;
					}
					value = sb.toString();
				}

			} else if (key.equals(SystemParameterConstant.MEMORY)
					|| key.equals(SystemParameterConstant.MEMORY + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				List<NodeMemoryInfo> list = nodeInfo.getNodeMemoryInfo();
				if (list == null || list.isEmpty()) {
					// 渡されたnodeInfoに詳細情報(cc_cfg_nodeテーブル以外の情報)が含まれていない場合があるので、再取得.
					list = QueryUtil.getNodeMemoryInfoByFacilityId(facilityId);
				}
				if (list == null || list.isEmpty()) {
					log.debug(methodName + DELIMITER + "list is empty. (key = " + key + ")");
					value = null;
				} else {
					StringBuilder sb = new StringBuilder();
					boolean isTop = true;
					for (NodeMemoryInfo info : list) {
						if (!isTop) {
							sb.append(dataSeparator);
						}
						sb.append(info.getDeviceDisplayName());
						sb.append(columnSeparator + info.getDeviceName());
						sb.append(columnSeparator + parseParam(info.getDeviceIndex(), false));
						sb.append(columnSeparator + info.getDeviceType());
						if (info.getDeviceSize() == -1) {
							// クライアントの画面表示に合わせ、値が-1の場合は0を表示
							sb.append(columnSeparator + 0);
						} else {
							sb.append(columnSeparator + parseParam(info.getDeviceSize(), false));
						}
						sb.append(columnSeparator + info.getDeviceSizeUnit());
						sb.append(columnSeparator + info.getDeviceDescription());
						isTop = false;
					}
					value = sb.toString();
				}

			} else if (key.equals(SystemParameterConstant.NIC)
					|| key.equals(SystemParameterConstant.NIC + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				List<NodeNetworkInterfaceInfo> list = nodeInfo.getNodeNetworkInterfaceInfo();
				if (list == null || list.isEmpty()) {
					// 渡されたnodeInfoに詳細情報(cc_cfg_nodeテーブル以外の情報)が含まれていない場合があるので、再取得.
					list = QueryUtil.getNodeNetworkInterfaceInfoByFacilityId(facilityId);
				}
				if (list == null || list.isEmpty()) {
					log.debug(methodName + DELIMITER + "list is empty. (key = " + key + ")");
					value = null;
				} else {
					StringBuilder sb = new StringBuilder();
					boolean isTop = true;
					for (NodeNetworkInterfaceInfo info : list) {
						if (!isTop) {
							sb.append(dataSeparator);
						}
						sb.append(info.getDeviceDisplayName());
						sb.append(columnSeparator + info.getDeviceName());
						sb.append(columnSeparator + parseParam(info.getDeviceIndex(), false));
						sb.append(columnSeparator + info.getDeviceType());
						if (info.getDeviceSize() == -1) {
							// クライアントの画面表示に合わせ、値が-1の場合は0を表示
							sb.append(columnSeparator + 0);
						} else {
							sb.append(columnSeparator + parseParam(info.getDeviceSize(), false));
						}
						sb.append(columnSeparator + info.getDeviceSizeUnit());
						sb.append(columnSeparator + info.getDeviceDescription());
						sb.append(columnSeparator + info.getNicIpAddress());
						sb.append(columnSeparator + info.getNicMacAddress());
						isTop = false;
					}
					value = sb.toString();
				}

			} else if (key.equals(SystemParameterConstant.DISK)
					|| key.equals(SystemParameterConstant.DISK + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				List<NodeDiskInfo> list = nodeInfo.getNodeDiskInfo();
				if (list == null || list.isEmpty()) {
					// 渡されたnodeInfoに詳細情報(cc_cfg_nodeテーブル以外の情報)が含まれていない場合があるので、再取得.
					list = QueryUtil.getNodeDiskInfoByFacilityId(facilityId);
				}
				if (list == null || list.isEmpty()) {
					log.debug(methodName + DELIMITER + "list is empty. (key = " + key + ")");
					value = null;
				} else {
					StringBuilder sb = new StringBuilder();
					boolean isTop = true;
					for (NodeDiskInfo info : list) {
						if (!isTop) {
							sb.append(dataSeparator);
						}
						sb.append(info.getDeviceDisplayName());
						sb.append(columnSeparator + info.getDeviceName());
						sb.append(columnSeparator + parseParam(info.getDeviceIndex(), false));
						sb.append(columnSeparator + info.getDeviceType());
						if (info.getDeviceSize() == -1) {
							// クライアントの画面表示に合わせ、値が-1の場合は0を表示
							sb.append(columnSeparator + 0);
						} else {
							sb.append(columnSeparator + parseParam(info.getDeviceSize(), false));
						}
						sb.append(columnSeparator + info.getDeviceSizeUnit());
						sb.append(columnSeparator + info.getDeviceDescription());
						sb.append(columnSeparator + info.getDiskRpm());
						isTop = false;
					}
					value = sb.toString();
				}

			} else if (key.equals(SystemParameterConstant.FILE_SYSTEM) || key
					.equals(SystemParameterConstant.FILE_SYSTEM + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				List<NodeFilesystemInfo> list = nodeInfo.getNodeFilesystemInfo();
				if (list == null || list.isEmpty()) {
					// 渡されたnodeInfoに詳細情報(cc_cfg_nodeテーブル以外の情報)が含まれていない場合があるので、再取得.
					list = QueryUtil.getNodeFilesystemInfoByFacilityId(facilityId);
				}
				if (list == null || list.isEmpty()) {
					log.debug(methodName + DELIMITER + "list is empty. (key = " + key + ")");
					value = null;
				} else {
					StringBuilder sb = new StringBuilder();
					boolean isTop = true;
					for (NodeFilesystemInfo info : list) {
						if (!isTop) {
							sb.append(dataSeparator);
						}
						sb.append(info.getDeviceDisplayName());
						sb.append(columnSeparator + info.getDeviceName());
						sb.append(columnSeparator + parseParam(info.getDeviceIndex(), false));
						sb.append(columnSeparator + info.getDeviceType());
						if (info.getDeviceSize() == -1) {
							// クライアントの画面表示に合わせ、値が-1の場合は0を表示
							sb.append(columnSeparator + 0);
						} else {
							sb.append(columnSeparator + parseParam(info.getDeviceSize(), false));
						}
						sb.append(columnSeparator + info.getDeviceSizeUnit());
						sb.append(columnSeparator + info.getDeviceDescription());
						sb.append(columnSeparator + info.getFilesystemType());
						isTop = false;
					}
					value = sb.toString();
				}

			} else if (key.equals(SystemParameterConstant.DEVICE)
					|| key.equals(SystemParameterConstant.DEVICE + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				List<NodeGeneralDeviceInfo> list = nodeInfo.getNodeDeviceInfo();
				if (list == null || list.isEmpty()) {
					// 渡されたnodeInfoに詳細情報(cc_cfg_nodeテーブル以外の情報)が含まれていない場合があるので、再取得.
					list = QueryUtil.getNodeGeneralDeviceInfoByFacilityId(facilityId);
				}
				if (list == null || list.isEmpty()) {
					log.debug(methodName + DELIMITER + "list is empty. (key = " + key + ")");
					value = null;
				} else {
					StringBuilder sb = new StringBuilder();
					boolean isTop = true;
					for (NodeGeneralDeviceInfo info : list) {
						if (!isTop) {
							sb.append(dataSeparator);
						}
						sb.append(info.getDeviceDisplayName());
						sb.append(columnSeparator + info.getDeviceName());
						sb.append(columnSeparator + parseParam(info.getDeviceIndex(), false));
						sb.append(columnSeparator + info.getDeviceType());
						if (info.getDeviceSize() == -1) {
							// クライアントの画面表示に合わせ、値が-1の場合は0を表示
							sb.append(columnSeparator + 0);
						} else {
							sb.append(columnSeparator + parseParam(info.getDeviceSize(), false));
						}
						sb.append(columnSeparator + info.getDeviceSizeUnit());
						sb.append(columnSeparator + info.getDeviceDescription());
						isTop = false;
					}
					value = sb.toString();
				}

			} else if (key.equals(SystemParameterConstant.NET_STAT)
					|| key.equals(SystemParameterConstant.NET_STAT + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				List<NodeNetstatInfo> list = nodeInfo.getNodeNetstatInfo();
				if (list == null || list.isEmpty()) {
					// 渡されたnodeInfoに詳細情報(cc_cfg_nodeテーブル以外の情報)が含まれていない場合があるので、再取得.
					list = QueryUtil.getNodeNetstatInfoByFacilityId(facilityId);
				}
				if (list == null || list.isEmpty()) {
					log.debug(methodName + DELIMITER + "list is empty. (key = " + key + ")");
					value = null;
				} else {
					StringBuilder sb = new StringBuilder();
					boolean isTop = true;
					for (NodeNetstatInfo info : list) {
						if (!isTop) {
							sb.append(dataSeparator);
						}
						sb.append(info.getProtocol());
						sb.append(columnSeparator + info.getLocalIpAddress());
						sb.append(columnSeparator + info.getLocalPort());
						sb.append(columnSeparator + info.getForeignIpAddress());
						sb.append(columnSeparator + info.getForeignPort());
						sb.append(columnSeparator + info.getProcessName());
						if (info.getPid() == -1) {
							// クライアントの画面表示に合わせ、値が-1の場合は空白を表示
							sb.append(columnSeparator + "");
						} else {
							sb.append(columnSeparator + parseParam(info.getPid(), false));
						}
						sb.append(columnSeparator + info.getStatus());
						isTop = false;
					}
					value = sb.toString();
				}

			} else if (key.equals(SystemParameterConstant.PROCESS)
					|| key.equals(SystemParameterConstant.PROCESS + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				List<NodeProcessInfo> list = nodeInfo.getNodeProcessInfo();
				if (list == null || list.isEmpty()) {
					// 渡されたnodeInfoに詳細情報(cc_cfg_nodeテーブル以外の情報)が含まれていない場合があるので、再取得.
					list = QueryUtil.getNodeProcessInfoByFacilityId(facilityId);
				}
				if (list == null || list.isEmpty()) {
					log.debug(methodName + DELIMITER + "list is empty. (key = " + key + ")");
					value = null;
				} else {
					StringBuilder sb = new StringBuilder();
					boolean isTop = true;
					for (NodeProcessInfo info : list) {
						if (!isTop) {
							sb.append(dataSeparator);
						}
						sb.append(info.getProcessName());
						if (info.getPid() == -1) {
							// クライアントの画面表示に合わせ、値が-1の場合は空白を表示
							sb.append(columnSeparator + "");
						} else {
							sb.append(columnSeparator + parseParam(info.getPid(), false));
						}
						sb.append(columnSeparator + info.getPath());
						sb.append(columnSeparator + info.getExecUser());
						if (info.getStartupDateTime() == 0) {
							// クライアントの画面表示に合わせ、値が0の場合は空白を表示
							sb.append(columnSeparator + "");
						} else {
							sb.append(columnSeparator + getFormattedDate(info.getStartupDateTime()));
						}
						isTop = false;
					}
					value = sb.toString();
				}

			} else if (key.equals(SystemParameterConstant.PACKAGE)
					|| key.equals(SystemParameterConstant.PACKAGE + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				List<NodePackageInfo> list = nodeInfo.getNodePackageInfo();
				if (list == null || list.isEmpty()) {
					// 渡されたnodeInfoに詳細情報(cc_cfg_nodeテーブル以外の情報)が含まれていない場合があるので、再取得.
					list = QueryUtil.getNodePackageInfoByFacilityId(facilityId);
				}
				if (list == null || list.isEmpty()) {
					log.debug(methodName + DELIMITER + "list is empty. (key = " + key + ")");
					value = null;
				} else {
					StringBuilder sb = new StringBuilder();
					boolean isTop = true;
					for (NodePackageInfo info : list) {
						if (!isTop) {
							sb.append(dataSeparator);
						}
						sb.append(info.getPackageId());
						sb.append(columnSeparator + info.getPackageName());
						sb.append(columnSeparator + info.getVersion());
						sb.append(columnSeparator + info.getRelease());
						if (info.getInstallDate() == 0) {
							// クライアントの画面表示に合わせ、値が0の場合は空白を表示
							sb.append(columnSeparator + "");
						} else {
							sb.append(columnSeparator + getFormattedDate(info.getInstallDate()));
						}
						sb.append(columnSeparator + info.getVendor());
						sb.append(columnSeparator + info.getArchitecture());
						isTop = false;
					}
					value = sb.toString();
				}

			} else if (key.equals(SystemParameterConstant.PRODUCT)
					|| key.equals(SystemParameterConstant.PRODUCT + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				List<NodeProductInfo> list = nodeInfo.getNodeProductInfo();
				if (list == null || list.isEmpty()) {
					// 渡されたnodeInfoに詳細情報(cc_cfg_nodeテーブル以外の情報)が含まれていない場合があるので、再取得.
					list = QueryUtil.getNodeProductInfoByFacilityId(facilityId);
				}
				if (list == null || list.isEmpty()) {
					log.debug(methodName + DELIMITER + "list is empty. (key = " + key + ")");
					value = null;
				} else {
					StringBuilder sb = new StringBuilder();
					boolean isTop = true;
					for (NodeProductInfo info : list) {
						if (!isTop) {
							sb.append(dataSeparator);
						}
						sb.append(info.getProductName());
						sb.append(columnSeparator + info.getVersion());
						sb.append(columnSeparator + info.getPath());
						isTop = false;
					}
					value = sb.toString();
				}

			} else if (key.equals(SystemParameterConstant.LICENSE)
					|| key.equals(SystemParameterConstant.LICENSE + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				List<NodeLicenseInfo> list = nodeInfo.getNodeLicenseInfo();
				if (list == null || list.isEmpty()) {
					// 渡されたnodeInfoに詳細情報(cc_cfg_nodeテーブル以外の情報)が含まれていない場合があるので、再取得.
					list = QueryUtil.getNodeLicenseInfoByFacilityId(facilityId);
				}
				if (list == null || list.isEmpty()) {
					log.debug(methodName + DELIMITER + "list is empty. (key = " + key + ")");
					value = null;
				} else {
					StringBuilder sb = new StringBuilder();
					boolean isTop = true;
					for (NodeLicenseInfo info : list) {
						if (!isTop) {
							sb.append(dataSeparator);
						}
						sb.append(info.getProductName());
						sb.append(columnSeparator + info.getVendor());
						sb.append(columnSeparator + info.getVendorContact());
						sb.append(columnSeparator + info.getSerialNumber());
						if (info.getCount() == -1) {
							// クライアントの画面表示に合わせ、値が-1の場合は空白を表示
							sb.append(columnSeparator + "");
						} else {
							sb.append(columnSeparator + parseParam(info.getCount(), false));
						}
						if (info.getExpirationDate() == 0) {
							// クライアントの画面表示に合わせ、値が0の場合は空白を表示
							sb.append(columnSeparator + "");
						} else {
							sb.append(columnSeparator + getFormattedDate(info.getExpirationDate()));
						}
						isTop = false;
					}
					value = sb.toString();
				}

			} else if (key.equals(SystemParameterConstant.CUSTOM)
					|| key.equals(SystemParameterConstant.CUSTOM + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				List<NodeCustomInfo> list = nodeInfo.getNodeCustomInfo();
				if (list == null || list.isEmpty()) {
					// 渡されたnodeInfoに詳細情報(cc_cfg_nodeテーブル以外の情報)が含まれていない場合があるので、再取得.
					list = QueryUtil.getNodeCustomByFacilityId(facilityId);
				}
				if (list == null || list.isEmpty()) {
					log.debug(methodName + DELIMITER + "list is empty. (key = " + key + ")");
					value = null;
				} else {
					StringBuilder sb = new StringBuilder();
					boolean isTop = true;
					for (NodeCustomInfo info : list) {
						if (!isTop) {
							sb.append(dataSeparator);
						}
						sb.append(info.getDisplayName());
						sb.append(columnSeparator + info.getValue());
						isTop = false;
					}
					value = sb.toString();
				}

			} else if (key.equals(SystemParameterConstant.JOB_PRIORITY) || key
					.equals(SystemParameterConstant.JOB_PRIORITY + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = parseParam(nodeInfo.getJobPriority(), true);

			} else if (key.equals(SystemParameterConstant.JOB_MULTIPLICITY) || key
					.equals(SystemParameterConstant.JOB_MULTIPLICITY + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = parseParam(nodeInfo.getJobMultiplicity(), true);

			} else if (key.equals(SystemParameterConstant.SNMP_USER)
					|| key.equals(SystemParameterConstant.SNMP_USER + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getSnmpUser();

			} else if (key.equals(SystemParameterConstant.SNMP_PORT)
					|| key.equals(SystemParameterConstant.SNMP_PORT + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = parseParam(nodeInfo.getSnmpPort(), true);

			} else if (key.equals(SystemParameterConstant.SNMP_COMMUNITY) || key
					.equals(SystemParameterConstant.SNMP_COMMUNITY + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getSnmpCommunity();

			} else if (key.equals(SystemParameterConstant.SNMP_VERSION) || key
					.equals(SystemParameterConstant.SNMP_VERSION + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				if (nodeInfo.getSnmpVersion() == null) {
					value = null;
				} else {
					value = SnmpVersionConstant.typeToString(nodeInfo.getSnmpVersion());
				}

			} else if (key.equals(SystemParameterConstant.SNMP_SECURITY_LEVEL) || key.equals(
					SystemParameterConstant.SNMP_SECURITY_LEVEL + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getSnmpSecurityLevel();

			} else if (key.equals(SystemParameterConstant.SNMP_AUTH_PASSWORD) || key.equals(
					SystemParameterConstant.SNMP_AUTH_PASSWORD + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getSnmpAuthPassword();

			} else if (key.equals(SystemParameterConstant.SNMP_PRIV_PASSWORD) || key.equals(
					SystemParameterConstant.SNMP_PRIV_PASSWORD + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getSnmpPrivPassword();

			} else if (key.equals(SystemParameterConstant.SNMP_AUTH_PROTOCOL) || key.equals(
					SystemParameterConstant.SNMP_AUTH_PROTOCOL + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getSnmpAuthProtocol();

			} else if (key.equals(SystemParameterConstant.SNMP_PRIV_PROTOCOL) || key.equals(
					SystemParameterConstant.SNMP_PRIV_PROTOCOL + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getSnmpPrivProtocol();

			} else if (key.equals(SystemParameterConstant.SNMP_TIMEOUT) || key
					.equals(SystemParameterConstant.SNMP_TIMEOUT + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = parseParam(nodeInfo.getSnmpTimeout(), true);

			} else if (key.equals(SystemParameterConstant.SNMP_TRIES)
					|| key.equals(SystemParameterConstant.SNMP_TRIES + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = parseParam(nodeInfo.getSnmpRetryCount(), true);

			} else if (key.equals(SystemParameterConstant.WBEM_USER)
					|| key.equals(SystemParameterConstant.WBEM_USER + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getWbemUser();

			} else if (key.equals(SystemParameterConstant.WBEM_PORT)
					|| key.equals(SystemParameterConstant.WBEM_PORT + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = parseParam(nodeInfo.getWbemPort(), true);

			} else if (key.equals(SystemParameterConstant.WBEM_PROTOCOL) || key
					.equals(SystemParameterConstant.WBEM_PROTOCOL + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getWbemProtocol();

			} else if (key.equals(SystemParameterConstant.WBEM_TIMEOUT) || key
					.equals(SystemParameterConstant.WBEM_TIMEOUT + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = parseParam(nodeInfo.getWbemTimeout(), true);

			} else if (key.equals(SystemParameterConstant.WBEM_TRIES)
					|| key.equals(SystemParameterConstant.WBEM_TRIES + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = parseParam(nodeInfo.getWbemRetryCount(), true);

			} else if (key.equals(SystemParameterConstant.WBEM_USER)
					|| key.equals(SystemParameterConstant.WBEM_USER + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getWbemUser();

			} else if (key.equals(SystemParameterConstant.WBEM_PASSWORD) || key
					.equals(SystemParameterConstant.WBEM_PASSWORD + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getWbemUserPassword();

			} else if (key.equals(SystemParameterConstant.WINRM_USER)
					|| key.equals(SystemParameterConstant.WINRM_USER + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getWinrmUser();

			} else if (key.equals(SystemParameterConstant.WINRM_PASSWORD) || key
					.equals(SystemParameterConstant.WINRM_PASSWORD + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getWinrmUserPassword();

			} else if (key.equals(SystemParameterConstant.WINRM_VERSION) || key
					.equals(SystemParameterConstant.WINRM_VERSION + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getWinrmVersion();

			} else if (key.equals(SystemParameterConstant.WINRM_PORT)
					|| key.equals(SystemParameterConstant.WINRM_PORT + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = parseParam(nodeInfo.getWinrmPort(), true);

			} else if (key.equals(SystemParameterConstant.WINRM_PROTOCOL) || key
					.equals(SystemParameterConstant.WINRM_PROTOCOL + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getWinrmProtocol();

			} else if (key.equals(SystemParameterConstant.WINRM_TIMEOUT) || key
					.equals(SystemParameterConstant.WINRM_TIMEOUT + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = parseParam(nodeInfo.getWinrmTimeout(), true);

			} else if (key.equals(SystemParameterConstant.WINRM_TRIES) || key
					.equals(SystemParameterConstant.WINRM_TRIES + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = parseParam(nodeInfo.getWinrmRetries(), true);

			} else if (key.equals(SystemParameterConstant.IPMI_IP_ADDRESS) || key
					.equals(SystemParameterConstant.IPMI_IP_ADDRESS + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getIpmiIpAddress();

			} else if (key.equals(SystemParameterConstant.IPMI_PORT)
					|| key.equals(SystemParameterConstant.IPMI_PORT + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = parseParam(nodeInfo.getIpmiPort(), true);

			} else if (key.equals(SystemParameterConstant.IPMI_TIMEOUT) || key
					.equals(SystemParameterConstant.IPMI_TIMEOUT + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = parseParam(nodeInfo.getIpmiTimeout(), true);

			} else if (key.equals(SystemParameterConstant.IPMI_TRIES)
					|| key.equals(SystemParameterConstant.IPMI_TRIES + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = parseParam(nodeInfo.getIpmiRetries(), true);

			} else if (key.equals(SystemParameterConstant.IPMI_PROTOCOL) || key
					.equals(SystemParameterConstant.IPMI_PROTOCOL + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getIpmiProtocol();

			} else if (key.equals(SystemParameterConstant.IPMI_LEVEL)
					|| key.equals(SystemParameterConstant.IPMI_LEVEL + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getIpmiLevel();

			} else if (key.equals(SystemParameterConstant.IPMI_USER)
					|| key.equals(SystemParameterConstant.IPMI_USER + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getIpmiUser();

			} else if (key.equals(SystemParameterConstant.IPMI_PASSWORD) || key
					.equals(SystemParameterConstant.IPMI_PASSWORD + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getIpmiUserPassword();

			} else if (key.equals(SystemParameterConstant.SSH_USER)
					|| key.equals(SystemParameterConstant.SSH_USER + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getSshUser();

			} else if (key.equals(SystemParameterConstant.SSH_USER_PASSWORD) || key.equals(
					SystemParameterConstant.SSH_USER_PASSWORD + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getSshUserPassword();

			} else if (key.equals(SystemParameterConstant.SSH_PRIVATE_KEY_FILENAME) || key.equals(
					SystemParameterConstant.SSH_PRIVATE_KEY_FILENAME + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getSshPrivateKeyFilepath();

			} else if (key.equals(SystemParameterConstant.SSH_PRIVATE_KEY_PASSPHRASE)
					|| key.equals(SystemParameterConstant.SSH_PRIVATE_KEY_PASSPHRASE
							+ SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getSshPrivateKeyPassphrase();

			} else if (key.equals(SystemParameterConstant.SSH_PORT)
					|| key.equals(SystemParameterConstant.SSH_PORT + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = parseParam(nodeInfo.getSshPort(), true);

			} else if (key.equals(SystemParameterConstant.SSH_TIMEOUT) || key
					.equals(SystemParameterConstant.SSH_TIMEOUT + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = parseParam(nodeInfo.getSshTimeout(), true);

			} else if (key.equals(SystemParameterConstant.CLOUD_SERVICE) || key
					.equals(SystemParameterConstant.CLOUD_SERVICE + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getCloudService();

			} else if (key.equals(SystemParameterConstant.CLOUD_SCOPE) || key
					.equals(SystemParameterConstant.CLOUD_SCOPE + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getCloudScope();

			} else if (key.equals(SystemParameterConstant.CLOUD_RESOURCE_TYPE) || key.equals(
					SystemParameterConstant.CLOUD_RESOURCE_TYPE + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getCloudResourceType();

			} else if (key.equals(SystemParameterConstant.CLOUD_RESOURCE_ID) || key.equals(
					SystemParameterConstant.CLOUD_RESOURCE_ID + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getCloudResourceId();

			} else if (key.equals(SystemParameterConstant.CLOUD_RESOURCE_NAME) || key.equals(
					SystemParameterConstant.CLOUD_RESOURCE_NAME + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getCloudResourceName();

			} else if (key.equals(SystemParameterConstant.CLOUD_LOCATION) || key
					.equals(SystemParameterConstant.CLOUD_LOCATION + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getCloudLocation();

			} else if (key.equals(SystemParameterConstant.RPA_LOG_DIRECTORY)
					|| key.equals(SystemParameterConstant.RPA_LOG_DIRECTORY + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getRpaLogDir();

			} else if (key.equals(SystemParameterConstant.RPA_MGMT_TOOL_TYPE)
					|| key.equals(SystemParameterConstant.RPA_MGMT_TOOL_TYPE + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getRpaManagementToolType();

			} else if (key.equals(SystemParameterConstant.RPA_RESOURCE_ID)
					|| key.equals(SystemParameterConstant.RPA_RESOURCE_ID + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getRpaResourceId();

			} else if (key.equals(SystemParameterConstant.RPA_USER)
					|| key.equals(SystemParameterConstant.RPA_USER + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getRpaUser();

			} else if (key.equals(SystemParameterConstant.RPA_EXEC_ENV_ID)
					|| key.equals(SystemParameterConstant.RPA_EXEC_ENV_ID + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getRpaExecEnvId();

			} else if (key.equals(SystemParameterConstant.CLOUD_LOG_PRIORITY) || key
					.equals(SystemParameterConstant.CLOUD_LOG_PRIORITY + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = parseParam(nodeInfo.getCloudLogPriority(), true);
		
			} else if (key.equals(SystemParameterConstant.ADMINISTRATOR) || key
					.equals(SystemParameterConstant.ADMINISTRATOR + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getAdministrator();

			} else if (key.equals(SystemParameterConstant.CONTACT)
					|| key.equals(SystemParameterConstant.CONTACT + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getContact();

			} else if (key.equals(SystemParameterConstant.CREATE_USER_ID) || key
					.equals(SystemParameterConstant.CREATE_USER_ID + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getCreateUserId();

			} else if (key.equals(SystemParameterConstant.CREATE_DATETIME) || key
					.equals(SystemParameterConstant.CREATE_DATETIME + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				if (nodeInfo.getCreateDatetime() == null) {
					value = null;
				} else if (nodeInfo.getCreateDatetime() == 0) {
					// クライアントの画面表示に合わせ、値が0の場合は空白を表示
					value = "";
				} else {
					String date = getFormattedDate(nodeInfo.getCreateDatetime());
					value = date;
				}

			} else if (key.equals(SystemParameterConstant.MODIFY_USER_ID) || key
					.equals(SystemParameterConstant.MODIFY_USER_ID + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				value = nodeInfo.getModifyUserId();

			} else if (key.equals(SystemParameterConstant.MODIFY_DATETIME) || key
					.equals(SystemParameterConstant.MODIFY_DATETIME + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				if (nodeInfo.getModifyDatetime() == null) {
					value = null;
				} else if (nodeInfo.getModifyDatetime() == 0) {
					// クライアントの画面表示に合わせ、値が0の場合は空白を表示
					value = "";
				} else {
					String date = getFormattedDate(nodeInfo.getModifyDatetime());
					value = date;
				}

			} else if (key.equals(SystemParameterConstant.NOTE)
					|| key.equals(SystemParameterConstant.NOTE + SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				List<NodeNoteInfo> list = nodeInfo.getNodeNoteInfo();
				if (list == null || list.isEmpty()) {
					// 渡されたnodeInfoに詳細情報(cc_cfg_nodeテーブル以外の情報)が含まれていない場合があるので、再取得.
					list = QueryUtil.getNodeNoteInfoByFacilityId(facilityId);
				}
				if (list == null || list.isEmpty()) {
					log.debug(methodName + DELIMITER + "list is empty. (key = " + key + ")");
					value = null;
				} else {
					log.trace(methodName + DELIMITER + "nodeInfo.getNodeNoteInfo().size()=" + list.size());
					StringBuilder sb = new StringBuilder();
					boolean isTop = true;
					for (NodeNoteInfo info : list) {
						if (!isTop) {
							sb.append(dataSeparator);
						}
						sb.append(info.getNote());
						isTop = false;
					}
					value = sb.toString();
				}

			} else if (nodeVariableMap.containsKey(key)) {
				log.trace(methodName + DELIMITER + "adding user parameter. (key = " + key + ", value = "
						+ nodeVariableMap.get(key) + ")");
				value = nodeVariableMap.get(key);
			} else {
				log.debug(methodName + DELIMITER + "key is not for Node property. (key = " + key + ")");
				continue;
			}

			// 値が最大長を超えてたらカット.
			if (maxChar > 3 && value != null && value.length() > maxChar) {
				value = value.substring(0, maxChar - 3);
				value = value + "...";
				log.info(methodName + DELIMITER + "adgust value because it's over the maximum length. key=" + key // 置換キー
						+ ", maximum=" + maxChar);
				log.trace(methodName + DELIMITER + "value(original)=" + value);
			}

			// 純粋なキー名のみを取り出す.
			if (key.contains(SystemParameterConstant.NOT_REPLACE_TO_ESCAPE)) {
				int startIndex = key.indexOf(SystemParameterConstant.NOT_REPLACE_TO_ESCAPE);
				key = key.substring(0, startIndex);
			}

			// 置換対象のキーと値をセット.
			param.put(key, value);
			if (value == null) {
				value = "null";
			}
			log.trace(methodName + DELIMITER + "put parameter to replace. key=" + key + ", value=" + value);
		}
		return param;

	}

	/**
	 * toString()メソッドが定義されたオブジェクトを文字列型に置換します.
	 * 
	 * @param value
	 *            置換対象のオブジェクト、toString()メソッドが定義されている前提です.
	 * @param toNull
	 *            true:オブジェクトがnullの時に、nullのまま返却する<br>
	 *            false:オブジェクトがnullの時に、空文字として返却する<br>
	 * @return 置換された文字列
	 */
	private static String parseParam(Object value, boolean toNull) {
		if (value == null) {
			if (toNull) {
				return null;
			}
			return "null";
		}
		// Booleanは"TRUE" or "FALSE" の文字列.
		if (value instanceof Boolean) {
			return value.toString().toUpperCase();
		}
		return value.toString();
	}

	/**
	 * 指定されたミリ秒をキー内で指定された形式に変換します<br>
	 * 
	 * @param value
	 *            フォーマット対象のミリ秒
	 * @return フォーマットされた日付、引数不正の場合はnull返却<br>
	 *         例) "2018年04月06日"
	 */
	private static String getFormattedDate(Long value) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		String dateFormat = HinemosPropertyCommon.repository_node_replace_date_format.getStringValue();
		if (dateFormat == null) {
			dateFormat = DEFAULT_DATE_FORMAT;
		}
		String date = null;
		try {
			date = DateUtil.millisToString(value, dateFormat);
			if (date == null) {
				log.warn(methodName + DELIMITER + "the format of date defined as Hinemos-property is invalid. format = "
						+ dateFormat);
				return null;
			}
		} catch (InvalidSetting e) {
			log.debug(methodName + DELIMITER + "the format of date is invalid.  format = " + dateFormat);
			return null;
		}
		return date;
	}

	/**
	 * 
	 * OS情報をDBから取得. <br>
	 * <br>
	 * 渡されたnodeInfoに詳細情報(cc_cfg_nodeテーブル以外の情報)が含まれていない場合があるので、再取得.
	 * 
	 */
	private static NodeOsInfo getOsInfo(String facilityId, boolean accessed) {
		// 一度アクセスした上でOS情報を持っていない場合は何度もDBアクセスさせない.
		if (accessed) {
			return null;
		}

		try {
			NodeOsInfo osEntity = QueryUtil.getNodeOsEntityPK(facilityId);
			return osEntity;
		} catch (FacilityNotFound e) {
			// 呼び出し先でログ出力してるので何もしない.
			return null;
		}
	}

	/**
	 * IPV4を数値型に変換します。
	 * @param ary オクテットごとに区切られたint型のアドレス
	 * @return int
	 */
	public static int ipV4ToInt(String addr) {
		int[] ary = new int[4];
		String[] strAry = addr.split("\\.");

		for (int i = 0; i < 4; i++ ) {
			ary[i] = Integer.parseInt(strAry[i]);
		}

		int l = ary[0] << 24;
        l += ary[1] << 16;
        l += ary[2] << 8;
        l += ary[3];

        return l;
	}

	/**
	 * INT型をIPアドレス(IPv4)文字列へ変換します。
	 * @param i IPv4アドレスの数値
	 * @return String
	 */
	public static String intToIpV4(int i) {
		int b1 = (i >> 24) & 0xff;
		int b2 = (i >> 16) & 0xff;
		int b3 = (i >> 8) & 0xff;
		int b4 = i & 0xff;

		return b1 + "." + b2 + "." + b3 + "." + b4;
	}

	/**
	 * BigInteger型をIPアドレス(IPv6)文字列へ変換します。
	 * @param argInt IPv6アドレスの数値
	 * @return String
	 */
	public static String bigIntToIpV6(BigInteger argInt) {

		StringBuilder str = new StringBuilder();
		for (int i=15; i>=0; i--) {
			int shift = 8 * i;
			Integer n = 0xff;
			BigInteger num = argInt.shiftRight(shift).and(new BigInteger(n.toString()));
			int intNum = num.intValue();
			String s = Integer.toHexString(intNum);
			if (s.length() < 2) {
				s = "0" + s;
			}
			str.append(s);
			if (i > 0 && i < 15) {
				int f = i % 2;
				str.append(f == 0 ? ":" : "");
			}
		}
		return str.toString();
	}

	/**
	 * byte配列をBigInteger型へ変換します。
	 * @param ary バイト配列
	 * @return　BigInteger
	 */
	public static BigInteger byteToBigIntV6(byte[] ary) {

		BigInteger ret = new BigInteger(ary);
		log.debug("ary=" + ret.toString());

		return ret;
	}

	/**
	 * 指定された範囲のIPアドレスリストを取得します。
	 * @param strFrom 開始アドレス
	 * @param strTo 終了アドレス
	 * @param version IPアドレスのバージョン(4 or 6)
	 * @return
	 * @throws HinemosUnknown
	 * @throws UnknownHostException
	 */
	public static List<String> getIpList (String strFrom, String strTo, int version) throws HinemosUnknown, UnknownHostException {
		List<String> list = new ArrayList<String>();
		if (version == 4) {
			int from = ipV4ToInt(strFrom);
			int to = ipV4ToInt(strTo);
			if(from > to) {
				throw new HinemosUnknown(MessageConstant.MESSAGE_PLEASE_SET_CORRECT_RANGE_OF_IP_ADDRESSES.getMessage());
			}
			for (int i = from ; i <= to; i ++) {
				list.add(intToIpV4(i));
			}
		} else {
			BigInteger from = byteToBigIntV6(InetAddress.getByName(strFrom).getAddress());
			BigInteger to = byteToBigIntV6(InetAddress.getByName(strTo).getAddress());

			if(from.compareTo(to) > 0) {
				throw new HinemosUnknown(MessageConstant.MESSAGE_PLEASE_SET_CORRECT_RANGE_OF_IP_ADDRESSES.getMessage());
			} else if (from.compareTo(to) == 0) {
				list.add(bigIntToIpV6(from));
			} else {
				int i = 0;
				while(true) {
					list.add(bigIntToIpV6(from));
					from = from.add(BigInteger.ONE);
					i++;
					if (i > 256 || from.compareTo(to) >= 0) {
						break;
					}
				}
			}
		}
		return list;
	}
}
