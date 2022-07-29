/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.repository.model.NodeCpuHistoryDetail;
import com.clustercontrol.repository.bean.NodeConfigSettingConstant;
import com.clustercontrol.repository.bean.NodeConfigSettingItem;
import com.clustercontrol.repository.bean.NodeRegisterFlagConstant;
import com.clustercontrol.repository.model.NodeCpuInfo;
import com.clustercontrol.repository.model.NodeCustomHistoryDetail;
import com.clustercontrol.repository.model.NodeCustomInfo;
import com.clustercontrol.repository.model.NodeCustomInfoPK;
import com.clustercontrol.repository.model.NodeDeviceInfoPK;
import com.clustercontrol.repository.model.NodeDiskHistoryDetail;
import com.clustercontrol.repository.model.NodeDiskInfo;
import com.clustercontrol.repository.model.NodeFilesystemHistoryDetail;
import com.clustercontrol.repository.model.NodeFilesystemInfo;
import com.clustercontrol.repository.model.NodeHostnameHistoryDetail;
import com.clustercontrol.repository.model.NodeHostnameInfo;
import com.clustercontrol.repository.model.NodeHostnameInfoPK;
import com.clustercontrol.repository.model.NodeLicenseHistoryDetail;
import com.clustercontrol.repository.model.NodeLicenseInfo;
import com.clustercontrol.repository.model.NodeLicenseInfoPK;
import com.clustercontrol.repository.model.NodeMemoryHistoryDetail;
import com.clustercontrol.repository.model.NodeMemoryInfo;
import com.clustercontrol.repository.model.NodeProductHistoryDetail;
import com.clustercontrol.repository.model.NodeProductInfo;
import com.clustercontrol.repository.model.NodeProductInfoPK;
import com.clustercontrol.repository.model.NodeNetstatHistoryDetail;
import com.clustercontrol.repository.model.NodeNetstatInfo;
import com.clustercontrol.repository.model.NodeNetstatInfoPK;
import com.clustercontrol.repository.model.NodeNetworkInterfaceHistoryDetail;
import com.clustercontrol.repository.model.NodeNetworkInterfaceInfo;
import com.clustercontrol.repository.model.NodeOsHistoryDetail;
import com.clustercontrol.repository.model.NodeOsInfo;
import com.clustercontrol.repository.model.NodePackageHistoryDetail;
import com.clustercontrol.repository.model.NodePackageInfo;
import com.clustercontrol.repository.model.NodePackageInfoPK;
import com.clustercontrol.repository.model.NodeProcessInfo;
import com.clustercontrol.repository.model.NodeProcessInfoPK;
import com.clustercontrol.repository.model.NodeVariableHistoryDetail;
import com.clustercontrol.repository.model.NodeVariableInfo;
import com.clustercontrol.repository.model.NodeVariableInfoPK;
import com.clustercontrol.util.HinemosTime;

/**
 * 構成情報収集処理
 *
 * @version 6.2.0
 */
public class NodeConfigRegisterUtil {

	/** 監視対象外設定向けのコード */
	public static final String IGNORE_MONITOR = "@IGNORE_MONITOR";

