/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.util;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.RoleTypeConstant;
import com.clustercontrol.accesscontrol.bean.UserTypeConstant;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfo;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfoPK;
import com.clustercontrol.accesscontrol.model.RoleInfo;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfoPK;
import com.clustercontrol.accesscontrol.model.UserInfo;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.PrivilegeNotFound;
import com.clustercontrol.fault.RoleNotFound;
import com.clustercontrol.fault.UserNotFound;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static UserInfo getUserPK(String userId) throws UserNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			UserInfo info = em.find(UserInfo.class, userId, ObjectPrivilegeMode.READ);
			if (info == null) {
				UserNotFound e = new UserNotFound("UserInfo.findByPrimaryKey, "
						+ "userId = " + userId);
				m_log.info("getUserPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return info;
		}
	}

	public static List<UserInfo> getAllShowUser() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<UserInfo> list
			= em.createNamedQuery("UserInfo.findAllLoginSystemUser", UserInfo.class)
			.setParameter("userType_login", UserTypeConstant.LOGIN_USER)
			.setParameter("userType_system", UserTypeConstant.SYSTEM_USER)
			.getResultList();
			return list;
		}
	}

	/**
	 * オブジェクト権限チェックなし
	 * @return
	 */
	public static List<UserInfo> getAllUser_NONE() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<UserInfo> list
				= em.createNamedQuery("UserInfo.findAllUser", UserInfo.class, ObjectPrivilegeMode.NONE)
					.getResultList();
			return list;
		}
	}

	public static RoleInfo getRolePK(String roleId) throws RoleNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			RoleInfo info = em.find(RoleInfo.class, roleId, ObjectPrivilegeMode.READ);
			if (info == null) {
				RoleNotFound e = new RoleNotFound("RoleInfo.findByPrimaryKey, "
						+ "roleId = " + roleId);
				m_log.info("getRolePK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return info;
		}
	}

	public static SystemPrivilegeInfo getSystemPrivilegePK(SystemPrivilegeInfoPK infoPk) throws RoleNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			SystemPrivilegeInfo info = em.find(SystemPrivilegeInfo.class, infoPk, ObjectPrivilegeMode.READ);
			if (info == null) {
				RoleNotFound e = new RoleNotFound("SystemPrivilegeInfo.findByPrimaryKey, "
						+ infoPk.toString());
				m_log.info("getSystemPrivilegePK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return info;
		}
	}

	public static List<RoleInfo> getAllShowRole() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<RoleInfo> list
			= em.createNamedQuery("RoleInfo.findAllLoginSystemRole", RoleInfo.class)
			.setParameter("roleType_user", RoleTypeConstant.USER_ROLE)
			.setParameter("roleType_system", RoleTypeConstant.SYSTEM_ROLE)
			.getResultList();
			return list;
		}
	}

	/**
	 * オブジェクト権限チェックなし
	 * @return
	 */
	public static List<RoleInfo> getAllRole_NONE() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<RoleInfo> list
				= em.createNamedQuery("RoleInfo.findAllRole", RoleInfo.class, ObjectPrivilegeMode.NONE)
					.getResultList();
			return list;
		}
	}

	public static List<SystemPrivilegeInfo> getAllSystemPrivilege() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<SystemPrivilegeInfo> list
				= em.createNamedQuery("SystemPrivilegeInfo.findAll", SystemPrivilegeInfo.class).getResultList();
			return list;
		}
	}

	public static ArrayList<SystemPrivilegeInfo> getSystemPrivilegeByUserId(String userId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			ArrayList<SystemPrivilegeInfo> rtnList = new ArrayList<SystemPrivilegeInfo>();
			@SuppressWarnings("unchecked")
			List<Object[]> list = em.createNamedQuery("SystemPrivilegeInfo.findByUserId").setParameter("userId", userId).getResultList();
			if (list != null && list.size() > 0) {
				for (Object[] obj : list) {
					SystemPrivilegeInfo info = new SystemPrivilegeInfo();
					info.setSystemFunction((String)obj[0]);
					info.setSystemPrivilege((String)obj[1]);
					rtnList.add(info);
				}
			}
			return rtnList;
		}
	}

	public static List<SystemPrivilegeInfo> getSystemPrivilegeByEditType(String editType) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<SystemPrivilegeInfo> rtnList 
				= em.createNamedQuery("SystemPrivilegeInfo.findByEditType", SystemPrivilegeInfo.class)
				.setParameter("editType", editType).getResultList();
			return rtnList;
		}
	}

	public static ObjectPrivilegeInfo getObjectPrivilegePK(String objectType, String objectId, String roleId, String objectPrivilege)
			throws PrivilegeNotFound {
		return getObjectPrivilegePK(new ObjectPrivilegeInfoPK(objectType, objectId, roleId, objectPrivilege));
	}

	public static ObjectPrivilegeInfo getObjectPrivilegePK(ObjectPrivilegeInfoPK pk)
			throws PrivilegeNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			ObjectPrivilegeInfo info = em.find(ObjectPrivilegeInfo.class, pk, ObjectPrivilegeMode.READ);
			if (info == null) {
				PrivilegeNotFound e = new PrivilegeNotFound("ObjectPrivilegeInfo.findByPrimaryKey, "
						+ pk.toString());
				m_log.info("getObjectPrivilegePK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return info;
		}
	}

	public static List<ObjectPrivilegeInfo> getAllObjectPrivilege() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<ObjectPrivilegeInfo> list
			= em.createNamedQuery("ObjectPrivilegeInfo.findAll", ObjectPrivilegeInfo.class).getResultList();
			return list;
		}
	}

	public static List<ObjectPrivilegeInfo> getAllObjectPrivilegeByFilter(
			String objectType,
			String objectId,
			String roleId,
			String objectPrivilege) {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			StringBuffer sbJpql = new StringBuffer();
			sbJpql.append("SELECT a FROM ObjectPrivilegeInfo a WHERE true = true");
			if(objectType != null && !"".equals(objectType)) {
				sbJpql.append(" AND a.id.objectType = :objectType");
			}
			if(objectId != null && !"".equals(objectId)) {
				sbJpql.append(" AND a.id.objectId = :objectId");
			}
			if(roleId != null && !"".equals(roleId)) {
				sbJpql.append(" AND a.id.roleId = :roleId");
			}
			if(objectPrivilege != null && !"".equals(objectPrivilege)) {
				sbJpql.append(" AND a.id.objectPrivilege = :objectPrivilege");
			}
			TypedQuery<ObjectPrivilegeInfo> typedQuery = em.createQuery(sbJpql.toString(), ObjectPrivilegeInfo.class);
			if(objectType != null && !"".equals(objectType)) {
				typedQuery = typedQuery.setParameter("objectType", objectType);
			}
			if(objectId != null && !"".equals(objectId)) {
				typedQuery = typedQuery.setParameter("objectId", objectId);
			}
			if(roleId != null && !"".equals(roleId)) {
				typedQuery = typedQuery.setParameter("roleId", roleId);
			}
			if(objectPrivilege != null && !"".equals(objectPrivilege)) {
				typedQuery = typedQuery.setParameter("objectPrivilege", objectPrivilege);
			}
			return typedQuery.getResultList();
		}
	}

}
