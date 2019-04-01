/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.util;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.model.RoleInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PluginConstant;
import com.clustercontrol.calendar.model.CalendarInfo;
import com.clustercontrol.calendar.model.CalendarPatternInfo;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.MailTemplateNotFound;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.UsedOwnerRole;
import com.clustercontrol.infra.model.InfraManagementInfo;
import com.clustercontrol.jobmanagement.model.JobKickEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntityPK;
import com.clustercontrol.jobmanagement.model.JobmapIconImageEntity;
import com.clustercontrol.jobmanagement.queue.JobQueue;
import com.clustercontrol.jobmanagement.queue.JobQueueContainer;
import com.clustercontrol.jobmanagement.queue.internal.JobQueueEntity;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.notify.mail.model.MailTemplateInfo;
import com.clustercontrol.notify.model.NotifyInfo;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.Singletons;

/**
 * ロール管理の入力チェッククラス
 * 
 * @since 4.0
 */
public class RoleValidator {

	private static Log m_log = LogFactory.getLog( RoleValidator.class );

	/**
	 * ロール情報(RoleInfo)の基本設定の妥当性チェック
	 * @param roleInfo
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 */
	public static void validateRoleInfo(RoleInfo roleInfo) throws InvalidSetting {

		// roleId
		CommonValidator.validateId(MessageConstant.ROLE_ID.getMessage(), roleInfo.getRoleId(), 64);

		// roleName
		CommonValidator.validateString(MessageConstant.ROLE_NAME.getMessage(), roleInfo.getRoleName(), true, 1, 128);

		// description
		CommonValidator.validateString(MessageConstant.DESCRIPTION.getMessage(), roleInfo.getDescription(), false, 0, 256);

	}


