/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.notify.model.MonitorStatusEntity;
import com.clustercontrol.notify.model.MonitorStatusEntityPK;
import com.clustercontrol.notify.model.NotifyCommandInfo;
import com.clustercontrol.notify.model.NotifyEventInfo;
import com.clustercontrol.notify.model.NotifyHistoryEntity;
import com.clustercontrol.notify.model.NotifyHistoryEntityPK;
import com.clustercontrol.notify.model.NotifyInfo;
import com.clustercontrol.notify.model.NotifyInfraInfo;
import com.clustercontrol.notify.model.NotifyJobInfo;
import com.clustercontrol.notify.model.NotifyLogEscalateInfo;
import com.clustercontrol.notify.model.NotifyMailInfo;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.notify.model.NotifyStatusInfo;
import com.clustercontrol.notify.monitor.model.StatusInfoEntity;

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
	 * オブジェクト権限チェックなし
	 * @return
	 */
	public static List<NotifyRelationInfo> getAllNotifyRelationInfoWithoutJob() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<NotifyRelationInfo> list
			=  em.createNamedQuery("NotifyRelationInfoEntity.findAllWithoutJob", NotifyRelationInfo.class, ObjectPrivilegeMode.NONE)
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

	public static List<MonitorStatusEntity> getAllMonitorStatus() {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorStatusEntity> list
			=  em.createNamedQuery("MonitorStatusEntity.findAll", MonitorStatusEntity.class)
			.getResultList();
			return list;
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

	public static MonitorStatusEntity getMonitorStatusPK(String facilityId,
			String pluginId,
			String monitorId,
			String subKey) throws NotifyNotFound {
		return getMonitorStatusPK(new MonitorStatusEntityPK(facilityId,
				pluginId,
				monitorId,
				subKey));
	}

	public static List<MonitorStatusEntity> getMonitorStatusByPluginIdAndMonitorId(String pluginId, String monitorId) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			List<MonitorStatusEntity> list =
					em.createNamedQuery("MonitorStatusEntity.findByPluginIdAndMonitorId", MonitorStatusEntity.class)
					.setParameter("pluginId", pluginId)
					.setParameter("monitorId", monitorId)
					.getResultList();
			return list;
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
}
