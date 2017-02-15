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

package com.clustercontrol.notify.mail.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MailTemplateNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.notify.mail.model.MailTemplateInfo;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static MailTemplateInfo getMailTemplateInfoPK(String mailTemplateId) throws MailTemplateNotFound, InvalidRole {
		return getMailTemplateInfoPK(mailTemplateId, ObjectPrivilegeMode.READ);
	}

	public static MailTemplateInfo getMailTemplateInfoPK(String mailTemplateId, ObjectPrivilegeMode mode) throws MailTemplateNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MailTemplateInfo entity = null;
		try {
			entity = em.find(MailTemplateInfo.class, mailTemplateId, mode);
			if (entity == null) {
				MailTemplateNotFound e = new MailTemplateNotFound("MailTemplateInfoEntity.findByPrimaryKey"
						+ ", mailTemplateId = " + mailTemplateId);
				m_log.info("getMailTemplateInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getMailTemplateInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static MailTemplateInfo getMailTemplateInfoPK_OR(String mailTemplateId, String ownerRoleId) throws MailTemplateNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		MailTemplateInfo entity = null;
		try {
			entity = em.find_OR(MailTemplateInfo.class, mailTemplateId, ownerRoleId);
			if (entity == null) {
				MailTemplateNotFound e = new MailTemplateNotFound("MailTemplateInfoEntity.findByPrimaryKey"
						+ ", mailTemplateId = " + mailTemplateId);
				m_log.info("getMailTemplateInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getMailTemplateInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static List<MailTemplateInfo> getAllMailTemplateInfo() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MailTemplateInfo> list
		= em.createNamedQuery("MailTemplateInfoEntity.findAll", MailTemplateInfo.class)
		.getResultList();
		return list;
	}

	public static List<MailTemplateInfo> getAllMailTemplateInfoOrderByMailTemplateId() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MailTemplateInfo> list
		= em.createNamedQuery("MailTemplateInfoEntity.findAllOrderByMailTemplateId", MailTemplateInfo.class)
		.getResultList();
		return list;
	}

	public static List<MailTemplateInfo> getAllMailTemplateInfoOrderByMailTemplateId_OR(String ownerRoleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MailTemplateInfo> list
		= em.createNamedQuery_OR("MailTemplateInfoEntity.findAllOrderByMailTemplateId", MailTemplateInfo.class, ownerRoleId)
		.getResultList();
		return list;
	}

	public static List<MailTemplateInfo> getMailTemplateInfoFindByOwnerRoleId_NONE(String roleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<MailTemplateInfo> list
		= em.createNamedQuery("MailTemplateInfoEntity.findByOwnerRoleId", MailTemplateInfo.class, ObjectPrivilegeMode.NONE)
		.setParameter("ownerRoleId", roleId)
		.getResultList();
		return list;
	}
}
