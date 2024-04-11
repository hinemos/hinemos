/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.FunctionPrefixEnum;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.CommandTemplateNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.notify.model.CommandTemplateInfo;
import com.clustercontrol.notify.model.MonitorStatusEntity;
import com.clustercontrol.notify.model.MonitorStatusEntityPK;
import com.clustercontrol.notify.model.NotifyCloudInfo;
import com.clustercontrol.notify.model.NotifyCommandInfo;
import com.clustercontrol.notify.model.NotifyEventInfo;
import com.clustercontrol.notify.model.NotifyHistoryEntity;
import com.clustercontrol.notify.model.NotifyHistoryEntityPK;
import com.clustercontrol.notify.model.NotifyInfo;
import com.clustercontrol.notify.model.NotifyInfraInfo;
import com.clustercontrol.notify.model.NotifyJobInfo;
import com.clustercontrol.notify.model.NotifyLogEscalateInfo;
import com.clustercontrol.notify.model.NotifyMailInfo;
import com.clustercontrol.notify.model.NotifyMessageInfo;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.notify.model.NotifyRestInfo;
import com.clustercontrol.notify.model.NotifyStatusInfo;
import com.clustercontrol.notify.monitor.model.StatusInfoEntity;

import jakarta.persistence.NoResultException;

public class QueryUtil {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( QueryUtil.class );

	public static NotifyInfo getNotifyInfoPK(String notifyId) throws NotifyNotFound, InvalidRole {
		return getNotifyInfoPK(notifyId, ObjectPrivilegeMode.READ);
	}

