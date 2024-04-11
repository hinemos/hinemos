/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.util;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.Ipv6Util;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.QueryDivergence;
import com.clustercontrol.commons.util.QueryExecutor;
import com.clustercontrol.fault.AutoRegisterNodeSettingNotFound;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.NodeConfigSettingNotFound;
import com.clustercontrol.fault.NodeHistoryNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.bean.NodeConfigFilterComparisonMethod;
import com.clustercontrol.repository.bean.NodeConfigFilterDataType;
import com.clustercontrol.repository.bean.NodeConfigFilterInfo;
import com.clustercontrol.repository.bean.NodeConfigFilterItem;
import com.clustercontrol.repository.bean.NodeConfigFilterItemInfo;
import com.clustercontrol.repository.bean.NodeConfigSettingConstant;
import com.clustercontrol.repository.bean.NodeConfigSettingItem;
import com.clustercontrol.repository.model.AutoRegisterNodeInfo;
import com.clustercontrol.repository.model.CollectorPlatformMstEntity;
import com.clustercontrol.repository.model.CollectorSubPlatformMstEntity;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.FacilityRelationEntity;
import com.clustercontrol.repository.model.FacilityRelationEntityPK;
import com.clustercontrol.repository.model.NodeConfigCustomInfo;
import com.clustercontrol.repository.model.NodeConfigCustomInfoPK;
import com.clustercontrol.repository.model.NodeConfigSettingInfo;
import com.clustercontrol.repository.model.NodeConfigSettingItemInfo;
import com.clustercontrol.repository.model.NodeConfigSettingItemInfoPK;
import com.clustercontrol.repository.model.NodeCpuHistoryDetail;
import com.clustercontrol.repository.model.NodeCpuInfo;
import com.clustercontrol.repository.model.NodeCustomHistoryDetail;
import com.clustercontrol.repository.model.NodeCustomInfo;
import com.clustercontrol.repository.model.NodeCustomInfoPK;
import com.clustercontrol.repository.model.NodeDeviceHistoryDetail;
import com.clustercontrol.repository.model.NodeDeviceInfoPK;
import com.clustercontrol.repository.model.NodeDiskHistoryDetail;
import com.clustercontrol.repository.model.NodeDiskInfo;
import com.clustercontrol.repository.model.NodeFilesystemHistoryDetail;
import com.clustercontrol.repository.model.NodeFilesystemInfo;
import com.clustercontrol.repository.model.NodeGeneralDeviceInfo;
import com.clustercontrol.repository.model.NodeHistory;
import com.clustercontrol.repository.model.NodeHistoryDetail;
import com.clustercontrol.repository.model.NodeHistoryPK;
import com.clustercontrol.repository.model.NodeHostnameHistoryDetail;
import com.clustercontrol.repository.model.NodeHostnameInfo;
import com.clustercontrol.repository.model.NodeHostnameInfoPK;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.NodeLicenseHistoryDetail;
import com.clustercontrol.repository.model.NodeLicenseInfo;
import com.clustercontrol.repository.model.NodeLicenseInfoPK;
import com.clustercontrol.repository.model.NodeMemoryHistoryDetail;
import com.clustercontrol.repository.model.NodeMemoryInfo;
import com.clustercontrol.repository.model.NodeNetstatHistoryDetail;
import com.clustercontrol.repository.model.NodeNetstatInfo;
import com.clustercontrol.repository.model.NodeNetstatInfoPK;
import com.clustercontrol.repository.model.NodeNetworkInterfaceHistoryDetail;
import com.clustercontrol.repository.model.NodeNetworkInterfaceInfo;
import com.clustercontrol.repository.model.NodeNoteInfo;
import com.clustercontrol.repository.model.NodeOsHistoryDetail;
import com.clustercontrol.repository.model.NodeOsInfo;
import com.clustercontrol.repository.model.NodePackageHistoryDetail;
import com.clustercontrol.repository.model.NodePackageInfo;
import com.clustercontrol.repository.model.NodePackageInfoPK;
import com.clustercontrol.repository.model.NodeProcessInfo;
import com.clustercontrol.repository.model.NodeProductHistoryDetail;
import com.clustercontrol.repository.model.NodeProductInfo;
import com.clustercontrol.repository.model.NodeProductInfoPK;
import com.clustercontrol.repository.model.NodeVariableHistoryDetail;
import com.clustercontrol.repository.model.NodeVariableInfo;
import com.clustercontrol.repository.model.NodeVariableInfoPK;
import com.clustercontrol.repository.model.ScopeInfo;

