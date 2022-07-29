/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.factory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.calendar.session.CalendarControllerBean;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.monitor.bean.PriorityChangeFailureTypeConstant;
import com.clustercontrol.monitor.bean.PriorityChangeJudgmentTypeConstant;
import com.clustercontrol.notify.bean.NotifyRequestMessage;
import com.clustercontrol.notify.bean.NotifyTypeConstant;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.bean.RenotifyTypeConstant;
import com.clustercontrol.notify.model.MonitorStatusEntity;
import com.clustercontrol.notify.model.MonitorStatusEntityPK;
import com.clustercontrol.notify.model.NotifyHistoryEntity;
import com.clustercontrol.notify.model.NotifyHistoryEntityPK;
import com.clustercontrol.notify.model.NotifyInfo;
import com.clustercontrol.notify.model.NotifyInfoDetail;
import com.clustercontrol.notify.util.MonitorStatusCache;
import com.clustercontrol.notify.util.NotifyCache;
import com.clustercontrol.notify.util.QueryUtil;
import com.clustercontrol.plugin.impl.AsyncWorkerPlugin;
import com.clustercontrol.selfcheck.monitor.AsyncTaskQueueMonitor;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.XMLUtil;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * 通知を通知ID毎のキューに振り分ける処理を行うユーティリティクラス
 */
public class NotifyDispatcher {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( NotifyDispatcher.class );

	public static final String MODE_VERBOSE = "verbose";
	private static AtomicBoolean hasNotifiedInternal = new AtomicBoolean(false);

	/**
	 * 引数で指定された情報を各種通知に出力します。<BR>
	 * イベント通知、ステータス通知、メール通知、ジョブ通知、ログエスカレーション通知を行います。
	 *
	 * @see com.clustercontrol.notify.ejb.mdb.NotifyEventBean#onMessage(javax.jms.Message)
	 * @see com.clustercontrol.notify.ejb.mdb.NotifyStatusBean#onMessage(javax.jms.Message)
	 * @see com.clustercontrol.notify.ejb.mdb.NotifyMailBean#onMessage(javax.jms.Message)
	 * @see com.clustercontrol.notify.ejb.mdb.NotifyJobBean#onMessage(javax.jms.Message)
	 * @see com.clustercontrol.notify.ejb.mdb.NotifyLogEscalationBean#onMessage(javax.jms.Message)
	 *
	 * @param info 通知出力情報
	 * @param notifyIdList 通知対象の通知ID
	 * @param queueDeliveryMode JMSの永続化モード指定
	 * @throws HinemosUnknown 
	 */
	public static void notifyAction(OutputBasicInfo info, List<String> notifyIdList, boolean persist) throws HinemosUnknown {
		List<OutputBasicInfo> infoList = new ArrayList<OutputBasicInfo>();
		infoList.add(info);
		notifyAction(infoList, notifyIdList, persist);
	}

