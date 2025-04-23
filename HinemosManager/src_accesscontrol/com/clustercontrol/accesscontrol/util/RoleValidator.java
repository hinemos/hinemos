/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.ObjectPrivilegeFilterInfo;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.accesscontrol.bean.RoleSettingTreeConstant;
import com.clustercontrol.accesscontrol.factory.RoleSelector;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfo;
import com.clustercontrol.accesscontrol.model.RoleInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
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
import com.clustercontrol.fault.SdmlControlSettingNotFound;
import com.clustercontrol.fault.UsedOwnerRole;
import com.clustercontrol.filtersetting.bean.FilterSettingObjectId;
import com.clustercontrol.filtersetting.entity.FilterEntity;
import com.clustercontrol.hub.model.LogFormat;
import com.clustercontrol.hub.model.TransferInfo;
import com.clustercontrol.infra.model.InfraFileInfo;
import com.clustercontrol.infra.model.InfraManagementInfo;
import com.clustercontrol.jobmanagement.model.JobKickEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntity;
import com.clustercontrol.jobmanagement.model.JobMstEntityPK;
import com.clustercontrol.jobmanagement.model.JobmapIconImageEntity;
import com.clustercontrol.jobmanagement.queue.internal.JobQueueEntity;
import com.clustercontrol.maintenance.model.MaintenanceInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.notify.mail.model.MailTemplateInfo;
import com.clustercontrol.notify.model.NotifyInfo;
import com.clustercontrol.reporting.model.ReportingInfoEntity;
import com.clustercontrol.reporting.model.TemplateSetInfoEntity;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.NodeConfigSettingInfo;
import com.clustercontrol.sdml.model.SdmlControlSettingInfo;
import com.clustercontrol.rpa.scenario.model.RpaScenario;
import com.clustercontrol.rpa.scenario.model.RpaScenarioOperationResultCreateSetting;
import com.clustercontrol.rpa.scenario.model.RpaScenarioTag;
import com.clustercontrol.util.MessageConstant;

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

		// reserved roleId check
		validateReservedRoleId(roleInfo.getRoleId());
	}

	private static void validateReservedRoleId(String roleId) throws InvalidSetting {
		if (roleId.equals(RoleSettingTreeConstant.MANAGER)) {
			String[] args = { RoleSettingTreeConstant.MANAGER };
			InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_NOT_ALLOWED_IN_ROLE.getMessage(args));
			m_log.info(" managerRoleInfo() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
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
		StringBuilder sb = new StringBuilder();
		HashMap<String, List<String>> map = new HashMap<>();
		List<String> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			// リポジトリ（ノード、スコープ）
			list = QueryUtil.getObjectPrivilegeIdsByOwnerRoleId_NONE(FacilityInfo.class, roleId);
			if (list != null && list.size() > 0) {
				map.put(MessageConstant.REPOSITORY.getMessage(), new ArrayList<>(list));
			}

			// 構成情報管理
			list = QueryUtil.getObjectPrivilegeIdsByOwnerRoleId_NONE(NodeConfigSettingInfo.class, roleId);
			if (list != null && list.size() > 0) {
				map.put(MessageConstant.NODE_CONFIG_SETTING.getMessage(), new ArrayList<>(list));
			}

			// 監視設定
			list = QueryUtil.getObjectPrivilegeIdsByOwnerRoleId_NONE(MonitorInfo.class, roleId);
			if (list != null && list.size() > 0) {
				map.put(MessageConstant.MONITOR_SETTING.getMessage(), new ArrayList<>(list));
			}

			// ジョブ
			list = QueryUtil.getObjectPrivilegeIdsByOwnerRoleId_NONE(JobMstEntity.class, roleId);
			if (list != null && list.size() > 0) {
				map.put(MessageConstant.JOB_MANAGEMENT.getMessage(), new ArrayList<>(list));
			}

			// ジョブ実行契機
			list = QueryUtil.getObjectPrivilegeIdsByOwnerRoleId_NONE(JobKickEntity.class, roleId);
			if (list != null && list.size() > 0) {
				map.put(MessageConstant.JOB_KICK_NAME.getMessage(), new ArrayList<>(list));
			}

			// ジョブキュー
			list = QueryUtil.getObjectPrivilegeIdsByOwnerRoleId_NONE(JobQueueEntity.class, roleId);
			if (list != null && list.size() > 0) {
				map.put(MessageConstant.JOB_QUEUE.getMessage(), new ArrayList<>(list));
			}

			// カレンダ
			list = QueryUtil.getObjectPrivilegeIdsByOwnerRoleId_NONE(CalendarInfo.class, roleId);
			if (list != null && list.size() > 0) {
				map.put(MessageConstant.CALENDAR.getMessage(), new ArrayList<>(list));
			}

			// カレンダパターン
			list = QueryUtil.getObjectPrivilegeIdsByOwnerRoleId_NONE(CalendarPatternInfo.class, roleId);
			if (list != null && list.size() > 0) {
				map.put(MessageConstant.CALENDAR_PATTERN.getMessage(), new ArrayList<>(list));
			}

			// 通知
			list = QueryUtil.getObjectPrivilegeIdsByOwnerRoleId_NONE(NotifyInfo.class, roleId);
			if (list != null && list.size() > 0) {
				map.put(MessageConstant.NOTIFY.getMessage(), new ArrayList<>(list));
			}

			// メールテンプレート
			list = QueryUtil.getObjectPrivilegeIdsByOwnerRoleId_NONE(MailTemplateInfo.class, roleId);
			if (list != null && list.size() > 0) {
				map.put(MessageConstant.MAIL_TEMPLATE.getMessage(), new ArrayList<>(list));
			}

			// ログフォーマット
			list = QueryUtil.getObjectPrivilegeIdsByOwnerRoleId_NONE(LogFormat.class, roleId);
			if (list != null && list.size() > 0) {
				map.put(MessageConstant.LOG_FORMAT.getMessage(), new ArrayList<>(list));
			}

			// 環境構築
			list = QueryUtil.getObjectPrivilegeIdsByOwnerRoleId_NONE(InfraManagementInfo.class, roleId);
			if (list != null && list.size() > 0) {
				map.put(MessageConstant.INFRA_MANAGEMENT.getMessage(), new ArrayList<>(list));
			}

			// 環境構築ファイル
			list = QueryUtil.getObjectPrivilegeIdsByOwnerRoleId_NONE(InfraFileInfo.class, roleId);
			if (list != null && list.size() > 0) {
				map.put(MessageConstant.INFRA_FILE.getMessage(), new ArrayList<>(list));
			}

			// 履歴情報削除設定
			list = QueryUtil.getObjectPrivilegeIdsByOwnerRoleId_NONE(MaintenanceInfo.class, roleId);
			if (list != null && list.size() > 0) {
				map.put(MessageConstant.HISTORY_DELETE.getMessage(), new ArrayList<>(list));
			}

			// 転送設定 (収集蓄積機能)
			list = QueryUtil.getObjectPrivilegeIdsByOwnerRoleId_NONE(TransferInfo.class, roleId);
			if (list != null && list.size() > 0) {
				map.put(MessageConstant.HUB_TRANSFER_SETTING.getMessage(), new ArrayList<>(list));
			}

			// SDML制御設定
			list = QueryUtil.getObjectPrivilegeIdsByOwnerRoleId_NONE(SdmlControlSettingInfo.class, roleId);
			if (list != null && list.size() > 0) {
				map.put(MessageConstant.SDML_CONTROL_SETTING.getMessage(), new ArrayList<>(list));
			}

			// アイコンイメージ (ジョブマップオプション)
			list = QueryUtil.getObjectPrivilegeIdsByOwnerRoleId_NONE(JobmapIconImageEntity.class, roleId);
			if (list != null && list.size() > 0) {
				map.put(MessageConstant.JOBMAP_ICON_IMAGE.getMessage(), new ArrayList<>(list));
			}

			// スケジュール (レポーティングオプション)
			list = QueryUtil.getObjectPrivilegeIdsByOwnerRoleId_NONE(ReportingInfoEntity.class, roleId);
			if (list != null && list.size() > 0) {
				map.put(MessageConstant.REPORTING_SCHEDULE.getMessage(), new ArrayList<>(list));
			}

			// テンプレートセット (レポーティングオプション)
			list = QueryUtil.getObjectPrivilegeIdsByOwnerRoleId_NONE(TemplateSetInfoEntity.class, roleId);
			if (list != null && list.size() > 0) {
				map.put(MessageConstant.REPORTING_TEMPLATE.getMessage(), new ArrayList<>(list));
			}

			// フィルタ設定
			list = QueryUtil.getObjectPrivilegeIdsByOwnerRoleId_NONE(FilterEntity.class, roleId);
			if (list != null && list.size() > 0) {
				List<String> readables = new ArrayList<>();
				for (String objectId : list) {
					readables.add(new FilterSettingObjectId(objectId).toMessage());
				}
				map.put(MessageConstant.FILTER_SETTING.getMessage(), readables);
			}

			
			// RPAシナリオ
			list = QueryUtil.getObjectPrivilegeIdsByOwnerRoleId_NONE(RpaScenario.class, roleId);
			if (list != null && list.size() > 0) {
				map.put(MessageConstant.RPA_SCENARIO.getMessage(), new ArrayList<>(list));
			}

			// RPAシナリオタグ
			list = QueryUtil.getObjectPrivilegeIdsByOwnerRoleId_NONE(RpaScenarioTag.class, roleId);
			if (list != null && list.size() > 0) {
				map.put(MessageConstant.RPA_SCENARIO_TAG.getMessage(), new ArrayList<>(list));
			}

			// RPAシナリオ実績作成設定
			list = QueryUtil.getObjectPrivilegeIdsByOwnerRoleId_NONE(RpaScenarioOperationResultCreateSetting.class, roleId);
			if (list != null && list.size() > 0) {
				map.put(MessageConstant.RPA_SCENARIO_OPERATION_RESULT_CREATE_SETTING.getMessage(), new ArrayList<>(list));
			}

			
			// オーナーロールIDに指定されている場合に例外発生
			if (!map.isEmpty()) {
				for (Map.Entry<String, List<String>> entry : map.entrySet()) {
					sb.append(entry.getKey() + " : ");
					sb.append(entry.getValue().stream().collect(Collectors.joining(",")));
					sb.append("\n");
				}
				m_log.info("validateDeleteRole,[" + roleId + "] : " + sb.toString());
				throw new UsedOwnerRole(0, sb.toString());
			}

			jtm.commit();
		} catch (UsedOwnerRole e) {
			jtm.rollback();
			e.setRoleId(roleId);
			throw e;
		} catch (Exception e) {
			m_log.warn("validateDeleteRole() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null) {
				jtm.rollback();
			}
			throw new HinemosUnknown(e.getMessage(),e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}

	/**
	 * オブジェクト権限で使用されているロールかどうか確認する。
	 * 参照状態の場合、メッセージダイアログが出力される。
	 * @param roleId
	 * @throw UsedRole
	 * @throw HinemosUnknown
	 */
	public static void checkHavingObjectPrivilege(String roleId) throws UsedOwnerRole, HinemosUnknown {
		try {
			ObjectPrivilegeFilterInfo filter = new ObjectPrivilegeFilterInfo();
			filter.setRoleId(roleId);
			ArrayList<ObjectPrivilegeInfo> list = RoleSelector.getObjectPrivilegeInfoList(filter);
			m_log.debug("Check ObjectPrivileges , Role [" + roleId + "]");

			// オブジェクト権限がない場合、例外スローなしでリターン
			if (list == null || list.size() == 0) {
				return;
			}

			// オブジェクト権限があればUsedOwnerRoleの例外をスローする
			// objectType, objectIdを格納する変数にはTreeMap, TreeSetを用いる
			// (メッセージ内容の表示順が変動しないようにするため)
			StringBuilder sb = new StringBuilder();
			Map<String, Set<String>> map = new TreeMap<>();

			// オブジェクト権限のobjectTypeごとにobjectIdをまとめる
			for (ObjectPrivilegeInfo entry : list) {
				if (!map.containsKey(entry.getObjectType())) {
					map.put(entry.getObjectType(), new TreeSet<String>());
				}
				map.get(entry.getObjectType()).add(entry.getObjectId());
			}

			// ダイアログのメッセージの文字列を作成する
			for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
				String str = "";
				switch (entry.getKey()) {
					// リポジトリ（スコープ）
					case HinemosModuleConstant.PLATFORM_REPOSITORY:
						str = MessageConstant.REPOSITORY.getMessage();
						break;
					// 監視設定
					case HinemosModuleConstant.MONITOR:
						str = MessageConstant.MONITOR_SETTING.getMessage();
						break;
					// ログフォーマット
					case HinemosModuleConstant.HUB_LOGFORMAT:
						str = MessageConstant.LOG_FORMAT.getMessage();
						break;
					// ジョブ
					case HinemosModuleConstant.JOB:
						str = MessageConstant.JOB_MANAGEMENT.getMessage();
						break;
					// ジョブ実行契機
					case HinemosModuleConstant.JOB_KICK:
						str = MessageConstant.JOB_KICK_NAME.getMessage();
						break;
					// ジョブキュー
					case HinemosModuleConstant.JOB_QUEUE:
						str = MessageConstant.JOB_QUEUE.getMessage();
						break;
					// 環境構築
					case HinemosModuleConstant.INFRA:
						str = MessageConstant.INFRA_MANAGEMENT.getMessage();
						break;
					// 環境構築ファイル
					case HinemosModuleConstant.INFRA_FILE:
						str = MessageConstant.INFRA_FILE.getMessage();
						break;
					// カレンダ
					case HinemosModuleConstant.PLATFORM_CALENDAR:
						str = MessageConstant.CALENDAR.getMessage();
						break;
					// カレンダパターン
					case HinemosModuleConstant.PLATFORM_CALENDAR_PATTERN:
						str = MessageConstant.CALENDAR_PATTERN.getMessage();
						break;
					// 通知
					case HinemosModuleConstant.PLATFORM_NOTIFY:
						str = MessageConstant.NOTIFY.getMessage();
						break;
					// メールテンプレート
					case HinemosModuleConstant.PLATFORM_MAIL_TEMPLATE:
						str = MessageConstant.MAIL_TEMPLATE.getMessage();
						break;
					// 履歴情報削除設定
					case HinemosModuleConstant.SYSYTEM_MAINTENANCE:
						str = MessageConstant.HISTORY_DELETE.getMessage();
						break;
					// 転送設定 (収集蓄積機能)
					case HinemosModuleConstant.HUB_TRANSFER:
						str = MessageConstant.HUB_TRANSFER_SETTING.getMessage();
						break;
					// SDML制御設定
					case HinemosModuleConstant.SDML_CONTROL:
						str = MessageConstant.SDML_CONTROL_SETTING.getMessage();
						break;
					// アイコンイメージ (ジョブマップ)
					case HinemosModuleConstant.JOBMAP_IMAGE_FILE:
						str = MessageConstant.JOBMAP_ICON_IMAGE.getMessage();
						break;
					// フィルタ設定
					case HinemosModuleConstant.FILTER_SETTING:
						str = MessageConstant.FILTER_SETTING.getMessage();
						break;
					// RPAシナリオ実績作成設定
					case HinemosModuleConstant.RPA_SCENARIO_CREATE:
						str = MessageConstant.RPA_SCENARIO_OPERATION_RESULT_CREATE_SETTING.getMessage();
						break;
					// RPAシナリオ
					case HinemosModuleConstant.RPA_SCENARIO:
						str = MessageConstant.RPA_SCENARIO.getMessage();
						break;						
					// objectTypeがオブジェクト権限の設定できるものでない場合
					default:
						m_log.info("checkHavingObjectPrivilege, [" + roleId + "] : "
									+ "Illegal objectType [" + entry.getKey() + "] of "
									+ String.join(",", entry.getValue()));
						str = entry.getKey();
				}
				sb.append(str + " : ");
				sb.append(String.join(",", entry.getValue()));
				sb.append("\n");
			}

			m_log.info("checkHavingObjectPrivilege, [" + roleId + "] : " + sb.toString());
			throw new UsedOwnerRole(0, sb.toString());
		} catch (UsedOwnerRole e) {
			e.setRoleId(roleId);
			throw e;
		} catch (Exception e) {
			m_log.warn("checkHavingObjectPrivilege() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
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

			// フィルタ設定
			else if (objectType.equals(HinemosModuleConstant.FILTER_SETTING)) {

				m_log.debug("validateModifyOwnerRole() : FilterEntity check.");
				HinemosEntityManager em = jtm.getEntityManager();
				FilterEntity info = em.find(FilterEntity.class, pk, ObjectPrivilegeMode.READ);
				if (info == null) {
					// 設定が見つからない場合は新規登録と判断し、何もしない
				}

				// 既存の設定のオーナーロールから変更されている場合
				if (info != null && !info.getOwnerRoleId().equals(ownerRoleId)) {
					throw ise;
				}
			}

			// SDML制御設定
			else if (objectType.equals(HinemosModuleConstant.SDML_CONTROL)) {

				m_log.debug("validateModifyOwnerRole() : SdmlControlSettingInfoEntity check.");
				SdmlControlSettingInfo info = null;

				try {
					info = com.clustercontrol.sdml.util.QueryUtil.getSdmlControlSettingInfoPK((String)pk);
				} catch (SdmlControlSettingNotFound e) {
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
