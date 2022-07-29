/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.DataRangeConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.maintenance.factory.SelectMaintenanceTypeMst;
import com.clustercontrol.maintenance.model.MaintenanceInfo;
import com.clustercontrol.maintenance.model.MaintenanceTypeMst;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.util.MessageConstant;

/**
 * 履歴削除の入力チェッククラス
 * 
 * @since 4.0
 */
public class MaintenanceValidator {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( MaintenanceValidator.class );

	/**
	 * メンテナンス情報の妥当性チェック
	 * 
	 * @param maintenanceInfo
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public static void validateMaintenanceInfo(MaintenanceInfo maintenanceInfo, boolean isModify) throws InvalidSetting, InvalidRole {

		// maintenanceId
		if (maintenanceInfo.getMaintenanceId() == null ||
				"".equals(maintenanceInfo.getMaintenanceId())) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_MAINTENANCE_ID.getMessage());
			m_log.info("validateMaintenanceInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateId(MessageConstant.MAINTENANCE_ID.getMessage(), maintenanceInfo.getMaintenanceId(), 64);

		// ownerRoleId
		if(!isModify){
			CommonValidator.validateString(MessageConstant.OWNER_ROLE_ID.getMessage(), maintenanceInfo.getOwnerRoleId(), true, 1, 64);	
		} 

		// schedule
		CommonValidator.validateScheduleHour(maintenanceInfo.getSchedule());

		// calendarId
		CommonValidator.validateCalenderId(maintenanceInfo.getCalendarId(), false, maintenanceInfo.getOwnerRoleId());

		// notifyId
		if(maintenanceInfo.getNotifyId() != null){
			for(NotifyRelationInfo notifyRelationInfo : maintenanceInfo.getNotifyId()){
				CommonValidator.validateNotifyId(notifyRelationInfo.getNotifyId(), true, maintenanceInfo.getOwnerRoleId());
			}
		}

		// typeId
		String typeId = maintenanceInfo.getTypeId();
		if (typeId == null || "".equals(typeId)) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_MAINTENANCE_TYPE.getMessage());
					m_log.info("validateMaintenanceInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		SelectMaintenanceTypeMst select = new SelectMaintenanceTypeMst();
		boolean flag = true;
		try {
			for (MaintenanceTypeMst mst : select.getMaintenanceTypeList()) {
				if (typeId.equals(mst.getType_id())) {
					flag = false;
					break;
				}
			}
			if (flag) {
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_MAINTENANCE_TYPE.getMessage());
				m_log.info("validateMaintenanceInfo() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (InvalidSetting e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("validateMaintenanceInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}

		// dataRetentionPeriod
		if(maintenanceInfo.getDataRetentionPeriod() == null){
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_RETENTION_PERIOD.getMessage());
			m_log.info("validateMaintenanceInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateInt(MessageConstant.MAINTENANCE_RETENTION_PERIOD.getMessage(),
				maintenanceInfo.getDataRetentionPeriod(), 0, DataRangeConstant.SMALLINT_HIGH);

		// description
		CommonValidator.validateString(MessageConstant.DESCRIPTION.getMessage(),
				maintenanceInfo.getDescription(), false, 0, 256);

		// application
		if (maintenanceInfo.getApplication() == null ||
				"".equals(maintenanceInfo.getApplication())) {
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_APPLICATION.getMessage());
			m_log.info("validateMaintenanceInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		CommonValidator.validateString(MessageConstant.APPLICATION.getMessage(),
				maintenanceInfo.getApplication(), true, 0, 64);
	}
}
