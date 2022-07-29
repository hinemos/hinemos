/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.http.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.http.model.HttpCheckInfo;
import com.clustercontrol.http.model.HttpScenarioCheckInfo;
import com.clustercontrol.http.model.Page;
import com.clustercontrol.http.model.PagePK;
import com.clustercontrol.http.model.Pattern;
import com.clustercontrol.http.model.PatternPK;
import com.clustercontrol.http.model.Variable;
import com.clustercontrol.http.model.VariablePK;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static HttpCheckInfo getMonitorHttpInfoPK(String monitorId) throws MonitorNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			HttpCheckInfo entity = em.find(HttpCheckInfo.class, monitorId, ObjectPrivilegeMode.READ);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound("MonitorHttpInfoEntity.findByPrimaryKey"
						+ ", monitorId = " + monitorId);
				m_log.info("getMonitorHttpInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static HttpScenarioCheckInfo getMonitorHttpScenarioInfoPK(String monitorId) throws MonitorNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			HttpScenarioCheckInfo entity = em.find(HttpScenarioCheckInfo.class, monitorId, ObjectPrivilegeMode.READ);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound("MonitorHttpInfoEntity.findByPrimaryKey"
						+ ", monitorId = " + monitorId);
				m_log.info("getMonitorHttpInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static Page getPagePK(PagePK pk) throws MonitorNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			Page entity = em.find(Page.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound("PageEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.info("getPagePK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static Pattern getPatternPK(PatternPK pk) throws MonitorNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			Pattern entity = em.find(Pattern.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound("PatternEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.info("getPatternPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static Variable getVariablePK(VariablePK pk) throws MonitorNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			Variable entity = em.find(Variable.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound("VariableEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.info("getVariablePK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}
}
