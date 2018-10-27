/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.vcloud.util;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.monitor.run.model.MonitorInfo;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(QueryUtil.class);
	static long fetchSize = 1000l;
	
	public static List<MonitorInfo> getMonitorInfoByMonitorTypeId(String monitorId, String monitorTypeId) {
		List<MonitorInfo> list = null;
		TypedQuery<MonitorInfo> typedQuery = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			StringBuilder jquery = new StringBuilder();
			jquery.append("SELECT a FROM MonitorInfo a WHERE true = true");
			jquery.append(" AND a.monitorTypeId like :monitorTypeId");
			// monitorId
			if (monitorId != null && "".equals(monitorId)) {
				jquery.append(" AND a.monitorId = :monitorId");
			}
			jquery.append(" ORDER BY a.monitorId, a.itemName, a.runInterval");
			
			typedQuery = em.createQuery(jquery.toString(), MonitorInfo.class);
			
			typedQuery.setParameter("monitorTypeId", monitorTypeId);
			
			// monitorId
			if (monitorId != null && "".equals(monitorId)) {
				typedQuery.setParameter("monitorId", monitorId);
			}

			list = typedQuery.getResultList();
		} catch (Exception e) {
			m_log.debug("getMonitorInfoByMonitorTypeId : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public static List<Object[]> getBillingMonitorIdByMonitorKind(String monitorId, String value) {
		List<Object[]> list = null;
		Query typedQuery = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			StringBuilder jquery = new StringBuilder();
			jquery.append("SELECT a.monitor_id");
			jquery.append(" FROM setting.cc_monitor_info a");
			jquery.append(" LEFT OUTER JOIN setting.cc_monitor_plugin_string_info b ON (a.monitor_id = b.monitor_id)");
			jquery.append(" WHERE a.monitor_type_id = 'MON_CLOUD_SERVICE_BILLING_DETAIL'");
			jquery.append(" AND b.property_key = 'MonitorKind'");
			jquery.append(" AND b.property_value = ?1");
			// monitorId
			if (monitorId != null && "".equals(monitorId)) {
				jquery.append(" AND a.monitor_id = ?2");
			}
			jquery.append(" ORDER BY a.monitor_id");

			typedQuery = em.createNativeQuery(jquery.toString());
			typedQuery.setParameter(1, value);
			typedQuery.setParameter(2, monitorId);
			
			list = (List<Object[]>)typedQuery.getResultList();
		} catch (Exception e) {
			m_log.debug("getBillingAlarmIdByMonitorTypeId : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
		}
		return list;
	}
}