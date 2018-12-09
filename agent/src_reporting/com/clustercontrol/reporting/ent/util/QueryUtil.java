/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.ent.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.collect.model.CollectKeyInfo;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.jmx.model.JmxCheckInfo;
import com.clustercontrol.jmx.model.JmxMasterInfo;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobInfoEntityPK;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.performance.monitor.model.CollectorItemCodeMstEntity;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.platform.QueryExecutor;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(QueryUtil.class);
	static long fetchSize = 1000l;
	
	public static List<MonitorInfo> getMonitorInfoByItemCode(List<String> itemCodeList) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorInfo> list = em.createNamedQuery("ReportingMonitorInfo.findByItemCodeList", MonitorInfo.class)
					.setParameter("itemCodeList", itemCodeList)
					.getResultList();
			return list;
		}
	}
	
	public static List<MonitorInfo> getMonitorInfoByMonitorTypeId(String monitorTypeId, String ownerRoleId) {
		List<MonitorInfo> list = null;
		TypedQuery<MonitorInfo> typedQuery = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			StringBuilder jquery = new StringBuilder();
			jquery.append("SELECT a FROM MonitorInfo a WHERE true = true");
			jquery.append(" AND a.monitorTypeId = :monitorTypeId");
			
			// ownerRoleId
			if (ownerRoleId != null && !"".equals(ownerRoleId)) {
				jquery.append(" AND a.ownerRoleId = :ownerRoleId");
			}
			jquery.append(" ORDER BY a.monitorId, a.itemName, a.runInterval");
			
			typedQuery = em.createQuery(jquery.toString(), MonitorInfo.class);
			
			typedQuery.setParameter("monitorTypeId", monitorTypeId);
			
			// ownerRoleId
			if (ownerRoleId != null && !"".equals(ownerRoleId)) {
				typedQuery.setParameter("ownerRoleId", ownerRoleId);
			}
			list = typedQuery.getResultList();
		} catch (Exception e) {
			m_log.info("getMonitorInfoByMonitorTypeId : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
		}
		return list;
	}
	
	public static List<CollectKeyInfo> getCollectKeyInfoListByMonitorIdAndFacilityidList(String monitorId, List<String> facilityidList) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<CollectKeyInfo> list = em.createNamedQuery("ReportingCollectKeyInfo.findByMonitorIdAndFacilityidList", CollectKeyInfo.class)
					.setParameter("monitorId", monitorId)
					.setParameter("facilityidList", facilityidList)
					.getResultList();
			return list;
		}
	}
	
	public static List<CollectorItemCodeMstEntity> getCollectorItemCodeMstListByItemCode(List<String> itemCodeList) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<CollectorItemCodeMstEntity> list = em.createNamedQuery("ReportingCollectorItemCodeMstEntity.findByItemCodeList", CollectorItemCodeMstEntity.class)
					.setParameter("itemCodeList", itemCodeList)
					.getResultList();
			return list;
		}
	}
	
	public static JmxCheckInfo getMonitorJmxInfoPK(String monitorId, ObjectPrivilegeMode privliegeMode) throws MonitorNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			JmxCheckInfo entity = em.find(JmxCheckInfo.class, monitorId, privliegeMode);
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
	
	public static JmxCheckInfo getMonitorJmxInfoByMonitorIdAndMasterId(String monitorId, String masterId) throws MonitorNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			JmxCheckInfo entity = em.createNamedQuery("ReportingJmxCheckInfo.getMonitorJmxInfoByMonitorIdAndMasterId", JmxCheckInfo.class)
					.setParameter("monitorId", monitorId)
					.setParameter("masterId", masterId)
					.getSingleResult();
			return entity;
		}
	}
	
	public static JmxMasterInfo getJmxMasterInfoPK(String masterId, ObjectPrivilegeMode privliegeMode) throws MonitorNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			JmxMasterInfo entity = em.find(JmxMasterInfo.class, masterId, privliegeMode);
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
	
	public static List<MonitorInfo> getAllMonitorInfo() throws HinemosUnknown {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorInfo> list
			= em.createNamedQuery("MonitorInfo.findAll", MonitorInfo.class)
			.getResultList();
			return list;
		}
	}

	public static List<Object[]> getSummaryJmxAvgData(
			String facilityId,
			Long fromTime,
			Long toTime,
			String masterId) throws HinemosDbTimeout {
		StringBuilder jquery = new StringBuilder();
		jquery.append("SELECT c.masterId, b.id.time, a.id.facilityid, SUM(b.value) FROM CollectKeyInfo a");
		jquery.append(" JOIN CollectData b ON a.collectorid = b.id.collectorid");
		jquery.append(" JOIN JmxCheckInfo c ON a.id.monitorId = c.monitorId");
		jquery.append(" JOIN JmxMasterInfo d ON c.masterId = d.id");
		jquery.append(" WHERE a.id.facilityid like :facilityId");
		jquery.append(" AND b.id.time >= :fromTime AND b.id.time <= :toTime");
		jquery.append(" AND c.masterId = :masterId");
		jquery.append(" GROUP BY c.masterId, b.id.time, a.id.facilityid");
		jquery.append(" ORDER BY b.id.time");

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("facilityId", facilityId);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		parameters.put("masterId", masterId);

		return QueryExecutor.getListByJpqlWithTimeout(jquery.toString(), Object[].class, parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
	}
	
	public static List<Object[]> getSummaryJmxAvgHour(
			String facilityId,
			Long fromTime,
			Long toTime,
			String masterId) throws HinemosDbTimeout {
		StringBuilder jquery = new StringBuilder();
		jquery.append("SELECT c.masterId, b.id.time, a.id.facilityid, SUM(b.avg) FROM CollectKeyInfo a");
		jquery.append(" JOIN SummaryHour b ON a.collectorid = b.id.collectorid");
		jquery.append(" JOIN JmxCheckInfo c ON a.id.monitorId = c.monitorId");
		jquery.append(" JOIN JmxMasterInfo d ON c.masterId = d.id");
		jquery.append(" WHERE a.id.facilityid like :facilityId");
		jquery.append(" AND b.id.time >= :fromTime AND b.id.time <= :toTime");
		jquery.append(" AND c.masterId = :masterId");
		jquery.append(" GROUP BY c.masterId, b.id.time, a.id.facilityid");
		jquery.append(" ORDER BY b.id.time");

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("facilityId", facilityId);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		parameters.put("masterId", masterId);

		return QueryExecutor.getListByJpqlWithTimeout(jquery.toString(), Object[].class, parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
	}
	
	public static List<Object[]> getSummaryJmxAvgDay(
			String facilityId,
			Long fromTime,
			Long toTime,
			String masterId) throws HinemosDbTimeout {
		StringBuilder jquery = new StringBuilder();
		jquery.append("SELECT c.masterId, b.id.time, a.id.facilityid, SUM(b.avg) FROM CollectKeyInfo a");
		jquery.append(" JOIN SummaryDay b ON a.collectorid = b.id.collectorid");
		jquery.append(" JOIN JmxCheckInfo c ON a.id.monitorId = c.monitorId");
		jquery.append(" JOIN JmxMasterInfo d ON c.masterId = d.id");
		jquery.append(" WHERE a.id.facilityid like :facilityId");
		jquery.append(" AND b.id.time >= :fromTime AND b.id.time <= :toTime");
		jquery.append(" AND c.masterId = :masterId");
		jquery.append(" GROUP BY c.masterId, b.id.time, a.id.facilityid");
		jquery.append(" ORDER BY b.id.time");

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("facilityId", facilityId);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		parameters.put("masterId", masterId);

		return QueryExecutor.getListByJpqlWithTimeout(jquery.toString(), Object[].class, parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
	}
	
	public static List<Object[]> getSummaryJmxAvgMonth(
			String facilityId,
			Long fromTime,
			Long toTime,
			String masterId) throws HinemosDbTimeout {
		StringBuilder jquery = new StringBuilder();
		jquery.append("SELECT c.masterId, b.id.time, a.id.facilityid, SUM(b.avg) FROM CollectKeyInfo a");
		jquery.append(" JOIN SummaryMonth b ON a.collectorid = b.id.collectorid");
		jquery.append(" JOIN JmxCheckInfo c ON a.id.monitorId = c.monitorId");
		jquery.append(" JOIN JmxMasterInfo d ON c.masterId = d.id");
		jquery.append(" WHERE a.id.facilityid like :facilityId");
		jquery.append(" AND b.id.time >= :fromTime AND b.id.time <= :toTime");
		jquery.append(" AND c.masterId = :masterId");
		jquery.append(" GROUP BY c.masterId, b.id.time, a.id.facilityid");
		jquery.append(" ORDER BY b.id.time");

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("facilityId", facilityId);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		parameters.put("masterId", masterId);

		return QueryExecutor.getListByJpqlWithTimeout(jquery.toString(), Object[].class, parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
	}
	
	public static List<Object[]> getSummaryPrefAvgData(
			String facilityId,
			Long fromTime,
			Long toTime,
			String monitorId,
			List<String> itemCodeList) throws HinemosDbTimeout {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("facilityId", facilityId);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		parameters.put("monitorId", monitorId);
		parameters.put("itemCodeList", itemCodeList);

		return QueryExecutor.getListByQueryNameWithTimeout("ReportingSummaryPerfAvg.getSummaryPrefAvgData", Object[].class, parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
	}
	
	public static List<Object[]> getSummaryPrefAvgHour(
			String facilityId,
			Long fromTime,
			Long toTime,
			String monitorId,
			List<String> itemCodeList) throws HinemosDbTimeout {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("facilityId", facilityId);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		parameters.put("monitorId", monitorId);
		parameters.put("itemCodeList", itemCodeList);

		return QueryExecutor.getListByQueryNameWithTimeout("ReportingSummaryPerfAvg.getSummaryPrefAvgHour", Object[].class, parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
	}
	
	public static List<Object[]> getSummaryPrefAvgDay(
			String facilityId,
			Long fromTime,
			Long toTime,
			String monitorId,
			List<String> itemCodeList) throws HinemosDbTimeout {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("facilityId", facilityId);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		parameters.put("monitorId", monitorId);
		parameters.put("itemCodeList", itemCodeList);

		return QueryExecutor.getListByQueryNameWithTimeout("ReportingSummaryPerfAvg.getSummaryPrefAvgDay", Object[].class, parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
	}
	
	public static List<Object[]> getSummaryPrefAvgMonth(
			String facilityId,
			Long fromTime,
			Long toTime,
			String monitorId,
			List<String> itemCodeList) throws HinemosDbTimeout {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("facilityId", facilityId);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		parameters.put("monitorId", monitorId);
		parameters.put("itemCodeList", itemCodeList);

		return QueryExecutor.getListByQueryNameWithTimeout("ReportingSummaryPerfAvg.getSummaryPrefAvgMonth", Object[].class, parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
	}
	
	public static List<Object[]> getCollectDataList(String facilityId, Long fromTime, Long toTime, String monitorId, List<String> itemCodeList) 
		throws HinemosDbTimeout {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("facilityId", facilityId);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		parameters.put("monitorId", monitorId);
		parameters.put("itemCodeList", itemCodeList);
		List<Object[]> list = QueryExecutor.getListByQueryNameWithTimeout("ReportingCollectData.findByTime", Object[].class, parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
		return list;
	}
	
	public static List<Object[]> getSummaryHourList(String facilityId, Long fromTime, Long toTime, String monitorId, List<String> itemCodeList) 
			throws HinemosDbTimeout {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("facilityId", facilityId);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		parameters.put("monitorId", monitorId);
		parameters.put("itemCodeList", itemCodeList);
		List<Object[]> list = QueryExecutor.getListByQueryNameWithTimeout("ReportingSummaryHour.findByTime", Object[].class, parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
		return list;
	}
	
	public static List<Object[]> getSummaryDayList(String facilityId, Long fromTime, Long toTime, String monitorId, List<String> itemCodeList) 
			throws HinemosDbTimeout {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("facilityId", facilityId);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		parameters.put("monitorId", monitorId);
		parameters.put("itemCodeList", itemCodeList);
		List<Object[]> list = QueryExecutor.getListByQueryNameWithTimeout("ReportingSummaryDay.findByTime", Object[].class, parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
		return list;
	}
	
	public static List<Object[]> getSummaryMonthList(String facilityId, Long fromTime, Long toTime, String monitorId, List<String> itemCodeList) 
			throws HinemosDbTimeout {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("facilityId", facilityId);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		parameters.put("monitorId", monitorId);
		parameters.put("itemCodeList", itemCodeList);
		List<Object[]> list = QueryExecutor.getListByQueryNameWithTimeout("ReportingSummaryMonth.findByTime", Object[].class, parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
		return list;
	}
	
	public static JobInfoEntity getJobInfoEntityPK(String sessionId, String jobunitId, String jobId, ObjectPrivilegeMode privliegeMode) throws JobInfoNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			//セッションIDとジョブIDから、セッションジョブを取得
			JobInfoEntityPK pk = new JobInfoEntityPK(sessionId, jobunitId, jobId);
			JobInfoEntity jobInfo = null;
			jobInfo = em.find(JobInfoEntity.class, pk, privliegeMode);
			if (jobInfo == null) {
				JobInfoNotFound je = new JobInfoNotFound("JobInfoEntity.findByPrimaryKey"
						+ ", " + pk.toString());
				m_log.info("getJobInfoEntityPK() : "
						+ je.getClass().getSimpleName() + ", " + je.getMessage());
				je.setSessionId(sessionId);
				je.setJobunitId(jobunitId);
				je.setJobId(jobId);
				throw je;
			}
			return jobInfo;
		}
	}
	
	public static List<Object[]> getSummaryJobSessionJob(
			Long fromTime,
			Long toTime,
			String jobunitId,
			String jobId,
			String excJobId,
			String jobOrderKey,
			String ownerRoleId,
			int orderNum) throws HinemosDbTimeout {
		StringBuilder jquery = new StringBuilder();
		jquery.append("SELECT a.id.jobunitId, a.id.jobId,");
		jquery.append(" MAX(a.endDate - a.startDate), AVG(a.endDate - a.startDate),");
		jquery.append(" (MAX(a.endDate - a.startDate) - AVG(a.endDate - a.startDate)) FROM JobSessionJobEntity a");
		jquery.append(" WHERE a.scopeText IS NOT NULL");
		jquery.append(" AND a.startDate IS NOT NULL");
		jquery.append(" AND a.endDate IS NOT NULL");
		jquery.append(" AND ((a.startDate >= :fromTime AND a.startDate < :toTime) OR (a.endDate >= :fromTime AND a.endDate < :toTime))");
		jquery.append(" AND a.id.jobunitId like :jobunitId");
		jquery.append(" AND a.id.jobId like :jobId");
		jquery.append(" AND a.id.jobId not like :excJobId");
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			jquery.append(" AND a.ownerRoleId = :ownerRoleId");
		}
		jquery.append(" GROUP BY a.id.jobunitId, a.id.jobId");
		switch (jobOrderKey) {
		case PropertiesConstant.ORDER_KEY_MAX:
			jquery.append(" ORDER BY MAX(a.endDate - a.startDate) desc");
			break;
		case PropertiesConstant.ORDER_KEY_AVG:
			jquery.append(" ORDER BY AVG(a.endDate - a.startDate) desc");
			break;
		default:
			jquery.append(" ORDER BY (MAX(a.endDate - a.startDate) - AVG(a.endDate - a.startDate)) desc");
			break;
		}

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		parameters.put("jobunitId", jobunitId);
		parameters.put("jobId", jobId);
		parameters.put("excJobId", excJobId);
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			parameters.put("ownerRoleId", ownerRoleId);
		}

		return QueryExecutor.getListByJpqlWithTimeout(jquery.toString(), Object[].class, parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue(), 0, orderNum);
	}
	
	public static List<JobSessionJobEntity> getJobSessionJobEntityByMaxTime(Long maxTime, String jobunitId, String jobId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<JobSessionJobEntity> entities = em.createNamedQuery("ReportingJobSessionJobEntity.findByMaxTime", JobSessionJobEntity.class)
					.setParameter("maxTime", maxTime)
					.setParameter("jobunitId", jobunitId)
					.setParameter("jobId", jobId)
					.getResultList();
			return entities;
		}
	}
	
	public static List<JobSessionJobEntity> getJobSessionJobByJobunitIdAndJobId(Long fromTime, Long toTime, String jobunitId, String jobId, String ownerRoleId){
		List<JobSessionJobEntity> list = null;
		TypedQuery<JobSessionJobEntity> typedQuery = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			StringBuilder jquery = new StringBuilder();
			jquery.append("SELECT a FROM JobSessionJobEntity a");
			jquery.append(" WHERE a.scopeText IS NOT NULL");
			jquery.append(" AND a.startDate IS NOT NULL");
			jquery.append(" AND a.endDate IS NOT NULL");
			jquery.append(" AND ((a.startDate >= :fromTime AND a.startDate < :toTime) OR (a.endDate >= :fromTime AND a.endDate < :toTime))");
			jquery.append(" AND a.id.jobunitId = :jobunitId");
			jquery.append(" AND a.id.jobId = :jobId");
			// ownerRoleId
			if (ownerRoleId != null && !"".equals(ownerRoleId)) {
				jquery.append(" AND a.ownerRoleId = :ownerRoleId");
			}
			jquery.append(" ORDER BY a.startDate");

			typedQuery = em.createQuery(jquery.toString(), JobSessionJobEntity.class);
			typedQuery.setParameter("fromTime", fromTime);
			typedQuery.setParameter("toTime", toTime);
			typedQuery.setParameter("jobunitId", jobunitId);
			typedQuery.setParameter("jobId", jobId);
			// ownerRoleId
			if (ownerRoleId != null && !"".equals(ownerRoleId)) {
				typedQuery.setParameter("ownerRoleId", ownerRoleId);
			}
			
			list = (List<JobSessionJobEntity>)typedQuery.getResultList();
	} catch (Exception e) {
		m_log.info("getJobSessionJobByJobunitIdAndJobId : "
				+ e.getClass().getSimpleName() + ", " + e.getMessage());
	}
	return list;
	}
}