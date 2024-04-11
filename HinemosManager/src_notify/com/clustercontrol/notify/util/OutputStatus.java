/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JdbcBatchExecutor;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.monitor.bean.PriorityChangeFailureTypeConstant;
import com.clustercontrol.monitor.bean.StatusExpirationConstant;
import com.clustercontrol.notify.bean.NotifyRequestMessage;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.model.NotifyStatusInfo;
import com.clustercontrol.notify.monitor.model.StatusInfoEntity;
import com.clustercontrol.notify.monitor.model.StatusInfoEntityPK;
import com.clustercontrol.util.HinemosTime;

/**
 * ステータス情報を更新するクラス<BR>
 *
 * @version 3.2.0
 * @since 3.0.0
 */
public class OutputStatus implements DependDbNotifier {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(OutputStatus.class);

	// 無期限収集の場合に設定される終了予定時刻（9999.12.31 23:59:59.000 に相当）
	private final long EXPIRATION_DATE_MAX = 253402268399000l;

	// log.cc_status_info#messageの桁数
	private final int MESSAGE_DEGIT = 255;

	public void updateStatus(OutputBasicInfo status) throws HinemosUnknown {
		NotifyRequestMessage msg = new NotifyRequestMessage();
		msg.setOutputInfo(status);
		msg.setOutputDate(HinemosTime.getDateInstance());
		msg.setNotifyId(null);
		notify(msg);
	}
	
	@Override
	public void notify(NotifyRequestMessage message) throws HinemosUnknown {
		updateStatus(message.getOutputInfo(), message.getNotifyId());
	}

	public void notify(List<NotifyRequestMessage> msgList) throws NotifyNotFound, HinemosUnknown {
		if (msgList.isEmpty()) {
			return;
		}
		NotifyRequestMessage firstMsg = msgList.get(0);
		List<StatusInfoEntity> oldEntityList = QueryUtil.getStatusInfoByPluginIdAndMonitorId(
						firstMsg.getOutputInfo().getPluginId(),
						firstMsg.getOutputInfo()	.getMonitorId());
		Map<StatusInfoEntityPK, StatusInfoEntity> oldEntityMap = new HashMap<StatusInfoEntityPK, StatusInfoEntity>();
		for (StatusInfoEntity oldEntity : oldEntityList) {
			oldEntityMap.put(oldEntity.getId(), oldEntity);
		}

		//一括通知で通知するメッセージは同一ロールなので、1個目のロールIDを利用する。
		String ownerRoleId = NotifyUtil.getOwnerRoleId(firstMsg, false);
		
		NotifyStatusInfo notifyStatus = null;
		if (firstMsg.getNotifyId() != null) {
			notifyStatus = getNotifyStatusInfo(firstMsg);
		}
		
		List<StatusInfoEntity> insertEntityList = new ArrayList<StatusInfoEntity>();
		for (NotifyRequestMessage msg : msgList) {
			outputStatusInfo(
					msg.getOutputInfo(),
					notifyStatus,
					ownerRoleId,
					oldEntityMap,
					insertEntityList);
		}

		if (insertEntityList.size() > 0) {
			JdbcBatchExecutor.execute(new StatusInfoEntityJdbcBatchInsert(insertEntityList));
		}
	}


	private NotifyStatusInfo getNotifyStatusInfo(NotifyRequestMessage msg) throws NotifyNotFound {
		return QueryUtil.getNotifyStatusInfoPK(msg.getNotifyId());
	}

	/**
	 * ステータス情報を更新します。
	 * 更新対象のステータスが存在しない場合は新規に生成します。
	 * 
	 * @param outputInfo 通知情報
	 * @param notifyId 通知ID
	 * @throws HinemosUnknown 
	 */
	private void updateStatus(OutputBasicInfo outputInfo, String notifyId) throws HinemosUnknown {
		NotifyStatusInfo notifyStatusInfo = null;
		
		if (notifyId != null) {
			try {
				notifyStatusInfo = QueryUtil.getNotifyStatusInfoPK(notifyId);
			} catch (NotifyNotFound e) {
				m_log.debug("notify(notifyId=" + notifyId + ") not found.", e);
			}
		}

		outputStatusInfo(outputInfo, notifyStatusInfo, null, null, new ArrayList<StatusInfoEntity>());
	}

