/*

Copyright (C) 2010 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.notify.factory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.calendar.session.CalendarControllerBean;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.notify.bean.NotifyRequestMessage;
import com.clustercontrol.notify.bean.NotifyTypeConstant;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.bean.RenotifyTypeConstant;
import com.clustercontrol.notify.entity.MonitorStatusPK;
import com.clustercontrol.notify.model.NotifyHistoryEntity;
import com.clustercontrol.notify.model.NotifyHistoryEntityPK;
import com.clustercontrol.notify.model.NotifyInfo;
import com.clustercontrol.notify.model.NotifyInfoDetail;
import com.clustercontrol.notify.util.MonitorResultStatusUpdater;
import com.clustercontrol.notify.util.NotifyCache;
import com.clustercontrol.notify.util.QueryUtil;
import com.clustercontrol.plugin.impl.AsyncWorkerPlugin;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.XMLUtil;

/**
 * 通知を通知ID毎のキューに振り分ける処理を行うユーティリティクラス
 */
public class NotifyDispatcher {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( NotifyDispatcher.class );

	public static final String MODE_VERBOSE = "verbose";
	public static final String MODE_NORMAL = "normal";

	private static final String NOTIFY_MODE_KEY = "notify.mode";

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
			boolean flag = HinemosPropertyUtil.getHinemosPropertyBool("notify.remove.subkey." + pluginId, false);
			if (flag) {
				info.setSubKey("");
			}
		}

		// 通知対象のリスト分の通知種別を振り分ける。
		String notify_mode = HinemosPropertyUtil.getHinemosPropertyStr(NOTIFY_MODE_KEY, MODE_NORMAL);

		// cc_monitor_statusのcounterを1増やす。
		HashMap<OutputBasicInfo, Boolean> priorityChangeMap = new HashMap<>();
		for (OutputBasicInfo info : outputBasicInfoList) {
			priorityChangeMap.put(info, MonitorResultStatusUpdater.update(info));
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
				m_log.debug("notifyAction() : notifyId = " + notifyId +" invalid.");
				continue;
			}
			
			if (!checkCalendar(notifyInfo.getCalendarId())) {
				m_log.debug("notifyAction() : notifyId = " + notifyId +" calendar is disabled now.");
				continue;
			}

			ArrayList<NotifyRequestMessage> msgList = new ArrayList<NotifyRequestMessage>();
			for (OutputBasicInfo info : outputBasicInfoList) {
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
			Map<NotifyHistoryEntityPK, NotifyHistoryEntity> notifyHistoryEntityMap) {

		JpaTransactionManager jtm = new JpaTransactionManager();
		HinemosEntityManager em = jtm.getEntityManager();

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

		MonitorStatusPK mspk = new MonitorStatusPK(facilityId, pluginId, monitorId, subkey);

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
					m_log.debug("priorityChangeFlag == true. remove entity. pk = " + pkStr);

					// 通知履歴の該当タプルを削除する
					try {
						NotifyHistoryEntity entity = null;
						try {
							entity = getNotifyHistoryEntity(entityPk, notifyHistoryEntityMap);
						} catch (NotifyNotFound e) {
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
					m_log.debug("ValidFlg is invalid. " + pkStr + ", priority = " + priority);
					m_log.debug("notify NG. (VALIDFLAG IS INVALID)." + pkStr);
					isNotifyPriority = false;
				}
			} else if (priority == PriorityConstant.TYPE_WARNING) {
				if (!notifyDetail.getWarnValidFlg().booleanValue()) {
					// 無効の場合は通知しない
					m_log.debug("ValidFlg is invalid. " + pkStr + ", priority = " + priority);
					m_log.debug("notify NG. (VALIDFLAG IS INVALID)." + pkStr);
					isNotifyPriority = false;
				}
			} else if (priority == PriorityConstant.TYPE_CRITICAL) {
				if (!notifyDetail.getCriticalValidFlg().booleanValue()) {
					// 無効の場合は通知しない
					m_log.debug("ValidFlg is invalid. " + pkStr + ", priority = " + priority);
					m_log.debug("notify NG. (VALIDFLAG IS INVALID)." + pkStr);
					isNotifyPriority = false;
				}
			} else if (priority == PriorityConstant.TYPE_UNKNOWN) {
				if (!notifyDetail.getUnknownValidFlg().booleanValue()) {
					// 無効の場合は通知しない
					m_log.debug("ValidFlg is invalid. " + pkStr + ", priority = " + priority);
					m_log.debug("notify NG. (VALIDFLAG IS INVALID)." + pkStr);
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
				counter = MonitorResultStatusUpdater.getCounter(mspk);
			} catch (NotifyNotFound e) {
				// ジョブからの通知はここを通る。
				m_log.debug("notify OK. (MONITOR STATUS NOT FOUND)." + pkStr);
				return isNotifyPriority;
			}

			if(counter >= notifyInfo.getInitialCount()){
				// カウンタが条件を満たしている場合
				m_log.debug("counter check. " + counter + " >= " + notifyInfo.getInitialCount() + "  " + pkStr);

				try{
					NotifyHistoryEntity history = null;
					if(!MODE_VERBOSE.equals(mode)){
						history = getNotifyHistoryEntity(entityPk, notifyHistoryEntityMap);
						// 現在の監視結果の重要度と最終通知時の重要度が異なる場合は通知する
						if (priority != history.getPriority()) {
							m_log.debug("update notify history." + pkStr);
							history.setLastNotify(outputDate.getTime());
							history.setPriority(priority);
							m_log.debug("notify OK. (PRIORITY CHANGE)." + pkStr);
							return isNotifyPriority;
						}
					}

					// 該当のタプルが存在する場合
					// 再通知種別を確認
					if(notifyInfo.getRenotifyType() == RenotifyTypeConstant.TYPE_NO_NOTIFY){
						// 再通知なしの場合
						m_log.debug("notify NG. (RENOTIFY NO)." + pkStr);
						return false;
					} else if(notifyInfo.getRenotifyType() == RenotifyTypeConstant.TYPE_ALWAYS_NOTIFY){
						// 常に再通知の場合
						// history.setLastNotify(new Timestamp(outputDate.getTime())); 常に通知するため更新の必要がない
						// history.setPriority(priority); 常に通知するため更新の必要がない
						m_log.debug("notify OK. (RENOTIFY ALWAYS)." + pkStr);
						return isNotifyPriority;
					} else {
						if (history == null) {
							history = getNotifyHistoryEntity(entityPk, notifyHistoryEntityMap);
						}
						if(outputDate != null && outputDate.getTime() >=
								(history.getLastNotify() + (notifyInfo.getRenotifyPeriod() * 60 * 1000l))){
							m_log.debug("update notify history." + pkStr);
							// 通知時刻が抑制期間を超えている場合
							history.setLastNotify(outputDate.getTime());
							history.setPriority(priority);
							m_log.debug("notify OK. (RENOTIFY PERIOD)." + pkStr);
							return isNotifyPriority;
						} else {
							m_log.debug("notify NG. (RENOTIFY PERIOD)." + pkStr);
							return false;
						}
					}
				} catch (NotifyNotFound e) {
					// 該当のタプルが存在しない場合
					// 初回通知
					m_log.debug("first notify. " + pkStr + ", priority = " + priority);

					// 新規に通知履歴を作成する
					NotifyHistoryEntity newHistoryEntity
					= new NotifyHistoryEntity(facilityId, pluginId, monitorId, notifyId, subkey);
					newHistoryEntity.setLastNotify(outputDate==null?null:outputDate.getTime());
					newHistoryEntity.setPriority(priority);
					m_log.debug("notify OK. (NEW)." + pkStr);
					notifyHistoryEntityMap.put(entityPk, newHistoryEntity);

					if(notifyInfo.getNotFirstNotify().booleanValue()){
						// 再通知なし（初回も通知しない）の場合
						return false;
					}
					return isNotifyPriority;
				}
			} else {
				m_log.debug("notify NG. (PRIORITY CHANGE)." + pkStr);
				return false;
			}
		} catch (Exception e) {
			m_log.warn("notifyCheck() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}

		m_log.info("notifyCheck() notify OK. (cause unexpected errors)" + pkStr);
		return true;
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
				m_log.debug("checkCalendar() " + " calenderId = " + calendarId
						+ ". The notice is not executed because of non-operating day.");
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
}