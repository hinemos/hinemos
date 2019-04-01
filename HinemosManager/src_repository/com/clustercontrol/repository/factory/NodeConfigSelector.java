/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.factory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.repository.bean.LatestNodeConfigWrapper;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.NodeNetworkInterfaceInfo;
import com.clustercontrol.repository.model.NodePackageHistoryDetail;
import com.clustercontrol.repository.model.NodePackageInfo;
import com.clustercontrol.repository.model.NodeProcessInfo;
import com.clustercontrol.repository.util.QueryUtil;

/**
 * 構成情報取得用クラス.
 *
 * @version 6.2.0
 * @since 6.2.0
 */
public class NodeConfigSelector {

	// ログ出力用.
	/** ロガー */
	private static Log m_log = LogFactory.getLog(NodeConfigSelector.class);
	/** ログ出力区切り文字 */
	private static final String DELIMITER = "() : ";

	/**
	 * 構成情報のパッケージ情報を取得する.
	 * 
	 * @param facilityId
	 *            ファシリティID
	 * @param date
	 *            指定した日付より過去の最新情報を取得する
	 * 
	 * @return 指定日付より過去の最新パッケージ情報
	 * @throws FacilityNotFound
	 */
	public static LatestNodeConfigWrapper<NodePackageInfo> getNodePackageList(String facilityId, Long date)
			throws FacilityNotFound {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start");

		// 引数のFacilityIDが存在するかチェック.
		try {
			QueryUtil.getNodePK(facilityId);
		} catch (FacilityNotFound e) {
			throw e;
		}

		LatestNodeConfigWrapper<NodePackageInfo> latestPackage = new LatestNodeConfigWrapper<>();

		// 最終収集日時を取得.
		Long latestHistoryDate = QueryUtil.getLatestNodePackageHistoryDate(facilityId, date);
		if (latestHistoryDate == null) {
			latestPackage.setCollected(null);
			m_log.debug(methodName + DELIMITER + "get last collected date. date=null");
		} else {
			latestPackage.setCollected(latestHistoryDate);
			if (m_log.isDebugEnabled()) {
				Date collectedDate = new Date(latestHistoryDate);
				m_log.debug(methodName + DELIMITER
						+ String.format("get last collected date. date=[%s]", collectedDate.toString()));
			}
		}

		// 最終更新日時
		long lastUpdated = 0L;

		// パッケージ情報を取得する
		List<NodePackageInfo> latestPackageList = new ArrayList<>();
		if (date == null) {
			latestPackageList = QueryUtil.getNodePackageInfoByFacilityId(facilityId);
			if (latestPackageList != null && latestPackageList.size() > 0) {
				for (NodePackageInfo packageInfo : latestPackageList) {
					if (packageInfo.getUpdateDate() != null && packageInfo.getUpdateDate() > lastUpdated) {
						lastUpdated = packageInfo.getUpdateDate();
					}
				}
			}
			
			latestPackage.setConfigList(latestPackageList);
			if (m_log.isDebugEnabled()) {
				int count = 0;
				if (latestPackageList != null) {
					count = latestPackageList.size();
				}
				m_log.debug(methodName + DELIMITER + String.format("get package list. count=[%d]", count));
			}
		} else {
			List<NodePackageHistoryDetail> packageHistoryList 
				= QueryUtil.getNodeHistoryDetailByFacilityIdRegDate(NodePackageHistoryDetail.class, facilityId, date);
			if (packageHistoryList != null && packageHistoryList.size() > 0) {
				for (NodePackageHistoryDetail packageHistoryInfo : packageHistoryList) {
					NodePackageInfo packageInfo 
						= new NodePackageInfo(packageHistoryInfo.getFacilityId(), packageHistoryInfo.getPackageId());
					packageInfo.setPackageName(packageHistoryInfo.getPackageName());
					packageInfo.setVersion(packageHistoryInfo.getVersion());
					packageInfo.setRelease(packageHistoryInfo.getRelease());
					packageInfo.setInstallDate(packageHistoryInfo.getInstallDate());
					packageInfo.setVendor(packageHistoryInfo.getVendor());
					packageInfo.setArchitecture(packageHistoryInfo.getArchitecture());
					packageInfo.setUpdateDate(packageHistoryInfo.getRegDate());
					packageInfo.setUpdateUser(packageHistoryInfo.getRegUser());
					if (packageInfo.getUpdateDate() != null && packageInfo.getUpdateDate() > lastUpdated) {
						lastUpdated = packageInfo.getUpdateDate();
					}
					latestPackageList.add(packageInfo);
				}
			}
			latestPackage.setConfigList(latestPackageList);
			if (m_log.isDebugEnabled()) {
				int count = 0;
				if (latestPackageList != null) {
					count = latestPackageList.size();
				}
				m_log.debug(methodName + DELIMITER + String.format("get package list. count=[%d]", count));
			}
		}

		// 取得したパッケージ情報の内、最終更新に該当する日付を返却値としてセット.
		if (latestPackageList == null || latestPackageList.isEmpty()) {
			latestPackage.setLastUpdated(null);
		} else {
			latestPackage.setLastUpdated(lastUpdated);
			if (m_log.isDebugEnabled()) {
				Date lastUpdatedDate = new Date(lastUpdated);
				m_log.debug(methodName + DELIMITER
						+ String.format("get last updated date. date=[%s]", lastUpdatedDate.toString()));
			}
		}
		return latestPackage;
	}

