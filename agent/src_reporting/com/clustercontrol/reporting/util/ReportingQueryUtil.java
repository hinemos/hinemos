/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.collect.bean.SummaryTypeConstant;
import com.clustercontrol.collect.model.CollectData;
import com.clustercontrol.collect.model.CollectDataPK;
import com.clustercontrol.collect.model.CollectKeyInfo;
import com.clustercontrol.collect.model.CollectKeyInfoPK;
import com.clustercontrol.collect.model.SummaryDay;
import com.clustercontrol.collect.model.SummaryHour;
import com.clustercontrol.collect.model.SummaryMonth;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.CollectKeyNotFound;
import com.clustercontrol.fault.CollectorNotFound;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobInfoEntityPK;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.performance.monitor.model.CollectorItemCodeMstEntity;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.platform.QueryExecutor;
import com.clustercontrol.platform.util.reporting.PriorityQueryUtil;
import com.clustercontrol.reporting.ReportUtil;
import com.clustercontrol.reporting.session.ReportingCollectControllerBean;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.FacilityRelationEntity;

public class ReportingQueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(ReportingQueryUtil.class);
	static long fetchSize = 1000l;
	
	public static CollectKeyInfo getCollectKeyPK(CollectKeyInfoPK pk, ObjectPrivilegeMode privliegeMode){
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			CollectKeyInfo entity=null;
			entity = em.find(CollectKeyInfo.class, pk, privliegeMode);
			return entity;
		}
	}
	
	public static List<CollectKeyInfo> getReportCollectKeyList(String monitorId, String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<CollectKeyInfo> list = em.createNamedQuery("ReportingCollectKeyInfo.findByMonitorId", CollectKeyInfo.class)
					.setParameter("monitorId", monitorId)
					.setParameter("facilityid", facilityId)
					.getResultList();
			return list;
		}
	}
	
	public static SummaryHour getSummaryHour(CollectDataPK pk, ObjectPrivilegeMode privliegeMode)
			throws CollectKeyNotFound{
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			SummaryHour entity = null;
			entity = em.find(SummaryHour.class, pk, privliegeMode);
			if(entity == null){
				CollectKeyNotFound e = new CollectKeyNotFound("getSummaryHour, "+"collectorId = "+pk.getCollectorid()+" time = "+pk.getTime());
				throw e;
			}
			return entity;
		}
	}
	
	public static SummaryDay getSummaryDay(CollectDataPK pk, ObjectPrivilegeMode privliegeMode)
			throws CollectKeyNotFound{
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			SummaryDay entity = null;
			entity = em.find(SummaryDay.class, pk, privliegeMode);
			if(entity == null){	
				CollectKeyNotFound e = new CollectKeyNotFound("getSummaryDay, "+"collectorId = "+pk.getCollectorid()+" time = "+pk.getTime());
				throw e;
			}
			return entity;
		}
	}
	
	public static SummaryMonth getSummaryMonth(CollectDataPK pk, ObjectPrivilegeMode privliegeMode)
			throws CollectKeyNotFound{
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			SummaryMonth entity = null;
			entity = em.find(SummaryMonth.class, pk, privliegeMode);
			if(entity == null){	
				CollectKeyNotFound e = new CollectKeyNotFound("getSummaryMonth, "+"collectorId = "+pk.getCollectorid()+" time = "+pk.getTime());
				throw e;
			}
			return entity;
		}
	}
	
	public static List<CollectData> getCollectDataList(List<Integer> idList, Long fromTime, Long toTime) 
			throws HinemosDbTimeout {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collectoridList", idList);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		List<CollectData> list = QueryExecutor.getListByQueryNameWithTimeout("CollectData.findByTime", CollectData.class, parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
		return list;
	}
	
	public static List<Object[]> getCollectDataList(String facilityId, Long fromTime, Long toTime, String monitorId, String displayName, String itemCode) 
			throws HinemosDbTimeout {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("facilityId", facilityId);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		parameters.put("monitorId", monitorId);
		
		// displayName
		if (displayName != null && !"".equals(displayName)) {
			parameters.put("displayName", displayName);
		}
		parameters.put("itemCode", itemCode);

		return QueryExecutor.getListByJpqlWithTimeout(PriorityQueryUtil.getCollectDataListQuery(displayName), Object[].class, parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
	}
	
	public static List<CollectData> getCollectDataList(Integer id) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<CollectData> list = em.createNamedQuery("CollectData.find", CollectData.class)
					.setHint("eclipselink.jdbc.fetch-size", fetchSize)
					.setParameter("collectorid", id)
					.getResultList();
			return list;
		}
	}
	
	public static List<SummaryHour> getSummaryHourList(List<Integer> idList, Long fromTime, Long toTime) 
			throws HinemosDbTimeout {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collectoridList", idList);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		List<SummaryHour> list = QueryExecutor.getListByQueryNameWithTimeout("SummaryHour.findByTime", SummaryHour.class, parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
		
		return list;
	}
	
	public static List<Object[]> getSummaryHourList(String facilityId, Long fromTime, Long toTime, String monitorId, String displayName, String itemCode) 
			throws HinemosDbTimeout {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("facilityId", facilityId);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		parameters.put("monitorId", monitorId);
		// displayName
		if (displayName != null && !"".equals(displayName)) {
			parameters.put("displayName", displayName);
		}
		parameters.put("itemCode", itemCode);

		return QueryExecutor.getListByJpqlWithTimeout(PriorityQueryUtil.getSummaryHourListQuery(displayName), Object[].class, parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
	}
	
	public static List<SummaryHour> getSummaryHourList(Integer id) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<SummaryHour> list = em.createNamedQuery("SummaryHour.find", SummaryHour.class)
					.setHint("eclipselink.jdbc.fetch-size", fetchSize)
					.setParameter("collectorid", id)
					.getResultList();
			return list;
		}
	}
	
	public static List<SummaryDay> getSummaryDayList(List<Integer> collectidList, Long fromTime, Long toTime) 
			throws HinemosDbTimeout {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collectoridList", collectidList);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		List<SummaryDay> list = QueryExecutor.getListByQueryNameWithTimeout("SummaryDay.findByTime", SummaryDay.class, parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
		return list;
	}
	
	public static List<Object[]> getSummaryDayList(String facilityId, Long fromTime, Long toTime, String monitorId, String displayName, String itemCode) 
			throws HinemosDbTimeout {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("facilityId", facilityId);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		parameters.put("monitorId", monitorId);
		// displayName
		if (displayName != null && !"".equals(displayName)) {
			parameters.put("displayName", displayName);
		}
		parameters.put("itemCode", itemCode);

		return QueryExecutor.getListByJpqlWithTimeout(PriorityQueryUtil.getSummaryDayListQuery(displayName), Object[].class, parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
	}
	
	public static List<SummaryDay> getSummaryDayList(Integer id) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<SummaryDay> list = em.createNamedQuery("SummaryDay.find", SummaryDay.class)
					.setHint("eclipselink.jdbc.fetch-size", fetchSize)
					.setParameter("collectorid", id)
					.getResultList();
			return list;
		}
	}
	
	public static List<SummaryMonth> getSummaryMonthList(List<Integer> idList, Long fromTime, Long toTime) 
			throws HinemosDbTimeout {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collectoridList", idList);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);

		List<SummaryMonth> list = QueryExecutor.getListByQueryNameWithTimeout("SummaryMonth.findByTime", SummaryMonth.class, parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
		
		return list;
	}
	
	public static List<Object[]> getSummaryMonthList(String facilityId, Long fromTime, Long toTime, String monitorId, String displayName, String itemCode) 
			throws HinemosDbTimeout {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("facilityId", facilityId);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		parameters.put("monitorId", monitorId);
		// displayName
		if (displayName != null && !"".equals(displayName)) {
			parameters.put("displayName", displayName);
		}
		parameters.put("itemCode", itemCode);

		return QueryExecutor.getListByJpqlWithTimeout(PriorityQueryUtil.getSummaryMonthListQuery(displayName), Object[].class, parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
	}
	
	public static List<SummaryMonth> getSummaryMonthList(Integer id) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<SummaryMonth> list = em.createNamedQuery("SummaryMonth.find", SummaryMonth.class)
					.setHint("eclipselink.jdbc.fetch-size", fetchSize)
					.setParameter("collectorid", id)
					.getResultList();
			return list;
		}
	}
	
	public static List<CollectKeyInfo> getReportingCollectKeyInfoList(String itemName, String displayName, String monitorid, String facilityid) {
		List<CollectKeyInfo> list = null;
		TypedQuery<CollectKeyInfo> typedQuery = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			StringBuilder jquery = new StringBuilder();
			jquery.append("SELECT a FROM CollectKeyInfo a WHERE true = true");
			// itemname
			if (itemName != null && !"".equals(itemName)) {
				jquery.append(" AND a.id.itemName like :itemName");
			}
			// displayName
			if (displayName != null && !"".equals(displayName)) {
				jquery.append(" AND a.id.displayName = :displayName");
			}
			// monitorid
			if (monitorid != null && !"".equals(monitorid)) {
				jquery.append(" AND a.id.monitorId = :monitorId");
			}
			// facilityid
			if (facilityid != null && !"".equals(facilityid)) {
				jquery.append(" AND a.id.facilityid = :facilityid");
			}
			jquery.append(" ORDER BY a.id.itemName, a.id.displayName");
			
			typedQuery = em.createQuery(jquery.toString(), CollectKeyInfo.class);
			
			// itemName
			if (itemName != null && !"".equals(itemName)) {
				typedQuery.setParameter("itemName", itemName + "%");
			}
			// displayName
			if (displayName != null && !"".equals(displayName)) {
				typedQuery.setParameter("displayName", displayName);
			}
			// monitorid
			if (monitorid != null && !"".equals(monitorid)) {
				typedQuery.setParameter("monitorId", monitorid);
			}
			// facilityid
			if (facilityid != null && !"".equals(facilityid)) {
				typedQuery.setParameter("facilityid", facilityid);
			}
			list = typedQuery.getResultList();
		} catch (Exception e) {
			m_log.debug("getReportingCollectKeyInfoList : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
		}
		return list;
	}
	
	public static List<String> getFacilityId(Integer collectorid){
		List<String> list = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			list = em.createNamedQuery("ReportingCollectKeyInfo.findFacilityId", String.class)
					.setParameter("collectorid", collectorid)
					.getResultList();
		}catch(Exception e){
			m_log.debug("getFacilityId : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
		}
		return list;
		
	}

	public static MonitorInfo getMonitorInfoPK(String monitorId, ObjectPrivilegeMode privliegeMode) throws MonitorNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			MonitorInfo entity
			= em.find(MonitorInfo.class, monitorId, privliegeMode);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound("MonitorInfo.findByPrimaryKey"
						+ ", monitorId = " + monitorId);
				m_log.info("getMonitorInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static List<Object[]> getMonitorDataList(String itemFilter, String facilityId, Long fromTime, Long toTime, String ownerRoleId) throws HinemosDbTimeout, NullPointerException {
		StringBuilder query = new StringBuilder();
		query.append("SELECT ids.monitor_id, ids.item_code, d.run_interval, ids.display_name, ids.breakdown_flg ");
		query.append("FROM (");
		query.append(" SELECT a.monitor_id, c.item_code, a.display_name, c.breakdown_flg ");
		query.append(" FROM log.cc_collect_key a");
		query.append(" 	LEFT OUTER JOIN log.cc_collect_data_raw b ON (a.collector_id = b.collector_id) ");
		query.append(" 	LEFT OUTER JOIN setting.cc_monitor_perf_info c ON (a.monitor_id = c.monitor_id) ");
		query.append(" WHERE c.item_code like ?1");
		query.append(" AND a.facility_id = ?2");
		query.append(" AND b.time >= ?3 ");
		query.append(" AND b.time < ?4 ");
		query.append(" GROUP BY a.monitor_id, c.item_code, a.display_name, c.breakdown_flg");
		query.append(" ) AS ids");
		query.append(" LEFT OUTER JOIN setting.cc_monitor_info d ON (ids.monitor_id = d.monitor_id) ");
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			query.append(" WHERE d.owner_role_id = ?5 ");
		}
		query.append(" ORDER BY ids.item_code, ids.display_name, d.run_interval");
		
		Map<Integer, Object> parameters = new HashMap<Integer, Object>();
		// itemFilter
		parameters.put(1, itemFilter);
		// facilityId
		parameters.put(2, facilityId);
		// fromTime
		parameters.put(3, fromTime);
		// toTime
		parameters.put(4, toTime);
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			parameters.put(5, ownerRoleId);
		}
		return QueryExecutor.getListByNativeQueryWithTimeout(query.toString(), parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
	}
	
	public static List<Object[]> getMonitorHourList(String itemFilter, String facilityId, Long fromTime, Long toTime, String ownerRoleId) throws HinemosDbTimeout, NullPointerException {
		StringBuilder query = new StringBuilder();
		query.append("SELECT ids.monitor_id, ids.item_code, d.run_interval, ids.display_name, ids.breakdown_flg");
		query.append(" FROM (");
		query.append(" SELECT a.monitor_id, c.item_code, a.display_name, c.breakdown_flg ");
		query.append(" FROM log.cc_collect_key a ");
		query.append(" LEFT OUTER JOIN log.cc_collect_summary_hour b ON (a.collector_id = b.collector_id) ");
		query.append(" LEFT OUTER JOIN setting.cc_monitor_perf_info c ON (a.monitor_id = c.monitor_id) ");
		query.append(" WHERE c.item_code like ?1");
		query.append(" AND a.facility_id = ?2");
		query.append(" AND b.time >= ?3 ");
		query.append("	AND b.time < ?4 ");
		query.append(" GROUP BY a.monitor_id, c.item_code, a.display_name, c.breakdown_flg");
		query.append(" ) AS ids");
		query.append(" JOIN setting.cc_monitor_info d ON (ids.monitor_id = d.monitor_id)");
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			query.append(" WHERE d.owner_role_id = ?5 ");
		}
		query.append(" ORDER BY ids.item_code, ids.display_name, d.run_interval");

		Map<Integer, Object> parameters = new HashMap<Integer, Object>();
		// itemFilter
		parameters.put(1, itemFilter);
		// facilityId
		parameters.put(2, facilityId);
		// fromTime
		parameters.put(3, fromTime);
		// toTime
		parameters.put(4, toTime);
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			parameters.put(5, ownerRoleId);
		}
		return QueryExecutor.getListByNativeQueryWithTimeout(query.toString(), parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
	}
	
	public static List<Object[]> getMonitorDayList(String itemFilter, String facilityId, Long fromTime, Long toTime, String ownerRoleId) throws HinemosDbTimeout, NullPointerException {
		StringBuilder query = new StringBuilder();
		query.append("SELECT ids.monitor_id, ids.item_code, d.run_interval, ids.display_name, ids.breakdown_flg ");
		query.append(" FROM (");
		query.append(" SELECT a.monitor_id, c.item_code, a.display_name, c.breakdown_flg ");
		query.append(" FROM log.cc_collect_key a ");
		query.append(" LEFT OUTER JOIN log.cc_collect_summary_day b ON (a.collector_id = b.collector_id)");
		query.append(" LEFT OUTER JOIN setting.cc_monitor_perf_info c ON (a.monitor_id = c.monitor_id)");
		query.append(" WHERE item_code like ?1 ");
		query.append(" AND facility_id = ?2");
		query.append(" AND time >= ?3 AND time < ?4");
		query.append(" GROUP BY a.monitor_id, c.item_code, a.display_name, c.breakdown_flg");
		query.append(" ) AS ids");
		query.append(" LEFT OUTER JOIN setting.cc_monitor_info d ON (ids.monitor_id = d.monitor_id)");
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			query.append(" WHERE owner_role_id = ?5");
		}
		query.append(" ORDER BY item_code, display_name, run_interval");
		
		Map<Integer, Object> parameters = new HashMap<Integer, Object>();
		// itemFilter
		parameters.put(1, itemFilter);
		// facilityId
		parameters.put(2, facilityId);
		// fromTime
		parameters.put(3, fromTime);
		// toTime
		parameters.put(4, toTime);
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			parameters.put(5, ownerRoleId);
		}
		return QueryExecutor.getListByNativeQueryWithTimeout(query.toString(), parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
	}
	
	public static List<Object[]> getMonitorMonthList(String itemFilter, String facilityId, Long fromTime, Long toTime, String ownerRoleId) throws HinemosDbTimeout, NullPointerException {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT ids.monitor_id, ids.item_code, d.run_interval, ids.display_name, ids.breakdown_flg ");
		query.append(" FROM (");
		query.append(" SELECT a.monitor_id, c.item_code, a.display_name, c.breakdown_flg ");
		query.append(" FROM log.cc_collect_key a ");
		query.append(" LEFT OUTER JOIN log.cc_collect_summary_month b ON (a.collector_id = b.collector_id) ");
		query.append(" LEFT OUTER JOIN setting.cc_monitor_perf_info c ON (a.monitor_id = c.monitor_id)  ");
		query.append(" WHERE item_code like ?1 ");
		query.append(" AND facility_id = ?2");
		query.append(" AND time >= ?3 AND time < ?4");
		query.append(" GROUP BY a.monitor_id, c.item_code, a.display_name, c.breakdown_flg) AS ids");
		query.append(" LEFT OUTER JOIN setting.cc_monitor_info d ON (ids.monitor_id = d.monitor_id)");
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			query.append(" WHERE d.owner_role_id = ?5");
		}
		query.append(" ORDER BY ids.item_code, ids.display_name, d.run_interval");
		
		Map<Integer, Object> parameters = new HashMap<Integer, Object>();
		// itemFilter
		parameters.put(1, itemFilter);
		// facilityId
		parameters.put(2, facilityId);
		// fromTime
		parameters.put(3, fromTime);
		// toTime
		parameters.put(4, toTime);
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			parameters.put(5, ownerRoleId);
		}
		return QueryExecutor.getListByNativeQueryWithTimeout(query.toString(), parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
	}
	
	public static List<Object[]> getMonitorInfoDataList(String facilityId, String monitorId, Long fromTime, Long toTime, String ownerRoleId) throws HinemosDbTimeout, NullPointerException {
		StringBuilder query = new StringBuilder();
		query.append("SELECT ids.monitor_id AS monitor_id,");
		query.append(" d.item_name AS item_name,");
		query.append(" d.measure AS measure,");
		query.append(" ids.display_name AS display_name,");
		query.append(" d.run_interval AS run_interval ");
		query.append(" FROM (");
		query.append(" SELECT monitor_id, display_name");
		query.append(" FROM log.cc_collect_key a ");
		query.append(" LEFT OUTER JOIN log.cc_collect_data_raw b ON (a.collector_id = b.collector_id)");
		query.append(" WHERE facility_id = ?1");
		query.append(" AND monitor_id = ?2 ");
		query.append(" AND time >= ?3 AND time < ?4 ");
		query.append(" GROUP BY monitor_id, display_name) AS ids");
		query.append(" LEFT OUTER JOIN setting.cc_monitor_info d ON (ids.monitor_id = d.monitor_id)");
		query.append(" WHERE d.monitor_type_id != 'MON_PRF_N'");
		query.append(" AND d.monitor_type_id NOT LIKE 'MON_CLOUD%'");
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			query.append(" AND d.owner_role_id = ?5");
		}
		query.append(" ORDER BY ids.monitor_id,d.item_name,ids.display_name,d.run_interval");
			
		Map<Integer, Object> parameters = new HashMap<Integer, Object>();
		// facilityId
		parameters.put(1, facilityId);
		// monitorId
		parameters.put(2, monitorId);
		// fromTime
		parameters.put(3, fromTime);
		// toTime
		parameters.put(4, toTime);
		
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			parameters.put(5, ownerRoleId);
		}
		return QueryExecutor.getListByNativeQueryWithTimeout(query.toString(), parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
	}
	
	public static List<Object[]> getMonitorInfoDataList(String facilityId, Boolean collectorFlg, Long fromTime, Long toTime, String ownerRoleId) throws HinemosDbTimeout {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT ");
		query.append(" ids.monitor_id AS monitor_id, ");
		query.append(" c.item_name AS item_name, ");
		query.append(" c.measure AS measure, ");
		query.append(" ids.display_name AS display_name, ");
		query.append(" c.run_interval AS run_interval ");
		query.append(" FROM (");
		query.append(" SELECT monitor_id, ");
		query.append(" display_name  ");
		query.append(" FROM log.cc_collect_key a ");
		query.append(" LEFT OUTER JOIN log.cc_collect_data_raw b ON (a.collector_id = b.collector_id)  ");
		query.append(" WHERE EXISTS (");
		query.append(" SELECT * ");
		query.append(" FROM setting.cc_monitor_info ");
		query.append(" WHERE monitor_type_id != 'MON_PRF_N' ");
		query.append(" AND monitor_type_id NOT LIKE 'MON_CLOUD%'");
		query.append(" AND cc_monitor_info.monitor_id = a.monitor_id");
		query.append(" AND cc_monitor_info.collector_flg = ?1 ) ");
		query.append(" AND a.facility_id = ?2");
		query.append(" AND b.time >= ?3 ");
		query.append(" AND b.time < ?4 ");
		query.append(" GROUP BY a.monitor_id, a.display_name) AS ids ");
		query.append(" LEFT OUTER JOIN setting.cc_monitor_info c ON (c.monitor_id = ids.monitor_id) ");
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			query.append(" WHERE c.owner_role_id = ?5 ");
		}
		query.append(" ORDER BY ids.monitor_id, c.item_name, ids.display_name, c.run_interval");
			
		Map<Integer, Object> parameters = new HashMap<Integer, Object>();
		// collectorFlg
		parameters.put(1, collectorFlg);
		// facilityId
		parameters.put(2, facilityId);
		// monitorId
		// fromTime
		parameters.put(3,  fromTime);
		// toTime
		parameters.put(4, toTime);
		
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			parameters.put(5, ownerRoleId);
		}
		return QueryExecutor.getListByNativeQueryWithTimeout(query.toString(), parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
	}
	
	public static List<Object[]> getMonitorInfoHourList(String facilityId, String monitorId, Long fromTime, Long toTime, String ownerRoleId) throws HinemosDbTimeout, NullPointerException {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT ids.monitor_id AS monitor_id,");
		query.append(" d.item_name AS item_name,");
		query.append(" d.measure AS measure,");
		query.append(" ids.display_name AS display_name,");
		query.append(" d.run_interval AS run_interval ");
		query.append(" FROM (");
		query.append(" SELECT monitor_id, display_name");
		query.append(" FROM log.cc_collect_key a");
		query.append(" LEFT OUTER JOIN log.cc_collect_summary_hour b ON (a.collector_id = b.collector_id) ");
		query.append(" WHERE facility_id = ?1");
		query.append(" AND monitor_id = ?2");
		query.append(" AND time >= ?3 AND time < ?4");
		query.append(" GROUP BY monitor_id,display_name");
		query.append(" ) AS ids");
		query.append(" LEFT OUTER JOIN setting.cc_monitor_info d ON (ids.monitor_id = d.monitor_id)");
		query.append(" WHERE d.monitor_type_id != 'MON_PRF_N'");
		query.append(" AND d.monitor_type_id NOT LIKE 'MON_CLOUD%'");
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			query.append(" AND d.owner_role_id = ?5");
		}
		query.append(" ORDER BY ids.monitor_id, d.item_name, ids.display_name, d.run_interval");
		
		
		Map<Integer, Object> parameters = new HashMap<Integer, Object>();
		// facilityId
		parameters.put(1, facilityId);
		// monitorId
		parameters.put(2, monitorId);
		// fromTime
		parameters.put(3, fromTime);
		// toTime
		parameters.put(4, toTime);
		
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			parameters.put(5, ownerRoleId);
		}
		return QueryExecutor.getListByNativeQueryWithTimeout(query.toString(), parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
	}
	
	public static List<Object[]> getMonitorInfoHourList(String facilityId, Boolean collectorFlg, Long fromTime, Long toTime, String ownerRoleId) throws HinemosDbTimeout {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT ");
		query.append(" ids.monitor_id AS monitor_id, ");
		query.append(" c.item_name AS item_name, ");
		query.append(" c.measure AS measure, ");
		query.append(" ids.display_name AS display_name, ");
		query.append(" c.run_interval AS run_interval ");
		query.append(" FROM (");
		query.append(" SELECT monitor_id, ");
		query.append(" display_name  ");
		query.append(" FROM log.cc_collect_key a ");
		query.append(" LEFT OUTER JOIN log.cc_collect_summary_hour b ON (a.collector_id = b.collector_id)  ");
		query.append(" WHERE EXISTS (");
		query.append(" SELECT * ");
		query.append(" FROM setting.cc_monitor_info ");
		query.append(" WHERE monitor_type_id != 'MON_PRF_N' ");
		query.append(" AND monitor_type_id NOT LIKE 'MON_CLOUD%'");
		query.append(" AND cc_monitor_info.monitor_id = a.monitor_id");
		query.append(" AND cc_monitor_info.collector_flg = ?1 ) ");
		query.append(" AND a.facility_id = ?2");
		query.append(" AND b.time >= ?3 ");
		query.append(" AND b.time < ?4 ");
		query.append(" GROUP BY a.monitor_id, a.display_name) AS ids ");
		query.append(" LEFT OUTER JOIN setting.cc_monitor_info c ON (c.monitor_id = ids.monitor_id) ");
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			query.append(" WHERE c.owner_role_id = ?5 ");
		}
		query.append(" ORDER BY ids.monitor_id, c.item_name, ids.display_name, c.run_interval");
		
		Map<Integer, Object> parameters = new HashMap<Integer, Object>();
		// collectorFlg
		parameters.put(1, collectorFlg);
		// facilityId
		parameters.put(2, facilityId);
		// monitorId
		// fromTime
		parameters.put(3, fromTime);
		// toTime
		parameters.put(4, toTime);
		
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			parameters.put(5, ownerRoleId);
		}
		return QueryExecutor.getListByNativeQueryWithTimeout(query.toString(), parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
	}
	
	public static List<Object[]> getMonitorInfoDayList(String facilityId, String monitorId, Long fromTime, Long toTime, String ownerRoleId) throws HinemosDbTimeout, NullPointerException {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT ids.monitor_id AS monitor_id,");
		query.append(" d.item_name AS item_name,");
		query.append(" d.measure AS measure,");
		query.append(" ids.display_name AS display_name,");
		query.append(" d.run_interval AS run_interval ");
		query.append(" FROM (");
		query.append(" SELECT monitor_id, display_name");
		query.append(" FROM log.cc_collect_key a");
		query.append(" LEFT OUTER JOIN log.cc_collect_summary_day b ON (a.collector_id = b.collector_id)");
		query.append(" WHERE facility_id = ?1");
		query.append(" AND monitor_id = ?2");
		query.append(" AND time >= ?3 AND time < ?4");
		query.append(" GROUP BY monitor_id,display_name");
		query.append(" ) AS ids");
		query.append(" LEFT OUTER JOIN setting.cc_monitor_info d ON (ids.monitor_id = d.monitor_id)");
		query.append(" WHERE d.monitor_type_id != 'MON_PRF_N'");
		query.append(" AND d.monitor_type_id NOT LIKE 'MON_CLOUD%'");
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			query.append(" AND d.owner_role_id = ?5");
		}
		query.append(" ORDER BY ids.monitor_id, d.item_name, ids.display_name, d.run_interval");
			
		Map<Integer, Object> parameters = new HashMap<Integer, Object>();
		// facilityId
		parameters.put(1, facilityId);
		// monitorId
		parameters.put(2, monitorId);
		// fromTime
		parameters.put(3, fromTime);
		// toTime
		parameters.put(4, toTime);
		
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			parameters.put(5, toTime);
		}
		return QueryExecutor.getListByNativeQueryWithTimeout(query.toString(), parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
	}
	
	public static List<Object[]> getMonitorInfoDayList(String facilityId, Boolean collectorFlg, Long fromTime, Long toTime, String ownerRoleId) throws HinemosDbTimeout {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT ");
		query.append(" ids.monitor_id AS monitor_id, ");
		query.append(" c.item_name AS item_name, ");
		query.append(" c.measure AS measure, ");
		query.append(" ids.display_name AS display_name, ");
		query.append(" c.run_interval AS run_interval ");
		query.append(" FROM (");
		query.append(" SELECT monitor_id, ");
		query.append(" display_name  ");
		query.append(" FROM log.cc_collect_key a ");
		query.append(" LEFT OUTER JOIN log.cc_collect_summary_day b ON (a.collector_id = b.collector_id)  ");
		query.append(" WHERE EXISTS (");
		query.append(" SELECT * ");
		query.append(" FROM setting.cc_monitor_info ");
		query.append(" WHERE monitor_type_id != 'MON_PRF_N' ");
		query.append(" AND monitor_type_id NOT LIKE 'MON_CLOUD%'");
		query.append(" AND cc_monitor_info.monitor_id = a.monitor_id");
		query.append(" AND cc_monitor_info.collector_flg = ?1 ) ");
		query.append(" AND a.facility_id = ?2");
		query.append(" AND b.time >= ?3 ");
		query.append(" AND b.time < ?4 ");
		query.append(" GROUP BY a.monitor_id, a.display_name) AS ids ");
		query.append(" LEFT OUTER JOIN setting.cc_monitor_info c ON (c.monitor_id = ids.monitor_id) ");
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			query.append(" WHERE c.owner_role_id = ?5 ");
		}
		query.append(" ORDER BY ids.monitor_id, c.item_name, ids.display_name, c.run_interval");
		
			
		Map<Integer, Object> parameters = new HashMap<Integer, Object>();
		// collectorFlg
		parameters.put(1, collectorFlg);
		// facilityId
		parameters.put(2, facilityId);
		// monitorId
		// fromTime
		parameters.put(3, fromTime);
		// toTime
		parameters.put(4, toTime);
		
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			parameters.put(5, ownerRoleId);
		}
		return QueryExecutor.getListByNativeQueryWithTimeout(query.toString(), parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
	}
	
	public static List<Object[]> getMonitorInfoMonthList(String facilityId, String monitorId, Long fromTime, Long toTime, String ownerRoleId) throws HinemosDbTimeout, NullPointerException {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT ids.monitor_id AS monitor_id,");
		query.append(" d.item_name AS item_name,");
		query.append(" d.measure AS measure,");
		query.append(" ids.display_name AS display_name,");
		query.append(" d.run_interval AS run_interval ");
		query.append(" FROM (");
		query.append(" SELECT monitor_id, display_name");
		query.append(" FROM log.cc_collect_key a");
		query.append(" LEFT OUTER JOIN log.cc_collect_summary_month b ON (a.collector_id = b.collector_id)");
		query.append(" WHERE facility_id = ?1");
		query.append(" AND monitor_id = ?2");
		query.append(" AND time >= ?3 AND time < ?4");
		query.append(" GROUP BY monitor_id, display_name");
		query.append(" ) AS ids");
		query.append(" LEFT OUTER JOIN setting.cc_monitor_info d ON (ids.monitor_id = d.monitor_id)");
		query.append(" WHERE d.monitor_type_id != 'MON_PRF_N'");
		query.append(" AND d.monitor_type_id NOT LIKE 'MON_CLOUD%'");
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			query.append(" AND d.owner_role_id = ?5");
		}
		query.append(" ORDER BY ids.monitor_id, d.item_name, ids.display_name, d.run_interval");
		
		Map<Integer, Object> parameters = new HashMap<Integer, Object>();
		// facilityId
		parameters.put(1, facilityId);
		// monitorId
		parameters.put(2, monitorId);
		// fromTime
		parameters.put(3, fromTime);
		// toTime
		parameters.put(4, toTime);
		
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			parameters.put(5, ownerRoleId);
		}
		return QueryExecutor.getListByNativeQueryWithTimeout(query.toString(), parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
	}
	
	public static List<Object[]> getMonitorInfoMonthList(String facilityId, Boolean collectorFlg, Long fromTime, Long toTime, String ownerRoleId) throws HinemosDbTimeout {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT ");
		query.append(" ids.monitor_id AS monitor_id, ");
		query.append(" c.item_name AS item_name, ");
		query.append(" c.measure AS measure, ");
		query.append(" ids.display_name AS display_name, ");
		query.append(" c.run_interval AS run_interval ");
		query.append(" FROM (");
		query.append(" SELECT monitor_id, ");
		query.append(" display_name  ");
		query.append(" FROM log.cc_collect_key a ");
		query.append(" LEFT OUTER JOIN log.cc_collect_summary_month b ON (a.collector_id = b.collector_id)  ");
		query.append(" WHERE EXISTS (");
		query.append(" SELECT * ");
		query.append(" FROM setting.cc_monitor_info ");
		query.append(" WHERE monitor_type_id != 'MON_PRF_N' ");
		query.append(" AND monitor_type_id NOT LIKE 'MON_CLOUD%'");
		query.append(" AND cc_monitor_info.monitor_id = a.monitor_id");
		query.append(" AND cc_monitor_info.collector_flg = ?1 ) ");
		query.append(" AND a.facility_id = ?2");
		query.append(" AND b.time >= ?3 ");
		query.append(" AND b.time < ?4 ");
		query.append(" GROUP BY a.monitor_id, a.display_name) AS ids ");
		query.append(" LEFT OUTER JOIN setting.cc_monitor_info c ON (c.monitor_id = ids.monitor_id) ");
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			query.append(" WHERE c.owner_role_id = ?5 ");
		}
		query.append(" ORDER BY ids.monitor_id, c.item_name, ids.display_name, c.run_interval");
			
		Map<Integer, Object> parameters = new HashMap<Integer, Object>();
		// collectorFlg
		parameters.put(1, collectorFlg);
		// facilityId
		parameters.put(2, facilityId);

		// fromTime
		parameters.put(3, fromTime);
		// toTime
		parameters.put(4, toTime);
		
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			parameters.put(5, ownerRoleId);
		}
		return QueryExecutor.getListByNativeQueryWithTimeout(query.toString(), parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
	}
	
	public static CollectorItemCodeMstEntity getCollectorItemCodeMstPK(String itemCode, ObjectPrivilegeMode privliegeMode) throws CollectorNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			CollectorItemCodeMstEntity entity = em.find(CollectorItemCodeMstEntity.class, itemCode, privliegeMode);
			if (entity == null) {
				CollectorNotFound e = new CollectorNotFound("CollectorItemCodeMstEntity.findByPrimaryKey"
						+ "itemCode = " + itemCode);
				m_log.info("getCollectorItemCodeMstPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}
	
	public static FacilityInfo getFacilityPK(String facilityId, ObjectPrivilegeMode privliegeMode) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			FacilityInfo entity = null;
			entity = em.find(FacilityInfo.class, facilityId, privliegeMode);
			if (entity == null) {
				FacilityNotFound e = new FacilityNotFound("FacilityEntity.findByPrimaryKey"
						+ ", facilityId = " + facilityId);
				m_log.info("getFacilityPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}

			return entity;
		}
	}
	
	public static FacilityInfo getFacilityPKAndFacilityType(String facilityId, Integer facilityType) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			FacilityInfo entity = em.createNamedQuery("ReportingFacilityInfo.findByPkAndFafcilityType", FacilityInfo.class)
					.setParameter("facilityId", facilityId)
					.setParameter("facilityType", facilityType)
					.getSingleResult();
			return entity;
		}
	}
	
	public static List<FacilityRelationEntity> getChildFacilityRelationEntity(String parentFacilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<FacilityRelationEntity> list
			= em.createNamedQuery("FacilityRelationEntity.findChild", FacilityRelationEntity.class)
			.setParameter("parentFacilityId", parentFacilityId)
			.getResultList();
			return list;
		}
	}
	
	public static JobInfoEntity getJobInfoEntityPK(String sessionId, String jobunitId, String jobId, ObjectPrivilegeMode privliegeMode) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			//セッションIDとジョブIDから、セッションジョブを取得
			JobInfoEntityPK pk = new JobInfoEntityPK(sessionId, jobunitId, jobId);
			JobInfoEntity jobInfo = null;
			jobInfo = em.find(JobInfoEntity.class, pk, privliegeMode);
			return jobInfo;
		}
	}
	
	public static List<JobSessionEntity> getReportingJobSessionList(
			String jobunitId,
			String jobId,
			String excJobId,
			String parentJobunitId,
			String ownerRoleId,
			Long fromTime,
			Long toTime) {
		List<JobSessionEntity> list = null;
		TypedQuery<JobSessionEntity> typedQuery = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			StringBuilder jquery = new StringBuilder();
			jquery.append("SELECT a FROM JobSessionEntity a LEFT JOIN FETCH a.jobSessionJobEntities b");
			jquery.append(" WHERE true = true AND b.parentJobunitId = :parentJobunitId");
			jquery.append(" AND a.jobunitId like :jobunitId");
			jquery.append(" AND a.jobId like :jobId");
			jquery.append(" AND a.jobId not like :excJobId");
			jquery.append(" AND ((a.scheduleDate >= :fromTime"
					+ " AND a.scheduleDate < :toTime)"
					+ " OR (b.endDate >= :fromTime"
					+ " AND b.endDate < :toTime))");
			// ownerRoleId
			if (ownerRoleId != null && !"".equals(ownerRoleId)) {
				jquery.append(" AND b.ownerRoleId = :ownerRoleId");
			}
			jquery.append(" ORDER BY a.scheduleDate");
			typedQuery = em.createQuery(jquery.toString(), JobSessionEntity.class);
			typedQuery.setParameter("parentJobunitId", parentJobunitId);
			typedQuery.setParameter("jobunitId", jobunitId);
			typedQuery.setParameter("jobId", jobId);
			typedQuery.setParameter("excJobId", excJobId);
			typedQuery.setParameter("fromTime", fromTime);
			typedQuery.setParameter("toTime", toTime);
			// ownerRoleId
			if (ownerRoleId != null && !"".equals(ownerRoleId)) {
				typedQuery.setParameter("ownerRoleId", ownerRoleId);
			}
			
			list = typedQuery.getResultList();
		} catch (Exception e) {
			m_log.debug("getReportingJobSessionEntityList : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
		}
		return list;
	}
	
	public static List<JobSessionNodeEntity> getReportingJobSessionNodeList(
			String facilityId,
			String jobunitId,
			String jobId,
			String excJobId,
			String ownerRoleId,
			Long fromTime,
			Long toTime) {
		List<JobSessionNodeEntity> list = null;
		TypedQuery<JobSessionNodeEntity> typedQuery = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			StringBuilder jquery = new StringBuilder();
			jquery.append("SELECT a FROM JobSessionNodeEntity a LEFT JOIN FETCH a.jobSessionJobEntity b");
			jquery.append(" LEFT JOIN FETCH b.jobInfoEntity c");
			jquery.append(" LEFT JOIN FETCH b.jobSessionEntity d");
			jquery.append(" WHERE true = true AND a.id.facilityId= :facilityId");
			jquery.append(" AND a.startDate IS NOT NULL");
			jquery.append(" AND ((a.startDate >= :fromTime AND a.startDate < :toTime) OR (a.endDate >= :fromTime AND a.endDate < :toTime))");
			jquery.append(" AND a.id.jobunitId like :jobunitId");
			jquery.append(" AND a.id.jobId like :jobId");
			jquery.append(" AND a.id.jobId not like :excJobId");
			
			// ownerRoleId
			if (ownerRoleId != null && !"".equals(ownerRoleId)) {
				jquery.append(" AND b.ownerRoleId = :ownerRoleId");
			}
			jquery.append(" ORDER BY a.startDate");
			typedQuery = em.createQuery(jquery.toString(), JobSessionNodeEntity.class);
			typedQuery.setParameter("facilityId", facilityId);
			typedQuery.setParameter("jobunitId", jobunitId);
			typedQuery.setParameter("jobId", jobId);
			typedQuery.setParameter("excJobId", excJobId);
			typedQuery.setParameter("fromTime", fromTime);
			typedQuery.setParameter("toTime", toTime);
			// ownerRoleId
			if (ownerRoleId != null && !"".equals(ownerRoleId)) {
				typedQuery.setParameter("ownerRoleId", ownerRoleId);
			}
			
			list = typedQuery.getResultList();
		} catch (Exception e) {
			m_log.debug("getReportingJobSessionNodeList : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
		}
		return list;
	}
	
	public static List<JobSessionJobEntity> getReportingJobDetailList(
			String sessionId,
			String jobunitId,
			String jobId,
			String excJobId, String ownerRoleId) {
		List<JobSessionJobEntity> list = null;
		TypedQuery<JobSessionJobEntity> typedQuery = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			StringBuilder jquery = new StringBuilder();
			jquery.append("SELECT a FROM JobSessionJobEntity a JOIN a.jobSessionEntity b");
			jquery.append(" JOIN a.jobInfoEntity c");
			jquery.append(" WHERE true = true");
			jquery.append(" AND a.id.sessionId = :sessionId");
			jquery.append(" AND a.parentJobunitId IS NOT NULL");
			jquery.append(" AND a.id.jobunitId like :jobunitId");
			jquery.append(" AND a.id.jobId like :jobId");
			jquery.append(" AND a.id.jobId not like :excJobId");
			// ownerRoleId
			if (ownerRoleId != null && !"".equals(ownerRoleId)) {
				jquery.append(" AND a.ownerRoleId = :ownerRoleId");
			}
			jquery.append(" ORDER BY b.scheduleDate, a.startDate");
			typedQuery = em.createQuery(jquery.toString(), JobSessionJobEntity.class);
			typedQuery.setParameter("sessionId", sessionId);
			typedQuery.setParameter("jobunitId", jobunitId);
			typedQuery.setParameter("jobId", jobId);
			typedQuery.setParameter("excJobId", excJobId);
			// ownerRoleId
			if (ownerRoleId != null && !"".equals(ownerRoleId)) {
				typedQuery.setParameter("ownerRoleId", ownerRoleId);
			}
			
			list = typedQuery.getResultList();
		} catch (Exception e) {
			m_log.debug("getReportingJobDetailList : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
		}
		return list;
	}
	
	public static List<JobSessionJobEntity> getRootJobSessionJobByParentJobunitId(String parentJobunitId, Long fromTime, Long toTime){
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			//ジョブ
			List<JobSessionJobEntity> jobSessionJobList
			= em.createNamedQuery("ReportingJobSessionJobEntity.findByParentJobunitId", JobSessionJobEntity.class)
			.setParameter("parentJobunitId", parentJobunitId)
			.setParameter("fromTime", fromTime)
			.setParameter("toTime", toTime).getResultList();
			return jobSessionJobList;
		}
	}

	@SuppressWarnings("unchecked")
	public static List<Object[]> getMonitorPriorityTotalList(
			String facilityId,
			Long fromTime,
			Long toTime,
			String ownerRoleId) {
		List<Object[]> list = null;
		Query typedQuery = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			StringBuilder query = new StringBuilder();
			query.append("WITH nums AS ");
			query.append(" (");
			query.append(" SELECT priority, count(*) AS count1");
			query.append(" FROM log.cc_event_log ");
			query.append(" WHERE facility_id = ?1 ");
			query.append(" AND generation_date >= ?2 ");
			query.append(" AND generation_date < ?3 ");
			if (ownerRoleId != null && !"".equals(ownerRoleId)) {
				query.append(" AND owner_role_id = ?4 ");
			}
			query.append(" GROUP BY priority");
			query.append(" ), ");
			query.append(" priority_type as (");
			query.append(" select 0 AS key1, " + PriorityConstant.TYPE_CRITICAL + " AS type1,'" + ReportUtil.STRING_CRITICAL + "' AS label");
			query.append(" union all select 1, " + PriorityConstant.TYPE_WARNING + ", '" + ReportUtil.STRING_WARNING + "'");
			query.append(" union all select 2, " + PriorityConstant.TYPE_INFO + ", '" + ReportUtil.STRING_INFO + "'");
			query.append(" union all select 3, " + PriorityConstant.TYPE_UNKNOWN + ", '" + ReportUtil.STRING_UNKNOWN + "')");
			query.append(" SELECT priority_type.label, coalesce(count1, 0) AS count ");
			query.append(" FROM nums RIGHT OUTER JOIN priority_type ON nums.priority = priority_type.type1 ");
			query.append(" ORDER BY key1");

			typedQuery = em.createNativeQuery(query.toString());
			typedQuery.setParameter(1, facilityId);
			typedQuery.setParameter(2, fromTime);
			typedQuery.setParameter(3, toTime);
			// ownerRoleId
			if (ownerRoleId != null && !"".equals(ownerRoleId)) {
				typedQuery.setParameter(4, ownerRoleId);
			}
			
			list = (List<Object[]>)typedQuery.getResultList();
		} catch (Exception e) {
			m_log.debug("getMonitorPriorityTotalList : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
		}
		return list;
	}
	
	public static List<EventLogEntity> getMonitorDetailList(
			String facilityId,
			Long fromTime,
			Long toTime,
			String ownerRoleId) {
		List<EventLogEntity> list = null;
		TypedQuery<EventLogEntity> typedQuery = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			StringBuilder jquery = new StringBuilder();
			jquery.append("SELECT a FROM EventLogEntity a");
			jquery.append(" WHERE a.id.facilityId = :facilityId");
			jquery.append(" AND a.generationDate >= :fromTime AND a.generationDate < :toTime");
			// ownerRoleId
			if (ownerRoleId != null && !"".equals(ownerRoleId)) {
				jquery.append(" AND a.ownerRoleId = :ownerRoleId");
			}
			jquery.append(" ORDER BY a.generationDate, a.priority, a.id.monitorId, a.id.pluginId");
			
			typedQuery = em.createQuery(jquery.toString(), EventLogEntity.class);
			typedQuery.setParameter("facilityId", facilityId);
			typedQuery.setParameter("fromTime", fromTime);
			typedQuery.setParameter("toTime", toTime);
			// ownerRoleId
			if (ownerRoleId != null && !"".equals(ownerRoleId)) {
				typedQuery.setParameter("ownerRoleId", ownerRoleId);
			}
			
			list = typedQuery.getResultList();
		} catch (Exception e) {
			m_log.debug("getMonitorDetailList : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
		}
		return list;
	}
	
	public static List<Object> getCollectItemCodes(String monitorId) 
			throws HinemosDbTimeout {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("monitorId", monitorId);

		return QueryExecutor.getListByJpqlWithTimeout(PriorityQueryUtil.getItemCodes(monitorId), Object.class, parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
	}

	/**
	 * 収集したサマリーデータが存在するかを判定する。<BR>
	 * 
	 * @param idList
	 * @param fromTime
	 * @param toTime
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public boolean hasSummaryData(List<Integer> idList, Long fromTime, Long toTime) throws HinemosDbTimeout {
		ReportingCollectControllerBean controller = new ReportingCollectControllerBean();
		boolean hasSummaryDataFlg = false;
		int summaryType = ReportUtil.getSummaryType(fromTime, toTime);
		// サマリデータ、または収集データ(raw)のタイプでスイッチ
		switch (summaryType) {
		case SummaryTypeConstant.TYPE_AVG_HOUR:
			List<SummaryHour> summaryHList = controller.getSummaryHourList(idList, fromTime, toTime);
			if (summaryHList.isEmpty()) {
				break;
			} else {
				hasSummaryDataFlg = true;
			}
			break;
		case SummaryTypeConstant.TYPE_AVG_DAY:
			List<SummaryDay> summaryDList = controller.getSummaryDayList(idList, fromTime, toTime);
			if (summaryDList.isEmpty()) {
				break;
			} else {
				hasSummaryDataFlg = true;
			}
			break;
		case SummaryTypeConstant.TYPE_AVG_MONTH:
			List<SummaryMonth> summaryMList = controller.getSummaryMonthList(idList, fromTime, toTime);
			if (summaryMList.isEmpty()) {
				break;
			} else {
				hasSummaryDataFlg = true;
			}
			break;
		default: // defaultはRAWとする
			List<CollectData> summaryList = controller.getCollectDataList(idList, fromTime, toTime);
			if (summaryList.isEmpty()) {
				break;
			} else {
				hasSummaryDataFlg = true;
			}
			break;
		}
		return hasSummaryDataFlg;
	}

}