	/**
	 * NotifyDispatcher#notifyAction(List<OutputBasicInfo>, List, boolean)は、<BR>
	 * NotifyDispatcher#notifyAction(OutputBasicInfo, List, boolean)よりも高速に動作する。<BR>
	 * (outputBasicInfoListをListで渡すことができる。)<BR>
	 * <BR>
	 * List<OutputBasicInfo>に含まれるOutputBasicInfoのPluginIdとMonitorIdは同一のものとすること！<BR>
	 * 
	 * @see NotifyDispatcher#notifyAction(OutputBasicInfo, List, boolean)
	 * 
	 * @param outputBasicInfoList
	 * @param notifyIdList
	 * @param persist
	 */
	public static void notifyAction(List<OutputBasicInfo> outputBasicInfoList,
			List<String> notifyIdList, boolean persist) throws HinemosUnknown {
		if(notifyIdList == null) {
			return;
		}
		if (outputBasicInfoList.isEmpty()) {
			return;
		}

		//NotifyHistoryを一括読み込み
		OutputBasicInfo firstOutputBasicInfo = outputBasicInfoList.get(0);
		List<NotifyHistoryEntity> notifyHistoryEntityList = null;
		if (outputBasicInfoList.size() > 1) {// 一括
			notifyHistoryEntityList = QueryUtil
					.getNotifyHistoryByPluginIdAndMonitorId(
							firstOutputBasicInfo.getPluginId(),
							firstOutputBasicInfo.getMonitorId());
		} else {// ひとつの場合
			notifyHistoryEntityList = QueryUtil
					.getNotifyHistoryByPluginIdAndMonitorIdAndFacilityId(
							firstOutputBasicInfo.getPluginId(),
							firstOutputBasicInfo.getMonitorId(),
							firstOutputBasicInfo.getFacilityId());
		}
		Map<NotifyHistoryEntityPK, NotifyHistoryEntity> notifyHistoryEntityMap = new ConcurrentHashMap<NotifyHistoryEntityPK, NotifyHistoryEntity>();
		for (NotifyHistoryEntity notifyHistoryEntity : notifyHistoryEntityList) {
			notifyHistoryEntityMap.put(notifyHistoryEntity.getId(),
					notifyHistoryEntity);
		}

		//監視詳細を空文字にする。(抑制の粒度を変更したい場合に利用する。)
		for (OutputBasicInfo info : outputBasicInfoList) {
			String pluginId = info.getPluginId();
			boolean flag = HinemosPropertyCommon.notify_remove_subkey_$.getBooleanValue(pluginId, false);
			if (flag) {
				info.setSubKey("");
			}
		}

		// 通知対象のリスト分の通知種別を振り分ける。
		String notify_mode = HinemosPropertyCommon.notify_mode.getStringValue();

		// cc_monitor_statusのcounterを1増やす。
		HashMap<OutputBasicInfo, Boolean> priorityChangeMap = new HashMap<>();
		HashMap<OutputBasicInfo, Long> acrossCountMap = new HashMap<>();
		for (OutputBasicInfo info : outputBasicInfoList) {
			// ジョブの場合、かつHinemosプロパティにて設定されている場合は
			// MonitorStatusCacheおよびcc_monitor_statusの更新を行わない
			if ((HinemosModuleConstant.JOB).equals(info.getPluginId())
					&& HinemosPropertyCommon.monitor_status_non_store_mode_job.getBooleanValue() ) {
				if( m_log.isDebugEnabled() ){
					m_log.debug("notifyAction() : monitor_status non-store mode is valid."
								+ "MonitorStatusCache update is skipped.");
				}
				// この条件を満たす場合には毎回「重要度が変わった」ものとして扱う
				priorityChangeMap.put(info, true);
			} else {
				priorityChangeMap.put(info, MonitorStatusCache.update(info));
				// 重要度変化のオプションに関するカウンター関連の処理を行います。(notify.remove.subkey.<pluginID> が有効な場合はそちらを優先)
				boolean isRemoveSubkey = HinemosPropertyCommon.notify_remove_subkey_$.getBooleanValue(info.getPluginId(), false);
				if( ! isRemoveSubkey ){
					try (JpaTransactionManager jtm = new JpaTransactionManager()) {
						Long acrossCounter = processCounterPriorityChangeOption(info,notifyIdList,jtm.getEntityManager());
						acrossCountMap.put(info,acrossCounter);
					}
				}
			}
		}
		
		// 監視結果重要度ステータスを更新
		for (String notifyId : notifyIdList) {
			NotifyInfo notifyInfo = NotifyCache.getNotifyInfo(notifyId);
			Integer notifyType = notifyInfo.getNotifyType();
			Boolean notifyValid = notifyInfo.getValidFlg();
			if (notifyType == null || notifyValid == null) {
				// 該当の通知IDの設定が見つからない場合はエラーログを出力し次の通知IDの処理を継続する
				m_log.info("notifyAction() : notifyId = " + notifyId +" not found.");
				continue;
			}
			if (!notifyValid.booleanValue()) {
				if( m_log.isDebugEnabled() ){
					m_log.debug("notifyAction() : notifyId = " + notifyId +" invalid.");
				}
				continue;
			}
			
			if (!checkCalendar(notifyInfo.getCalendarId())) {
				if( m_log.isDebugEnabled() ){
					m_log.debug("notifyAction() : notifyId = " + notifyId +" calendar is disabled now.");
				}
				continue;
			}

			ArrayList<NotifyRequestMessage> msgList = new ArrayList<NotifyRequestMessage>();
			for (OutputBasicInfo info : outputBasicInfoList) {
				if (m_log.isDebugEnabled()) {
					m_log.debug("notifyAction start : "
							+ "notifyGroupId=" + info.getNotifyGroupId()
							+ ", pluginId=" + info.getPluginId()
							+ ", monitorId=" + info.getMonitorId()
							+ ", facilityId=" + info.getFacilityId());
				}
				boolean prioityChangeFlag = priorityChangeMap.get(info);
				Timestamp outputDate = new Timestamp(HinemosTime.currentTimeMillis());

				// デフォルトでは抑制せずに通知する（予期せぬエラーの場合は通知を行う）
				boolean isNotify = notifyCheck(
						info.getFacilityId(),
						info.getPluginId(),
						info.getMonitorId(),
						info.getSubKey(),
						notifyId,
						info.getPriority(),
						outputDate,
						prioityChangeFlag,
						notify_mode,
						acrossCountMap.get(info),
						notifyHistoryEntityMap);

				if(!isNotify){
					continue;
				}

				// 通知出力情報をディープコピーする（AsyncWorkerPlugin.addTaskのため）
				OutputBasicInfo clonedInfo = info.clone();

				// Ignore Invalid XML Chars
				clonedInfo.setMessage(XMLUtil.ignoreInvalidString(clonedInfo.getMessage()));
				clonedInfo.setMessageOrg(XMLUtil.ignoreInvalidString(clonedInfo.getMessageOrg()));

				// 通知IDは設定されていないため、ここで設定する
				NotifyRequestMessage msg = new NotifyRequestMessage(
						clonedInfo,
						notifyId,
						outputDate,
						prioityChangeFlag
						);

				msgList.add(msg);
			}

			try {
				addNotifyTask(persist, notifyType, msgList);
			} catch (HinemosUnknown e) {
				m_log.warn(e);
				throw e;
			}
		}
	}
	
	private static void addNotifyTask(boolean persist, Integer notifyType,
			ArrayList<NotifyRequestMessage> msgList) throws HinemosUnknown {
		//イベント通知のみがメッセージの一括処理を対応した
		if (notifyType == NotifyTypeConstant.TYPE_EVENT) {
			AsyncWorkerPlugin.addTask(NotifyEventTaskFactory.class.getSimpleName(), msgList, persist);
			return;
		} else if (notifyType == NotifyTypeConstant.TYPE_STATUS) {
			AsyncWorkerPlugin.addTask(NotifyStatusTaskFactory.class.getSimpleName(), msgList, persist);
			return;
		}

		for (NotifyRequestMessage msg : msgList) {
			switch (notifyType) {
			case NotifyTypeConstant.TYPE_MAIL :
				AsyncWorkerPlugin.addTask(NotifyMailTaskFactory.class.getSimpleName(), msg, persist);
				break;
			case NotifyTypeConstant.TYPE_COMMAND :
				AsyncWorkerPlugin.addTask(NotifyCommandTaskFactory.class.getSimpleName(), msg, persist);
				break;
			case NotifyTypeConstant.TYPE_LOG_ESCALATE :
				AsyncWorkerPlugin.addTask(NotifyLogEscalationTaskFactory.class.getSimpleName(), msg, persist);
				break;
			case NotifyTypeConstant.TYPE_JOB :
				AsyncWorkerPlugin.addTask(NotifyJobTaskFactory.class.getSimpleName(), msg, persist);
				break;
			case NotifyTypeConstant.TYPE_INFRA :
				AsyncWorkerPlugin.addTask(NotifyInfraTaskFactory.class.getSimpleName(), msg, persist);
				break;
			case NotifyTypeConstant.TYPE_REST :
				AsyncWorkerPlugin.addTask(NotifyRestTaskFactory.class.getSimpleName(), msg, persist);
				break;	
			case NotifyTypeConstant.TYPE_CLOUD:
				// クラウド通知を実行可能な場合のみタスクに追加
				if (shouldExecCloudNotify(msg)) {
					AsyncWorkerPlugin.addTask(NotifyCloudTaskFactory.class.getSimpleName(), msg, persist);
				}
				break;
			case NotifyTypeConstant.TYPE_MESSAGE :
				AsyncWorkerPlugin.addTask(NotifyMessageTaskFactory.class.getSimpleName(), msg, persist);
				break;
			default :
				m_log.warn("notify type is invalid. (notifyType = " + notifyType + ")");
			}
		}
	}