	/**
	 * 構成情報のプロセス情報を取得する.
	 * 
	 * @param facilityId
	 *            ファシリティID
	 * @return 最新プロセス情報
	 * @throws FacilityNotFound
	 */
	public static List<NodeProcessInfo> getNodeProcessList(String facilityId) throws FacilityNotFound {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start");

		// 引数のFacilityIDが存在するかチェック.
		try {
			QueryUtil.getNodePK(facilityId);
		} catch (FacilityNotFound e) {
			throw e;
		}

		// プロセスの最新情報を取得する.
		List<NodeProcessInfo> latestProcesses = QueryUtil.getLatestNodeProcesses(facilityId);
		if (m_log.isDebugEnabled()) {
			int count = 0;
			if (latestProcesses != null) {
				count = latestProcesses.size();
			}
			m_log.debug(methodName + DELIMITER + String.format("get process list. count=[%d]", count));
		}

		return latestProcesses;
	}

	/**
	 * 構成情報のNIC情報を含むノード情報を返却.
	 * 
	 * @return 構成情報のNIC情報を含むノード情報
	 */
	public static List<NodeInfo> getNodeNicList() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		m_log.debug(methodName + DELIMITER + "start");

		// ノード全量を取得する.
		List<NodeInfo> allNodeList = QueryUtil.getAllNode();
		if (m_log.isDebugEnabled()) {
			int count = 0;
			if (allNodeList != null) {
				count = allNodeList.size();
			}
			m_log.debug(methodName + DELIMITER + String.format("get all nodes. count=[%d]", count));
		}

		// 0件の場合はそのまま返却.
		if (allNodeList == null || allNodeList.isEmpty()) {
			return allNodeList;
		}

		// ノード毎にNICを取得する.
		List<NodeInfo> nodeWithNicList = new ArrayList<NodeInfo>();
		for (NodeInfo node : allNodeList) {
			String facilityId = node.getFacilityId();

			// ありえない想定だけど次.
			if (facilityId == null || facilityId.isEmpty()) {
				nodeWithNicList.add(node);
				continue;
			}

			// NIC取得.
			List<NodeNetworkInterfaceInfo> nicList = QueryUtil.getNodeNetworkInterfaceInfoByFacilityId(facilityId);
			if (m_log.isDebugEnabled()) {
				int count = 0;
				if (nicList != null) {
					count = nicList.size();
				}
				m_log.debug(methodName + DELIMITER
						+ String.format("get NIC on node. facilityID=[%s], count=[%d]", facilityId, count));
			}
			node.setNodeNetworkInterfaceInfo(nicList);
			nodeWithNicList.add(node);

		}

		return nodeWithNicList;
	}

}
