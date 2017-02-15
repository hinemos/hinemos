package com.clustercontrol.collect.util;

import java.util.List;

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
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.CollectKeyNotFound;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(QueryUtil.class);
	static long fetchSize = HinemosPropertyUtil.getHinemosPropertyNum("performance.export.fetchsize", 1000l);
	
	public static CollectKeyInfo getCollectKeyPK(CollectKeyInfoPK pk) throws CollectKeyNotFound{
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		CollectKeyInfo entity=null;
		entity = em.find(CollectKeyInfo.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			CollectKeyNotFound e = new CollectKeyNotFound(pk.toString());
			m_log.info("getMonitorInfoPK() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		
		return entity;
	}
	
	public static Integer getMaxId(){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		Integer maxid = null;
		try {
			maxid = em.createNamedQuery("CollectKeyInfo.findMaxId",Integer.class).getSingleResult();
		} catch (NoResultException e) {
			m_log.debug("getMaxId : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
		}
		return maxid;
	}
	
	public static SummaryHour getSummaryHour(CollectDataPK pk)
			throws CollectKeyNotFound{
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		SummaryHour entity = null;
		entity = em.find(SummaryHour.class, pk, ObjectPrivilegeMode.NONE);
		if(entity == null){
			CollectKeyNotFound e = new CollectKeyNotFound("getSummaryHour, "+"collectorId = "+pk.getCollectorid()+" time = "+pk.getTime());
			throw e;
		}
		return entity;
	}
	
	public static SummaryDay getSummaryDay(CollectDataPK pk)
			throws CollectKeyNotFound{
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		SummaryDay entity = null;
		entity = em.find(SummaryDay.class, pk, ObjectPrivilegeMode.NONE);
		if(entity == null){	
			CollectKeyNotFound e = new CollectKeyNotFound("getSummaryDay, "+"collectorId = "+pk.getCollectorid()+" time = "+pk.getTime());
			throw e;
		}
			return entity;
	}
	
	public static SummaryMonth getSummaryMonth(CollectDataPK pk)
			throws CollectKeyNotFound{
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		SummaryMonth entity = null;
		entity = em.find(SummaryMonth.class, pk, ObjectPrivilegeMode.NONE);
		if(entity == null){	
			CollectKeyNotFound e = new CollectKeyNotFound("getSummaryMonth, "+"collectorId = "+pk.getCollectorid()+" time = "+pk.getTime());
			throw e;
		}
			return entity;
	}
	
	public static List<CollectData> getCollectDataList(List<Integer> idList, Long fromTime, Long toTime) {
		setStatementTimeout();
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CollectData> list = em.createNamedQuery("CollectData.findByTime", CollectData.class)
				.setParameter("collectoridList", idList)
				.setParameter("fromTime", fromTime)
				.setParameter("toTime", toTime)
				.getResultList();
		resetStatementTimeout();
		return list;
	}
	
	public static List<CollectData> getCollectDataList(Integer id) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CollectData> list = em.createNamedQuery("CollectData.find", CollectData.class)
				.setHint("eclipselink.jdbc.fetch-size", fetchSize)
				.setParameter("collectorid", id)
				.getResultList();
		return list;
	}
	
	public static List<SummaryHour> getSummaryHourList(List<Integer> idList, Long fromTime, Long toTime) {
		setStatementTimeout();
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<SummaryHour> list = em.createNamedQuery("SummaryHour.findByTime", SummaryHour.class)
				.setParameter("collectoridList", idList)
				.setParameter("fromTime", fromTime)
				.setParameter("toTime", toTime)
				.getResultList();
		resetStatementTimeout();
		return list;
	}
	
	public static List<SummaryHour> getSummaryHourList(Integer id) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<SummaryHour> list = em.createNamedQuery("SummaryHour.find", SummaryHour.class)
				.setHint("eclipselink.jdbc.fetch-size", fetchSize)
				.setParameter("collectorid", id)
				.getResultList();
		return list;
	}
	
	public static List<SummaryDay> getSummaryDayList(List<Integer> collectidList, Long fromTime, Long toTime) {
		setStatementTimeout();
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<SummaryDay> list = em.createNamedQuery("SummaryDay.findByTime", SummaryDay.class)
				.setParameter("collectoridList", collectidList)
				.setParameter("fromTime", fromTime)
				.setParameter("toTime", toTime)
				.getResultList();
		resetStatementTimeout();
		return list;
	}
	
	public static List<SummaryDay> getSummaryDayList(Integer id) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<SummaryDay> list = em.createNamedQuery("SummaryDay.find", SummaryDay.class)
				.setHint("eclipselink.jdbc.fetch-size", fetchSize)
				.setParameter("collectorid", id)
				.getResultList();
		return list;
	}
	
	public static List<SummaryMonth> getSummaryMonthList(List<Integer> idList, Long fromTime, Long toTime) {
		setStatementTimeout();
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<SummaryMonth> list = em.createNamedQuery("SummaryMonth.findByTime", SummaryMonth.class)
				.setParameter("collectoridList", idList)
				.setParameter("fromTime", fromTime)
				.setParameter("toTime", toTime)
				.getResultList();
		resetStatementTimeout();
		return list;
	}
	
	public static List<SummaryMonth> getSummaryMonthList(Integer id) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<SummaryMonth> list = em.createNamedQuery("SummaryMonth.find", SummaryMonth.class)
				.setHint("eclipselink.jdbc.fetch-size", fetchSize)
				.setParameter("collectorid", id)
				.getResultList();
		return list;
	}

	public static List<CollectKeyInfoPK> getCollectKeyAll() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CollectKeyInfoPK> list = null;
		try {
			list = em.createNamedQuery("CollectKeyInfo.findAll", CollectKeyInfoPK.class).getResultList();
		} catch (Exception e) {
			m_log.debug("getItemCode : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
		}
		return list;
	}
	
	public static List<CollectKeyInfo> getCollectKeyInfoList(Integer collectorid){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CollectKeyInfo> list = null;
		try{
			list = em.createNamedQuery("CollectKeyInfo.findByCollectId", CollectKeyInfo.class)
					.setParameter("collectorid", collectorid)
					.getResultList();
		}catch(Exception e){
			m_log.debug("getCollectKeyInfoList : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
		}
		return list;
		
	}
	
	private static void setStatementTimeout() {
		long timeout = HinemosPropertyUtil.getHinemosPropertyNum("collect.graph.timeout", Long.valueOf(50000));
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		String sql = "SET local statement_timeout TO " + timeout + ";";
		m_log.trace("setStatementTimeout : " + sql);
		try {
			em.createNativeQuery(sql).executeUpdate();
		} catch(Exception e) {
			m_log.error("setStatementTimeout ERROR statement:" + sql);
		}
	}
	
	private static void resetStatementTimeout() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		String sql = "RESET statement_timeout;";
		m_log.trace("resetStatementTimeout : " + sql);
		try {
			em.createNativeQuery(sql).executeUpdate();
		} catch(Exception e) {
			m_log.error("resetStatementTimeout ERROR statement:" + sql);
		}
	}
}