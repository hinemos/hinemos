/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.factory;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.FunctionConstant;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.accesscontrol.bean.RoleTypeConstant;
import com.clustercontrol.accesscontrol.bean.UserTypeConstant;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfo;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfoPK;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.accesscontrol.model.RoleInfo;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfoPK;
import com.clustercontrol.accesscontrol.model.UserInfo;
import com.clustercontrol.accesscontrol.util.ObjectPrivilegeUtil;
import com.clustercontrol.accesscontrol.util.QueryUtil;
import com.clustercontrol.accesscontrol.util.RoleValidator;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.calendar.model.CalendarInfo;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.PrivilegeDuplicate;
import com.clustercontrol.fault.PrivilegeNotFound;
import com.clustercontrol.fault.RoleDuplicate;
import com.clustercontrol.fault.RoleNotFound;
import com.clustercontrol.fault.UnEditableRole;
import com.clustercontrol.fault.UsedObjectPrivilege;
import com.clustercontrol.fault.UsedRole;
import com.clustercontrol.fault.UserNotFound;
import com.clustercontrol.hub.model.TransferInfo;
import com.clustercontrol.infra.model.InfraManagementInfo;
import com.clustercontrol.jobmanagement.model.JobKickEntity;
import com.clustercontrol.jobmanagement.model.JobLinkSendSettingEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.maintenance.model.MaintenanceInfo;
import com.clustercontrol.monitor.run.bean.CollectMonitorNotifyConstant;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.notify.model.NotifyInfo;
import com.clustercontrol.reporting.model.ReportingInfoEntity;
import com.clustercontrol.repository.model.NodeConfigSettingInfo;
import com.clustercontrol.rpa.scenario.model.RpaScenarioOperationResultCreateSetting;
import com.clustercontrol.sdml.model.SdmlControlSettingInfo;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

import jakarta.persistence.EntityExistsException;

/**
 * ロール情報を更新するファクトリクラス<BR>
 *
 * @version 1.0.0
 * @since 3.2.0
 */
public class RoleModifier {

	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(RoleModifier.class);

