/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.factory;


import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.repository.model.NodeCpuHistoryDetail;
import com.clustercontrol.repository.model.NodeCustomHistoryDetail;
import com.clustercontrol.repository.model.NodeDiskHistoryDetail;
import com.clustercontrol.repository.model.NodeFilesystemHistoryDetail;
import com.clustercontrol.repository.model.NodeHostnameHistoryDetail;
import com.clustercontrol.repository.model.NodeLicenseHistoryDetail;
import com.clustercontrol.repository.model.NodeMemoryHistoryDetail;
import com.clustercontrol.repository.model.NodeProductHistoryDetail;
import com.clustercontrol.repository.model.NodeNetstatHistoryDetail;
import com.clustercontrol.repository.model.NodeNetworkInterfaceHistoryDetail;
import com.clustercontrol.repository.model.NodeOsHistoryDetail;
import com.clustercontrol.repository.model.NodePackageHistoryDetail;
import com.clustercontrol.repository.model.NodeVariableHistoryDetail;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * 構成情報収集の削除処理
 *
 * @version 6.2.0
 * @since 6.2.0
 *
 */
public class MaintenanceNodeConfigSettingHistory extends MaintenanceObject {

	private static Log m_log = LogFactory.getLog( MaintenanceNodeConfigSettingHistory.class );

