/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MaintenanceNotFound;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.maintenance.bean.MaintenanceTypeMstConstant;
import com.clustercontrol.maintenance.model.MaintenanceInfo;
import com.clustercontrol.maintenance.session.MaintenanceControllerBean;
import com.clustercontrol.maintenance.util.QueryUtil;
import com.clustercontrol.notify.bean.OutputBasicInfo;

/**
 * 
 * メンテナンス機能が提供する操作を実行するクラスです。
 * 
 * @version 6.1.0 バイナリ収集データ削除を追加.
 * @since 2.2.0
 *
 */
public class OperationMaintenance {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( OperationMaintenance.class );

	/**
	 * @param maintenanceId
	 * @return 通知情報
	 */
	public OutputBasicInfo runMaintenance(String maintenanceId) {

		OutputBasicInfo rtn = null;

		int result = 0;

		try {
			MaintenanceInfo entity = QueryUtil.getMaintenanceInfoPK(maintenanceId);
			MaintenanceControllerBean controller = new MaintenanceControllerBean();

			Integer dataRetentionPeriod = entity.getDataRetentionPeriod();
			String type_id = entity.getMaintenanceTypeMstEntity() == null ? null :
				entity.getMaintenanceTypeMstEntity().getType_id();
			String ownerRoleId = entity.getOwnerRoleId();

			if (MaintenanceTypeMstConstant.DELETE_EVENT_LOG_ALL.equals(type_id)) {
				result = controller.deleteEventLog(dataRetentionPeriod, true, ownerRoleId);
			} else if (MaintenanceTypeMstConstant.DELETE_EVENT_LOG.equals(type_id)) {
				result = controller.deleteEventLog(dataRetentionPeriod, false, ownerRoleId);
			} else if (MaintenanceTypeMstConstant.DELETE_JOB_HISTORY_ALL.equals(type_id)) {
				result = controller.deleteJobHistory(dataRetentionPeriod, true, ownerRoleId);
			} else if (MaintenanceTypeMstConstant.DELETE_JOB_HISTORY.equals(type_id)) {
				result = controller.deleteJobHistory(dataRetentionPeriod, false, ownerRoleId);
			} else if (MaintenanceTypeMstConstant.DELETE_COLLECT_DATA_RAW.equals(type_id)) {
				result = controller.deleteCollectData(dataRetentionPeriod, true, ownerRoleId);
			} else if (MaintenanceTypeMstConstant.DELETE_SUMMARY_DATA_HOUR.equals(type_id)) {
				result = controller.deleteSummaryHour(dataRetentionPeriod, true, ownerRoleId);
			} else if (MaintenanceTypeMstConstant.DELETE_SUMMARY_DATA_DAY.equals(type_id)) {
				result = controller.deleteSummaryDay(dataRetentionPeriod, true, ownerRoleId);
			} else if (MaintenanceTypeMstConstant.DELETE_SUMMARY_DATA_MONTH.equals(type_id)) {
				result = controller.deleteSummaryMonth(dataRetentionPeriod, true, ownerRoleId);
			} else if (MaintenanceTypeMstConstant.DELETE_COLLECT_STRING_DATA.equals(type_id)) {
				result = controller.deleteCollectStringData(dataRetentionPeriod, true, ownerRoleId);
			} else if (MaintenanceTypeMstConstant.DELETE_COLLECT_BINFILE_DATA.equals(type_id)) {
				result = controller.deleteCollectBinaryData(dataRetentionPeriod, true, ownerRoleId, HinemosModuleConstant.MONITOR_BINARYFILE_BIN);
			} else if (MaintenanceTypeMstConstant.DELETE_COLLECT_PCAP_DATA.equals(type_id)) {
				result = controller.deleteCollectBinaryData(dataRetentionPeriod, true, ownerRoleId, HinemosModuleConstant.MONITOR_PCAP_BIN);
			} else if (MaintenanceTypeMstConstant.DELETE_COLLECT_BINARY_DATA.equals(type_id)) {
				result = controller.deleteCollectBinaryData(dataRetentionPeriod, true, ownerRoleId);
			} else if (MaintenanceTypeMstConstant.DELETE_NODE_CONFIG_HISTORY.equals(type_id)) {
				result = controller.deleteNodeConfigSettingHistory(dataRetentionPeriod, true, ownerRoleId);
			} else {
				m_log.info("runMaintenance() : " + type_id);
			}

		} catch (MaintenanceNotFound e) {
			// 何もしない
		} catch (InvalidRole e) {
			// 何もしない
		} catch (HinemosUnknown e) {
			// 何もしない
		} finally {
			try {
				rtn = new Notice().createOutputBasicInfo(maintenanceId, result);
			} catch (HinemosUnknown e) {
				m_log.debug(e.getMessage(), e);
			}
		}
		return rtn;
	}

}
