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

package com.clustercontrol.monitor.run.util;

import java.util.List;

import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.model.MonitorNumericValueInfo;
import com.clustercontrol.monitor.run.model.MonitorNumericValueInfoPK;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfo;
import com.clustercontrol.monitor.run.model.MonitorStringValueInfoPK;
import com.clustercontrol.monitor.run.model.MonitorTruthValueInfo;
import com.clustercontrol.monitor.run.model.MonitorTruthValueInfoPK;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static MonitorInfo getMonitorInfoPK(String monitorId) throws MonitorNotFound, InvalidRole {
		return getMonitorInfoPK(monitorId, ObjectPrivilegeMode.READ);
	}

	public static MonitorInfo getMonitorInfoPK(String monitorId, ObjectPrivilegeMode mode) throws MonitorNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MonitorInfo entity = null;
		try {
			entity = em.find(MonitorInfo.class, monitorId, mode);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound("MonitorInfo.findByPrimaryKey"
						+ ", monitorId = " + monitorId);
				m_log.info("getMonitorInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getMonitorInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static MonitorInfo getMonitorInfoPK_OR(String monitorId, String ownerRoleId) throws MonitorNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MonitorInfo entity = null;
		try {
			entity = em.find_OR(MonitorInfo.class, monitorId, ownerRoleId);
			if (entity == null) {
				MonitorNotFound e = new MonitorNotFound("MonitorInfo.findByPrimaryKey"
						+ ", monitorId = " + monitorId);
				m_log.info("getMonitorInfoPK_OR() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getMonitorInfoPK_OR() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static MonitorInfo getMonitorInfoPK_NONE(String monitorId) throws MonitorNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MonitorInfo entity
		= em.find(MonitorInfo.class, monitorId, ObjectPrivilegeMode.NONE);
		if (entity == null) {
			MonitorNotFound e = new MonitorNotFound("MonitorInfo.findByPrimaryKey"
					+ ", monitorId = " + monitorId);
			m_log.info("getMonitorInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static List<MonitorInfo> getAllMonitorInfo() throws HinemosUnknown {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MonitorInfo> list
		= em.createNamedQuery("MonitorInfo.findAll", MonitorInfo.class)
		.getResultList();
		return list;
	}

	public static List<MonitorInfo> getMonitorInfoByOwnerRoleId_NONE(String roleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MonitorInfo> list
		= em.createNamedQuery("MonitorInfo.findByOwnerRoleId", MonitorInfo.class, ObjectPrivilegeMode.NONE)
		.setParameter("ownerRoleId", roleId)
		.getResultList();
		return list;
	}

	public static List<MonitorInfo> getMonitorInfoByFacilityId_NONE(String facilityId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MonitorInfo> list
		= em.createNamedQuery("MonitorInfo.findByFacilityId", MonitorInfo.class, ObjectPrivilegeMode.NONE)
		.setParameter("facilityId", facilityId)
		.getResultList();
		return list;
	}

	public static List<MonitorInfo> getMonitorInfoByMonitorTypeId(String monitorTypeId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MonitorInfo> list
		= em.createNamedQuery("MonitorInfo.findByMonitorTypeId", MonitorInfo.class)
		.setParameter("monitorTypeId", monitorTypeId)
		.getResultList();
		return list;
	}

	public static List<MonitorInfo> getMonitorInfoByMonitorTypeId_NONE(String monitorTypeId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MonitorInfo> list
		= em.createNamedQuery("MonitorInfo.findByMonitorTypeId", MonitorInfo.class, ObjectPrivilegeMode.NONE)
		.setParameter("monitorTypeId", monitorTypeId)
		.getResultList();
		return list;
	}

	public static MonitorNumericValueInfo getMonitorNumericValueInfoPK(MonitorNumericValueInfoPK pk) throws MonitorNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MonitorNumericValueInfo entity = em.find(MonitorNumericValueInfo.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			MonitorNotFound e = new MonitorNotFound("MonitorNumericValueInfoEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getMonitorNumericValueInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static MonitorNumericValueInfo getMonitorNumericValueInfoPK(String monitorId, Integer priority) throws MonitorNotFound {
		return getMonitorNumericValueInfoPK(new MonitorNumericValueInfoPK(monitorId, priority));
	}

	public static MonitorStringValueInfo getMonitorStringValueInfoPK(MonitorStringValueInfoPK pk) throws MonitorNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MonitorStringValueInfo entity = em.find(MonitorStringValueInfo.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			MonitorNotFound e = new MonitorNotFound("MonitorStringValueInfoEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getMonitorStringValueInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static MonitorStringValueInfo getMonitorStringValueInfoPK(String monitorId, Integer orderNo) throws MonitorNotFound {
		return getMonitorStringValueInfoPK(new MonitorStringValueInfoPK(monitorId, orderNo));
	}

	public static MonitorTruthValueInfo getMonitorTruthValueInfoPK(MonitorTruthValueInfoPK pk) throws MonitorNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MonitorTruthValueInfo entity = em.find(MonitorTruthValueInfo.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			MonitorNotFound e = new MonitorNotFound("MonitorTruthValueInfoEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getMonitorTruthValueInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}
	public static List<MonitorInfo> getMonitorInfoFindByCalendarId_NONE(String calendarId){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MonitorInfo> monitorInfoList
		= em.createNamedQuery("MonitorInfo.findByCalendarId", MonitorInfo.class, ObjectPrivilegeMode.NONE)
		.setParameter("calendarId", calendarId).getResultList();
		return monitorInfoList;
	}

	public static MonitorTruthValueInfo getMonitorTruthValueInfoPK(String monitorId, Integer priority, Integer truthValue) throws MonitorNotFound {
		return getMonitorTruthValueInfoPK(new MonitorTruthValueInfoPK(monitorId, priority, truthValue));
	}

	public static List<MonitorTruthValueInfo> getMonitorTruthValueInfoFindByMonitorId(String monitorId, ObjectPrivilegeMode mode){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MonitorTruthValueInfo> monitorTruthValueInfoList
		= em.createNamedQuery("MonitorTruthValueInfo.findByMonitorId", MonitorTruthValueInfo.class, mode)
		.setParameter("monitorId", monitorId).getResultList();
		return monitorTruthValueInfoList;
	}

	public static List<MonitorStringValueInfo> getMonitorStringValueInfoFindByMonitorId(String monitorId, ObjectPrivilegeMode mode){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MonitorStringValueInfo> monitorStringValueInfoList
		= em.createNamedQuery("MonitorStringValueInfo.findByMonitorId", MonitorStringValueInfo.class, mode)
		.setParameter("monitorId", monitorId).getResultList();
		return monitorStringValueInfoList;
	}

	public static List<MonitorNumericValueInfo> getMonitorNumericValueInfoFindByMonitorId(String monitorId, ObjectPrivilegeMode mode){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MonitorNumericValueInfo> monitorNumericValueInfoList
		= em.createNamedQuery("MonitorNumericValueInfo.findByMonitorId", MonitorNumericValueInfo.class, mode)
		.setParameter("monitorId", monitorId).getResultList();
		return monitorNumericValueInfoList;
	}

	public static List<MonitorInfo> getMonitorInfoByFilter(
			String monitorId,
			String monitorTypeId,
			String description,
			String calendarId,
			String regUser,
			Long regFromDate,
			Long regToDate,
			String updateUser,
			Long updateFromDate,
			Long updateToDate,
			Boolean monitorFlg,
			Boolean collectorFlg,
			String ownerRoleId) {

		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();

		// 「含まない」検索を行うかの判断に使う値
		String notInclude = "NOT:";
	
		StringBuffer sbJpql = new StringBuffer();
		sbJpql.append("SELECT a FROM MonitorInfo a WHERE true = true");
		// monitorId
		if(monitorId != null && !"".equals(monitorId)) {
			if(!monitorId.startsWith(notInclude)) {
				sbJpql.append(" AND a.monitorId like :monitorId");
			}else{
				sbJpql.append(" AND a.monitorId not like :monitorId");
			}
		}
		// monitorTypeId
		if(monitorTypeId != null && !"".equals(monitorTypeId)) {
			if(!monitorTypeId.startsWith(notInclude)) {
				sbJpql.append(" AND a.monitorTypeId like :monitorTypeId");
			}else{
				sbJpql.append(" AND a.monitorTypeId not like :monitorTypeId");
			}
		}
		// description
		if(description != null && !"".equals(description)) {
			if(!description.startsWith(notInclude)) {
				sbJpql.append(" AND a.description like :description");
			}else{
				sbJpql.append(" AND a.description not like :description");
			}
		}
		// calendarId
		if(calendarId != null && !"".equals(calendarId)) {
			sbJpql.append(" AND a.calendarId like :calendarId");
		}
		// regUser
		if(regUser != null && !"".equals(regUser)) {
			if(!regUser.startsWith(notInclude)) {
				sbJpql.append(" AND a.regUser like :regUser");
			}else{
				sbJpql.append(" AND a.regUser not like :regUser");
			}
		}
		// regFromDate
		if (regFromDate > 0) {
			sbJpql.append(" AND a.regDate >= :regFromDate");
		}
		// regToDate
		if (regToDate > 0){
			sbJpql.append(" AND a.regDate <= :regToDate");
		}
		// updateUser
		if(updateUser != null && !"".equals(updateUser)) {
			if (!updateUser.startsWith(notInclude)) {
				sbJpql.append(" AND a.updateUser like :updateUser");
			}else{
				sbJpql.append(" AND a.updateUser not like :updateUser");
			}
		}
		// updateFromDate
		if(updateFromDate > 0) {
			sbJpql.append(" AND a.updateDate >= :updateFromDate");
		}
		// updateToDate
		if(updateToDate > 0) {
			sbJpql.append(" AND a.updateDate <= :updateToDate");
		}
		// monitorFlg
		if(monitorFlg != null) {
			sbJpql.append(" AND a.monitorFlg = :monitorFlg");
		}
		// collectorFlg
		if(collectorFlg != null) {
			sbJpql.append(" AND a.collectorFlg = :collectorFlg");
		}
		// ownerRoleId
		if(ownerRoleId != null && !"".equals(ownerRoleId)) {
			if (!ownerRoleId.startsWith(notInclude)) {
				sbJpql.append(" AND a.ownerRoleId like :ownerRoleId");
			}else{
				sbJpql.append(" AND a.ownerRoleId not like :ownerRoleId");
			}
		}
		TypedQuery<MonitorInfo> typedQuery = em.createQuery(sbJpql.toString(), MonitorInfo.class);

		// monitorId
		if(monitorId != null && !"".equals(monitorId)) {
			if(!monitorId.startsWith(notInclude)) {
				typedQuery = typedQuery.setParameter("monitorId", monitorId);
			}else{
				typedQuery = typedQuery.setParameter("monitorId", monitorId.substring(notInclude.length()));
			}
		}
		// monitorTypeId
		if(monitorTypeId != null && !"".equals(monitorTypeId)) {
			if(!monitorTypeId.startsWith(notInclude)) {
				typedQuery = typedQuery.setParameter("monitorTypeId", monitorTypeId);
			}else{
				typedQuery = typedQuery.setParameter("monitorTypeId", monitorTypeId.substring(notInclude.length()));
			}
		}
		// description
		if(description != null && !"".equals(description)) {
			if(!description.startsWith(notInclude)) {
				typedQuery = typedQuery.setParameter("description", description);
			}else{
				typedQuery = typedQuery.setParameter("description", description.substring(notInclude.length()));
			}
		}
		// calendarId
		if(calendarId != null && !"".equals(calendarId)) {
			typedQuery = typedQuery.setParameter("calendarId", calendarId);
		}
		// regUser
		if(regUser != null && !"".equals(regUser)) {
			if(!regUser.startsWith(notInclude)) {
				typedQuery = typedQuery.setParameter("regUser", regUser);
			}else{
				typedQuery = typedQuery.setParameter("regUser", regUser.substring(notInclude.length()));
			}
		}
		// regFromDate
		if (regFromDate > 0) {
			typedQuery = typedQuery.setParameter("regFromDate", regFromDate);
		}
		// regToDate
		if (regToDate > 0){
			typedQuery = typedQuery.setParameter("regToDate", regToDate);
		}
		// updateUser
		if(updateUser != null && !"".equals(updateUser)) {
			if(!updateUser.startsWith(notInclude)) {
				typedQuery = typedQuery.setParameter("updateUser", updateUser);
			}else{
				typedQuery = typedQuery.setParameter("updateUser", updateUser.substring(notInclude.length()));
			}
		}
		// updateFromDate
		if(updateFromDate > 0) {
			typedQuery = typedQuery.setParameter("updateFromDate", updateFromDate);
		}
		// updateToDate
		if(updateToDate > 0) {
			typedQuery = typedQuery.setParameter("updateToDate", updateToDate);
		}
		// monitorFlg
		if(monitorFlg != null) {
			typedQuery = typedQuery.setParameter("monitorFlg", monitorFlg);
		}
		// collectorFlg
		if(collectorFlg != null) {
			typedQuery = typedQuery.setParameter("collectorFlg", collectorFlg);
		}
		// ownerRoleId
		if(ownerRoleId != null && !"".equals(ownerRoleId)) {
			if(!ownerRoleId.startsWith(notInclude)) {
				typedQuery = typedQuery.setParameter("ownerRoleId", ownerRoleId);
			}else{
				typedQuery = typedQuery.setParameter("ownerRoleId", ownerRoleId.substring(notInclude.length()));
			}
		}
		return typedQuery.getResultList();
	}

}
