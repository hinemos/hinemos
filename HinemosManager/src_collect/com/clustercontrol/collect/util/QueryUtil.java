/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.collect.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.collect.model.CollectData;
import com.clustercontrol.collect.model.CollectDataPK;
import com.clustercontrol.collect.model.CollectKeyInfo;
import com.clustercontrol.collect.model.CollectKeyInfoPK;
import com.clustercontrol.collect.model.SummaryDay;
import com.clustercontrol.collect.model.SummaryHour;
import com.clustercontrol.collect.model.SummaryMonth;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.CollectKeyNotFound;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.platform.QueryExecutor;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(QueryUtil.class);
	static long fetchSize = HinemosPropertyCommon.performance_export_fetchsize.getNumericValue();
	
	public static CollectKeyInfo getCollectKeyPK(CollectKeyInfoPK pk) throws CollectKeyNotFound{
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			CollectKeyInfo entity=null;
			entity = em.find(CollectKeyInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				CollectKeyNotFound e = new CollectKeyNotFound(pk.toString());
				m_log.info("getCollectKeyPK() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			
			return entity;
		}
	}
	
	public static Integer getMaxId(){
		Integer maxid = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			maxid = em.createNamedQuery("CollectKeyInfo.findMaxId",Integer.class).getSingleResult();
		} catch (NoResultException e) {
			m_log.debug("getMaxId : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
		}
		return maxid;
	}
	
	public static SummaryHour getSummaryHour(CollectDataPK pk)
			throws CollectKeyNotFound{
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			SummaryHour entity = null;
			entity = em.find(SummaryHour.class, pk, ObjectPrivilegeMode.NONE);
			if(entity == null){
				CollectKeyNotFound e = new CollectKeyNotFound("getSummaryHour, "+"collectorId = "+pk.getCollectorid()+" time = "+pk.getTime());
				throw e;
			}
			return entity;
		}
	}
	
	public static SummaryDay getSummaryDay(CollectDataPK pk)
			throws CollectKeyNotFound{
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			SummaryDay entity = null;
			entity = em.find(SummaryDay.class, pk, ObjectPrivilegeMode.NONE);
			if(entity == null){	
				CollectKeyNotFound e = new CollectKeyNotFound("getSummaryDay, "+"collectorId = "+pk.getCollectorid()+" time = "+pk.getTime());
				throw e;
			}
			return entity;
		}
	}
	
	public static SummaryMonth getSummaryMonth(CollectDataPK pk)
			throws CollectKeyNotFound{
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			SummaryMonth entity = null;
			entity = em.find(SummaryMonth.class, pk, ObjectPrivilegeMode.NONE);
			if(entity == null){	
				CollectKeyNotFound e = new CollectKeyNotFound("getSummaryMonth, "+"collectorId = "+pk.getCollectorid()+" time = "+pk.getTime());
				throw e;
			}
			return entity;
		}
	}
	
	public static List<CollectData> getCollectDataList(List<Integer> idList, Long fromTime, Long toTime, Integer timeout)
			throws HinemosDbTimeout {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collectoridList", idList);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		
		List<CollectData> list = QueryExecutor.getListByQueryNameWithTimeout("CollectData.findByTime", CollectData.class, parameters, timeout);
		return list;
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
	
	public static List<SummaryHour> getSummaryHourList(List<Integer> idList, Long fromTime, Long toTime, Integer timeout)
			throws HinemosDbTimeout {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collectoridList", idList);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		
		List<SummaryHour> list = QueryExecutor.getListByQueryNameWithTimeout("SummaryHour.findByTime", SummaryHour.class, parameters, timeout);
		return list;
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
	
	public static List<SummaryDay> getSummaryDayList(List<Integer> collectidList, Long fromTime, Long toTime, Integer timeout)
			throws HinemosDbTimeout {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collectoridList", collectidList);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		
		List<SummaryDay> list = QueryExecutor.getListByQueryNameWithTimeout("SummaryDay.findByTime", SummaryDay.class, parameters, timeout);
		return list;
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
	
	public static List<SummaryMonth> getSummaryMonthList(List<Integer> idList, Long fromTime, Long toTime, Integer timeout)
			throws HinemosDbTimeout {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("collectoridList", idList);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		
		List<SummaryMonth> list = QueryExecutor.getListByQueryNameWithTimeout("SummaryMonth.findByTime", SummaryMonth.class, parameters, timeout);
		return list;
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

	public static List<CollectKeyInfoPK> getCollectKeyAll() {
		List<CollectKeyInfoPK> list = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			list = em.createNamedQuery("CollectKeyInfoPk.findAll", CollectKeyInfoPK.class).getResultList();
		} catch (Exception e) {
			m_log.debug("getItemCode : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
		}
		return list;
	}

	public static List<CollectKeyInfo> getCollectKeyInfoAll() {
		List<CollectKeyInfo> list = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			list = em.createNamedQuery("CollectKeyInfo.findAll", CollectKeyInfo.class).getResultList();
		} catch (Exception e) {
			m_log.debug("getCollectKeyInfoAll : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
		}
		return list;
	}

	public static List<CollectKeyInfo> getCollectKeyInfoList(Integer collectorid){
		List<CollectKeyInfo> list = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			list = em.createNamedQuery("CollectKeyInfo.findByCollectId", CollectKeyInfo.class)
					.setParameter("collectorid", collectorid)
					.getResultList();
		}catch(Exception e){
			m_log.debug("getCollectKeyInfoList : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
		}
		return list;
		
	}

	public static List<CollectKeyInfo> getCollectKeyInfoListByMonitorId(String monitorId){
		List<CollectKeyInfo> list = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			list = em.createNamedQuery("CollectKeyInfo.findByMonitorId", CollectKeyInfo.class)
					.setParameter("monitorId", monitorId)
					.getResultList();
		}catch(Exception e){
			m_log.debug("getCollectKeyInfoListByMonitorId : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
		}
		return list;
		
	}

	public static List<CollectData> getCollectDataListOrderByTimeDesc(
			Integer id, Long startDate, Long endDate) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<CollectData> list = em.createNamedQuery("CollectData.findOrderByTimeDesc", CollectData.class)
					.setHint("eclipselink.jdbc.fetch-size", fetchSize)
					.setParameter("collectorid", id)
					.setParameter("startDate", startDate)
					.setParameter("endDate", endDate)
					.getResultList();
			return list;
		}
	}

	public static List<CollectData> getCollectDataListOrderByTimeAsc(
			Integer id, Long startDate, Long endDate) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<CollectData> list = em.createNamedQuery("CollectData.findOrderByTimeAsc", CollectData.class)
					.setHint("eclipselink.jdbc.fetch-size", fetchSize)
					.setParameter("collectorid", id)
					.setParameter("startDate", startDate)
					.setParameter("endDate", endDate)
					.getResultList();
			return list;
		}
	}
	/**
	 * 指定された条件に一致する収集データを返す
	 * 
	 * @param id　収集キー
	 * @param startDate 収集日（From）
	 * @param endDate 収集日（To）
	 * @param comparisonMethod 収集方法
	 * @param comparisonValue 収集値
	 * @return 収集データリスト
	 */
	public static List<CollectData> getCollectDataListByCondition(
			Integer id, Long startDate, Long endDate, String comparisonMethod, Double comparisonValue) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			StringBuilder sbJpql = new StringBuilder();
			sbJpql.append("SELECT a FROM CollectData a"
					+ " WHERE a.id.collectorid = :collectorid"
					+ " AND a.id.time > :startDate"
					+ " AND a.id.time <= :endDate");
			sbJpql.append(" AND a.value ");
			sbJpql.append(comparisonMethod);
			sbJpql.append(" :value");
			sbJpql.append(" ORDER BY a.id.time asc");

			List<CollectData> list = em.createQuery(sbJpql.toString(), CollectData.class)
					.setParameter("collectorid", id)
					.setParameter("startDate", startDate)
					.setParameter("endDate", endDate)
					.setParameter("value", comparisonValue)
					.getResultList();
			return list;
		}
	}
}