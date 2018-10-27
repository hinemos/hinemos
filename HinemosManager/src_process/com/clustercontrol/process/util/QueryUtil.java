/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
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
	}

	public static List<MonitorProcessPollingMstEntity> getAllMonitorProcessPollingMst() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorProcessPollingMstEntity> list
			= em.createNamedQuery("MonitorProcessPollingMstEntity.findAll", MonitorProcessPollingMstEntity.class)
			.getResultList();
			return list;
		}
	}

	public static List<MonitorProcessMethodMstEntity> getAllMonitorProcessMethodMst() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorProcessMethodMstEntity> list
			= em.createNamedQuery("MonitorProcessMethodMstEntity.findAll", MonitorProcessMethodMstEntity.class)
			.getResultList();
			return list;
		}
	}
}
