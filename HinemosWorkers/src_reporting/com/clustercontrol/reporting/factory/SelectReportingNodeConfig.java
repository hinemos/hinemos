/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.factory;

import java.util.List;

import com.clustercontrol.repository.model.NodeCpuHistoryDetail;
import com.clustercontrol.repository.model.NodeCustomHistoryDetail;
import com.clustercontrol.repository.model.NodeDiskHistoryDetail;
import com.clustercontrol.repository.model.NodeFilesystemHistoryDetail;
import com.clustercontrol.repository.model.NodeMemoryHistoryDetail;
import com.clustercontrol.repository.model.NodeNetworkInterfaceHistoryDetail;
import com.clustercontrol.repository.model.NodeOsHistoryDetail;
import com.clustercontrol.repository.model.NodePackageHistoryDetail;

/**
 * 構成情報を取得するクラス<BR>
 * <p>
 *
 * @version 6.2.b
 * @since 6.2.b
 */
public class SelectReportingNodeConfig {

	/**
	 * NodeOsHistoryDetailの一覧を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param maxLimit
	 * @return
	 */
	public List<NodeOsHistoryDetail> getNodeOsHistoryDetail(String facilityId, long fromTime, long toTime,
			int maxLimit) {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getNodeHistoryDetailsByRegDateAndLimit(
				NodeOsHistoryDetail.class, facilityId, fromTime, toTime, maxLimit);
	}

	/**
	 * NodeCpuHistoryDetailの一覧を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param maxLimit
	 * @return
	 */
	public List<NodeCpuHistoryDetail> getNodeCpuHistoryDetail(String facilityId, long fromTime, long toTime,
			int maxLimit) {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getNodeHistoryDetailsByRegDateAndLimit(
				NodeCpuHistoryDetail.class, facilityId, fromTime, toTime, maxLimit);
	}

	/**
	 * NodeMemoryHistoryDetailの一覧を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param maxLimit
	 * @return
	 */
	public List<NodeMemoryHistoryDetail> getNodeMemoryHistoryDetail(String facilityId, long fromTime, long toTime,
			int maxLimit) {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getNodeHistoryDetailsByRegDateAndLimit(
				NodeMemoryHistoryDetail.class, facilityId, fromTime, toTime, maxLimit);
	}

	/**
	 * NodeNetworkInterfaceHistoryDetailの一覧を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param maxLimit
	 * @return
	 */
	public List<NodeNetworkInterfaceHistoryDetail> getNodeNetworkInterfaceHistoryDetail(String facilityId,
			long fromTime, long toTime, int maxLimit) {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getNodeHistoryDetailsByRegDateAndLimit(
				NodeNetworkInterfaceHistoryDetail.class, facilityId, fromTime, toTime, maxLimit);
	}

	/**
	 * NodeDiskHistoryDetailの一覧を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param maxLimit
	 * @return
	 */
	public List<NodeDiskHistoryDetail> getNodeDiskHistoryDetail(String facilityId, long fromTime, long toTime,
			int maxLimit) {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getNodeHistoryDetailsByRegDateAndLimit(
				NodeDiskHistoryDetail.class, facilityId, fromTime, toTime, maxLimit);
	}

	/**
	 * NodeFilesystemHistoryDetailの一覧を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param maxLimit
	 * @return
	 */
	public List<NodeFilesystemHistoryDetail> getNodeFilesystemHistoryDetail(String facilityId, long fromTime,
			long toTime, int maxLimit) {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getNodeHistoryDetailsByRegDateAndLimit(
				NodeFilesystemHistoryDetail.class, facilityId, fromTime, toTime, maxLimit);
	}

	/**
	 * NodePackageHistoryDetailの一覧を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param maxLimit
	 * @return
	 */
	public List<NodePackageHistoryDetail> getNodePackageHistoryDetail(String facilityId, long fromTime, long toTime,
			int maxLimit) {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getNodeHistoryDetailsByRegDateAndLimit(
				NodePackageHistoryDetail.class, facilityId, fromTime, toTime, maxLimit);
	}

	/**
	 * NodeCustomHistoryDetailの一覧を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param maxLimit
	 * @return
	 */
	public List<NodeCustomHistoryDetail> getNodeCustomHistoryDetail(String facilityId, long fromTime, long toTime,
			int maxLimit) {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getNodeHistoryDetailsByRegDateAndLimit(
				NodeCustomHistoryDetail.class, facilityId, fromTime, toTime, maxLimit);
	}

	/**
	 * OS構成情報履歴内のデータの存在を返却します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param deviceName
	 * @return
	 */
	public boolean hasOldOSNodeConfigHistory(String facilityId, long fromTime) {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.hasOldNodeConfigHistory(NodeOsHistoryDetail.class,
				facilityId, fromTime, null, null, null, null);
	}

	/**
	 * CPU構成情報履歴内のデータの存在を返却します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param deviceName
	 * @return
	 */
	public boolean hasOldCPUNodeConfigHistory(String facilityId, long fromTime, String deviceName) {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.hasOldNodeConfigHistory(NodeCpuHistoryDetail.class,
				facilityId, fromTime, deviceName, null, null, null);
	}

	/**
	 * メモリ構成情報履歴内のデータの存在を返却します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param deviceName
	 * @return
	 */
	public boolean hasOldMemoryNodeConfigHistory(String facilityId, long fromTime, String deviceName) {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.hasOldNodeConfigHistory(
				NodeMemoryHistoryDetail.class, facilityId, fromTime, deviceName, null, null, null);
	}

	/**
	 * NIC構成情報履歴内のデータの存在を返却します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param deviceName
	 * @return
	 */
	public boolean hasOldNICNodeConfigHistory(String facilityId, long fromTime, String deviceName) {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.hasOldNodeConfigHistory(
				NodeNetworkInterfaceHistoryDetail.class, facilityId, fromTime, deviceName, null, null, null);
	}

	/**
	 * ディスク構成情報履歴内のデータの存在を返却します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param deviceName
	 * @return
	 */
	public boolean hasOldDiskNodeConfigHistory(String facilityId, long fromTime, String deviceName) {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.hasOldNodeConfigHistory(NodeDiskHistoryDetail.class,
				facilityId, fromTime, deviceName, null, null, null);
	}

	/**
	 * ファイルシステム構成情報履歴内のデータの存在を返却します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param deviceName
	 * @return
	 */
	public boolean hasOldFileSystemNodeConfigHistory(String facilityId, long fromTime, String deviceName) {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.hasOldNodeConfigHistory(
				NodeFilesystemHistoryDetail.class, facilityId, fromTime, deviceName, null, null, null);
	}

	/**
	 * パッケージ履歴内のデータの存在を返却します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param packageId
	 * @return
	 */
	public boolean hasOldPackageNodeConfigHistory(String facilityId, long fromTime, String packageId) {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.hasOldNodeConfigHistory(
				NodePackageHistoryDetail.class, facilityId, fromTime, null, packageId, null, null);
	}

	/**
	 * ユーザ任意情報履歴内のデータの存在を返却します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param settingId
	 * @param settingCustomId
	 * @return
	 */
	public boolean hasOldCustomNodeConfigHistory(String facilityId, long fromTime, String settingId,
			String settingCustomId) {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.hasOldNodeConfigHistory(
				NodeCustomHistoryDetail.class, facilityId, fromTime, null, null, settingId, settingCustomId);
	}

}