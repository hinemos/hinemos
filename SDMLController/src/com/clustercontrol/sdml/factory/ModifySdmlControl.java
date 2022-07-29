/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.FunctionPrefixEnum;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.NotifyGroupIdGenerator;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.fault.SdmlControlSettingDuplicate;
import com.clustercontrol.fault.SdmlControlSettingNotFound;
import com.clustercontrol.monitor.session.MonitorSettingControllerBean;
import com.clustercontrol.notify.factory.ModifyNotifyRelation;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.notify.session.NotifyControllerBean;
import com.clustercontrol.sdml.model.SdmlControlMonitorRelation;
import com.clustercontrol.sdml.model.SdmlControlSettingInfo;
import com.clustercontrol.sdml.model.SdmlMonitorNotifyRelation;
import com.clustercontrol.sdml.model.SdmlMonitorNotifyRelationPK;
import com.clustercontrol.sdml.util.ControlStatusUtil;
import com.clustercontrol.sdml.util.InitializeDataUtil;
import com.clustercontrol.sdml.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;

import jakarta.persistence.EntityExistsException;

/**
 * SDMLの自動制御に関する設定を変更するクラス
 *
 */
public class ModifySdmlControl {
	private static Log logger = LogFactory.getLog(ModifySdmlControl.class);

