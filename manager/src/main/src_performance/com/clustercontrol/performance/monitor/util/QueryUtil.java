/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.performance.monitor.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.CollectorNotFound;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.performance.monitor.model.CollectorCalcMethodMstEntity;
import com.clustercontrol.performance.monitor.model.CollectorCategoryCollectMstEntity;
import com.clustercontrol.performance.monitor.model.CollectorCategoryCollectMstEntityPK;
import com.clustercontrol.performance.monitor.model.CollectorCategoryMstEntity;
import com.clustercontrol.performance.monitor.model.CollectorItemCalcMethodMstEntity;
import com.clustercontrol.performance.monitor.model.CollectorItemCalcMethodMstEntityPK;
import com.clustercontrol.performance.monitor.model.CollectorItemCodeMstEntity;
import com.clustercontrol.performance.monitor.model.CollectorPollingMstEntity;
import com.clustercontrol.performance.monitor.model.CollectorPollingMstEntityPK;
import com.clustercontrol.performance.monitor.model.PerfCheckInfo;
import com.clustercontrol.performance.monitor.model.SnmpValueTypeMstEntity;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static CollectorCalcMethodMstEntity getCollectorCalcMethodMstPK(String calcMethod) throws CollectorNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		CollectorCalcMethodMstEntity entity = em.find(CollectorCalcMethodMstEntity.class, calcMethod, ObjectPrivilegeMode.READ);
		if (entity == null) {
			CollectorNotFound e = new CollectorNotFound("CollectorCalcMethodMstEntity.findByPrimaryKey"
					+ "calcMethod = " + calcMethod);
			m_log.info("getCollectorCalcMethodMstPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static List<CollectorCalcMethodMstEntity> getAllCollectorCalcMethodMst() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CollectorCalcMethodMstEntity> list
		= em.createNamedQuery("CollectorCalcMethodMstEntity.findAll", CollectorCalcMethodMstEntity.class)
		.getResultList();
		return list;
	}

	public static CollectorCategoryMstEntity getCollectorCategoryMstPK(String categoryCode) throws CollectorNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		CollectorCategoryMstEntity entity = em.find(CollectorCategoryMstEntity.class, categoryCode, ObjectPrivilegeMode.READ);
		if (entity == null) {
			CollectorNotFound e = new CollectorNotFound("CollectorCategoryMstEntity.findByPrimaryKey"
					+ "categoryCode = " + categoryCode);
			m_log.info("getCollectorCategoryMstPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static List<CollectorCategoryMstEntity> getAllCollectorCategoryMst() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CollectorCategoryMstEntity> list
		= em.createNamedQuery("CollectorCategoryMstEntity.findAll", CollectorCategoryMstEntity.class)
		.getResultList();
		return list;
	}

	public static CollectorCategoryCollectMstEntity getCollectorCategoryCollectMstPK(CollectorCategoryCollectMstEntityPK pk) throws CollectorNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		CollectorCategoryCollectMstEntity entity = em.find(CollectorCategoryCollectMstEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			CollectorNotFound e = new CollectorNotFound("CollectorCategoryCollectMstEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getCollectorCategoryCollectMstPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static CollectorCategoryCollectMstEntity getCollectorCategoryCollectMstPK(String platformId, String subPlatformId, String categoryCode) throws CollectorNotFound {
		return getCollectorCategoryCollectMstPK(new CollectorCategoryCollectMstEntityPK(platformId, subPlatformId, categoryCode));
	}

	public static List<CollectorCategoryCollectMstEntity> getAllCollectorCategoryCollectMst() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CollectorCategoryCollectMstEntity> list
		= em.createNamedQuery("CollectorCategoryCollectMstEntity.findAll", CollectorCategoryCollectMstEntity.class)
		.getResultList();
		return list;
	}

	public static List<CollectorCategoryCollectMstEntity> getCollectorCategoryCollectMstByPlatformIdSubPlatformId(
			String platformId,
			String subPlatformId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CollectorCategoryCollectMstEntity> list
		= em.createNamedQuery("CollectorCategoryCollectMstEntity.findByPlatformIdAndSubPlatformId", CollectorCategoryCollectMstEntity.class)
		.setParameter("platformId", platformId)
		.setParameter("subPlatformId", subPlatformId)
		.getResultList();
		return list;
	}

	public static CollectorItemCodeMstEntity getCollectorItemCodeMstPK(String itemCode) throws CollectorNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		CollectorItemCodeMstEntity entity = em.find(CollectorItemCodeMstEntity.class, itemCode, ObjectPrivilegeMode.READ);
		if (entity == null) {
			CollectorNotFound e = new CollectorNotFound("CollectorItemCodeMstEntity.findByPrimaryKey"
					+ "itemCode = " + itemCode);
			m_log.info("getCollectorItemCodeMstPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static List<CollectorItemCodeMstEntity> getAllCollectorItemCodeMst() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CollectorItemCodeMstEntity> list
		= em.createNamedQuery("CollectorItemCodeMstEntity.findAll", CollectorItemCodeMstEntity.class)
		.getResultList();
		return list;
	}

	public static CollectorItemCalcMethodMstEntity getCollectorItemCalcMethodMstPK(String collectMethod,
			String platformId,
			String subPlatformId,
			String itemCode) throws CollectorNotFound {
		return getCollectorItemCalcMethodMstPK(new CollectorItemCalcMethodMstEntityPK(collectMethod,
				platformId,
				subPlatformId,
				itemCode));
	}

	public static CollectorItemCalcMethodMstEntity getCollectorItemCalcMethodMstPK(CollectorItemCalcMethodMstEntityPK pk) throws CollectorNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		CollectorItemCalcMethodMstEntity entity = em.find(CollectorItemCalcMethodMstEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			CollectorNotFound e = new CollectorNotFound("CollectorItemCalcMethodMstEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getCollectorItemCalcMethodMstPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static List<CollectorItemCalcMethodMstEntity> getAllCollectorItemCalcMethodMst() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CollectorItemCalcMethodMstEntity> list
		= em.createNamedQuery("CollectorItemCalcMethodMstEntity.findAll", CollectorItemCalcMethodMstEntity.class)
		.getResultList();
		return list;
	}

	public static List<CollectorItemCalcMethodMstEntity> getCollectorItemCalcMethodMstByPlatformIdSubPlatformId(
			String platformId, String subPlatformId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CollectorItemCalcMethodMstEntity> list
		= em.createNamedQuery("CollectorItemCalcMethodMstEntity.findByPlatformIdAndSubPlatformId", CollectorItemCalcMethodMstEntity.class)
		.setParameter("platformId", platformId)
		.setParameter("subPlatformId", subPlatformId)
		.getResultList();
		return list;
	}

	public static SnmpValueTypeMstEntity getSnmpValueTypeMstPK(String valueType) throws CollectorNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		SnmpValueTypeMstEntity entity = em.find(SnmpValueTypeMstEntity.class, valueType, ObjectPrivilegeMode.READ);
		if (entity == null) {
			CollectorNotFound e = new CollectorNotFound("CollectorItemCalcMethodMstEntity.findByPrimaryKey, "
					+ "valueType = " + valueType);
			m_log.info("getSnmpValueTypeMstPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static CollectorPollingMstEntity getCollectorPollingMstPK(String collectMethod,
			String platformId,
			String subPlatformId,
			String itemCode,
			String variableId) throws CollectorNotFound {
		return getCollectorPollingMstPK(new CollectorPollingMstEntityPK(collectMethod,
				platformId,
				subPlatformId,
				itemCode,
				variableId));
	}

	public static CollectorPollingMstEntity getCollectorPollingMstPK(CollectorPollingMstEntityPK pk) throws CollectorNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		CollectorPollingMstEntity entity = em.find(CollectorPollingMstEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			CollectorNotFound e = new CollectorNotFound("CollectorPollingMstEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getCollectorPollingMstPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static List<CollectorPollingMstEntity> getAllCollectorPollingMst() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CollectorPollingMstEntity> list
		= em.createNamedQuery("CollectorPollingMstEntity.findAll", CollectorPollingMstEntity.class)
		.getResultList();
		return list;
	}

	public static List<CollectorPollingMstEntity> getAllCollectorPollingMstVariableId(String collectMethod,
			String platformId,
			String subPlatformId,
			String itemCode) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CollectorPollingMstEntity> list =
				em.createNamedQuery("CollectorPollingMstEntity.findVariableId", CollectorPollingMstEntity.class)
				.setParameter("collectMethod", collectMethod)
				.setParameter("platformId", platformId)
				.setParameter("subPlatformId", subPlatformId)
				.setParameter("itemCode", itemCode)
				.getResultList();
		return list;
	}

	public static List<CollectorPollingMstEntity> getCollectorPollingMst() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CollectorPollingMstEntity> list
		= em.createNamedQuery("CollectorPollingMstEntity.findAll", CollectorPollingMstEntity.class)
		.getResultList();
		return list;
	}

	public static PerfCheckInfo getMonitorPerfInfoPK(String monitorId) throws MonitorNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		PerfCheckInfo entity = em.find(PerfCheckInfo.class, monitorId, ObjectPrivilegeMode.READ);
		if (entity == null) {
			MonitorNotFound e = new MonitorNotFound("CollectorPollingMstEntity.findByPrimaryKey, "
					+ "monitorId = " + monitorId);
			m_log.info("getMonitorPerfInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}
}
