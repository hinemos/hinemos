/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;

import jakarta.persistence.Query;

/**
 * 
 * for RHEL
 *
 */
public class PriorityQueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(PriorityQueryUtil.class);

	public static String getCollectDataListQuery(String ownerRoleId) {
		StringBuilder jquery = new StringBuilder();
		jquery.append("SELECT a, b, c.itemCode FROM CollectKeyInfo a");
		jquery.append(" JOIN CollectData b ON a.collectorid = b.id.collectorid");
		jquery.append(" JOIN CollectorItemCodeMstEntity c ON (a.id.itemName like CONCAT(c.itemName, '%'))");
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			jquery.append(" JOIN MonitorInfo d ON d.monitorId = a.id.monitorId");
		}
		jquery.append(" WHERE a.id.facilityid = :facilityId");
		jquery.append(" AND b.id.time >= :fromTime AND b.id.time <= :toTime");
		jquery.append(" AND a.id.monitorId = :monitorId");
		jquery.append(" AND c.itemCode IN :itemCodeList");
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			jquery.append(" AND (d.ownerRoleId = :ownerRoleId");
			jquery.append(" OR EXISTS (");
			jquery.append(" SELECT e FROM ObjectPrivilegeInfo e");
			jquery.append(" WHERE e.id.objectType = :objectType");
			jquery.append(" AND e.id.objectId = d.monitorId");
			jquery.append(" AND e.id.roleId = :ownerRoleId");
			jquery.append(" AND e.id.objectPrivilege = :objectPrivilege");
			jquery.append(" )");
			jquery.append(" )");
		}
		jquery.append(" ORDER BY a.id.facilityid, c.itemCode, a.id.displayName, b.id.time");

		return jquery.toString();
	}

	public static String getCollectDataListQuery(String displayName, String ownerRoleId) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT a, b, c.itemCode FROM CollectKeyInfo a");
		query.append(" JOIN CollectData b ON a.collectorid = b.id.collectorid");
		query.append(" JOIN CollectorItemCodeMstEntity c ON (a.id.itemName like CONCAT(c.itemName, '%'))");
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			query.append(" JOIN MonitorInfo d ON d.monitorId = a.id.monitorId");
		}
		query.append(" WHERE a.id.facilityid = :facilityId");
		query.append(" AND b.id.time >= :fromTime AND b.id.time <= :toTime");
		query.append(" AND a.id.monitorId = :monitorId");
		// displayName
		if (displayName != null && !"".equals(displayName)) {
			query.append(" AND a.id.displayName = :displayName");
		}
		query.append(" AND c.itemCode = :itemCode");
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			query.append(" AND (d.ownerRoleId = :ownerRoleId");
			query.append(" OR EXISTS (");
			query.append(" SELECT e FROM ObjectPrivilegeInfo e");
			query.append(" WHERE e.id.objectType = :objectType");
			query.append(" AND e.id.objectId = d.monitorId");
			query.append(" AND e.id.roleId = :ownerRoleId");
			query.append(" AND e.id.objectPrivilege = :objectPrivilege");
			query.append(" )");
			query.append(" )");
		}
		query.append(" ORDER BY b.id.time, c.itemCode, a.id.displayName");

		return query.toString();
	}

	public static String getSummaryPrefAvgDataQuery(String ownerRoleId) {
		StringBuilder jquery = new StringBuilder();
		jquery.append("SELECT a.id.monitorId, c.itemCode, a.id.itemName, b.id.time, a.id.facilityid, SUM(b.value), a.id.displayName FROM CollectKeyInfo a");
		jquery.append(" JOIN CollectData b ON a.collectorid = b.id.collectorid");
		jquery.append(" JOIN CollectorItemCodeMstEntity c ON (a.id.itemName like CONCAT(c.itemName, '%'))");
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			jquery.append(" JOIN MonitorInfo d ON d.monitorId = a.id.monitorId");
		}
		jquery.append(" WHERE a.id.facilityid = :facilityId");
		jquery.append(" AND b.id.time >= :fromTime AND b.id.time <= :toTime");
		jquery.append(" AND a.id.monitorId = :monitorId");
		jquery.append(" AND c.itemCode IN :itemCodeList");
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			jquery.append(" AND (d.ownerRoleId = :ownerRoleId");
			jquery.append(" OR EXISTS (");
			jquery.append(" SELECT e FROM ObjectPrivilegeInfo e");
			jquery.append(" WHERE e.id.objectType = :objectType");
			jquery.append(" AND e.id.objectId = d.monitorId");
			jquery.append(" AND e.id.roleId = :ownerRoleId");
			jquery.append(" AND e.id.objectPrivilege = :objectPrivilege");
			jquery.append(" )");
			jquery.append(" )");
		}
		jquery.append(" GROUP BY a.id.monitorId, c.itemCode, a.id.itemName, a.id.displayName, b.id.time, a.id.facilityid");
		jquery.append(" ORDER BY a.id.facilityid, c.itemCode, a.id.displayName, b.id.time");

		return jquery.toString();
	}

	public static String getSummaryHourListQuery(String ownerRoleId) {
		StringBuilder jquery = new StringBuilder();
		jquery.append("SELECT a, b, c.itemCode FROM CollectKeyInfo a");
		jquery.append(" JOIN SummaryHour b ON a.collectorid = b.id.collectorid");
		jquery.append(" JOIN CollectorItemCodeMstEntity c ON (a.id.itemName like CONCAT(c.itemName, '%'))");
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			jquery.append(" JOIN MonitorInfo d ON d.monitorId = a.id.monitorId");
		}
		jquery.append(" WHERE a.id.facilityid = :facilityId");
		jquery.append(" AND b.id.time >= :fromTime AND b.id.time <= :toTime");
		jquery.append(" AND a.id.monitorId = :monitorId");
		jquery.append(" AND c.itemCode IN :itemCodeList");
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			jquery.append(" AND (d.ownerRoleId = :ownerRoleId");
			jquery.append(" OR EXISTS (");
			jquery.append(" SELECT e FROM ObjectPrivilegeInfo e");
			jquery.append(" WHERE e.id.objectType = :objectType");
			jquery.append(" AND e.id.objectId = d.monitorId");
			jquery.append(" AND e.id.roleId = :ownerRoleId");
			jquery.append(" AND e.id.objectPrivilege = :objectPrivilege");
			jquery.append(" )");
			jquery.append(" )");
		}
		jquery.append(" ORDER BY a.id.facilityid, c.itemCode, a.id.displayName, b.id.time");

		return jquery.toString();
	}

	public static String getSummaryHourListQuery(String displayName, String ownerRoleId) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT a, b, c.itemCode FROM CollectKeyInfo a");
		query.append(" JOIN SummaryHour b ON a.collectorid = b.id.collectorid");
		query.append(" JOIN CollectorItemCodeMstEntity c ON (a.id.itemName like CONCAT(c.itemName, '%'))");
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			query.append(" JOIN MonitorInfo d ON d.monitorId = a.id.monitorId");
		}
		query.append(" WHERE a.id.facilityid = :facilityId");
		query.append(" AND b.id.time >= :fromTime AND b.id.time <= :toTime");
		query.append(" AND a.id.monitorId = :monitorId");
		// displayName
		if (displayName != null && !"".equals(displayName)) {
			query.append(" AND a.id.displayName = :displayName");
		}
		query.append(" AND c.itemCode = :itemCode");
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			query.append(" AND (d.ownerRoleId = :ownerRoleId");
			query.append(" OR EXISTS (");
			query.append(" SELECT e FROM ObjectPrivilegeInfo e");
			query.append(" WHERE e.id.objectType = :objectType");
			query.append(" AND e.id.objectId = d.monitorId");
			query.append(" AND e.id.roleId = :ownerRoleId");
			query.append(" AND e.id.objectPrivilege = :objectPrivilege");
			query.append(" )");
			query.append(" )");
		}
		query.append(" ORDER BY b.id.time, c.itemCode, a.id.displayName");

		return query.toString();
	}

	public static String getSummaryPrefAvgHourQuery(String ownerRoleId) {
		StringBuilder jquery = new StringBuilder();
		jquery.append("SELECT a.id.monitorId, c.itemCode, a.id.itemName, b.id.time, a.id.facilityid, SUM(b.avg), a.id.displayName FROM CollectKeyInfo a");
		jquery.append(" JOIN SummaryHour b ON a.collectorid = b.id.collectorid");
		jquery.append(" JOIN CollectorItemCodeMstEntity c ON (a.id.itemName like CONCAT(c.itemName, '%'))");
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			jquery.append(" JOIN MonitorInfo d ON d.monitorId = a.id.monitorId");
		}
		jquery.append(" WHERE a.id.facilityid = :facilityId");
		jquery.append(" AND b.id.time >= :fromTime AND b.id.time <= :toTime");
		jquery.append(" AND a.id.monitorId = :monitorId");
		jquery.append(" AND c.itemCode IN :itemCodeList");
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			jquery.append(" AND (d.ownerRoleId = :ownerRoleId");
			jquery.append(" OR EXISTS (");
			jquery.append(" SELECT e FROM ObjectPrivilegeInfo e");
			jquery.append(" WHERE e.id.objectType = :objectType");
			jquery.append(" AND e.id.objectId = d.monitorId");
			jquery.append(" AND e.id.roleId = :ownerRoleId");
			jquery.append(" AND e.id.objectPrivilege = :objectPrivilege");
			jquery.append(" )");
			jquery.append(" )");
		}
		jquery.append(" GROUP BY a.id.monitorId, c.itemCode, a.id.itemName, a.id.displayName, b.id.time, a.id.facilityid");
		jquery.append(" ORDER BY a.id.facilityid, c.itemCode, a.id.displayName, b.id.time");

		return jquery.toString();
	}

	public static String getSummaryDayListQuery(String ownerRoleId) {
		StringBuilder jquery = new StringBuilder();
		jquery.append("SELECT a, b, c.itemCode FROM CollectKeyInfo a");
		jquery.append(" JOIN SummaryDay b ON a.collectorid = b.id.collectorid");
		jquery.append(" JOIN CollectorItemCodeMstEntity c ON (a.id.itemName like CONCAT(c.itemName, '%'))");
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			jquery.append(" JOIN MonitorInfo d ON d.monitorId = a.id.monitorId");
		}
		jquery.append(" WHERE a.id.facilityid = :facilityId");
		jquery.append(" AND b.id.time >= :fromTime AND b.id.time <= :toTime");
		jquery.append(" AND a.id.monitorId = :monitorId");
		jquery.append(" AND c.itemCode IN :itemCodeList");
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			jquery.append(" AND (d.ownerRoleId = :ownerRoleId");
			jquery.append(" OR EXISTS (");
			jquery.append(" SELECT e FROM ObjectPrivilegeInfo e");
			jquery.append(" WHERE e.id.objectType = :objectType");
			jquery.append(" AND e.id.objectId = d.monitorId");
			jquery.append(" AND e.id.roleId = :ownerRoleId");
			jquery.append(" AND e.id.objectPrivilege = :objectPrivilege");
			jquery.append(" )");
			jquery.append(" )");
		}
		jquery.append(" ORDER BY a.id.facilityid, c.itemCode, a.id.displayName, b.id.time");

		return jquery.toString();
	}

	public static String getSummaryDayListQuery(String displayName, String ownerRoleId) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT a, b, c.itemCode FROM CollectKeyInfo a");
		query.append(" JOIN SummaryDay b ON a.collectorid = b.id.collectorid");
		query.append(" JOIN CollectorItemCodeMstEntity c ON (a.id.itemName like CONCAT(c.itemName, '%'))");
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			query.append(" JOIN MonitorInfo d ON d.monitorId = a.id.monitorId");
		}
		query.append(" WHERE a.id.facilityid = :facilityId");
		query.append(" AND b.id.time >= :fromTime AND b.id.time <= :toTime");
		query.append(" AND a.id.monitorId = :monitorId");
		// displayName
		if (displayName != null && !"".equals(displayName)) {
			query.append(" AND a.id.displayName = :displayName");
		}
		query.append(" AND c.itemCode = :itemCode");
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			query.append(" AND (d.ownerRoleId = :ownerRoleId");
			query.append(" OR EXISTS (");
			query.append(" SELECT e FROM ObjectPrivilegeInfo e");
			query.append(" WHERE e.id.objectType = :objectType");
			query.append(" AND e.id.objectId = d.monitorId");
			query.append(" AND e.id.roleId = :ownerRoleId");
			query.append(" AND e.id.objectPrivilege = :objectPrivilege");
			query.append(" )");
			query.append(" )");
		}
		query.append(" ORDER BY b.id.time, c.itemCode, a.id.displayName");

		return query.toString();
	}

	public static String getSummaryPrefAvgDayQuery(String ownerRoleId) {
		StringBuilder jquery = new StringBuilder();
		jquery.append("SELECT a.id.monitorId, c.itemCode, a.id.itemName, b.id.time, a.id.facilityid, SUM(b.avg), a.id.displayName FROM CollectKeyInfo a");
		jquery.append(" JOIN SummaryDay b ON a.collectorid = b.id.collectorid");
		jquery.append(" JOIN CollectorItemCodeMstEntity c ON (a.id.itemName like CONCAT(c.itemName, '%'))");
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			jquery.append(" JOIN MonitorInfo d ON d.monitorId = a.id.monitorId");
		}
		jquery.append(" WHERE a.id.facilityid = :facilityId");
		jquery.append(" AND b.id.time >= :fromTime AND b.id.time <= :toTime");
		jquery.append(" AND a.id.monitorId = :monitorId");
		jquery.append(" AND c.itemCode IN :itemCodeList");
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			jquery.append(" AND (d.ownerRoleId = :ownerRoleId");
			jquery.append(" OR EXISTS (");
			jquery.append(" SELECT e FROM ObjectPrivilegeInfo e");
			jquery.append(" WHERE e.id.objectType = :objectType");
			jquery.append(" AND e.id.objectId = d.monitorId");
			jquery.append(" AND e.id.roleId = :ownerRoleId");
			jquery.append(" AND e.id.objectPrivilege = :objectPrivilege");
			jquery.append(" )");
			jquery.append(" )");
		}
		jquery.append(" GROUP BY a.id.monitorId, c.itemCode, a.id.itemName, a.id.displayName, b.id.time, a.id.facilityid");
		jquery.append(" ORDER BY a.id.facilityid, c.itemCode, a.id.displayName, b.id.time");

		return jquery.toString();
	}

	public static String getSummaryMonthListQuery(String ownerRoleId) {
		StringBuilder jquery = new StringBuilder();
		jquery.append("SELECT a, b, c.itemCode FROM CollectKeyInfo a");
		jquery.append(" JOIN SummaryMonth b ON a.collectorid = b.id.collectorid");
		jquery.append(" JOIN CollectorItemCodeMstEntity c ON (a.id.itemName like CONCAT(c.itemName, '%'))");
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			jquery.append(" JOIN MonitorInfo d ON d.monitorId = a.id.monitorId");
		}
		jquery.append(" WHERE a.id.facilityid = :facilityId");
		jquery.append(" AND b.id.time >= :fromTime AND b.id.time <= :toTime");
		jquery.append(" AND a.id.monitorId = :monitorId");
		jquery.append(" AND c.itemCode IN :itemCodeList");
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			jquery.append(" AND (d.ownerRoleId = :ownerRoleId");
			jquery.append(" OR EXISTS (");
			jquery.append(" SELECT e FROM ObjectPrivilegeInfo e");
			jquery.append(" WHERE e.id.objectType = :objectType");
			jquery.append(" AND e.id.objectId = d.monitorId");
			jquery.append(" AND e.id.roleId = :ownerRoleId");
			jquery.append(" AND e.id.objectPrivilege = :objectPrivilege");
			jquery.append(" )");
			jquery.append(" )");
		}
		jquery.append(" ORDER BY a.id.facilityid, c.itemCode, a.id.displayName, b.id.time");

		return jquery.toString();
	}

	public static String getSummaryMonthListQuery(String displayName, String ownerRoleId) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT a, b, c.itemCode FROM CollectKeyInfo a");
		query.append(" JOIN SummaryMonth b ON a.collectorid = b.id.collectorid");
		query.append(" JOIN CollectorItemCodeMstEntity c ON (a.id.itemName like CONCAT(c.itemName, '%'))");
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			query.append(" JOIN MonitorInfo d ON d.monitorId = a.id.monitorId");
		}
		query.append(" WHERE a.id.facilityid = :facilityId");
		query.append(" AND b.id.time >= :fromTime AND b.id.time <= :toTime");
		query.append(" AND a.id.monitorId = :monitorId");
		// displayName
		if (displayName != null && !"".equals(displayName)) {
			query.append(" AND a.id.displayName = :displayName");
		}
		query.append(" AND c.itemCode = :itemCode");
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			query.append(" AND (d.ownerRoleId = :ownerRoleId");
			query.append(" OR EXISTS (");
			query.append(" SELECT e FROM ObjectPrivilegeInfo e");
			query.append(" WHERE e.id.objectType = :objectType");
			query.append(" AND e.id.objectId = d.monitorId");
			query.append(" AND e.id.roleId = :ownerRoleId");
			query.append(" AND e.id.objectPrivilege = :objectPrivilege");
			query.append(" )");
			query.append(" )");
		}
		query.append(" ORDER BY b.id.time, c.itemCode, a.id.displayName");

		return query.toString();
	}

	public static String getSummaryPrefAvgMonthQuery(String ownerRoleId) {
		StringBuilder jquery = new StringBuilder();
		jquery.append("SELECT a.id.monitorId, c.itemCode, a.id.itemName, b.id.time, a.id.facilityid, SUM(b.avg), a.id.displayName FROM CollectKeyInfo a");
		jquery.append(" JOIN SummaryMonth b ON a.collectorid = b.id.collectorid");
		jquery.append(" JOIN CollectorItemCodeMstEntity c ON (a.id.itemName like CONCAT(c.itemName, '%'))");
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			jquery.append(" JOIN MonitorInfo d ON d.monitorId = a.id.monitorId");
		}
		jquery.append(" WHERE a.id.facilityid = :facilityId");
		jquery.append(" AND b.id.time >= :fromTime AND b.id.time <= :toTime");
		jquery.append(" AND a.id.monitorId = :monitorId");
		jquery.append(" AND c.itemCode IN :itemCodeList");
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			jquery.append(" AND (d.ownerRoleId = :ownerRoleId");
			jquery.append(" OR EXISTS (");
			jquery.append(" SELECT e FROM ObjectPrivilegeInfo e");
			jquery.append(" WHERE e.id.objectType = :objectType");
			jquery.append(" AND e.id.objectId = d.monitorId");
			jquery.append(" AND e.id.roleId = :ownerRoleId");
			jquery.append(" AND e.id.objectPrivilege = :objectPrivilege");
			jquery.append(" )");
			jquery.append(" )");
		}
		jquery.append(" GROUP BY a.id.monitorId, c.itemCode, a.id.itemName, a.id.displayName, b.id.time, a.id.facilityid");
		jquery.append(" ORDER BY a.id.facilityid, c.itemCode, a.id.displayName, b.id.time");

		return jquery.toString();
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
			jquery.append(" SELECT DISTINCT a.generation_date AS d,");
			jquery.append(" COUNT(CASE WHEN a.priority = "+ PriorityConstant.TYPE_CRITICAL +" THEN 1 END) OVER (PARTITION BY generation_date) AS critical,");
			jquery.append(" COUNT(CASE WHEN a.priority = "+ PriorityConstant.TYPE_WARNING +" THEN 1 END) OVER (PARTITION BY generation_date) AS warning,");
			jquery.append(" COUNT(CASE WHEN a.priority = "+ PriorityConstant.TYPE_INFO +" THEN 1 END) OVER (PARTITION BY generation_date) AS info,");
			jquery.append(" COUNT(CASE WHEN a.priority = "+ PriorityConstant.TYPE_UNKNOWN +" THEN 1 END) OVER (PARTITION BY generation_date) AS unknown");
			jquery.append(" FROM log.cc_event_log a");
			jquery.append(" WHERE a.facility_id = ?1");
			// ownerRoleId
			if (ownerRoleId != null && !"".equals(ownerRoleId)) {
				jquery.append(" AND (a.owner_role_id = ?2");
				jquery.append(" OR EXISTS (");
				jquery.append(" SELECT b FROM setting.cc_object_privilege b");
				jquery.append(" WHERE b.object_type = ?7");
				jquery.append(" AND b.object_id = a.monitor_id");
				jquery.append(" AND b.role_id = ?2");
				jquery.append(" AND b.object_privilege = ?8");
				jquery.append(" )");
				jquery.append(" )");
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
				typedQuery.setParameter(7, HinemosModuleConstant.MONITOR);
				typedQuery.setParameter(8, ObjectPrivilegeMode.READ.name());
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