	public static NotifyInfo getNotifyInfoPK(String notifyId, ObjectPrivilegeMode mode) throws NotifyNotFound, InvalidRole {
		NotifyInfo entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find(NotifyInfo.class, notifyId, mode);
			if (entity == null) {
				NotifyNotFound e = new NotifyNotFound("NotifyInfoEntity.findByPrimaryKey"
						+ "notifyId = " + notifyId);
				m_log.info("getNotifyInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getNotifyInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static NotifyInfo getNotifyInfoPK_OR(String notifyId, String ownerRoleId) throws NotifyNotFound, InvalidRole {
		NotifyInfo entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find_OR(NotifyInfo.class, notifyId, ownerRoleId);
			if (entity == null) {
				NotifyNotFound e = new NotifyNotFound("NotifyInfoEntity.findByPrimaryKey"
						+ "notifyId = " + notifyId);
				m_log.info("getNotifyInfoPK_OR() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getNotifyInfoPK_OR() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static NotifyInfo getNotifyInfoFindByNotifyIdAndNotifyType(String notifyId, Integer notifyType) throws NotifyNotFound, InvalidRole {
		NotifyInfo entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.createNamedQuery("NotifyInfoEntity.findByNotifyIdAndNotifyType", NotifyInfo.class)
					.setParameter("notifyId", notifyId)
					.setParameter("notifyType", notifyType)
					.getSingleResult();
		} catch (NoResultException e) {
			NotifyNotFound e1 = new NotifyNotFound("NotifyInfoEntity.findByNotifyIdAndNotifyType"
					+ "notifyId = " + notifyId + ", notifyType = " + notifyType);
			m_log.info("getNotifyInfoFindByNotifyIdAndNotifyType() : "
					+ e1.getClass().getSimpleName() + ", " + e1.getMessage());
			throw e1;
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getNotifyInfoFindByNotifyIdAndNotifyType() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static List<NotifyInfo> getNotifyInfoFindByNotifyType( Integer notifyType) throws NotifyNotFound, InvalidRole {
		List<NotifyInfo> list =null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			list = em.createNamedQuery("NotifyInfoEntity.findByNotifyType", NotifyInfo.class)
					.setParameter("notifyType", notifyType)
					.getResultList();
		}
		return list;
	}

	public static List<NotifyInfo> getNotifyInfoFindByNotifyType_OR( Integer notifyType, String ownerRoleId ) throws NotifyNotFound, InvalidRole {
		List<NotifyInfo> list =null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			list = em.createNamedQuery_OR("NotifyInfoEntity.findByNotifyType", NotifyInfo.class,ownerRoleId)
					.setParameter("notifyType", notifyType)
					.getResultList();
		}
		return list;
	}

	public static List<NotifyInfo> getAllNotifyInfo() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NotifyInfo> list
			= em.createNamedQuery("NotifyInfoEntity.findAll", NotifyInfo.class).getResultList();
			return list;
		}
	}

	/**
	 *  オブジェクト権限チェックなし
	 * @return
	 */
	public static List<NotifyInfo> getAllNotifyInfo_NONE() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NotifyInfo> list
			= em.createNamedQuery("NotifyInfoEntity.findAll", NotifyInfo.class, ObjectPrivilegeMode.NONE).getResultList();
			return list;
		}
	}

	public static List<NotifyInfo> getAllNotifyInfoOrderByNotifyId() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NotifyInfo> list
			= em.createNamedQuery("NotifyInfoEntity.findAllOrderByNotifyId", NotifyInfo.class).getResultList();
			return list;
		}
	}

	public static List<NotifyInfo> getNotifyInfoByNotifyTypeOrderByNotifyId(Integer notifyType) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NotifyInfo> list
			= em.createNamedQuery("NotifyInfoEntity.findByNotifyTypeOrderByNotifyId", NotifyInfo.class)
				.setParameter("notifyType", notifyType)
				.getResultList();
			return list;
		}
	}
	
	public static List<NotifyInfo> getAllNotifyInfoOrderByNotifyId_OR(String ownerRoleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NotifyInfo> list
			= em
			.createNamedQuery_OR("NotifyInfoEntity.findAllOrderByNotifyId", NotifyInfo.class, ownerRoleId)
			.getResultList();
			return list;
		}
	}

	public static List<NotifyInfo> getNotifyInfoByNotifyTypeOrderByNotifyId_OR(String ownerRoleId, Integer notifyType) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NotifyInfo> list
			= em
			.createNamedQuery_OR("NotifyInfoEntity.findByNotifyTypeOrderByNotifyId", NotifyInfo.class, ownerRoleId)
			.setParameter("notifyType", notifyType)
			.getResultList();
			return list;
		}
	}
	
	public static List<NotifyInfo> getNotifyInfoFindByOwnerRoleId_NONE(String roleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NotifyInfo> list
			= em.createNamedQuery("NotifyInfoEntity.findByOwnerRoleId", NotifyInfo.class, ObjectPrivilegeMode.NONE)
			.setParameter("ownerRoleId", roleId)
			.getResultList();
			return list;
		}
	}
	
	public static List<NotifyInfo> getNotifyInfoFindByCalendarId_NONE(String calendarId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NotifyInfo> list
			= em.createNamedQuery("NotifyInfoEntity.findByCalendarId", NotifyInfo.class, ObjectPrivilegeMode.NONE)
			.setParameter("calendarId", calendarId)
			.getResultList();
			return list;
		}
	}

	public static NotifyCommandInfo getNotifyCommandInfoPK(String pk) throws NotifyNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NotifyCommandInfo entity = em.find(NotifyCommandInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				NotifyNotFound e = new NotifyNotFound("NotifyCommandInfoEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.info("getNotifyCommandInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static NotifyEventInfo getNotifyEventInfoPK(String pk) throws NotifyNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NotifyEventInfo entity = em.find(NotifyEventInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				NotifyNotFound e = new NotifyNotFound("NotifyEventInfoEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.info("getNotifyEventInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static NotifyJobInfo getNotifyJobInfoPK(String pk) throws NotifyNotFound {
		NotifyJobInfo entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			entity = em.find(NotifyJobInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				NotifyNotFound e = new NotifyNotFound("NotifyJobInfoEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.info("getNotifyJobInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}
		return entity;
	}

	public static List<NotifyJobInfo> getNotifyJobInfoByJobExecFacilityId(String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NotifyJobInfo> list
			= em.createNamedQuery("NotifyJobInfoEntity.findByJobExecFacilityId", NotifyJobInfo.class)
			.setParameter("facilityId", facilityId)
			.getResultList();
			return list;
		}
	}

	public static List<NotifyJobInfo> getNotifyJobInfoByNotifyJobType_NONE(Integer notifyJobType) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NotifyJobInfo> list = em.createNamedQuery("NotifyJobInfoEntity.findByNotifyJobType",
					NotifyJobInfo.class, ObjectPrivilegeMode.NONE).setParameter("notifyJobType", notifyJobType)
					.getResultList();
			return list;
		}
	}

	public static NotifyLogEscalateInfo getNotifyLogEscalateInfoPK(String pk) throws NotifyNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NotifyLogEscalateInfo entity = em.find(NotifyLogEscalateInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				NotifyNotFound e = new NotifyNotFound("NotifyLogEscalateInfoEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.info("getNotifyLogEscalateInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static List<NotifyLogEscalateInfo> getNotifyLogEscalateInfoByEscalateFacilityId(String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NotifyLogEscalateInfo> list
			= em.createNamedQuery("NotifyLogEscalateInfoEntity.findByEscalateFacilityId", NotifyLogEscalateInfo.class)
			.setParameter("facilityId", facilityId)
			.getResultList();
			return list;
		}
	}

	public static NotifyMailInfo getNotifyMailInfoPK(String pk) throws NotifyNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NotifyMailInfo entity = em.find(NotifyMailInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				NotifyNotFound e = new NotifyNotFound("NotifyMailInfoEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.info("getNotifyMailInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static NotifyStatusInfo getNotifyStatusInfoPK(String pk) throws NotifyNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NotifyStatusInfo entity = em.find(NotifyStatusInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				NotifyNotFound e = new NotifyNotFound("NotifyStatusInfoEntity.findByPrimaryKey"
						+ pk.toString());
				// 対応する通知設定が存在しない（Internalメッセージ）場合にも呼ばれるのでdebugとする
				m_log.debug("getNotifyStatusInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}
	
	public static NotifyInfraInfo getNotifyInfraInfoPK(String pk) throws NotifyNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NotifyInfraInfo entity = em.find(NotifyInfraInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				NotifyNotFound e = new NotifyNotFound("NotifyInfraInfoEntity.findByPrimaryKey"
						+ pk.toString());
				// 対応する通知設定が存在しない（Internalメッセージ）場合にも呼ばれるのでdebugとする
				m_log.debug("getNotifyInfraInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static NotifyMessageInfo getNotifyMessageInfoPK(String pk) throws NotifyNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NotifyMessageInfo entity = em.find(NotifyMessageInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				NotifyNotFound e = new NotifyNotFound("NotifyMessageInfo.findByPrimaryKey"
						+ pk.toString());
				// 対応する通知設定が存在しない（Internalメッセージ）場合にも呼ばれるのでdebugとする
				m_log.debug("getNotifyMessageInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static List<NotifyInfraInfo> getNotifyInfraInfoByInfraExecFacilityId(String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NotifyInfraInfo> list
			= em.createNamedQuery("NotifyInfraInfoEntity.findByInfraExecFacilityId", NotifyInfraInfo.class)
			.setParameter("facilityId", facilityId)
			.getResultList();
			return list;
		}
	}

	public static List<NotifyInfraInfo> getAllNotifyInfraInfo_NONE() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NotifyInfraInfo> list = em
					.createNamedQuery("NotifyInfraInfoEntity.findAll", NotifyInfraInfo.class, ObjectPrivilegeMode.NONE)
					.getResultList();
			return list;
		}
	}
	
	public static NotifyCloudInfo getNotifyCloudInfoPK(String pk) throws NotifyNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NotifyCloudInfo entity = em.find(NotifyCloudInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				NotifyNotFound e = new NotifyNotFound("NotifyCloudInfoEntity.findByPrimaryKey"
						+ pk.toString());
				// 対応する通知設定が存在しない（Internalメッセージ）場合にも呼ばれるのでdebugとする
				m_log.debug("getNotifyCloudInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}
	
	public static List<NotifyCloudInfo> getNotifyCloudInfoByCloudExecFacilityId(String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NotifyCloudInfo> list
			= em.createNamedQuery("NotifyCloudInfoEntity.findByCloudExecFacilityId", NotifyCloudInfo.class)
			.setParameter("facilityId", facilityId)
			.getResultList();
			return list;
		}
	}
	
	public static NotifyHistoryEntity getNotifyHistoryPK(NotifyHistoryEntityPK pk) throws NotifyNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NotifyHistoryEntity entity = em.find(NotifyHistoryEntity.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				NotifyNotFound e = new NotifyNotFound("NotifyHistoryEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.debug("getNotifyHistoryPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static NotifyHistoryEntity getNotifyHistoryPK(String facilityId,
			String pluginId,
			String monitorId,
			String notifyId,
			String subKey) throws NotifyNotFound {
		return getNotifyHistoryPK(new NotifyHistoryEntityPK(facilityId,
				pluginId,
				monitorId,
				notifyId,
				subKey));
	}

	public static NotifyRestInfo getNotifyRestInfoPK(String pk) throws NotifyNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			NotifyRestInfo entity = em.find(NotifyRestInfo.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				NotifyNotFound e = new NotifyNotFound("NotifyRestInfoEntity.findByPrimaryKey"
						+ pk.toString());
				// 対応する通知設定が存在しない（Internalメッセージ）場合にも呼ばれるのでdebugとする
				m_log.debug("getNotifyInfraInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static List<NotifyHistoryEntity> getNotifyHistoryByNotifyId(String notifyId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NotifyHistoryEntity> list
			= em.createNamedQuery("NotifyHistoryEntity.findByNotifyId", NotifyHistoryEntity.class)
			.setParameter("notifyId", notifyId)
			.getResultList();
			return list;
		}
	}

	public static void deleteNotifyHistoryByNotifyId(String notifyId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			em.createNamedQuery("NotifyHistoryEntity.deleteByNotifyId")
			.setParameter("notifyId", notifyId)
			.executeUpdate();
		}
	}

	public static List<NotifyRelationInfo> getAllNotifyRelationInfo() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NotifyRelationInfo> list
			=  em.createNamedQuery("NotifyRelationInfoEntity.findAll", NotifyRelationInfo.class)
			.getResultList();
			return list;
		}
	}

	/**
	 * 通知関連情報より、ジョブ設定を対象としたもののみを抽出する
	 * オブジェクト権限チェックなし
	 * @return
	 */
	public static List<NotifyRelationInfo> getNotifyRelationInfoJob() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			
			List<String> functionPrefixList = new ArrayList<String>();
			functionPrefixList.add(FunctionPrefixEnum.JOB_MASTER.name());
			
			List<NotifyRelationInfo> list
			=  em.createNamedQuery("NotifyRelationInfoEntity.findByFunctionPrefix", NotifyRelationInfo.class, ObjectPrivilegeMode.NONE)
			.setParameter("functionPrefix", functionPrefixList)
			.getResultList();
			return list;
		}
	}

	/**
	 * オブジェクト権限チェックなし
	 * @return
	 */
	public static List<NotifyRelationInfo> getAllNotifyRelationInfoWithoutJob() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			
			List<String> functionPrefixList = new ArrayList<String>();
			functionPrefixList.add(FunctionPrefixEnum.MONITOR.name());
			functionPrefixList.add(FunctionPrefixEnum.PREDICTION.name());
			functionPrefixList.add(FunctionPrefixEnum.CHANGE.name());
			functionPrefixList.add(FunctionPrefixEnum.INFRA.name());
			functionPrefixList.add(FunctionPrefixEnum.NODE_CONFIG_SETTING.name());
			functionPrefixList.add(FunctionPrefixEnum.MAINTENANCE.name());
			functionPrefixList.add(FunctionPrefixEnum.REPORTING.name());
			functionPrefixList.add(FunctionPrefixEnum.SDML_CONTROL.name());
			functionPrefixList.add(FunctionPrefixEnum.RPA_SCENARIO_CREATE.name());
			functionPrefixList.add(FunctionPrefixEnum.RPA_SCENARIO_CORRECT.name());
			
			List<NotifyRelationInfo> list
			=  em.createNamedQuery("NotifyRelationInfoEntity.findByFunctionPrefix", NotifyRelationInfo.class, ObjectPrivilegeMode.NONE)
			.setParameter("functionPrefix", functionPrefixList)
			.getResultList();
			return list;
		}
	}

	public static List<NotifyRelationInfo> getNotifyRelationInfoByNotifyGroupId(String notifyGroupId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NotifyRelationInfo> list
			=  em.createNamedQuery("NotifyRelationInfoEntity.findByNotifyGroupId", NotifyRelationInfo.class)
			.setParameter("notifyGroupId", notifyGroupId)
			.getResultList();
			m_log.debug("queryUtil " + list.size() + "," + notifyGroupId);
			return list;
		}
	}
	
	public static void deleteNotifyRelationInfoByNotifyGroupId(String notifyGroupId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			em.createNamedQuery("NotifyRelationInfoEntity.deleteByNotifyGroupId", NotifyRelationInfo.class)
			.setParameter("notifyGroupId", notifyGroupId)
			.executeUpdate();
		}
	}

	public static List<NotifyRelationInfo> getNotifyRelationInfoByNotifyId(String notifyId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NotifyRelationInfo> list
			=  em.createNamedQuery("NotifyRelationInfoEntity.findByNotifyId", NotifyRelationInfo.class)
			.setParameter("notifyId", notifyId)
			.getResultList();
			return list;
		}
	}

	public static void deleteNotifyRelationInfoByNotifyId(String notifyId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			em.createNamedQuery("NotifyRelationInfoEntity.deleteByNotifyId")
			.setParameter("notifyId", notifyId)
			.executeUpdate();
		}
	}

	public static List<NotifyHistoryEntity> getNotifyHistoryByPluginIdAndMonitorId(String pluginId, String monitorId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NotifyHistoryEntity> list =
					em.createNamedQuery("NotifyHistoryEntity.findByPluginIdAndMonitorId", NotifyHistoryEntity.class)
					.setParameter("pluginId", pluginId)
					.setParameter("monitorId", monitorId)
					.getResultList();
			return list;
		}
	}

	public static void deleteNotifyHistoryByPluginIdAndMonitorId(String pluginId, String monitorId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			em.createNamedQuery("NotifyHistoryEntity.deleteByPluginIdAndMonitorId", NotifyHistoryEntity.class)
			.setParameter("pluginId", pluginId)
			.setParameter("monitorId", monitorId)
			.executeUpdate();
		}
	}

	public static List<NotifyHistoryEntity> getNotifyHistoryByPluginIdAndMonitorIdAndFacilityId(
			String pluginId, String monitorId, String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NotifyHistoryEntity> list =
					em.createNamedQuery("NotifyHistoryEntity.findByPluginIdAndMonitorIdAndFacilityId", NotifyHistoryEntity.class)
					.setParameter("pluginId", pluginId)
					.setParameter("monitorId", monitorId)
					.setParameter("facilityId", facilityId)
					.getResultList();
			return list;
		}
	}

	public static void deleteNotifyHistoryByFacilityId(String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			em.createNamedQuery("NotifyHistoryEntity.deleteByFacilityId")
			.setParameter("facilityId", facilityId)
			.executeUpdate();
		}
	}

	public static List<MonitorStatusEntity> getMonitorStatusWithoutPluginIds(List<String> withoutPluginIds) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorStatusEntity> list
			=  em.createNamedQuery("MonitorStatusEntity.findWithoutPluginIds", MonitorStatusEntity.class)
			.setParameter("withoutPluginIds", withoutPluginIds)
			.getResultList();
			return list;
		}
	}

	public static void deleteMonitorStatusWithoutPluginIds(List<String> withoutPluginIds) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			em.createNamedQuery("MonitorStatusEntity.deleteWithoutPluginIds")
			.setParameter("withoutPluginIds", withoutPluginIds)
			.executeUpdate();
		}
	}

	public static void deleteMonitorStatusByFacilityId(String facilityId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			em.createNamedQuery("MonitorStatusEntity.deleteByFacilityId")
			.setParameter("facilityId", facilityId)
			.executeUpdate();
		}
	}

	public static MonitorStatusEntity getMonitorStatusPK(MonitorStatusEntityPK pk) throws NotifyNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			MonitorStatusEntity entity = em.find(MonitorStatusEntity.class, pk, ObjectPrivilegeMode.READ);
			if (entity == null) {
				NotifyNotFound e = new NotifyNotFound("MonitorStatusEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.info("getMonitorStatusPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static MonitorStatusEntity getMonitorStatusPK(MonitorStatusEntityPK pk, ObjectPrivilegeMode mode) throws NotifyNotFound {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			MonitorStatusEntity entity = em.find(MonitorStatusEntity.class, pk, mode);
			if (entity == null) {
				NotifyNotFound e = new NotifyNotFound("MonitorStatusEntity.findByPrimaryKey"
						+ pk.toString());
				m_log.info("getMonitorStatusPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
			return entity;
		}
	}

	public static MonitorStatusEntity getMonitorStatusPK(String facilityId,
			String pluginId,
			String monitorId,
			String subKey) throws NotifyNotFound {
		return getMonitorStatusPK(new MonitorStatusEntityPK(facilityId,
				pluginId,
				monitorId,
				subKey));
	}

	public static void deleteMonitorStatusByPluginIdAndMonitorId(String pluginId, String monitorId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			em.createNamedQuery("MonitorStatusEntity.deleteByPluginIdAndMonitorId")
			.setParameter("pluginId", pluginId)
			.setParameter("monitorId", monitorId)
			.executeUpdate();
		}
	}

	public static List<StatusInfoEntity> getStatusInfoByPluginIdAndMonitorId(String pluginId,
			String monitorId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<StatusInfoEntity> list =
					em.createNamedQuery("StatusInfoEntity.findByPluginIdAndMonitorId", StatusInfoEntity.class)
					.setParameter("pluginId", pluginId)
					.setParameter("monitorId", monitorId)
					.getResultList();
			return list;
		}
	}

	public static CommandTemplateInfo getCommandTemplateInfoPK(String commandTemplateId) throws CommandTemplateNotFound, InvalidRole {
		return getCommandTemplateInfoPK(commandTemplateId, ObjectPrivilegeMode.READ);
	}

	public static CommandTemplateInfo getCommandTemplateInfoPK(String commandTemplateId, ObjectPrivilegeMode mode) throws CommandTemplateNotFound, InvalidRole {
		CommandTemplateInfo entity = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			entity = em.find(CommandTemplateInfo.class, commandTemplateId, mode);
			if (entity == null) {
				CommandTemplateNotFound e = new CommandTemplateNotFound("CommandTemplateInfo.findByPrimaryKey"
						+ "commandTemplateId = " + commandTemplateId);
				m_log.info("getCommandTemplateInfoPK() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		} catch (ObjectPrivilege_InvalidRole e) {
			m_log.info("getCommandTemplateInfoPK() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new InvalidRole(e.getMessage(), e);
		}
		return entity;
	}

	public static List<CommandTemplateInfo> getAllCommandTemplateOrderByCommandTemplateId() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<CommandTemplateInfo> list
			= em.createNamedQuery("CommandTemplateInfo.findAllOrderByCommandTemplateId", CommandTemplateInfo.class)
			.getResultList();
			return list;
		}
	}

	public static List<CommandTemplateInfo> getAllCommandTemplateOrderByCommandTemplateId_OR(String ownerRoleId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<CommandTemplateInfo> list
			= em.createNamedQuery_OR("CommandTemplateInfo.findAllOrderByCommandTemplateId", CommandTemplateInfo.class, ownerRoleId)
			.getResultList();
			return list;
		}
	}

	public static List<NotifyCommandInfo> getNotifyCommandInfoByCommandSettingType(Integer commandSettingType) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NotifyCommandInfo> list
			= em.createNamedQuery("NotifyCommandInfo.findByCommandSettingType", NotifyCommandInfo.class)
			.setParameter("commandSettingType", commandSettingType)
			.getResultList();
			return list;
		}
	}

	/**
	 * ジョブ連携送信設定IDが設定されているジョブ通知情報を検索
	 * 
	 * @param joblinkSendSettingId ジョブ連携送信設定ID
	 * @param mode オブジェクト権限種別
	 * @return ジョブ通知情報一覧
	 */
	public static List<NotifyJobInfo> getNotifyJobInfoByJoblinkSendSettingId_NONE(String joblinkSendSettingId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			return em.createNamedQuery("NotifyJobInfoEntity.findByJoblinkSendSettingId", NotifyJobInfo.class, ObjectPrivilegeMode.NONE)
					.setParameter("joblinkSendSettingId", joblinkSendSettingId).getResultList();
		}
	}
}
