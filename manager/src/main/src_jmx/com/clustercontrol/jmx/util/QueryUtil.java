/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jmx.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.jmx.model.JmxCheckInfo;
import com.clustercontrol.jmx.model.JmxMasterInfo;

/**
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static JmxCheckInfo getMonitorJmxInfoPK(String monitorId) throws MonitorNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			JmxCheckInfo entity = em.find(JmxCheckInfo.class, monitorId, ObjectPrivilegeMode.READ);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound("MonitorJmxInfoEntity.findByPrimaryKey"
						+ ", monitorId = " + monitorId);
				m_log.info("getMonitorJmxInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}
	
	public static JmxMasterInfo getJmxMasterInfoPK(String masterId) throws MonitorNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			JmxMasterInfo entity = em.find(JmxMasterInfo.class, masterId, ObjectPrivilegeMode.READ);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound("JmxMasterInfoEntity.findByPrimaryKey"
						+ ", masterId = " + masterId);
				m_log.info("getJmxMasterInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}
}