	/**
	 * ロールを新規登録・変更する。<BR>
	 * 
	 * @param roleInfo 新規登録・変更するロール情報
	 * @param modifyUserId 作業ユーザID
	 * @param isNew true:新規登録／false:更新 
	 * @throws RoleDuplicate
	 * @throws RoleNotFound
	 * @throws UnEditableRole
	 * @throws HinemosUnknown
	 */
	public static void modifyRoleInfo(RoleInfo roleInfo, String modifyUserId, boolean isNew) 
			throws RoleDuplicate, RoleNotFound, UnEditableRole, HinemosUnknown {

		if(roleInfo == null || modifyUserId == null || modifyUserId.compareTo("") == 0){
			return;
		}
		m_log.debug("modifyRoleInfo() start (roleId = " + roleInfo.getRoleId() 
			+ ", modifyUserId = " + modifyUserId + ", isNew = " + isNew + ")");
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			long currentTimeMillis = HinemosTime.currentTimeMillis();
			if (isNew) {
				// 新規登録
				// 重複チェック
				jtm.checkEntityExists(RoleInfo.class, roleInfo.getRoleId());
				// 情報設定
				roleInfo.setCreateUserId(modifyUserId);
				roleInfo.setCreateDate(currentTimeMillis);
				// リポジトリの参照権限を設定する
				SystemPrivilegeInfo systemPrivilegeInfo = QueryUtil.getSystemPrivilegePK(
						new SystemPrivilegeInfoPK(FunctionConstant.REPOSITORY, SystemPrivilegeMode.READ.name()));
				roleInfo.setSystemPrivilegeList(new ArrayList<SystemPrivilegeInfo>());
				roleInfo.getSystemPrivilegeList().add(systemPrivilegeInfo);
				if (systemPrivilegeInfo.getRoleList() == null) {
					systemPrivilegeInfo.setRoleList(new ArrayList<RoleInfo>());
				}
				systemPrivilegeInfo.getRoleList().add(roleInfo);
				roleInfo.setRoleType(RoleTypeConstant.USER_ROLE);
				roleInfo.setModifyUserId(modifyUserId);
				roleInfo.setModifyDate(currentTimeMillis);
				jtm.getEntityManager().persist(roleInfo);
			} else {
				// 更新
				// インスタンスの取得
				RoleInfo roleInfoEntity = QueryUtil.getRolePK(roleInfo.getRoleId());
				// システムロール、内部モジュールロールは変更不可
				if (!roleInfoEntity.getRoleType().equals(RoleTypeConstant.USER_ROLE)) {
					throw new UnEditableRole();
				}
				// 情報設定
				roleInfoEntity.setRoleName(roleInfo.getRoleName());
				roleInfoEntity.setDescription(roleInfo.getDescription());
				roleInfoEntity.setModifyUserId(modifyUserId);
				roleInfoEntity.setModifyDate(currentTimeMillis);
			}
			m_log.info("successful in modifying a role. (roleId = " + roleInfo.getRoleId() + ")");
		} catch (RoleNotFound | UnEditableRole e) {
			throw e;
		} catch (EntityExistsException e) {
			m_log.info("modifyRoleInfo() failure to add a role. a role'id is duplicated. (roleId = " + roleInfo.getRoleId() + ")");
			throw new RoleDuplicate(e.getMessage(), e);
		} catch (Exception e) {
			m_log.warn("modifyRoleInfo() failure to modify a role. (roleId = " + roleInfo.getRoleId() + ")", e);
			throw new HinemosUnknown("failure to modify a role. (roleId = " + roleInfo.getRoleId() + ")", e);
		}
	}

	/**
	 * ロールを削除する。<BR>
	 * 
	 * @param roleId 削除対象のロールID
	 * @param modifyUserId 作業ユーザID
	 * @throws RoleNotFound
	 * @throws UnEditableRole
	 * @throws UsedRole
	 * @throws HinemosUnknown
	 */
	public static void deleteRoleInfo(String roleId, String modifyUserId) throws RoleNotFound, UnEditableRole, UsedRole, HinemosUnknown {

		if(roleId == null || roleId.compareTo("") == 0 
				|| modifyUserId == null || modifyUserId.compareTo("") == 0){
			return;
		}

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// 該当するロールを検索して取得
			RoleInfo role = QueryUtil.getRolePK(roleId);
			// システムロール、内部モジュールロールは削除不可
			if (role != null && !role.getRoleType().equals(RoleTypeConstant.USER_ROLE)) {
				throw new UnEditableRole();
			}
			if (role.getUserInfoList() != null && role.getUserInfoList().size() > 0) {
				throw new UsedRole();
			}

			// リレーションを削除する
			role.unchainUserInfoList();
			role.unchainSystemPrivilegeInfoList();
			// ロールを削除する
			em.remove(role);

		} catch (RoleNotFound | UnEditableRole | UsedRole e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("deleteRoleInfo() failure to delete a role. (roleId = " + roleId + ")", e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

		m_log.info("successful in deleting a role. (roleId = " + roleId + ")");
	}

	/**
	 * ロールにユーザを割り当てる。<BR>
	 * 
	 * @param roleId ロールID
	 * @param userIds ユーザID配列
	 * @throws UserNotFound
	 * @throws RoleNotFound
	 * @throws UnEditableRole
	 * @throws HinemosUnknown
	 */
	public static void assignUserToRole(String roleId, String[] userIds) throws UserNotFound, RoleNotFound, UnEditableRole, HinemosUnknown {

		/** ローカル変数 */
		RoleInfo roleInfo = null;

		/** メイン処理 */
		try {
			// ロール情報の取得
			roleInfo = QueryUtil.getRolePK(roleId);

			// システムロールである場合、システムユーザ（hinemos）が含まれてない場合は変更不可
			if (roleInfo.getRoleType().equals(RoleTypeConstant.SYSTEM_ROLE)) {
				boolean existsFlg = false;
				for (String userId : userIds) {
					UserInfo userInfo = QueryUtil.getUserPK(userId);
					if(userInfo.getUserType().equals(UserTypeConstant.SYSTEM_USER)) {
						existsFlg = true;
						break;
					}
				}
				// システムユーザが存在しない場合
				if (!existsFlg) {
					throw new UnEditableRole();
				}
			}

			// ユーザ情報からロール情報を削除
			roleInfo.unchainUserInfoList();

			ArrayList<UserInfo> userInfoList = new ArrayList<UserInfo>();
			if (userIds != null) {
				for (String userId : userIds) {
					UserInfo userInfo = QueryUtil.getUserPK(userId);
					if (userInfo.getRoleList() == null) {
						userInfo.setRoleList(new ArrayList<RoleInfo>());
					}
					if (!userInfo.getRoleList().contains(roleInfo)) {
						userInfo.getRoleList().add(roleInfo);
					}
					userInfoList.add(userInfo);
				}
			}
			roleInfo.setUserInfoList(userInfoList);

		} catch (UserNotFound | RoleNotFound | UnEditableRole e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("modifyRoleInfo() failure to assign. (roleId = " + roleId + ")", e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * ロールにシステム権限を割り当てる。<BR>
	 * 
	 * @param roleId ロールID
	 * @param systemPrivileges システム権限配列
	 * @throws RoleNotFound
	 * @throws UnEditableRole
	 * @throws HinemosUnknown
	 */
	public static void replaceSystemPrivilegeToRole(String roleId, List<SystemPrivilegeInfo> systemPrivileges) throws RoleNotFound, UnEditableRole, HinemosUnknown {

		/** ローカル変数 */
		RoleInfo roleInfo = null;

		/** メイン処理 */
		try {
			// ロール情報の取得
			roleInfo = QueryUtil.getRolePK(roleId);

			// ADMINISTRATORSロールはシステム権限変更不可
			if (roleInfo.getRoleId().equals(RoleIdConstant.ADMINISTRATORS)) {
				throw new UnEditableRole();
			}

			if (systemPrivileges != null) {
				SystemPrivilegeInfo systemPrivilegeInfoEntity = null;
				List<SystemPrivilegeInfoPK> systemPrivilegeInfoPKList = new ArrayList<>();
				for (SystemPrivilegeInfo systemPrivilegeInfo : systemPrivileges) {
					systemPrivilegeInfoEntity = QueryUtil.getSystemPrivilegePK(systemPrivilegeInfo.getId());
					boolean isExist = false;
					for (RoleInfo role : systemPrivilegeInfoEntity.getRoleList()) {
						if (role.getRoleId().equals(roleId)) {
							isExist = true;
							break;
						}
					}
					if (!isExist) {
						roleInfo.getSystemPrivilegeList().add(systemPrivilegeInfoEntity);
						systemPrivilegeInfoEntity.getRoleList().add(roleInfo);
					}
					systemPrivilegeInfoPKList.add(systemPrivilegeInfo.getId());
				}

				// 不要なRoleInfoを削除
				roleInfo.deleteSystemPrivilegeEntities(systemPrivilegeInfoPKList);
			}
			m_log.debug("replaceSystemPrivilegeToRole " + roleId);

		} catch (RoleNotFound e) {
			throw e;
		} catch (UnEditableRole e) {
			throw e;
		} catch (Exception e) {
			m_log.warn("replaceSystemPrivilegeToRole() failure to assign. (roleId = " + roleId + ")", e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * オブジェクト種別、オブジェクトIDに紐づくオブジェクト権限情報を差し替える。<BR>
	 * 
	 * @param objectType オブジェクト種別
	 * @param objectId オブジェクトID
	 * @param list 差し替えるオブジェクト情報
	 * @param modifyUserId 作業ユーザID
	 * @throws PrivilegeDuplicate
	 * @throws HinemosUnknown
	 */
	public static void replaceObjectPrivilegeInfo(String objectType, String objectId, List<ObjectPrivilegeInfo> list, String modifyUserId)
			throws PrivilegeDuplicate, UsedObjectPrivilege, HinemosUnknown, InvalidSetting {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// オブジェクト種別、オブジェクトIDに該当する既存のオブジェクト権限を取得し、削除
			List<ObjectPrivilegeInfoPK> deleteList = new ArrayList<ObjectPrivilegeInfoPK>();
			List<ObjectPrivilegeInfo> oldList = QueryUtil.getAllObjectPrivilegeByFilter(
					objectType,
					objectId,
					null,
					null);
			if (oldList != null && oldList.size() > 0) {
				for (ObjectPrivilegeInfo oldInfo : oldList) {
					deleteList.add(oldInfo.getId());
				}
			}

			// 更新対象オブジェクトのオーナーロールIDを取得しておく
			ObjectPrivilegeTargetInfo objectPrivilegeTargetInfo
			= (ObjectPrivilegeTargetInfo)ObjectPrivilegeUtil.getObjectPrivilegeObject(objectType, objectId, ObjectPrivilegeMode.READ);
			if (objectPrivilegeTargetInfo == null) {
				m_log.warn("unknown object : objectType=" + objectType + ", objectId=" + objectId);
				throw new HinemosUnknown("objectPrivilegeTargetEntity is null objectId: " + objectId);
			}
			String ownerRoleId = objectPrivilegeTargetInfo.getOwnerRoleId();
			if(list != null
					&& list.size() > 0
					&& modifyUserId != null
					&& modifyUserId.compareTo("") != 0){

				// 登録・更新処理
				for (ObjectPrivilegeInfo info : list) {
					// オーナーロールIDが指定されている場合はスルーする。
					if (ownerRoleId.equals(info.getRoleId())) {
						continue;
					}
					// インスタンスの作成
					ObjectPrivilegeInfoPK infoPk = new ObjectPrivilegeInfoPK(
							objectType,
							objectId,
							info.getRoleId(),
							info.getObjectPrivilege());
					ObjectPrivilegeInfo modifyInfo = null;
					try {
						modifyInfo = QueryUtil.getObjectPrivilegePK(infoPk);
						// 更新する場合、削除対象から除く。
						deleteList.remove(infoPk);
					} catch (PrivilegeNotFound e) {
						// 新規作成
						//ユーザがロールIDに所属しているかチェック
						RoleValidator.validateUserBelongRole(info.getRoleId(),
								(String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID),
								(Boolean)HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR));
						
						modifyInfo = new ObjectPrivilegeInfo(infoPk);
						em.persist(modifyInfo);
					}
					modifyInfo.setCreateUserId(modifyUserId);
					modifyInfo.setCreateDate(HinemosTime.currentTimeMillis());
					modifyInfo.setModifyUserId(modifyUserId);
					modifyInfo.setModifyDate(HinemosTime.currentTimeMillis());
				}
			}

			// 削除処理
			if (deleteList != null && deleteList.size() > 0) {
				List<? extends ObjectPrivilegeTargetInfo> referList = null;
				String referObjectType = null;
				for (ObjectPrivilegeInfoPK deletePk : deleteList) {
					// READの場合、使用されているオブジェクト権限の場合はエラーとする
					if (deletePk.getObjectPrivilege().equals(ObjectPrivilegeMode.READ.name())) {
						if (HinemosModuleConstant.PLATFORM_REPOSITORY.equals(objectType)) {
							/*
							 *  リポジトリ関連（スコープ、ノード）の場合
							 *  (参照権限の継承されている場合、ノードが指定されている場合はチェック不可)
							 */
							// 監視設定
							referList = em.createNamedQuery("MonitorInfo.findByFacilityIdOwnerRoleId", MonitorInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.MONITOR;
								break;
							}

							// 監視設定(相関係数監視 参照先スコープ)
							referList = em.createNamedQuery("MonitorInfo.findByCorrelationFacilityIdOwnerRoleId", MonitorInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.MONITOR;
								break;
							}

							// 監視設定(収集値統合監視 参照先スコープ)
							referList = em.createNamedQuery("MonitorInfo.findByIntegrationFacilityIdOwnerRoleId", MonitorInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.MONITOR;
								break;
							}

							// ジョブ
							referList = em.createNamedQuery("JobMstEntity.findByFacilityIdOwnerRoleId", JobMstEntity.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.JOB;
								break;
							}

							// ファイル転送ジョブ(転送スコープ、受信スコープ)
							referList = em.createNamedQuery("JobMstEntity.findByFileFacilityIdOwnerRoleId", JobMstEntity.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.JOB;
								break;
							}

							// ジョブ実行契機
							referList = em.createNamedQuery("JobKickEntity.findByFacilityIdOwnerRoleId", JobKickEntity.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.JOB_KICK;
								break;
							}

							// ジョブ連携送信設定
							referList = em.createNamedQuery("JobLinkSendSettingEntity.findByFacilityIdOwnerRoleId", JobLinkSendSettingEntity.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.JOB_LINK_SEND;
								break;
							}

							// 通知（ログエスカレーション）
							referList = em.createNamedQuery("NotifyInfoEntity.findByEscalateFacilityIdOwnerRoleId", NotifyInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.PLATFORM_NOTIFY;
								break;
							}

							// 通知（ジョブ）
							referList = em.createNamedQuery("NotifyInfoEntity.findByExecFacilityIdOwnerRoleId", NotifyInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.PLATFORM_NOTIFY;
								break;
							}

							// 通知（環境構築）
							referList = em.createNamedQuery("NotifyInfoEntity.findByInfraFacilityIdOwnerRoleId", NotifyInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.PLATFORM_NOTIFY;
								break;
							}

							// 環境構築
							referList = em.createNamedQuery("InfraManagementInfo.findByFacilityIdOwnerRoleId", InfraManagementInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.INFRA;
								break;
							}

							// レポーティングスケジュール
							referList = em.createNamedQuery("ReportingInfoEntity.findByFacilityIdOwnerRoleId", ReportingInfoEntity.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.REPORTING;
								break;
							}

							// 構成情報取得設定
							referList = em.createNamedQuery("NodeConfigSettingInfo.findByFacilityIdOwnerRoleId", NodeConfigSettingInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.NODE_CONFIG_SETTING;
								break;
							}

							// SDML制御設定
							referList = em.createNamedQuery("SdmlControlSettingInfo.findByFacilityIdOwnerRoleId", SdmlControlSettingInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.SDML_CONTROL;
								break;
							}

							// RPAシナリオ実績作成設定
							referList = em.createNamedQuery("RpaScenarioOperationResultCreateSetting.findByFacilityIdOwnerRoleId", RpaScenarioOperationResultCreateSetting.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.RPA_SCENARIO_CREATE;
								break;
							}

						} else if (HinemosModuleConstant.JOB.equals(objectType)) {
							/*
							 * ジョブの場合
							 */
							// ジョブ実行契機
							referList = em.createNamedQuery("JobKickEntity.findByJobUnitIdOwnerRoleId", JobKickEntity.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.JOB_KICK;
								break;
							}

							// 通知（ジョブ）
							referList = em.createNamedQuery("NotifyInfoEntity.findByJobUnitIdOwnerRoleId", NotifyInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.PLATFORM_NOTIFY;
								break;
							}
							
							// ジョブ(承認ジョブ)
							referList = em.createNamedQuery("JobMstEntity.findByJobUnitIdApprovalReqRoleId", JobMstEntity.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("roleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.JOB_MST;
								break;
							}

						} else if (HinemosModuleConstant.PLATFORM_CALENDAR.equals(objectType)) {
							/*
							 *  カレンダの場合
							 */
							// 監視設定
							referList = em.createNamedQuery("MonitorInfo.findByCalendarIdOwnerRoleId", MonitorInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.MONITOR;
								break;
							}

							// ジョブ
							referList = em.createNamedQuery("JobMstEntity.findByCalendarIdOwnerRoleId", JobMstEntity.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.JOB;
								break;
							}

							// ジョブ実行契機
							referList = em.createNamedQuery("JobKickEntity.findByCalendarIdOwnerRoleId", JobKickEntity.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.JOB_KICK;
								break;
							}

							// レポーティングスケジュール
							referList = em.createNamedQuery("ReportingInfoEntity.findByCalendarIdOwnerRoleId", ReportingInfoEntity.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.REPORTING;
								break;
							}

							// 通知設定
							referList = em.createNamedQuery("NotifyInfoEntity.findByCalendarIdOwnerRoleId", NotifyInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.PLATFORM_NOTIFY;
								break;
							}

							// 履歴情報削除
							referList = em.createNamedQuery("MaintenanceInfo.findByCalendarIdOwnerRoleId", MaintenanceInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.SYSYTEM_MAINTENANCE;
								break;
							}

							// 転送設定
							referList = em.createNamedQuery("TransferInfo.findByCalendarIdOwnerRoleId", TransferInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.HUB_TRANSFER;
								break;
							}

							// 構成情報取得設定
							referList = em.createNamedQuery("NodeConfigSettingInfo.findByCalendarIdOwnerRoleId", NodeConfigSettingInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.NODE_CONFIG_SETTING;
								break;
							}

							// SDML制御設定
							referList = em.createNamedQuery("SdmlControlSettingInfo.findByCalendarIdOwnerRoleId", SdmlControlSettingInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.SDML_CONTROL;
								break;
							}

							// RPAシナリオ実績作成設定
							referList = em.createNamedQuery("RpaScenarioOperationResultCreateSetting.findByCalendarIdOwnerRoleId", RpaScenarioOperationResultCreateSetting.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.RPA_SCENARIO_CREATE;
								break;
							}

						} else if (HinemosModuleConstant.PLATFORM_CALENDAR_PATTERN.equals(objectType)) {
							/*
							 * カレンダパターンの場合
							 */
							// カレンダ
							referList = em.createNamedQuery("CalInfoEntity.findByCalendarPatternIdOwnerRoleId", CalendarInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.PLATFORM_CALENDAR;
								break;
							}

						} else if (HinemosModuleConstant.PLATFORM_NOTIFY.equals(objectType)) {
							/*
							 *  通知の場合
							 */
							// 監視設定
							referList = em.createNamedQuery("MonitorInfo.findByNotifyIdOwnerRoleId", MonitorInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.MONITOR;
								break;
							}

							// 変化量監視設定
							referList = em.createNamedQuery("MonitorInfo.findByOtherNotifyIdOwnerRoleId", MonitorInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.setParameter("otherKey", CollectMonitorNotifyConstant.CHANGE_NOTIFY_GROUPID_PREFIX)
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.MONITOR;
								break;
							}

							// 将来予測監視設定
							referList = em.createNamedQuery("MonitorInfo.findByOtherNotifyIdOwnerRoleId", MonitorInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.setParameter("otherKey", CollectMonitorNotifyConstant.PREDICTION_NOTIFY_GROUPID_PREFIX)
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.MONITOR;
								break;
							}

							// ジョブ
							referList = em.createNamedQuery("JobMstEntity.findByNotifyIdOwnerRoleId", JobMstEntity.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.JOB;
								break;
							}

							// 環境構築設定
							referList = em.createNamedQuery("InfraManagementInfo.findByNotifyIdOwnerRoleId", InfraManagementInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.INFRA;
								break;
							}

							// 履歴情報削除
							referList = em.createNamedQuery("MaintenanceInfo.findByNotifyIdOwnerRoleId", MaintenanceInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.SYSYTEM_MAINTENANCE;
								break;
							}

							// レポーティングスケジュール
							referList = em.createNamedQuery("ReportingInfoEntity.findByNotifyIdOwnerRoleId", ReportingInfoEntity.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.REPORTING;
								break;
							}

							// 構成情報取得設定
							referList = em.createNamedQuery("NodeConfigSettingInfo.findByNotifyIdOwnerRoleId", NodeConfigSettingInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.NODE_CONFIG_SETTING;
								break;
							}

							// SDML制御設定
							referList = em.createNamedQuery("SdmlControlSettingInfo.findByNotifyIdOwnerRoleId", SdmlControlSettingInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.SDML_CONTROL;
								break;
							}
							referList = em.createNamedQuery("SdmlControlSettingInfo.findByAutoMonitorCommonNotifyIdOwnerRoleId", SdmlControlSettingInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.SDML_CONTROL;
								break;
							}
							referList = em.createNamedQuery("SdmlControlSettingInfo.findByAutoMonitorIndividualNotifyIdOwnerRoleId", SdmlControlSettingInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.SDML_CONTROL;
								break;
							}
							// RPAシナリオ実績作成
							referList = em.createNamedQuery("RpaScenarioOperationResultCreateSetting.findByNotifyIdOwnerRoleId", RpaScenarioOperationResultCreateSetting.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.RPA_SCENARIO_CREATE;
								break;
							}

						} else if (HinemosModuleConstant.MONITOR.equals(objectType)) {
							/*
							 *  監視の場合
							 */
							// 監視設定
							referList = em.createNamedQuery("MonitorInfo.findByCorrelationMonitorIdOwnerRoleId", MonitorInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if (referList.size() == 0) {
								referList = em.createNamedQuery("MonitorInfo.findByIntegrationMonitorIdOwnerRoleId", MonitorInfo.class, ObjectPrivilegeMode.NONE)
										.setParameter("objectId", objectId)
										.setParameter("ownerRoleId", deletePk.getRoleId())
										.getResultList();
							}
							if (referList.size() == 0) {
								referList = em.createNamedQuery("MonitorInfo.findByLogcountMonitorIdOwnerRoleId", MonitorInfo.class, ObjectPrivilegeMode.NONE)
										.setParameter("objectId", objectId)
										.setParameter("ownerRoleId", deletePk.getRoleId())
										.getResultList();
							}
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.MONITOR;
								break;
							}

						} else if (HinemosModuleConstant.PLATFORM_MAIL_TEMPLATE.equals(objectType)) {
							/*
							 *  メールテンプレート
							 */
							// メール通知
							referList = em.createNamedQuery("NotifyInfoEntity.findByMailTemplateIdOwnerRoleId", NotifyInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							referObjectType = HinemosModuleConstant.PLATFORM_NOTIFY;

						} else if (HinemosModuleConstant.INFRA.equals(objectType)) {
							/*
							 *  環境構築設定
							 */
							// 通知設定
							referList = em.createNamedQuery("NotifyInfoEntity.findByInfraManagementIdOwnerRoleId", NotifyInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							referObjectType = HinemosModuleConstant.PLATFORM_NOTIFY;

						} else if (HinemosModuleConstant.INFRA_FILE.equals(objectType)) {
							/*
							 *  環境構築ファイル
							 */
							// 環境構築設定
							referList = em.createNamedQuery("InfraManagementInfo.findByInfraFileIdOwnerRoleId", InfraManagementInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							referObjectType = HinemosModuleConstant.PLATFORM_NOTIFY;

						} else if (HinemosModuleConstant.JOBMAP_IMAGE_FILE.equals(objectType)) {
							/*
							 *  ジョブマップイメージ
							 */
							// ジョブ設定
							referList = em.createNamedQuery("JobMstEntity.findByJobmapIconIdOwnerRoleId", JobMstEntity.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							referObjectType = HinemosModuleConstant.JOB;

						} else if (HinemosModuleConstant.JOB_QUEUE.equals(objectType)) {
							/*
							 * ジョブキュー
							 */
							// ジョブ設定
							referList = em.createNamedQuery("JobMstEntity.findByQueueIdOwnerRoleId", JobMstEntity.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.JOB;
								break;
							}
						} else if (HinemosModuleConstant.JOB_LINK_SEND.equals(objectType)) {
							/*
							 * ジョブ連携送信設定
							 */
							// ジョブ設定
							referList = em.createNamedQuery("JobMstEntity.findByJoblinkSendSettingIdOwnerRoleId", JobMstEntity.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.JOB;
								break;
							}

							// 通知（ジョブ）
							referList = em.createNamedQuery("NotifyInfoEntity.findByJoblinkSendSettingIdOwnerRoleId", NotifyInfo.class, ObjectPrivilegeMode.NONE)
									.setParameter("objectId", objectId)
									.setParameter("ownerRoleId", deletePk.getRoleId())
									.getResultList();
							if(referList.size() > 0) {
								referObjectType = HinemosModuleConstant.PLATFORM_NOTIFY;
								break;
							}
						}
					}
					ObjectPrivilegeInfo deleteInfo = null;
					try {
						deleteInfo = QueryUtil.getObjectPrivilegePK(deletePk);
						em.remove(deleteInfo);
					} catch (PrivilegeNotFound e) {
						// データが存在しない場合は特に処理しない。
						m_log.debug("ObjectPrivilegeInfo is not found.");
					}
				}
				if (referList != null && referList.size() > 0) {
					UsedObjectPrivilege e = new UsedObjectPrivilege(
							MessageConstant.MESSAGE_ACCESS_OBJECT_PRIVILEGE_DELETE_FAILED.getMessage(referObjectType, referList.get(0).getObjectId()));
					m_log.warn("replaceObjectPrivilegeInfo() : "
							+ "objectType = " + referObjectType
							+ ", objectId = " + referList.get(0).getObjectId()
							+ ", " + e.getClass().getSimpleName(), e);
					throw e;
				}
			}

		} catch (UsedObjectPrivilege e) {
			m_log.debug("replaceObjectPrivilegeInfo() failure to add a entity. " + e.getMessage());
			throw e;
		} catch (EntityExistsException e) {
			m_log.debug("replaceObjectPrivilegeInfo() failure to add a entity. " + e.getMessage());
			throw new PrivilegeDuplicate(e.getMessage(), e);
		} catch (InvalidSetting e) {
			m_log.debug("replaceObjectPrivilegeInfo() failure to add a entity. " + e.getMessage());
			throw e;
		} catch (Exception e) {
			m_log.warn("replaceObjectPrivilegeInfo() failure to add a entity. " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
		m_log.info("successful in modifing a entity.");
	}

}
