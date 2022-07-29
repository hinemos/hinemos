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

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MaintenanceNotFound;
import com.clustercontrol.commons.util.JpaTransactionManager;
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

		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			MaintenanceInfo entity = QueryUtil.getMaintenanceInfoPK(maintenanceId);
			MaintenanceControllerBean controller = new MaintenanceControllerBean();

			Integer dataRetentionPeriod = entity.getDataRetentionPeriod();
			String type_id = entity.getMaintenanceTypeMstEntity() == null ? null :
				entity.getMaintenanceTypeMstEntity().getType_id();
			String ownerRoleId = entity.getOwnerRoleId();
			
			jtm.commit();

			if (MaintenanceTypeMstConstant.DELETE_EVENT_LOG_ALL.equals(type_id)) {
				result = controller.deleteEventLog(dataRetentionPeriod, true, ownerRoleId, maintenanceId);
			} else if (MaintenanceTypeMstConstant.DELETE_EVENT_LOG.equals(type_id)) {
				result = controller.deleteEventLog(dataRetentionPeriod, false, ownerRoleId, maintenanceId);
			} else if (MaintenanceTypeMstConstant.DELETE_JOB_HISTORY_ALL.equals(type_id)) {
				result = controller.deleteJobHistory(dataRetentionPeriod, true, ownerRoleId, maintenanceId);
			} else if (MaintenanceTypeMstConstant.DELETE_JOB_HISTORY.equals(type_id)) {
				result = controller.deleteJobHistory(dataRetentionPeriod, false, ownerRoleId, maintenanceId);
			} else if (MaintenanceTypeMstConstant.DELETE_COLLECT_DATA_RAW.equals(type_id)) {
				result = controller.deleteCollectData(dataRetentionPeriod, true, ownerRoleId, maintenanceId);
			} else if (MaintenanceTypeMstConstant.DELETE_SUMMARY_DATA_HOUR.equals(type_id)) {
				result = controller.deleteSummaryHour(dataRetentionPeriod, true, ownerRoleId, maintenanceId);
			} else if (MaintenanceTypeMstConstant.DELETE_SUMMARY_DATA_DAY.equals(type_id)) {
				result = controller.deleteSummaryDay(dataRetentionPeriod, true, ownerRoleId, maintenanceId);
			} else if (MaintenanceTypeMstConstant.DELETE_SUMMARY_DATA_MONTH.equals(type_id)) {
				result = controller.deleteSummaryMonth(dataRetentionPeriod, true, ownerRoleId, maintenanceId);
			} else if (MaintenanceTypeMstConstant.DELETE_COLLECT_STRING_DATA.equals(type_id)) {
				result = controller.deleteCollectStringData(dataRetentionPeriod, true, ownerRoleId, maintenanceId);
			} else if (MaintenanceTypeMstConstant.DELETE_COLLECT_BINFILE_DATA.equals(type_id)) {
				result = controller.deleteCollectBinaryData(dataRetentionPeriod, true, ownerRoleId, maintenanceId, HinemosModuleConstant.MONITOR_BINARYFILE_BIN);
			} else if (MaintenanceTypeMstConstant.DELETE_COLLECT_PCAP_DATA.equals(type_id)) {
				result = controller.deleteCollectBinaryData(dataRetentionPeriod, true, ownerRoleId, maintenanceId, HinemosModuleConstant.MONITOR_PCAP_BIN);
			} else if (MaintenanceTypeMstConstant.DELETE_COLLECT_BINARY_DATA.equals(type_id)) {
				result = controller.deleteCollectBinaryData(dataRetentionPeriod, true, ownerRoleId, maintenanceId);
			} else if (MaintenanceTypeMstConstant.DELETE_NODE_CONFIG_HISTORY.equals(type_id)) {
				result = controller.deleteNodeConfigSettingHistory(dataRetentionPeriod, true, ownerRoleId, maintenanceId);
			} else if (MaintenanceTypeMstConstant.DELETE_JOBLINK_MSG.equals(type_id)) {
				result = controller.deleteJobLinkMessage(dataRetentionPeriod, true, ownerRoleId, maintenanceId);
			} else if (MaintenanceTypeMstConstant.DELETE_RPA_SCENARIO_OPERATION_RESULT.equals(type_id)) {
				result = controller.deleteRpaScenarioOperationResult(dataRetentionPeriod, true, ownerRoleId, maintenanceId);
			} else {
				m_log.info("runMaintenance() : " + type_id);
			}
			
		} catch (MaintenanceNotFound e) {
			if (jtm != null) {
				jtm.rollback();
			}
		} catch (InvalidRole e) {
			if (jtm != null) {
				jtm.rollback();
			}
		} catch (HinemosUnknown e) {
			if (jtm != null) {
				jtm.rollback();
			}
		} finally {
			if (jtm != null) {
				jtm.close();
			}
			try {
				rtn = new Notice().createOutputBasicInfo(maintenanceId, result);
			} catch (HinemosUnknown e) {
				m_log.debug(e.getMessage(), e);
			}
		}
		return rtn;
	}

}