	/**
	 * SDML制御設定情報を追加します。
	 * 
	 * @param info
	 * @param user
	 * @return
	 * @throws SdmlControlSettingDuplicate
	 * @throws HinemosUnknown
	 */
	public boolean addControlSetting(SdmlControlSettingInfo info, String user)
			throws SdmlControlSettingDuplicate, HinemosUnknown {
		logger.debug("addControlSetting() : " + "applicationId = " + info.getApplicationId());

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// 現在日時を取得
			long now = HinemosTime.currentTimeMillis();

			// 重複チェック
			jtm.checkEntityExists(SdmlControlSettingInfo.class, info.getApplicationId());

			// TODO ver7.0.1では暫定対処として常に削除する設定とする
			info.setAutoMonitorDeleteFlg(true);

			info.setNotifyGroupId(NotifyGroupIdGenerator.generate(info));
			info.setAutoMonitorCommonNotifyGroupId(NotifyGroupIdGenerator.generate(info, true));
			info.setRegDate(now);
			info.setRegUser(user);
			info.setUpdateDate(now);
			info.setUpdateUser(user);
			em.persist(info);

			ModifyNotifyRelation notifyModifier = new ModifyNotifyRelation();
			// 通知情報の登録
			if (info.getNotifyRelationList() != null && info.getNotifyRelationList().size() > 0) {
				for (NotifyRelationInfo notifyRelationInfo : info.getNotifyRelationList()) {
					notifyRelationInfo.setNotifyGroupId(info.getNotifyGroupId());
					notifyRelationInfo.setFunctionPrefix(FunctionPrefixEnum.SDML_CONTROL.name());
				}
				// 通知情報を登録
				notifyModifier.add(info.getNotifyRelationList());
			}

			// 自動作成監視用共通通知情報の登録
			if (info.getAutoMonitorCommonNotifyRelationList() != null
					&& info.getAutoMonitorCommonNotifyRelationList().size() > 0) {
				for (NotifyRelationInfo notifyRelationInfo : info.getAutoMonitorCommonNotifyRelationList()) {
					notifyRelationInfo.setNotifyGroupId(info.getAutoMonitorCommonNotifyGroupId());
					notifyRelationInfo.setFunctionPrefix(FunctionPrefixEnum.SDML_CONTROL.name());
				}
				// 自動作成監視用共通通知情報を登録
				notifyModifier.add(info.getAutoMonitorCommonNotifyRelationList());
			}

			// 自動作成監視用通知設定の登録
			List<SdmlMonitorNotifyRelation> list = info.getSdmlMonitorNotifyRelationList();
			if (list != null) {
				for (int index = 0; index < list.size(); index++) {
					SdmlMonitorNotifyRelation relationInfo = list.get(index);
					relationInfo.setApplicationId(info.getApplicationId());
					relationInfo.setNotifyGroupId(NotifyGroupIdGenerator.generate(relationInfo));
					em.persist(relationInfo);
					relationInfo.relateToSdmlControlSettingInfo(info);

					// 種別ごとの通知情報の登録
					if (relationInfo.getNotifyRelationList() != null
							&& relationInfo.getNotifyRelationList().size() > 0) {
						for (NotifyRelationInfo notifyRelationInfo : relationInfo.getNotifyRelationList()) {
							notifyRelationInfo.setNotifyGroupId(relationInfo.getNotifyGroupId());
							notifyRelationInfo.setFunctionPrefix(FunctionPrefixEnum.SDML_CONTROL.name());
						}
						// 通知情報を登録
						notifyModifier.add(relationInfo.getNotifyRelationList());
					}
				}
			}
		} catch (EntityExistsException e) {
			logger.info("addControlSetting() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new SdmlControlSettingDuplicate(e.getMessage(), e);
		} catch (Exception e) {
			logger.warn("addControlSetting() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
		return true;
	}

	/**
	 * SDML制御設定情報を変更します。
	 * 
	 * @param info
	 * @param user
	 * @return
	 * @throws SdmlControlSettingNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public boolean modifyControlSetting(SdmlControlSettingInfo info, String user)
			throws SdmlControlSettingNotFound, InvalidRole, HinemosUnknown {
		logger.debug("modifyControlSetting() : " + "applicationId = " + info.getApplicationId());

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// 現在日時を取得
			long now = HinemosTime.currentTimeMillis();

			SdmlControlSettingInfo entity = QueryUtil.getSdmlControlSettingInfoPK(info.getApplicationId(),
					ObjectPrivilegeMode.MODIFY);

			entity.setDescription(info.getDescription());
			entity.setFacilityId(info.getFacilityId());
			entity.setControlLogCollectFlg(info.getControlLogCollectFlg());
			entity.setApplication(info.getApplication());
			entity.setValidFlg(info.getValidFlg());
			// TODO ver7.0.1では暫定対処として常に削除する設定とする
			entity.setAutoMonitorDeleteFlg(true);
			entity.setAutoMonitorCalendarId(info.getAutoMonitorCalendarId());
			entity.setEarlyStopThresholdSecond(info.getEarlyStopThresholdSecond());
			entity.setEarlyStopNotifyPriority(info.getEarlyStopNotifyPriority());
			entity.setAutoCreateSuccessPriority(info.getAutoCreateSuccessPriority());
			entity.setAutoEnableSuccessPriority(info.getAutoEnableSuccessPriority());
			entity.setAutoDisableSuccessPriority(info.getAutoDisableSuccessPriority());
			entity.setAutoUpdateSuccessPriority(info.getAutoUpdateSuccessPriority());
			entity.setAutoControlFailedPriority(info.getAutoControlFailedPriority());
			entity.setUpdateDate(now);
			entity.setUpdateUser(user);

			entity.setNotifyGroupId(NotifyGroupIdGenerator.generate(info));
			entity.setAutoMonitorCommonNotifyGroupId(NotifyGroupIdGenerator.generate(info, true));

			NotifyControllerBean notifyControllerBean = new NotifyControllerBean();
			// 通知情報を更新
			if (info.getNotifyRelationList() != null && info.getNotifyRelationList().size() > 0) {
				for (NotifyRelationInfo notifyRelationInfo : info.getNotifyRelationList()) {
					notifyRelationInfo.setNotifyGroupId(entity.getNotifyGroupId());
					notifyRelationInfo.setFunctionPrefix(FunctionPrefixEnum.SDML_CONTROL.name());
				}
			}
			notifyControllerBean.modifyNotifyRelation(info.getNotifyRelationList(), entity.getNotifyGroupId());

			// 自動作成監視用共通通知情報を更新
			if (info.getAutoMonitorCommonNotifyRelationList() != null
					&& info.getAutoMonitorCommonNotifyRelationList().size() > 0) {
				for (NotifyRelationInfo notifyRelationInfo : info.getAutoMonitorCommonNotifyRelationList()) {
					notifyRelationInfo.setNotifyGroupId(entity.getAutoMonitorCommonNotifyGroupId());
					notifyRelationInfo.setFunctionPrefix(FunctionPrefixEnum.SDML_CONTROL.name());
				}
			}
			notifyControllerBean.modifyNotifyRelation(info.getAutoMonitorCommonNotifyRelationList(),
					entity.getAutoMonitorCommonNotifyGroupId());

			// 自動作成監視用個別通知の更新
			if (info.getSdmlMonitorNotifyRelationList() != null) {
				SdmlMonitorNotifyRelation relationInfoEntity = null;
				List<SdmlMonitorNotifyRelationPK> relationInfoPkList = new ArrayList<>();
				SdmlMonitorNotifyRelationPK relationInfoPk = null;
				for (SdmlMonitorNotifyRelation relationInfo : info.getSdmlMonitorNotifyRelationList()) {
					relationInfoPk = new SdmlMonitorNotifyRelationPK(info.getApplicationId(),
							relationInfo.getSdmlMonitorTypeId());
					try {
						relationInfoEntity = QueryUtil.getSdmlMonitorNotifyRelationPK(relationInfoPk);
					} catch (SdmlControlSettingNotFound e) {
						relationInfoEntity = new SdmlMonitorNotifyRelation(relationInfoPk);
						relationInfoEntity.setNotifyGroupId(NotifyGroupIdGenerator.generate(relationInfoEntity));
						em.persist(relationInfoEntity);
						relationInfoEntity.relateToSdmlControlSettingInfo(entity);
					}
					// 通知情報を更新
					if (relationInfo.getNotifyRelationList() != null
							&& relationInfo.getNotifyRelationList().size() > 0) {
						for (NotifyRelationInfo notifyRelationInfo : relationInfo.getNotifyRelationList()) {
							notifyRelationInfo.setNotifyGroupId(relationInfoEntity.getNotifyGroupId());
							notifyRelationInfo.setFunctionPrefix(FunctionPrefixEnum.SDML_CONTROL.name());
						}
					}
					notifyControllerBean.modifyNotifyRelation(relationInfo.getNotifyRelationList(),
							relationInfoEntity.getNotifyGroupId());

					relationInfoPkList.add(relationInfoPk);
				}
				// 不要なSdmlMonitorNotifyRelationを削除
				for (SdmlMonitorNotifyRelation relationEntity : entity.getSdmlMonitorNotifyRelationList()) {
					if (!relationInfoPkList.contains(relationEntity.getId())) {
						// 先に通知関連情報を削除
						// ループで削除を実施しているが、最大でもSDML監視種別数分しか処理されないので性能的に大きな問題はない
						notifyControllerBean.deleteNotifyRelation(relationEntity.getNotifyGroupId());
					}
				}
				entity.deleteSdmlMonitorNotifyRelationList(relationInfoPkList);
			}

			// 設定が無効の場合
			if (!entity.getValidFlg().booleanValue()) {
				// 自動作成された監視設定を削除する
				deleteAutoCreatedMonitorAll(entity.getApplicationId());
				// 蓄積情報を削除する
				InitializeDataUtil.clearAll(entity.getApplicationId());
				// 自動制御の状態を削除する
				ControlStatusUtil.clearAll(entity.getApplicationId());
			}

		} catch (NotifyNotFound e) {
			logger.warn("modifyControlSetting() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new SdmlControlSettingNotFound(e.getMessage(), e);
		} catch (SdmlControlSettingNotFound e) {
			throw e;
		} catch (InvalidRole e) {
			throw e;
		}
		return true;
	}

	/**
	 * SDML制御設定情報を削除します。
	 * 
	 * @param applicationId
	 * @return
	 * @throws SdmlControlSettingNotFound
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public boolean deleteControlSetting(String applicationId)
			throws SdmlControlSettingNotFound, InvalidRole, HinemosUnknown {
		logger.debug("delete() : " + "applicationId = " + applicationId);

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// SDML制御設定情報を取得
			SdmlControlSettingInfo entity = QueryUtil.getSdmlControlSettingInfoPK(applicationId,
					ObjectPrivilegeMode.MODIFY);

			// 通知グループ情報を削除
			NotifyControllerBean notifyControllerBean = new NotifyControllerBean();
			notifyControllerBean.deleteNotifyRelation(entity.getNotifyGroupId());
			notifyControllerBean.deleteNotifyRelation(entity.getAutoMonitorCommonNotifyGroupId());
			if (entity.getSdmlMonitorNotifyRelationList() != null) {
				for (SdmlMonitorNotifyRelation notifyRelationEntity : entity.getSdmlMonitorNotifyRelationList()) {
					notifyControllerBean.deleteNotifyRelation(notifyRelationEntity.getNotifyGroupId());
				}
			}

			// SDML制御設定情報を削除
			em.remove(entity);

			// 自動作成された監視設定を削除する
			deleteAutoCreatedMonitorAll(entity.getApplicationId());
			// 蓄積情報を削除する
			InitializeDataUtil.clearAll(entity.getApplicationId());
			// 自動制御の状態を削除する
			ControlStatusUtil.clearAll(entity.getApplicationId());

			// 通知履歴情報を削除
			new NotifyControllerBean().deleteNotifyHistory(HinemosModuleConstant.SDML_CONTROL, applicationId);
		}
		return true;
	}

	/**
	 * SDML制御設定と自動作成監視の関連情報を追加します。
	 * 
	 * @param relation
	 * @return
	 * @throws SdmlControlSettingDuplicate
	 * @throws HinemosUnknown
	 */
	public boolean addSdmlControlMonitorRelation(SdmlControlMonitorRelation relation)
			throws SdmlControlSettingDuplicate, HinemosUnknown {
		logger.debug("addSdmlControlMonitorRelation() : " + "applicationId = " + relation.getApplicationId()
				+ ", facilityId = " + relation.getFacilityId() + ", monitorId = " + relation.getMonitorId());

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// 重複チェック
			jtm.checkEntityExists(SdmlControlMonitorRelation.class, relation.getId());

			em.persist(relation);

		} catch (EntityExistsException e) {
			logger.error("addSdmlControlMonitorRelation() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw new SdmlControlSettingDuplicate(e.getMessage(), e);
		} catch (Exception e) {
			logger.warn("addSdmlControlMonitorRelation() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
		return true;
	}

	/**
	 * アプリケーションIDに紐づく自動作成監視の関連情報を全て削除する
	 * 
	 * @param applicationId
	 * @return
	 */
	private boolean deleteAutoCreatedMonitorAll(String applicationId) {
		logger.debug("deleteAutoCreatedMonitorAll() : " + "applicationId = " + applicationId);
		// 自動作成監視の関連情報を取得
		List<SdmlControlMonitorRelation> list = QueryUtil.getSdmlControlMonitorRelationByApplicationId(applicationId);
		for (SdmlControlMonitorRelation relation : list) {
			try {
				// 一件ずつ削除する
				deleteAutoCreatedMonitor(relation);
			} catch (MonitorNotFound | InvalidRole | InvalidSetting | HinemosUnknown e) {
				// 削除に失敗した場合は継続
				continue;
			}
		}
		return true;
	}

	/**
	 * 引数で受け取った関連情報に紐づく監視設定を削除し、関連情報自体も削除する
	 * 
	 * @param relation
	 * @return
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 */
	public boolean deleteAutoCreatedMonitor(SdmlControlMonitorRelation relation)
			throws MonitorNotFound, InvalidRole, InvalidSetting, HinemosUnknown {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			try {
				// 監視設定を削除する
				new MonitorSettingControllerBean().deleteMonitor(Arrays.asList(relation.getMonitorId()));
			} catch (MonitorNotFound | InvalidRole | InvalidSetting | HinemosUnknown e) {
				logger.warn("deleteAutoCreatedMonitor() : failed. " + "monitorId = " + relation.getMonitorId());
				// 監視設定の削除に失敗した場合は関連情報だけ削除する
				// NotFound以外は監視設定が残ってしまうことになるが手動で削除するしかない
				em.remove(relation);
				throw e;
			}
			// 関連情報を削除
			em.remove(relation);
		}
		return true;
	}

	/**
	 * 引数で受け取った関連情報を削除する<br>
	 * 紐づく監視設定が既に存在しないことがわかっている場合のみ使用すること<br>
	 * 
	 * @param relation
	 * @return
	 */
	public boolean deleteOnlyControlMonitorRelation(SdmlControlMonitorRelation relation) {
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// 関連情報を削除
			em.remove(relation);
		}
		return true;
	}
}
