/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.session;

import java.util.List;

import com.clustercontrol.reporting.factory.SelectReportingNodeConfig;
import com.clustercontrol.repository.model.NodeCpuHistoryDetail;
import com.clustercontrol.repository.model.NodeCustomHistoryDetail;
import com.clustercontrol.repository.model.NodeDiskHistoryDetail;
import com.clustercontrol.repository.model.NodeFilesystemHistoryDetail;
import com.clustercontrol.repository.model.NodeMemoryHistoryDetail;
import com.clustercontrol.repository.model.NodeNetworkInterfaceHistoryDetail;
import com.clustercontrol.repository.model.NodeOsHistoryDetail;
import com.clustercontrol.repository.model.NodePackageHistoryDetail;

/**
 *
 * <!-- begin-user-doc --> 構成情報の制御を行うsession bean <!-- end-user-doc --> *
 *
 */
public class ReportingNodeConfigControllerBean {

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
		SelectReportingNodeConfig select = new SelectReportingNodeConfig();
		List<NodeOsHistoryDetail> osHistory = select.getNodeOsHistoryDetail(facilityId, fromTime, toTime, maxLimit);
		return osHistory;
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
		SelectReportingNodeConfig select = new SelectReportingNodeConfig();
		List<NodeCpuHistoryDetail> cpuHistory = select.getNodeCpuHistoryDetail(facilityId, fromTime, toTime, maxLimit);
		return cpuHistory;
	}

	/**
	 * NodeMemoryHistoryDetailの一覧を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param maxLimit
	 * 
	 * @return
	 */
	public List<NodeMemoryHistoryDetail> getNodeMemoryHistoryDetail(String facilityId, long fromTime, long toTime,
			int maxLimit) {
		SelectReportingNodeConfig select = new SelectReportingNodeConfig();
		List<NodeMemoryHistoryDetail> memoryHistory = select.getNodeMemoryHistoryDetail(facilityId, fromTime, toTime,
				maxLimit);
		return memoryHistory;
	}

	/**
	 * NodeNetworkInterfaceHistoryDetailの一覧を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param maxLimit
	 * 
	 * @return
	 */
	public List<NodeNetworkInterfaceHistoryDetail> getNodeNetworkInterfaceHistoryDetail(String facilityId,
			long fromTime, long toTime, int maxLimit) {
		SelectReportingNodeConfig select = new SelectReportingNodeConfig();
		List<NodeNetworkInterfaceHistoryDetail> networkInterfaceHistory = select
				.getNodeNetworkInterfaceHistoryDetail(facilityId, fromTime, toTime, maxLimit);
		return networkInterfaceHistory;
	}

	/**
	 * NodeDiskHistoryDetailの一覧を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param maxLimit
	 * 
	 * @return
	 */
	public List<NodeDiskHistoryDetail> getNodeDiskHistoryDetail(String facilityId, long fromTime, long toTime,
			int maxLimit) {
		SelectReportingNodeConfig select = new SelectReportingNodeConfig();
		List<NodeDiskHistoryDetail> diskHistory = select.getNodeDiskHistoryDetail(facilityId, fromTime, toTime,
				maxLimit);
		return diskHistory;
	}

	/**
	 * NodeFilesystemHistoryDetailの一覧を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param maxLimit
	 * 
	 * @return
	 */
	public List<NodeFilesystemHistoryDetail> getNodeFilesystemHistoryDetail(String facilityId, long fromTime,
			long toTime, int maxLimit) {
		SelectReportingNodeConfig select = new SelectReportingNodeConfig();
		List<NodeFilesystemHistoryDetail> filesystemHistory = select.getNodeFilesystemHistoryDetail(facilityId,
				fromTime, toTime, maxLimit);
		return filesystemHistory;
	}

	/**
	 * NodePackageHistoryDetailの一覧を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param maxLimit
	 * 
	 * @return
	 */
	public List<NodePackageHistoryDetail> getNodePackageHistoryDetail(String facilityId, long fromTime, long toTime,
			int maxLimit) {
		SelectReportingNodeConfig select = new SelectReportingNodeConfig();
		List<NodePackageHistoryDetail> packageHistory = select.getNodePackageHistoryDetail(facilityId, fromTime, toTime,
				maxLimit);
		return packageHistory;
	}

	/**
	 * NodeCustomHistoryDetailの一覧を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param maxLimit
	 * 
	 * @return
	 */
	public List<NodeCustomHistoryDetail> getNodeCustomHistoryDetail(String facilityId, long fromTime, long toTime,
			int maxLimit) {
		SelectReportingNodeConfig select = new SelectReportingNodeConfig();
		List<NodeCustomHistoryDetail> customHistory = select.getNodeCustomHistoryDetail(facilityId, fromTime, toTime,
				maxLimit);
		return customHistory;
	}

	/**
	 * OS構成情報履歴内のデータの存在を返却します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @return
	 */
	public boolean hasOldOSNodeConfigHistory(String facilityId, long fromTime) {
		SelectReportingNodeConfig select = new SelectReportingNodeConfig();
		return select.hasOldOSNodeConfigHistory(facilityId, fromTime);
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
		SelectReportingNodeConfig select = new SelectReportingNodeConfig();
		return select.hasOldCPUNodeConfigHistory(facilityId, fromTime, deviceName);
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
		SelectReportingNodeConfig select = new SelectReportingNodeConfig();
		return select.hasOldMemoryNodeConfigHistory(facilityId, fromTime, deviceName);
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
		SelectReportingNodeConfig select = new SelectReportingNodeConfig();
		return select.hasOldNICNodeConfigHistory(facilityId, fromTime, deviceName);
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
		SelectReportingNodeConfig select = new SelectReportingNodeConfig();
		return select.hasOldDiskNodeConfigHistory(facilityId, fromTime, deviceName);
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
		SelectReportingNodeConfig select = new SelectReportingNodeConfig();
		return select.hasOldFileSystemNodeConfigHistory(facilityId, fromTime, deviceName);
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
		SelectReportingNodeConfig select = new SelectReportingNodeConfig();
		return select.hasOldPackageNodeConfigHistory(facilityId, fromTime, packageId);
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
		SelectReportingNodeConfig select = new SelectReportingNodeConfig();
		return select.hasOldCustomNodeConfigHistory(facilityId, fromTime, settingId, settingCustomId);
	}

}