	private void outputStatusInfo(OutputBasicInfo outputInfo,
			NotifyStatusInfo notifyStatusInfo,
			String roleId,
			Map<StatusInfoEntityPK, StatusInfoEntity> oldEntityMap,
			List<StatusInfoEntity> insertEntityList) throws HinemosUnknown {
		long outputDateTime = HinemosTime.currentTimeMillis();

		// 有効期限制御フラグを設定（わかりずらい仕様のため要注意）
		// cc_status_info の expirationFlg は、有効期限制御を行うか否かの2値ではなく、
		// 行う場合はどの重要度に置き換えるのかの情報も合わせて管理する。
		Integer expirationFlg = null;
		if (notifyStatusInfo == null || notifyStatusInfo.getStatusInvalidFlg() == StatusExpirationConstant.TYPE_DELETE) {
			expirationFlg = Integer.valueOf(StatusExpirationConstant.TYPE_DELETE);
		}
		else if (notifyStatusInfo.getStatusInvalidFlg() == StatusExpirationConstant.TYPE_UPDATE) {
			// 有効期間経過後に、更新されていない旨のメッセージに置き換える場合は、
			// 置換え後の重要度を設定する
			expirationFlg = notifyStatusInfo.getStatusUpdatePriority();
		}

		// 有効期限日時を設定
		long expirationDateTime = EXPIRATION_DATE_MAX;
		if (notifyStatusInfo != null && expirationFlg != null && notifyStatusInfo.getStatusValidPeriod() > 0) {
			// StatusValidPeriod は分単位であるため、ミリ秒単位に変換する。
			expirationDateTime = outputDateTime + notifyStatusInfo.getStatusValidPeriod() * 60 * 1000l;
		}
		
		if (expirationFlg == null) {
			throw new HinemosUnknown("expirationFlg is null.");
		}

		// 判定による重要度変化の有無によるoutputStatusInfoの作成方法を選択
		if (outputInfo.getPriorityChangeJudgmentType() != null 
				&& outputInfo.getPriorityChangeJudgmentType() == PriorityChangeFailureTypeConstant.TYPE_PRIORITY_CHANGE) {
			// 判定による重要度変化する場合
			outputStatusInfoPriorityChanged(outputInfo, expirationFlg, expirationDateTime, outputDateTime, oldEntityMap, roleId, insertEntityList);
		} else {
			outputStatusInfo(outputInfo, expirationFlg, expirationDateTime, outputDateTime, oldEntityMap, roleId, insertEntityList);
		}
	}

