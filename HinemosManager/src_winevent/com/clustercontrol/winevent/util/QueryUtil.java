/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.winevent.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.winevent.model.MonitorWinEventCategoryInfoEntity;
import com.clustercontrol.winevent.model.MonitorWinEventIdInfoEntity;
import com.clustercontrol.winevent.model.WinEventCheckInfo;
import com.clustercontrol.winevent.model.MonitorWinEventKeywordInfoEntity;
import com.clustercontrol.winevent.model.MonitorWinEventLogInfoEntity;
import com.clustercontrol.winevent.model.MonitorWinEventSourceInfoEntity;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static WinEventCheckInfo getMonitorWinEventInfoPK(String monitorId) throws MonitorNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			WinEventCheckInfo entity = em.find(WinEventCheckInfo.class, monitorId, ObjectPrivilegeMode.READ);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound("MonitorWinEventInfoEntity.findByPrimaryKey, "
						+ "monitorId = " + monitorId);
				m_log.info("getMonitorWinEventInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static List<MonitorWinEventLogInfoEntity> getMonitorWinEventLogInfoByMonitorId(String monitorId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorWinEventLogInfoEntity> list
			= em.createNamedQuery("MonitorWinEventLogInfoEntity.findByMonitorId", MonitorWinEventLogInfoEntity.class)
			.setParameter("monitorId", monitorId)
			.getResultList();
			return list;
		}
	}

	public static List<MonitorWinEventSourceInfoEntity> getMonitorWinEventSourceInfoByMonitorId(String monitorId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorWinEventSourceInfoEntity> list
			= em.createNamedQuery("MonitorWinEventSourceInfoEntity.findByMonitorId", MonitorWinEventSourceInfoEntity.class)
			.setParameter("monitorId", monitorId)
			.getResultList();
			return list;
		}
	}

	public static List<MonitorWinEventIdInfoEntity> getMonitorWinEventIdInfoByMonitorId(String monitorId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorWinEventIdInfoEntity> list
			= em.createNamedQuery("MonitorWinEventIdInfoEntity.findByMonitorId", MonitorWinEventIdInfoEntity.class)
			.setParameter("monitorId", monitorId)
			.getResultList();
			return list;
		}
	}

	public static List<MonitorWinEventCategoryInfoEntity> getMonitorWinEventCategoryInfoByMonitorId(String monitorId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorWinEventCategoryInfoEntity> list
			= em.createNamedQuery("MonitorWinEventCategoryInfoEntity.findByMonitorId", MonitorWinEventCategoryInfoEntity.class)
			.setParameter("monitorId", monitorId)
			.getResultList();
			return list;
		}
	}

	public static List<MonitorWinEventKeywordInfoEntity> getMonitorWinEventKeywordInfoByMonitorId(String monitorId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorWinEventKeywordInfoEntity> list
			= em.createNamedQuery("MonitorWinEventKeywordInfoEntity.findByMonitorId", MonitorWinEventKeywordInfoEntity.class)
			.setParameter("monitorId", monitorId)
			.getResultList();
			return list;
		}
	}

	public static void deleteRelatedEntitiesByMonitorid(String monitorId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			em.createNamedQuery("MonitorWinEventLogInfoEntity.deleteByMonitorId")
			.setParameter("monitorId", monitorId)
			.executeUpdate();
			em.createNamedQuery("MonitorWinEventSourceInfoEntity.deleteByMonitorId")
			.setParameter("monitorId", monitorId)
			.executeUpdate();
			em.createNamedQuery("MonitorWinEventIdInfoEntity.deleteByMonitorId")
			.setParameter("monitorId", monitorId)
			.executeUpdate();
			em.createNamedQuery("MonitorWinEventCategoryInfoEntity.deleteByMonitorId")
			.setParameter("monitorId", monitorId)
			.executeUpdate();
			em.createNamedQuery("MonitorWinEventKeywordInfoEntity.deleteByMonitorId")
			.setParameter("monitorId", monitorId)
			.executeUpdate();
		}
	}
}
