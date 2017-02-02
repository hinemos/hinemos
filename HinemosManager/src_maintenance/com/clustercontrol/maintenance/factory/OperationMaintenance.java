/*

Copyright (C) 2007 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.maintenance.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MaintenanceNotFound;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.maintenance.bean.MaintenanceTypeMstConstant;
import com.clustercontrol.maintenance.model.MaintenanceInfo;
import com.clustercontrol.maintenance.session.MaintenanceControllerBean;
import com.clustercontrol.maintenance.util.QueryUtil;

/**
 * 
 * メンテナンス機能が提供する操作を実行するクラスです。
 * 
 * @version 4.0.0
 * @since 2.2.0
 *
 */
public class OperationMaintenance {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( OperationMaintenance.class );

	/**
	 * @param maintenanceId
	 */
	public void runMaintenance(String maintenanceId) {

		int result = -1;
		Integer type = PriorityConstant.TYPE_CRITICAL;

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
			} else {
				m_log.info("runMaintenance() : " + type_id);
			}

			type = PriorityConstant.TYPE_INFO;
		} catch (MaintenanceNotFound e) {
			// 何もしない
		} catch (InvalidRole e) {
			// 何もしない
		} catch (HinemosUnknown e) {
			// 何もしない
		} finally {
			try {
				new Notice().notify(maintenanceId, type, result);
			} catch (HinemosUnknown e) {
				m_log.debug(e.getMessage(), e);
			}
		}
	}

}