	/**
	 * 他の機能にて、オーナーロールとして使用されているか調査する。
	 * 参照状態の場合、メッセージダイアログが出力される。
	 * @param roleId
	 * @throw UsedRole
	 * @throw HinemosUnknown
	 */
	public static void validateDeleteRole(String roleId) throws UsedOwnerRole, HinemosUnknown {
		JpaTransactionManager jtm = null;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// リポジトリ（ノード、スコープ）
			List<FacilityInfo> infoCollectionFacility
			= com.clustercontrol.repository.util.QueryUtil.getFacilityByOwnerRoleId_NONE(roleId);
			if (infoCollectionFacility != null && infoCollectionFacility.size() > 0) {
				m_log.info("validateDeleteRole,[" + roleId + "] : " + PluginConstant.TYPE_REPOSITORY);
				throw new UsedOwnerRole(PluginConstant.TYPE_REPOSITORY);
			}

			// 監視設定
			List<MonitorInfo> infoCollectionMonitor
			=  com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoByOwnerRoleId_NONE(roleId);
			if (infoCollectionMonitor != null && infoCollectionMonitor.size() > 0) {
				m_log.info("validateDeleteRole,[" + roleId + "] : " + PluginConstant.TYPE_MONITOR);
				throw new UsedOwnerRole(PluginConstant.TYPE_MONITOR);
			}

			// ジョブ
			List<JobMstEntity> infoCollectionJob
			= com.clustercontrol.jobmanagement.util.QueryUtil.getJobMstEntityFindByOwnerRoleId_NONE(roleId);
			if (infoCollectionJob != null && infoCollectionJob.size() > 0) {
				m_log.info("validateDeleteRole,[" + roleId + "] : " + PluginConstant.TYPE_JOBMANAGEMENT);
				throw new UsedOwnerRole(PluginConstant.TYPE_JOBMANAGEMENT);
			}

			// ジョブ実行契機
			List<JobKickEntity> infoCollectionJobKick
			= com.clustercontrol.jobmanagement.util.QueryUtil.getJobKickEntityFindByOwnerRoleId_NONE(roleId);
			if (infoCollectionJobKick != null && infoCollectionJobKick.size() > 0) {
				m_log.info("validateDeleteRole,[" + roleId + "] : " + PluginConstant.TYPE_JOBMANAGEMENT);
				throw new UsedOwnerRole(PluginConstant.TYPE_JOBMANAGEMENT);
			}

			// ジョブキュー
			List<JobQueue> jobQueues = Singletons.get(JobQueueContainer.class).findByOwnerRoleId(roleId);
			if (!jobQueues.isEmpty()) {
				String ids = jobQueues.stream().map(q -> q.getId()).collect(Collectors.joining(","));
				m_log.info("validateDeleteRole,[" + roleId + "] : " + PluginConstant.TYPE_JOBMANAGEMENT
						+ " JobQueue[" + ids + "]");
				throw new UsedOwnerRole(PluginConstant.TYPE_JOBMANAGEMENT);
			}

			// カレンダ
			List<CalendarInfo> infoCollectionCalInfo
			= com.clustercontrol.calendar.util.QueryUtil.getCalInfoFindByOwnerRoleId_NONE(roleId);
			if (infoCollectionCalInfo != null && infoCollectionCalInfo.size() > 0) {
				m_log.info("validateDeleteRole,[" + roleId + "] : " + PluginConstant.TYPE_CALENDAR);
				throw new UsedOwnerRole(PluginConstant.TYPE_CALENDAR);
			}

			// カレンダパターン
			List<CalendarPatternInfo> infoCollectionCalPattern
			= com.clustercontrol.calendar.util.QueryUtil.getCalPatternInfoFindByOwnerRoleId_NONE(roleId);
			if (infoCollectionCalPattern != null && infoCollectionCalPattern.size() > 0) {
				m_log.info("validateDeleteRole,[" + roleId + "] : " + PluginConstant.TYPE_CALENDAR);
				throw new UsedOwnerRole(PluginConstant.TYPE_CALENDAR);
			}

			// 通知
			List<NotifyInfo> infoCollectionNotifyInfo
			= com.clustercontrol.notify.util.QueryUtil.getNotifyInfoFindByOwnerRoleId_NONE(roleId);
			if (infoCollectionNotifyInfo != null && infoCollectionNotifyInfo.size() > 0) {
				m_log.info("validateDeleteRole,[" + roleId + "] : " + PluginConstant.TYPE_NOTIFY);
				throw new UsedOwnerRole(PluginConstant.TYPE_NOTIFY);
			}

			// メールテンプレート
			List<MailTemplateInfo> infoCollectionMailTemp
			= com.clustercontrol.notify.mail.util.QueryUtil.getMailTemplateInfoFindByOwnerRoleId_NONE(roleId);
			if (infoCollectionMailTemp != null && infoCollectionMailTemp.size() > 0) {
				m_log.info("validateDeleteRole,[" + roleId + "] : " + PluginConstant.TYPE_NOTIFY);
				throw new UsedOwnerRole(PluginConstant.TYPE_NOTIFY);
			}

			// 環境構築
			List<InfraManagementInfo> infoCollectionInfra
			= com.clustercontrol.infra.util.QueryUtil.getInfraManagementInfoFindByOwnerRoleId_NONE(roleId);
			if (infoCollectionInfra != null && infoCollectionInfra.size() > 0) {
				m_log.info("validateDeleteRole,[" + roleId + "] : " + PluginConstant.TYPE_INFRA);
				throw new UsedOwnerRole(PluginConstant.TYPE_INFRA);
			}
			
			
			jtm.commit();
		} catch (UsedOwnerRole e) {
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("validateDeleteRole() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	/**
	 * 変更前後でオーナーロールが変更されていないかチェックする
	 * 
	 * @param pk
	 * @param objectType
	 * @param ownerRoleId
	 * @throw InvalidSetting
	 */
	public static void validateModifyOwnerRole(Object pk, String objectType, String ownerRoleId) throws InvalidSetting{

		JpaTransactionManager jtm = null;
		InvalidSetting ise = new InvalidSetting(MessageConstant.MESSAGE_OWNERROLEID_CHANGE_NG.getMessage());

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// リポジトリ(ノード、スコープ)
			if (objectType.equals(HinemosModuleConstant.PLATFORM_REPOSITORY)) {

				m_log.debug("validateModifyOwnerRole() : FacilityEntity check.");
				FacilityInfo info = null;

				try {
					info = com.clustercontrol.repository.util.QueryUtil.getFacilityPK((String)pk);
				}catch (FacilityNotFound e) {
					// 設定が見つからない場合は新規登録と判断し、何もしない;
				}

				// 既存の設定のオーナーロールから変更されている場合
				if(info != null && !info.getOwnerRoleId().equals(ownerRoleId)) {
					throw ise;
				}
			}
			// 監視
			else if (objectType.equals(HinemosModuleConstant.MONITOR)) {

				m_log.debug("validateModifyOwnerRole() : MonitorInfo check.");
				MonitorInfo info = null;

				try {
					info = com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK((String)pk);
				} catch (MonitorNotFound e) {
					// 設定が見つからない場合は新規登録と判断し、何もしない
				}

				// 既存の設定のオーナーロールから変更されている場合
				if(info != null && !info.getOwnerRoleId().equals(ownerRoleId)) {
					throw ise;
				}
			}
			// ジョブユニット
			else if (objectType.equals(HinemosModuleConstant.JOB)) {

				m_log.debug("validateModifyOwnerRole() : JobMstEntity check.");
				JobMstEntity info = null;

				try {
					info = com.clustercontrol.jobmanagement.util.QueryUtil.getJobMstPK_NONE((JobMstEntityPK)pk);
				} catch (JobMasterNotFound e) {
					// 設定が見つからない場合は新規登録と判断し、何もしない
				}

				// 既存の設定のオーナーロールから変更されている場合
				if(info != null && !info.getOwnerRoleId().equals(ownerRoleId)) {
					throw ise;
				}
			}

			// ジョブ実行契機
			else if (objectType.equals(HinemosModuleConstant.JOB_KICK)) {

				m_log.debug("validateModifyOwnerRole() : JobKickEntity check.");
				HinemosEntityManager em = jtm.getEntityManager();
				JobKickEntity info = em.find(JobKickEntity.class, pk, ObjectPrivilegeMode.READ);
				if (info == null) {
					// 設定が見つからない場合は新規登録と判断し、何もしない
				}

				// 既存の設定のオーナーロールから変更されている場合
				if(info != null && !info.getOwnerRoleId().equals(ownerRoleId)) {
					throw ise;
				}
			}

			// ジョブマップ用イメージファイル
			else if (objectType.equals(HinemosModuleConstant.JOBMAP_IMAGE_FILE)) {

				m_log.debug("validateModifyOwnerRole() : JobmapIconImageEntity check.");
				HinemosEntityManager em = jtm.getEntityManager();
				JobmapIconImageEntity info = em.find(JobmapIconImageEntity.class, pk, ObjectPrivilegeMode.READ);
				if (info == null) {
					// 設定が見つからない場合は新規登録と判断し、何もしない
				}

				// 既存の設定のオーナーロールから変更されている場合
				if(info != null && !info.getOwnerRoleId().equals(ownerRoleId)) {
					throw ise;
				}
			}

			// ジョブ同時実行制御キュー
			else if (objectType.equals(HinemosModuleConstant.JOB_QUEUE)) {

				m_log.debug("validateModifyOwnerRole() : JobQueueEntity check.");
				HinemosEntityManager em = jtm.getEntityManager();
				JobQueueEntity info = em.find(JobQueueEntity.class, pk, ObjectPrivilegeMode.READ);
				if (info == null) {
					// 設定が見つからない場合は新規登録と判断し、何もしない
				}

				// 既存の設定のオーナーロールから変更されている場合
				if(info != null && !info.getOwnerRoleId().equals(ownerRoleId)) {
					throw ise;
				}
			}
			
			// カレンダ
			else if (objectType.equals(HinemosModuleConstant.PLATFORM_CALENDAR)) {

				m_log.debug("validateModifyOwnerRole() : CalInfoEntity check.");
				CalendarInfo info = null;

				try {
					info = com.clustercontrol.calendar.util.QueryUtil.getCalInfoPK((String)pk);
				} catch (CalendarNotFound e) {
					// 設定が見つからない場合は新規登録と判断し、何もしない
				}

				// 既存の設定のオーナーロールから変更されている場合
				if(info != null && !info.getOwnerRoleId().equals(ownerRoleId)) {
					throw ise;
				}
			}

			// カレンダパターン
			else if (objectType.equals(HinemosModuleConstant.PLATFORM_CALENDAR_PATTERN)) {

				m_log.debug("validateModifyOwnerRole() : CalPatternInfoEntity check.");
				CalendarPatternInfo info = null;

				try {
					info = com.clustercontrol.calendar.util.QueryUtil.getCalPatternInfoPK((String)pk);
				} catch (CalendarNotFound e) {
					// 設定が見つからない場合は新規登録と判断し、何もしない
				}

				// 既存の設定のオーナーロールから変更されている場合
				if(info != null && !info.getOwnerRoleId().equals(ownerRoleId)) {
					throw ise;
				}
			}

			// 通知
			else if (objectType.equals(HinemosModuleConstant.PLATFORM_NOTIFY)) {

				m_log.debug("validateModifyOwnerRole() : NotifyInfoEntity check.");
				NotifyInfo info = null;

				try {
					info = com.clustercontrol.notify.util.QueryUtil.getNotifyInfoPK((String)pk);
				} catch (NotifyNotFound e) {
					// 設定が見つからない場合は新規登録と判断し、何もしない
				}

				// 既存の設定のオーナーロールから変更されている場合
				if(info != null && !info.getOwnerRoleId().equals(ownerRoleId)) {
					throw ise;
				}
			}

			// メールテンプレート
			else if (objectType.equals(HinemosModuleConstant.PLATFORM_MAIL_TEMPLATE)) {

				m_log.debug("validateModifyOwnerRole() : MailTemplateInfoEntity check.");
				MailTemplateInfo info = null;

				try {
					info = com.clustercontrol.notify.mail.util.QueryUtil.getMailTemplateInfoPK((String)pk);
				} catch (MailTemplateNotFound e) {
					// 設定が見つからない場合は新規登録と判断し、何もしない
				}

				// 既存の設定のオーナーロールから変更されている場合
				if(info != null && !info.getOwnerRoleId().equals(ownerRoleId)) {
					throw ise;
				}
			}

			jtm.commit();
		} catch (InvalidSetting e) {
			m_log.warn("validateModifyOwnerRole() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			jtm.rollback();
			throw e;
		} catch (Exception e) {
			m_log.warn("validateModifyOwnerRole() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	/**
	 * ユーザがロールに所属しているかチェックする
	 * (ADMINISTRATORS所属のユーザの場合はチェックしない)
	 * 
	 * @param role
	 * @param user
	 * @param isAdmin
	 * @throw InvalidSetting
	 */
	public static void validateUserBelongRole(String role, String user, boolean isAdmin) throws InvalidSetting{

		if(!isAdmin && !UserRoleCache.getRoleIdList(user).contains(role)) {
			String args[] = {user, role};
			throw new InvalidSetting(MessageConstant.MESSAGE_USER_DOES_NOT_BELONG_TO_ROLE.getMessage(args));
		}
	}
}
