/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.factory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.repository.bean.NodeConfigSettingItem;
import com.clustercontrol.repository.bean.NodeRegisterFlagConstant;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.NodeConfigSettingInfo;
import com.clustercontrol.repository.model.NodeConfigSettingItemInfo;
import com.clustercontrol.repository.model.NodeCpuInfo;
import com.clustercontrol.repository.model.NodeCustomInfo;
import com.clustercontrol.repository.model.NodeCustomInfoPK;
import com.clustercontrol.repository.model.NodeDeviceInfo;
import com.clustercontrol.repository.model.NodeDiskInfo;
import com.clustercontrol.repository.model.NodeFilesystemInfo;
import com.clustercontrol.repository.model.NodeHistory;
import com.clustercontrol.repository.model.NodeHostnameInfo;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.NodeMemoryInfo;
import com.clustercontrol.repository.model.NodeNetstatInfo;
import com.clustercontrol.repository.model.NodeNetworkInterfaceInfo;
import com.clustercontrol.repository.model.NodeOsInfo;
import com.clustercontrol.repository.model.NodePackageInfo;
import com.clustercontrol.repository.model.NodeVariableInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.repository.util.NodeConfigRegisterUtil;
import com.clustercontrol.repository.util.NodeConfigRegisterUtil.NodeConfigRegisterDiffInfo;
import com.clustercontrol.repository.util.QueryUtil;
import com.clustercontrol.repository.util.RepositoryValidator;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * 構成情報登録用ファクトリクラス
 *
 */
public class NodeConfigRegister {

	private static Log m_log = LogFactory.getLog(NodeConfigRegister.class);

	// 構成情報を取得した日時
	private Long m_registerDatetime = 0L;
	// 更新ユーザID
	private String m_modifyUserId = "";
	// 登録する構成情報
	private NodeInfo m_nodeInfo = null;
	// 対象構成情報
	private NodeConfigSettingInfo m_settingInfo = null;

	/**
	 * コンストラクタ
	 * 
	 * @param registerDatetime 構成情報を送信した日時
	 * @param modifyUserId 更新ユーザID
	 * @param nodeInfo 登録する構成情報
	 * @param nodeConfigSetting 対象構成情報
	 */
	public NodeConfigRegister (Long registerDatetime, String modifyUserId, NodeInfo nodeInfo, NodeConfigSettingInfo settingInfo) {
		m_registerDatetime = registerDatetime;
		m_modifyUserId = modifyUserId;
		m_nodeInfo = nodeInfo;
		m_settingInfo = settingInfo;
	}

	/**
	 * 
	 * 構成情報登録実行
	 * 
	 * @return 通知情報リスト
	 * @throws FacilityNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<OutputBasicInfo> exec() throws FacilityNotFound, InvalidRole, HinemosUnknown {

		/** 収集対象ごとの対象構成情報を設定する(SettingItemIdリスト) */
		List<String> settingItemInfoList = createSettingItemInfoList(m_settingInfo);

		// 通知情報を保持する
		List<OutputBasicInfo> outputBasicInfoList = new ArrayList<>();

		// 変更ありフラグ
		HashMap<NodeConfigSettingItem, NodeConfigRegisterDiffInfo> diffMap = new HashMap<>();

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// cc_node_historyテーブルへの登録 
			NodeHistory history = new NodeHistory(m_nodeInfo.getFacilityId(), m_registerDatetime);

