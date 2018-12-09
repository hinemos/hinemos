/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
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
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.reporting.fault.ReportingNotFound;
import com.clustercontrol.reporting.model.ReportingInfoEntity;
import com.clustercontrol.reporting.model.TemplateSetDetailInfoEntity;
import com.clustercontrol.reporting.model.TemplateSetDetailInfoEntityPK;
import com.clustercontrol.reporting.model.TemplateSetInfoEntity;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	/*
	 * ReportingInfoEntity
	 */
	public static ReportingInfoEntity getReportingInfoPK(String reportScheduleId) throws ReportingNotFound, InvalidRole {
		return getReportingInfoPK(reportScheduleId, ObjectPrivilegeMode.READ);
	}
	
	
	public static ReportingInfoEntity getReportingInfoPK(String reportScheduleId, ObjectPrivilegeMode mode) throws ReportingNotFound, InvalidRole {
		ReportingInfoEntity entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find(ReportingInfoEntity.class, reportScheduleId, mode);
			if (entity == null) {
				ReportingNotFound e = new ReportingNotFound("ReportingInfoEntity.findByPrimaryKey"
						+ ", reportScheduleId = " + reportScheduleId);
				m_log.info("getReportingInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getReportingInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static List<ReportingInfoEntity> getAllReportingInfoOrderByReportId() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<ReportingInfoEntity> list = em.createNamedQuery("ReportingInfoEntity.findAllOrderByReportId"
					,ReportingInfoEntity.class).getResultList();
			return list;
		}
	}

	public static List<ReportingInfoEntity> getReportingInfoFindByCalendarId_NONE(String calendarId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<ReportingInfoEntity> list
			= em.createNamedQuery("ReportingInfoEntity.findByCalendarId"
					,ReportingInfoEntity.class, ObjectPrivilegeMode.NONE)
					.setParameter("calendarId", calendarId)
					.getResultList();
			return list;
		}
	}
	
	
	/*
	 * TemplateSetInfoEntity
	 */
	public static TemplateSetInfoEntity getTemplateSetInfoPK(String templateSetId) throws ReportingNotFound, InvalidRole {
		return getTemplateSetInfoPK(templateSetId, ObjectPrivilegeMode.READ);
	}
	
	public static TemplateSetInfoEntity getTemplateSetInfoPK(String templateSetId, ObjectPrivilegeMode mode) throws ReportingNotFound, InvalidRole {
		TemplateSetInfoEntity entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find(TemplateSetInfoEntity.class, templateSetId, mode);
			if (entity == null) {
				ReportingNotFound e = new ReportingNotFound("TemplateSetInfoEntity.findByPrimaryKey"
						+ ", templateSetId = " + templateSetId);
				m_log.info("getTemplateSetInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getTemplateSetInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}
	
	public static TemplateSetInfoEntity getTemplateSetInfoPK_NONE(String templateSetId) throws ReportingNotFound, InvalidRole {
		TemplateSetInfoEntity entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find(TemplateSetInfoEntity.class, templateSetId, ObjectPrivilegeMode.NONE);
			if (entity == null) {
				ReportingNotFound e = new ReportingNotFound("TemplateSetInfoEntity.findByPrimaryKey"
						+ ", templateSetId = " + templateSetId);
				m_log.info("getTemplateSetInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getTemplateSetInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}
	
	public static TemplateSetInfoEntity getTemplateSetInfoPK_OR(String templateSetId, String ownerRoleId) throws ReportingNotFound, InvalidRole {
		TemplateSetInfoEntity entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find_OR(TemplateSetInfoEntity.class, templateSetId, ownerRoleId);
			if (entity == null) {
				ReportingNotFound e = new ReportingNotFound("TemplateSetInfoEntity.findByPrimaryKey"
						+ ", templateSetId = " + templateSetId);
				m_log.info("getTemplateSetInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getTemplateSetInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}
	
	public static List<TemplateSetInfoEntity> getAllTemplateSetInfo() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<TemplateSetInfoEntity> list
			= em.createNamedQuery("TemplateSetInfoEntity.findAll", TemplateSetInfoEntity.class)
			.getResultList();
			return list;
		}
	}
	
	public static List<TemplateSetInfoEntity> getAllTemplateSetInfo_OR(String ownerRoleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<TemplateSetInfoEntity> list
			= em.createNamedQuery_OR("TemplateSetInfoEntity.findAll", TemplateSetInfoEntity.class, ownerRoleId)
			.getResultList();
			return list;
		}
	}
	
	public static List<TemplateSetDetailInfoEntity> getTemplateSetDetailByTemplateSetId(String templateSetId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<TemplateSetDetailInfoEntity> list
			= em.createNamedQuery("TemplateSetDetailInfoEntity.findByTemplateSetId", TemplateSetDetailInfoEntity.class)
			.setParameter("templateSetId", templateSetId)
			.getResultList();
			return list;
		}
	}
	
	public static List<TemplateSetDetailInfoEntity> getTemplateSetDetailByTest(String templateSetId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<TemplateSetDetailInfoEntity> list
			= em.createNamedQuery("TemplateSetDetailInfoEntity.findTest", TemplateSetDetailInfoEntity.class)
			.setParameter("templateSetId", templateSetId)
			.getResultList();
			return list;
		}
	}
	
	
	public static TemplateSetDetailInfoEntity getTemplateSetDetailInfoPK(String templateSetId, Integer orderNo) throws ReportingNotFound {
		return getTemplateSetDetailInfoPK(new TemplateSetDetailInfoEntityPK(templateSetId, orderNo));
	}
	
	public static TemplateSetDetailInfoEntity getTemplateSetDetailInfoPK(TemplateSetDetailInfoEntityPK pk) throws ReportingNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			TemplateSetDetailInfoEntity entity = em.find(TemplateSetDetailInfoEntity.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				ReportingNotFound e = new ReportingNotFound("TemplateSetDetailInfoEntity.findByPrimaryKey, "
						+ pk.toString());
				m_log.info("getTemplateSetDetailInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}
}