	/**
	 * 抑制条件を確認し現時点で通知すべきか確認する。
	 *
	 * @param facilityId ファシリティID
	 * @param pluginId プラグインID
	 * @param monitorId 監視ID
	 * @param subkey 通知抑制用サブキー
	 * @param notifyId 通知ID
	 * @param priority 重要度
	 * @param outputDate 出力日時
	 * @param priorityChangeFlag 重要度変更フラグ
	 * @param mode 抑制モード（VERBOSE:重要度が変化した場合は出力する, NORMAL:重要度が変化しても前回通知の重要度と同じ場合は通知しない）
	 * @param acrossCounter サブキー横断での同一重要度カウント合計件数（判定を跨いだ重要度変化 有効時のみ設定）
	 * @param notifyHistoryEntityMap
	 * @return 通知する場合は true
	 */
	private static boolean notifyCheck(
			String facilityId,
			String pluginId,
			String monitorId,
			String subkey,
			String notifyId,
			int priority,
			Date outputDate,
			boolean priorityChangeFlag,
			String mode,
			Long acrossCounter,
			Map<NotifyHistoryEntityPK, NotifyHistoryEntity> notifyHistoryEntityMap) {

		if(m_log.isDebugEnabled()){
			m_log.debug("notifyCheck() " +
					"facilityId=" + facilityId +
					", pluginId=" +  pluginId +
					", monitorId=" + monitorId +
					", subkey=" + subkey +
					", notifyId=" + notifyId +
					", priority=" + priority +
					", outputDate=" + outputDate +
					", priorityChangeFlag=" + priorityChangeFlag +
					", mode="+ mode);
		}

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			MonitorStatusEntityPK mspk = new MonitorStatusEntityPK(facilityId, pluginId, monitorId, subkey);

			// チェック処理を（ファシリティID, プラグインID, 監視項目ID, サブキー）の粒度で排他
			NotifyHistoryEntityPK entityPk = new NotifyHistoryEntityPK(facilityId, pluginId, monitorId, notifyId, subkey);
			String pkStr = "facilityId = " + facilityId
					+ ", pluginId  " + pluginId
					+ ", monitorId  " + monitorId
					+ ", notifyId  " + notifyId
					+ ", subkey  " + subkey;
			try {
				boolean isNotifyPriority = true;

				if(MODE_VERBOSE.equals(mode)){
					// 重要度変化があった場合
					if(priorityChangeFlag == true){
						if( m_log.isDebugEnabled() ){
							m_log.debug("priorityChangeFlag == true. remove entity. pk = " + pkStr);
						}

						// 通知履歴の該当タプルを削除する
						try {
							NotifyHistoryEntity entity = null;
							try {
								entity = getNotifyHistoryEntity(entityPk, notifyHistoryEntityMap);
							} catch (NotifyNotFound e) {
								// 処理なし
							}
							if (entity != null) {
								em.remove(entity);
							}
						} catch (Exception e) {
							m_log.warn("notifyCheck() : "
									+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
						}
					}
				}

				// 重要度単位の通知フラグを確認する
				NotifyInfo notifyInfo = NotifyCache.getNotifyInfo(notifyId);
				NotifyInfoDetail notifyDetail = NotifyCache.getNotifyInfoDetail(notifyId);

				// 重要度単位の有効無効を確認
				if (priority == PriorityConstant.TYPE_INFO) {
					if (!notifyDetail.getInfoValidFlg().booleanValue()) {
						// 無効の場合は通知しない
						if( m_log.isDebugEnabled() ){
							m_log.debug("ValidFlg is invalid. " + pkStr + ", priority = " + priority);
							m_log.debug("notify NG. (VALIDFLAG IS INVALID)." + pkStr);
						}
						isNotifyPriority = false;
					}
				} else if (priority == PriorityConstant.TYPE_WARNING) {
					if (!notifyDetail.getWarnValidFlg().booleanValue()) {
						// 無効の場合は通知しない
						if( m_log.isDebugEnabled() ){
							m_log.debug("ValidFlg is invalid. " + pkStr + ", priority = " + priority);
							m_log.debug("notify NG. (VALIDFLAG IS INVALID)." + pkStr);
						}
						isNotifyPriority = false;
					}
				} else if (priority == PriorityConstant.TYPE_CRITICAL) {
					if (!notifyDetail.getCriticalValidFlg().booleanValue()) {
						// 無効の場合は通知しない
						if( m_log.isDebugEnabled() ){
							m_log.debug("ValidFlg is invalid. " + pkStr + ", priority = " + priority);
							m_log.debug("notify NG. (VALIDFLAG IS INVALID)." + pkStr);
						}
						isNotifyPriority = false;
					}
				} else if (priority == PriorityConstant.TYPE_UNKNOWN) {
					if (!notifyDetail.getUnknownValidFlg().booleanValue()) {
						// 無効の場合は通知しない
						if( m_log.isDebugEnabled() ){
							m_log.debug("ValidFlg is invalid. " + pkStr + ", priority = " + priority);
							m_log.debug("notify NG. (VALIDFLAG IS INVALID)." + pkStr);
						}
						isNotifyPriority = false;
					}
				} else {
					m_log.info("unknown priority : " + priority);
					return false;
				}

				// 通知条件である同一重要度カウンタ数を満たしているか確認
				// 同一重要度カウンタを保持する監視結果ステータス情報を検索
				Long counter = null;
				try {
					// MonitorStatusの更新タイミングと時間差が出る可能性はほぼ少ないため、本処理箇所にてカウンタを取得する
					// ジョブの場合、かつHinemosプロパティにて設定されている場合はNotifyNotFoundになるので、
					// カウンタを取得することなくNotifyNotFoundの場合と同じ処理をする
					if ((HinemosModuleConstant.JOB).equals(pluginId) &&
							HinemosPropertyCommon.monitor_status_non_store_mode_job.getBooleanValue() ) {
						return isNotifyPriority;
					} else {
						counter = MonitorStatusCache.getCounter(mspk);
					}
				} catch (NotifyNotFound e) {
					// ジョブからの通知はここを通る。
					if( m_log.isDebugEnabled() ){
						m_log.debug("notify OK. (MONITOR STATUS NOT FOUND)." + pkStr);
					}
					return isNotifyPriority;
				}

				// 同一重要度内のカウント合計が通知カウンタ数を上回っているか判定
				boolean acrossCountFlag = false;
				if(	 acrossCounter != null ){ 
					acrossCountFlag = acrossCounter >= notifyInfo.getInitialCount();
				}

				if (acrossCountFlag || counter >= notifyInfo.getInitialCount()) {
					// カウンタが条件を満たしている場合
					if( m_log.isDebugEnabled() ){
						m_log.debug("counter check. " + counter + " >= " + notifyInfo.getInitialCount() + "  " + pkStr);
						if (acrossCountFlag) {
							m_log.debug("totalCounter check. " + acrossCounter + " >= " + notifyInfo.getInitialCount()
							+ "  facilityId=" + facilityId + ", pluginId=" + pluginId + ", monitorId=" + monitorId);
						}
					}
	
					if(pluginId.equals(HinemosModuleConstant.JOB) &&
							notifyInfo.getRenotifyType() == RenotifyTypeConstant.TYPE_ALWAYS_NOTIFY &&
							!notifyInfo.getNotFirstNotify().booleanValue()){
						// ジョブの場合 かつ
						// [重要度変化後の二回目以降の通知]が常に通知するの場合 かつ [有効にした直後は通知しない]がオフの場合
						// 判定のための履歴は不要のためDBへ登録しないようにする
						// ※ジョブはセッションごとに履歴が作成されてしまうため登録を抑制する
						//   設定を抑制ありに変更する際、他と変更直後の挙動が変わるがジョブの実行中に変更するケースが稀なため許容する
						if( m_log.isDebugEnabled() ){
							m_log.debug("notify OK. (JOB AND RENOTIFY ALWAYS AND INVALID NOT FIRST NOTIFY)." + pkStr);
						}
						return isNotifyPriority;
					}

					try{
						NotifyHistoryEntity history = null;
						if(!MODE_VERBOSE.equals(mode)){
							if (acrossCountFlag 
									&& notifyInfo.getRenotifyType() == RenotifyTypeConstant.TYPE_NO_NOTIFY) {
								// 再通知なしの場合は同一重要度内に存在する通知履歴を取得します。
								history = getNotifyHistoryEntityByFacilityIdAndPluginIdAndMonitorIdAndNotifyIdAndPriority(entityPk, notifyHistoryEntityMap, priority);
							} else {
								history = getNotifyHistoryEntity(entityPk, notifyHistoryEntityMap);
							}
							// 現在の監視結果の重要度と最終通知時の重要度が異なる場合は通知する
							if (priority != history.getPriority()) {
								if( m_log.isDebugEnabled() ){
									m_log.debug("update notify history." + pkStr);
								}
								history.setLastNotify(outputDate.getTime());
								history.setPriority(priority);
								if( m_log.isDebugEnabled() ){
									m_log.debug("notify OK. (PRIORITY CHANGE)." + pkStr);
								}
								return isNotifyPriority;
							}
						}

						// 該当のタプルが存在する場合
						// [重要度変化後の二回目以降の通知]の種別を確認
						if(notifyInfo.getRenotifyType() == RenotifyTypeConstant.TYPE_NO_NOTIFY){
							// 通知しないの場合
							if( m_log.isDebugEnabled() ){
								m_log.debug("notify NG. (RENOTIFY NO)." + pkStr);
							}
							return false;
						} else if(notifyInfo.getRenotifyType() == RenotifyTypeConstant.TYPE_ALWAYS_NOTIFY){
							// 常に通知するの場合
							// history.setLastNotify(new Timestamp(outputDate.getTime())); 常に通知するため更新の必要がない
							// history.setPriority(priority); 常に通知するため更新の必要がない
							if( m_log.isDebugEnabled() ){
								m_log.debug("notify OK. (RENOTIFY ALWAYS)." + pkStr);
							}
							return isNotifyPriority;
						} else {
							if (history == null) {
								history = getNotifyHistoryEntity(entityPk, notifyHistoryEntityMap);
							}
							if(outputDate != null && outputDate.getTime() >=
									(history.getLastNotify() + (notifyInfo.getRenotifyPeriod() * 60 * 1000l))){
								if( m_log.isDebugEnabled() ){
									m_log.debug("update notify history." + pkStr);
								}
								// 通知時刻が抑制期間を超えている場合
								history.setLastNotify(outputDate.getTime());
								history.setPriority(priority);
								if( m_log.isDebugEnabled() ){
									m_log.debug("notify OK. (RENOTIFY PERIOD)." + pkStr);
								}
								return isNotifyPriority;
							} else {
								if( m_log.isDebugEnabled() ){
									m_log.debug("notify NG. (RENOTIFY PERIOD)." + pkStr);
								}
								return false;
							}
						}
					} catch (NotifyNotFound e) {
						// 該当のタプルが存在しない場合
						// 初回通知
						if( m_log.isDebugEnabled() ){
							m_log.debug("first notify. " + pkStr + ", priority = " + priority);
						}

						// 新規に通知履歴を作成する
						NotifyHistoryEntity newHistoryEntity
						= new NotifyHistoryEntity(facilityId, pluginId, monitorId, notifyId, subkey);
						em.persist(newHistoryEntity);
						newHistoryEntity.setLastNotify(outputDate==null?null:outputDate.getTime());
						newHistoryEntity.setPriority(priority);
						if( m_log.isDebugEnabled() ){
							m_log.debug("notify OK. (NEW)." + pkStr);
						}
						notifyHistoryEntityMap.put(entityPk, newHistoryEntity);

						if(notifyInfo.getNotFirstNotify().booleanValue()){
							// [有効にした直後は通知しない]がオンの場合
							return false;
						}
						return isNotifyPriority;
					}
				} else {
					if( m_log.isDebugEnabled() ){
						m_log.debug("notify NG. (PRIORITY CHANGE)." + pkStr);
					}
					return false;
				}
			} catch (Exception e) {
				m_log.warn("notifyCheck() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}

			m_log.info("notifyCheck() notify OK. (cause unexpected errors)" + pkStr);
			return true;
		}
	}

	private static NotifyHistoryEntity getNotifyHistoryEntity(
			NotifyHistoryEntityPK pk,
			Map<NotifyHistoryEntityPK, NotifyHistoryEntity> notifyHistoryEntityMap)
			throws NotifyNotFound {
		NotifyHistoryEntity entity = notifyHistoryEntityMap.get(pk);
		if (entity == null) {
			throw new NotifyNotFound();
		}
		return entity;
	}
	
	private static boolean checkCalendar(String calendarId) throws HinemosUnknown {
		long now = HinemosTime.getDateInstance().getTime();

		// 稼働日か否かチェック
		try {
			CalendarControllerBean calendar = new CalendarControllerBean();
			if(!calendar.isRun(calendarId, now)){
				// 非稼働日の場合は、処理終了
				if( m_log.isDebugEnabled() ){
					m_log.debug("checkCalendar() " + " calenderId = " + calendarId
							+ ". The notice is not executed because of non-operating day.");
				}
				return false;
			}
		} catch (InvalidRole e) {
			// 指定されたカレンダIDの参照権限がない場合は、処理終了
			// 手動でない限りここは通らない。
			// （ADMINISTRATORSロールのユーザ、もしくはユーザ未指定の場合はオブジェクト権限をしないため）
			return false;
		} catch (CalendarNotFound e) {
			// 指定されたカレンダIDがすでに存在しない場合は、処理終了
			return false;
		}

		return true;
	}
	/**
	 * 取得失敗を除いた同一重要度内に存在する通知履歴を取得します。
	 * 
	 * @param pk 通知履歴PK
	 * @param notifyHistoryEntityMap 通知履歴一覧
	 * @param priority 重要度
	 * @return 通知履歴
	 * @throws NotifyNotFound
	 */
	private static NotifyHistoryEntity getNotifyHistoryEntityByFacilityIdAndPluginIdAndMonitorIdAndNotifyIdAndPriority(
			NotifyHistoryEntityPK pk,
			Map<NotifyHistoryEntityPK, NotifyHistoryEntity> notifyHistoryEntityMap,
			int priority)
			throws NotifyNotFound {

		NotifyHistoryEntity entity = notifyHistoryEntityMap.get(pk);
		// PKで通知履歴を取得できなかった場合は、同一重要度(取得失敗を除く)内に存在する通知履歴を取得する。
		if (entity == null) {
			if (HinemosModuleConstant.MONITOR_HTTP_SCENARIO.equals(pk.getPluginId())) {
				// HTTPシナリオは取得成功時にサブキーが空なので例外的に処理
				List<NotifyHistoryEntity> entityList = new ArrayList<>(notifyHistoryEntityMap.values());
				entity = entityList.stream()
						.filter(notifyHistoryEntity -> notifyHistoryEntity.getId().getFacilityId().equals(pk.getFacilityId())
								&& notifyHistoryEntity.getId().getPluginId().equals(pk.getPluginId())
								&& notifyHistoryEntity.getId().getMonitorId().equals(pk.getMonitorId())
								&& notifyHistoryEntity.getId().getNotifyId().equals(pk.getNotifyId())
								&& notifyHistoryEntity.getPriority().equals(priority))
						.findFirst()
						.orElse(null);
			}else{
				// 同一重要度(取得失敗＝subkeyがブランクを除く)内に存在する通知履歴を取得する。
				List<NotifyHistoryEntity> entityList = new ArrayList<>(notifyHistoryEntityMap.values());
				entity = entityList.stream()
						.filter(notifyHistoryEntity -> notifyHistoryEntity.getId().getFacilityId().equals(pk.getFacilityId())
								&& notifyHistoryEntity.getId().getPluginId().equals(pk.getPluginId())
								&& notifyHistoryEntity.getId().getMonitorId().equals(pk.getMonitorId())
								&& notifyHistoryEntity.getId().getNotifyId().equals(pk.getNotifyId())
								&& notifyHistoryEntity.getPriority().equals(priority)
								&& notifyHistoryEntity.getId().getSubKey() != null
								&& !notifyHistoryEntity.getId().getSubKey().isEmpty())
						.findFirst()
						.orElse(null);
			}
		}

		if (entity == null) {
			throw new NotifyNotFound();
		}
		return entity;
	}

	/**
	 * 判定を跨いだ重要度変化の対象か判定します。
	 * 
	 * @param pluginId プラグインID
	 * @return true:重要度変化の対象,false:重要度変化の対象外
	 */
	private static boolean isPriorityChange(Integer priorityChangeJudgmentType) {
		if (priorityChangeJudgmentType != null
				&& priorityChangeJudgmentType == PriorityChangeJudgmentTypeConstant.TYPE_ACROSS_MONITOR_DETAIL_ID) {
			return true;
		}
		return false;
	}

	/**
	 * 監視対象値が取得失敗時の「不明」も重要度変化として扱うか判定します。
	 * 
	 * @param pluginId プラグインID
	 * @return true:重要度変化の対象,false:重要度変化の対象外
	 */
	private static boolean isPriorityChangeFail(Integer priorityChangeFailureType) {
		if (priorityChangeFailureType != null
				&& priorityChangeFailureType == PriorityChangeFailureTypeConstant.TYPE_PRIORITY_CHANGE) {
			return true;
		}

		return false;
	}

	/**
	 * 監視ステータスのキャッシュ情報を取得します。
	 * 
	 * @param facilityId ファシリティID
	 * @param pluginId プラグインID
	 * @param monitorId 監視ID
	 * @return 監視ステータスキャッシュリスト
	 */
	private static List<MonitorStatusEntity> getMonitorStatusEntityCacheList(String facilityId, String pluginId, String monitorId) {
		return MonitorStatusCache.getCacheByFacilityIdAndPluginIdAndMonitorId(facilityId, pluginId, monitorId);
	}

	/**
	 * 重要度変化のオプションに関するカウンター関連の処理を行います。<br>
	 * 
	 * 本メソッドの動作仕様としては以下の通り<br>
	 * 
	 *   判定を跨いだ重要度変化の対象である場合（ 文字列監視各種 ） <br>
	 *     ・取得成功時、同一重要度のサブキー合算にて発生件数を算出<br>
	 *     ・取得成功時、取得失敗をのぞく別重要度のサブキーカウンタをリセット<br>
	 * 
	 *   判定を跨いだ重要度変化の対象である場合（ HTTPシナリオ監視のみ ）<br>
	 *     ・同一重要度のサブキー合算にて発生件数を算出 <br>
	 *     ・別重要度のサブキーカウンタをリセット<br>
	 *     ※HTTPシナリオ監視のみサブキーの利用方法が特殊なため個別対応
	 * 
	 *    監視対象値が取得失敗時の「不明」も重要度変化として扱う場合<br>
	 *     ・取得失敗時、取得成功系サブキーカウンタをリセット<br>
	 *     ・取得成功時、取得失敗サブキーカウンタをリセット<br>
	 * 
	 * 前提となる動作仕様<br>
	 *   以下の監視については 対象データの取得失敗時にサブキーをブランクとしてる<br>
	 *   ・HTTP監視（文字列）<br>
	 *   ・SNMP監視（文字列）<br>
	 *   ・カスタム監視（文字列）<br>
	 *   ・カスタム監視（数値）<br>
	 *   ・SQL監視（文字列）<br>
	 *   ・クラウドサービス監視（真偽）<br>
	 *   
	 *   HTTPシナリオ監視では 監視の正常完了パターンのサブキーをブランクとしてる 。<br>
	 *   
	 *   文字列監視については 判定の対象となったパターンマッチ表現をサブキーとしてる。<br> 
	 * 
	 *   getNotifyHistoryEntityByFacilityIdAndPluginIdAndMonitorIdAndNotifyIdAndPriority
	 *   での取得対象は 本メソッドのカウント合計の取得対象と同じになる。
	 * 
	 * @param basicInfo 通知の基本情報
	 * @param notifyIdList 通知IDList
	 * @param em EntityManagerオブジェクト
	 * @return 通知の連続件数を返します。
	 */
	private static Long processCounterPriorityChangeOption(OutputBasicInfo basicInfo,  List<String> notifyIdList , HinemosEntityManager em) {
		boolean isPriorityChangeJudgeAcross = isPriorityChange(basicInfo.getPriorityChangeJudgmentType());
		boolean isPriorityChangeFailOuccer = isPriorityChangeFail(basicInfo.getPriorityChangeFailureType());

		// 有効なオプション設定が無ければ処理を打ち切り
		if (!(isPriorityChangeJudgeAcross) && !(isPriorityChangeFailOuccer)) {
			return null;
		}

		String facilityId = basicInfo.getFacilityId();
		String pluginId = basicInfo.getPluginId();
		String monitorId = basicInfo.getMonitorId();
		int priority = basicInfo.getPriority();
		String subkey = basicInfo.getSubKey();
		Long totalCounter = null; 
		List<MonitorStatusEntity> deleteTargetStatusList = new ArrayList<MonitorStatusEntity>();

		// 判定を跨いだ重要度変化の対象である場合
		if (isPriorityChangeJudgeAcross) {
			if( m_log.isDebugEnabled() ){
				m_log.debug("processCounterPriorityChangeOption() : isPriorityChangeJudgeAcross = true."
						+ " facilityId = " + facilityId + ", pluginId = " + pluginId
						+ ", monitorId = " + monitorId + ", subkey = " + subkey);
			}

			// HTTPシナリオは取得成功時にサブキーが空なので例外的に処理（現状はAのみ通る）
			if (HinemosModuleConstant.MONITOR_HTTP_SCENARIO.equals(pluginId)) {
				if (m_log.isDebugEnabled()) {
					m_log.debug("processCounterPriorityChangeOption() : monitor http scenario. facilityId = " + facilityId
							+ ", pluginId = " + pluginId + ", monitorId = " + monitorId + ", subkey =  "+ subkey);
				}
				// 監視ステータスのキャッシュ情報取得
				List<MonitorStatusEntity> monitorStatusEntityList =
						getMonitorStatusEntityCacheList(facilityId, pluginId, monitorId);

				// 監視ステータスの中から、重要度以外が相違している一覧を取得(HTTPシナリオはサブキーを見ない)
				List<MonitorStatusEntity> diffPriortyList = monitorStatusEntityList.stream()
						.filter(monitorStatusEntity -> !monitorStatusEntity.getPriority().equals(priority))
						.collect(Collectors.toList());

				// 監視ステータスの中から、重要度が同じ情報のカウント合計を求める(HTTPシナリオはサブキーを見ない)
				totalCounter = monitorStatusEntityList.stream()
						.filter(monitorStatusEntity -> monitorStatusEntity.getPriority().equals(priority))
						.collect(Collectors.summingLong(MonitorStatusEntity::getCounter));
				
				// 重要度が一致していないものはキャッシュ、通知履歴から削除対象とする
				deleteTargetStatusList.addAll(diffPriortyList);

			// HTTPシナリオ以外で取得成功の場合
			} else if (isSuccessAcquisition(subkey, pluginId)) {
				if (m_log.isDebugEnabled()) {
					m_log.debug("processCounterPriorityChangeOption() : success acquisition. facilityId = " + facilityId
							+ ", pluginId = " + pluginId + ", monitorId = " + monitorId + ", subkey =  "+ subkey);
				}
				// 監視ステータスのキャッシュ情報取得
				List<MonitorStatusEntity> monitorStatusEntityList =
						getMonitorStatusEntityCacheList(facilityId, pluginId, monitorId);
				// 監視ステータスの中から、重要度以外が相違している一覧を取得(ただし、サブキーが存在するもの=取得成功のものに限る)
				List<MonitorStatusEntity> diffPriortyList = monitorStatusEntityList.stream()
						.filter(monitorStatusEntity -> !monitorStatusEntity.getPriority().equals(priority)
								&& monitorStatusEntity.getId().getSubKey() != null
								&& !monitorStatusEntity.getId().getSubKey().isEmpty())
						.collect(Collectors.toList());
				// 監視ステータスの中から、重要度が同じ情報のカウント合計を求める(ただし、サブキーが存在するもの=取得成功のものに限る)
				totalCounter = monitorStatusEntityList.stream()
						.filter(monitorStatusEntity -> monitorStatusEntity.getPriority().equals(priority)
								&& monitorStatusEntity.getId().getSubKey() != null
								&& !monitorStatusEntity.getId().getSubKey().isEmpty())
						.collect(Collectors.summingLong(MonitorStatusEntity::getCounter));

				// 重要度が一致していないものはキャッシュ、通知履歴から削除対象とする
				deleteTargetStatusList.addAll(diffPriortyList);

			// HTTPシナリオ以外でサブキーの中身が無い場合は取得失敗として処理しない
			} else {
				if (m_log.isDebugEnabled()) {
					m_log.debug("processCounterPriorityChangeOption() :  acquisition failed. facilityId = " + facilityId
							+ ", pluginId = " + pluginId + ", monitorId = " + monitorId + ", subkey =  "+ subkey);
				}
				// 処理なし
			}
		}

		// 監視対象値が取得失敗時の「不明」も重要度変化として扱う場合
		if ( isPriorityChangeFailOuccer) {
			if( m_log.isDebugEnabled() ){
				m_log.debug("processCounterPriorityChangeOption() : isPriorityChangeFailOuccer = true."
						+ " facilityId = " + facilityId + ", pluginId = " + pluginId
						+ ", monitorId = " + monitorId + ", subkey = " + subkey);
			}
			
			if (!(isSuccessAcquisition(subkey, pluginId))) {
				if (m_log.isDebugEnabled()) {
					m_log.debug("processCounterPriorityChangeOption() : acquisition failed. facilityId = " + facilityId
							+ ", pluginId = " + pluginId + ", monitorId = " + monitorId + ", subkey =  "+ subkey);
				}
				// 取得失敗の場合、キャッシュと通知履歴に残っている取得成功の情報を削除
				// キャッシュ情報取得
				List<MonitorStatusEntity> monitorStatusEntityList =
						getMonitorStatusEntityCacheList(facilityId, pluginId, monitorId);

				// 監視ステータスの中から、サブキーが存在するもの=取得成功の一覧を取得する
				List<MonitorStatusEntity> successList = monitorStatusEntityList.stream()
						.filter(monitorStatusEntity -> monitorStatusEntity.getId().getSubKey() != null
								&& !monitorStatusEntity.getId().getSubKey().isEmpty())
						.collect(Collectors.toList());

				// 取得失敗の場合、サブキーが存在する取得成功の情報は削除対象とする
				deleteTargetStatusList.addAll(successList);

			} else {
				// 取得成功の場合、キャッシュと通知履歴に残っている取得失敗の情報を削除
				if (m_log.isDebugEnabled()) {
					m_log.debug("processCounterPriorityChangeOption() : success acquisition. facilityId = " + facilityId
							+ ", pluginId = " + pluginId + ", monitorId = " + monitorId + ", subkey =  "+ subkey);
				}
				// キャッシュの削除
				MonitorStatusEntityPK failPk = new MonitorStatusEntityPK(facilityId, pluginId, monitorId, "");
				MonitorStatusCache.removeCache(failPk);
				for (String notifyId : notifyIdList) {
					// 通知履歴の削除
					try {
						NotifyHistoryEntity entity = null;
						try {
							entity = QueryUtil.getNotifyHistoryPK(facilityId, pluginId, monitorId, notifyId, "");
						} catch (NotifyNotFound e) {
							// 処理なし
						}
						if (entity != null) {
							em.remove(entity);
						}
					} catch (Exception e) {
						m_log.warn("processCounterPriorityChangeOption() : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					}
				}
			}
		}

		// 削除対象となってるキャッシュおよび通知履歴を削除
		if (!deleteTargetStatusList.isEmpty()) {
			deleteCacheAndHistory(deleteTargetStatusList,notifyIdList, em);
		}
		return totalCounter;
	}

	/**
	 * サブキーに基づき取得の成否を判定します。
	 * 
	 * @param subkey サブキー
	 * @param pluginId プラグインID
	 * @return true 成功 / false 失敗
	 */
	private static boolean isSuccessAcquisition( String subkey ,String pluginId ){
		if (pluginId != null) {
			switch (pluginId) {
				case  HinemosModuleConstant.MONITOR_HTTP_S:
				case  HinemosModuleConstant.MONITOR_CUSTOM_S :
				case  HinemosModuleConstant.MONITOR_CUSTOM_N:
				case  HinemosModuleConstant.MONITOR_SNMP_S:
				case  HinemosModuleConstant.MONITOR_SQL_S:
				case  HinemosModuleConstant.MONITOR_CLOUD_SERVICE_CONDITION :
					if ((subkey == null || subkey.isEmpty())) {
						return false;
					}
				default:
			}
		}
		return true;
	} 
	/**
	 * リストのキャッシュ、通知履歴を削除します。
	 * 
	 * @param priortyList 重要度一覧
	 * @param notifyIdList 通知ID一単
	 * @param em EntityManagerオブジェクト
	 */
	private static void deleteCacheAndHistory(List<MonitorStatusEntity> priortyList,  List<String> notifyIdList, HinemosEntityManager em) {
		for (MonitorStatusEntity deleteStatusEntity : priortyList) {
			if (deleteStatusEntity != null) {
				if (m_log.isDebugEnabled()) {
					m_log.debug("deleteCacheAndHistory() : MonitorStatusEntity . delete data is exist. facilityId = " + deleteStatusEntity.getId().getFacilityId() + ", pluginId = "
							+ deleteStatusEntity.getId().getPluginId() + ", monitorId = " + deleteStatusEntity.getId().getMonitorId()
							+ ", subkey = " + deleteStatusEntity.getId().getSubKey());
				}
				// キャッシュの削除
				MonitorStatusCache.removeCache(deleteStatusEntity.getId());
				for (String notifyId : notifyIdList) {
					// 通知履歴の削除
					try {
						if (m_log.isDebugEnabled()) {
							m_log.debug("deleteCacheAndHistory() : NotifyHistoryEntity . delete target. facilityId = " + deleteStatusEntity.getId().getFacilityId() + ", pluginId = "
									+ deleteStatusEntity.getId().getPluginId() + ", monitorId = " + deleteStatusEntity.getId().getMonitorId()
									+ "notifyId = " + notifyId  + ", subkey = " + deleteStatusEntity.getId().getSubKey());
						}
						NotifyHistoryEntity entity = null;
						try {
							entity = QueryUtil.getNotifyHistoryPK(deleteStatusEntity.getId().getFacilityId(),
									deleteStatusEntity.getId().getPluginId(), deleteStatusEntity.getId().getMonitorId(),
									notifyId, deleteStatusEntity.getId().getSubKey());
						} catch (NotifyNotFound e) {
							// 処理なし
						}
						if (entity != null) {
							em.remove(entity);
						}
					} catch (Exception e) {
						m_log.warn("deleteCacheAndHistory() : "
								+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
					}
				}
			}
		}
	}	
	
	/**
	 * クラウド通知のバーストに対する対策のため、をAsyncタスクに受け渡して良いかの判定を行います。
	 * プロパティが無効の場合は常にTrueが返ります。
	 * @param msg
	 * @return
	 */
	private static boolean shouldExecCloudNotify(NotifyRequestMessage msg) {
		int taskCount;
		try {
			taskCount = AsyncTaskQueueMonitor.getTaskCount(NotifyCloudTaskFactory.class.getSimpleName());
		} catch (HinemosUnknown e) {
			// 通常ここには来ない想定のため、ログを出力してfalseを返す
			m_log.error("shouldExecCloudNotify(): ", e);
			return false;
		}
		m_log.debug("shouldExecCloudNotify(): current task count: " + taskCount);

		boolean notify = true;
		if (HinemosPropertyCommon.notify_cloud_drop_excessive_msg.getBooleanValue()) {
			if (taskCount > HinemosPropertyCommon.notify_cloud_drop_excessive_count.getIntegerValue()) {
				// バースト時のドロップが有効、かつタスク数が既定の数を超えた場合
				notify = false;
				m_log.warn(String.format(
						"shouldExecCloudNotify(): Notify Queue exceeds maximum. Current Count:%d, Maximum:%d. Drop msg:%s",
						taskCount, HinemosPropertyCommon.notify_cloud_drop_excessive_count.getIntegerValue(), msg));

				// INTERNALは1度だけ出力
				if (!hasNotifiedInternal.get()) {
					String[] args = { Integer
							.toString(HinemosPropertyCommon.notify_cloud_drop_excessive_count.getIntegerValue()) };
					AplLogger.put(InternalIdCommon.PLT_NTF_SYS_030, args, "");
					hasNotifiedInternal.set(true);
				}
			}
		}

		// バースト状態から回復した場合にINTERNAL
		if (notify) {
			if (hasNotifiedInternal.get()) {
				String[] args = {
						Integer.toString(HinemosPropertyCommon.notify_cloud_drop_excessive_count.getIntegerValue()) };
				AplLogger.put(InternalIdCommon.PLT_NTF_SYS_031, args, "");
			}
			hasNotifiedInternal.set(false);
		}

		return notify;
	}
}