			/** 登録処理 (OS情報) */
			if (settingItemInfoList.contains(NodeConfigSettingItem.OS.name())) {
				if (m_nodeInfo.getNodeOsRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
					// 入力チェック
					List<String> msgList = RepositoryValidator.validateNodeOsConfigInfo(m_nodeInfo.getNodeOsInfo());
					for (String msg : msgList) {
						outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.OS.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_OS.getMessage()),
								msg, 
								m_settingInfo));
					}
					// 登録処理
					if (msgList.size() <= 0) {
						diffMap.put(NodeConfigSettingItem.OS, 
								NodeConfigRegisterUtil.registerNodeOsInfo(m_registerDatetime, m_modifyUserId, m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeOsInfo(), true));
						// cc_node_historyテーブルへの登録 
						history.setOsFlag(true);
					}
				} else if (m_nodeInfo.getNodeOsRegisterFlag().intValue() == NodeRegisterFlagConstant.NOT_GET) {
					// 対象外
				} else if (m_nodeInfo.getNodeOsRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_FAILURE) {
					// 取得失敗
					outputBasicInfoList.add(
						createOutputBasicList(
							NodeConfigSettingItem.OS.name(),
							PriorityConstant.TYPE_WARNING, 
							MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
									MessageConstant.NODE_CONFIG_SETTING_OS.getMessage()),
							MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
									MessageConstant.NODE_CONFIG_SETTING_OS.getMessage()), 
							m_settingInfo));
				} else {
					// 構成情報設定フラグエラー
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CORRECT_VALUE.getMessage(
							MessageConstant.NODE_REGISTER_FLAG.getMessage(), MessageConstant.OS.getMessage()));
					m_log.info("exec() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					outputBasicInfoList.add(
						createOutputBasicList(
							NodeConfigSettingItem.OS.name(),
							PriorityConstant.TYPE_WARNING, 
							MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
									MessageConstant.NODE_CONFIG_SETTING_OS.getMessage()),
							e.getMessage(), 
							m_settingInfo));
				}
			}
	
			/** 登録処理 (CPU情報) */
			if (settingItemInfoList.contains(NodeConfigSettingItem.HW_CPU.name())) {
				if (m_nodeInfo.getNodeCpuRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
					// 入力チェック
					List<String> msgList = RepositoryValidator.validateNodeCpuConfigInfo(m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeCpuInfo());
					for (String msg : msgList) {
						outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HW_CPU.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_CPU.getMessage()),
								msg, 
								m_settingInfo));
					}
					if (msgList.size() <= 0) {
						// 登録処理
						diffMap.put(NodeConfigSettingItem.HW_CPU,
								NodeConfigRegisterUtil.registerNodeCpuInfo(m_registerDatetime, m_modifyUserId, m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeCpuInfo(), true));
						// cc_node_historyテーブルへの登録 
						history.setCpuFlag(true);
					}
				} else if (m_nodeInfo.getNodeCpuRegisterFlag().intValue() == NodeRegisterFlagConstant.NOT_GET) {
					// 対象外
				} else if (m_nodeInfo.getNodeCpuRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_FAILURE) {
					// 取得失敗
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HW_CPU.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_CPU.getMessage()),
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_CPU.getMessage()),
								m_settingInfo));
				} else {
					// 構成情報設定フラグエラー
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CORRECT_VALUE.getMessage(
							MessageConstant.NODE_REGISTER_FLAG.getMessage(), MessageConstant.CPU_LIST.getMessage()));
					m_log.info("exec() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HW_CPU.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_CPU.getMessage()),
								e.getMessage(), 
								m_settingInfo));
				}
			}		

			/** 登録処理 (Disk情報) */
			if (settingItemInfoList.contains(NodeConfigSettingItem.HW_DISK.name())) {
				if (m_nodeInfo.getNodeDiskRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
					// 入力チェック
					List<String> msgList = RepositoryValidator.validateNodeDiskConfigInfo(m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeDiskInfo());
					for (String msg : msgList) {
						outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HW_DISK.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_DISK.getMessage()),
								msg, 
								m_settingInfo));
					}
					if (msgList.size() <= 0) {
						// 登録処理
						diffMap.put(NodeConfigSettingItem.HW_DISK,
								NodeConfigRegisterUtil.registerNodeDiskInfo(m_registerDatetime, m_modifyUserId, m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeDiskInfo(), true));
						// cc_node_historyテーブルへの登録 
						history.setDiskFlag(true);
					}
				} else if (m_nodeInfo.getNodeDiskRegisterFlag().intValue() == NodeRegisterFlagConstant.NOT_GET) {
					// 対象外
				} else if (m_nodeInfo.getNodeDiskRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_FAILURE) {
					// 取得失敗
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HW_DISK.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_DISK.getMessage()),
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_DISK.getMessage()), 
								m_settingInfo));
				} else {
					// 構成情報設定フラグエラー
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CORRECT_VALUE.getMessage(
							MessageConstant.NODE_REGISTER_FLAG.getMessage(), MessageConstant.DISK_LIST.getMessage()));
					m_log.info("exec() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HW_DISK.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_DISK.getMessage()),
								e.getMessage(), 
								m_settingInfo));
				}
			}		

			/** 登録処理 (Filesystem情報) */
			if (settingItemInfoList.contains(NodeConfigSettingItem.HW_FILESYSTEM.name())) {
				if (m_nodeInfo.getNodeFilesystemRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
					// 入力チェック
					List<String> msgList = RepositoryValidator.validateNodeFilesystemConfigInfo(m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeFilesystemInfo());
					for (String msg : msgList) {
						outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HW_FILESYSTEM.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_FILESYSTEM.getMessage()),
								msg, 
								m_settingInfo));
					}
					if (msgList.size() <= 0) {
						// 登録処理
						diffMap.put(NodeConfigSettingItem.HW_FILESYSTEM,
								NodeConfigRegisterUtil.registerNodeFilesystemInfo(m_registerDatetime, m_modifyUserId, m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeFilesystemInfo(), true));
						// cc_node_historyテーブルへの登録 
						history.setFilesystemFlag(true);
					}
				} else if (m_nodeInfo.getNodeFilesystemRegisterFlag().intValue() == NodeRegisterFlagConstant.NOT_GET) {
					// 対象外
				} else if (m_nodeInfo.getNodeFilesystemRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_FAILURE) {
					// 取得失敗
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HW_FILESYSTEM.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_FILESYSTEM.getMessage()),
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_FILESYSTEM.getMessage()),
								m_settingInfo));
				} else {
					// 構成情報設定フラグエラー
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CORRECT_VALUE.getMessage(
							MessageConstant.NODE_REGISTER_FLAG.getMessage(), MessageConstant.FILE_SYSTEM_LIST.getMessage()));
					m_log.info("exec() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HW_FILESYSTEM.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_FILESYSTEM.getMessage()),
								e.getMessage(), 
								m_settingInfo));
				}
			}		

			/** 登録処理 (Hostname情報) */
			if (settingItemInfoList.contains(NodeConfigSettingItem.HOSTNAME.name())) {
				if (m_nodeInfo.getNodeHostnameRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
					// 入力チェック
					List<String> msgList = RepositoryValidator.validateNodeHostnameConfigInfo(m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeHostnameInfo());
					for (String msg : msgList) {
						outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HOSTNAME.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HOSTNAME.getMessage()),
								msg, 
								m_settingInfo));
					}
					if (msgList.size() <= 0) {
						// 登録処理
						diffMap.put(NodeConfigSettingItem.HOSTNAME,
								NodeConfigRegisterUtil.registerNodeHostnameInfo(m_registerDatetime, m_modifyUserId, m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeHostnameInfo(), true));
						// cc_node_historyテーブルへの登録 
						history.setHostnameFlag(true);
					}
				} else if (m_nodeInfo.getNodeHostnameRegisterFlag().intValue() == NodeRegisterFlagConstant.NOT_GET) {
					// 対象外
				} else if (m_nodeInfo.getNodeHostnameRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_FAILURE) {
					// 取得失敗
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HOSTNAME.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HOSTNAME.getMessage()),
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HOSTNAME.getMessage()), 
								m_settingInfo));
				} else {
					// 構成情報設定フラグエラー
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CORRECT_VALUE.getMessage(
							MessageConstant.NODE_REGISTER_FLAG.getMessage(), MessageConstant.HOST_NAME.getMessage()));
					m_log.info("exec() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HOSTNAME.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HOSTNAME.getMessage()),
								e.getMessage(), 
								m_settingInfo));
				}
			}		

			/** 登録処理 (メモリ情報) */
			if (settingItemInfoList.contains(NodeConfigSettingItem.HW_MEMORY.name())) {
				if (m_nodeInfo.getNodeMemoryRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
					// 入力チェック
					List<String> msgList = RepositoryValidator.validateNodeMemoryConfigInfo(m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeMemoryInfo());
					for (String msg : msgList) {
						outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HW_MEMORY.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_MEMORY.getMessage()),
								msg, 
								m_settingInfo));
					}
					if (msgList.size() <= 0) {
						// 登録処理
						diffMap.put(NodeConfigSettingItem.HW_MEMORY,
								NodeConfigRegisterUtil.registerNodeMemoryInfo(m_registerDatetime, m_modifyUserId, m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeMemoryInfo(), true));
						// cc_node_historyテーブルへの登録 
						history.setMemoryFlag(true);
					}
				} else if (m_nodeInfo.getNodeMemoryRegisterFlag().intValue() == NodeRegisterFlagConstant.NOT_GET) {
					// 対象外
				} else if (m_nodeInfo.getNodeMemoryRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_FAILURE) {
					// 取得失敗
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HW_MEMORY.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_MEMORY.getMessage()),
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_MEMORY.getMessage()), 
								m_settingInfo));
				} else {
					// 構成情報設定フラグエラー
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CORRECT_VALUE.getMessage(
							MessageConstant.NODE_REGISTER_FLAG.getMessage(), MessageConstant.MEMORY_LIST.getMessage()));
					m_log.info("exec() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HW_MEMORY.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_MEMORY.getMessage()),
								e.getMessage(), 
								m_settingInfo));
				}
			}		

			/** 登録処理 (NIC情報) */
			if (settingItemInfoList.contains(NodeConfigSettingItem.HW_NIC.name())) {
				if (m_nodeInfo.getNodeNetworkInterfaceRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
					// 入力チェック
					List<String> msgList = RepositoryValidator.validateNodeNicConfigInfo(m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeNetworkInterfaceInfo());
					for (String msg : msgList) {
						outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HW_NIC.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_NIC.getMessage()),
								msg, 
								m_settingInfo));
					}
					if (msgList.size() <= 0) {
						// 登録処理
						diffMap.put(NodeConfigSettingItem.HW_NIC,
								NodeConfigRegisterUtil.registerNodeNetworkInterfaceInfo(m_registerDatetime, m_modifyUserId, m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeNetworkInterfaceInfo(), true));
						// cc_node_historyテーブルへの登録 
						history.setNetworkInterfaceFlag(true);
					}
				} else if (m_nodeInfo.getNodeNetworkInterfaceRegisterFlag().intValue() == NodeRegisterFlagConstant.NOT_GET) {
					// 対象外
				} else if (m_nodeInfo.getNodeNetworkInterfaceRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_FAILURE) {
					// 取得失敗
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HW_NIC.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_NIC.getMessage()),
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_NIC.getMessage()), 
								m_settingInfo));
				} else {
					// 構成情報設定フラグエラー
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CORRECT_VALUE.getMessage(
							MessageConstant.NODE_REGISTER_FLAG.getMessage(), MessageConstant.NETWORK_INTERFACE_LIST.getMessage()));
					m_log.info("exec() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.HW_NIC.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_HW_NIC.getMessage()),
								e.getMessage(), 
								m_settingInfo));
				}
			}
			
			/** ネットワーク接続情報 */
			if (settingItemInfoList.contains(NodeConfigSettingItem.NETSTAT.name())) {
				/** 登録処理 (ネットワーク接続情報) */
				if (m_nodeInfo.getNodeNetstatRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
					// 入力チェック
					List<String> msgList = RepositoryValidator.validateNodeNetstatConfigInfo(m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeNetstatInfo());
					for (String msg : msgList) {
						outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.NETSTAT.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_NETSTAT.getMessage()),
								msg, 
								m_settingInfo));
					}
					if (msgList.size() <= 0) {
						// 登録処理
						diffMap.put(NodeConfigSettingItem.NETSTAT,
								NodeConfigRegisterUtil.registerNodeNetstatInfo(m_registerDatetime, m_modifyUserId, m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeNetstatInfo(), true));
						// cc_node_historyテーブルへの登録 
						history.setNetstatFlag(true);
					}
	
				} else if (m_nodeInfo.getNodeNetstatRegisterFlag().intValue() == NodeRegisterFlagConstant.NOT_GET) {
					// 対象外
				} else if (m_nodeInfo.getNodeNetstatRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_FAILURE) {
					// 取得失敗
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.NETSTAT.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_NETSTAT.getMessage()),
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_NETSTAT.getMessage()), 
								m_settingInfo));
				} else {
					// 構成情報設定フラグエラー
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CORRECT_VALUE.getMessage(
							MessageConstant.NODE_REGISTER_FLAG.getMessage(), MessageConstant.NODE_NETSTAT.getMessage()));
					m_log.info("exec() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.NETSTAT.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_NETSTAT.getMessage()),
								e.getMessage(), 
								m_settingInfo));
				}
			}
	
			/** プロセス情報 */
			if (settingItemInfoList.contains(NodeConfigSettingItem.PROCESS.name())) {
				/** 登録処理 (プロセス情報) */
				if (m_nodeInfo.getNodeProcessRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
					// 入力チェック
					List<String> msgList = RepositoryValidator.validateNodeProcessConfigInfo(m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeProcessInfo());
					for (String msg : msgList) {
						outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.PROCESS.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_PROCESS.getMessage()),
								msg, 
								m_settingInfo));
					}
					if (msgList.size() <= 0) {
						// 登録処理
						boolean isUpdate = NodeConfigRegisterUtil.registerNodeProcessInfo(m_registerDatetime, m_modifyUserId, m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeProcessInfo());
						if (isUpdate) {
							diffMap.put(NodeConfigSettingItem.PROCESS, new NodeConfigRegisterDiffInfo());
						}
					}
	
				} else if (m_nodeInfo.getNodeProcessRegisterFlag().intValue() == NodeRegisterFlagConstant.NOT_GET) {
					// 対象外
				} else if (m_nodeInfo.getNodeProcessRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_FAILURE) {
					// 取得失敗
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.PROCESS.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_PROCESS.getMessage()),
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_PROCESS.getMessage()), 
								m_settingInfo));
				} else {
					// 構成情報設定フラグエラー
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CORRECT_VALUE.getMessage(
							MessageConstant.NODE_REGISTER_FLAG.getMessage(), MessageConstant.NODE_PROCESS.getMessage()));
					m_log.info("exec() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.PROCESS.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_PROCESS.getMessage()),
								e.getMessage(), 
								m_settingInfo));
				}
			}
	
			/** パッケージ情報 */
			if (settingItemInfoList.contains(NodeConfigSettingItem.PACKAGE.name())) {
				/** 登録処理 (パッケージ情報) */
				if (m_nodeInfo.getNodePackageRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
					// 入力チェック
					List<String> msgList = RepositoryValidator.validateNodePackageConfigInfo(m_nodeInfo.getFacilityId(), m_nodeInfo.getNodePackageInfo());
					for (String msg : msgList) {
						outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.PACKAGE.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_PACKAGE.getMessage()),
								msg, 
								m_settingInfo));
					}
					if (msgList.size() <= 0) {
						// 登録処理
						diffMap.put(NodeConfigSettingItem.PACKAGE,
								NodeConfigRegisterUtil.registerNodePackageInfo(m_registerDatetime, m_modifyUserId, m_nodeInfo.getFacilityId(), m_nodeInfo.getNodePackageInfo(), true));
						// cc_node_historyテーブルへの登録 
						history.setPackageFlag(true);
					}
	
				} else if (m_nodeInfo.getNodePackageRegisterFlag().intValue() == NodeRegisterFlagConstant.NOT_GET) {
					// 対象外
				} else if (m_nodeInfo.getNodePackageRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_FAILURE) {
					// 取得失敗
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.PACKAGE.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_PACKAGE.getMessage()),
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_PACKAGE.getMessage()), 
								m_settingInfo));
				} else {
					// 構成情報設定フラグエラー
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CORRECT_VALUE.getMessage(
							MessageConstant.NODE_REGISTER_FLAG.getMessage(), MessageConstant.NODE_PACKAGE.getMessage()));
					m_log.info("exec() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.PACKAGE.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_PACKAGE.getMessage()),
								e.getMessage(), 
								m_settingInfo));
				}
			}
			
			/** ユーザ任意情報 */
			/** 登録処理 (ユーザ任意情報) */
			
			//ユーザー任意情報の取得が有効だった場合にTrueになる
			boolean isCustomValid = false;
			
			List<NodeCustomInfo> customList = m_nodeInfo.getNodeCustomInfo();
			if(customList == null || customList.isEmpty()){
				m_log.debug("There is no custom list");
				//ユーザー任意情報を取得が有効になっていても、取得されなかった場合
				if (settingItemInfoList.contains(NodeConfigSettingItem.CUSTOM.name())) {
					outputBasicInfoList.add(
							createOutputBasicList(
								NodeConfigSettingItem.CUSTOM.name(),
								PriorityConstant.TYPE_WARNING, 
								MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
										MessageConstant.NODE_CONFIG_SETTING_CUSTOM.getMessage()),
								MessageConstant.MESSAGE_FAILED_TO_GET_NODE_CONFIG_CUSTOM_ALL.getMessage(m_nodeInfo.getFacilityId()), 
								m_settingInfo));	
				}
				// 取得対象外の場合は、特に何もしない
				
			} else{
				// コマンド実行に成功したデータを登録.
				List<NodeCustomInfo> registerList = new ArrayList<NodeCustomInfo>();
				//無効な設定のデータを登録
				List<NodeCustomInfo> notGetList = new ArrayList<NodeCustomInfo>();
				//通知用オリジナルメッセージを保存
				String notifyOrgMessage = null;
				for(NodeCustomInfo customResult : customList){
					m_log.debug("Flag num is "+customResult.getRegisterFlag().intValue());
					
					if (customResult.getRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_SUCCESS) {
						try {
							//取得設定が有効なため、フラグをtrue
							isCustomValid = true;
							RepositoryValidator.validateNodeCustomInfo(customResult);
							registerList.add(customResult);
						} catch(InvalidSetting e){
							String stackTrace = Arrays.toString( e.getStackTrace());
							String[] args = { customResult.getSettingCustomId(), e.getMessage() + "\n" + stackTrace};
							//取得したメッセージが1024文字を超えた場合、トランケート
							if(args[1].length() >= HinemosPropertyCommon.notify_event_messageorg_max_length.getIntegerValue()){
								m_log.debug("[InvalidSetting]Original message to be inserted into Event log exceeded notify.event.messageorg.max.length. Message truncated.");
								notifyOrgMessage = "Custom Info Setting ID: "+args[0]+"\n\nError Details:\n"+args[1];
								notifyOrgMessage = notifyOrgMessage.substring(0,HinemosPropertyCommon.notify_event_messageorg_max_length.getIntegerValue() -1);
							}else{
								notifyOrgMessage =MessageConstant.MESSAGE_FAILED_TO_GET_NODE_CONFIG_CUSTOM_BY_SETTING.getMessage(args);
							}
							outputBasicInfoList.add(
									createOutputBasicList(
										NodeConfigSettingItem.CUSTOM.name(),
										PriorityConstant.TYPE_WARNING, 
										MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
												MessageConstant.NODE_CONFIG_SETTING_CUSTOM.getMessage()),
										notifyOrgMessage, 
										m_settingInfo));
						}
					} else if(customResult.getRegisterFlag().intValue() == NodeRegisterFlagConstant.NOT_GET){
						// 対象外
						m_log.debug("Got custom info that is not supposed to be exec");
						//無効な設定の場合、notGetListに保存
						notGetList.add(customResult);
					} else if(customResult.getRegisterFlag().intValue() == NodeRegisterFlagConstant.GET_FAILURE){
						// 取得失敗
						String[] args = { customResult.getSettingCustomId(), customResult.getValue()};
						//取得したメッセージが1024文字を超えた場合、トランケート
						if(args[1].length() >= HinemosPropertyCommon.notify_event_messageorg_max_length.getIntegerValue()){
							m_log.debug("[Failed Command]Original message to be inserted into Event log exceeded notify.event.messageorg.max.length. Message truncated.");
							notifyOrgMessage = "Custom Info Setting ID: "+args[0]+"\n\nError Details:\n"+args[1];
							notifyOrgMessage = notifyOrgMessage.substring(0,HinemosPropertyCommon.notify_event_messageorg_max_length.getIntegerValue() -1);
						}else{
							notifyOrgMessage =MessageConstant.MESSAGE_FAILED_TO_GET_NODE_CONFIG_CUSTOM_BY_SETTING.getMessage(args);
						}
						outputBasicInfoList.add(
								createOutputBasicList(
									NodeConfigSettingItem.CUSTOM.name(),
									PriorityConstant.TYPE_WARNING, 
									MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
											MessageConstant.NODE_CONFIG_SETTING_CUSTOM.getMessage()),
									notifyOrgMessage, 
									m_settingInfo));
					} else{
						// 構成情報設定フラグエラー
						InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_CORRECT_VALUE.getMessage(
								MessageConstant.NODE_REGISTER_FLAG.getMessage(), MessageConstant.NODE_CONFIG_SETTING_CUSTOM.getMessage()));
						m_log.info("exec() : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage());
							outputBasicInfoList.add(
								createOutputBasicList(
									NodeConfigSettingItem.CUSTOM.name(),
									PriorityConstant.TYPE_WARNING, 
									MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
											MessageConstant.NODE_CONFIG_SETTING_CUSTOM.getMessage() + customResult.getSettingId()),
									e.getMessage(), 
									m_settingInfo));
					}
				}
				
				// 登録対象で主キー重複してないかチェック.
				if(!registerList.isEmpty()){
					Iterator<NodeCustomInfo> iter = registerList.iterator();
					List<NodeCustomInfoPK> pkList = new ArrayList<>();
					while(iter.hasNext()) {
						NodeCustomInfo info = iter.next();
						NodeCustomInfoPK entityPk = new NodeCustomInfoPK(m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeConfigSettingId(), info.getSettingCustomId());
						if (pkList.contains(entityPk)) {
							String[] args = { MessageConstant.NODE_CONFIG_SETTING_CUSTOM.getMessage(), info.getSettingCustomId()};
							String msg = MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(args);
							outputBasicInfoList.add(
									createOutputBasicList(
										NodeConfigSettingItem.CUSTOM.name(),
										PriorityConstant.TYPE_WARNING, 
										MessageConstant.MESSAGE_GET_NODE_CONFIG_SETTING_ITEM_FAILURE_MSG.getMessage(
												MessageConstant.NODE_CONFIG_SETTING_CUSTOM.getMessage()),
										msg, 
										m_settingInfo));
							iter.remove();
							continue;
						}
						pkList.add(entityPk);
					}
					
					// 登録処理.
					if(!registerList.isEmpty()){
						//有効な設定が存在していて、かつ無効な設定が存在する場合
						if(!notGetList.isEmpty()){
							//無効な設定を登録リストに追加する
							m_log.debug("There is NOT_GET setting to delete entity from DB");
							registerList.addAll(notGetList);
						}
						m_log.debug("Finally ready to add to DB");
						diffMap.put(NodeConfigSettingItem.CUSTOM,
								NodeConfigRegisterUtil.registerNodeCustomInfo(m_registerDatetime, m_modifyUserId, m_nodeInfo.getFacilityId(), m_nodeInfo.getNodeConfigSettingId(), registerList, true));
						// cc_node_historyテーブルへの登録 
						if(isCustomValid){
							//有効なユーザー任意情報設定があった場合のみ、flagをtrueにする
							history.setCustomFlag(true);
							m_log.debug("Valid custom info setting exist. Set Custom Flag to True");
						}else{
							//無効なユーザー任意情報設定の場合は、特に何もしない
							m_log.debug("No Valid custom info setting exist. Custom Flag is False");
						}
					}
				}
				
			}
				
				
			// cc_node_historyテーブルへの登録 
			history.setRegUser(m_modifyUserId);
			em.persist(history);

			if (diffMap.size() > 0) {
				// メッセージ作成
				String orgMessage = createDiffMessage(diffMap);
				//orgMessageが空文字の場合、更新なしとみなす
				if(orgMessage.equals("")){
					m_log.info("exec(): No NodeConfigInfo Updated");
				}else{
					// FacilityInfo更新
					FacilityInfo facilityInfo = QueryUtil.getFacilityPK(m_nodeInfo.getFacilityId(), ObjectPrivilegeMode.MODIFY);
					facilityInfo.setModifyDatetime(m_registerDatetime);
					facilityInfo.setModifyUserId(m_modifyUserId);
					// 終了メッセージ
					outputBasicInfoList.add(
							createOutputBasicList(
									null,
									PriorityConstant.TYPE_INFO, 
									MessageConstant.MESSAGE_NODE_CONFIG_SETTING_SUCCESS.getMessage(),
									orgMessage,
									this.m_settingInfo,
									this.m_registerDatetime));
				}
			}

			// デバイスサーチ有効時のINTERNALエラー対応
			if (((diffMap.containsKey(NodeConfigSettingItem.OS) && diffMap.get(NodeConfigSettingItem.OS) != null)
					|| (diffMap.containsKey(NodeConfigSettingItem.HW_CPU) && diffMap.get(NodeConfigSettingItem.HW_CPU) != null)
					|| (diffMap.containsKey(NodeConfigSettingItem.HW_MEMORY) && diffMap.get(NodeConfigSettingItem.HW_MEMORY) != null)
					|| (diffMap.containsKey(NodeConfigSettingItem.HW_NIC) && diffMap.get(NodeConfigSettingItem.HW_NIC) != null)
					|| (diffMap.containsKey(NodeConfigSettingItem.HW_DISK) && diffMap.get(NodeConfigSettingItem.HW_DISK) != null)
					|| (diffMap.containsKey(NodeConfigSettingItem.HW_FILESYSTEM) && diffMap.get(NodeConfigSettingItem.HW_FILESYSTEM) != null)
					|| (diffMap.containsKey(NodeConfigSettingItem.HOSTNAME) && diffMap.get(NodeConfigSettingItem.HOSTNAME) != null))
				&& HinemosPropertyCommon.repository_device_search_interval.getIntegerValue() > 0) {
				String facilityId = m_nodeInfo.getFacilityId();
				NodeInfo nodeInfo = NodeProperty.getProperty(facilityId);
				if (nodeInfo.getAutoDeviceSearch()) {
					// INTERNALイベント通知
					try {
						AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.NODE_CONFIG_SETTING,
								MessageConstant.MESSAGE_PLEASE_SET_NODE_CONFIG_AUTO_DEVICE_OFF, new String[] { facilityId });
					} catch (Exception e) {
						// 通知に失敗したとしても終了処理を中止しないように、例外はここで抑える。
						m_log.warn("exec(): Failed to notify InternalEvent.", e);
					}
				}
			}
			
		}

		return outputBasicInfoList;
	}

	/**
	 * 対象構成情報の収集項目IDを保持するリストを返す。
	 * 
	 * @param settingInfo 対象構成情報
	 * @return 対象構成情報の収集項目IDリスト
	 */
	private List<String> createSettingItemInfoList(NodeConfigSettingInfo settingInfo) {
		List<String> settingItemInfoList = new ArrayList<>();
		if (settingInfo == null || settingInfo.getNodeConfigSettingItemList() == null) {
			return settingItemInfoList;
		}
		for (NodeConfigSettingItemInfo itemInfo : settingInfo.getNodeConfigSettingItemList()) {
			settingItemInfoList.add(itemInfo.getSettingItemId());
		}
		return settingItemInfoList;
	}

	/**
	 * 
	 * 通知情報作成
	 * 
	 * @param settingItemId 収集項目に対応した対象構成情報
	 * @param priority 優先順位
	 * @param message メッセージ
	 * @param messageOrg オリジナルメッセージ
	 * @param settingInfo 設定情報
	 * @return 通知情報リスト
	 * @throws HinemosUnknown
	 */
	private OutputBasicInfo createOutputBasicList(
			String settingItemId, 
			Integer priority,
			String message,
			String messageOrg, 
			NodeConfigSettingInfo settingInfo
			) throws HinemosUnknown {
		
		return  createOutputBasicList(settingItemId, priority, message, messageOrg, settingInfo, HinemosTime.getDateInstance().getTime());
	}

	/**
	 * 
	 * 通知情報作成
	 * 
	 * @param settingItemId 収集項目に対応した対象構成情報
	 * @param priority 優先順位
	 * @param message メッセージ
	 * @param messageOrg オリジナルメッセージ
	 * @param settingInfo 設定情報
	 * @param outputDate 出力日時
	 * @return 通知情報リスト
	 * @throws HinemosUnknown
	 */
	private OutputBasicInfo createOutputBasicList(
			String settingItemId, 
			Integer priority,
			String message,
			String messageOrg, 
			NodeConfigSettingInfo settingInfo,
			Long outputDate
			) throws HinemosUnknown {

		// 通知情報作成
		OutputBasicInfo outputBasicInfo = new OutputBasicInfo();
		// 通知グループID
		outputBasicInfo.setNotifyGroupId(settingInfo.getNotifyGroupId());
		// プラグインID
		outputBasicInfo.setPluginId(HinemosModuleConstant.NODE_CONFIG_SETTING);
		// アプリケーション
		outputBasicInfo.setApplication("");
		// 監視項目ID
		outputBasicInfo.setMonitorId(settingInfo.getSettingId());
		// ファシリティID
		outputBasicInfo.setFacilityId(m_nodeInfo.getFacilityId());
		// スコープ
		String facilityPath = new RepositoryControllerBean().getFacilityPath(m_nodeInfo.getFacilityId(), null);
		outputBasicInfo.setScopeText(facilityPath);
		// メッセージ
		outputBasicInfo.setMessage(message);
		// オリジナルメッセージ
		outputBasicInfo.setMessageOrg(messageOrg);
		//重要度
		outputBasicInfo.setPriority(priority);
		//発生日時
		outputBasicInfo.setGenerationDate(outputDate);

		return outputBasicInfo;
	}

	/**
	 * 変更情報のメッセージを返す
	 * 
	 * @param diffMap 差分情報(収集項目、差分情報)
	 * @return 差分情報のメッセージ
	 */
	public String createDiffMessage(Map<NodeConfigSettingItem, NodeConfigRegisterDiffInfo> diffMap) {

		if (diffMap.keySet().size() == 0) {
			// 変更項目なし
			return "";
		}

		StringBuilder sbMessage = new StringBuilder();
		StringBuilder sbDetail = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");

		// 件数表示
		// OS情報
		NodeConfigRegisterDiffInfo diffInfo = diffMap.get(NodeConfigSettingItem.OS);
		if (diffInfo != null) {
			String title = MessageConstant.OS.getMessage();
			// 概要
			sbMessage.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_COUNT.getMessage(
					title, Integer.toString(diffInfo.getAddObj().size()), Integer.toString(diffInfo.getModObj().size()), Integer.toString(diffInfo.getDelObj().size())) + "\n");
			// 詳細
			if (diffInfo.getAddObj().size() > 0) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_ADD.getMessage(
						title,
						MessageConstant.FACILITY_ID.getMessage() + "=" + m_nodeInfo.getFacilityId())
						+ "\n");
			}
			for (Object[] objs : diffInfo.getModObj()) {
				NodeOsInfo beforeObj = (NodeOsInfo)objs[0];
				NodeOsInfo afterObj = (NodeOsInfo)objs[1];
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY.getMessage(
						title,
						MessageConstant.FACILITY_ID.getMessage() + "=" + m_nodeInfo.getFacilityId())
						+ "\n");
				if (!beforeObj.getOsName().equals(afterObj.getOsName())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.OS_NAME.getMessage(), beforeObj.getOsName(), afterObj.getOsName()) + "\n");
				}
				if (beforeObj.getOsRelease() != afterObj.getOsRelease()) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.OS_RELEASE.getMessage(), beforeObj.getOsRelease(), afterObj.getOsRelease()) + "\n");
				}
				if (!beforeObj.getOsVersion().equals(afterObj.getOsVersion())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.OS_VERSION.getMessage(), beforeObj.getOsVersion(), afterObj.getOsVersion()) + "\n");
				}
				if (!beforeObj.getCharacterSet().equals(afterObj.getCharacterSet())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.CHARACTER_SET.getMessage(), beforeObj.getCharacterSet(), afterObj.getCharacterSet()) + "\n");
				}
				if (beforeObj.getStartupDateTime() != afterObj.getStartupDateTime()) {
					String beforeDate = "";
					String afterDate = "";
					if (beforeDate != null) {
						beforeDate = sdf.format(new Date(beforeObj.getStartupDateTime()));
					}
					if (afterDate != null) {
						afterDate = sdf.format(new Date(afterObj.getStartupDateTime()));
					}
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.NODE_OS_STARTUP_DATE_TIME.getMessage(), beforeDate, afterDate) + "\n");
				}
			}
			if (diffInfo.getDelObj().size() > 0) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_DELETE.getMessage(
						title,
						MessageConstant.FACILITY_ID.getMessage() + "=" + m_nodeInfo.getFacilityId())
						+ "\n");
			}
		}
		// HW情報 - CPU情報
		diffInfo = diffMap.get(NodeConfigSettingItem.HW_CPU);
		if (diffInfo != null) {
			String title = MessageConstant.CPU.getMessage();
			// 概要
			sbMessage.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_COUNT.getMessage(
					title, Integer.toString(diffInfo.getAddObj().size()), Integer.toString(diffInfo.getModObj().size()), Integer.toString(diffInfo.getDelObj().size())) + "\n");
			// 詳細
			for (Object obj : diffInfo.getAddObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_ADD.getMessage(
						title,
						MessageConstant.DEVICE_NAME.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceName()
						+ ", " + MessageConstant.DEVICE_INDEX.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceIndex())
						+ "\n");
			}
			for (Object[] objs : diffInfo.getModObj()) {
				NodeCpuInfo beforeObj = (NodeCpuInfo)objs[0];
				NodeCpuInfo afterObj = (NodeCpuInfo)objs[1];
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY.getMessage(
						title,
						MessageConstant.DEVICE_NAME.getMessage() + "=" + beforeObj.getDeviceName()
						+ ", " + MessageConstant.DEVICE_INDEX.getMessage() + "=" + afterObj.getDeviceIndex())
						+ "\n");
				if (!beforeObj.getDeviceName().equals(afterObj.getDeviceDisplayName())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DEVICE_DISPLAY_NAME.getMessage(), beforeObj.getDeviceName(), afterObj.getDeviceName()) + "\n");
				}
				if (beforeObj.getDeviceSize() != afterObj.getDeviceSize()) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DEVICE_SIZE.getMessage(), beforeObj.getDeviceSize().toString(), afterObj.getDeviceSize().toString()) + "\n");
				}
				if (!beforeObj.getDeviceSizeUnit().equals(afterObj.getDeviceSizeUnit())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DEVICE_SIZE_UNIT.getMessage(), beforeObj.getDeviceSizeUnit(), afterObj.getDeviceSizeUnit()) + "\n");
				}
				if (!beforeObj.getDeviceDescription().equals(afterObj.getDeviceDescription())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DESCRIPTION.getMessage(), beforeObj.getDeviceDescription(), afterObj.getDeviceDescription()) + "\n");
				}
				if (!beforeObj.getCoreCount().equals(afterObj.getCoreCount())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.CPU_CORE_COUNT.getMessage(), beforeObj.getCoreCount().toString(), afterObj.getCoreCount().toString()) + "\n");
				}
				if (!beforeObj.getThreadCount().equals(afterObj.getThreadCount())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.CPU_THREAD_COUNT.getMessage(), beforeObj.getThreadCount().toString(), afterObj.getThreadCount().toString()) + "\n");
				}
				if (!beforeObj.getClockCount().equals(afterObj.getClockCount())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.CPU_CLOCK_COUNT.getMessage(), beforeObj.getClockCount().toString(), afterObj.getClockCount().toString()) + "\n");
				}
			}
			for (Object obj : diffInfo.getDelObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_DELETE.getMessage(
						title,
						MessageConstant.DEVICE_NAME.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceName()
						+ ", " + MessageConstant.DEVICE_INDEX.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceIndex())
						+ "\n");
			}
		}
		// HW情報 - メモリ情報
		diffInfo = diffMap.get(NodeConfigSettingItem.HW_MEMORY);
		if (diffInfo != null) {
			String title = MessageConstant.MEMORY.getMessage();
			// 概要
			sbMessage.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_COUNT.getMessage(
					title, Integer.toString(diffInfo.getAddObj().size()), Integer.toString(diffInfo.getModObj().size()), Integer.toString(diffInfo.getDelObj().size())) + "\n");
			// 詳細
			for (Object obj : diffInfo.getAddObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_ADD.getMessage(
						title,
						MessageConstant.DEVICE_NAME.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceName()
						+ ", " + MessageConstant.DEVICE_INDEX.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceIndex())
						+ "\n");
			}
			for (Object[] objs : diffInfo.getModObj()) {
				NodeMemoryInfo beforeObj = (NodeMemoryInfo)objs[0];
				NodeMemoryInfo afterObj = (NodeMemoryInfo)objs[1];
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY.getMessage(
						title,
						MessageConstant.DEVICE_NAME.getMessage() + "=" + beforeObj.getDeviceName()
						+ ", " + MessageConstant.DEVICE_INDEX.getMessage() + "=" + afterObj.getDeviceIndex())
						+ "\n");
				if (!beforeObj.getDeviceName().equals(afterObj.getDeviceDisplayName())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DEVICE_DISPLAY_NAME.getMessage(), beforeObj.getDeviceName(), afterObj.getDeviceName()) + "\n");
				}
				if (beforeObj.getDeviceSize() != afterObj.getDeviceSize()) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DEVICE_SIZE.getMessage(), beforeObj.getDeviceSize().toString(), afterObj.getDeviceSize().toString()) + "\n");
				}
				if (!beforeObj.getDeviceSizeUnit().equals(afterObj.getDeviceSizeUnit())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DEVICE_SIZE_UNIT.getMessage(), beforeObj.getDeviceSizeUnit(), afterObj.getDeviceSizeUnit()) + "\n");
				}
				if (!beforeObj.getDeviceDescription().equals(afterObj.getDeviceDescription())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DESCRIPTION.getMessage(), beforeObj.getDeviceDescription(), afterObj.getDeviceDescription()) + "\n");
				}
			}
			for (Object obj : diffInfo.getDelObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_DELETE.getMessage(
						title,
						MessageConstant.DEVICE_NAME.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceName()
						+ ", " + MessageConstant.DEVICE_INDEX.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceIndex())
						+ "\n");
			}
		}
		// HW情報 - NIC情報
		diffInfo = diffMap.get(NodeConfigSettingItem.HW_NIC);
		if (diffInfo != null) {
			String title = MessageConstant.NETWORK_INTERFACE.getMessage();
			// 概要
			sbMessage.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_COUNT.getMessage(
					title, Integer.toString(diffInfo.getAddObj().size()), Integer.toString(diffInfo.getModObj().size()), Integer.toString(diffInfo.getDelObj().size())) + "\n");
			// 詳細
			for (Object obj : diffInfo.getAddObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_ADD.getMessage(
						title,
						MessageConstant.DEVICE_NAME.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceName()
						+ ", " + MessageConstant.DEVICE_INDEX.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceIndex())
						+ "\n");
			}
			for (Object[] objs : diffInfo.getModObj()) {
				NodeNetworkInterfaceInfo beforeObj = (NodeNetworkInterfaceInfo)objs[0];
				NodeNetworkInterfaceInfo afterObj = (NodeNetworkInterfaceInfo)objs[1];
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY.getMessage(
						title,
						MessageConstant.DEVICE_NAME.getMessage() + "=" + beforeObj.getDeviceName()
						+ ", " + MessageConstant.DEVICE_INDEX.getMessage() + "=" + afterObj.getDeviceIndex())
						+ "\n");
				if (!beforeObj.getDeviceName().equals(afterObj.getDeviceDisplayName())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DEVICE_DISPLAY_NAME.getMessage(), beforeObj.getDeviceName(), afterObj.getDeviceName()) + "\n");
				}
				if (beforeObj.getDeviceSize() != afterObj.getDeviceSize()) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DEVICE_SIZE.getMessage(), beforeObj.getDeviceSize().toString(), afterObj.getDeviceSize().toString()) + "\n");
				}
				if (!beforeObj.getDeviceSizeUnit().equals(afterObj.getDeviceSizeUnit())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DEVICE_SIZE_UNIT.getMessage(), beforeObj.getDeviceSizeUnit(), afterObj.getDeviceSizeUnit()) + "\n");
				}
				if (!beforeObj.getDeviceDescription().equals(afterObj.getDeviceDescription())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DESCRIPTION.getMessage(), beforeObj.getDeviceDescription(), afterObj.getDeviceDescription()) + "\n");
				}
				if (!beforeObj.getNicIpAddress().equals(afterObj.getNicIpAddress())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.NIC_IP_ADDRESS.getMessage(), beforeObj.getNicIpAddress(), afterObj.getNicIpAddress()) + "\n");
				}
				if (!beforeObj.getNicMacAddress().equals(afterObj.getNicMacAddress())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.NIC_MAC_ADDRESS.getMessage(), beforeObj.getNicMacAddress(), afterObj.getNicMacAddress()) + "\n");
				}
			}
			for (Object obj : diffInfo.getDelObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_DELETE.getMessage(
						title,
						MessageConstant.DEVICE_NAME.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceName()
						+ ", " + MessageConstant.DEVICE_INDEX.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceIndex())
						+ "\n");
			}
		}
		// HW情報 - ディスク情報
		diffInfo = diffMap.get(NodeConfigSettingItem.HW_DISK);
		if (diffInfo != null) {
			String title = MessageConstant.DISK.getMessage();
			// 概要
			sbMessage.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_COUNT.getMessage(
					title, Integer.toString(diffInfo.getAddObj().size()), Integer.toString(diffInfo.getModObj().size()), Integer.toString(diffInfo.getDelObj().size())) + "\n");
			// 詳細
			for (Object obj : diffInfo.getAddObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_ADD.getMessage(
						title,
						MessageConstant.DEVICE_NAME.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceName()
						+ ", " + MessageConstant.DEVICE_INDEX.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceIndex())
						+ "\n");
			}
			for (Object[] objs : diffInfo.getModObj()) {
				NodeDiskInfo beforeObj = (NodeDiskInfo)objs[0];
				NodeDiskInfo afterObj = (NodeDiskInfo)objs[1];
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY.getMessage(
						title,
						MessageConstant.DEVICE_NAME.getMessage() + "=" + beforeObj.getDeviceName()
						+ ", " + MessageConstant.DEVICE_INDEX.getMessage() + "=" + afterObj.getDeviceIndex())
						+ "\n");
				if (!beforeObj.getDeviceName().equals(afterObj.getDeviceDisplayName())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DEVICE_DISPLAY_NAME.getMessage(), beforeObj.getDeviceName(), afterObj.getDeviceName()) + "\n");
				}
				if (beforeObj.getDeviceSize() != afterObj.getDeviceSize()) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DEVICE_SIZE.getMessage(), beforeObj.getDeviceSize().toString(), afterObj.getDeviceSize().toString()) + "\n");
				}
				if (!beforeObj.getDeviceSizeUnit().equals(afterObj.getDeviceSizeUnit())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DEVICE_SIZE_UNIT.getMessage(), beforeObj.getDeviceSizeUnit(), afterObj.getDeviceSizeUnit()) + "\n");
				}
				if (!beforeObj.getDeviceDescription().equals(afterObj.getDeviceDescription())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DESCRIPTION.getMessage(), beforeObj.getDeviceDescription(), afterObj.getDeviceDescription()) + "\n");
				}
				if (!beforeObj.getDiskRpm().equals(afterObj.getDiskRpm())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DISK_RPM.getMessage(), beforeObj.getDiskRpm().toString(), afterObj.getDiskRpm().toString()) + "\n");
				}
			}
			for (Object obj : diffInfo.getDelObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_DELETE.getMessage(
						title,
						MessageConstant.DEVICE_NAME.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceName()
						+ ", " + MessageConstant.DEVICE_INDEX.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceIndex())
						+ "\n");
			}
		}
		// HW情報 - ファイルシステム情報
		diffInfo = diffMap.get(NodeConfigSettingItem.HW_FILESYSTEM);
		if (diffInfo != null) {
			String title = MessageConstant.FILE_SYSTEM.getMessage();
			// 概要
			sbMessage.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_COUNT.getMessage(
					title, Integer.toString(diffInfo.getAddObj().size()), Integer.toString(diffInfo.getModObj().size()), Integer.toString(diffInfo.getDelObj().size())) + "\n");
			// 詳細
			for (Object obj : diffInfo.getAddObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_ADD.getMessage(
						title,
						MessageConstant.DEVICE_NAME.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceName()
						+ ", " + MessageConstant.DEVICE_INDEX.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceIndex())
						+ "\n");
			}
			for (Object[] objs : diffInfo.getModObj()) {
				NodeFilesystemInfo beforeObj = (NodeFilesystemInfo)objs[0];
				NodeFilesystemInfo afterObj = (NodeFilesystemInfo)objs[1];
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY.getMessage(
						title,
						MessageConstant.DEVICE_NAME.getMessage() + "=" + beforeObj.getDeviceName()
						+ ", " + MessageConstant.DEVICE_INDEX.getMessage() + "=" + afterObj.getDeviceIndex())
						+ "\n");
				if (!beforeObj.getDeviceName().equals(afterObj.getDeviceDisplayName())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DEVICE_DISPLAY_NAME.getMessage(), beforeObj.getDeviceName(), afterObj.getDeviceName()) + "\n");
				}
				if (beforeObj.getDeviceSize() != afterObj.getDeviceSize()) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DEVICE_SIZE.getMessage(), beforeObj.getDeviceSize().toString(), afterObj.getDeviceSize().toString()) + "\n");
				}
				if (!beforeObj.getDeviceSizeUnit().equals(afterObj.getDeviceSizeUnit())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DEVICE_SIZE_UNIT.getMessage(), beforeObj.getDeviceSizeUnit(), afterObj.getDeviceSizeUnit()) + "\n");
				}
				if (!beforeObj.getDeviceDescription().equals(afterObj.getDeviceDescription())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.DESCRIPTION.getMessage(), beforeObj.getDeviceDescription(), afterObj.getDeviceDescription()) + "\n");
				}
				if (!beforeObj.getFilesystemType().equals(afterObj.getFilesystemType())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.FILE_SYSTEM_TYPE.getMessage(), beforeObj.getFilesystemType().toString(), afterObj.getFilesystemType().toString()) + "\n");
				}
			}
			for (Object obj : diffInfo.getDelObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_DELETE.getMessage(
						title,
						MessageConstant.DEVICE_NAME.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceName()
						+ ", " + MessageConstant.DEVICE_INDEX.getMessage() + "=" + ((NodeDeviceInfo)obj).getDeviceIndex())
						+ "\n");
			}
		}
		// HW情報 - ホスト名情報
		diffInfo = diffMap.get(NodeConfigSettingItem.HOSTNAME);
		if (diffInfo != null) {
			String title = MessageConstant.HOST_NAME.getMessage();
			// 概要
			sbMessage.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_COUNT.getMessage(
					title, Integer.toString(diffInfo.getAddObj().size()), Integer.toString(diffInfo.getModObj().size()), Integer.toString(diffInfo.getDelObj().size())) + "\n");
			// 詳細
			for (Object obj : diffInfo.getAddObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_ADD.getMessage(
						title,
						MessageConstant.HOST_NAME.getMessage() + "=" + ((NodeHostnameInfo)obj).getHostname())
						+ "\n");
			}
			for (Object obj : diffInfo.getDelObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_DELETE.getMessage(
						title,
						MessageConstant.HOST_NAME.getMessage() + "=" + ((NodeHostnameInfo)obj).getHostname())
						+ "\n");
			}
		}
		// ノード変数情報
		diffInfo = diffMap.get(NodeConfigSettingItem.NODE_VARIABLE);
		if (diffInfo != null) {
			String title = MessageConstant.NODE_VARIABLE.getMessage();
			// 概要
			sbMessage.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_COUNT.getMessage(
					title, Integer.toString(diffInfo.getAddObj().size()), Integer.toString(diffInfo.getModObj().size()), Integer.toString(diffInfo.getDelObj().size())) + "\n");
			// 詳細
			for (Object obj : diffInfo.getAddObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_ADD.getMessage(
						title,
						MessageConstant.NODE_VARIABLE_NAME + "=" + ((NodeVariableInfo)obj).getNodeVariableName())
						+ "\n");
			}
			for (Object[] objs : diffInfo.getModObj()) {
				NodeVariableInfo beforeObj = (NodeVariableInfo)objs[0];
				NodeVariableInfo afterObj = (NodeVariableInfo)objs[1];
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY.getMessage(
						title,
						MessageConstant.NODE_VARIABLE_NAME.getMessage() + "=" + beforeObj.getNodeVariableName())
						+ "\n");
				sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.NODE_VARIABLE_VALUE.getMessage(), beforeObj.getNodeVariableValue(), afterObj.getNodeVariableValue()) + "\n");
			}
			for (Object obj : diffInfo.getDelObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_DELETE.getMessage(
						title,
						MessageConstant.NODE_VARIABLE_NAME.getMessage() + "=" + ((NodeVariableInfo)obj).getNodeVariableName())
						+ "\n");
			}
		}
		// ネットワーク接続
		diffInfo = diffMap.get(NodeConfigSettingItem.NETSTAT);
		if (diffInfo != null) {
			String title = MessageConstant.NODE_NETSTAT.getMessage();
			// 概要
			sbMessage.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_COUNT.getMessage(
					title, Integer.toString(diffInfo.getAddObj().size()), Integer.toString(diffInfo.getModObj().size()), Integer.toString(diffInfo.getDelObj().size())) + "\n");
			// 詳細
			for (Object obj : diffInfo.getAddObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_ADD.getMessage(
						title,
						MessageConstant.NODE_NETSTAT_PROTOCOL.getMessage() + "=" + ((NodeNetstatInfo)obj).getProtocol()
						+ ", " + MessageConstant.NODE_NETSTAT_LOCAL_IP_ADDRESS.getMessage() + "=" + ((NodeNetstatInfo)obj).getLocalIpAddress()
						+ ", " + MessageConstant.NODE_NETSTAT_LOCAL_PORT.getMessage() + "=" + ((NodeNetstatInfo)obj).getLocalPort()
						+ ", " + MessageConstant.NODE_NETSTAT_FOREIGN_IP_ADDRESS.getMessage() + "=" + ((NodeNetstatInfo)obj).getForeignIpAddress()
						+ ", " + MessageConstant.NODE_NETSTAT_FOREIGN_PORT.getMessage() + "=" + ((NodeNetstatInfo)obj).getForeignPort()
						+ ", " + MessageConstant.NODE_NETSTAT_PROCESS_NAME.getMessage() + "=" + ((NodeNetstatInfo)obj).getProcessName()
						+ ", " + MessageConstant.NODE_NETSTAT_PID.getMessage() + "=" + ((NodeNetstatInfo)obj).getPid().toString()
						)
						+ "\n");
			}
			for (Object[] objs : diffInfo.getModObj()) {
				NodeNetstatInfo beforeObj = (NodeNetstatInfo)objs[0];
				NodeNetstatInfo afterObj = (NodeNetstatInfo)objs[1];
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY.getMessage(
						title,
						MessageConstant.NODE_NETSTAT_PROTOCOL.getMessage() + "=" +  beforeObj.getProtocol()
						+ ", " + MessageConstant.NODE_NETSTAT_LOCAL_IP_ADDRESS.getMessage() + "=" + beforeObj.getLocalIpAddress()
						+ ", " + MessageConstant.NODE_NETSTAT_LOCAL_PORT.getMessage() + "=" +  beforeObj.getLocalPort()
						+ ", " + MessageConstant.NODE_NETSTAT_FOREIGN_IP_ADDRESS.getMessage() + "=" + beforeObj.getForeignIpAddress()
						+ ", " + MessageConstant.NODE_NETSTAT_FOREIGN_PORT.getMessage() + "=" +  beforeObj.getForeignPort()
						+ ", " + MessageConstant.NODE_NETSTAT_PROCESS_NAME.getMessage() + "=" + beforeObj.getProcessName()
						+ ", " + MessageConstant.NODE_NETSTAT_PID.getMessage() + "=" +  beforeObj.getPid().toString()
						)
						+ "\n");
				if (!beforeObj.getStatus().equals(afterObj.getStatus())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.NODE_NETSTAT_STATUS.getMessage(), beforeObj.getStatus(), afterObj.getStatus()) + "\n");
				}
			}
			for (Object obj : diffInfo.getDelObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_DELETE.getMessage(
						title,
						MessageConstant.NODE_NETSTAT_PROTOCOL.getMessage() + "=" + ((NodeNetstatInfo)obj).getProtocol()
						+ ", " + MessageConstant.NODE_NETSTAT_LOCAL_IP_ADDRESS.getMessage() + "=" + ((NodeNetstatInfo)obj).getLocalIpAddress()
						+ ", " + MessageConstant.NODE_NETSTAT_LOCAL_PORT.getMessage() + "=" + ((NodeNetstatInfo)obj).getLocalPort()
						+ ", " + MessageConstant.NODE_NETSTAT_FOREIGN_IP_ADDRESS.getMessage() + "=" + ((NodeNetstatInfo)obj).getForeignIpAddress()
						+ ", " + MessageConstant.NODE_NETSTAT_FOREIGN_PORT.getMessage() + "=" + ((NodeNetstatInfo)obj).getForeignPort()
						+ ", " + MessageConstant.NODE_NETSTAT_PROCESS_NAME.getMessage() + "=" + ((NodeNetstatInfo)obj).getProcessName()
						+ ", " + MessageConstant.NODE_NETSTAT_PID.getMessage() + "=" + ((NodeNetstatInfo)obj).getPid().toString()
						)
						+ "\n");
			}
		}
		// プロセス情報
		diffInfo = diffMap.get(NodeConfigSettingItem.PROCESS);
		if (diffInfo != null) {
			// 概要
			sbMessage.append(MessageConstant.NODE_PROCESS.getMessage() + " : " + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_IS_MODIFY.getMessage() + "\n");
		}
		// パッケージ情報
		diffInfo = diffMap.get(NodeConfigSettingItem.PACKAGE);
		if (diffInfo != null) {
			String title = MessageConstant.NODE_PACKAGE.getMessage();
			// 概要
			sbMessage.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_COUNT.getMessage(
					title, Integer.toString(diffInfo.getAddObj().size()), Integer.toString(diffInfo.getModObj().size()), Integer.toString(diffInfo.getDelObj().size())) + "\n");
			// 詳細
			for (Object obj : diffInfo.getAddObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_ADD.getMessage(
						title,
						MessageConstant.NODE_PACKAGE_ID.getMessage() + "=" + ((NodePackageInfo)obj).getPackageId())
						+ "\n");
			}
			for (Object[] objs : diffInfo.getModObj()) {
				NodePackageInfo beforeObj = (NodePackageInfo)objs[0];
				NodePackageInfo afterObj = (NodePackageInfo)objs[1];
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY.getMessage(
						title,
						MessageConstant.NODE_PACKAGE_ID.getMessage() + "=" + beforeObj.getPackageId())
						+ "\n");
				if (!beforeObj.getPackageName().equals(afterObj.getPackageName())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
									MessageConstant.NODE_PACKAGE_NAME.getMessage(), beforeObj.getPackageName(), afterObj.getPackageName()) + "\n");
				}
				if (!beforeObj.getVersion().equals(afterObj.getVersion())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
									MessageConstant.NODE_PACKAGE_VERSION.getMessage(), beforeObj.getVersion(), afterObj.getVersion()) + "\n");
				}
				if (!beforeObj.getRelease().equals(afterObj.getRelease())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.NODE_PACKAGE_RELEASE.getMessage(), beforeObj.getRelease(), afterObj.getRelease()) + "\n");
				}
				if (beforeObj.getInstallDate() != afterObj.getInstallDate()) {
					String beforeDate = "";
					String afterDate = "";
					if (beforeDate != null) {
						beforeDate = sdf.format(new Date(beforeObj.getInstallDate()));
					}
					if (afterDate != null) {
						afterDate = sdf.format(new Date(afterObj.getInstallDate()));
					}
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.NODE_PACKAGE_INSTALL_DATE.getMessage(), beforeDate, afterDate) + "\n");
				}
				if (!beforeObj.getVendor().equals(afterObj.getVendor())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.NODE_PACKAGE_VENDOR.getMessage(), beforeObj.getVendor(), afterObj.getVendor()) + "\n");
				}
				if (!beforeObj.getArchitecture().equals(afterObj.getArchitecture())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.NODE_PACKAGE_ARCHITECTURE.getMessage(), beforeObj.getArchitecture(), afterObj.getArchitecture()) + "\n");
				}
			}
			for (Object obj : diffInfo.getDelObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_DELETE.getMessage(
						title,
						MessageConstant.NODE_PACKAGE_ID.getMessage() + "=" + ((NodePackageInfo)obj).getPackageId())
						+ "\n");
			}
		}

		// ユーザ任意情報
		diffInfo = diffMap.get(NodeConfigSettingItem.CUSTOM);
		if (diffInfo != null) {
			String title = MessageConstant.NODE_CONFIG_SETTING_CUSTOM.getMessage();
			// 概要
			sbMessage.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_COUNT.getMessage(
					title, Integer.toString(diffInfo.getAddObj().size()), Integer.toString(diffInfo.getModObj().size()), Integer.toString(diffInfo.getDelObj().size())) + "\n");
			// 詳細
			for (Object obj : diffInfo.getAddObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_ADD.getMessage(
						title,
						MessageConstant.SETTING_CUSTOM_ID.getMessage() + "=" + ((NodeCustomInfo)obj).getSettingCustomId())
						+ "\n");
			}
			for (Object[] objs : diffInfo.getModObj()) {
				NodeCustomInfo beforeObj = (NodeCustomInfo)objs[0];
				NodeCustomInfo afterObj = (NodeCustomInfo)objs[1];
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY.getMessage(
						title,
						MessageConstant.SETTING_CUSTOM_ID.getMessage() + "=" + beforeObj.getSettingCustomId())
						+ "\n");
				if (!beforeObj.getDisplayName().equals(afterObj.getDisplayName())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
									MessageConstant.NODE_CUSTOM_DISPLAY_NAME.getMessage(), beforeObj.getDisplayName(), afterObj.getDisplayName()) + "\n");
				}
				if (!beforeObj.getCommand().equals(afterObj.getCommand())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
									MessageConstant.COMMAND.getMessage(), beforeObj.getCommand(), afterObj.getCommand()) + "\n");
				}
				if (!beforeObj.getValue().equals(afterObj.getValue())) {
					sbDetail.append("\t" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_MODIFY_DETAIL.getMessage(
							MessageConstant.NODE_CUSTOM_RESULT.getMessage(), beforeObj.getValue(), afterObj.getValue()) + "\n");
				}
			}
			for (Object obj : diffInfo.getDelObj()) {
				sbDetail.append(MessageConstant.MESSAGE_NODE_CONFIG_SETTING_DELETE.getMessage(
						title,
						MessageConstant.SETTING_CUSTOM_ID.getMessage() + "=" + ((NodeCustomInfo)obj).getSettingCustomId())
						+ "\n");
			}
		}

		// 詳細表示
		if (sbDetail.length() > 0) {
			sbMessage.append("\n");
			sbMessage.append("<" + MessageConstant.MESSAGE_NODE_CONFIG_SETTING_DETAIL.getMessage() + ">\n");
			sbMessage.append(sbDetail);
		}
		return sbMessage.toString();
	}
}
