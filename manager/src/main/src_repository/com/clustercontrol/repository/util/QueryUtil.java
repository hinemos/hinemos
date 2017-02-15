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

package com.clustercontrol.repository.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.Ipv6Util;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.model.CollectorPlatformMstEntity;
import com.clustercontrol.repository.model.CollectorSubPlatformMstEntity;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.FacilityRelationEntity;
import com.clustercontrol.repository.model.FacilityRelationEntityPK;
import com.clustercontrol.repository.model.NodeGeneralDeviceInfo;
import com.clustercontrol.repository.model.NodeCpuInfo;
import com.clustercontrol.repository.model.NodeDeviceInfoPK;
import com.clustercontrol.repository.model.NodeDiskInfo;
import com.clustercontrol.repository.model.NodeFilesystemInfo;
import com.clustercontrol.repository.model.NodeHostnameInfo;
import com.clustercontrol.repository.model.NodeHostnameInfoPK;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.NodeMemoryInfo;
import com.clustercontrol.repository.model.NodeNetworkInterfaceInfo;
import com.clustercontrol.repository.model.NodeNoteInfo;
import com.clustercontrol.repository.model.NodeNoteInfoPK;
import com.clustercontrol.repository.model.NodeVariableInfo;
import com.clustercontrol.repository.model.NodeVariableInfoPK;
import com.clustercontrol.repository.model.ScopeInfo;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static NodeInfo getNodePK(String facilityId) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		NodeInfo entity = em.find(NodeInfo.class, facilityId, ObjectPrivilegeMode.READ);
		if (entity == null) {
			FacilityNotFound e = new FacilityNotFound("NodeEntity.findByPrimaryKey"
					+ ", facilityId = " + facilityId);
			m_log.info("getNodePK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}
	
	public static NodeInfo getNodePK(String facilityId, ObjectPrivilegeMode mode) throws FacilityNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		NodeInfo entity = null;
		try {
			entity = em.find(NodeInfo.class, facilityId, mode);
			if (entity == null) {
				FacilityNotFound e = new FacilityNotFound("FacilityEntity.findByPrimaryKey"
						+ ", facilityId = " + facilityId);
				m_log.info("getFacilityPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getFacilityPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static ScopeInfo getScopePK(String facilityId) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		ScopeInfo entity = em.find(ScopeInfo.class, facilityId, ObjectPrivilegeMode.READ);
		if (entity == null) {
			FacilityNotFound e = new FacilityNotFound("ScopeEntity.findByPrimaryKey"
					+ ", facilityId = " + facilityId);
			m_log.info("getScopePK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static FacilityInfo getFacilityPK(String facilityId) throws FacilityNotFound, InvalidRole {
		return getFacilityPK(facilityId, ObjectPrivilegeMode.READ);
	}

	public static FacilityInfo getFacilityPK(String facilityId, ObjectPrivilegeMode mode) throws FacilityNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		FacilityInfo entity = null;
		try {
			entity = em.find(FacilityInfo.class, facilityId, mode);
			if (entity == null) {
				FacilityNotFound e = new FacilityNotFound("FacilityEntity.findByPrimaryKey"
						+ ", facilityId = " + facilityId);
				m_log.info("getFacilityPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getFacilityPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static FacilityInfo getFacilityPK_OR(String facilityId, String ownerRoleId) throws FacilityNotFound, InvalidRole {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		FacilityInfo entity = null;
		try {
			entity = em.find_OR(FacilityInfo.class, facilityId, ownerRoleId);
			if (entity == null) {
				FacilityNotFound e = new FacilityNotFound("FacilityEntity.findByPrimaryKey"
						+ ", facilityId = " + facilityId);
				m_log.info("getFacilityPK_OR() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getFacilityPK_OR() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static FacilityInfo getFacilityPK_NONE(String facilityId) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		FacilityInfo entity = null;
		try {
			entity = em.find(FacilityInfo.class, facilityId, ObjectPrivilegeMode.NONE);
			if (entity == null) {
				FacilityNotFound e = new FacilityNotFound("FacilityEntity.findByPrimaryKey"
						+ ", facilityId = " + facilityId);
				m_log.info("getFacilityPK_NONE() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			// NONE（オブジェクト権限チェックなし）のため、ここは通らない。
		}

		return entity;
	}

	@Deprecated
	public static FacilityInfo getFacilityPK_WRITE(String facilityId) throws FacilityNotFound, InvalidRole {
		return getFacilityPK(facilityId, ObjectPrivilegeMode.MODIFY);
	}

	public static List<FacilityInfo> getFacilityByOwnerRoleId_NONE(String roleId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<FacilityInfo> list
		= em.createNamedQuery("FacilityEntity.findByOwnerRoleId", FacilityInfo.class, ObjectPrivilegeMode.NONE)
		.setParameter("ownerRoleId", roleId)
		.getResultList();
		return list;
	}

	public static List<FacilityInfo> getAllFacility() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<FacilityInfo> list
		= em.createNamedQuery("FacilityEntity.findAll", FacilityInfo.class)
		.getResultList();
		return list;
	}

	public static List<FacilityInfo> getAllFacility_NONE() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<FacilityInfo> list
		= em.createNamedQuery("FacilityEntity.findAll", FacilityInfo.class, ObjectPrivilegeMode.NONE)
		.getResultList();
		return list;
	}

	public static List<ScopeInfo> getRootScopeFacility_NONE() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<ScopeInfo> list
		= em.createNamedQuery("FacilityEntity.findRootByFacilityType", ScopeInfo.class, ObjectPrivilegeMode.NONE)
		.setParameter("facilityType", FacilityConstant.TYPE_SCOPE)
		.getResultList();
		return list;
	}

	public static FacilityRelationEntity getFacilityRelationPk(String parentFacilityId, String childFacilityId) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		FacilityRelationEntityPK pk = new FacilityRelationEntityPK(parentFacilityId, childFacilityId);
		FacilityRelationEntity entity = em.find(FacilityRelationEntity.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			FacilityNotFound e = new FacilityNotFound("FacilityRelationEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getFacilityRelationPk() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static List<FacilityInfo> getParentFacilityEntity(String childFacilityId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<FacilityInfo> list = new ArrayList<FacilityInfo>();
		List<FacilityRelationEntity> FacilityRelationEntities
		= em.createNamedQuery("FacilityRelationEntity.findParent", FacilityRelationEntity.class)
		.setParameter("childFacilityId", childFacilityId)
		.getResultList();
		for (FacilityRelationEntity facilityRelationEntity : FacilityRelationEntities) {
			try {
				FacilityInfo parentFacilityEntity
					= QueryUtil.getFacilityPK_NONE(facilityRelationEntity.getParentFacilityId());
				list.add(parentFacilityEntity);
			} catch (FacilityNotFound e) {
				// 通らない。
			}
		}
		return list;
	}

	public static List<FacilityInfo> getChildFacilityEntity(String parentFacilityId) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<FacilityInfo> list = new ArrayList<FacilityInfo>();
		List<FacilityRelationEntity> FacilityRelationEntities
		= em.createNamedQuery("FacilityRelationEntity.findChild", FacilityRelationEntity.class)
		.setParameter("parentFacilityId", parentFacilityId)
		.getResultList();
		for (FacilityRelationEntity facilityRelationEntity : FacilityRelationEntities) {
			try {
				FacilityInfo childFacilityEntity
					= QueryUtil.getFacilityPK_NONE(facilityRelationEntity.getChildFacilityId());
				list.add(childFacilityEntity);
			} catch (FacilityNotFound e) {
				// 通らない。
			}
		}
		return list;
	}

	public static List<FacilityRelationEntity> getAllFacilityRelations_NONE() {
		HinemosEntityManager em = new JpaTransactionManager()
				.getEntityManager();
		List<FacilityRelationEntity> list = em.createNamedQuery(
				"FacilityRelationEntity.findAll", FacilityRelationEntity.class,
				ObjectPrivilegeMode.NONE).getResultList();
		return list;
	}

	public static List<NodeInfo> getAllNode() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NodeInfo> list
		= em.createNamedQuery("NodeEntity.findAll", NodeInfo.class)
		.getResultList();
		return list;
	}

	public static List<NodeInfo> getAllNode_NONE() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NodeInfo> list
		= em.createNamedQuery("FacilityEntity.findByFacilityType", NodeInfo.class, ObjectPrivilegeMode.NONE)
		.setParameter("facilityType", FacilityConstant.TYPE_NODE)
		.getResultList();
		return list;
	}

	public static List<NodeHostnameInfo> getAllNodeHostname() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NodeHostnameInfo> list
		= em.createNamedQuery("NodeHostnameEntity.findAll", NodeHostnameInfo.class)
		.getResultList();
		return list;
	}

	public static List<NodeInfo> getNodeByIpv4(String ipAddressV4) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NodeInfo> list
		= em.createNamedQuery("FacilityEntity.findByIpAddressV4", NodeInfo.class)
		.setParameter("ipAddressV4", ipAddressV4)
		.getResultList();
		return list;
	}

	public static List<NodeInfo> getNodeByIpv6(String ipAddressV6) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NodeInfo> list
		= em.createNamedQuery("FacilityEntity.findByIpAddressV6", NodeInfo.class)
		.setParameter("ipAddressV6", Ipv6Util.expand(ipAddressV6))
		.getResultList();
		return list;
	}

	public static List<NodeInfo> getNodeByNodename(String nodeName) {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<NodeInfo> list
		= em.createNamedQuery("FacilityEntity.findByNodename", NodeInfo.class)
		.setParameter("nodeName", nodeName.toLowerCase())
		.getResultList();
		return list;
	}

	public static NodeHostnameInfo getNodeHostnamePK(NodeHostnameInfoPK pk) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		NodeHostnameInfo entity = em.find(NodeHostnameInfo.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			FacilityNotFound e = new FacilityNotFound("NodeHostnameEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getNodeHostnamePK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static NodeGeneralDeviceInfo getNodeDeviceEntityPK(NodeDeviceInfoPK pk) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		NodeGeneralDeviceInfo entity = em.find(NodeGeneralDeviceInfo.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			FacilityNotFound e = new FacilityNotFound("NodeDeviceEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getNodeDeviceEntityPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static NodeCpuInfo getNodeCpuEntityPK(NodeDeviceInfoPK pk) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		NodeCpuInfo entity = em.find(NodeCpuInfo.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			FacilityNotFound e = new FacilityNotFound("NodeCpuEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getNodeCpuEntityPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static NodeMemoryInfo getNodeMemoryEntityPK(NodeDeviceInfoPK pk) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		NodeMemoryInfo entity = em.find(NodeMemoryInfo.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			FacilityNotFound e = new FacilityNotFound("NodeMemoryEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getNodeMemoryEntityPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static NodeNetworkInterfaceInfo getNodeNetworkInterfaceEntityPK(NodeDeviceInfoPK pk) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		NodeNetworkInterfaceInfo entity = em.find(NodeNetworkInterfaceInfo.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			FacilityNotFound e = new FacilityNotFound("NodeNetworkInterfaceEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getNodeNetworkInterfaceEntityPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static NodeDiskInfo getNodeDiskEntityPK(NodeDeviceInfoPK pk) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		NodeDiskInfo entity = em.find(NodeDiskInfo.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			FacilityNotFound e = new FacilityNotFound("NodeDiskEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getNodeDiskEntityPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static NodeFilesystemInfo getFilesystemDiskEntityPK(NodeDeviceInfoPK pk) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		NodeFilesystemInfo entity = em.find(NodeFilesystemInfo.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			FacilityNotFound e = new FacilityNotFound("NodeFilesystemEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getNodeFilesystemEntityPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static NodeVariableInfo getNodeVariableEntityPK(NodeVariableInfoPK pk) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		NodeVariableInfo entity = em.find(NodeVariableInfo.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			FacilityNotFound e = new FacilityNotFound("NodeVariableEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getNodeVariableEntityPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static NodeNoteInfo getNodeNoteEntityPK(NodeNoteInfoPK pk) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		NodeNoteInfo entity = em.find(NodeNoteInfo.class, pk, ObjectPrivilegeMode.READ);
		if (entity == null) {
			FacilityNotFound e = new FacilityNotFound("NodeNoteEntity.findByPrimaryKey"
					+ pk.toString());
			m_log.info("getNodeNoteEntityPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static CollectorPlatformMstEntity getCollectorPlatformMstPK(String platformId) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		CollectorPlatformMstEntity entity = em.find(CollectorPlatformMstEntity.class, platformId, ObjectPrivilegeMode.READ);
		if (entity == null) {
			FacilityNotFound e = new FacilityNotFound("CollectorPlatformMstEntity.findByPrimaryKey"
					+ ", platformId = " + platformId);
			m_log.info("getCollectorPlatformMstPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static CollectorSubPlatformMstEntity getCollectorSubPlatformMstPK(String subPlatformId) throws FacilityNotFound {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		CollectorSubPlatformMstEntity entity = em.find(CollectorSubPlatformMstEntity.class, subPlatformId, ObjectPrivilegeMode.READ);
		if (entity == null) {
			FacilityNotFound e = new FacilityNotFound("CollectorSubPlatformMstEntity.findByPrimaryKey"
					+ ", subPlatformId = " + subPlatformId);
			m_log.info("getCollectorSubPlatformMstPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		return entity;
	}

	public static List<CollectorPlatformMstEntity> getAllCollectorPlatformMst() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CollectorPlatformMstEntity> list
		= em.createNamedQuery("CollectorPlatformMstEntity.findAll", CollectorPlatformMstEntity.class)
		.getResultList();
		return list;
	}

	public static List<CollectorSubPlatformMstEntity> getAllCollectorSubPlatformMstEntity() {
		HinemosEntityManager em = new JpaTransactionManager().getEntityManager();
		List<CollectorSubPlatformMstEntity> list
		= em.createNamedQuery("CollectorSubPlatformMstEntity.findAll", CollectorSubPlatformMstEntity.class)
		.getResultList();
		return list;
	}
}