import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static NodeInfo getNodePK(String facilityId) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NodeInfo entity = em.find(NodeInfo.class, facilityId, ObjectPrivilegeMode.READ);
			if (entity == null) {
				FacilityNotFound e = new FacilityNotFound("NodeEntity.findByPrimaryKey"
						+ ", facilityId = " + facilityId);
				m_log.info("getNodePK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			// OS情報付与
			try {
				NodeOsInfo nodeOsInfo = QueryUtil.getNodeOsEntityPK(facilityId);
				entity.setNodeOsInfo(nodeOsInfo);
			} catch (FacilityNotFound e) {
				// 何もしない
			}
			return entity;
		}
	}

	public static NodeInfo getNodePK(String facilityId, ObjectPrivilegeMode mode) throws FacilityNotFound, InvalidRole {
		NodeInfo entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
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
		// OS情報付与
		try {
			NodeOsInfo nodeOsInfo = QueryUtil.getNodeOsEntityPK(facilityId);
			entity.setNodeOsInfo(nodeOsInfo);
		} catch (FacilityNotFound e) {
			// 何もしない
		}
		return entity;
	}

	public static ScopeInfo getScopePK(String facilityId) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
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
	}

	public static FacilityInfo getFacilityPK(String facilityId) throws FacilityNotFound, InvalidRole {
		return getFacilityPK(facilityId, ObjectPrivilegeMode.READ);
	}

	public static FacilityInfo getFacilityPK(String facilityId, ObjectPrivilegeMode mode) throws FacilityNotFound, InvalidRole {
		FacilityInfo entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
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
		return getFacilityPK_OR(facilityId, ownerRoleId, ObjectPrivilegeMode.READ);
	}
	
	public static FacilityInfo getFacilityPK_OR(String facilityId, String ownerRoleId, ObjectPrivilegeMode mode) throws FacilityNotFound, InvalidRole {
		FacilityInfo entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find_OR(FacilityInfo.class, facilityId, mode, ownerRoleId);
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
		FacilityInfo entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
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
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<FacilityInfo> list
			= em.createNamedQuery("FacilityEntity.findByOwnerRoleId", FacilityInfo.class, ObjectPrivilegeMode.NONE)
			.setParameter("ownerRoleId", roleId)
			.getResultList();
			return list;
		}
	}

	public static List<NodeCpuInfo> getNodeCpuInfoByFacilityId(String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeCpuInfo> list = em.createNamedQuery("NodeCpuInfo.findByfacilityId", NodeCpuInfo.class, ObjectPrivilegeMode.NONE)
					.setParameter("facilityId", facilityId)
					.getResultList();
			return list;
		}
	}

	public static List<NodeGeneralDeviceInfo> getNodeGeneralDeviceInfoByFacilityId(String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeGeneralDeviceInfo> list = em.createNamedQuery("NodeGeneralDeviceInfo.findByfacilityId", NodeGeneralDeviceInfo.class, ObjectPrivilegeMode.NONE)
					.setParameter("facilityId", facilityId)
					.getResultList();
			return list;
		}
	}

	public static List<NodeDiskInfo> getNodeDiskInfoByFacilityId(String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeDiskInfo> list = em.createNamedQuery("NodeDiskInfo.findByfacilityId", NodeDiskInfo.class, ObjectPrivilegeMode.NONE)
					.setParameter("facilityId", facilityId)
					.getResultList();
			return list;
		}
	}

	public static List<NodeFilesystemInfo> getNodeFilesystemInfoByFacilityId(String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeFilesystemInfo> list = em.createNamedQuery("NodeFilesystemInfo.findByfacilityId", NodeFilesystemInfo.class, ObjectPrivilegeMode.NONE)
					.setParameter("facilityId", facilityId)
					.getResultList();
			return list;
		}
	}

	public static List<NodeHostnameInfo> getNodeHostnameInfoByFacilityId(String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeHostnameInfo> list = em.createNamedQuery("NodeHostnameInfo.findByfacilityId", NodeHostnameInfo.class, ObjectPrivilegeMode.NONE)
					.setParameter("facilityId", facilityId)
					.getResultList();
			return list;
		}
	}

	public static List<NodeMemoryInfo> getNodeMemoryInfoByFacilityId(String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeMemoryInfo> list = em.createNamedQuery("NodeMemoryInfo.findByfacilityId", NodeMemoryInfo.class, ObjectPrivilegeMode.NONE)
					.setParameter("facilityId", facilityId)
					.getResultList();
			return list;
		}
	}

	public static List<NodeNetworkInterfaceInfo> getNodeNetworkInterfaceInfoByFacilityId(String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeNetworkInterfaceInfo> list = em.createNamedQuery("NodeNetworkInterfaceInfo.findByfacilityId", NodeNetworkInterfaceInfo.class, ObjectPrivilegeMode.NONE)
					.setParameter("facilityId", facilityId)
					.getResultList();
			return list;
		}
	}

	public static List<NodeNetworkInterfaceInfo> getNodeNetworkInterfaceInfoByMacAddress(String macAddress) {
		String macAddress1="";
		String macAddress2="";
		String macAddress3="";
		String macAddress4="";
		String macAddress5="";
		String macAddress6="";
		if(macAddress != null){
			try{
				String lowerMacAddress = macAddress.toLowerCase();
				macAddress1 = lowerMacAddress.substring(0, 2);
				macAddress2 = lowerMacAddress.substring(3, 5);
				macAddress3 = lowerMacAddress.substring(6, 8);
				macAddress4 = lowerMacAddress.substring(9, 11);
				macAddress5 = lowerMacAddress.substring(12, 14);
				macAddress6 = lowerMacAddress.substring(15, 17);
				m_log.trace(String.format("getNodeNetworkInterfaceInfoByMacAddress() : "
						+ "macAddress1=[%s], macAddress2=[%s], macAddress3=[%s], macAddress4=[%s], macAddress5=[%s], macAddress6=[%s]",
						macAddress1, macAddress2, macAddress3, macAddress4, macAddress5, macAddress6));
			} catch(StringIndexOutOfBoundsException e){
				m_log.info("getNodeNetworkInterfaceInfoByMacAddress() : macAddress=["
						+ macAddress + "], " + e.getClass().getSimpleName() + ", " + e.getMessage());
			}
		}
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeNetworkInterfaceInfo> list = em.createNamedQuery("NodeNetworkInterfaceInfo.findByMacAddress", NodeNetworkInterfaceInfo.class, ObjectPrivilegeMode.NONE)
					.setParameter(1, macAddress1)
					.setParameter(2, macAddress2)
					.setParameter(3, macAddress3)
					.setParameter(4, macAddress4)
					.setParameter(5, macAddress5)
					.setParameter(6, macAddress6)
					.getResultList();
			return list;
		}
	}

	public static List<NodeNoteInfo> getNodeNoteInfoByFacilityId(String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeNoteInfo> list = em.createNamedQuery("NodeNoteInfo.findByfacilityId", NodeNoteInfo.class, ObjectPrivilegeMode.NONE)
					.setParameter("facilityId", facilityId)
					.getResultList();
			return list;
		}
	}

	public static List<NodeVariableInfo> getNodeVariableInfoByFacilityId(String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeVariableInfo> list = em.createNamedQuery("NodeVariableInfo.findByfacilityId", NodeVariableInfo.class, ObjectPrivilegeMode.NONE)
					.setParameter("facilityId", facilityId)
					.getResultList();
			return list;
		}
	}

	public static List<NodeNetstatInfo> getNodeNetstatInfoByFacilityId(String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeNetstatInfo> list = em.createNamedQuery("NodeNetstatInfo.findByfacilityId", NodeNetstatInfo.class, ObjectPrivilegeMode.NONE)
					.setParameter("facilityId", facilityId)
					.getResultList();
			return list;
		}
	}

	public static List<NodeProcessInfo> getNodeProcessInfoByFacilityId(String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeProcessInfo> list = em.createNamedQuery("NodeProcessInfo.findByfacilityId", NodeProcessInfo.class, ObjectPrivilegeMode.NONE)
					.setParameter("facilityId", facilityId)
					.getResultList();
			return list;
		}
	}

	public static List<NodePackageInfo> getNodePackageInfoByFacilityId(String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodePackageInfo> list = em.createNamedQuery("NodePackageInfo.findByfacilityId", NodePackageInfo.class, ObjectPrivilegeMode.NONE)
					.setParameter("facilityId", facilityId)
					.getResultList();
			return list;
		}
	}

	public static List<NodeProductInfo> getNodeProductInfoByFacilityId(String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeProductInfo> list = em.createNamedQuery("NodeProductInfo.findByfacilityId", NodeProductInfo.class, ObjectPrivilegeMode.NONE)
					.setParameter("facilityId", facilityId)
					.getResultList();
			return list;
		}
	}

	public static List<NodeLicenseInfo> getNodeLicenseInfoByFacilityId(String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeLicenseInfo> list = em.createNamedQuery("NodeLicenseInfo.findByfacilityId", NodeLicenseInfo.class, ObjectPrivilegeMode.NONE)
					.setParameter("facilityId", facilityId)
					.getResultList();
			return list;
		}
	}

	public static List<NodeCustomInfo> getNodeCustomByFacilityId(String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeCustomInfo> list = em.createNamedQuery("NodeCustomInfo.findByfacilityId", NodeCustomInfo.class, ObjectPrivilegeMode.NONE)
					.setParameter("facilityId", facilityId)
					.getResultList();
			return list;
		}
	}

	/**
	 * ScopeInfoの一覧を取得する
	 * 
	 * @return ScopeInfoのリスト
	 */
	public static List<ScopeInfo> getAllScope_NONE() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<ScopeInfo> list
			= em.createNamedQuery("ScopeEntity.findAll", ScopeInfo.class, ObjectPrivilegeMode.NONE)
			.getResultList();
			return list;
		}
	}

	public static List<ScopeInfo> getRootScopeFacility_NONE() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<ScopeInfo> list
			= em.createNamedQuery("FacilityEntity.findRootByFacilityType", ScopeInfo.class, ObjectPrivilegeMode.NONE)
			.setParameter("facilityType", FacilityConstant.TYPE_SCOPE)
			.getResultList();
			return list;
		}
	}

	public static FacilityRelationEntity getFacilityRelationPk(String parentFacilityId, String childFacilityId) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
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
	}

	public static List<FacilityInfo> getParentFacilityEntity(String childFacilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
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
	}

	public static List<FacilityInfo> getChildFacilityEntity(String parentFacilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
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
	}

	public static List<FacilityRelationEntity> getAllFacilityRelations_NONE() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<FacilityRelationEntity> list = em.createNamedQuery(
					"FacilityRelationEntity.findAll", FacilityRelationEntity.class,
					ObjectPrivilegeMode.NONE).getResultList();
			return list;
		}
	}

	public static Long getAllFacilityCount() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			Long count
			= em.createNamedQuery("FacilityEntity.findAllCount", Long.class)
			.getSingleResult();
			return count;
		}
	}

	public static List<NodeInfo> getAllNode() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeInfo> list
			= em.createNamedQuery("NodeEntity.findAll", NodeInfo.class)
			.getResultList();
			if (list != null) {
				for (NodeInfo entity : list) {
					try {
						NodeOsInfo nodeOsInfo = QueryUtil.getNodeOsEntityPK(entity.getFacilityId());
						entity.setNodeOsInfo(nodeOsInfo);
					} catch (FacilityNotFound e) {
						// 何もしない
					}
				}
			}
			return list;
		}
	}

	public static Long getAllNodeCount() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			Long count
			= em.createNamedQuery("NodeEntity.findAllCount", Long.class)
			.getSingleResult();
			return count;
		}
	}

	public static List<NodeInfo> getAllNode_NONE() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeInfo> list
			= em.createNamedQuery("FacilityEntity.findByFacilityType", NodeInfo.class, ObjectPrivilegeMode.NONE)
			.setParameter("facilityType", FacilityConstant.TYPE_NODE)
			.getResultList();
			if (list != null) {
				for (NodeInfo entity : list) {
					try {
						NodeOsInfo nodeOsInfo = QueryUtil.getNodeOsEntityPK(entity.getFacilityId());
						entity.setNodeOsInfo(nodeOsInfo);
					} catch (FacilityNotFound e) {
						// 何もしない
					}
				}
			}
			return list;
		}
	}

	public static List<NodeHostnameInfo> getAllNodeHostname() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeHostnameInfo> list
			= em.createNamedQuery("NodeHostnameEntity.findAll", NodeHostnameInfo.class)
			.getResultList();
			return list;
		}
	}

	public static List<NodeInfo> getNodeByIpv4(String ipAddressV4) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeInfo> list
			= em.createNamedQuery("FacilityEntity.findByIpAddressV4", NodeInfo.class)
			.setParameter("ipAddressV4", ipAddressV4)
			.getResultList();

			if (list != null) {
				for (NodeInfo entity : list) {
					try {
						NodeOsInfo nodeOsInfo = QueryUtil.getNodeOsEntityPK(entity.getFacilityId());
						entity.setNodeOsInfo(nodeOsInfo);
					} catch (FacilityNotFound e) {
						// 何もしない
					}
				}
			}
			return list;
		}
	}

	public static List<NodeInfo> getNodeByIpv6(String ipAddressV6) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeInfo> list
			= em.createNamedQuery("FacilityEntity.findByIpAddressV6", NodeInfo.class)
			.setParameter("ipAddressV6", Ipv6Util.expand(ipAddressV6))
			.getResultList();

			if (list != null) {
				for (NodeInfo entity : list) {
					try {
						NodeOsInfo nodeOsInfo = QueryUtil.getNodeOsEntityPK(entity.getFacilityId());
						entity.setNodeOsInfo(nodeOsInfo);
					} catch (FacilityNotFound e) {
						// 何もしない
					}
				}
			}
			return list;
		}
	}

	public static List<NodeInfo> getNodeByNodename(String nodeName) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeInfo> list
			= em.createNamedQuery("FacilityEntity.findByNodename", NodeInfo.class)
			.setParameter("nodeName", nodeName.toLowerCase())
			.getResultList();

			if (list != null) {
				for (NodeInfo entity : list) {
					try {
						NodeOsInfo nodeOsInfo = QueryUtil.getNodeOsEntityPK(entity.getFacilityId());
						entity.setNodeOsInfo(nodeOsInfo);
					} catch (FacilityNotFound e) {
						// 何もしない
					}
				}
			}
			return list;
		}
	}

	public static NodeHostnameInfo getNodeHostnamePK(NodeHostnameInfoPK pk) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
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
	}

	public static NodeVariableInfo getNodeVariablePkForNodeConfigSetting(NodeVariableInfoPK pk) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NodeVariableInfo entity = em.find(NodeVariableInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				FacilityNotFound e = new FacilityNotFound("NodeVariableEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.debug("getNodeVariablePkForNodeConfigSetting() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static NodeHostnameInfo getNodeHostnamePkForNodeConfigSetting(NodeHostnameInfoPK pk) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NodeHostnameInfo entity = em.find(NodeHostnameInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				FacilityNotFound e = new FacilityNotFound("NodeHostnameEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.debug("getNodeHostnamePkForNodeConfigSetting() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static NodeCpuInfo getNodeCpuEntityPK(NodeDeviceInfoPK pk) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
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
	}

	public static NodeCpuInfo getNodeCpuEntityPkForNodeConfigSetting(NodeDeviceInfoPK pk) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NodeCpuInfo entity = em.find(NodeCpuInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				FacilityNotFound e = new FacilityNotFound("NodeCpuEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.debug("getNodeCpuEntityPkForNodeConfigSetting() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static NodeMemoryInfo getNodeMemoryEntityPK(NodeDeviceInfoPK pk) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
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
	}

	public static NodeMemoryInfo getNodeMemoryEntityPkForNodeConfigSetting(NodeDeviceInfoPK pk) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NodeMemoryInfo entity = em.find(NodeMemoryInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				FacilityNotFound e = new FacilityNotFound("NodeMemoryEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.debug("getNodeMemoryEntityPkForNodeConfigSetting() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static NodeNetworkInterfaceInfo getNodeNetworkInterfaceEntityPK(NodeDeviceInfoPK pk) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
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
	}

	public static NodeNetworkInterfaceInfo getNodeNetworkInterfaceEntityPkForNodeConfigSetting(NodeDeviceInfoPK pk) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NodeNetworkInterfaceInfo entity = em.find(NodeNetworkInterfaceInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				FacilityNotFound e = new FacilityNotFound("NodeNetworkInterfaceEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.debug("getNodeNetworkInterfaceEntityPkForNodeConfigSetting() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static NodeDiskInfo getNodeDiskEntityPK(NodeDeviceInfoPK pk) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
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
	}

	public static NodeDiskInfo getNodeDiskEntityPkForNodeConfigSetting(NodeDeviceInfoPK pk) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NodeDiskInfo entity = em.find(NodeDiskInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				FacilityNotFound e = new FacilityNotFound("NodeDiskEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.debug("getNodeDiskEntityPkForNodeConfigSetting() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static NodeFilesystemInfo getNodeFilesystemEntityPK(NodeDeviceInfoPK pk) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
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
	}

	public static NodeFilesystemInfo getNodeFilesystemEntityPkForNodeConfigSetting(NodeDeviceInfoPK pk) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NodeFilesystemInfo entity = em.find(NodeFilesystemInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				FacilityNotFound e = new FacilityNotFound("NodeFilesystemEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.debug("getNodeFilesystemEntityPkForNodeConfigSetting() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static NodeNetstatInfo getNodeNetstatEntityPK(NodeNetstatInfoPK pk) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NodeNetstatInfo entity = em.find(NodeNetstatInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				FacilityNotFound e = new FacilityNotFound("NodeNetstatEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.info("getNodeNetstatEntityPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static NodeNetstatInfo getNodeNetstatEntityPkForNodeConfigSetting(NodeNetstatInfoPK pk) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NodeNetstatInfo entity = em.find(NodeNetstatInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				FacilityNotFound e = new FacilityNotFound("NodeNetstatEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.debug("getNodeNetstatEntityPkForNodeConfigSetting() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static NodePackageInfo getNodePackageEntityPK(NodePackageInfoPK pk) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NodePackageInfo entity = em.find(NodePackageInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				FacilityNotFound e = new FacilityNotFound("NodePackageEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.info("getNodePackageEntityPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static NodePackageInfo getNodePackageEntityPkForNodeConfigSetting(NodePackageInfoPK pk) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NodePackageInfo entity = em.find(NodePackageInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				FacilityNotFound e = new FacilityNotFound("NodePackageEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.debug("getNodePackageEntityPkForNodeConfigSetting() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static NodeProductInfo getNodeProductEntityPK(NodeProductInfoPK pk) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NodeProductInfo entity = em.find(NodeProductInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				FacilityNotFound e = new FacilityNotFound("NodeProductEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.info("getNodeProductEntityPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static NodeProductInfo getNodeProductEntityPkForNodeConfigSetting(NodeProductInfoPK pk) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NodeProductInfo entity = em.find(NodeProductInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				FacilityNotFound e = new FacilityNotFound("NodeProductEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.debug("getNodeProductEntityPkForNodeConfigSetting() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static NodeLicenseInfo getNodeLicenseEntityPK(NodeLicenseInfoPK pk) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NodeLicenseInfo entity = em.find(NodeLicenseInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				FacilityNotFound e = new FacilityNotFound("NodeLicenseEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.info("getNodeLicenseEntityPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static NodeLicenseInfo getNodeLicenseEntityPkForNodeConfigSetting(NodeLicenseInfoPK pk) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NodeLicenseInfo entity = em.find(NodeLicenseInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				FacilityNotFound e = new FacilityNotFound("NodeLicenseEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.debug("getNodeLicenseEntityPkForNodeConfigSetting() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static NodeCustomInfo getNodeCustomEntityPkForNodeConfigSetting(NodeCustomInfoPK pk) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NodeCustomInfo entity = em.find(NodeCustomInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				FacilityNotFound e = new FacilityNotFound("NodeCustomEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.debug("getNodeCustomEntityPkForNodeConfigSetting() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static NodeOsInfo getNodeOsEntityPK(String facilityId) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NodeOsInfo entity = em.find(NodeOsInfo.class, facilityId, ObjectPrivilegeMode.READ);
			if (entity == null) {
				FacilityNotFound e = new FacilityNotFound("NodeOsEntity.findByPrimaryKey"
						+ ", facilityId = " + facilityId);
				m_log.info("getNodeOsEntityPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static NodeOsInfo getNodeOsEntityPkForNodeConfigSetting(String facilityId) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NodeOsInfo entity = em.find(NodeOsInfo.class, facilityId, ObjectPrivilegeMode.READ);
			if (entity == null) {
				FacilityNotFound e = new FacilityNotFound("NodeOsEntity.findByPrimaryKey"
						+ ", facilityId = " + facilityId);
				m_log.debug("getNodeOsEntityPkForNodeConfigSetting() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static NodeConfigSettingInfo getNodeConfigSettingInfoPK(String settingId, ObjectPrivilegeMode mode) throws NodeConfigSettingNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NodeConfigSettingInfo entity = em.find(NodeConfigSettingInfo.class, settingId, mode);
			if (entity == null) {
				NodeConfigSettingNotFound e = new NodeConfigSettingNotFound("NodeConfigSettingInfo.findByPrimaryKey"
						+ settingId);
				m_log.info("getNodeConfigSettingInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static List<NodeConfigSettingInfo> getNodeConfigSettingListByFacilityIdsAndValid(List<String> facilityIds, ObjectPrivilegeMode mode)
			throws NodeConfigSettingNotFound {
		List<NodeConfigSettingInfo> list = new ArrayList<>();
		if (facilityIds == null || facilityIds.size() == 0) {
			return list;
		}
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			StringBuilder sbJpql = new StringBuilder();
			sbJpql.append("SELECT a FROM NodeConfigSettingInfo a");
			sbJpql.append(" WHERE a.facilityId IN (" + HinemosEntityManager.getParamNameString("facilityId", facilityIds.toArray()) + ")");
			sbJpql.append(" AND a.validFlg = true");
			TypedQuery<NodeConfigSettingInfo> typedQuery = em.createQuery(sbJpql.toString(), NodeConfigSettingInfo.class, mode);
			typedQuery = HinemosEntityManager.appendParam(typedQuery, "facilityId", facilityIds.toArray());

			return typedQuery.getResultList();
		}
	}

	public static List<NodeConfigSettingInfo> getAllNodeConfigSettingList() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeConfigSettingInfo> list
				= em.createNamedQuery("NodeConfigSettingInfo.findAll", NodeConfigSettingInfo.class)
				.getResultList();
			return list;
		}
	}

	public static NodeHistory getNodeHistoryPk(String facilityId, Long regDate) throws NodeHistoryNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NodeHistoryPK pk = new NodeHistoryPK(facilityId, regDate);
			NodeHistory entity = em.find(NodeHistory.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				NodeHistoryNotFound e = new NodeHistoryNotFound("NodeHistoryEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.info("getNodeHistoryPk() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static List<NodeConfigSettingInfo> getNodeConfigSettingInfoFindByCalendarId_NONE(String calendarId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeConfigSettingInfo> list
			= em.createNamedQuery("NodeConfigSettingInfo.findByCalendarId"
					,NodeConfigSettingInfo.class, ObjectPrivilegeMode.NONE)
					.setParameter("calendarId", calendarId)
					.getResultList();
			return list;
		}
	}

	public static List<NodeConfigSettingInfo> getNodeConfigSettingInfoByFacilityId_NONE(String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeConfigSettingInfo> list
			= em.createNamedQuery("NodeConfigSettingInfo.findByFacilityId", NodeConfigSettingInfo.class, ObjectPrivilegeMode.NONE)
			.setParameter("facilityId", facilityId)
			.getResultList();
			return list;
		}
	}

	public static NodeCpuHistoryDetail getNodeCpuHistoryDetailByRegDateTo(
			NodeDeviceInfoPK pk, Long regDateTo) {
		NodeCpuHistoryDetail rtn = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeCpuHistoryDetail> list
				= em.createNamedQuery("NodeCpuHistoryDetailEntity.findByRegDateTo", NodeCpuHistoryDetail.class, ObjectPrivilegeMode.NONE)
				.setParameter("facilityId", pk.getFacilityId())
				.setParameter("deviceIndex", pk.getDeviceIndex())
				.setParameter("deviceType", pk.getDeviceType())
				.setParameter("deviceName", pk.getDeviceName())
				.setParameter("regDateTo", regDateTo)
				.getResultList();
			if (list.size() > 0) {
				rtn = list.get(0);
			}
			return rtn;
		}
	}

	public static NodeDiskHistoryDetail getNodeDiskHistoryDetailByRegDateTo(
			NodeDeviceInfoPK pk, Long regDateTo) {
		NodeDiskHistoryDetail rtn = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeDiskHistoryDetail> list
				= em.createNamedQuery("NodeDiskHistoryDetailEntity.findByRegDateTo", NodeDiskHistoryDetail.class, ObjectPrivilegeMode.NONE)
				.setParameter("facilityId", pk.getFacilityId())
				.setParameter("deviceIndex", pk.getDeviceIndex())
				.setParameter("deviceType", pk.getDeviceType())
				.setParameter("deviceName", pk.getDeviceName())
				.setParameter("regDateTo", regDateTo)
				.getResultList();
			if (list.size() > 0) {
				rtn = list.get(0);
			}
			return rtn;
		}
	}

	public static NodeFilesystemHistoryDetail getNodeFilesystemHistoryDetailByRegDateTo(
			NodeDeviceInfoPK pk, Long regDateTo) {
		NodeFilesystemHistoryDetail rtn = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeFilesystemHistoryDetail> list
				= em.createNamedQuery("NodeFilesystemHistoryDetailEntity.findByRegDateTo", NodeFilesystemHistoryDetail.class, ObjectPrivilegeMode.NONE)
				.setParameter("facilityId", pk.getFacilityId())
				.setParameter("deviceIndex", pk.getDeviceIndex())
				.setParameter("deviceType", pk.getDeviceType())
				.setParameter("deviceName", pk.getDeviceName())
				.setParameter("regDateTo", regDateTo)
				.getResultList();
			if (list.size() > 0) {
				rtn = list.get(0);
			}
			return rtn;
		}
	}

	public static NodeVariableHistoryDetail getNodeVariableHistoryDetailByRegDateTo(
			NodeVariableInfoPK pk, Long regDateTo) {
		NodeVariableHistoryDetail rtn = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeVariableHistoryDetail> list
				= em.createNamedQuery("NodeVariableHistoryDetailEntity.findByRegDateTo", NodeVariableHistoryDetail.class, ObjectPrivilegeMode.NONE)
				.setParameter("facilityId", pk.getFacilityId())
				.setParameter("nodeVariableName", pk.getNodeVariableName())
				.setParameter("regDateTo", regDateTo)
				.getResultList();
			if (list.size() > 0) {
				rtn = list.get(0);
			}
			return rtn;
		}
	}

	public static NodeHostnameHistoryDetail getNodeHostnameHistoryDetailByRegDateTo(
			NodeHostnameInfoPK pk, Long regDateTo) {
		NodeHostnameHistoryDetail rtn = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeHostnameHistoryDetail> list
				= em.createNamedQuery("NodeHostnameHistoryDetailEntity.findByRegDateTo", NodeHostnameHistoryDetail.class, ObjectPrivilegeMode.NONE)
				.setParameter("facilityId", pk.getFacilityId())
				.setParameter("hostname", pk.getHostname())
				.setParameter("regDateTo", regDateTo)
				.getResultList();
			if (list.size() > 0) {
				rtn = list.get(0);
			}
			return rtn;
		}
	}

	public static NodeMemoryHistoryDetail getNodeMemoryHistoryDetailByRegDateTo(
			NodeDeviceInfoPK pk, Long regDateTo) {
		NodeMemoryHistoryDetail rtn = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeMemoryHistoryDetail> list
				= em.createNamedQuery("NodeMemoryHistoryDetailEntity.findByRegDateTo", NodeMemoryHistoryDetail.class, ObjectPrivilegeMode.NONE)
				.setParameter("facilityId", pk.getFacilityId())
				.setParameter("deviceIndex", pk.getDeviceIndex())
				.setParameter("deviceType", pk.getDeviceType())
				.setParameter("deviceName", pk.getDeviceName())
				.setParameter("regDateTo", regDateTo)
				.getResultList();
			if (list.size() > 0) {
				rtn = list.get(0);
			}
			return rtn;
		}
	}

	public static NodeNetworkInterfaceHistoryDetail getNodeNetworkInterfaceHistoryDetailByRegDateTo(
			NodeDeviceInfoPK pk, Long regDateTo) {
		NodeNetworkInterfaceHistoryDetail rtn = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeNetworkInterfaceHistoryDetail> list
				= em.createNamedQuery("NodeNetworkInterfaceHistoryDetailEntity.findByRegDateTo", NodeNetworkInterfaceHistoryDetail.class, ObjectPrivilegeMode.NONE)
				.setParameter("facilityId", pk.getFacilityId())
				.setParameter("deviceIndex", pk.getDeviceIndex())
				.setParameter("deviceType", pk.getDeviceType())
				.setParameter("deviceName", pk.getDeviceName())
				.setParameter("regDateTo", regDateTo)
				.getResultList();
			if (list.size() > 0) {
				rtn = list.get(0);
			}
			return rtn;
		}
	}

	public static NodeOsHistoryDetail getNodeOsHistoryDetailByRegDateTo(
			String facilityId, Long regDateTo) {
		NodeOsHistoryDetail rtn = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeOsHistoryDetail> list
				= em.createNamedQuery("NodeOsHistoryDetailEntity.findByRegDateTo", NodeOsHistoryDetail.class, ObjectPrivilegeMode.NONE)
				.setParameter("facilityId", facilityId)
				.setParameter("regDateTo", regDateTo)
				.getResultList();
			if (list.size() > 0) {
				rtn = list.get(0);
			}
			return rtn;
		}
	}

	public static NodeNetstatHistoryDetail getNodeNetstatHistoryDetailByRegDateTo(NodeNetstatInfoPK pk, Long regDateTo) {
		NodeNetstatHistoryDetail rtn = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeNetstatHistoryDetail> list
				= em.createNamedQuery("NodeNetstatHistoryDetailEntity.findByRegDateTo", NodeNetstatHistoryDetail.class, ObjectPrivilegeMode.NONE)
				.setParameter("facilityId", pk.getFacilityId())
				.setParameter("protocol", pk.getProtocol())
				.setParameter("localIpAddress", pk.getLocalIpAddress())
				.setParameter("localPort", pk.getLocalPort())
				.setParameter("foreignIpAddress", pk.getForeignIpAddress())
				.setParameter("foreignPort", pk.getForeignPort())
				.setParameter("processName", pk.getProcessName())
				.setParameter("pid", pk.getPid())
				.setParameter("regDateTo", regDateTo)
				.getResultList();
			if (list.size() > 0) {
				rtn = list.get(0);
			}
			return rtn;
		}
	}

	public static NodePackageHistoryDetail getNodePackageHistoryDetailByRegDateTo(NodePackageInfoPK pk, Long regDateTo) {
		NodePackageHistoryDetail rtn = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodePackageHistoryDetail> list
				= em.createNamedQuery("NodePackageHistoryDetailEntity.findByRegDateTo", NodePackageHistoryDetail.class, ObjectPrivilegeMode.NONE)
				.setParameter("facilityId", pk.getFacilityId())
				.setParameter("packageId", pk.getPackageId())
				.setParameter("regDateTo", regDateTo)
				.getResultList();
			if (list.size() > 0) {
				rtn = list.get(0);
			}
			return rtn;
		}
	}

	public static NodeProductHistoryDetail getNodeProductHistoryDetailByRegDateTo(NodeProductInfoPK pk, Long regDateTo) {
		NodeProductHistoryDetail rtn = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeProductHistoryDetail> list
				= em.createNamedQuery("NodeProductHistoryDetailEntity.findByRegDateTo", NodeProductHistoryDetail.class, ObjectPrivilegeMode.NONE)
				.setParameter("facilityId", pk.getFacilityId())
				.setParameter("productName", pk.getProductName())
				.setParameter("regDateTo", regDateTo)
				.getResultList();
			if (list.size() > 0) {
				rtn = list.get(0);
			}
			return rtn;
		}
	}

	public static NodeLicenseHistoryDetail getNodeLicenseHistoryDetailByRegDateTo(NodeLicenseInfoPK pk, Long regDateTo) {
		NodeLicenseHistoryDetail rtn = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeLicenseHistoryDetail> list
				= em.createNamedQuery("NodeLicenseHistoryDetailEntity.findByRegDateTo", NodeLicenseHistoryDetail.class, ObjectPrivilegeMode.NONE)
				.setParameter("facilityId", pk.getFacilityId())
				.setParameter("productName", pk.getProductName())
				.setParameter("regDateTo", regDateTo)
				.getResultList();
			if (list.size() > 0) {
				rtn = list.get(0);
			}
			return rtn;
		}
	}

	public static NodeCustomHistoryDetail getNodeCustomHistoryDetailByRegDateTo(NodeCustomInfoPK pk, Long regDateTo) {
		NodeCustomHistoryDetail rtn = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeCustomHistoryDetail> list
				= em.createNamedQuery("NodeCustomHistoryDetailEntity.findByRegDateTo", NodeCustomHistoryDetail.class, ObjectPrivilegeMode.NONE)
				.setParameter("facilityId", pk.getFacilityId())
				.setParameter("settingId", pk.getSettingId())
				.setParameter("settingCustomId", pk.getSettingCustomId())
				.setParameter("regDateTo", regDateTo)
				.getResultList();
			if (list.size() > 0) {
				rtn = list.get(0);
			}
			return rtn;
		}
	}

	public static List<Date> selectTargetDateNodeHistoryByRegDate(String roleId, Long regDate) {
		StringBuilder sbJpql = new StringBuilder();
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			if (roleId != null && !"".equals(roleId)) {
				sbJpql.append("SELECT a.id.regDate FROM NodeHistory a");
				sbJpql.append(" WHERE a.id.facilityId IN (");
				sbJpql.append(" SELECT b.facilityId FROM FacilityInfo b");
				sbJpql.append(" WHERE b.ownerRoleId = :roleId");
				sbJpql.append(" )");
				sbJpql.append(" AND a.id.regDate < :regDate");
				sbJpql.append(" GROUP BY a.id.regDate ORDER BY a.id.regDate");
				List<Long> list = em.createQuery(sbJpql.toString(), Long.class)
						.setParameter("roleId", roleId)
						.setParameter("regDate", regDate)
						.getResultList();
				return getTargetDateListByUnixTimeLsit(list);
			} else {
				sbJpql.append("SELECT a.id.regDate FROM NodeHistory a");
				sbJpql.append(" WHERE a.id.regDate < :regDate");
				sbJpql.append(" GROUP BY a.id.regDate ORDER BY a.id.regDate");
				List<Long> list = em.createQuery(sbJpql.toString(), Long.class)
						.setParameter("regDate", regDate)
						.getResultList();
				return getTargetDateListByUnixTimeLsit(list);
			}
		}
	}

	public static int deleteNodeHistoryByRegDate(String roleId, Date targetDate) {
		StringBuilder sbJpql = new StringBuilder();
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			if (roleId != null && !"".equals(roleId)) {
				sbJpql.append("DELETE FROM NodeHistory a");
				sbJpql.append(" WHERE a.id.facilityId IN (");
				sbJpql.append(" SELECT b.facilityId FROM FacilityInfo b");
				sbJpql.append(" WHERE b.ownerRoleId = :roleId");
				sbJpql.append(" )");
				sbJpql.append(" AND a.id.regDate < :regDate");
				return em.createQuery(sbJpql.toString(), Integer.class)
						.setParameter("roleId", roleId)
						.setParameter("regDate", parseTargetDateToTargetUnixTime(targetDate))
						.executeUpdate();
			} else {
				sbJpql.append("DELETE FROM NodeHistory a");
				sbJpql.append(" WHERE a.id.regDate < :regDate");
				return em.createQuery(sbJpql.toString(), Integer.class)
						.setParameter("regDate", parseTargetDateToTargetUnixTime(targetDate))
						.executeUpdate();
			}
		}
	}

	public static List<Date> selectTargetDateNodeHistoryDetailByRegDateTo(
			Class<? extends NodeHistoryDetail> historyDetailClass, String roleId, Long regDateTo) {
		StringBuilder sbJpql = new StringBuilder();
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			if (roleId != null && !"".equals(roleId)) {
				sbJpql.append(String.format("SELECT a.regDateTo FROM %s a", historyDetailClass.getSimpleName()));
				sbJpql.append(" WHERE a.id.facilityId IN (");
				sbJpql.append(" SELECT b.facilityId FROM FacilityInfo b");
				sbJpql.append(" WHERE b.ownerRoleId = :roleId");
				sbJpql.append(" )");
				sbJpql.append(" AND a.regDateTo < :regDateTo");
				sbJpql.append(" GROUP BY a.regDateTo ORDER BY a.regDateTo");
				List<Long> list = em.createQuery(sbJpql.toString(), Long.class)
						.setParameter("roleId", roleId)
						.setParameter("regDateTo", regDateTo)
						.getResultList();
				return getTargetDateListByUnixTimeLsit(list);
			} else {
				sbJpql.append(String.format("SELECT a.regDateTo FROM %s a", historyDetailClass.getSimpleName()));
				sbJpql.append(" WHERE a.regDateTo < :regDateTo");
				sbJpql.append(" GROUP BY a.regDateTo ORDER BY a.regDateTo");
				List<Long> list = em.createQuery(sbJpql.toString(), Long.class)
						.setParameter("regDateTo", regDateTo)
						.getResultList();
				return getTargetDateListByUnixTimeLsit(list);
			}
		}
	}

	public static int deleteNodeHistoryDetailByRegDateTo(
			Class<? extends NodeHistoryDetail> historyDetailClass, String roleId, Date targetDate) {
		StringBuilder sbJpql = new StringBuilder();
		Long regDate = parseTargetDateToTargetUnixTime(targetDate);
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			if (roleId != null && !"".equals(roleId)) {
				sbJpql.append(String.format("DELETE FROM %s a", historyDetailClass.getSimpleName()));
				sbJpql.append(" WHERE a.id.facilityId IN (");
				sbJpql.append(" SELECT b.facilityId FROM FacilityInfo b");
				sbJpql.append(" WHERE b.ownerRoleId = :roleId");
				sbJpql.append(" )");
				sbJpql.append(" AND a.regDateTo < :regDateTo");
				return em.createQuery(sbJpql.toString(), Integer.class)
						.setParameter("roleId", roleId)
						.setParameter("regDateTo", regDate)
						.executeUpdate();
			} else {
				sbJpql.append(String.format("DELETE FROM %s a", historyDetailClass.getSimpleName()));
				sbJpql.append(" WHERE a.regDateTo < :regDateTo");
				return em.createQuery(sbJpql.toString(), Integer.class)
						.setParameter("regDateTo", regDate)
						.executeUpdate();
			}
		}
	}

	/**
	 * 構成情報削除処理
	 * 
	 * @param infoClass 構成情報クラス
	 * @param facilityId ファシリティID
	 * @return 削除件数
	 */
	public static int deleteNodeOptionalInfoByFacilityId(Class<?> infoClass, String facilityId) {
		if (!(infoClass == NodeOsInfo.class
				|| infoClass == NodeCpuInfo.class
				|| infoClass == NodeMemoryInfo.class
				|| infoClass == NodeNetworkInterfaceInfo.class
				|| infoClass == NodeDiskInfo.class
				|| infoClass == NodeFilesystemInfo.class
				|| infoClass == NodeVariableInfo.class
				|| infoClass == NodeHostnameInfo.class
				|| infoClass == NodeNetstatInfo.class
				|| infoClass == NodeProcessInfo.class
				|| infoClass == NodePackageInfo.class
				|| infoClass == NodeProductInfo.class
				|| infoClass == NodeLicenseInfo.class
				|| infoClass == NodeCustomInfo.class)) {
			return 0;
		}
		StringBuilder sbJpql = new StringBuilder();
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			sbJpql.append(String.format("DELETE FROM %s a", infoClass.getSimpleName()));
			if (infoClass == NodeOsInfo.class) {
				sbJpql.append(" WHERE a.facilityId = :facilityId");
			} else {
				sbJpql.append(" WHERE a.id.facilityId = :facilityId");
			}
			return em.createQuery(sbJpql.toString(), Integer.class)
					.setParameter("facilityId", facilityId)
					.executeUpdate();
		}
	}

	/**
	 * 構成情報履歴詳細削除フラグ更新処理
	 * 
	 * @param historyDetailClass 構成情報履歴詳細クラス
	 * @param modifyDatetime 更新日時
	 * @param modifyUserId 更新ユーザ
	 * @param facilityId ファシリティID
	 * @return 更新件数
	 */
	public static int modifyNodeHistoryDetailDeleteByFacilityId(
			Class<? extends NodeHistoryDetail> historyDetailClass, Long modifyDatetime, String modifyUserId, String facilityId) {
		StringBuilder sbJpql = new StringBuilder();
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			sbJpql.append(String.format("UPDATE %s a SET", historyDetailClass.getSimpleName()));
			sbJpql.append(" a.regDateTo = :regDateTo,");
			sbJpql.append(" a.regUser = :regUser");
			sbJpql.append(" WHERE a.id.facilityId = :facilityId");
			sbJpql.append(" AND a.regDateTo = :regDateToBefore");
			return em.createQuery(sbJpql.toString(), Integer.class)
					.setParameter("regDateTo", modifyDatetime)
					.setParameter("regUser", modifyUserId)
					.setParameter("facilityId", facilityId)
					.setParameter("regDateToBefore", NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE)
					.executeUpdate();
		}
	}

	public static <T extends NodeHistoryDetail> List<T> getNodeHistoryDetailByFacilityIdRegDate(
			Class<T> historyDetailClass, String facilityId, Long regDate) {
		StringBuilder sbJpql = new StringBuilder();
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			sbJpql.append(String.format("SELECT a FROM %s a", historyDetailClass.getSimpleName()));
			sbJpql.append(" WHERE a.id.facilityId = :facilityId");
			sbJpql.append(" AND a.id.regDate <= :regDate");
			sbJpql.append(" AND a.regDateTo > :regDateTo");
			if (NodeDeviceHistoryDetail.class.isAssignableFrom(historyDetailClass)) {
				sbJpql.append(" ORDER BY a.id.deviceName");
			} else if (historyDetailClass == NodeOsHistoryDetail.class) {
				// Order By 不要
			} else if (historyDetailClass == NodeHostnameHistoryDetail.class) {
				sbJpql.append(" ORDER BY a.id.hostname");
			} else if (historyDetailClass == NodeNetstatHistoryDetail.class) {
				sbJpql.append(" ORDER BY a.id.protocol, a.id.localIpAddress, a.id.localPort, a.id.foreignIpAddress,");
				sbJpql.append(" a.id.foreignPort, a.id.processName, a.id.pid");
			} else if (historyDetailClass == NodePackageHistoryDetail.class) {
				sbJpql.append(" ORDER BY a.packageName, a.id.packageId");
			} else if (historyDetailClass == NodeProductHistoryDetail.class) {
				sbJpql.append(" ORDER BY a.id.productName");
			} else if (historyDetailClass == NodeVariableHistoryDetail.class) {
				sbJpql.append(" ORDER BY a.id.nodeVariableName");
			} else if (historyDetailClass == NodeLicenseHistoryDetail.class) {
				sbJpql.append(" ORDER BY a.id.productName");
			} else if (historyDetailClass == NodeCustomHistoryDetail.class) {
				sbJpql.append(" ORDER BY  a.id.settingId, a.id.settingCustomId");
			}
			return em.createQuery(sbJpql.toString(), historyDetailClass)
					.setParameter("facilityId", facilityId)
					.setParameter("regDate", regDate)
					.setParameter("regDateTo", regDate)
					.getResultList();
		}
	}

	public static Long getNodeHistoryByMaxRegDate(NodeConfigSettingItem item, String facilityId) {
		String columnName = "";
		if (item == NodeConfigSettingItem.OS) {
			columnName = "osFlag";
		} else if (item == NodeConfigSettingItem.HW_CPU) {
			columnName = "cpuFlag";
		} else if (item == NodeConfigSettingItem.HW_DISK) {
			columnName = "diskFlag";
		} else if (item == NodeConfigSettingItem.HW_FILESYSTEM) {
			columnName = "filesystemFlag";
		} else if (item == NodeConfigSettingItem.HOSTNAME) {
			columnName = "hostnameFlag";
		} else if (item == NodeConfigSettingItem.HW_MEMORY) {
			columnName = "memoryFlag";
		} else if (item == NodeConfigSettingItem.HW_NIC) {
			columnName = "networkInterfaceFlag";
		} else if (item == NodeConfigSettingItem.NODE_VARIABLE) {
			columnName = "nodeVariableFlag";
		} else if (item == NodeConfigSettingItem.NETSTAT) {
			columnName = "netstatFlag";
		} else if (item == NodeConfigSettingItem.PACKAGE) {
			columnName = "packageFlag";
		} else if (item == NodeConfigSettingItem.PRODUCT) {
			columnName = "productFlag";
		} else if (item == NodeConfigSettingItem.LICENSE) {
			columnName = "licenseFlag";
		} else {
			return null;
		}

		StringBuilder sbJpql = new StringBuilder();
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			sbJpql.append("SELECT MAX(a.id.regDate) FROM NodeHistory a");
			sbJpql.append(" WHERE a.id.facilityId = :facilityId");
			sbJpql.append(String.format(" AND a.%s = true", columnName));
			return em.createQuery(sbJpql.toString(), Long.class, ObjectPrivilegeMode.NONE)
					.setParameter("facilityId", facilityId)
					.getSingleResult();
		}
	}

	/**
	 * 対象構成情報の収集項目取得
	 * 
	 * @param pk
	 * @return 対象構成情報の収集項目
	 * @throws NodeConfigSettingNotFound
	 */
	public static NodeConfigSettingItemInfo getNodeConfigSettingItemInfoPK(NodeConfigSettingItemInfoPK pk) throws NodeConfigSettingNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NodeConfigSettingItemInfo entity = em.find(NodeConfigSettingItemInfo.class, pk, ObjectPrivilegeMode.NONE);
			if (entity == null) {
				NodeConfigSettingNotFound e = new NodeConfigSettingNotFound("NodeConfigSettingItemInfo.findByPrimaryKey"
						+ pk.toString());
				m_log.info("getNodeConfigSettingItemInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	/**
	 * ユーザ任意情報の収集設定の取得
	 * 
	 * @param pk
	 * @return ユーザ任意情報の収集設定
	 * @throws NodeConfigSettingNotFound
	 */
	public static NodeConfigCustomInfo getNodeConfigCustomInfoPK(NodeConfigCustomInfoPK pk) throws NodeConfigSettingNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NodeConfigCustomInfo entity = em.find(NodeConfigCustomInfo.class, pk, ObjectPrivilegeMode.NONE);
			if (entity == null) {
				NodeConfigSettingNotFound e = new NodeConfigSettingNotFound("NodeConfigCustomInfo.findByPrimaryKey"
						+ pk.toString());
				m_log.info("getNodeConfigCustomInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static CollectorPlatformMstEntity getCollectorPlatformMstPK(String platformId) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
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
	}

	public static CollectorSubPlatformMstEntity getCollectorSubPlatformMstPK(String subPlatformId) throws FacilityNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
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
	}

	public static List<CollectorPlatformMstEntity> getAllCollectorPlatformMst() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<CollectorPlatformMstEntity> list
			= em.createNamedQuery("CollectorPlatformMstEntity.findAll", CollectorPlatformMstEntity.class)
			.getResultList();
			return list;
		}
	}

	public static List<CollectorSubPlatformMstEntity> getAllCollectorSubPlatformMstEntity() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<CollectorSubPlatformMstEntity> list
			= em.createNamedQuery("CollectorSubPlatformMstEntity.findAll", CollectorSubPlatformMstEntity.class)
			.getResultList();
			return list;
		}
	}
	
	public static AutoRegisterNodeInfo getAutoRegisterNodeInfo(Integer orderNo, ObjectPrivilegeMode mode) throws AutoRegisterNodeSettingNotFound, InvalidRole {
		AutoRegisterNodeInfo entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find(AutoRegisterNodeInfo.class, orderNo, mode);
			if (entity == null) {
				AutoRegisterNodeSettingNotFound e = new AutoRegisterNodeSettingNotFound("AutoRegisterNodeInfo.findByPrimaryKey"
						+ ", orderNo = " + orderNo);
				m_log.info("getAutoRegisterNodeInfo() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getAutoRegisterNodeInfo() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static Long getLatestNodePackageHistoryDate(String facilityId, Long date) {
		Long maxDate = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			long regDate = NodeConfigSettingConstant.REG_DATE_TO_DEFAULT_VALUE;
			if (date != null) {
				regDate = date;
			}
			maxDate = em.createNamedQuery("NodeHistoryEntity.findPackageLatest",Long.class)
					.setParameter("facilityId", facilityId)
					.setParameter("regDate", regDate)
					.getSingleResult();
		} catch (NoResultException e) {
			m_log.debug("getLatestNodePackageHistoryDate : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
		}
		return maxDate;
	}

	public static List<NodeProcessInfo> getLatestNodeProcesses(String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NodeProcessInfo> list
			= em.createNamedQuery("NodeProcessInfoEntity.findLatests", NodeProcessInfo.class, ObjectPrivilegeMode.NONE)
			.setParameter("facilityId", facilityId)
			.getResultList();
			return list;
		}
	}

	public static List<AutoRegisterNodeInfo> getValidAutoRegisterNodeInfo() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<AutoRegisterNodeInfo> list
			= em.createNamedQuery("AutoRegisterNodeInfo.findValid", AutoRegisterNodeInfo.class, ObjectPrivilegeMode.NONE)
			.getResultList();
			return list;
		}
	}

	/**
	 * ノードフィルタ検索処理
	 * 
	 * @param filterInfo 検索条件
	 * @param targetDatetime 対象日時
	 * @param allNodeFacilityIdList ノード全件のファシリティID一覧
	 * @return ノードのファシリティID一覧
	 * @throws HinemosDbTimeout
	 */
	public static List<String> getNodeFacilityIdByNodeConfig(
			NodeConfigFilterInfo filterInfo, Long targetDatetime, List<String> allNodeFacilityIdList) throws HinemosDbTimeout {
		m_log.debug("getNodeFacilityIdByNodeConfig() : start");

		List<String> rtnList = new ArrayList<>();

		boolean isNow = true;

		if (filterInfo == null 
				|| filterInfo.getItemList() == null
				|| filterInfo.getItemList().size() == 0) {
			return rtnList;
		}

		// 現在のデータを対象として検索を行うのか
		if (targetDatetime != null && targetDatetime > 0L) {
			isNow = false;
		}

		// タイムアウト値を取得
		int searchTimeout = HinemosPropertyCommon.node_config_search_timeout.getIntegerValue();

		// 検索結果格納用
		HashSet<String> compareSet = new HashSet<>();
		Map<String, Object> parameters = new HashMap<String, Object>();
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {

			StringBuffer sbJpql = new StringBuffer();

			String column = "";

			if (filterInfo.getNodeConfigSettingItem() == NodeConfigSettingItem.PACKAGE) {
				// パッケージ情報
				NodeConfigFilterItemInfo versionItem = null;
				NodeConfigFilterItemInfo releaseItem = null;
				if (isNow) {
					sbJpql.append(" SELECT a FROM NodePackageInfo a WHERE true = true");
				} else {
					sbJpql.append(" SELECT a FROM NodePackageHistoryDetail a WHERE true = true");
				}
				// 対象のファシリティIDを条件に追加
				sbJpql.append(" AND a.id.facilityId IN :facilityIds");
				parameters.put("facilityIds", allNodeFacilityIdList);

				for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
					if (itemInfo.getItem() == NodeConfigFilterItem.PACKAGE_NAME) {
						column = "a.packageName";
					} else if (itemInfo.getItem() == NodeConfigFilterItem.PACKAGE_VERSION) {
						versionItem = itemInfo;
						continue;
					} else if (itemInfo.getItem() == NodeConfigFilterItem.PACKAGE_RELEASE) {
						releaseItem = itemInfo;
						continue;
					} else if (itemInfo.getItem() == NodeConfigFilterItem.PACKAGE_INSTALL_DATE) {
						column = "a.installDate";
					} else if (itemInfo.getItem() == NodeConfigFilterItem.PACKAGE_VENDOR) {
						column = "a.vendor";
					} else if (itemInfo.getItem() == NodeConfigFilterItem.PACKAGE_ARCHITECTURE) {
						column = "a.architecture";
					}
					if (itemInfo.getItem().dataType() == NodeConfigFilterDataType.STRING_ONLYEQUAL) { 
						if (itemInfo.getMethodType() == NodeConfigFilterComparisonMethod.EQ) {
							sbJpql.append(String.format(" AND %s like :%s", column, itemInfo.getItem().name()));
						} else if (itemInfo.getMethodType() == NodeConfigFilterComparisonMethod.NE) {
							sbJpql.append(String.format(" AND %s not like :%s", column, itemInfo.getItem().name()));
						}
					} else {
						sbJpql.append(String.format(" AND %s %s :%s", column, itemInfo.getMethodType().symbol(), itemInfo.getItem().name()));
					}
				}

				if (isNow) {
					for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
						if (itemInfo.getItem() == NodeConfigFilterItem.PACKAGE_VERSION
								|| itemInfo.getItem() == NodeConfigFilterItem.PACKAGE_RELEASE) {
							continue;
						}
						if (itemInfo.getItem().dataType() == NodeConfigFilterDataType.STRING_ONLYEQUAL) {
							parameters.put(itemInfo.getItem().name(), 
									QueryDivergence.escapeConditionNodeMap((String)itemInfo.getItemValue()) + "%");
						} else {
							parameters.put(itemInfo.getItem().name(), itemInfo.getItemValue());
						}

					}
					List<NodePackageInfo> list = QueryExecutor.getListByJpqlWithTimeout(
							sbJpql.toString(), NodePackageInfo.class, parameters, searchTimeout);

					if (list.size() != 0) {
						for (NodePackageInfo info : list) {
							boolean isTarget = true;
							if (versionItem != null) {
								Integer compareResult = NodeConfigFilterUtil.compareVersion(info.getVersion(), (String)versionItem.getItemValue());
								if((versionItem.getMethodType() == NodeConfigFilterComparisonMethod.EQ && compareResult == 0)
									|| 	(versionItem.getMethodType() == NodeConfigFilterComparisonMethod.GE && compareResult >= 0)
									|| 	(versionItem.getMethodType() == NodeConfigFilterComparisonMethod.GT && compareResult > 0)
									|| 	(versionItem.getMethodType() == NodeConfigFilterComparisonMethod.LE && compareResult <= 0)
									|| 	(versionItem.getMethodType() == NodeConfigFilterComparisonMethod.LT && compareResult < 0)
									|| 	(versionItem.getMethodType() == NodeConfigFilterComparisonMethod.NE && compareResult != 0)) {
									// 条件に一致する場合
								} else {
									isTarget = false;
								}
							}
							if (releaseItem != null) {
								Integer compareResult = NodeConfigFilterUtil.compareVersion(info.getRelease(), (String)releaseItem.getItemValue());
								if((releaseItem.getMethodType() == NodeConfigFilterComparisonMethod.EQ && compareResult == 0)
									|| 	(releaseItem.getMethodType() == NodeConfigFilterComparisonMethod.GE && compareResult >= 0)
									|| 	(releaseItem.getMethodType() == NodeConfigFilterComparisonMethod.GT && compareResult > 0)
									|| 	(releaseItem.getMethodType() == NodeConfigFilterComparisonMethod.LE && compareResult <= 0)
									|| 	(releaseItem.getMethodType() == NodeConfigFilterComparisonMethod.LT && compareResult < 0)
									|| 	(releaseItem.getMethodType() == NodeConfigFilterComparisonMethod.NE && compareResult != 0)) {
									// 条件に一致する場合
								} else {
									isTarget = false;
								}
							}
							if (isTarget) {
								compareSet.add(info.getFacilityId());
							}
						}
					}
				} else {
					// 検索条件に日時を付与
					sbJpql.append(String.format(" AND a.id.regDate <= :regDate"));
					sbJpql.append(String.format(" AND a.regDateTo > :regDateTo"));

					for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
						if (itemInfo.getItem() == NodeConfigFilterItem.PACKAGE_VERSION
								|| itemInfo.getItem() == NodeConfigFilterItem.PACKAGE_RELEASE) {
							continue;
						}
						if (itemInfo.getItem().dataType() == NodeConfigFilterDataType.STRING_ONLYEQUAL) { 
							parameters.put(itemInfo.getItem().name(),
									QueryDivergence.escapeConditionNodeMap((String)itemInfo.getItemValue()) + "%");
						} else {
							parameters.put(itemInfo.getItem().name(), itemInfo.getItemValue());
						}

					}
					// 検索条件に日時を付与
					parameters.put("regDate", targetDatetime);
					parameters.put("regDateTo", targetDatetime);

					List<NodePackageHistoryDetail> list = QueryExecutor.getListByJpqlWithTimeout(
							sbJpql.toString(), NodePackageHistoryDetail.class, parameters, searchTimeout);

					if (list.size() != 0) {
						for (NodePackageHistoryDetail historyDetail : list) {
							boolean isTarget = true;
							if (versionItem != null) {
								Integer compareResult = NodeConfigFilterUtil.compareVersion(historyDetail.getVersion(), (String)versionItem.getItemValue());
								if((versionItem.getMethodType() == NodeConfigFilterComparisonMethod.EQ && compareResult == 0)
									|| 	(versionItem.getMethodType() == NodeConfigFilterComparisonMethod.GE && compareResult >= 0)
									|| 	(versionItem.getMethodType() == NodeConfigFilterComparisonMethod.GT && compareResult > 0)
									|| 	(versionItem.getMethodType() == NodeConfigFilterComparisonMethod.LE && compareResult <= 0)
									|| 	(versionItem.getMethodType() == NodeConfigFilterComparisonMethod.LT && compareResult < 0)
									|| 	(versionItem.getMethodType() == NodeConfigFilterComparisonMethod.NE && compareResult != 0)) {
									// 条件に一致する場合
								} else {
									isTarget = false;
								}
							}
							if (releaseItem != null) {
								Integer compareResult = NodeConfigFilterUtil.compareVersion(historyDetail.getRelease(), (String)releaseItem.getItemValue());
								if((releaseItem.getMethodType() == NodeConfigFilterComparisonMethod.EQ && compareResult == 0)
									|| 	(releaseItem.getMethodType() == NodeConfigFilterComparisonMethod.GE && compareResult >= 0)
									|| 	(releaseItem.getMethodType() == NodeConfigFilterComparisonMethod.GT && compareResult > 0)
									|| 	(releaseItem.getMethodType() == NodeConfigFilterComparisonMethod.LE && compareResult <= 0)
									|| 	(releaseItem.getMethodType() == NodeConfigFilterComparisonMethod.LT && compareResult < 0)
									|| 	(releaseItem.getMethodType() == NodeConfigFilterComparisonMethod.NE && compareResult != 0)) {
									// 条件に一致する場合
								} else {
									isTarget = false;
								}
							}
							if (isTarget) {
								compareSet.add(historyDetail.getFacilityId());
							}
						}
					}
				}

				// Exists/Not Exists判定
				if (filterInfo.getExists()) {
					return new ArrayList<>(compareSet);
				} else {
					HashSet<String> rtnSet = new HashSet<>();
					rtnSet.addAll(allNodeFacilityIdList);
					rtnSet.removeAll(compareSet);
					return new ArrayList<>(rtnSet);
				}

			} else if (filterInfo.getNodeConfigSettingItem() == NodeConfigSettingItem.PRODUCT) {
				// 個別導入支援情報
				NodeConfigFilterItemInfo versionItem = null;
				if (isNow) {
					sbJpql.append(" SELECT a FROM NodeProductInfo a WHERE true = true");
				} else {
					sbJpql.append(" SELECT a FROM NodeProductHistoryDetail a WHERE true = true");
				}
				// 対象のファシリティIDを条件に追加
				sbJpql.append(" AND a.id.facilityId IN :facilityIds");
				parameters.put("facilityIds", allNodeFacilityIdList);

				for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
					if (itemInfo.getItem() == NodeConfigFilterItem.PRODUCT_NAME) {
						column = "a.id.productName";
					} else if (itemInfo.getItem() == NodeConfigFilterItem.PRODUCT_VERSION) {
						versionItem = itemInfo;
						continue;
					} else if (itemInfo.getItem() == NodeConfigFilterItem.PRODUCT_PATH) {
						column = "a.path";
					}
					if (itemInfo.getItem().dataType() == NodeConfigFilterDataType.STRING_ONLYEQUAL) { 
						if (itemInfo.getMethodType() == NodeConfigFilterComparisonMethod.EQ) {
							sbJpql.append(String.format(" AND %s like :%s", column, itemInfo.getItem().name()));
						} else if (itemInfo.getMethodType() == NodeConfigFilterComparisonMethod.NE) {
							sbJpql.append(String.format(" AND %s not like :%s", column, itemInfo.getItem().name()));
						}
					} else {
						sbJpql.append(String.format(" AND %s %s :%s", column, itemInfo.getMethodType().symbol(), itemInfo.getItem().name()));
					}
				}

				if (isNow) {
					for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
						if (itemInfo.getItem() == NodeConfigFilterItem.PRODUCT_VERSION) {
							continue;
						}
						if (itemInfo.getItem().dataType() == NodeConfigFilterDataType.STRING_ONLYEQUAL) { 
							parameters.put(itemInfo.getItem().name(),
									QueryDivergence.escapeConditionNodeMap((String)itemInfo.getItemValue()) + "%");
						} else {
							parameters.put(itemInfo.getItem().name(), itemInfo.getItemValue());
						}
					}

					List<NodeProductInfo> list = QueryExecutor.getListByJpqlWithTimeout(
							sbJpql.toString(), NodeProductInfo.class, parameters, searchTimeout);

					if (list.size() != 0) {
						for (NodeProductInfo info : list) {
							boolean isTarget = true;
							if (versionItem != null) {
								Integer compareResult = NodeConfigFilterUtil.compareVersion(info.getVersion(), (String)versionItem.getItemValue());
								if((versionItem.getMethodType() == NodeConfigFilterComparisonMethod.EQ && compareResult == 0)
									|| 	(versionItem.getMethodType() == NodeConfigFilterComparisonMethod.GE && compareResult >= 0)
									|| 	(versionItem.getMethodType() == NodeConfigFilterComparisonMethod.GT && compareResult > 0)
									|| 	(versionItem.getMethodType() == NodeConfigFilterComparisonMethod.LE && compareResult <= 0)
									|| 	(versionItem.getMethodType() == NodeConfigFilterComparisonMethod.LT && compareResult < 0)
									|| 	(versionItem.getMethodType() == NodeConfigFilterComparisonMethod.NE && compareResult != 0)) {
									// 条件に一致する場合
								} else {
									isTarget = false;
								}
							}
							if (isTarget) {
								compareSet.add(info.getFacilityId());
							}
						}
					}
				} else {
					sbJpql.append(String.format(" AND a.id.regDate <= :regDate"));
					sbJpql.append(String.format(" AND a.regDateTo > :regDateTo"));

					for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
						if (itemInfo.getItem() == NodeConfigFilterItem.PRODUCT_VERSION) {
							continue;
						}
						if (itemInfo.getItem().dataType() == NodeConfigFilterDataType.STRING_ONLYEQUAL) { 
							parameters.put(itemInfo.getItem().name(),
									QueryDivergence.escapeConditionNodeMap((String)itemInfo.getItemValue()) + "%");
						} else {
							parameters.put(itemInfo.getItem().name(), itemInfo.getItemValue());
						}

					}
					parameters.put("regDate", targetDatetime);
					parameters.put("regDateTo", targetDatetime);

					List<NodeProductHistoryDetail> list = QueryExecutor.getListByJpqlWithTimeout(
							sbJpql.toString(), NodeProductHistoryDetail.class, parameters, searchTimeout);

					if (list.size() != 0) {
						for (NodeProductHistoryDetail historyDetail : list) {
							boolean isTarget = true;
							if (versionItem != null) {
								Integer compareResult = NodeConfigFilterUtil.compareVersion(historyDetail.getVersion(), (String)versionItem.getItemValue());
								if((versionItem.getMethodType() == NodeConfigFilterComparisonMethod.EQ && compareResult == 0)
									|| 	(versionItem.getMethodType() == NodeConfigFilterComparisonMethod.GE && compareResult >= 0)
									|| 	(versionItem.getMethodType() == NodeConfigFilterComparisonMethod.GT && compareResult > 0)
									|| 	(versionItem.getMethodType() == NodeConfigFilterComparisonMethod.LE && compareResult <= 0)
									|| 	(versionItem.getMethodType() == NodeConfigFilterComparisonMethod.LT && compareResult < 0)
									|| 	(versionItem.getMethodType() == NodeConfigFilterComparisonMethod.NE && compareResult != 0)) {
									// 条件に一致する場合
								} else {
									isTarget = false;
								}
							}
							if (isTarget) {
								compareSet.add(historyDetail.getFacilityId());
							}
						}
					}
				}

				// Exists/Not Exists判定
				if (filterInfo.getExists()) {
					return new ArrayList<>(compareSet);
				} else {
					HashSet<String> rtnSet = new HashSet<>();
					rtnSet.addAll(allNodeFacilityIdList);
					rtnSet.removeAll(compareSet);
					return new ArrayList<>(rtnSet);
				}

			} else if (filterInfo.getNodeConfigSettingItem() == NodeConfigSettingItem.PROCESS) {
				// プロセス情報
				sbJpql.append("SELECT b.facilityId FROM NodeInfo b WHERE true = true");

				// 対象のファシリティIDを条件に追加
				sbJpql.append(" AND b.facilityId IN :facilityIds");
				parameters.put("facilityIds", allNodeFacilityIdList);

				if (filterInfo.getExists()) {
					sbJpql.append(" AND EXISTS (");
				} else {
					sbJpql.append(" AND NOT EXISTS (");
				}
				sbJpql.append(" SELECT a FROM NodeProcessInfo a WHERE true = true");
				for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
					if (itemInfo.getItem() == NodeConfigFilterItem.PROCESS_NAME) {
						column = "a.id.processName";
					} else if (itemInfo.getItem() == NodeConfigFilterItem.PROCESS_PID) {
						column = "a.id.pid";
					} else if (itemInfo.getItem() == NodeConfigFilterItem.PROCESS_PATH) {
						column = "a.path";
					} else if (itemInfo.getItem() == NodeConfigFilterItem.PROCESS_EXEC_USER) {
						column = "a.execUser";
					} else if (itemInfo.getItem() == NodeConfigFilterItem.PROCESS_STARTUP_DATE_TIME) {
						column = "a.startupDateTime";
					}
					if (itemInfo.getItem().dataType() == NodeConfigFilterDataType.STRING_ONLYEQUAL) { 
						if (itemInfo.getMethodType() == NodeConfigFilterComparisonMethod.EQ) {
							sbJpql.append(String.format(" AND %s like :%s", column, itemInfo.getItem().name()));
						} else if (itemInfo.getMethodType() == NodeConfigFilterComparisonMethod.NE) {
							sbJpql.append(String.format(" AND %s not like :%s", column, itemInfo.getItem().name()));
						}
					} else {
						sbJpql.append(String.format(" AND %s %s :%s", column, itemInfo.getMethodType().symbol(), itemInfo.getItem().name()));
					}
				}
				sbJpql.append(" AND a.id.facilityId = b.facilityId)");

				for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
					if (itemInfo.getItem().dataType() == NodeConfigFilterDataType.STRING_ONLYEQUAL) { 
						parameters.put(itemInfo.getItem().name(),
								QueryDivergence.escapeConditionNodeMap((String)itemInfo.getItemValue()) + "%");
					} else {
						parameters.put(itemInfo.getItem().name(), itemInfo.getItemValue());
					}

				}
				rtnList = QueryExecutor.getListByJpqlWithTimeout(sbJpql.toString(), String.class, parameters, searchTimeout);

				// 処理終了
				return rtnList;
			} else {

				// バージョンチェック無
				sbJpql.append("SELECT b.facilityId FROM NodeInfo b WHERE true = true");
				if (filterInfo.getExists()) {
					sbJpql.append(" AND EXISTS (");
				} else {
					sbJpql.append(" AND NOT EXISTS (");
				}
				if (filterInfo.getNodeConfigSettingItem() == NodeConfigSettingItem.OS) {
					// OS情報
					if (isNow) {
						sbJpql.append("SELECT a FROM NodeOsInfo a WHERE true = true");
						// 対象のファシリティIDを条件に追加
						sbJpql.append(" AND a.facilityId IN :facilityIds");
					} else {
						sbJpql.append("SELECT a FROM NodeOsHistoryDetail a WHERE true = true");
						// 対象のファシリティIDを条件に追加
						sbJpql.append(" AND a.id.facilityId IN :facilityIds");
					}

					// 対象のファシリティIDを条件に追加
					parameters.put("facilityIds", allNodeFacilityIdList);

					for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
						if (itemInfo.getItem() == NodeConfigFilterItem.OS_NAME) {
							column = "a.osName";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.OS_RELEASE) {
							column = "a.osRelease";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.OS_VERSION) {
							column = "a.osVersion";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.OS_CHARACTER_SET) {
							column = "a.characterSet";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.OS_STARTUP_DATE_TIME) {
							column = "a.startupDateTime";
						}
						if (itemInfo.getItem().dataType() == NodeConfigFilterDataType.STRING_ONLYEQUAL) { 
							if (itemInfo.getMethodType() == NodeConfigFilterComparisonMethod.EQ) {
								sbJpql.append(String.format(" AND %s like :%s", column, itemInfo.getItem().name()));
							} else if (itemInfo.getMethodType() == NodeConfigFilterComparisonMethod.NE) {
								sbJpql.append(String.format(" AND %s not like :%s", column, itemInfo.getItem().name()));
							}
						} else {
							sbJpql.append(String.format(" AND %s %s :%s", column, itemInfo.getMethodType().symbol(), itemInfo.getItem().name()));
						}
					}
				} else if (filterInfo.getNodeConfigSettingItem() == NodeConfigSettingItem.HW_CPU) {
					// HW情報 - CPU情報
					if (isNow) {
						sbJpql.append("SELECT a FROM NodeCpuInfo a WHERE true = true");
					} else {
						sbJpql.append("SELECT a FROM NodeCpuHistoryDetail a WHERE true = true");
					}

					// 対象のファシリティIDを条件に追加
					sbJpql.append(" AND a.id.facilityId IN :facilityIds");
					parameters.put("facilityIds", allNodeFacilityIdList);

					for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
						if (itemInfo.getItem() == NodeConfigFilterItem.CPU_DEVICE_NAME) {
							column = "a.id.deviceName";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.CPU_DEVICE_DISPLAY_NAME) {
							column = "a.deviceDisplayName";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.CPU_DEVICE_SIZE) {
							column = "a.deviceSize";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.CPU_DEVICE_SIZE_UNIT) {
							column = "a.deviceSizeUnit";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.CPU_DEVICE_DESCRIPTION) {
							column = "a.deviceDescription";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.CPU_CORE_COUNT) {
							column = "a.coreCount";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.CPU_THREAD_COUNT) {
							column = "a.threadCount";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.CPU_CLOCK_COUNT) {
							column = "a.clockCount";
						}
						if (itemInfo.getItem().dataType() == NodeConfigFilterDataType.STRING_ONLYEQUAL) { 
							if (itemInfo.getMethodType() == NodeConfigFilterComparisonMethod.EQ) {
								sbJpql.append(String.format(" AND %s like :%s", column, itemInfo.getItem().name()));
							} else if (itemInfo.getMethodType() == NodeConfigFilterComparisonMethod.NE) {
								sbJpql.append(String.format(" AND %s not like :%s", column, itemInfo.getItem().name()));
							}
						} else {
							sbJpql.append(String.format(" AND %s %s :%s", column, itemInfo.getMethodType().symbol(), itemInfo.getItem().name()));
						}
					}
				} else if (filterInfo.getNodeConfigSettingItem() == NodeConfigSettingItem.HW_MEMORY) {
					// HW情報 - メモリ情報
					if (isNow) {
						sbJpql.append("SELECT a.id.facilityId FROM NodeMemoryInfo a WHERE true = true");
					} else {
						sbJpql.append("SELECT a.id.facilityId FROM NodeMemoryHistoryDetail a WHERE true = true");
					}

					// 対象のファシリティIDを条件に追加
					sbJpql.append(" AND a.id.facilityId IN :facilityIds");
					parameters.put("facilityIds", allNodeFacilityIdList);

					for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
						if (itemInfo.getItem() == NodeConfigFilterItem.MEMORY_DEVICE_NAME) {
							column = "a.id.deviceName";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.MEMORY_DEVICE_DISPLAY_NAME) {
							column = "a.deviceDisplayName";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.MEMORY_DEVICE_SIZE) {
							column = "a.deviceSize";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.MEMORY_DEVICE_SIZE_UNIT) {
							column = "a.deviceSizeUnit";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.MEMORY_DEVICE_DESCRIPTION) {
							column = "a.deviceDescription";
						}
						if (itemInfo.getItem().dataType() == NodeConfigFilterDataType.STRING_ONLYEQUAL) { 
							if (itemInfo.getMethodType() == NodeConfigFilterComparisonMethod.EQ) {
								sbJpql.append(String.format(" AND %s like :%s", column, itemInfo.getItem().name()));
							} else if (itemInfo.getMethodType() == NodeConfigFilterComparisonMethod.NE) {
								sbJpql.append(String.format(" AND %s not like :%s", column, itemInfo.getItem().name()));
							}
						} else {
							sbJpql.append(String.format(" AND %s %s :%s", column, itemInfo.getMethodType().symbol(), itemInfo.getItem().name()));
						}
					}
				} else if (filterInfo.getNodeConfigSettingItem() == NodeConfigSettingItem.HW_NIC) {
					// HW情報 - NIC情報
					if (isNow) {
						sbJpql.append("SELECT a.id.facilityId FROM NodeNetworkInterfaceInfo a WHERE true = true");
					} else {
						sbJpql.append("SELECT a.id.facilityId FROM NodeNetworkInterfaceHistoryDetail a WHERE true = true");
					}

					// 対象のファシリティIDを条件に追加
					sbJpql.append(" AND a.id.facilityId IN :facilityIds");
					parameters.put("facilityIds", allNodeFacilityIdList);

					for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
						if (itemInfo.getItem() == NodeConfigFilterItem.NIC_DEVICE_NAME) {
							column = "a.id.deviceName";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.NIC_DEVICE_DISPLAY_NAME) {
							column = "a.deviceDisplayName";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.NIC_DEVICE_SIZE) {
							column = "a.deviceSize";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.NIC_DEVICE_SIZE_UNIT) {
						column = "a.deviceSizeUnit";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.NIC_DEVICE_DESCRIPTION) {
							column = "a.deviceDescription";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.NIC_IP_ADDRESS) {
							column = "a.nicIpAddress";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.NIC_MAC_ADDRESS) {
							column = "a.nicMacAddress";
						}
						if (itemInfo.getItem().dataType() == NodeConfigFilterDataType.STRING_ONLYEQUAL) { 
							if (itemInfo.getMethodType() == NodeConfigFilterComparisonMethod.EQ) {
								sbJpql.append(String.format(" AND %s like :%s", column, itemInfo.getItem().name()));
							} else if (itemInfo.getMethodType() == NodeConfigFilterComparisonMethod.NE) {
								sbJpql.append(String.format(" AND %s not like :%s", column, itemInfo.getItem().name()));
							}
						} else {
							sbJpql.append(String.format(" AND %s %s :%s", column, itemInfo.getMethodType().symbol(), itemInfo.getItem().name()));
						}
					}
				} else if (filterInfo.getNodeConfigSettingItem() == NodeConfigSettingItem.HW_DISK) {
					// HW情報 - ディスク情報
					if (isNow) {
						sbJpql.append("SELECT a.id.facilityId FROM NodeDiskInfo a WHERE true = true");
					} else {
						sbJpql.append("SELECT a.id.facilityId FROM NodeDiskHistoryDetail a WHERE true = true");
					}

					// 対象のファシリティIDを条件に追加
					sbJpql.append(" AND a.id.facilityId IN :facilityIds");
					parameters.put("facilityIds", allNodeFacilityIdList);

					for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
						if (itemInfo.getItem() == NodeConfigFilterItem.DISK_DEVICE_NAME) {
							column = "a.id.deviceName";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.DISK_DEVICE_DISPLAY_NAME) {
							column = "a.deviceDisplayName";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.DISK_DEVICE_SIZE) {
							column = "a.deviceSize";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.DISK_DEVICE_SIZE_UNIT) {
							column = "a.deviceSizeUnit";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.DISK_DEVICE_DESCRIPTION) {
							column = "a.deviceDescription";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.DISK_RPM) {
							column = "a.diskRpm";
						}
						if (itemInfo.getItem().dataType() == NodeConfigFilterDataType.STRING_ONLYEQUAL) { 
							if (itemInfo.getMethodType() == NodeConfigFilterComparisonMethod.EQ) {
								sbJpql.append(String.format(" AND %s like :%s", column, itemInfo.getItem().name()));
							} else if (itemInfo.getMethodType() == NodeConfigFilterComparisonMethod.NE) {
								sbJpql.append(String.format(" AND %s not like :%s", column, itemInfo.getItem().name()));
							}
						} else {
							sbJpql.append(String.format(" AND %s %s :%s", column, itemInfo.getMethodType().symbol(), itemInfo.getItem().name()));
						}
					}
				} else if (filterInfo.getNodeConfigSettingItem() == NodeConfigSettingItem.HW_FILESYSTEM) {
					// HW情報 - ファイルシステム情報
					if (isNow) {
						sbJpql.append("SELECT a.id.facilityId FROM NodeFilesystemInfo a WHERE true = true");
					} else {
						sbJpql.append("SELECT a.id.facilityId FROM NodeFilesystemHistoryDetail a WHERE true = true");
					}

					// 対象のファシリティIDを条件に追加
					sbJpql.append(" AND a.id.facilityId IN :facilityIds");
					parameters.put("facilityIds", allNodeFacilityIdList);

					for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
						if (itemInfo.getItem() == NodeConfigFilterItem.FILESYSTEM_DEVICE_NAME) {
							column = "a.id.deviceName";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.FILESYSTEM_DEVICE_DISPLAY_NAME) {
							column = "a.deviceDisplayName";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.FILESYSTEM_DEVICE_SIZE) {
							column = "a.deviceSize";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.FILESYSTEM_DEVICE_SIZE_UNIT) {
							column = "a.deviceSizeUnit";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.FILESYSTEM_DEVICE_DESCRIPTION) {
							column = "a.deviceDescription";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.FILESYSTEM_TYPE) {
							column = "a.filesystemType";
						}
						if (itemInfo.getItem().dataType() == NodeConfigFilterDataType.STRING_ONLYEQUAL) { 
							if (itemInfo.getMethodType() == NodeConfigFilterComparisonMethod.EQ) {
								sbJpql.append(String.format(" AND %s like :%s", column, itemInfo.getItem().name()));
							} else if (itemInfo.getMethodType() == NodeConfigFilterComparisonMethod.NE) {
								sbJpql.append(String.format(" AND %s not like :%s", column, itemInfo.getItem().name()));
							}
						} else {
							sbJpql.append(String.format(" AND %s %s :%s", column, itemInfo.getMethodType().symbol(), itemInfo.getItem().name()));
						}
					}
				} else if (filterInfo.getNodeConfigSettingItem() == NodeConfigSettingItem.HOSTNAME) {
					// HW情報 - ホスト名情報
					if (isNow) {
						sbJpql.append("SELECT a.id.facilityId FROM NodeHostnameInfo a WHERE true = true");
					} else {
						sbJpql.append("SELECT a.id.facilityId FROM NodeHostnameHistoryDetail a WHERE true = true");
					}

					// 対象のファシリティIDを条件に追加
					sbJpql.append(" AND a.id.facilityId IN :facilityIds");
					parameters.put("facilityIds", allNodeFacilityIdList);

					for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
						if (itemInfo.getItem() == NodeConfigFilterItem.HOSTNAME) {
							column = "a.id.hostname";
						}
						if (itemInfo.getItem().dataType() == NodeConfigFilterDataType.STRING_ONLYEQUAL) { 
							if (itemInfo.getMethodType() == NodeConfigFilterComparisonMethod.EQ) {
								sbJpql.append(String.format(" AND %s like :%s", column, itemInfo.getItem().name()));
							} else if (itemInfo.getMethodType() == NodeConfigFilterComparisonMethod.NE) {
								sbJpql.append(String.format(" AND %s not like :%s", column, itemInfo.getItem().name()));
							}
						} else {
							sbJpql.append(String.format(" AND %s %s :%s", column, itemInfo.getMethodType().symbol(), itemInfo.getItem().name()));
						}
					}
				} else if (filterInfo.getNodeConfigSettingItem() == NodeConfigSettingItem.NODE_VARIABLE) {
					// HW情報 - ノード変数情報
					if (isNow) {
						sbJpql.append("SELECT a.id.facilityId FROM NodeVariableInfo a WHERE true = true");
					} else {
						sbJpql.append("SELECT a.id.facilityId FROM NodeVariableHistoryDetail a WHERE true = true");
					}

					// 対象のファシリティIDを条件に追加
					sbJpql.append(" AND a.id.facilityId IN :facilityIds");
					parameters.put("facilityIds", allNodeFacilityIdList);

					for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
						if (itemInfo.getItem() == NodeConfigFilterItem.NODE_VARIABLE_NAME) {
							column = "a.id.nodeVariableName";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.NODE_VARIABLE_VALUE) {
							column = "a.nodeVariableValue";
						}
	
						if (itemInfo.getItem().dataType() == NodeConfigFilterDataType.STRING_ONLYEQUAL) { 
							if (itemInfo.getMethodType() == NodeConfigFilterComparisonMethod.EQ) {
								sbJpql.append(String.format(" AND %s like :%s", column, itemInfo.getItem().name()));
							} else if (itemInfo.getMethodType() == NodeConfigFilterComparisonMethod.NE) {
								sbJpql.append(String.format(" AND %s not like :%s", column, itemInfo.getItem().name()));
							}
						} else {
							sbJpql.append(String.format(" AND %s %s :%s", column, itemInfo.getMethodType().symbol(), itemInfo.getItem().name()));
						}
					}
				} else if (filterInfo.getNodeConfigSettingItem() == NodeConfigSettingItem.NETSTAT) {
					// ネットワーク接続情報
					if (isNow) {
						sbJpql.append("SELECT a.id.facilityId FROM NodeNetstatInfo a WHERE true = true");
					} else {
						sbJpql.append("SELECT a.id.facilityId FROM NodeNetstatHistoryDetail a WHERE true = true");
					}

					// 対象のファシリティIDを条件に追加
					sbJpql.append(" AND a.id.facilityId IN :facilityIds");
					parameters.put("facilityIds", allNodeFacilityIdList);

					for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
						if (itemInfo.getItem() == NodeConfigFilterItem.NETSTAT_PROTOCOL) {
							column = "a.id.protocol";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.NETSTAT_LOCAL_IP_ADDRESS) {
							column = "a.id.localIpAddress";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.NETSTAT_LOCAL_PORT) {
							column = "a.id.localPort";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.NETSTAT_FOREIGN_IP_ADDRESS) {
							column = "a.id.foreignIpAddress";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.NETSTAT_FOREIGN_PORT) {
							column = "a.id.foreignPort";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.NETSTAT_PROCESS_NAME) {
							column = "a.id.processName";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.NETSTAT_PID) {
							column = "a.id.pid";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.NETSTAT_STATUS) {
							column = "a.status";
						}
						if (itemInfo.getItem().dataType() == NodeConfigFilterDataType.STRING_ONLYEQUAL) { 
							if (itemInfo.getMethodType() == NodeConfigFilterComparisonMethod.EQ) {
								sbJpql.append(String.format(" AND %s like :%s", column, itemInfo.getItem().name()));
							} else if (itemInfo.getMethodType() == NodeConfigFilterComparisonMethod.NE) {
								sbJpql.append(String.format(" AND %s not like :%s", column, itemInfo.getItem().name()));
							}
						} else {
							sbJpql.append(String.format(" AND %s %s :%s", column, itemInfo.getMethodType().symbol(), itemInfo.getItem().name()));
						}
					}
				} else if (filterInfo.getNodeConfigSettingItem() == NodeConfigSettingItem.LICENSE) {
					// ライセンス情報
					if (isNow) {
						sbJpql.append("SELECT a.id.facilityId FROM NodeLicenseInfo a WHERE true = true");
					} else {
						sbJpql.append("SELECT a.id.facilityId FROM NodeLicenseHistoryDetail a WHERE true = true");
					}

					// 対象のファシリティIDを条件に追加
					sbJpql.append(" AND a.id.facilityId IN :facilityIds");
					parameters.put("facilityIds", allNodeFacilityIdList);

					for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
						if (itemInfo.getItem() == NodeConfigFilterItem.LICENSE_PRODUCT_NAME) {
							column = "a.id.productName";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.LICENSE_VENDOR) {
							column = "a.vendor";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.LICENSE_VENDOR_CONTACT) {
							column = "a.vendorContact";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.LICENSE_SERIAL_NUMBER) {
							column = "a.serialNumber";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.LICENSE_COUNT) {
							column = "a.count";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.LICENSE_EXPIRATION_DATE) {
							column = "a.expirationDate";
						}
						if (itemInfo.getItem().dataType() == NodeConfigFilterDataType.STRING_ONLYEQUAL) { 
							if (itemInfo.getMethodType() == NodeConfigFilterComparisonMethod.EQ) {
								sbJpql.append(String.format(" AND %s like :%s", column, itemInfo.getItem().name()));
							} else if (itemInfo.getMethodType() == NodeConfigFilterComparisonMethod.NE) {
								sbJpql.append(String.format(" AND %s not like :%s", column, itemInfo.getItem().name()));
							}
						} else {
							sbJpql.append(String.format(" AND %s %s :%s", column, itemInfo.getMethodType().symbol(), itemInfo.getItem().name()));
						}
					}
				} else if (filterInfo.getNodeConfigSettingItem() == NodeConfigSettingItem.CUSTOM) {
					// ユーザ任意情報
					if (isNow) {
						sbJpql.append("SELECT a.id.facilityId FROM NodeCustomInfo a WHERE true = true");
					} else {
						sbJpql.append("SELECT a.id.facilityId FROM NodeCustomHistoryDetail a WHERE true = true");
					}

					// 対象のファシリティIDを条件に追加
					sbJpql.append(" AND a.id.facilityId IN :facilityIds");
					parameters.put("facilityIds", allNodeFacilityIdList);

					for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
						if (itemInfo.getItem() == NodeConfigFilterItem.CUSTOM_DISPLAY_NAME) {
							column = "a.displayName";
						} else if (itemInfo.getItem() == NodeConfigFilterItem.CUSTOM_VALUE) {
							column = "a.value";
						}
						if (itemInfo.getItem().dataType() == NodeConfigFilterDataType.STRING_ONLYEQUAL) { 
							if (itemInfo.getMethodType() == NodeConfigFilterComparisonMethod.EQ) {
								sbJpql.append(String.format(" AND %s like :%s", column, itemInfo.getItem().name()));
							} else if (itemInfo.getMethodType() == NodeConfigFilterComparisonMethod.NE) {
								sbJpql.append(String.format(" AND %s not like :%s", column, itemInfo.getItem().name()));
							}
						} else {
							sbJpql.append(String.format(" AND %s %s :%s", column, itemInfo.getMethodType().symbol(), itemInfo.getItem().name()));
						}
					}
				}

				if (!isNow){
					sbJpql.append(String.format(" AND a.id.regDate <= :regDate"));
					sbJpql.append(String.format(" AND a.regDateTo > :regDateTo"));
				}
				if (filterInfo.getNodeConfigSettingItem() == NodeConfigSettingItem.OS && isNow) {
					sbJpql.append(" AND a.facilityId = b.facilityId)");
				} else {
					sbJpql.append(" AND a.id.facilityId = b.facilityId)");
				}

				for (NodeConfigFilterItemInfo itemInfo : filterInfo.getItemList()) {
					if (itemInfo.getItem().dataType() == NodeConfigFilterDataType.STRING_ONLYEQUAL) { 
						parameters.put(itemInfo.getItem().name(),
								QueryDivergence.escapeConditionNodeMap((String)itemInfo.getItemValue()) + "%");
					} else {
						parameters.put(itemInfo.getItem().name(), itemInfo.getItemValue());
					}
				}
				if (!isNow){
					parameters.put("regDate", targetDatetime);
					parameters.put("regDateTo", targetDatetime);
				}
				rtnList = QueryExecutor.getListByJpqlWithTimeout(sbJpql.toString(), String.class, parameters, searchTimeout);
			}

		} catch (RuntimeException e) {
			m_log.warn("getNodeFacilityIdByNodeConfig() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		m_log.debug("getNodeFacilityIdByNodeConfig() : end");
		return rtnList;
	}
	
	public static List<FacilityInfo> getAllFacility() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<FacilityInfo> resultList
			= em.createNamedQuery_OR("FacilityEntity.findAll", FacilityInfo.class, ObjectPrivilegeMode.READ, null)
			.getResultList();
			return resultList;
		}
	}
	
	/**
	 * UnixTime(ミリ秒)のリストから、Date型の日付重複のないリストを取得する
	 * 
	 * @param unixTimeList
	 * @return 処理対象となる日付のリスト
	 */
	private static List<Date> getTargetDateListByUnixTimeLsit(List<Long> unixTimeList){
		List<Date> ret = new ArrayList<Date>();
		Calendar calendar = Calendar.getInstance();
		for(long unixTime : unixTimeList){
			calendar.setTimeInMillis(unixTime);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			Date date = new Date(calendar.getTime().getTime());
			if(!ret.contains(date)){
				ret.add(date);
			}
		}
		return ret;
	}

	/**
	 * 削除対象の日付(Date)をDBに合わせてUnixTime(ミリ秒)に変換する
	 * ※その際、削除は渡したUnixTime未満で行われるため、削除対象の日付を削除するために＋1日してからUnixTimeに変換する
	 * 
	 * @param targetDate 削除対象日付
	 * @return 削除対象日付＋1日のUnixTime
	 */
	private static long parseTargetDateToTargetUnixTime(Date targetDate){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(targetDate);
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		return calendar.getTimeInMillis();
	}
}
