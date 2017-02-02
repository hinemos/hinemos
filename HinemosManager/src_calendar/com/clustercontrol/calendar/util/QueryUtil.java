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

package com.clustercontrol.calendar.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.calendar.model.CalendarDetailInfo;
import com.clustercontrol.calendar.model.CalendarDetailInfoPK;
import com.clustercontrol.calendar.model.CalendarInfo;
import com.clustercontrol.calendar.model.YMD;
import com.clustercontrol.calendar.model.YMDPK;
import com.clustercontrol.calendar.model.CalendarPatternInfo;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static CalendarInfo getCalInfoPK(String calendarId) throws CalendarNotFound, InvalidRole {
		return getCalInfoPK(calendarId, ObjectPrivilegeMode.READ);
	}

	public static CalendarInfo getCalInfoPK(String calendarId, ObjectPrivilegeMode mode) throws CalendarNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		CalendarInfo entity = null;
		try {
			entity = em.find(CalendarInfo.class, calendarId, mode);
			if (entity == null) {
				CalendarNotFound e = new CalendarNotFound("CalInfoEntity.findByPrimaryKey, " +
						"calendarId = " + calendarId);
				m_log.info("getCalInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getCalInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static CalendarInfo getCalInfoPK_NONE(String calendarId) throws CalendarNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		CalendarInfo entity = null;
		try {
			entity = em.find(CalendarInfo.class, calendarId, ObjectPrivilegeMode.NONE);
			if (entity == null) {
				CalendarNotFound e = new CalendarNotFound("CalInfoEntity.findByPrimaryKey, " +
						"calendarId = " + calendarId);
				m_log.info("getCalInfoPK_NONE() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			// NONE（オブジェクト権限チェックなし）のため、ここは通らない。
		}
		return entity;
	}

	public static CalendarInfo getCalInfoPK_OR(String calendarId, String ownerRoleId) throws CalendarNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		CalendarInfo entity = null;
		try {
			entity = em.find_OR(CalendarInfo.class, calendarId, ownerRoleId);
			if (entity == null) {
				CalendarNotFound e = new CalendarNotFound("CalInfoEntity.findByPrimaryKey, " +
						"calendarId = " + calendarId);
				m_log.info("getCalInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getCalInfoPK_OR() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}


	public static CalendarDetailInfo getCalDetailInfoPK(String calendarId, Integer orderNo) throws CalendarNotFound {
		return getCalDetailInfoPK(new CalendarDetailInfoPK(calendarId, orderNo));
	}

	public static CalendarDetailInfo getCalDetailInfoPK(CalendarDetailInfoPK pk) throws CalendarNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		CalendarDetailInfo entity = em.find(CalendarDetailInfo.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			CalendarNotFound e = new CalendarNotFound("CalDetailInfoEntity.findByPrimaryKey, "
					+ pk.toString());
			m_log.info("getCalDetailInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static CalendarPatternInfo getCalPatternInfoPK(String calPatternId) throws CalendarNotFound, InvalidRole {
		return getCalPatternInfoPK(calPatternId, ObjectPrivilegeMode.READ);
	}

	public static CalendarPatternInfo getCalPatternInfoPK(String calPatternId, ObjectPrivilegeMode mode) throws CalendarNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		CalendarPatternInfo entity = null;
		try {
			entity = em.find(CalendarPatternInfo.class, calPatternId, mode);
			if (entity == null) {
				CalendarNotFound e = new CalendarNotFound("CalPatternInfoEntity.findByPrimaryKey, " +
						"calPatternId = " + calPatternId);
				m_log.info("getCalPatternInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getCalPatternInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static CalendarPatternInfo getCalPatternInfoPK_OR(String calPatternId, String ownerRoleId) throws CalendarNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		CalendarPatternInfo entity = null;
		try {
			entity = em.find_OR(CalendarPatternInfo.class, calPatternId, ownerRoleId);
			if (entity == null) {
				CalendarNotFound e = new CalendarNotFound("CalPatternInfoEntity.findByPrimaryKey, " +
						"calPatternId = " + calPatternId);
				m_log.info("getCalPatternInfoPK_OR() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getCalPatternInfoPK_OR() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static YMD getCalPatternDetailInfoPK(String calendarId,
			Integer yearNo, Integer monthNo, Integer dayNo) throws CalendarNotFound {
		return getCalPatternDetailInfoPK(new YMDPK(calendarId, yearNo, monthNo, dayNo));
	}

	public static YMD getCalPatternDetailInfoPK(YMDPK pk) throws CalendarNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		YMD entity = em.find(YMD.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			CalendarNotFound e = new CalendarNotFound("CalPatternDetailInfoEntity.findByPrimaryKey, "
					+ pk.toString());
			m_log.info("getCalPatternDetailInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static List<CalendarInfo> getAllCalInfo() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CalendarInfo> list
		= em.createNamedQuery("CalInfoEntity.findAll", CalendarInfo.class).getResultList();
		return list;
	}

	public static List<CalendarInfo> getAllCalInfo_OR(String ownerRoleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CalendarInfo> list
		= em.createNamedQuery_OR("CalInfoEntity.findAll", CalendarInfo.class, ownerRoleId)
		.getResultList();
		return list;
	}

	public static List<CalendarInfo> getCalInfoFindByOwnerRoleId_NONE(String roleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CalendarInfo> list
		= em.createNamedQuery("CalInfoEntity.findByOwnerRoleId", CalendarInfo.class, ObjectPrivilegeMode.NONE)
		.setParameter("ownerRoleId", roleId)
		.getResultList();
		return list;
	}

	public static List<CalendarDetailInfo> getAllCalDetailInfo() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CalendarDetailInfo> list
		= em.createNamedQuery("CalDetailInfoEntity.findAll", CalendarDetailInfo.class).getResultList();
		return list;
	}

	public static List<CalendarDetailInfo> getCalDetailByCalendarId(String calendarId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CalendarDetailInfo> list
		= em.createNamedQuery("CalDetailInfoEntity.findByCalendarId", CalendarDetailInfo.class)
		.setParameter("calendarId", calendarId)
		.getResultList();
		return list;
	}

	public static List<CalendarDetailInfo> getCalDetailByCalPatternId(String calPatternId){
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CalendarDetailInfo> list
		= em.createNamedQuery("CalDetailInfoEntity.findByCalPatternId", CalendarDetailInfo.class)
		.setParameter("calPatternId", calPatternId)
		.getResultList();
		return list;
	}

	public static List<CalendarPatternInfo> getAllCalPatternInfo() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CalendarPatternInfo> list
		= em.createNamedQuery("CalPatternInfoEntity.findAll", CalendarPatternInfo.class).getResultList();
		return list;
	}

	public static List<CalendarPatternInfo> getAllCalPatternInfo_OR(String ownerRoleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CalendarPatternInfo> list
		= em.createNamedQuery_OR("CalPatternInfoEntity.findAll", CalendarPatternInfo.class, ownerRoleId)
		.getResultList();
		return list;
	}

	public static List<CalendarPatternInfo> getCalPatternInfoFindByOwnerRoleId_NONE(String roleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CalendarPatternInfo> list
		= em.createNamedQuery("CalPatternInfoEntity.findByOwnerRoleId", CalendarPatternInfo.class, ObjectPrivilegeMode.NONE)
		.setParameter("ownerRoleId", roleId)
		.getResultList();
		return list;
	}

	public static List<YMD> getCalPatternDetailByCalPatternId(String calPatternId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<YMD> list
		= em.createNamedQuery("CalPatternDetailInfoEntity.findByCalendarPatternId", YMD.class)
		.setParameter("calPatternId", calPatternId)
		.getResultList();
		return list;
	}
}