	private static Log m_log = LogFactory.getLog( NodeConfigRegisterUtil.class );
	/**
	 * OS情報の登録処理を行う。
	 * 
	 * @param registerDatetime 収集日時
	 * @param modifyUserId 更新ユーザID
	 * @param facilityId ファシリティID
	 * @param info OS情報
	 * @param isCollect true:構成情報収集で実施, false:ノードプロパティより登録など
	 * @return 差分情報 (変更がない場合はnull)
	 */
	public static NodeConfigRegisterDiffInfo registerNodeOsInfo(
			Long registerDatetime, String modifyUserId, String facilityId, NodeOsInfo info, boolean isCollect) {

		long start = HinemosTime.currentTimeMillis();
		m_log.debug("registerNodeOsInfo() : start");

		NodeConfigRegisterDiffInfo diffInfo = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			Long leastRegDate = QueryUtil.getNodeHistoryByMaxRegDate(NodeConfigSettingItem.OS, facilityId);
			if (leastRegDate != null && registerDatetime <= leastRegDate) {
				// 既に最新の情報が登録されている場合はエラー
				m_log.warn(String.format("registerNodeOsInfo() : The latest node information has already been registered. ("
						+ "item=%s, facilityId=%s, registerDatetime=%d",
						NodeConfigSettingItem.OS.name(), facilityId, registerDatetime));
				return diffInfo;
			}

			diffInfo = new NodeConfigRegisterDiffInfo();

			if (info != null) {
				/** 差分登録処理(新規登録、変更) */
				NodeOsInfo entity = null;
				boolean isUpdate = false;
				try {
					entity = QueryUtil.getNodeOsEntityPkForNodeConfigSetting(facilityId);

					if (isCollect) {
						// 構成情報収集対象外を設定
						info.setCharacterSet(entity.getCharacterSet());
					}

					// 差分確認
					if (!entity.getOsName().equals(info.getOsName())
						|| !entity.getOsRelease().equals(info.getOsRelease())
						|| !entity.getOsVersion().equals(info.getOsVersion())
						|| !entity.getCharacterSet().equals(info.getCharacterSet())
						|| !entity.getStartupDateTime().equals(info.getStartupDateTime())) {

						// 差分がある場合
						isUpdate = true;

						// NodeOsHistoryDetail更新
						NodeOsHistoryDetail oldHistoryDetail 
							= QueryUtil.getNodeOsHistoryDetailByRegDateTo(facilityId, NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
						if (oldHistoryDetail != null) {
							oldHistoryDetail.setRegDateTo(registerDatetime);
						}

						// 変更情報
						diffInfo.addModObj(new NodeOsInfo[]{entity.clone(), info.clone()});
					}
				} catch (FacilityNotFound e) {

					// 差分がある場合
					isUpdate = true;

					// 重複チェック
					jtm.checkEntityExists(NodeOsInfo.class, facilityId);
					// 新規登録
					entity = new NodeOsInfo(facilityId);
					entity.setRegDate(registerDatetime);
					entity.setRegUser(modifyUserId);
					em.persist(entity);

					// 変更情報
					diffInfo.addAddObj(info.clone());
				}

				if (isUpdate) {
					// 新規登録・変更がある場合

					// 更新処理
					entity.setOsName(info.getOsName());
					entity.setOsRelease(info.getOsRelease());
					entity.setOsVersion(info.getOsVersion());
					entity.setCharacterSet(info.getCharacterSet());
					entity.setStartupDateTime(info.getStartupDateTime());
					entity.setUpdateDate(registerDatetime);
					entity.setUpdateUser(modifyUserId);

					// NodeOsHistoryDetail登録
					NodeOsHistoryDetail historyDetail = new NodeOsHistoryDetail(
							facilityId, registerDatetime);
					historyDetail.setOsName(info.getOsName());
					historyDetail.setOsRelease(info.getOsRelease());
					historyDetail.setOsVersion(info.getOsVersion());
					historyDetail.setCharacterSet(info.getCharacterSet());
					historyDetail.setStartupDateTime(info.getStartupDateTime());
					historyDetail.setRegUser(modifyUserId);
					em.persist(historyDetail);
				}
			} else {
				/** 差分登録処理(削除) */

				NodeOsInfo entity = null;
				try {
					entity = QueryUtil.getNodeOsEntityPkForNodeConfigSetting(facilityId);

					// NodeOsHistoryDetail更新
					NodeOsHistoryDetail oldHistoryDetail 
						= QueryUtil.getNodeOsHistoryDetailByRegDateTo(
								facilityId, NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
					if (oldHistoryDetail != null) {
						oldHistoryDetail.setRegDateTo(registerDatetime);
					}
					// 変更情報
					diffInfo.addDelObj(entity.clone());
				} catch (FacilityNotFound e) {
					// 何もしない
				}
			}

			if (diffInfo.getAddObj().size() == 0
					&& diffInfo.getModObj().size() == 0
					&& diffInfo.getDelObj().size() == 0) {
				diffInfo = null;
			}

			long end = HinemosTime.currentTimeMillis() - start;
			m_log.debug("registerNodeOsInfo : end (" + end + "ms)");

			return diffInfo;
		}
	}

	/**
	 * CPU情報の登録処理を行う。
	 * 
	 * @param registerDatetime 収集日時
	 * @param modifyUserId 更新ユーザID
	 * @param facilityId ファシリティID
	 * @param list CPU情報
	 * @param isCollect true:構成情報収集で実施, false:ノードプロパティより登録など
	 * @return 差分情報 (変更がない場合はnull)
	 */
	public static NodeConfigRegisterDiffInfo registerNodeCpuInfo(
		Long registerDatetime, String modifyUserId, String facilityId, List<NodeCpuInfo> list, boolean isCollect) {

		long start = HinemosTime.currentTimeMillis();
		m_log.debug("registerNodeCpuInfo() : start");

		NodeConfigRegisterDiffInfo diffInfo = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			Long leastRegDate = QueryUtil.getNodeHistoryByMaxRegDate(NodeConfigSettingItem.HW_CPU, facilityId);
			if (leastRegDate != null && registerDatetime <= leastRegDate) {
				// 既に最新の情報が登録されている場合はエラー
				m_log.warn(String.format("registerNodeCpuInfo() : The latest node information has already been registered. ("
						+ "item=%s, facilityId=%s, registerDatetime=%d",
						NodeConfigSettingItem.HW_CPU.name(), facilityId, registerDatetime));
				return diffInfo;
			}

			diffInfo = new NodeConfigRegisterDiffInfo();

			/** 差分登録処理(新規登録、変更) */
			List<NodeDeviceInfoPK> notDelPkList = new ArrayList<>();
			if (list != null) {
				for (NodeCpuInfo info : list) {
					NodeCpuInfo entity = null;
					NodeDeviceInfoPK entityPk = new NodeDeviceInfoPK(facilityId, info.getDeviceIndex(), info.getDeviceType(), info.getDeviceName());
					notDelPkList.add(entityPk);
					try {
						entity = QueryUtil.getNodeCpuEntityPkForNodeConfigSetting(entityPk);

						// 差分確認
						//DeviceDescriptionについては IgnoreMonフラグを考慮して一致を確認する。
						if (entity.getDeviceDisplayName().equals(info.getDeviceDisplayName())
								&& entity.getDeviceSize().equals(info.getDeviceSize())
								&& entity.getDeviceSizeUnit().equals(info.getDeviceSizeUnit())
								&& checkDescriptonDiff(isCollect,entity.getDeviceDescription(),info.getDeviceDescription())
								&& entity.getCoreCount().equals(info.getCoreCount())
								&& entity.getThreadCount().equals(info.getThreadCount())
								&& entity.getClockCount().equals(info.getClockCount())) {
							// 差分がなければ何もしない
							continue;
						}

						// NodeCpuHistoryDetail更新
						NodeCpuHistoryDetail oldHistoryDetail 
							= QueryUtil.getNodeCpuHistoryDetailByRegDateTo(entityPk, NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
						if (oldHistoryDetail != null) {
							oldHistoryDetail.setRegDateTo(registerDatetime);
						}

						// 構成情報管理による変更なら、元々、末尾に@IGNORE_MONITORがついていた場合は変更後も残るようにする。
						if ( isCollect && chkIgnoreMonitorCode(entity.getDeviceDescription()) && !( chkIgnoreMonitorCode(info.getDeviceDescription()) ) ) {
							info.setDeviceDescription(addIgnoreMonitorCode(info.getDeviceDescription()));
						}

						// 変更情報
						diffInfo.addModObj(new NodeCpuInfo[]{entity.clone(), info.clone()});
					} catch (FacilityNotFound e) {
						// 重複チェック
						jtm.checkEntityExists(NodeCpuInfo.class, entityPk);
						// 新規登録
						entity = new NodeCpuInfo(entityPk);
						entity.setRegDate(registerDatetime);
						entity.setRegUser(modifyUserId);
						em.persist(entity);
						// 変更情報
						diffInfo.addAddObj(info.clone());
					}

					// NodeCpuInfo
					entity.setDeviceDisplayName(info.getDeviceDisplayName());
					entity.setDeviceSize(info.getDeviceSize());
					entity.setDeviceSizeUnit(info.getDeviceSizeUnit());
					entity.setDeviceDescription(info.getDeviceDescription());
					entity.setCoreCount(info.getCoreCount());
					entity.setThreadCount(info.getThreadCount());
					entity.setClockCount(info.getClockCount());
					entity.setUpdateDate(registerDatetime);
					entity.setUpdateUser(modifyUserId);

					// NodeCpuHistoryDetail登録
					NodeCpuHistoryDetail historyDetail = new NodeCpuHistoryDetail(
							facilityId, info.getDeviceIndex(), info.getDeviceType(), info.getDeviceName(), registerDatetime);
					historyDetail.setDeviceDisplayName(info.getDeviceDisplayName());
					historyDetail.setDeviceSize(info.getDeviceSize());
					historyDetail.setDeviceSizeUnit(info.getDeviceSizeUnit());
					historyDetail.setDeviceDescription(info.getDeviceDescription());
					historyDetail.setCoreCount(info.getCoreCount());
					historyDetail.setThreadCount(info.getThreadCount());
					historyDetail.setClockCount(info.getClockCount());
					historyDetail.setRegUser(modifyUserId);
					em.persist(historyDetail);
				}
			}
			
			/** 差分登録処理(削除) */
			List<NodeCpuInfo> entityList = QueryUtil.getNodeCpuInfoByFacilityId(facilityId);
			for (NodeCpuInfo entity : entityList) {
				if (!notDelPkList.contains(entity.getId())) {
					// cc_cfg_node_cpu_info削除処理
					em.remove(entity);
					// NodeCpuHistoryDetail更新処理
					NodeCpuHistoryDetail oldHistoryDetail 
						= QueryUtil.getNodeCpuHistoryDetailByRegDateTo(
								entity.getId(), NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
					if (oldHistoryDetail != null) {
						oldHistoryDetail.setRegDateTo(registerDatetime);
					}

					// 変更情報
					diffInfo.addDelObj(entity.clone());
				}
			}
		}

		if (diffInfo.getAddObj().size() == 0
				&& diffInfo.getModObj().size() == 0
				&& diffInfo.getDelObj().size() == 0) {
			diffInfo = null;
		}

		long end = HinemosTime.currentTimeMillis() - start;
		m_log.debug("registerNodeCpuInfo : end (" + end + "ms)");

		return diffInfo;
	}

	/**
	 * メモリ情報の登録処理を行う。
	 * 
	 * @param registerDatetime 収集日時
	 * @param modifyUserId 更新ユーザID
	 * @param facilityId ファシリティID
	 * @param list メモリ情報
	 * @param isCollect true:構成情報収集で実施, false:ノードプロパティより登録など
	 * @return 差分情報 (変更がない場合はnull)
	 */
	public static NodeConfigRegisterDiffInfo registerNodeMemoryInfo(
		Long registerDatetime, String modifyUserId, String facilityId, List<NodeMemoryInfo> list, boolean isCollect) {

		long start = HinemosTime.currentTimeMillis();
		m_log.debug("registerNodeMemoryInfo() : start");

		NodeConfigRegisterDiffInfo diffInfo = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			Long leastRegDate = QueryUtil.getNodeHistoryByMaxRegDate(NodeConfigSettingItem.HW_MEMORY, facilityId);
			if (leastRegDate != null && registerDatetime <= leastRegDate) {
				// 既に最新の情報が登録されている場合はエラー
				m_log.warn(String.format("registerNodeMemoryInfo() : The latest node information has already been registered. ("
						+ "item=%s, facilityId=%s, registerDatetime=%d",
						NodeConfigSettingItem.HW_MEMORY.name(), facilityId, registerDatetime));
				return diffInfo;
			}

			diffInfo = new NodeConfigRegisterDiffInfo();

			/** 差分登録処理(新規登録、変更) */
			List<NodeDeviceInfoPK> notDelPkList = new ArrayList<>();
			if (list != null) {
				for (NodeMemoryInfo info : list) {
					NodeMemoryInfo entity = null;
					NodeDeviceInfoPK entityPk = new NodeDeviceInfoPK(facilityId, info.getDeviceIndex(), info.getDeviceType(), info.getDeviceName());
					notDelPkList.add(entityPk);
					try {
						entity = QueryUtil.getNodeMemoryEntityPkForNodeConfigSetting(entityPk);

						if (isCollect) {
							// 構成情報収集対象外を設定
							info.setDeviceDescription(entity.getDeviceDescription());
						}

						// 差分確認
						if (entity.getDeviceDisplayName().equals(info.getDeviceDisplayName())
								&& entity.getDeviceSize().equals(info.getDeviceSize())
								&& entity.getDeviceSizeUnit().equals(info.getDeviceSizeUnit())
								&& entity.getDeviceDescription().equals(info.getDeviceDescription())) {
							// 差分がなければ何もしない
							continue;
						}

						// NodeMemoryHistoryDetail更新
						NodeMemoryHistoryDetail oldHistoryDetail 
							= QueryUtil.getNodeMemoryHistoryDetailByRegDateTo(entityPk, NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
						if (oldHistoryDetail != null) {
							oldHistoryDetail.setRegDateTo(registerDatetime);
						}

						// 変更情報
						diffInfo.addModObj(new NodeMemoryInfo[]{entity.clone(), info.clone()});
					} catch (FacilityNotFound e) {
						// 重複チェック
						jtm.checkEntityExists(NodeMemoryInfo.class, entityPk);
						// 新規登録
						entity = new NodeMemoryInfo(entityPk);
						entity.setRegDate(registerDatetime);
						entity.setRegUser(modifyUserId);
						em.persist(entity);
						// 変更情報
						diffInfo.addAddObj(info.clone());
					}

					// NodeMemoryInfo
					entity.setDeviceDisplayName(info.getDeviceDisplayName());
					entity.setDeviceSize(info.getDeviceSize());
					entity.setDeviceSizeUnit(info.getDeviceSizeUnit());
					entity.setDeviceDescription(info.getDeviceDescription());
					entity.setUpdateDate(registerDatetime);
					entity.setUpdateUser(modifyUserId);

					// NodeMemoryHistoryDetail登録
					NodeMemoryHistoryDetail historyDetail = new NodeMemoryHistoryDetail(
							facilityId, info.getDeviceIndex(), info.getDeviceType(), info.getDeviceName(), registerDatetime);
					historyDetail.setDeviceDisplayName(info.getDeviceDisplayName());
					historyDetail.setDeviceSize(info.getDeviceSize());
					historyDetail.setDeviceSizeUnit(info.getDeviceSizeUnit());
					historyDetail.setDeviceDescription(info.getDeviceDescription());
					historyDetail.setRegUser(modifyUserId);
					em.persist(historyDetail);
				}
			}

			/** 差分登録処理(削除) */
			List<NodeMemoryInfo> entityList = QueryUtil.getNodeMemoryInfoByFacilityId(facilityId);
			for (NodeMemoryInfo entity : entityList) {
				if (!notDelPkList.contains(entity.getId())) {
					// cc_cfg_node_memory_info削除処理
					em.remove(entity);
					// NodeMemoryHistoryDetail更新処理
					NodeMemoryHistoryDetail oldHistoryDetail 
						= QueryUtil.getNodeMemoryHistoryDetailByRegDateTo(
								entity.getId(), NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
					if (oldHistoryDetail != null) {
						oldHistoryDetail.setRegDateTo(registerDatetime);
					}

					// 変更情報
					diffInfo.addDelObj(entity.clone());
				}
			}
		}

		if (diffInfo.getAddObj().size() == 0
				&& diffInfo.getModObj().size() == 0
				&& diffInfo.getDelObj().size() == 0) {
			diffInfo = null;
		}

		long end = HinemosTime.currentTimeMillis() - start;
		m_log.debug("registerNodeMemoryInfo : end (" + end + "ms)");

		return diffInfo;
	}

	/**
	 * NIC情報の登録処理を行う。
	 * 
	 * @param registerDatetime 収集日時
	 * @param modifyUserId 更新ユーザID
	 * @param facilityId ファシリティID
	 * @param list NIC情報
	 * @param isCollect true:構成情報収集で実施, false:ノードプロパティより登録など
	 * @return 差分情報 (変更がない場合はnull)
	 */
	public static NodeConfigRegisterDiffInfo registerNodeNetworkInterfaceInfo(
		Long registerDatetime, String modifyUserId, String facilityId, List<NodeNetworkInterfaceInfo> list, boolean isCollect) {

		long start = HinemosTime.currentTimeMillis();
		m_log.debug("registerNodeNetworkInterfaceInfo() : start");

		NodeConfigRegisterDiffInfo diffInfo = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			Long leastRegDate = QueryUtil.getNodeHistoryByMaxRegDate(NodeConfigSettingItem.HW_NIC, facilityId);
			if (leastRegDate != null && registerDatetime <= leastRegDate) {
				// 既に最新の情報が登録されている場合はエラー
				m_log.warn(String.format("registerNodeNetworkInterfaceInfo() : The latest node information has already been registered. ("
						+ "item=%s, facilityId=%s, registerDatetime=%d",
						NodeConfigSettingItem.HW_NIC.name(), facilityId, registerDatetime));
				return diffInfo;
			}

			diffInfo = new NodeConfigRegisterDiffInfo();

			/** 差分登録処理(新規登録、変更) */
			List<NodeDeviceInfoPK> notDelPkList = new ArrayList<>();
			if (list != null) {
				for (NodeNetworkInterfaceInfo info : list) {
					NodeNetworkInterfaceInfo entity = null;
					NodeDeviceInfoPK entityPk = new NodeDeviceInfoPK(facilityId, info.getDeviceIndex(), info.getDeviceType(), info.getDeviceName());
					notDelPkList.add(entityPk);
					try {
						entity = QueryUtil.getNodeNetworkInterfaceEntityPkForNodeConfigSetting(entityPk);

						// 差分確認
						//DeviceDescriptionについては IgnoreMonフラグを考慮して一致を確認する。
						if (entity.getDeviceDisplayName().equals(info.getDeviceDisplayName())
								&& entity.getDeviceSize().equals(info.getDeviceSize())
								&& entity.getDeviceSizeUnit().equals(info.getDeviceSizeUnit())
								&& checkDescriptonDiff(isCollect,entity.getDeviceDescription(),info.getDeviceDescription())
								&& entity.getNicIpAddress().equals(info.getNicIpAddress())
								&& entity.getNicMacAddress().equals(info.getNicMacAddress())) {
							// 差分がなければ何もしない
							continue;
						}

						// 構成情報管理による変更なら、元々、末尾に@IGNORE_MONITORがついていた場合は変更後も残るようにする。
						if ( isCollect && chkIgnoreMonitorCode(entity.getDeviceDescription()) && !( chkIgnoreMonitorCode(info.getDeviceDescription()) ) ) {
							info.setDeviceDescription(addIgnoreMonitorCode(info.getDeviceDescription()));
						}

						// NodeNetworkInterfaceHistoryDetail更新
						NodeNetworkInterfaceHistoryDetail oldHistoryDetail 
							= QueryUtil.getNodeNetworkInterfaceHistoryDetailByRegDateTo(entityPk, NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
						if (oldHistoryDetail != null) {
							oldHistoryDetail.setRegDateTo(registerDatetime);
						}

						// 変更情報
						diffInfo.addModObj(new NodeNetworkInterfaceInfo[]{entity.clone(), info.clone()});
					} catch (FacilityNotFound e) {
						// 重複チェック
						jtm.checkEntityExists(NodeNetworkInterfaceInfo.class, entityPk);
						// 新規登録
						entity = new NodeNetworkInterfaceInfo(entityPk);
						entity.setRegDate(registerDatetime);
						entity.setRegUser(modifyUserId);
						em.persist(entity);
						// 変更情報
						diffInfo.addAddObj(info.clone());
					}

					// NodeNetworkInterfaceInfo
					entity.setDeviceDisplayName(info.getDeviceDisplayName());
					entity.setDeviceSize(info.getDeviceSize());
					entity.setDeviceSizeUnit(info.getDeviceSizeUnit());
					entity.setDeviceDescription(info.getDeviceDescription());
					entity.setNicIpAddress(info.getNicIpAddress());
					entity.setNicMacAddress(info.getNicMacAddress());
					entity.setUpdateDate(registerDatetime);
					entity.setUpdateUser(modifyUserId);

					// NodeNetworkInterfaceHistoryDetail登録
					NodeNetworkInterfaceHistoryDetail historyDetail = new NodeNetworkInterfaceHistoryDetail(
							facilityId, info.getDeviceIndex(), info.getDeviceType(), info.getDeviceName(), registerDatetime);
					historyDetail.setDeviceDisplayName(info.getDeviceDisplayName());
					historyDetail.setDeviceSize(info.getDeviceSize());
					historyDetail.setDeviceSizeUnit(info.getDeviceSizeUnit());
					historyDetail.setDeviceDescription(info.getDeviceDescription());
					historyDetail.setNicIpAddress(info.getNicIpAddress());
					historyDetail.setNicMacAddress(info.getNicMacAddress());
					historyDetail.setRegUser(modifyUserId);
					em.persist(historyDetail);
				}
			}

			/** 差分登録処理(削除) */
			List<NodeNetworkInterfaceInfo> entityList = QueryUtil.getNodeNetworkInterfaceInfoByFacilityId(facilityId);
			for (NodeNetworkInterfaceInfo entity : entityList) {
				if (!notDelPkList.contains(entity.getId())) {
					//クラウド自動検出で登録されたNICは削除しない
					if(isCollect && entity.getDeviceType().equals("vnic")){
						m_log.debug("registerNodeNetworkInterfaceInfo(): "+entity.getDeviceName()+" is vnic. Do not delete");
						continue;
					}
					// cc_cfg_node_network_interface_info削除処理
					em.remove(entity);
					// NodeNetworkInterfaceHistoryDetail更新
					NodeNetworkInterfaceHistoryDetail oldHistoryDetail 
						= QueryUtil.getNodeNetworkInterfaceHistoryDetailByRegDateTo(entity.getId(), NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
					if (oldHistoryDetail != null) {
						oldHistoryDetail.setRegDateTo(registerDatetime);
					}

					// 変更情報
					diffInfo.addDelObj(entity.clone());
				}
			}
		}

		if (diffInfo.getAddObj().size() == 0
				&& diffInfo.getModObj().size() == 0
				&& diffInfo.getDelObj().size() == 0) {
			diffInfo = null;
		}

		long end = HinemosTime.currentTimeMillis() - start;
		m_log.debug("registerNodeNetworkInterfaceInfo : end (" + end + "ms)");

		return diffInfo;
	}

	/**
	 * ディスク情報の登録処理を行う。
	 * 
	 * @param registerDatetime 収集日時
	 * @param modifyUserId 更新ユーザID
	 * @param facilityId ファシリティID
	 * @param list ディスク情報
	 * @param isCollect true:構成情報収集で実施, false:ノードプロパティより登録など
	 * @return 差分情報 (変更がない場合はnull)
	 */
	public static NodeConfigRegisterDiffInfo registerNodeDiskInfo(
		Long registerDatetime, String modifyUserId, String facilityId, List<NodeDiskInfo> list, boolean isCollect) {

		long start = HinemosTime.currentTimeMillis();
		m_log.debug("registerNodeDiskInfo() : start");

		NodeConfigRegisterDiffInfo diffInfo = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			Long leastRegDate = QueryUtil.getNodeHistoryByMaxRegDate(NodeConfigSettingItem.HW_DISK, facilityId);
			if (leastRegDate != null && registerDatetime <= leastRegDate) {
				// 既に最新の情報が登録されている場合はエラー
				m_log.warn(String.format("registerNodeDiskInfo() : The latest node information has already been registered. ("
						+ "item=%s, facilityId=%s, registerDatetime=%d",
						NodeConfigSettingItem.HW_DISK.name(), facilityId, registerDatetime));
				return diffInfo;
			}

			diffInfo = new NodeConfigRegisterDiffInfo();

			/** 差分登録処理(新規登録、変更) */
			List<NodeDeviceInfoPK> notDelPkList = new ArrayList<>();
			if (list != null) {
				for (NodeDiskInfo info : list) {
					NodeDiskInfo entity = null;
					NodeDeviceInfoPK entityPk = new NodeDeviceInfoPK(facilityId, info.getDeviceIndex(), info.getDeviceType(), info.getDeviceName());
					notDelPkList.add(entityPk);
					try {
						entity = QueryUtil.getNodeDiskEntityPkForNodeConfigSetting(entityPk);

						if (isCollect) {
							// 構成情報収集対象外を設定
							info.setDiskRpm(entity.getDiskRpm());
						}

						// 差分確認
						//DeviceDescriptionについては IgnoreMonフラグを考慮して一致を確認する。
						if (entity.getDeviceDisplayName().equals(info.getDeviceDisplayName())
								&& entity.getDeviceSize().equals(info.getDeviceSize())
								&& entity.getDeviceSizeUnit().equals(info.getDeviceSizeUnit())
								&& checkDescriptonDiff(isCollect,entity.getDeviceDescription(),info.getDeviceDescription())
								&& entity.getDiskRpm().equals(info.getDiskRpm())) {
							// 差分がなければ何もしない
							continue;
						}

						// 構成情報管理による変更なら、元々、末尾に@IGNORE_MONITORがついていた場合は変更後も残るようにする。
						if ( isCollect && chkIgnoreMonitorCode(entity.getDeviceDescription()) && !( chkIgnoreMonitorCode(info.getDeviceDescription()) ) ) {
							info.setDeviceDescription(addIgnoreMonitorCode(info.getDeviceDescription()));
						}

						// NodeDiskHistoryDetail更新
						NodeDiskHistoryDetail oldHistoryDetail 
							= QueryUtil.getNodeDiskHistoryDetailByRegDateTo(entityPk, NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
						if (oldHistoryDetail != null) {
							oldHistoryDetail.setRegDateTo(registerDatetime);
						}

						// 変更情報
						diffInfo.addModObj(new NodeDiskInfo[]{entity.clone(), info.clone()});
					} catch (FacilityNotFound e) {
						// 重複チェック
						jtm.checkEntityExists(NodeDiskInfo.class, entityPk);
						// 新規登録
						entity = new NodeDiskInfo(entityPk);
						entity.setRegDate(registerDatetime);
						entity.setRegUser(modifyUserId);
						em.persist(entity);
						// 変更情報
						diffInfo.addAddObj(info.clone());
					}

					// NodeDiskInfo
					entity.setDeviceDisplayName(info.getDeviceDisplayName());
					entity.setDeviceSize(info.getDeviceSize());
					entity.setDeviceSizeUnit(info.getDeviceSizeUnit());
					entity.setDeviceDescription(info.getDeviceDescription());
					entity.setDiskRpm(info.getDiskRpm());
					entity.setUpdateDate(registerDatetime);
					entity.setUpdateUser(modifyUserId);

					// NodeDiskHistoryDetail登録
					NodeDiskHistoryDetail historyDetail = new NodeDiskHistoryDetail(
							facilityId, info.getDeviceIndex(), info.getDeviceType(), info.getDeviceName(), registerDatetime);
					historyDetail.setDeviceDisplayName(info.getDeviceDisplayName());
					historyDetail.setDeviceSize(info.getDeviceSize());
					historyDetail.setDeviceSizeUnit(info.getDeviceSizeUnit());
					historyDetail.setDeviceDescription(info.getDeviceDescription());
					historyDetail.setDiskRpm(info.getDiskRpm());
					historyDetail.setRegUser(modifyUserId);
					em.persist(historyDetail);
				}
			}

			/** 差分登録処理(削除) */
			List<NodeDiskInfo> entityList = QueryUtil.getNodeDiskInfoByFacilityId(facilityId);
			for (NodeDiskInfo entity : entityList) {
				if (!notDelPkList.contains(entity.getId())) {
					//クラウド自動検出で検出されたディスクは削除しない
					if(isCollect && entity.getDeviceType().equals("vdisk")){
						m_log.debug("registerNodeDiskInfo(): "+entity.getDeviceName()+" is vdisk. Do not delete");
						continue;
					}
					// cc_cfg_node_disk_info削除処理
					em.remove(entity);
					// NodeDiskHistoryDetail更新
					NodeDiskHistoryDetail oldHistoryDetail 
						= QueryUtil.getNodeDiskHistoryDetailByRegDateTo(entity.getId(), NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
					if (oldHistoryDetail != null) {
						oldHistoryDetail.setRegDateTo(registerDatetime);
					}

					// 変更情報
					diffInfo.addDelObj(entity.clone());
				}
			}
		}

		if (diffInfo.getAddObj().size() == 0
				&& diffInfo.getModObj().size() == 0
				&& diffInfo.getDelObj().size() == 0) {
			diffInfo = null;
		}

		long end = HinemosTime.currentTimeMillis() - start;
		m_log.debug("registerNodeDiskInfo : end (" + end + "ms)");

		return diffInfo;
	}

	/**
	 * ファイルシステム情報の登録処理を行う。
	 * 
	 * @param registerDatetime 収集日時
	 * @param modifyUserId 更新ユーザID
	 * @param facilityId ファシリティID
	 * @param list ファイルシステム情報
	 * @param isCollect true:構成情報収集で実施, false:ノードプロパティより登録など
	 * @return 差分情報 (変更がない場合はnull)
	 */
	public static NodeConfigRegisterDiffInfo registerNodeFilesystemInfo(
		Long registerDatetime, String modifyUserId, String facilityId, List<NodeFilesystemInfo> list, boolean isCollect) {

		long start = HinemosTime.currentTimeMillis();
		m_log.debug("registerNodeFilesystemInfo() : start");

		NodeConfigRegisterDiffInfo diffInfo = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			Long leastRegDate = QueryUtil.getNodeHistoryByMaxRegDate(NodeConfigSettingItem.HW_FILESYSTEM, facilityId);
			if (leastRegDate != null && registerDatetime <= leastRegDate) {
				// 既に最新の情報が登録されている場合はエラー
				m_log.warn(String.format("registerNodeFilesystemInfo() : The latest node information has already been registered. ("
						+ "item=%s, facilityId=%s, registerDatetime=%d",
						NodeConfigSettingItem.HW_FILESYSTEM.name(), facilityId, registerDatetime));
				return diffInfo;
			}

			diffInfo = new NodeConfigRegisterDiffInfo();

			/** 差分登録処理(新規登録、変更) */
			List<NodeDeviceInfoPK> notDelPkList = new ArrayList<>();
			if (list != null) {
				for (NodeFilesystemInfo info : list) {
					NodeFilesystemInfo entity = null;
					NodeDeviceInfoPK entityPk = new NodeDeviceInfoPK(facilityId, info.getDeviceIndex(), info.getDeviceType(), info.getDeviceName());
					notDelPkList.add(entityPk);
					try {
						entity = QueryUtil.getNodeFilesystemEntityPkForNodeConfigSetting(entityPk);

						// 差分確認
						//DeviceDescriptionについては IgnoreMonフラグを考慮して一致を確認する。
						if (entity.getDeviceDisplayName().equals(info.getDeviceDisplayName())
								&& entity.getDeviceSize().equals(info.getDeviceSize())
								&& entity.getDeviceSizeUnit().equals(info.getDeviceSizeUnit())
								&& checkDescriptonDiff(isCollect,entity.getDeviceDescription(),info.getDeviceDescription())
								&& entity.getFilesystemType().equals(info.getFilesystemType())) {
							// 差分がなければ何もしない
							continue;
						}

						// 構成情報管理による変更なら、元々、末尾に@IGNORE_MONITORがついていた場合は変更後も残るようにする。
						if ( isCollect && chkIgnoreMonitorCode(entity.getDeviceDescription()) && !( chkIgnoreMonitorCode(info.getDeviceDescription()) ) ) {
							info.setDeviceDescription(addIgnoreMonitorCode(info.getDeviceDescription()));
						}

						// NodeFilesystemHistoryDetail更新
						NodeFilesystemHistoryDetail oldHistoryDetail 
							= QueryUtil.getNodeFilesystemHistoryDetailByRegDateTo(entityPk, NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
						if (oldHistoryDetail != null) {
							oldHistoryDetail.setRegDateTo(registerDatetime);
						}
						// 変更情報
						diffInfo.addModObj(new NodeFilesystemInfo[]{entity.clone(), info.clone()});
					} catch (FacilityNotFound e) {
						// 重複チェック
						jtm.checkEntityExists(NodeFilesystemInfo.class, entityPk);
						// 新規登録
						entity = new NodeFilesystemInfo(entityPk);
						entity.setRegDate(registerDatetime);
						entity.setRegUser(modifyUserId);
						em.persist(entity);
						// 変更情報
						diffInfo.addAddObj(info.clone());
					}

					// NodeFilesystemInfo
					entity.setDeviceDisplayName(info.getDeviceDisplayName());
					entity.setDeviceSize(info.getDeviceSize());
					entity.setDeviceSizeUnit(info.getDeviceSizeUnit());
					entity.setDeviceDescription(info.getDeviceDescription());
					entity.setFilesystemType(info.getFilesystemType());
					entity.setUpdateDate(registerDatetime);
					entity.setUpdateUser(modifyUserId);

					// NodeFilesystemHistoryDetail登録
					NodeFilesystemHistoryDetail historyDetail = new NodeFilesystemHistoryDetail(
							facilityId, info.getDeviceIndex(), info.getDeviceType(), info.getDeviceName(), registerDatetime);
					historyDetail.setDeviceDisplayName(info.getDeviceDisplayName());
					historyDetail.setDeviceSize(info.getDeviceSize());
					historyDetail.setDeviceSizeUnit(info.getDeviceSizeUnit());
					historyDetail.setDeviceDescription(info.getDeviceDescription());
					historyDetail.setFilesystemType(info.getFilesystemType());
					historyDetail.setRegUser(modifyUserId);
					em.persist(historyDetail);
				}
			}

			/** 差分登録処理(削除) */
			List<NodeFilesystemInfo> entityList = QueryUtil.getNodeFilesystemInfoByFacilityId(facilityId);
			for (NodeFilesystemInfo entity : entityList) {
				if (!notDelPkList.contains(entity.getId())) {
					// cc_cfg_node_filesystem_info削除処理
					em.remove(entity);
					// NodeFilesystemHistoryDetail更新
					NodeFilesystemHistoryDetail oldHistoryDetail 
						= QueryUtil.getNodeFilesystemHistoryDetailByRegDateTo(entity.getId(), NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
					if (oldHistoryDetail != null) {
						oldHistoryDetail.setRegDateTo(registerDatetime);
					}

					// 変更情報
					diffInfo.addDelObj(entity.clone());
				}
			}
		}

		if (diffInfo.getAddObj().size() == 0
				&& diffInfo.getModObj().size() == 0
				&& diffInfo.getDelObj().size() == 0) {
			diffInfo = null;
		}

		long end = HinemosTime.currentTimeMillis() - start;
		m_log.debug("registerNodeFilesystemInfo : end (" + end + "ms)");

		return diffInfo;
	}

	/**
	 * ノード変数情報の登録処理を行う。
	 * 
	 * @param registerDatetime 収集日時
	 * @param modifyUserId 更新ユーザID
	 * @param facilityId ファシリティID
	 * @param list ノード変数情報
	 * @param isCollect true:構成情報収集で実施, false:ノードプロパティより登録など
	 * @return 差分情報 (変更がない場合はnull)
	 */
	public static NodeConfigRegisterDiffInfo registerNodeVariableInfo(
		Long registerDatetime, String modifyUserId, String facilityId, List<NodeVariableInfo> list, boolean isCollect) {

		long start = HinemosTime.currentTimeMillis();
		m_log.debug("registerNodeVariableInfo() : start");

		NodeConfigRegisterDiffInfo diffInfo = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			Long leastRegDate = QueryUtil.getNodeHistoryByMaxRegDate(NodeConfigSettingItem.NODE_VARIABLE, facilityId);
			if (leastRegDate != null && registerDatetime <= leastRegDate) {
				// 既に最新の情報が登録されている場合はエラー
				m_log.warn(String.format("registerNodeVariableInfo() : The latest node information has already been registered. ("
						+ "item=%s, facilityId=%s, registerDatetime=%d",
						NodeConfigSettingItem.NODE_VARIABLE.name(), facilityId, registerDatetime));
				return diffInfo;
			}

			diffInfo = new NodeConfigRegisterDiffInfo();

			/** 差分登録処理(新規登録、変更) */
			List<NodeVariableInfoPK> notDelPkList = new ArrayList<>();
			if (list != null) {
				for (NodeVariableInfo info : list) {
					NodeVariableInfo entity = null;
					NodeVariableInfoPK entityPk = new NodeVariableInfoPK(facilityId, info.getNodeVariableName());
					notDelPkList.add(entityPk);
					try {
						entity = QueryUtil.getNodeVariablePkForNodeConfigSetting(entityPk);

						// 差分確認
						if (entity.getNodeVariableValue().equals(info.getNodeVariableValue())) {
							// 差分がなければ何もしない
							continue;
						}

						// NodeVariableHistoryDetail更新
						NodeVariableHistoryDetail oldHistoryDetail 
							= QueryUtil.getNodeVariableHistoryDetailByRegDateTo(entityPk, NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
						if (oldHistoryDetail != null) {
							oldHistoryDetail.setRegDateTo(registerDatetime);
						}

						// 変更情報
						diffInfo.addModObj(new NodeVariableInfo[]{entity.clone(), info.clone()});
					} catch (FacilityNotFound e) {
						// 重複チェック
						jtm.checkEntityExists(NodeVariableInfo.class, entityPk);
						// 新規登録
						entity = new NodeVariableInfo(entityPk);
						entity.setRegDate(registerDatetime);
						entity.setRegUser(modifyUserId);
						em.persist(entity);
						// 変更情報
						diffInfo.addAddObj(info.clone());
					}

					// NodeVariableInfo
					entity.setNodeVariableValue(info.getNodeVariableValue());
					entity.setUpdateDate(registerDatetime);
					entity.setUpdateUser(modifyUserId);

					// NodeVariableHistoryDetail登録
					NodeVariableHistoryDetail historyDetail = new NodeVariableHistoryDetail(
							facilityId, info.getNodeVariableName(), registerDatetime);
					historyDetail.setNodeVariableValue(info.getNodeVariableValue());
					historyDetail.setRegUser(modifyUserId);
					em.persist(historyDetail);
				}
			}

			/** 差分登録処理(削除) */
			List<NodeVariableInfo> entityList = QueryUtil.getNodeVariableInfoByFacilityId(facilityId);
			for (NodeVariableInfo entity : entityList) {
				if (!notDelPkList.contains(entity.getId())) {
					// cc_cfg_node_variable_info削除処理
					em.remove(entity);
					// NodeVariableHistoryDetail更新
					NodeVariableHistoryDetail oldHistoryDetail 
						= QueryUtil.getNodeVariableHistoryDetailByRegDateTo(entity.getId(), NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
					if (oldHistoryDetail != null) {
						oldHistoryDetail.setRegDateTo(registerDatetime);
					}

					// 変更情報
					diffInfo.addDelObj(entity.clone());
				}
			}
		}

		if (diffInfo.getAddObj().size() == 0
				&& diffInfo.getModObj().size() == 0
				&& diffInfo.getDelObj().size() == 0) {
			diffInfo = null;
		}

		long end = HinemosTime.currentTimeMillis() - start;
		m_log.debug("registerNodeVariableInfo : end (" + end + "ms)");

		return diffInfo;
	}

	/**
	 * ホスト名情報の登録処理を行う。
	 * 
	 * @param registerDatetime 収集日時
	 * @param modifyUserId 更新ユーザID
	 * @param facilityId ファシリティID
	 * @param list ホスト名情報
	 * @param isCollect true:構成情報収集で実施, false:ノードプロパティより登録など
	 * @return 差分情報 (変更がない場合はnull)
	 */
	public static NodeConfigRegisterDiffInfo registerNodeHostnameInfo(
		Long registerDatetime, String modifyUserId, String facilityId, List<NodeHostnameInfo> list, boolean isCollect) {

		long start = HinemosTime.currentTimeMillis();
		m_log.debug("registerNodeHostnameInfo() : start");

		NodeConfigRegisterDiffInfo diffInfo = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			Long leastRegDate = QueryUtil.getNodeHistoryByMaxRegDate(NodeConfigSettingItem.HOSTNAME, facilityId);
			if (leastRegDate != null && registerDatetime <= leastRegDate) {
				// 既に最新の情報が登録されている場合はエラー
				m_log.warn(String.format("registerNodeHostnameInfo() : The latest node information has already been registered. ("
						+ "item=%s, facilityId=%s, registerDatetime=%d",
						NodeConfigSettingItem.HOSTNAME.name(), facilityId, registerDatetime));
				return diffInfo;
			}

			diffInfo = new NodeConfigRegisterDiffInfo();

			/** 差分登録処理(新規登録、変更) */
			List<NodeHostnameInfoPK> notDelPkList = new ArrayList<>();
			if (list != null) {
				for (NodeHostnameInfo info : list) {
					if (info.getHostname() == null || info.getHostname().equals("")) {
						continue;
					}
					NodeHostnameInfo entity = null;
					NodeHostnameInfoPK entityPk = new NodeHostnameInfoPK(facilityId, info.getHostname());
					notDelPkList.add(entityPk);
					try {
						entity = QueryUtil.getNodeHostnamePkForNodeConfigSetting(entityPk);
						// 存在する場合は何もしない
						continue;
					} catch (FacilityNotFound e) {
						// 重複チェック
						jtm.checkEntityExists(NodeHostnameInfo.class, entityPk);
						// 新規登録
						entity = new NodeHostnameInfo(entityPk);
						entity.setRegDate(registerDatetime);
						entity.setRegUser(modifyUserId);
						em.persist(entity);
						// 変更情報
						diffInfo.addAddObj(info.clone());
					}

					// NodeHostnameHistoryDetail登録
					NodeHostnameHistoryDetail historyDetail = new NodeHostnameHistoryDetail(
							facilityId, info.getHostname(), registerDatetime);
					historyDetail.setHostnameItem(info.getHostname());
					historyDetail.setRegUser(modifyUserId);
					em.persist(historyDetail);
				}
			}

			/** 差分登録処理(削除) */
			List<NodeHostnameInfo> entityList = QueryUtil.getNodeHostnameInfoByFacilityId(facilityId);
			for (NodeHostnameInfo entity : entityList) {
				if (!notDelPkList.contains(entity.getId())) {
					// cc_cfg_node_hostname_info削除処理
					em.remove(entity);
					// NodeHostnameHistoryDetail更新
					NodeHostnameHistoryDetail oldHistoryDetail 
						= QueryUtil.getNodeHostnameHistoryDetailByRegDateTo(entity.getId(), NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
					if (oldHistoryDetail != null) {
						oldHistoryDetail.setRegDateTo(registerDatetime);
					}

					// 変更情報
					diffInfo.addDelObj(entity.clone());
				}
			}
		}

		if (diffInfo.getAddObj().size() == 0
				&& diffInfo.getModObj().size() == 0
				&& diffInfo.getDelObj().size() == 0) {
			diffInfo = null;
		}

		long end = HinemosTime.currentTimeMillis() - start;
		m_log.debug("registerNodeHostnameInfo : end (" + end + "ms)");

		return diffInfo;
	}

	/**
	 * ネットワーク接続情報の登録処理を行う。
	 * 
	 * @param registerDatetime 収集日時
	 * @param modifyUserId 更新ユーザID
	 * @param facilityId ファシリティID
	 * @param list ネットワーク接続情報
	 * @param isCollect true:構成情報収集で実施, false:ノードプロパティより登録など
	 * @return 差分情報 (変更がない場合はnull)
	 */
	public static NodeConfigRegisterDiffInfo registerNodeNetstatInfo(
			Long registerDatetime, String modifyUserId, String facilityId, List<NodeNetstatInfo> list, boolean isCollect) {

		List<String> listenString = new ArrayList<>();
		listenString.add("LISTEN");
		listenString.add("LISTENING");
		listenString.add("UNCONN");

		long start = HinemosTime.currentTimeMillis();
		m_log.debug("registerNodeNetstatInfo() : start");

		NodeConfigRegisterDiffInfo diffInfo = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			Long leastRegDate = QueryUtil.getNodeHistoryByMaxRegDate(NodeConfigSettingItem.NETSTAT, facilityId);
			if (leastRegDate != null && registerDatetime <= leastRegDate) {
				// 既に最新の情報が登録されている場合はエラー
				m_log.warn(String.format("registerNodeNetstatInfo() : The latest node information has already been registered. ("
						+ "item=%s, facilityId=%s, registerDatetime=%d",
						NodeConfigSettingItem.NETSTAT.name(), facilityId, registerDatetime));
				return diffInfo;
			}

			diffInfo = new NodeConfigRegisterDiffInfo();

			/** 差分登録処理(新規登録、変更) */
			List<NodeNetstatInfoPK> notDelPkList = new ArrayList<>();
			if (list != null) {
				for (NodeNetstatInfo info : list) {
					boolean isNotListen = false;
					NodeNetstatInfo entity = null;
					NodeNetstatInfoPK entityPk = new NodeNetstatInfoPK(
							facilityId, info.getProtocol(), info.getLocalIpAddress(), info.getLocalPort(), info.getForeignIpAddress(), info.getForeignPort(),
							info.getProcessName(), info.getPid());
					notDelPkList.add(entityPk);
					try {
						entity = QueryUtil.getNodeNetstatEntityPkForNodeConfigSetting(entityPk);

						// 差分確認
						if (entity.getStatus().equals(info.getStatus())) {
							// 差分がなければ何もしない
							continue;
						}
						if (listenString.contains(entity.getStatus())) {
							NodeNetstatHistoryDetail oldHistoryDetail 
							= QueryUtil.getNodeNetstatHistoryDetailByRegDateTo(entityPk, NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
							if (oldHistoryDetail != null) {
								oldHistoryDetail.setRegDateTo(registerDatetime);
							}
							if (listenString.contains(info.getStatus())) {
								// 変更前、後ともにLISTEN -> 履歴更新
								// 変更情報
								diffInfo.addModObj(new NodeNetstatInfo[]{entity.clone(), info.clone()});
							} else {
								// 変更前のみLISTEN -> 履歴削除
								// 変更情報
								diffInfo.addDelObj(entity.clone());
								isNotListen = true;
							}
						} else {
							if (listenString.contains(info.getStatus())) {
								// 変更前がLISTEN以外、後がLISTEN -> 履歴新規登録
								// 変更情報
								diffInfo.addAddObj(info.clone());
							} else {
								// 変更前、後ともにLISTEN以外 -> 履歴対象外
								isNotListen = true;
							}
						}
					} catch (FacilityNotFound e) {
						// 重複チェック
						jtm.checkEntityExists(NodeNetstatInfo.class, entityPk);
						// 新規登録
						entity = new NodeNetstatInfo(entityPk);
						entity.setRegDate(registerDatetime);
						entity.setRegUser(modifyUserId);
						em.persist(entity);
						if (listenString.contains(info.getStatus())) {
							// LISTEN -> 履歴新規登録
							// 変更情報
							diffInfo.addAddObj(info.clone());
						} else {
							// LISTEN以外 -> 履歴対象外
							isNotListen = true;
						}
					}

					// NodeNetstatInfo
					entity.setStatus(info.getStatus());
					entity.setUpdateDate(registerDatetime);
					entity.setUpdateUser(modifyUserId);

					if (!isNotListen) {
						// NodeNetstatHistoryDetail登録
						NodeNetstatHistoryDetail historyDetail = new NodeNetstatHistoryDetail(
								facilityId, info.getProtocol(), info.getLocalIpAddress(), info.getLocalPort(), info.getForeignIpAddress(), info.getForeignPort(), 
								info.getProcessName(), info.getPid(), registerDatetime);
						historyDetail.setStatus(info.getStatus());
						historyDetail.setRegUser(modifyUserId);
						em.persist(historyDetail);
					}
				}
			}

			/** 差分登録処理(削除) */
			List<NodeNetstatInfo> entityList = QueryUtil.getNodeNetstatInfoByFacilityId(facilityId);
			for (NodeNetstatInfo entity : entityList) {
				if (!notDelPkList.contains(entity.getId())) {
					if (listenString.contains(entity.getStatus())) { 
						// NodeNetstatHistoryDetail更新処理
						NodeNetstatHistoryDetail oldHistoryDetail 
							= QueryUtil.getNodeNetstatHistoryDetailByRegDateTo(
									entity.getId(), NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
						if (oldHistoryDetail != null) {
							oldHistoryDetail.setRegDateTo(registerDatetime);
						}
						// 変更情報
						diffInfo.addDelObj(entity.clone());
					}
					// cc_cfg_node_pacakge_info削除処理
					em.remove(entity);
				}
			}
		}

		if (diffInfo.getAddObj().size() == 0
				&& diffInfo.getModObj().size() == 0
				&& diffInfo.getDelObj().size() == 0) {
			diffInfo = null;
		}

		long end = HinemosTime.currentTimeMillis() - start;
		m_log.debug("registerNodeNetstatInfo : end (" + end + "ms)");

		return diffInfo;
	}

	/**
	 * プロセス情報の登録処理を行う。
	 * 
	 * @param registerDatetime 収集日時
	 * @param modifyUserId 更新ユーザID
	 * @param facilityId ファシリティID
	 * @param list プロセス情報
	 * @return true:更新されている
	 */
	public static boolean registerNodeProcessInfo(Long registerDatetime, String modifyUserId, String facilityId, List<NodeProcessInfo> list) {

		long start = HinemosTime.currentTimeMillis();
		m_log.debug("registerNodeProcessInfo() : start");

		boolean isUpdate = false;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			/** cc_cfg_node_process_infoテーブルから削除 */ 
			QueryUtil.deleteNodeOptionalInfoByFacilityId(NodeProcessInfo.class, facilityId);

			em.flush();

			/** cc_cfg_node_process_infoテーブルに登録 */ 
			/** 差分登録処理(新規登録、変更) */
			if (list != null) {
				for (NodeProcessInfo info : list) {
					NodeProcessInfoPK entityPk = new NodeProcessInfoPK(facilityId, info.getProcessName(), info.getPid());
					// 重複チェック
					jtm.checkEntityExists(NodeProcessInfo.class, entityPk);
					// 新規登録
					NodeProcessInfo entity = new NodeProcessInfo(entityPk);
					entity.setPath(info.getPath());
					entity.setExecUser(info.getExecUser());
					entity.setStartupDateTime(info.getStartupDateTime());
					entity.setRegDate(registerDatetime);
					entity.setRegUser(modifyUserId);
					em.persist(entity);

					if (!isUpdate) {
						isUpdate = true;
					}
				}
			}
		}

		long end = HinemosTime.currentTimeMillis() - start;
		m_log.debug("registerNodeProcessInfo : end (" + end + "ms)");

		return isUpdate;
	}

	/**
	 * パッケージ情報の登録処理を行う。
	 * 
	 * @param registerDatetime 収集日時
	 * @param modifyUserId 更新ユーザID
	 * @param facilityId ファシリティID
	 * @param list パッケージ情報
	 * @param isCollect true:構成情報収集で実施, false:ノードプロパティより登録など
	 * @return 差分情報 (変更がない場合はnull)
	 */
	public static NodeConfigRegisterDiffInfo registerNodePackageInfo(
			Long registerDatetime, String modifyUserId, String facilityId, List<NodePackageInfo> list, boolean isCollect) {

		long start = HinemosTime.currentTimeMillis();
		m_log.debug("registerNodePackageInfo() : start");

		NodeConfigRegisterDiffInfo diffInfo = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			Long leastRegDate = QueryUtil.getNodeHistoryByMaxRegDate(NodeConfigSettingItem.PACKAGE, facilityId);
			if (leastRegDate != null && registerDatetime <= leastRegDate) {
				// 既に最新の情報が登録されている場合はエラー
				m_log.warn(String.format("registerNodePackageInfo() : The latest node information has already been registered. ("
						+ "item=%s, facilityId=%s, registerDatetime=%d",
						NodeConfigSettingItem.PACKAGE.name(), facilityId, registerDatetime));
				return diffInfo;
			}

			diffInfo = new NodeConfigRegisterDiffInfo();

			/** 差分登録処理(新規登録、変更) */
			List<NodePackageInfoPK> notDelPkList = new ArrayList<>();
			if (list != null) {
				for (NodePackageInfo info : list) {
					NodePackageInfo entity = null;
					NodePackageInfoPK entityPk = new NodePackageInfoPK(facilityId, info.getPackageId());
					notDelPkList.add(entityPk);
					try {
						entity = QueryUtil.getNodePackageEntityPkForNodeConfigSetting(entityPk);

						// 差分確認
						if (entity.getPackageName().equals(info.getPackageName())
								&& entity.getVersion().equals(info.getVersion())
								&& entity.getRelease().equals(info.getRelease())
								&& entity.getInstallDate().equals(info.getInstallDate())
								&& entity.getVendor().equals(info.getVendor())
								&& entity.getArchitecture().equals(info.getArchitecture())) {
							// 差分がなければ何もしない
							continue;
						}

						// NodePackageHistoryDetail更新
						NodePackageHistoryDetail oldHistoryDetail 
							= QueryUtil.getNodePackageHistoryDetailByRegDateTo(entityPk, NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
						if (oldHistoryDetail != null) {
							oldHistoryDetail.setRegDateTo(registerDatetime);
						}
						// 変更情報
						diffInfo.addModObj(new NodePackageInfo[]{entity.clone(), info.clone()});
					} catch (FacilityNotFound e) {
						// 重複チェック
						jtm.checkEntityExists(NodePackageInfo.class, entityPk);
						// 新規登録
						entity = new NodePackageInfo(entityPk);
						entity.setRegDate(registerDatetime);
						entity.setRegUser(modifyUserId);
						em.persist(entity);
						// 変更情報
						diffInfo.addAddObj(info.clone());
					}

					// NodePackageInfo
					entity.setPackageName(info.getPackageName());
					entity.setVersion(info.getVersion());
					entity.setRelease(info.getRelease());
					entity.setInstallDate(info.getInstallDate());
					entity.setVendor(info.getVendor());
					entity.setArchitecture(info.getArchitecture());
					entity.setUpdateDate(registerDatetime);
					entity.setUpdateUser(modifyUserId);

					// NodePackageHistoryDetail登録
					NodePackageHistoryDetail historyDetail = new NodePackageHistoryDetail(
							facilityId, info.getPackageId(), registerDatetime);
					historyDetail.setPackageName(info.getPackageName());
					historyDetail.setVersion(info.getVersion());
					historyDetail.setRelease(info.getRelease());
					historyDetail.setInstallDate(info.getInstallDate());
					historyDetail.setVendor(info.getVendor());
					historyDetail.setArchitecture(info.getArchitecture());
					historyDetail.setRegUser(modifyUserId);
					em.persist(historyDetail);
				}
			}

			/** 差分登録処理(削除) */
			List<NodePackageInfo> entityList = QueryUtil.getNodePackageInfoByFacilityId(facilityId);
			for (NodePackageInfo entity : entityList) {
				if (!notDelPkList.contains(entity.getId())) {
					// cc_cfg_node_pacakge_info削除処理
					em.remove(entity);
					// NodePackageHistoryDetail更新処理
					NodePackageHistoryDetail oldHistoryDetail 
						= QueryUtil.getNodePackageHistoryDetailByRegDateTo(
								entity.getId(), NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
					if (oldHistoryDetail != null) {
						oldHistoryDetail.setRegDateTo(registerDatetime);
					}

					// 変更情報
					diffInfo.addDelObj(entity.clone());
				}
			}
		}

		if (diffInfo.getAddObj().size() == 0
				&& diffInfo.getModObj().size() == 0
				&& diffInfo.getDelObj().size() == 0) {
			diffInfo = null;
		}

		long end = HinemosTime.currentTimeMillis() - start;
		m_log.debug("registerNodePackageInfo : end (" + end + "ms)");

		return diffInfo;
	}

	/**
	 * 個別導入製品情報の登録処理を行う。
	 * 
	 * @param registerDatetime 収集日時
	 * @param modifyUserId 更新ユーザID
	 * @param facilityId ファシリティID
	 * @param list 個別導入製品情報
	 * @param isCollect true:構成情報収集で実施, false:ノードプロパティより登録など
	 * @return 差分情報 (変更がない場合はnull)
	 */
	public static NodeConfigRegisterDiffInfo registerNodeProductInfo(
			Long registerDatetime, String modifyUserId, String facilityId, List<NodeProductInfo> list, boolean isCollect) {

		long start = HinemosTime.currentTimeMillis();
		m_log.debug("registerNodeProductInfo() : start");

		NodeConfigRegisterDiffInfo diffInfo = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			Long leastRegDate = QueryUtil.getNodeHistoryByMaxRegDate(NodeConfigSettingItem.PRODUCT, facilityId);
			if (leastRegDate != null && registerDatetime <= leastRegDate) {
				// 既に最新の情報が登録されている場合はエラー
				m_log.warn(String.format("registerNodeProductInfo() : The latest node information has already been registered. ("
						+ "item=%s, facilityId=%s, registerDatetime=%d",
						NodeConfigSettingItem.PRODUCT.name(), facilityId, registerDatetime));
				return diffInfo;
			}

			diffInfo = new NodeConfigRegisterDiffInfo();

			/** 差分登録処理(新規登録、変更) */
			List<NodeProductInfoPK> notDelPkList = new ArrayList<>();
			if (list != null) {
				for (NodeProductInfo info : list) {
					NodeProductInfo entity = null;
					NodeProductInfoPK entityPk = new NodeProductInfoPK(facilityId, info.getProductName());
					notDelPkList.add(entityPk);
					try {
						entity = QueryUtil.getNodeProductEntityPkForNodeConfigSetting(entityPk);

						// 差分確認
						if (entity.getVersion().equals(info.getVersion())
								&& entity.getPath().equals(info.getPath())) {
							// 差分がなければ何もしない
							continue;
						}

						// NodeProductHistoryDetail更新
						NodeProductHistoryDetail oldHistoryDetail 
							= QueryUtil.getNodeProductHistoryDetailByRegDateTo(entityPk, NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
						if (oldHistoryDetail != null) {
							oldHistoryDetail.setRegDateTo(registerDatetime);
						}
						// 変更情報
						diffInfo.addModObj(new NodeProductInfo[]{entity.clone(), info.clone()});
					} catch (FacilityNotFound e) {
						// 重複チェック
						jtm.checkEntityExists(NodeProductInfo.class, entityPk);
						// 新規登録
						entity = new NodeProductInfo(entityPk);
						entity.setRegDate(registerDatetime);
						entity.setRegUser(modifyUserId);
						em.persist(entity);
						// 変更情報
						diffInfo.addAddObj(info.clone());
					}

					// NodeProductInfo
					entity.setVersion(info.getVersion());
					entity.setPath(info.getPath());
					entity.setUpdateDate(registerDatetime);
					entity.setUpdateUser(modifyUserId);

					// NodeProductHistoryDetail登録
					NodeProductHistoryDetail historyDetail = new NodeProductHistoryDetail(
							facilityId, info.getProductName(), registerDatetime);
					historyDetail.setVersion(info.getVersion());
					historyDetail.setPath(info.getPath());
					historyDetail.setRegUser(modifyUserId);
					em.persist(historyDetail);
				}
			}

			/** 差分登録処理(削除) */
			List<NodeProductInfo> entityList = QueryUtil.getNodeProductInfoByFacilityId(facilityId);
			for (NodeProductInfo entity : entityList) {
				if (!notDelPkList.contains(entity.getId())) {
					// cc_cfg_node_product_info削除処理
					em.remove(entity);
					// NodeProductHistoryDetail更新処理
					NodeProductHistoryDetail oldHistoryDetail 
						= QueryUtil.getNodeProductHistoryDetailByRegDateTo(
								entity.getId(), NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
					if (oldHistoryDetail != null) {
						oldHistoryDetail.setRegDateTo(registerDatetime);
					}

					// 変更情報
					diffInfo.addDelObj(entity.clone());
				}
			}
		}

		if (diffInfo.getAddObj().size() == 0
				&& diffInfo.getModObj().size() == 0
				&& diffInfo.getDelObj().size() == 0) {
			diffInfo = null;
		}

		long end = HinemosTime.currentTimeMillis() - start;
		m_log.debug("registerNodeProductInfo : end (" + end + "ms)");

		return diffInfo;
	}

	/**
	 * ライセンス情報の登録処理を行う。
	 * 
	 * @param registerDatetime 収集日時
	 * @param modifyUserId 更新ユーザID
	 * @param facilityId ファシリティID
	 * @param list ライセンス情報
	 * @param isCollect true:構成情報収集で実施, false:ノードプロパティより登録など
	 * @return 差分情報 (変更がない場合はnull)
	 */
	public static NodeConfigRegisterDiffInfo registerNodeLicenseInfo(
			Long registerDatetime, String modifyUserId, String facilityId, List<NodeLicenseInfo> list, boolean isCollect) {

		long start = HinemosTime.currentTimeMillis();
		m_log.debug("registerNodeLicenseInfo() : start");

		NodeConfigRegisterDiffInfo diffInfo = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			Long leastRegDate = QueryUtil.getNodeHistoryByMaxRegDate(NodeConfigSettingItem.LICENSE, facilityId);
			if (leastRegDate != null && registerDatetime <= leastRegDate) {
				// 既に最新の情報が登録されている場合はエラー
				m_log.warn(String.format("registerNodeLicenseInfo() : The latest node information has already been registered. ("
						+ "item=%s, facilityId=%s, registerDatetime=%d",
						NodeConfigSettingItem.LICENSE.name(), facilityId, registerDatetime));
				return diffInfo;
			}

			diffInfo = new NodeConfigRegisterDiffInfo();

			/** 差分登録処理(新規登録、変更) */
			List<NodeLicenseInfoPK> notDelPkList = new ArrayList<>();
			if (list != null) {
				for (NodeLicenseInfo info : list) {
					NodeLicenseInfo entity = null;
					NodeLicenseInfoPK entityPk = new NodeLicenseInfoPK(facilityId, info.getProductName());
					notDelPkList.add(entityPk);
					try {
						entity = QueryUtil.getNodeLicenseEntityPkForNodeConfigSetting(entityPk);

						// 差分確認
						if (entity.getVendor().equals(info.getVendor())
								&& entity.getVendorContact().equals(info.getVendorContact())
								&& entity.getSerialNumber().equals(info.getSerialNumber())
								&& entity.getCount().equals(info.getCount())
								&& entity.getExpirationDate().equals(info.getExpirationDate())) {
							// 差分がなければ何もしない
							continue;
						}

						// NodeLicenseHistoryDetail更新
						NodeLicenseHistoryDetail oldHistoryDetail 
							= QueryUtil.getNodeLicenseHistoryDetailByRegDateTo(entityPk, NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
						if (oldHistoryDetail != null) {
							oldHistoryDetail.setRegDateTo(registerDatetime);
						}
						// 変更情報
						diffInfo.addModObj(new NodeLicenseInfo[]{entity.clone(), info.clone()});
					} catch (FacilityNotFound e) {
						// 重複チェック
						jtm.checkEntityExists(NodeLicenseInfo.class, entityPk);
						// 新規登録
						entity = new NodeLicenseInfo(entityPk);
						entity.setRegDate(registerDatetime);
						entity.setRegUser(modifyUserId);
						em.persist(entity);
						// 変更情報
						diffInfo.addAddObj(info.clone());
					}

					// NodeLicenseInfo
					entity.setVendor(info.getVendor());
					entity.setVendorContact(info.getVendorContact());
					entity.setSerialNumber(info.getSerialNumber());
					entity.setCount(info.getCount());
					entity.setExpirationDate(info.getExpirationDate());
					entity.setUpdateDate(registerDatetime);
					entity.setUpdateUser(modifyUserId);

					// NodeLicenseHistoryDetail登録
					NodeLicenseHistoryDetail historyDetail = new NodeLicenseHistoryDetail(
							facilityId, info.getProductName(), registerDatetime);
					historyDetail.setVendor(info.getVendor());
					historyDetail.setVendorContact(info.getVendorContact());
					historyDetail.setSerialNumber(info.getSerialNumber());
					historyDetail.setCount(info.getCount());
					historyDetail.setExpirationDate(info.getExpirationDate());
					historyDetail.setRegUser(modifyUserId);
					em.persist(historyDetail);
				}
			}

			/** 差分登録処理(削除) */
			List<NodeLicenseInfo> entityList = QueryUtil.getNodeLicenseInfoByFacilityId(facilityId);
			for (NodeLicenseInfo entity : entityList) {
				if (!notDelPkList.contains(entity.getId())) {
					// cc_cfg_node_license_info削除処理
					em.remove(entity);
					// NodeLicenseHistoryDetail更新処理
					NodeLicenseHistoryDetail oldHistoryDetail 
						= QueryUtil.getNodeLicenseHistoryDetailByRegDateTo(
								entity.getId(), NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
					if (oldHistoryDetail != null) {
						oldHistoryDetail.setRegDateTo(registerDatetime);
					}

					// 変更情報
					diffInfo.addDelObj(entity.clone());
				}
			}
		}

		if (diffInfo.getAddObj().size() == 0
				&& diffInfo.getModObj().size() == 0
				&& diffInfo.getDelObj().size() == 0) {
			diffInfo = null;
		}

		long end = HinemosTime.currentTimeMillis() - start;
		m_log.debug("registerNodeLicenseInfo : end (" + end + "ms)");

		return diffInfo;
	}

	/**
	 * ユーザ任意情報の登録処理を行う。
	 * 
	 * @param registerDatetime 収集日時
	 * @param modifyUserId 更新ユーザID
	 * @param facilityId ファシリティID
	 * @param list ユーザ任意情報
	 * @param isCollect true:構成情報収集で実施, false:ノードプロパティより登録など
	 * @return 差分情報 (変更がない場合はnull)
	 */
	public static NodeConfigRegisterDiffInfo registerNodeCustomInfo(
			Long registerDatetime, String modifyUserId, String facilityId, String parentSettingId, List<NodeCustomInfo> list, boolean isCollect) {

		long start = HinemosTime.currentTimeMillis();
		m_log.debug("registerNodeCustomInfo() : start");

		NodeConfigRegisterDiffInfo diffInfo = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			Long leastRegDate = QueryUtil.getNodeHistoryByMaxRegDate(NodeConfigSettingItem.CUSTOM, facilityId);
			if (leastRegDate != null && registerDatetime <= leastRegDate) {
				// 既に最新の情報が登録されている場合はエラー
				m_log.warn(String.format("registerNodeCustomInfo() : The latest node information has already been registered. ("
						+ "item=%s, facilityId=%s, registerDatetime=%d",
						NodeConfigSettingItem.CUSTOM.name(), facilityId, registerDatetime));
				return diffInfo;
			}

			diffInfo = new NodeConfigRegisterDiffInfo();

			/** 差分登録処理(新規登録、変更) */
			List<NodeCustomInfoPK> notDelPkList = new ArrayList<>();
			if (list != null) {
				for (NodeCustomInfo info : list) {
					NodeCustomInfo entity = null;
					NodeCustomInfoPK entityPk = new NodeCustomInfoPK(facilityId, parentSettingId, info.getSettingCustomId());
					notDelPkList.add(entityPk);
					try {
						entity = QueryUtil.getNodeCustomEntityPkForNodeConfigSetting(entityPk);
						
						//収集対象外の場合は、DBから対象を削除し、以降の処理をスキップ
						if (info.getRegisterFlag().equals(NodeRegisterFlagConstant.NOT_GET)){
							m_log.debug("Invalid flag detected. Remove entity from DB");
							notDelPkList.remove(entityPk);
							continue;
						}
						
						// 差分確認
						if (entity.getDisplayName().equals(info.getDisplayName())
								&& entity.getCommand().equals(info.getCommand())
								&& entity.getValue().equals(info.getValue())) {
							// 差分がなければ何もしない
							continue;
						}

						// HistoryDetail更新
						NodeCustomHistoryDetail oldHistoryDetail 
							= QueryUtil.getNodeCustomHistoryDetailByRegDateTo(entityPk, NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
						if (oldHistoryDetail != null) {
							oldHistoryDetail.setRegDateTo(registerDatetime);
						}
						// 変更情報
						diffInfo.addModObj(new NodeCustomInfo[]{entity.clone(), info.clone()});
					} catch (FacilityNotFound e) {
						// 重複チェック
						jtm.checkEntityExists(NodeCustomInfo.class, entityPk);
						//無効の場合は登録しない
						if (info.getRegisterFlag().equals(NodeRegisterFlagConstant.NOT_GET)){
							m_log.debug("Invalid flag detected. Do not add to DB");
							continue;
						}
						// 新規登録
						entity = new NodeCustomInfo(entityPk);
						entity.setRegDate(registerDatetime);
						entity.setRegUser(modifyUserId);
						em.persist(entity);
						// 変更情報
						diffInfo.addAddObj(info.clone());
					}

					// NodeCustomInfo
					entity.setDisplayName(info.getDisplayName());
					entity.setCommand(info.getCommand());
					entity.setValue(info.getValue());
					entity.setUpdateDate(registerDatetime);
					entity.setUpdateUser(modifyUserId);

					// NodePackageHistoryDetail登録
					NodeCustomHistoryDetail historyDetail = new NodeCustomHistoryDetail(
							facilityId, registerDatetime, parentSettingId, info.getSettingCustomId());
					historyDetail.setDisplayName(info.getDisplayName());
					historyDetail.setCommand(info.getCommand());
					historyDetail.setValue(info.getValue());
					historyDetail.setRegUser(modifyUserId);
					em.persist(historyDetail);
				}
			}

			/** 差分登録処理(削除) */
			List<NodeCustomInfo> entityList = QueryUtil.getNodeCustomByFacilityId(facilityId);
			for (NodeCustomInfo entity : entityList) {
				if (!notDelPkList.contains(entity.getId())) {
					// 削除処理
					em.remove(entity);
					// HistoryDetail更新処理
					NodeCustomHistoryDetail oldHistoryDetail 
						= QueryUtil.getNodeCustomHistoryDetailByRegDateTo(
								entity.getId(), NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE);
					if (oldHistoryDetail != null) {
						oldHistoryDetail.setRegDateTo(registerDatetime);
					}

					// 変更情報
					diffInfo.addDelObj(entity.clone());
				}
			}
		}

		if (diffInfo.getAddObj().size() == 0
				&& diffInfo.getModObj().size() == 0
				&& diffInfo.getDelObj().size() == 0) {
			diffInfo = null;
		}

		long end = HinemosTime.currentTimeMillis() - start;
		m_log.debug("registerNodeCustomInfo : end (" + end + "ms)");

		return diffInfo;
	}

	/**
	 * 構成情報の削除、および構成情報履歴詳細の削除情報の更新を行う。
	 * 
	 * @param registerDatetime 収集日時
	 * @param modifyUserId 更新ユーザID
	 * @param facilityId ファシリティID
	 */
	public static void deleteNodeHistoryDetailInfo(Long registerDatetime, String modifyUserId, String facilityId) {

		long start = HinemosTime.currentTimeMillis();
		long end = 0L;

		m_log.debug("deleteNodeHistoryDetailInfo() : start");

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {

			HinemosEntityManager em = jtm.getEntityManager();

			// OS情報
			QueryUtil.modifyNodeHistoryDetailDeleteByFacilityId(
					NodeOsHistoryDetail.class, registerDatetime, modifyUserId, facilityId);
			QueryUtil.deleteNodeOptionalInfoByFacilityId(NodeOsInfo.class, facilityId);
			if (m_log.isDebugEnabled()) {
				end = HinemosTime.currentTimeMillis();
				m_log.debug("deleteNodeHistoryDetailInfo() : os end (" + (end - start) + "ms)");
				start = end;
			}

			// CPU情報
			QueryUtil.modifyNodeHistoryDetailDeleteByFacilityId(
					NodeCpuHistoryDetail.class, registerDatetime, modifyUserId, facilityId);
			QueryUtil.deleteNodeOptionalInfoByFacilityId(NodeCpuInfo.class, facilityId);
			if (m_log.isDebugEnabled()) {
				end = HinemosTime.currentTimeMillis();
				m_log.debug("deleteNodeHistoryDetailInfo() : cpu end (" + (end - start) + "ms)");
				start = end;
			}

			// メモリ情報
			QueryUtil.modifyNodeHistoryDetailDeleteByFacilityId(
					NodeMemoryHistoryDetail.class, registerDatetime, modifyUserId, facilityId);
			QueryUtil.deleteNodeOptionalInfoByFacilityId(NodeMemoryInfo.class, facilityId);
			if (m_log.isDebugEnabled()) {
				end = HinemosTime.currentTimeMillis();
				m_log.debug("deleteNodeHistoryDetailInfo() : memory end (" + (end - start) + "ms)");
				start = end;
			}

			// NIC情報
			QueryUtil.modifyNodeHistoryDetailDeleteByFacilityId(
					NodeNetworkInterfaceHistoryDetail.class, registerDatetime, modifyUserId, facilityId);
			QueryUtil.deleteNodeOptionalInfoByFacilityId(NodeNetworkInterfaceInfo.class, facilityId);
			if (m_log.isDebugEnabled()) {
				end = HinemosTime.currentTimeMillis();
				m_log.debug("deleteNodeHistoryDetailInfo() : nic end (" + (end - start) + "ms)");
				start = end;
			}

			// ディスク情報
			QueryUtil.modifyNodeHistoryDetailDeleteByFacilityId(
					NodeDiskHistoryDetail.class, registerDatetime, modifyUserId, facilityId);
			QueryUtil.deleteNodeOptionalInfoByFacilityId(NodeDiskInfo.class, facilityId);
			if (m_log.isDebugEnabled()) {
				end = HinemosTime.currentTimeMillis();
				m_log.debug("deleteNodeHistoryDetailInfo() : disk end (" + (end - start) + "ms)");
				start = end;
			}

			// ファイルシステム情報
			QueryUtil.modifyNodeHistoryDetailDeleteByFacilityId(
					NodeFilesystemHistoryDetail.class, registerDatetime, modifyUserId, facilityId);
			QueryUtil.deleteNodeOptionalInfoByFacilityId(NodeFilesystemInfo.class, facilityId);
			if (m_log.isDebugEnabled()) {
				end = HinemosTime.currentTimeMillis();
				m_log.debug("deleteNodeHistoryDetailInfo() : filesystem end (" + (end - start) + "ms)");
				start = end;
			}

			// ノード変数情報
			QueryUtil.modifyNodeHistoryDetailDeleteByFacilityId(
					NodeVariableHistoryDetail.class, registerDatetime, modifyUserId, facilityId);
			QueryUtil.deleteNodeOptionalInfoByFacilityId(NodeVariableInfo.class, facilityId);
			if (m_log.isDebugEnabled()) {
				end = HinemosTime.currentTimeMillis();
				m_log.debug("deleteNodeHistoryDetailInfo() : variable end (" + (end - start) + "ms)");
				start = end;
			}

			// ホスト名情報
			QueryUtil.modifyNodeHistoryDetailDeleteByFacilityId(
					NodeHostnameHistoryDetail.class, registerDatetime, modifyUserId, facilityId);
			QueryUtil.deleteNodeOptionalInfoByFacilityId(NodeHostnameInfo.class, facilityId);
			if (m_log.isDebugEnabled()) {
				end = HinemosTime.currentTimeMillis();
				m_log.debug("deleteNodeHistoryDetailInfo() : hostname end (" + (end - start) + "ms)");
				start = end;
			}

			// ネットワーク接続情報
			QueryUtil.modifyNodeHistoryDetailDeleteByFacilityId(
					NodeNetstatHistoryDetail.class, registerDatetime, modifyUserId, facilityId);
			QueryUtil.deleteNodeOptionalInfoByFacilityId(NodeNetstatInfo.class, facilityId);
			if (m_log.isDebugEnabled()) {
				end = HinemosTime.currentTimeMillis();
				m_log.debug("deleteNodeHistoryDetailInfo() : netstat end (" + (end - start) + "ms)");
				start = end;
			}

			// プロセス情報
			QueryUtil.deleteNodeOptionalInfoByFacilityId(NodeProcessInfo.class, facilityId);
			if (m_log.isDebugEnabled()) {
				end = HinemosTime.currentTimeMillis();
				m_log.debug("deleteNodeHistoryDetailInfo() : process end (" + (end - start) + "ms)");
				start = end;
			}

			// パッケージ情報
			QueryUtil.modifyNodeHistoryDetailDeleteByFacilityId(
					NodePackageHistoryDetail.class, registerDatetime, modifyUserId, facilityId);
			QueryUtil.deleteNodeOptionalInfoByFacilityId(NodePackageInfo.class, facilityId);
			if (m_log.isDebugEnabled()) {
				end = HinemosTime.currentTimeMillis();
				m_log.debug("deleteNodeHistoryDetailInfo() : package end (" + (end - start) + "ms)");
				start = end;
			}

			// 個別導入製品情報
			QueryUtil.modifyNodeHistoryDetailDeleteByFacilityId(
					NodeProductHistoryDetail.class, registerDatetime, modifyUserId, facilityId);
			QueryUtil.deleteNodeOptionalInfoByFacilityId(NodeProductInfo.class, facilityId);
			if (m_log.isDebugEnabled()) {
				end = HinemosTime.currentTimeMillis();
				m_log.debug("deleteNodeHistoryDetailInfo() : product end (" + (end - start) + "ms)");
				start = end;
			}

			// ライセンス情報
			QueryUtil.modifyNodeHistoryDetailDeleteByFacilityId(
					NodeLicenseHistoryDetail.class, registerDatetime, modifyUserId, facilityId);
			QueryUtil.deleteNodeOptionalInfoByFacilityId(NodeLicenseInfo.class, facilityId);
			if (m_log.isDebugEnabled()) {
				end = HinemosTime.currentTimeMillis();
				m_log.debug("deleteNodeHistoryDetailInfo() : license end (" + (end - start) + "ms)");
				start = end;
			}

			// ユーザ任意情報
			QueryUtil.modifyNodeHistoryDetailDeleteByFacilityId(
					NodeCustomHistoryDetail.class, registerDatetime, modifyUserId, facilityId);
			QueryUtil.deleteNodeOptionalInfoByFacilityId(NodeCustomInfo.class, facilityId);
			if (m_log.isDebugEnabled()) {
				end = HinemosTime.currentTimeMillis();
				m_log.debug("deleteNodeHistoryDetailInfo() : custom end (" + (end - start) + "ms)");
				start = end;
			}

			em.flush();
		} catch (RuntimeException e) {
			m_log.warn("deleteNodeHistoryDetailInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}
		m_log.debug("deleteNodeHistoryDetailInfo() : end");
	}

	public static class NodeConfigRegisterDiffInfo {
		// 新規登録情報
		private List<Object> addObj = new ArrayList<>();
		// 更新情報
		private List<Object[]> modObj = new ArrayList<>();
		// 削除情報
		private List<Object> delObj = new ArrayList<>();
		public List<Object> getAddObj() {
			return addObj;
		}
		public void addAddObj(Object obj) {
			this.addObj.add(obj);
		}
		public List<Object[]> getModObj() {
			return modObj;
		}
		public void addModObj(Object[] objs) {
			this.modObj.add(objs);
		}
		public List<Object> getDelObj() {
			return delObj;
		}
		public void addDelObj(Object obj) {
			this.delObj.add(obj);
		}
	}

	/**
	 * チェック対象文字列の末尾に@IGNORE_MONITORがある場合はTRUEを返す。
	 * @param str
	 * @return IGNORE_MONITORの有無
	 */
	public static boolean chkIgnoreMonitorCode(String str) {
		boolean chk = false;
		if (str.trim().endsWith(IGNORE_MONITOR)) {
			chk = true;
		}
		
		return chk;
	}

	/**
	 * 末尾の@IGNORE_MONITORの有無を考慮して Descriptonの 差分をチェックする
	 * 
	 * @param isCollect 構成情報収集であるかどうか
	 * @param oldDesc 旧Descripton
	 * @param newDesc 新Descripton
	 * @return 差分の有無
	 */
	private static boolean checkDescriptonDiff(boolean isCollect ,String oldDesc, String newDesc) {
		boolean chk = false;
		if ( isCollect ) {
			//構成情報収集による呼び出しの場合は 末尾の@IGNORE_MONITORは 差分判定に含めない
			chk = rmIgnoreMonitorCode(oldDesc).equals(newDesc);
		} else {
			//構成情報収集以外による呼び出しの場合は通常の比較を行う。
			chk = oldDesc.equals(newDesc);
		}
		return chk;
	}


	/**
	 * 末尾の@IGNORE_MONITORを取り除く
	 * @param str
	 * @return
	 */
	private static String rmIgnoreMonitorCode(String str) {
		if (str.trim().endsWith(IGNORE_MONITOR)) {
			str = str.substring(0, str.length() - IGNORE_MONITOR.length());
		}
		return str;
	}
	
	/**
	 * 末尾に@IGNORE_MONITORを追記する
	 * @param str
	 * @return
	 */
	private static String addIgnoreMonitorCode( String str ) {
		return str + IGNORE_MONITOR;
	}
}