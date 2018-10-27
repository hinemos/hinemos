/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.analytics.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.analytics.model.CorrelationCheckInfo;
import com.clustercontrol.analytics.model.IntegrationCheckInfo;
import com.clustercontrol.analytics.model.IntegrationConditionInfo;
import com.clustercontrol.analytics.model.IntegrationConditionInfoPK;
import com.clustercontrol.analytics.model.LogcountCheckInfo;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.model.MonitorInfo;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static LogcountCheckInfo getMonitorLogcountInfoPK(String monitorId) throws MonitorNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			LogcountCheckInfo entity = em.find(LogcountCheckInfo.class, monitorId, ObjectPrivilegeMode.READ);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound("MonitorLogcountInfoEntity.findByPrimaryKey"
						+ ", monitorId = " + monitorId);
				m_log.info("getMonitorLogcountInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static CorrelationCheckInfo getMonitorCorrelationInfoPK(String monitorId) throws MonitorNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			CorrelationCheckInfo entity = em.find(CorrelationCheckInfo.class, monitorId, ObjectPrivilegeMode.READ);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound("MonitorCorrelationInfoEntity.findByPrimaryKey"
						+ ", monitorId = " + monitorId);
				m_log.info("getMonitorCorrelationInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static List<MonitorInfo> getMonitorInfoListFindByCorrelation_NONE(String monitorId){
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// 相関係数監視
			return em.createNamedQuery("MonitorInfo.findByCorrelation", MonitorInfo.class, ObjectPrivilegeMode.NONE)
			.setParameter("targetMonitorId", monitorId)
			.setParameter("referMonitorId", monitorId)
			.getResultList();
		}
	}

	public static List<MonitorInfo> getMonitorInfoListFindByIntegration_NONE(String monitorId){
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// 収集値統合監視
			return em.createNamedQuery("MonitorInfo.findByIntegration", MonitorInfo.class, ObjectPrivilegeMode.NONE)
			.setParameter("targetMonitorId", monitorId)
			.getResultList();
		}
	}

	public static List<MonitorInfo> getMonitorInfoListFindByLogcount_NONE(String monitorId){
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// 収集値統合監視
			return em.createNamedQuery("MonitorInfo.findByLogcount", MonitorInfo.class, ObjectPrivilegeMode.NONE)
			.setParameter("targetMonitorId", monitorId)
			.getResultList();
		}
	}

	public static IntegrationCheckInfo getMonitorIntegrationInfoPK(String monitorId) throws MonitorNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			IntegrationCheckInfo entity = em.find(IntegrationCheckInfo.class, monitorId, ObjectPrivilegeMode.READ);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound("MonitorIntegrationInfoEntity.findByPrimaryKey"
						+ ", monitorId = " + monitorId);
				m_log.info("getMonitorIntegrationInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static IntegrationConditionInfo getMonitorIntegrationConditionInfoPK(IntegrationConditionInfoPK pk) 
			throws MonitorNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			IntegrationConditionInfo entity = em.find(IntegrationConditionInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound("MonitorIntegrationConditionInfoEntity.findByPrimaryKey"
						+ ", pk = " + pk.toString());
				m_log.info("getMonitorIntegrationConditionInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}
}
