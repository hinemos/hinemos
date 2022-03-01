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

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MaintenanceNotFound;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.maintenance.model.MaintenanceInfo;
import com.clustercontrol.maintenance.model.MaintenanceTypeMst;
import com.clustercontrol.maintenance.util.QueryUtil;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.util.HinemosTime;

import jakarta.persistence.EntityExistsException;


/**
 * メンテナンス情報を更新するためのクラスです。
 * 
 * @version 4.0.0
 * @since 2.2.0
 *
 */
public class ModifyMaintenance {

	private static Log m_log = LogFactory.getLog( ModifyMaintenance.class );

	/**
	 * @param data
	 * @param name
	 * @return
	 * @throws EntityExistsException
	 * @throws HinemosUnknown
	 */
	public boolean addMaintenance(MaintenanceInfo data, String name)
			throws EntityExistsException, InvalidRole, HinemosUnknown {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// Entityクラスのインスタンス生成
			MaintenanceTypeMst maintenanceTypeMstEntity = null;
			if (data.getTypeId() != null) {
				try {
					maintenanceTypeMstEntity = QueryUtil.getMaintenanceTypeMstPK(data.getTypeId());
				} catch (MaintenanceNotFound e) {
				}
			}

			try {
				// 重複チェック
				jtm.checkEntityExists(MaintenanceInfo.class, data.getMaintenanceId());
				
				long now = HinemosTime.currentTimeMillis();
				
				data.setRegUser(name);
				data.setRegDate(now);
				data.setUpdateUser(name);
				data.setUpdateDate(now);
				
				em.persist(data);
				data.relateToMaintenanceTypeMstEntity(maintenanceTypeMstEntity);
			} catch (EntityExistsException e){
				m_log.info("addMaintenance() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				throw e;
			}

			if(data.getNotifyId() != null){
				new NotifyControllerBean().addNotifyRelation(data.getNotifyId(), data.getOwnerRoleId());
			}
		}

		return true;

	}
	
	/**
	 * @param info
	 * @param name
	 * @return
	 * @throws MaintenanceNotFound
	 * @throws NotifyNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public boolean modifyMaintenance(MaintenanceInfo info, String name)
			throws MaintenanceNotFound, NotifyNotFound, InvalidRole, HinemosUnknown {

		//メンテナンス情報を取得
		MaintenanceInfo entity = QueryUtil.getMaintenanceInfoPK(info.getMaintenanceId(), ObjectPrivilegeMode.MODIFY);

		//メンテナンス情報を更新
		entity.setDescription(info.getDescription());
		MaintenanceTypeMst maintenanceTypeMstEntity = null;
		if (info.getTypeId() != null) {
			try {
				maintenanceTypeMstEntity = QueryUtil.getMaintenanceTypeMstPK(info.getTypeId());
			} catch (MaintenanceNotFound e) {
			}
		}
		entity.relateToMaintenanceTypeMstEntity(maintenanceTypeMstEntity);
		entity.setDataRetentionPeriod(info.getDataRetentionPeriod());
		entity.setCalendarId(info.getCalendarId());
		entity.getSchedule().setType(info.getSchedule().getType());
		entity.getSchedule().setMonth(info.getSchedule().getMonth());
		entity.getSchedule().setDay(info.getSchedule().getDay());
		entity.getSchedule().setWeek(info.getSchedule().getWeek());
		entity.getSchedule().setHour(info.getSchedule().getHour());
		entity.getSchedule().setMinute(info.getSchedule().getMinute());
		entity.setNotifyGroupId(info.getNotifyGroupId());
		entity.setApplication(info.getApplication());
		entity.setValidFlg(info.getValidFlg());
		entity.setUpdateUser(name);
		entity.setUpdateDate(HinemosTime.currentTimeMillis());

		new NotifyControllerBean().modifyNotifyRelation(info.getNotifyId(), info.getNotifyGroupId(), info.getOwnerRoleId());

		return true;
	}
	
	/**
	 * @param maintenanceId
	 * @return
	 * @throws MaintenanceNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public boolean deleteMaintenance(String maintenanceId)
			throws MaintenanceNotFound, InvalidRole, HinemosUnknown {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// 削除対象を検索
			MaintenanceInfo entity = QueryUtil.getMaintenanceInfoPK(maintenanceId, ObjectPrivilegeMode.MODIFY);

			//通知情報の削除
			new NotifyControllerBean().deleteNotifyRelation(entity.getNotifyGroupId());

			//メンテナンス情報の削除
			entity.unchain();	// 削除前処理
			em.remove(entity);

			// 通知履歴情報を削除する
			new NotifyControllerBean().deleteNotifyHistory(HinemosModuleConstant.SYSYTEM_MAINTENANCE, maintenanceId);

			return true;
		}
	}
}