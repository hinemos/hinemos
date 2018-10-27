/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.platform.util.reporting;

import java.util.List;

import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;

/**
 * 
 * for RHEL
 *
 */
public class PriorityQueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(PriorityQueryUtil.class);
	
	public static String getCollectDataListQuery(String displayName) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT a, b, c.itemCode FROM CollectKeyInfo a");
		query.append(" JOIN CollectData b ON a.collectorid = b.id.collectorid");
		query.append(" JOIN CollectorItemCodeMstEntity c ON a.id.itemName like CONCAT(c.itemName, '%')");
		query.append(" WHERE a.id.facilityid = :facilityId");
		query.append(" AND b.id.time >= :fromTime AND b.id.time <= :toTime");
		query.append(" AND a.id.monitorId = :monitorId");
		// displayName
		if (displayName != null && !"".equals(displayName)) {
			query.append(" AND a.id.displayName = :displayName");
		}
		query.append(" AND c.itemCode = :itemCode");
		query.append(" ORDER BY b.id.time, c.itemCode, a.id.displayName");

		return query.toString();
	}
	
	
	public static String getSummaryHourListQuery(String displayName) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT a, b, c.itemCode FROM CollectKeyInfo a");
		query.append(" JOIN SummaryHour b ON a.collectorid = b.id.collectorid");
		query.append(" JOIN CollectorItemCodeMstEntity c ON a.id.itemName like CONCAT(c.itemName, '%')");
		query.append(" WHERE a.id.facilityid = :facilityId");
		query.append(" AND b.id.time >= :fromTime AND b.id.time <= :toTime");
		query.append(" AND a.id.monitorId = :monitorId");
		// displayName
		if (displayName != null && !"".equals(displayName)) {
			query.append(" AND a.id.displayName = :displayName");
		}
		query.append(" AND c.itemCode = :itemCode");
		query.append(" ORDER BY b.id.time, c.itemCode, a.id.displayName");

		return query.toString();
	}

	public static String getSummaryDayListQuery(String displayName) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT a, b, c.itemCode FROM CollectKeyInfo a");
		query.append(" JOIN SummaryDay b ON a.collectorid = b.id.collectorid");
		query.append(" JOIN CollectorItemCodeMstEntity c ON a.id.itemName like CONCAT(c.itemName, '%')");
		query.append(" WHERE a.id.facilityid = :facilityId");
		query.append(" AND b.id.time >= :fromTime AND b.id.time <= :toTime");
		query.append(" AND a.id.monitorId = :monitorId");
		// displayName
		if (displayName != null && !"".equals(displayName)) {
			query.append(" AND a.id.displayName = :displayName");
		}
		query.append(" AND c.itemCode = :itemCode");
		query.append(" ORDER BY b.id.time, c.itemCode, a.id.displayName");

		return query.toString();
	}
	
	public static String getSummaryMonthListQuery(String displayName) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT a, b, c.itemCode FROM CollectKeyInfo a");
		query.append(" JOIN SummaryMonth b ON a.collectorid = b.id.collectorid");
		query.append(" JOIN CollectorItemCodeMstEntity c ON a.id.itemName like CONCAT(c.itemName, '%')");
		query.append(" WHERE a.id.facilityid = :facilityId");
		query.append(" AND b.id.time >= :fromTime AND b.id.time <= :toTime");
		query.append(" AND a.id.monitorId = :monitorId");
		// displayName
		if (displayName != null && !"".equals(displayName)) {
			query.append(" AND a.id.displayName = :displayName");
		}
		query.append(" AND c.itemCode = :itemCode");
		query.append(" ORDER BY b.id.time, c.itemCode, a.id.displayName");

		return query.toString();
	}
	

	@SuppressWarnings("unchecked")
	public static List<Object[]> getMonitorPriorityDailyList(
			String facilityId,
			Long fromTime,
			Long toTime,
			Integer daySec,
			String ownerRoleId) {
		List<Object[]> list = null;
		Query typedQuery = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			StringBuilder jquery = new StringBuilder();
			// 集計値を一度に取得しないと、データに差分が出てしまう懸念があるので、5.0と同じクエリーを発行している。
			jquery.append("WITH detail AS ("); 
			jquery.append(" SELECT DISTINCT generation_date AS d,");
			jquery.append(" COUNT(CASE WHEN priority = "+ PriorityConstant.TYPE_CRITICAL +" THEN 1 END) OVER (PARTITION BY generation_date) AS critical,");
			jquery.append(" COUNT(CASE WHEN priority = "+ PriorityConstant.TYPE_WARNING +" THEN 1 END) OVER (PARTITION BY generation_date) AS warning,");
			jquery.append(" COUNT(CASE WHEN priority = "+ PriorityConstant.TYPE_INFO +" THEN 1 END) OVER (PARTITION BY generation_date) AS info,");
			jquery.append(" COUNT(CASE WHEN priority = "+ PriorityConstant.TYPE_UNKNOWN +" THEN 1 END) OVER (PARTITION BY generation_date) AS unknown");
			jquery.append(" FROM log.cc_event_log");
			jquery.append(" WHERE facility_id = ?1");
			// ownerRoleId
			if (ownerRoleId != null && !"".equals(ownerRoleId)) {
				jquery.append(" AND owner_role_id = ?2");
			}
			jquery.append(")");
			jquery.append(" SELECT dates.d AS date, sum(critical) as critical, sum(warning) as warning, sum(info) as info, sum(unknown) as unknown");
			jquery.append(" FROM detail RIGHT OUTER JOIN");
			if (ownerRoleId != null && !"".equals(ownerRoleId)) {
				jquery.append(" (SELECT generate_series(?3, ?4 - 1, ?5) as d) dates");
				jquery.append(" ON detail.d >= dates.d AND detail.d < (dates.d + ?6)");
			} else {
				jquery.append(" (SELECT generate_series(?2, ?3 - 1, ?4) as d) dates");
				jquery.append(" ON detail.d >= dates.d AND detail.d < (dates.d + ?5)");
			}
			
			jquery.append(" GROUP BY dates.d");
			jquery.append(" ORDER BY dates.d");
			
			String execQuery = String.format(jquery.toString(), facilityId, ownerRoleId);

			typedQuery = em.createNativeQuery(execQuery.toString());
			typedQuery.setParameter(1, facilityId);
			// ownerRoleId
			if (ownerRoleId != null && !"".equals(ownerRoleId)) {
				typedQuery.setParameter(2, ownerRoleId);
				typedQuery.setParameter(3, fromTime);
				typedQuery.setParameter(4, toTime);
				typedQuery.setParameter(5, daySec);
				typedQuery.setParameter(6, daySec);
			} else {
				typedQuery.setParameter(2, fromTime);
				typedQuery.setParameter(3, toTime);
				typedQuery.setParameter(4, daySec);
				typedQuery.setParameter(5, daySec);
			}
			
			list = (List<Object[]>)typedQuery.getResultList();
			
		} catch (Exception e) {
			m_log.error("getMonitorPriorityDailyList : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}
		return list;
	}

	public static String getItemCodes(String monitorId) {
		
		StringBuilder query = new StringBuilder();
		
		if (monitorId != null && !"".equals(monitorId)) {
			
			query.append("SELECT a.itemCode FROM CollectorItemCodeMstEntity a");
			query.append(" LEFT OUTER JOIN CollectKeyInfo b ON b.id.itemName like CONCAT(a.itemName, '%')");
			query.append(" WHERE b.id.monitorId = :monitorId");
		}
		return query.toString();
	}
}