	/**
	 * ステータス情報を出力します。<BR>
	 * 同じステータス情報が存在しない場合は、ステータス情報を作成します。
	 * 同じステータス情報がすでに存在する場合は、ステータス情報を更新します。
	 * 
	 * @param outputInfo 通知情報
	 * @param expirationFlg 有効期限制御フラグ
	 * @param expirationDateTime 有効期限日時
	 * @param outputDateTime 受信日時
	 * @param oldEntityMap
	 * @param roleId
	 * @param insertEntityList
	 */
	private void outputStatusInfo(OutputBasicInfo outputInfo, int expirationFlg, long expirationDateTime, long outputDateTime,
			Map<StatusInfoEntityPK, StatusInfoEntity> oldEntityMap, String roleId, List<StatusInfoEntity> insertEntityList) {

		if (m_log.isTraceEnabled()) {
			m_log.trace("outputStatusInfo start." + outputInfo.toString());
		}
		StatusInfoEntity outputStatus = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// エンティティ情報の検索
			StatusInfoEntityPK outputStatusPk
			= new StatusInfoEntityPK(outputInfo.getFacilityId(),
					outputInfo.getMonitorId(),
					outputInfo.getSubKey(),
					outputInfo.getPluginId());
			if (oldEntityMap == null) {
				try {
					outputStatus = com.clustercontrol.notify.monitor.util.QueryUtil.getStatusInfoPK(outputStatusPk);
				} catch (MonitorNotFound e) {
					m_log.debug("monitor not found.", e);
				} catch (InvalidRole e) {
					m_log.debug("invalid role.", e);
				}
			} else {
				outputStatus = oldEntityMap.get(outputStatusPk);
			}
			if (outputStatus == null) {
				// 検索条件に合致するエンティティが存在しないため新規に生成
				// インスタンス生成
				outputStatus = new StatusInfoEntity(outputStatusPk);
				if (roleId == null) {
					outputStatus.setOwnerRoleId(NotifyUtil.getOwnerRoleId(
							outputStatusPk.getPluginId(), outputStatusPk.getMonitorId(),
							outputStatusPk.getMonitorDetailId(), outputStatusPk.getFacilityId(), false));
					em.persist(outputStatus);
				} else {
					outputStatus.setOwnerRoleId(roleId);
				}

				// 重複チェック
				outputStatus.setApplication(outputInfo.getApplication());
				outputStatus.setExpirationDate(expirationDateTime);
				outputStatus.setExpirationFlg(expirationFlg);
				outputStatus.setGenerationDate(outputInfo.getGenerationDate());
				String message = outputInfo.getMessage();
				if (message == null) {
					message = "";
				}
				if (message.length() > MESSAGE_DEGIT) {
					outputStatus.setMessage(message.substring(0, MESSAGE_DEGIT));
				} else {
					outputStatus.setMessage(message);
				}
				outputStatus.setOutputDate(outputDateTime);
				outputStatus.setPriority(outputInfo.getPriority());
				
				if (checkDuplicateStatus(insertEntityList, outputStatus)) {
					insertEntityList.add(outputStatus);
				}
			} else {
				// ステータス情報の更新
				outputStatus.setApplication(outputInfo.getApplication());
				if (outputInfo.getMessage().length() > MESSAGE_DEGIT) {
					outputStatus.setMessage(outputInfo.getMessage().substring(0, MESSAGE_DEGIT));
				} else {
					outputStatus.setMessage(outputInfo.getMessage());
				}

				// 重要度が変更されていた場合、出力日時を更新する
				if (outputStatus.getPriority().intValue() != outputInfo.getPriority()) {
					outputStatus.setGenerationDate(outputInfo.getGenerationDate());
				}

				outputStatus.setPriority(outputInfo.getPriority());
				outputStatus.setOutputDate(outputDateTime);
				outputStatus.setExpirationFlg(expirationFlg);
				outputStatus.setExpirationDate(expirationDateTime);
				// 同一IDによる監視項目の再作成により、既設の通知でもオーナーロールが変更となる場合がありえるので、再設定
				if (roleId == null) {
					outputStatus.setOwnerRoleId(NotifyUtil.getOwnerRoleId(
							outputStatusPk.getPluginId(), outputStatusPk.getMonitorId(),
							outputStatusPk.getMonitorDetailId(), outputStatusPk.getFacilityId(), false));
				} else {
					outputStatus.setOwnerRoleId(roleId);
				}
			}
		}
	}
	
	/**
	 * キーが同じステータス通知が同時に発行される場合は、最新のものだけが通知されるように
	 * targetのほうが最新の場合は、entityListから古い情報を削除する
	 * 
	 * @param entityList insertするのステータス通知のリスト
	 * @param target 追加でinsertするステータス通知のリスト
	 * 
	 * @return targetを追加してよい場合はtrue、targetより新しいものがentityListにある場合はfalse
	 */
	private boolean checkDuplicateStatus(List<StatusInfoEntity> entityList, StatusInfoEntity target) {
		int index = 0;
		boolean removeFlag = false;
		for (StatusInfoEntity entity : entityList) {
			if (entity.getId().equals(target.getId())) {
				if (entity.getOutputDate() < target.getOutputDate()) {
					// キーが一致して、targetの出力日時が新しい場合はリストから削除する
					removeFlag = true;
					break;
				} else {
					// キーが一致して、targetの出力日時が古い場合はfalseを返す
					return false;
				}
			}
		}
		
		if (removeFlag) {
			entityList.remove(index);
		}
		
		return true;
	}

	/**
	 * 判定による重要度変化する場合のステータス情報を出力します。<BR>
	 * 監視項目IDとファシリティID、プラグインIDが合致するステータス情報が存在しない場合は、ステータス情報を作成します。
	 * 監視項目IDとファシリティID、プラグインIDが合致するステータス情報がすでに存在する場合は、ステータス情報を更新します。
	 * 今回の更新対象を最新のステータスとし、それ以外の監視項目IDとファシリティID、プラグインIDが合致するステータス情報が
	 * 存在する場合は削除を行います。
	 * 
	 * @param outputInfo 通知情報
	 * @param expirationFlg 有効期限制御フラグ
	 * @param expirationDateTime 有効期限日時
	 * @param outputDateTime 受信日時
	 * @param oldEntityMap
	 * @param roleId
	 * @param insertEntityList
	 */
	private void outputStatusInfoPriorityChanged(OutputBasicInfo outputInfo, int expirationFlg, long expirationDateTime, long outputDateTime,
			Map<StatusInfoEntityPK, StatusInfoEntity> oldEntityMap, String roleId, List<StatusInfoEntity> insertEntityList) {

		if (m_log.isTraceEnabled()) {
			m_log.trace("outputStatusInfoPriorityChanged start." + outputInfo.toString());
		}
		StatusInfoEntity outputStatus = null;

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// ステータス情報のPKエンティティの作成
			StatusInfoEntityPK outputStatusPk
			= new StatusInfoEntityPK(outputInfo.getFacilityId(),
					outputInfo.getMonitorId(),
					outputInfo.getSubKey(),
					outputInfo.getPluginId());

			// サブキーを除いたリストを取得する
			List<StatusInfoEntity> updateStatusList
			= com.clustercontrol.notify.monitor.util.QueryUtil.getStatusInfoByPKWithoutMonitorDetailId(
					outputInfo.getPluginId(),
					outputInfo.getMonitorId(),
					outputInfo.getFacilityId());

			// DELETE INSERTをメインとするため、インスタンス生成
			outputStatus = new StatusInfoEntity(outputStatusPk);
			// 検索条件に合致するエンティティが存在しないため新規に生成
			if (roleId == null) {
				outputStatus.setOwnerRoleId(NotifyUtil.getOwnerRoleId(
						outputStatusPk.getPluginId(), outputStatusPk.getMonitorId(),
						outputStatusPk.getMonitorDetailId(), outputStatusPk.getFacilityId(), false));
				em.persist(outputStatus);
			} else {
				outputStatus.setOwnerRoleId(roleId);
			}

			outputStatus.setApplication(outputInfo.getApplication());
			outputStatus.setExpirationDate(expirationDateTime);
			outputStatus.setExpirationFlg(expirationFlg);

			String message = outputInfo.getMessage();
			if (message == null) {
				message = "";
			}
			if (message.length() > MESSAGE_DEGIT) {
				outputStatus.setMessage(message.substring(0, MESSAGE_DEGIT));
			} else {
				outputStatus.setMessage(message);
			}

			// 前回情報が存在しており、重要度が同一の場合は、出力日時を前回情報のものとする
			if (!updateStatusList.isEmpty() 
					&& updateStatusList.get(0).getPriority().intValue() == outputInfo.getPriority()) {
				outputStatus.setGenerationDate(updateStatusList.get(0).getGenerationDate());
			} else {
				outputStatus.setGenerationDate(outputInfo.getGenerationDate());
			}

			outputStatus.setOutputDate(outputDateTime);
			outputStatus.setPriority(outputInfo.getPriority());

			// 追加対象の重複チェック
			if (checkDuplicateStatusWithoutMonitorDetailId(insertEntityList, outputStatus)) {
				insertEntityList.add(outputStatus);
			}

			// 今回のoutputStatus以外のステータス通知を削除する
			// 削除対象がoldEntityMapにいた場合は削除する
			if (!updateStatusList.isEmpty()) {
				for (StatusInfoEntity delStatusinfo : updateStatusList) {
					em.remove(delStatusinfo);
					oldEntityMap.remove(delStatusinfo.getId());
				}
			}
			
		}
	}
	
	/**
	 * 監視詳細IDを除くキーが同じステータス通知が同時に発行される場合は、
	 * 最新のものだけが通知されるようにする。
	 * targetのほうが最新の場合は、entityListから古い情報を削除する
	 * なお、文字列監視において、List<NotifyRequestMessage>が
	 * 2件以上なければ、削除処理は実行されない
	 * 
	 * @param entityList insertするステータス通知のリスト
	 * @param target 追加でinsertするステータス通知のリスト
	 * 
	 * @return targetを追加してよい場合はtrue、targetより新しいものがentityListにある場合はfalse
	 */
	private boolean checkDuplicateStatusWithoutMonitorDetailId(List<StatusInfoEntity> entityList, StatusInfoEntity target) {
		ArrayList<StatusInfoEntity> deleteList = new ArrayList<>();
		boolean targetAdoptFlag = true;
		for (StatusInfoEntity entity : entityList) {
			// 監視詳細IDを除くキーの操作
			if (entity.getId().getFacilityId().equals(target.getId().getFacilityId())
					&& entity.getId().getMonitorId().equals(target.getId().getMonitorId())
					&& entity.getId().getPluginId().equals(target.getId().getPluginId())) {
				if (entity.getOutputDate() < target.getOutputDate()) {
					// キーが一致して、targetの最終変更日時が新しい場合はentityをリストに追加
					deleteList.add(entity);
				} else {
					// キーが一致して、targetの最終変更日時が古い場合はtargetをaddしない
					targetAdoptFlag = false;
				}
			}
		}
		// 重複した追加対象の削除
		if (!deleteList.isEmpty()) {
			for (StatusInfoEntity result : deleteList) {
				entityList.remove(result);
			}
		}
		
		return targetAdoptFlag;
	}
}