	/**
	 * 削除処理
	 */
	@Override
	protected int _delete(Long boundary, boolean status, String ownerRoleId, String maintenanceId){
		m_log.debug("_delete() start : status = " + status);
		int ret = 0;
		long deletedSince = 0;
		long deletedUntil = 0;
		long startMaintenanceTimestamp = HinemosTime.currentTimeMillis();
		long timeout = HinemosPropertyCommon.maintenance_node_history_deletion_timeout.getNumericValue();

		String roleId = null;

		JpaTransactionManager jtm = null;

		try{

			// AdminRoleの場合はファシリティIDを条件にせず、全て削除
			if(!RoleIdConstant.isAdministratorRole(ownerRoleId)){
				roleId = ownerRoleId;
			}

			// 履歴削除処理
			jtm = new JpaTransactionManager();

			// 履歴削除
			List<Date> targetDateList = new ArrayList<Date>();
			targetDateList = com.clustercontrol.repository.util.QueryUtil.selectTargetDateNodeHistoryByRegDate(roleId, boundary);
			m_log.info("_delete() node target date list = " + targetDateList);
			int nodeCount = 0;
			deletedSince = 0;
			deletedUntil = 0;
			for (Date targetDate : targetDateList) {
				long start = HinemosTime.currentTimeMillis();
				if (isTimedOut(startMaintenanceTimestamp, start, timeout)) {
					sendInternalMessageForTimeout(deletedSince, deletedUntil, boundary, timeout,
							startMaintenanceTimestamp, maintenanceId, MessageConstant.MAINTENANCE_NODE.getMessage());
					return -1;
				}
				Long[] deletionSinceAndUntil = getTimestampOfDayStartAndEnd(targetDate.getTime());
				Long deletionSince = deletionSinceAndUntil[0];
				Long deletionUntil = deletionSinceAndUntil[1];
				m_log.info("_delete() : Oldest startDate of history to be deleted: " + targetDate.getTime());
				m_log.info("_delete() : Deletion range: " + deletionSince + " -> " + deletionUntil);
				
				jtm.begin();
				int deleteCount = com.clustercontrol.repository.util.QueryUtil.deleteNodeHistoryByRegDate(roleId, targetDate);
				nodeCount += deleteCount;
				jtm.commit();
				m_log.info("_delete() node target date = " + targetDate + ", nodeCount = " + deleteCount);
				
				// 削除済み期間の記録
				if (deletedSince == 0) {
					deletedSince = deletionSince;
				}
				deletedUntil = deletionUntil;
			}
			ret += nodeCount;

			/** CPU情報 */
			// 履歴詳細削除
			targetDateList = com.clustercontrol.repository.util.QueryUtil.selectTargetDateNodeHistoryDetailByRegDateTo(NodeCpuHistoryDetail.class, roleId, boundary);
			m_log.info("_delete() cpu target date list = " + targetDateList);
			int cpuCount = 0;
			deletedSince = 0;
			deletedUntil = 0;
			for (Date targetDate : targetDateList) {
				long start = HinemosTime.currentTimeMillis();
				if (isTimedOut(startMaintenanceTimestamp, start, timeout)) {
					sendInternalMessageForTimeout(deletedSince, deletedUntil, boundary, timeout,
							startMaintenanceTimestamp, maintenanceId, MessageConstant.MAINTENANCE_CPU.getMessage());
					return -1;
				}
				Long[] deletionSinceAndUntil = getTimestampOfDayStartAndEnd(targetDate.getTime());
				Long deletionSince = deletionSinceAndUntil[0];
				Long deletionUntil = deletionSinceAndUntil[1];
				m_log.info("_delete() : Oldest startDate of history to be deleted: " + targetDate.getTime());
				m_log.info("_delete() : Deletion range: " + deletionSince + " -> " + deletionUntil);
				
				jtm.begin();
				int deleteCount = com.clustercontrol.repository.util.QueryUtil.deleteNodeHistoryDetailByRegDateTo(NodeCpuHistoryDetail.class, roleId, targetDate);
				cpuCount += deleteCount;
				jtm.commit();
				m_log.info("_delete() cpu target date = " + targetDate + ", cpuCount = " + deleteCount);
				
				// 削除済み期間の記録
				if (deletedSince == 0) {
					deletedSince = deletionSince;
				}
				deletedUntil = deletionUntil;
			}
			ret += cpuCount;

			/** Disk情報 */
			// 履歴詳細削除
			targetDateList = com.clustercontrol.repository.util.QueryUtil.selectTargetDateNodeHistoryDetailByRegDateTo(NodeDiskHistoryDetail.class, roleId, boundary);
			m_log.info("_delete() disk target date list = " + targetDateList);
			int diskCount = 0;
			deletedSince = 0;
			deletedUntil = 0;
			for (Date targetDate : targetDateList) {
				long start = HinemosTime.currentTimeMillis();
				if (isTimedOut(startMaintenanceTimestamp, start, timeout)) {
					sendInternalMessageForTimeout(deletedSince, deletedUntil, boundary, timeout,
							startMaintenanceTimestamp, maintenanceId, MessageConstant.MAINTENANCE_DISK.getMessage());
					return -1;
				}
				Long[] deletionSinceAndUntil = getTimestampOfDayStartAndEnd(targetDate.getTime());
				Long deletionSince = deletionSinceAndUntil[0];
				Long deletionUntil = deletionSinceAndUntil[1];
				m_log.info("_delete() : Oldest startDate of history to be deleted: " + targetDate.getTime());
				m_log.info("_delete() : Deletion range: " + deletionSince + " -> " + deletionUntil);
				
				jtm.begin();
				int deleteCount = com.clustercontrol.repository.util.QueryUtil.deleteNodeHistoryDetailByRegDateTo(NodeDiskHistoryDetail.class, roleId, targetDate);
				diskCount += deleteCount;
				jtm.commit();
				m_log.info("_delete() disk target date = " + targetDate + ", diskCount = " + deleteCount);
				
				// 削除済み期間の記録
				if (deletedSince == 0) {
					deletedSince = deletionSince;
				}
				deletedUntil = deletionUntil;
			}
			ret += diskCount;

			/** Filesystem情報 */
			// 履歴詳細削除
			targetDateList = com.clustercontrol.repository.util.QueryUtil.selectTargetDateNodeHistoryDetailByRegDateTo(NodeFilesystemHistoryDetail.class, roleId, boundary);
			m_log.info("_delete() filesystem target date list = " + targetDateList);
			int filesystemCount = 0;
			deletedSince = 0;
			deletedUntil = 0;
			for (Date targetDate : targetDateList) {
				long start = HinemosTime.currentTimeMillis();
				if (isTimedOut(startMaintenanceTimestamp, start, timeout)) {
					sendInternalMessageForTimeout(deletedSince, deletedUntil, boundary, timeout,
							startMaintenanceTimestamp, maintenanceId,
							MessageConstant.MAINTENANCE_FILESYSTEM.getMessage());
					return -1;
				}
				Long[] deletionSinceAndUntil = getTimestampOfDayStartAndEnd(targetDate.getTime());
				Long deletionSince = deletionSinceAndUntil[0];
				Long deletionUntil = deletionSinceAndUntil[1];
				m_log.info("_delete() : Oldest startDate of history to be deleted: " + targetDate.getTime());
				m_log.info("_delete() : Deletion range: " + deletionSince + " -> " + deletionUntil);
				
				jtm.begin();
				int deleteCount = com.clustercontrol.repository.util.QueryUtil.deleteNodeHistoryDetailByRegDateTo(NodeFilesystemHistoryDetail.class, roleId, targetDate);
				filesystemCount += deleteCount;
				jtm.commit();
				m_log.info("_delete() filesystem target date = " + targetDate + ", filesystemCount = " + deleteCount);
				
				// 削除済み期間の記録
				if (deletedSince == 0) {
					deletedSince = deletionSince;
				}
				deletedUntil = deletionUntil;
			}
			ret += filesystemCount;

			/** ノード変数情報 */
			// 履歴詳細削除
			targetDateList = com.clustercontrol.repository.util.QueryUtil.selectTargetDateNodeHistoryDetailByRegDateTo(NodeVariableHistoryDetail.class, roleId, boundary);
			m_log.info("_delete() variable target date list = " + targetDateList);
			int variableCount = 0;
			deletedSince = 0;
			deletedUntil = 0;
			for (Date targetDate : targetDateList) {
				long start = HinemosTime.currentTimeMillis();
				if (isTimedOut(startMaintenanceTimestamp, start, timeout)) {
					sendInternalMessageForTimeout(deletedSince, deletedUntil, boundary, timeout,
							startMaintenanceTimestamp, maintenanceId,
							MessageConstant.MAINTENANCE_NODE_VARIABLE.getMessage());
					return -1;
				}
				Long[] deletionSinceAndUntil = getTimestampOfDayStartAndEnd(targetDate.getTime());
				Long deletionSince = deletionSinceAndUntil[0];
				Long deletionUntil = deletionSinceAndUntil[1];
				m_log.info("_delete() : Oldest startDate of history to be deleted: " + targetDate.getTime());
				m_log.info("_delete() : Deletion range: " + deletionSince + " -> " + deletionUntil);
				
				jtm.begin();
				int deleteCount = com.clustercontrol.repository.util.QueryUtil.deleteNodeHistoryDetailByRegDateTo(NodeVariableHistoryDetail.class, roleId, targetDate);
				variableCount += deleteCount;
				jtm.commit();
				m_log.info("_delete() variable target date = " + targetDate + ", variableCount = " + deleteCount);
				
				// 削除済み期間の記録
				if (deletedSince == 0) {
					deletedSince = deletionSince;
				}
				deletedUntil = deletionUntil;
			}
			ret += variableCount;

			/** ホスト名情報 */
			// 履歴詳細削除
			targetDateList = com.clustercontrol.repository.util.QueryUtil.selectTargetDateNodeHistoryDetailByRegDateTo(NodeHostnameHistoryDetail.class, roleId, boundary);
			m_log.info("_delete() hostname target date list = " + targetDateList);
			int hostnameCount = 0;
			deletedSince = 0;
			deletedUntil = 0;
			for (Date targetDate : targetDateList) {
				long start = HinemosTime.currentTimeMillis();
				if (isTimedOut(startMaintenanceTimestamp, start, timeout)) {
					sendInternalMessageForTimeout(deletedSince, deletedUntil, boundary, timeout,
							startMaintenanceTimestamp, maintenanceId,
							MessageConstant.MAINTENANCE_HOSTNAME.getMessage());
					return -1;
				}
				Long[] deletionSinceAndUntil = getTimestampOfDayStartAndEnd(targetDate.getTime());
				Long deletionSince = deletionSinceAndUntil[0];
				Long deletionUntil = deletionSinceAndUntil[1];
				m_log.info("_delete() : Oldest startDate of history to be deleted: " + targetDate.getTime());
				m_log.info("_delete() : Deletion range: " + deletionSince + " -> " + deletionUntil);
				
				jtm.begin();
				int deleteCount = com.clustercontrol.repository.util.QueryUtil.deleteNodeHistoryDetailByRegDateTo(NodeHostnameHistoryDetail.class, roleId, targetDate);
				hostnameCount += deleteCount;
				jtm.commit();
				m_log.info("_delete() hostname target date = " + targetDate + ", hostnameCount = " + deleteCount);
				
				// 削除済み期間の記録
				if (deletedSince == 0) {
					deletedSince = deletionSince;
				}
				deletedUntil = deletionUntil;
			}
			ret += hostnameCount;

			/** メモリ情報 */
			// 履歴詳細削除
			targetDateList = com.clustercontrol.repository.util.QueryUtil.selectTargetDateNodeHistoryDetailByRegDateTo(NodeMemoryHistoryDetail.class, roleId, boundary);
			m_log.info("_delete() memory target date list = " + targetDateList);
			int memoryCount = 0;
			deletedSince = 0;
			deletedUntil = 0;
			for (Date targetDate : targetDateList) {
				long start = HinemosTime.currentTimeMillis();
				if (isTimedOut(startMaintenanceTimestamp, start, timeout)) {
					sendInternalMessageForTimeout(deletedSince, deletedUntil, boundary, timeout,
							startMaintenanceTimestamp, maintenanceId, MessageConstant.MAINTENANCE_MEMORY.getMessage());
					return -1;
				}
				Long[] deletionSinceAndUntil = getTimestampOfDayStartAndEnd(targetDate.getTime());
				Long deletionSince = deletionSinceAndUntil[0];
				Long deletionUntil = deletionSinceAndUntil[1];
				m_log.info("_delete() : Oldest startDate of history to be deleted: " + targetDate.getTime());
				m_log.info("_delete() : Deletion range: " + deletionSince + " -> " + deletionUntil);
				
				jtm.begin();
				int deleteCount = com.clustercontrol.repository.util.QueryUtil.deleteNodeHistoryDetailByRegDateTo(NodeMemoryHistoryDetail.class, roleId, targetDate);
				memoryCount += deleteCount;
				jtm.commit();
				m_log.info("_delete() memory target date = " + targetDate + ", memoryCount = " + deleteCount);
				
				// 削除済み期間の記録
				if (deletedSince == 0) {
					deletedSince = deletionSince;
				}
				deletedUntil = deletionUntil;
			}
			ret += memoryCount;

			/** NIC情報 */
			// 履歴詳細削除
			targetDateList = com.clustercontrol.repository.util.QueryUtil.selectTargetDateNodeHistoryDetailByRegDateTo(NodeNetworkInterfaceHistoryDetail.class, roleId, boundary);
			m_log.info("_delete() nic target date list = " + targetDateList);
			int nicCount = 0;
			deletedSince = 0;
			deletedUntil = 0;
			for (Date targetDate : targetDateList) {
				long start = HinemosTime.currentTimeMillis();
				if (isTimedOut(startMaintenanceTimestamp, start, timeout)) {
					sendInternalMessageForTimeout(deletedSince, deletedUntil, boundary, timeout,
							startMaintenanceTimestamp, maintenanceId, MessageConstant.MAINTENANCE_NIC.getMessage());
					return -1;
				}
				Long[] deletionSinceAndUntil = getTimestampOfDayStartAndEnd(targetDate.getTime());
				Long deletionSince = deletionSinceAndUntil[0];
				Long deletionUntil = deletionSinceAndUntil[1];
				m_log.info("_delete() : Oldest startDate of history to be deleted: " + targetDate.getTime());
				m_log.info("_delete() : Deletion range: " + deletionSince + " -> " + deletionUntil);
				
				jtm.begin();
				int deleteCount = com.clustercontrol.repository.util.QueryUtil.deleteNodeHistoryDetailByRegDateTo(NodeNetworkInterfaceHistoryDetail.class, roleId, targetDate);
				nicCount += deleteCount;
				jtm.commit();
				m_log.info("_delete() nic target date = " + targetDate + ", nicCount = " + deleteCount);
				
				// 削除済み期間の記録
				if (deletedSince == 0) {
					deletedSince = deletionSince;
				}
				deletedUntil = deletionUntil;
			}
			ret += nicCount;

			/** OS情報 */
			// 履歴詳細削除
			targetDateList = com.clustercontrol.repository.util.QueryUtil.selectTargetDateNodeHistoryDetailByRegDateTo(NodeOsHistoryDetail.class, roleId, boundary);
			m_log.info("_delete() os target date list = " + targetDateList);
			int osCount = 0;
			deletedSince = 0;
			deletedUntil = 0;
			for (Date targetDate : targetDateList) {
				long start = HinemosTime.currentTimeMillis();
				if (isTimedOut(startMaintenanceTimestamp, start, timeout)) {
					sendInternalMessageForTimeout(deletedSince, deletedUntil, boundary, timeout,
							startMaintenanceTimestamp, maintenanceId, MessageConstant.MAINTENANCE_OS.getMessage());
					return -1;
				}
				Long[] deletionSinceAndUntil = getTimestampOfDayStartAndEnd(targetDate.getTime());
				Long deletionSince = deletionSinceAndUntil[0];
				Long deletionUntil = deletionSinceAndUntil[1];
				m_log.info("_delete() : Oldest startDate of history to be deleted: " + targetDate.getTime());
				m_log.info("_delete() : Deletion range: " + deletionSince + " -> " + deletionUntil);
				
				jtm.begin();
				int deleteCount = com.clustercontrol.repository.util.QueryUtil.deleteNodeHistoryDetailByRegDateTo(NodeOsHistoryDetail.class, roleId, targetDate);
				osCount += deleteCount;
				jtm.commit();
				m_log.info("_delete() os target date = " + targetDate + ", osCount = " + deleteCount);
				
				// 削除済み期間の記録
				if (deletedSince == 0) {
					deletedSince = deletionSince;
				}
				deletedUntil = deletionUntil;
			}
			ret += osCount;

			/** ネットワーク接続 */
			// 履歴詳細削除
			targetDateList = com.clustercontrol.repository.util.QueryUtil.selectTargetDateNodeHistoryDetailByRegDateTo(NodeNetstatHistoryDetail.class, roleId, boundary);
			m_log.info("_delete() netstat target date list = " + targetDateList);
			int netstatCount = 0;
			deletedSince = 0;
			deletedUntil = 0;
			for (Date targetDate : targetDateList) {
				long start = HinemosTime.currentTimeMillis();
				if (isTimedOut(startMaintenanceTimestamp, start, timeout)) {
					sendInternalMessageForTimeout(deletedSince, deletedUntil, boundary, timeout,
							startMaintenanceTimestamp, maintenanceId, MessageConstant.MAINTENANCE_NETSTAT.getMessage());
					return -1;
				}
				Long[] deletionSinceAndUntil = getTimestampOfDayStartAndEnd(targetDate.getTime());
				Long deletionSince = deletionSinceAndUntil[0];
				Long deletionUntil = deletionSinceAndUntil[1];
				m_log.info("_delete() : Oldest startDate of history to be deleted: " + targetDate.getTime());
				m_log.info("_delete() : Deletion range: " + deletionSince + " -> " + deletionUntil);
				
				jtm.begin();
				int deleteCount = com.clustercontrol.repository.util.QueryUtil.deleteNodeHistoryDetailByRegDateTo(NodeNetstatHistoryDetail.class, roleId, targetDate);
				netstatCount += deleteCount;
				jtm.commit();
				m_log.info("_delete() netstat target date = " + targetDate + ", netstatCount = " + deleteCount);
				
				// 削除済み期間の記録
				if (deletedSince == 0) {
					deletedSince = deletionSince;
				}
				deletedUntil = deletionUntil;
			}
			ret += netstatCount;

			/** パッケージ情報 */
			// 履歴詳細削除
			targetDateList = com.clustercontrol.repository.util.QueryUtil.selectTargetDateNodeHistoryDetailByRegDateTo(NodePackageHistoryDetail.class, roleId, boundary);
			m_log.info("_delete() package target date list = " + targetDateList);
			int packageCount = 0;
			deletedSince = 0;
			deletedUntil = 0;
			for (Date targetDate : targetDateList) {
				long start = HinemosTime.currentTimeMillis();
				if (isTimedOut(startMaintenanceTimestamp, start, timeout)) {
					sendInternalMessageForTimeout(deletedSince, deletedUntil, boundary, timeout,
							startMaintenanceTimestamp, maintenanceId, MessageConstant.MAINTENANCE_PACKAGE.getMessage());
					return -1;
				}
				Long[] deletionSinceAndUntil = getTimestampOfDayStartAndEnd(targetDate.getTime());
				Long deletionSince = deletionSinceAndUntil[0];
				Long deletionUntil = deletionSinceAndUntil[1];
				m_log.info("_delete() : Oldest startDate of history to be deleted: " + targetDate.getTime());
				m_log.info("_delete() : Deletion range: " + deletionSince + " -> " + deletionUntil);
				
				jtm.begin();
				int deleteCount = com.clustercontrol.repository.util.QueryUtil.deleteNodeHistoryDetailByRegDateTo(NodePackageHistoryDetail.class, roleId, targetDate);
				packageCount += deleteCount;
				jtm.commit();
				m_log.info("_delete() package target date = " + targetDate + ", packageCount = " + deleteCount);
				
				// 削除済み期間の記録
				if (deletedSince == 0) {
					deletedSince = deletionSince;
				}
				deletedUntil = deletionUntil;
			}
			ret += packageCount;

			/** ユーザ任意情報 */
			// 履歴詳細削除
			targetDateList = com.clustercontrol.repository.util.QueryUtil.selectTargetDateNodeHistoryDetailByRegDateTo(NodeCustomHistoryDetail.class, roleId, boundary);
			m_log.info("_delete() custom target date list = " + targetDateList);
			int customCount = 0;
			deletedSince = 0;
			deletedUntil = 0;
			for (Date targetDate : targetDateList) {
				long start = HinemosTime.currentTimeMillis();
				if (isTimedOut(startMaintenanceTimestamp, start, timeout)) {
					sendInternalMessageForTimeout(deletedSince, deletedUntil, boundary, timeout,
							startMaintenanceTimestamp, maintenanceId, MessageConstant.MAINTENANCE_CUSTOM.getMessage());
					return -1;
				}
				Long[] deletionSinceAndUntil = getTimestampOfDayStartAndEnd(targetDate.getTime());
				Long deletionSince = deletionSinceAndUntil[0];
				Long deletionUntil = deletionSinceAndUntil[1];
				m_log.info("_delete() : Oldest startDate of history to be deleted: " + targetDate.getTime());
				m_log.info("_delete() : Deletion range: " + deletionSince + " -> " + deletionUntil);
				
				jtm.begin();
				int deleteCount = com.clustercontrol.repository.util.QueryUtil.deleteNodeHistoryDetailByRegDateTo(NodeCustomHistoryDetail.class, roleId, targetDate);
				customCount += deleteCount;
				jtm.commit();
				m_log.info("_delete() custom target date = " + targetDate + ", customCount = " + deleteCount);
				
				// 削除済み期間の記録
				if (deletedSince == 0) {
					deletedSince = deletionSince;
				}
				deletedUntil = deletionUntil;
			}
			ret += customCount;

			/** 個別導入製品情報 */
			// 履歴詳細削除
			targetDateList = com.clustercontrol.repository.util.QueryUtil.selectTargetDateNodeHistoryDetailByRegDateTo(NodeProductHistoryDetail.class, roleId, boundary);
			m_log.info("_delete() product target date list = " + targetDateList);
			int productCount = 0;
			deletedSince = 0;
			deletedUntil = 0;
			for (Date targetDate : targetDateList) {
				long start = HinemosTime.currentTimeMillis();
				if (isTimedOut(startMaintenanceTimestamp, start, timeout)) {
					sendInternalMessageForTimeout(deletedSince, deletedUntil, boundary, timeout,
							startMaintenanceTimestamp, maintenanceId, MessageConstant.MAINTENANCE_PRODUCT.getMessage());
					return -1;
				}
				Long[] deletionSinceAndUntil = getTimestampOfDayStartAndEnd(targetDate.getTime());
				Long deletionSince = deletionSinceAndUntil[0];
				Long deletionUntil = deletionSinceAndUntil[1];
				m_log.info("_delete() : Oldest startDate of history to be deleted: " + targetDate.getTime());
				m_log.info("_delete() : Deletion range: " + deletionSince + " -> " + deletionUntil);
				
				jtm.begin();
				int deleteCount = com.clustercontrol.repository.util.QueryUtil.deleteNodeHistoryDetailByRegDateTo(NodeProductHistoryDetail.class, roleId, targetDate);
				productCount += deleteCount;
				jtm.commit();
				m_log.info("_delete() product target date = " + targetDate + ", productCount = " + deleteCount);
				
				// 削除済み期間の記録
				if (deletedSince == 0) {
					deletedSince = deletionSince;
				}
				deletedUntil = deletionUntil;
			}
			ret += productCount;

			/** ライセンス情報 */
			// 履歴詳細削除
			targetDateList = com.clustercontrol.repository.util.QueryUtil.selectTargetDateNodeHistoryDetailByRegDateTo(NodeLicenseHistoryDetail.class, roleId, boundary);
			m_log.info("_delete() license target date list = " + targetDateList);
			int licenseCount = 0;
			deletedSince = 0;
			deletedUntil = 0;
			for (Date targetDate : targetDateList) {
				long start = HinemosTime.currentTimeMillis();
				if (isTimedOut(startMaintenanceTimestamp, start, timeout)) {
					sendInternalMessageForTimeout(deletedSince, deletedUntil, boundary, timeout,
							startMaintenanceTimestamp, maintenanceId, MessageConstant.MAINTENANCE_LICENSE.getMessage());
					return -1;
				}
				Long[] deletionSinceAndUntil = getTimestampOfDayStartAndEnd(targetDate.getTime());
				Long deletionSince = deletionSinceAndUntil[0];
				Long deletionUntil = deletionSinceAndUntil[1];
				m_log.info("_delete() : Oldest startDate of history to be deleted: " + targetDate.getTime());
				m_log.info("_delete() : Deletion range: " + deletionSince + " -> " + deletionUntil);
				
				jtm.begin();
				int deleteCount = com.clustercontrol.repository.util.QueryUtil.deleteNodeHistoryDetailByRegDateTo(NodeLicenseHistoryDetail.class, roleId, targetDate);
				licenseCount += deleteCount;
				jtm.commit();
				m_log.info("_delete() license target date = " + targetDate + ", licenseCount = " + deleteCount);
				
				// 削除済み期間の記録
				if (deletedSince == 0) {
					deletedSince = deletionSince;
				}
				deletedUntil = deletionUntil;
			}
			ret += licenseCount;

			long deleteTime = HinemosTime.currentTimeMillis() - startMaintenanceTimestamp;
			m_log.info("_delete() "
					+ "total delete count=" + ret
					+ ", nodeCount=" + nodeCount
					+ ", cpuCount=" + cpuCount 
					+ ", diskCount=" + diskCount 
					+ ", filesystemCount=" + filesystemCount 
					+ ", variableCount=" + variableCount
					+ ", hostnameCount=" + hostnameCount 
					+ ", memoryCount=" + memoryCount 
					+ ", nicCount=" + nicCount 
					+ ", osCount=" + osCount 
					+ ", netstatCount=" + netstatCount
					+ ", packageCount=" + packageCount
					+ ", customCount=" + customCount
					+ ", productCount=" + productCount
					+ ", licenseCount=" + licenseCount
					+ ", deleteTime=" + deleteTime  +"ms");

		} catch(Exception e){
			String countMessage = "delete count : " + ret + " records" + "\n";
			m_log.warn("deleteNodeConfigSettingHistory() : " + countMessage
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			ret = -1;
			if (jtm != null)
				jtm.rollback();
		} finally {
			if (jtm != null)
				jtm.close();
		}
		return ret;
	}
	
	/**
	 * 構成情報履歴削除がタイムアウトした場合のINTERNALメッセージを送信
	 * @param deletedSince
	 * @param deletedUntil
	 * @param boundary
	 * @param timeout
	 * @param startMaintenanceTimestamp
	 * @param maintenanceId
	 */
	protected void sendInternalMessageForTimeout(long deletedSince, long deletedUntil, long boundary, Long timeout,
			long startMaintenanceTimestamp, String maintenanceId, String deleteTarget) {
		Calendar cal = HinemosTime.getCalendarInstance();

		// 実際に削除された期間の終了日を取得
		// deletedUntilは削除対象期間には含まれないため、その前日を取得する
		cal.setTimeInMillis(deletedUntil);
		cal.add(Calendar.DAY_OF_MONTH, -1);
		long deletedUntilYesterday = cal.getTimeInMillis();
		
		// 削除対象期間の終了日を取得
		// boundaryは削除対象期間に含まれないため、その前日を取得する
		cal.setTimeInMillis(boundary);
		cal.add(Calendar.DAY_OF_MONTH, -1);
		long boundaryYesterday = cal.getTimeInMillis();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		String deletedSinceStr = sdf.format(deletedSince);
		String deletedUntilStr = sdf.format(deletedUntilYesterday);
		String boundaryStr = sdf.format(boundaryYesterday);
		
		String[] args = new String[5];
		args[0] = maintenanceId;
		args[1] = timeout.toString();
		args[2] = deleteTarget;
		args[3] = deletedSinceStr + " - " + boundaryStr;
		if (deletedUntil == 0) {
			// deletedUntilが0、すなわち1日も削除できていない場合は
			// 削除済み期間にはNoneと表示しておく
			args[4] = "None";
		} else {
			args[4] = deletedSinceStr + " - " + deletedUntilStr;
		}
		
		AplLogger.put(InternalIdCommon.MAINTENANCE_SYS_003, args);
		m_log.warn("_delete() : Maintenance is timed out. " +
				"startMaintenanceTimestamp: " + startMaintenanceTimestamp + ", " +
				"deletion target: " + deletedSince + " -> " + boundaryYesterday + ", " +
				"deleted: " + deletedSince + " -> " + deletedUntilYesterday);
	}
}
