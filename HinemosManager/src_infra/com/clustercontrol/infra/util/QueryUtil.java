/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.util;

import java.util.List;

import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InfraManagementNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.infra.model.FileTransferModuleInfo;
import com.clustercontrol.infra.model.InfraCheckResult;
import com.clustercontrol.infra.model.InfraFileInfo;
import com.clustercontrol.infra.model.InfraManagementInfo;
import com.clustercontrol.infra.model.InfraManagementParamInfo;
import com.clustercontrol.infra.model.InfraManagementParamInfoPK;
import com.clustercontrol.infra.model.InfraModuleInfo;
import com.clustercontrol.infra.model.InfraModuleInfoPK;
import com.clustercontrol.infra.model.ReferManagementModuleInfo;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static InfraManagementInfo getInfraManagementInfoPK(String managementId) throws InfraManagementNotFound, InvalidRole {
		return getInfraManagementInfoPK(managementId, ObjectPrivilegeMode.READ);
	}

	public static InfraManagementInfo getInfraManagementInfoPK(String managementId, ObjectPrivilegeMode mode) throws InfraManagementNotFound, InvalidRole {
		InfraManagementInfo entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find(InfraManagementInfo.class, managementId, mode);
			if (entity == null) {
				InfraManagementNotFound e = new InfraManagementNotFound(managementId, "InfraManagementInfoEntity.findByPrimaryKey"
						+ ", managementId = " + managementId);
				m_log.info("getInfraManagementInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getInfraManagementInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static List<InfraManagementInfo> getAllInfraManagementInfo() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<InfraManagementInfo> list
			= em.createNamedQuery("InfraManagementInfoEntity.findAll", InfraManagementInfo.class)
			.getResultList();
			return list;
		}
	}

	public static List<InfraManagementInfo> getAllInfraManagementInfoOrderByInfraManagementId_OR(String ownerRoleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<InfraManagementInfo> list
			= em.createNamedQuery_OR("InfraManagementInfoEntity.findAll", InfraManagementInfo.class, ownerRoleId)
			.getResultList();
			return list;
		}
	}

	public static List<InfraManagementInfo> getInfraManagementInfoFindByOwnerRoleId_NONE(String roleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<InfraManagementInfo> list
			= em.createNamedQuery("InfraManagementInfoEntity.findByOwnerRoleId", InfraManagementInfo.class, ObjectPrivilegeMode.NONE)
			.setParameter("ownerRoleId", roleId)
			.getResultList();
			return list;
		}
	}

	public static List<InfraFileInfo> getAllInfraFile() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<InfraFileInfo> list
				= em.createNamedQuery("InfraFileEntity.findAll", InfraFileInfo.class).getResultList();
			return list;
		}
	}

	public static List<InfraFileInfo> getAllInfraFile_OR(String ownerRoleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<InfraFileInfo> list
				= em.createNamedQuery_OR("InfraFileEntity.findAll", InfraFileInfo.class, ownerRoleId).getResultList();
			return list;
		}
	}

	public static List<InfraManagementInfo> getInfraManagementInfoFindByFacilityId_NONE (String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<InfraManagementInfo> list = null;
	
			// ファシリティIDが使用されている設定を取得する。
			list = em.createNamedQuery("InfraManagementInfoEntity.findByFacilityId", InfraManagementInfo.class, ObjectPrivilegeMode.NONE)
				.setParameter("facilityId", facilityId)
				.getResultList();
			
			return list;
		}
	}
	
	public static List<InfraCheckResult> getInfraCheckResultFindByManagementId(String managementId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			TypedQuery<InfraCheckResult> query = em.createNamedQuery("InfraCheckResultEntity.findByManagementId", InfraCheckResult.class);
			query.setParameter("managementId", managementId);
			List<InfraCheckResult> list = query.getResultList();
			return list;
		}
	}
	
	public static List<InfraCheckResult> getInfraCheckResultFindByModuleId(String managementId, String moduleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			TypedQuery<InfraCheckResult> query = em.createNamedQuery("InfraCheckResultEntity.findByModuleId", InfraCheckResult.class);
			query.setParameter("managementId", managementId);
			query.setParameter("moduleId", moduleId);
			List<InfraCheckResult> list = query.getResultList();
			return list;
		}
	}

	public static boolean isInfraFileReferredByFileTransferModuleInfoEntity(String fileId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			TypedQuery<FileTransferModuleInfo> query = em.createNamedQuery("FileTransferModuleInfoEntity.findByFileId", FileTransferModuleInfo.class);
			query.setParameter("fileId", fileId);
			query.setMaxResults(1);
			return !query.getResultList().isEmpty();
		}
	}
	
	public static InfraManagementInfo getInfraManagementInfoPK_OR(String managementId, String ownerRoleId) throws InfraManagementNotFound, InvalidRole {
		return getInfraManagementInfoPK_OR(managementId, ownerRoleId, ObjectPrivilegeMode.READ);
	}
	
	public static InfraManagementInfo getInfraManagementInfoPK_OR(String managementId, String ownerRoleId, ObjectPrivilegeMode mode) throws InfraManagementNotFound, InvalidRole {
		InfraManagementInfo info = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			info = em.find_OR(InfraManagementInfo.class, managementId, mode, ownerRoleId);
			if (info == null) {
				InfraManagementNotFound e = new InfraManagementNotFound(managementId);
				m_log.info("getInfraManagementInfoPK_OR() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getInfraManagementInfoPK_OR() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return info;
	}

	public static InfraModuleInfo<?> getInfraModuleInfoPK(InfraModuleInfoPK pk)
			throws InfraManagementNotFound, InvalidRole {
		InfraModuleInfo<?> entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find(InfraModuleInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				InfraManagementNotFound e = new InfraManagementNotFound("InfraModuleInfoEntity.findByPrimaryKey"
						+ ", pk = " + pk);
				m_log.info("getInfraModuleInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getInfraModuleInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static InfraFileInfo getInfraFileInfoPK(String fileId, ObjectPrivilegeMode mode)
			throws InfraManagementNotFound, InvalidRole {
		InfraFileInfo entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find(InfraFileInfo.class, fileId, mode);
			if (entity == null) {
				InfraManagementNotFound e = new InfraManagementNotFound("InfraFileInfoEntity.findByPrimaryKey"
						+ ", fileId = " + fileId);
				m_log.info("getInfraFileInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getInfraFileInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static InfraFileInfo getInfraFileInfoPK_OR(String fileId, ObjectPrivilegeMode mode, String ownerRoleId)
			throws InfraManagementNotFound, InvalidRole {
		InfraFileInfo entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find_OR(InfraFileInfo.class, fileId, mode, ownerRoleId);
			if (entity == null) {
				InfraManagementNotFound e = new InfraManagementNotFound("InfraFileInfoEntity.findByPrimaryKey"
						+ ", fileId = " + fileId
						+ ", ownerRoleId = " + ownerRoleId);
				m_log.info("getInfraFileInfoPK_OR() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getInfraFileInfoPK_OR() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static InfraManagementParamInfo getInfraManagementParamInfoPK(InfraManagementParamInfoPK pk)
			throws InfraManagementNotFound, InvalidRole {
		InfraManagementParamInfo entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find(InfraManagementParamInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				InfraManagementNotFound e = new InfraManagementNotFound("InfraManagementParamInfo.findByPrimaryKey"
						+ pk.toString());
				m_log.info("getInfraManagementParamInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getInfraFileInfoPK_OR() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static List<InfraManagementParamInfo> getInfraManagementParamListFindByManagementId(String managementId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<InfraManagementParamInfo> list
				= em.createNamedQuery("InfraManagementParamInfo.findByManagementId", InfraManagementParamInfo.class)
				.setParameter("managementId", managementId)
				.getResultList();
			return list;
		}
	}
}
