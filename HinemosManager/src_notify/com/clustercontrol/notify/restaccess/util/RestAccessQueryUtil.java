/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.restaccess.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.fault.RestAccessNotFound;
import com.clustercontrol.notify.restaccess.model.RestAccessInfo;

public class RestAccessQueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( RestAccessQueryUtil.class );

	public static RestAccessInfo getRestAccessInfoPK(String RestAccessId) throws RestAccessNotFound, InvalidRole {
		return getRestAccessInfoPK(RestAccessId, ObjectPrivilegeMode.READ);
	}

	public static RestAccessInfo getRestAccessInfoPK(String RestAccessId, ObjectPrivilegeMode mode) throws RestAccessNotFound, InvalidRole {
		RestAccessInfo entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find(RestAccessInfo.class, RestAccessId, mode);
			if (entity == null) {
				RestAccessNotFound e = new RestAccessNotFound("RestAccessInfoEntity.findByPrimaryKey"
						+ ", RestAccessId = " + RestAccessId);
				m_log.info("getRestAccessInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getRestAccessInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static RestAccessInfo getRestAccessInfoPK_OR(String RestAccessId, String ownerRoleId) throws RestAccessNotFound, InvalidRole {
		RestAccessInfo entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find_OR(RestAccessInfo.class, RestAccessId, ownerRoleId);
			if (entity == null) {
				RestAccessNotFound e = new RestAccessNotFound("RestAccessInfoEntity.findByPrimaryKey"
						+ ", RestAccessId = " + RestAccessId);
				m_log.info("getRestAccessInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getRestAccessInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static List<RestAccessInfo> getAllRestAccessInfoOrderByRestAccessId() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<RestAccessInfo> list
			= em.createNamedQuery("RestAccessInfoEntity.findAllOrderByRestAccessId", RestAccessInfo.class)
			.getResultList();
			return list;
		}
	}

	public static List<RestAccessInfo> getAllRestAccessInfoOrderByRestAccessId_OR(String ownerRoleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<RestAccessInfo> list
			= em.createNamedQuery_OR("RestAccessInfoEntity.findAllOrderByRestAccessId", RestAccessInfo.class, ownerRoleId)
			.getResultList();
			return list;
		}
	}

	public static List<RestAccessInfo> getRestAccessInfoFindByOwnerRoleId_NONE(String roleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<RestAccessInfo> list
			= em.createNamedQuery("RestAccessInfoEntity.findByOwnerRoleId", RestAccessInfo.class, ObjectPrivilegeMode.NONE)
			.setParameter("ownerRoleId", roleId)
			.getResultList();
			return list;
		}
	}

}
