/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.process.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.process.model.ProcessCheckInfo;
import com.clustercontrol.process.model.MonitorProcessMethodMstEntity;
import com.clustercontrol.process.model.MonitorProcessPollingMstEntity;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static ProcessCheckInfo getMonitorProcessInfoPK(String monitorId) throws MonitorNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		ProcessCheckInfo entity = em.find(ProcessCheckInfo.class, monitorId, ObjectPrivilegeMode.READ);
		if (entity == null) {
			MonitorNotFound e = new MonitorNotFound("MonitorProcessInfoEntity.findByPrimaryKey"
					+ ", monitorId = " + monitorId);
			m_log.info("getMonitorProcessInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static List<MonitorProcessPollingMstEntity> getAllMonitorProcessPollingMst() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MonitorProcessPollingMstEntity> list
		= em.createNamedQuery("MonitorProcessPollingMstEntity.findAll", MonitorProcessPollingMstEntity.class)
		.getResultList();
		return list;
	}

	public static List<MonitorProcessMethodMstEntity> getAllMonitorProcessMethodMst() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MonitorProcessMethodMstEntity> list
		= em.createNamedQuery("MonitorProcessMethodMstEntity.findAll", MonitorProcessMethodMstEntity.class)
		.getResultList();
		return list;
	}
}
