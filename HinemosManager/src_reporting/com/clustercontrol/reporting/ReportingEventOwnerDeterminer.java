/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.notify.util.INotifyOwnerDeterminer;
import com.clustercontrol.reporting.model.ReportingInfoEntity;

public final class ReportingEventOwnerDeterminer implements INotifyOwnerDeterminer {

	private static final Log m_log = LogFactory.getLog( ReportingEventOwnerDeterminer.class );
	
	@Override
	/**
	 * レポーティングのイベントログのオーナーロールIdを決定するための関数。
	 * イベントの対象となるノードのオーナーロールIDを返す。
	 */
	public String getEventOwnerRoleId(String monitorId, String monitorDetailId, String pluginId, String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			if (m_log.isDebugEnabled()) {
				m_log.debug("getEventOwnerRoleId() : decide event ownerroleid." +
						"monitorId = " + monitorId + 
						", monitorDetailId = " + monitorDetailId + 
						", pluginId = " + pluginId + 
						", facilityId = " + facilityId);
			}

			ReportingInfoEntity entity
			= em.find(ReportingInfoEntity.class, monitorId, ObjectPrivilegeMode.NONE);

			if (entity != null && entity.getOwnerRoleId() != null) {
				return entity.getOwnerRoleId();
			} else {
				return RoleIdConstant.INTERNAL;
			}

		} catch (Exception e) {
			String msg = "getEventOwnerRoleId() : Can't decide Reporting Event OwnerRoleId." + 
					"monitorId = " + monitorId + 
					", monitorDetailId = " + monitorDetailId + 
					", pluginId = " + pluginId + 
					", facilityId = " + facilityId;
			m_log.warn(msg, e);
			return RoleIdConstant.INTERNAL;
		}
	}

	@Override
	public String getStatusOwnerRoleId(String monitorId, String monitorDetailId, String pluginId, String facilityId) {
		return this.getEventOwnerRoleId(monitorId, monitorDetailId, pluginId, facilityId);
	}

}
