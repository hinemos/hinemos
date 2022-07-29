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
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.HinemosManagerMain;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JdbcBatchExecutor;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.monitor.bean.ConfirmConstant;
import com.clustercontrol.monitor.bean.EventHinemosPropertyConstant;
import com.clustercontrol.monitor.bean.EventUserExtensionItemInfo;
import com.clustercontrol.monitor.factory.SelectEventHinemosProperty;
import com.clustercontrol.monitor.run.util.EventCacheModifyCallback;
import com.clustercontrol.monitor.run.util.EventUtil;
import com.clustercontrol.notify.bean.NotifyRequestMessage;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.model.NotifyEventInfo;
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.notify.monitor.model.EventLogEntityPK;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;


/**
 * イベント情報を更新するクラス<BR>
 *
 * @version 4.0.0
 * @since 3.0.0
 */
public class OutputEvent implements DependDbNotifier {

	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog( OutputEvent.class );

	/**
	 * 最終出力日時
	 * cc_event_logテーブルは主キーに出力日時を含む。
	 * 同じプラグインID,監視項目ID, ファシリティIDの同一イベントを出力するためには、出力日時を変更する必要がある
	 * **/
	private volatile static long _lastOutputDateTime = 0;

	/**
	 * イベントの出力を行います。
	 * 
	 * @param message 出力・通知情報
	 */
	@Override
	public void notify(NotifyRequestMessage message){
		outputEvent(message.getOutputInfo(), message.getNotifyId());
	}
	
	public EventLogEntity outputEvent(OutputBasicInfo outputInfo, String notifyId) {
		return outputEvent(outputInfo, notifyId, null, null, null);
	}

	/**
	 * イベントの出力を行います。
	 * 
	 * @param outputInfo 出力・通知情報
	 * @param notifyId 通知ID
	 * @param ownerRoleId オーナーロールID
	 * @param notifyEventInfoEntity イベント通知
	 * @param userExtenstionItemInfoMap　ユーザ拡張イベント項目の設定
	 * @return イベントログ
	 */
	private EventLogEntity outputEvent(
			OutputBasicInfo outputInfo,
			String notifyId, 
			String ownerRoleId,
			NotifyEventInfo notifyEventInfoEntity, 
			Map<Integer, EventUserExtensionItemInfo> userExtenstionItemInfoMap
			) {
		m_log.debug("outputEvent() notifyId=" + notifyId + ", ownerRoleId=" + ownerRoleId);
		if(notifyId == null || "SYS".equals(outputInfo.getPluginId())) {
			// インターナルイベントの場合
			return this.insertEventLog(outputInfo, ConfirmConstant.TYPE_UNCONFIRMED);
		} else {
			if (notifyEventInfoEntity == null) {
				try {
					notifyEventInfoEntity = QueryUtil.getNotifyEventInfoPK(notifyId);
				} catch (NotifyNotFound e) {
					m_log.debug("notify not found.", e);
				}
			}

			if (notifyEventInfoEntity != null) {
				Integer eventNormalState = getEventNormalState(notifyEventInfoEntity, outputInfo);
				return insertEventLog(outputInfo, eventNormalState, ownerRoleId, userExtenstionItemInfoMap, null);
			}
		}

		return null;
	}

	private Integer getEventNormalState(NotifyEventInfo notifyEventInfo, OutputBasicInfo outputInfo) {
		int priority = outputInfo.getPriority();
		switch (priority) {
		case PriorityConstant.TYPE_INFO:
			return notifyEventInfo.getInfoEventNormalState();
		case PriorityConstant.TYPE_WARNING:
			return notifyEventInfo.getWarnEventNormalState();
		case PriorityConstant.TYPE_CRITICAL:
			return notifyEventInfo.getCriticalEventNormalState();
		case PriorityConstant.TYPE_UNKNOWN:
			return notifyEventInfo.getUnknownEventNormalState();

		default:
			break;
		}
		m_log.warn("priority=" + priority + ", id=" + notifyEventInfo.getNotifyId() + ", output=" + outputInfo.getMonitorId());
		return null;
	}

	/**
	 * イベント情報を作成します。
	 * 
	 * @param output 出力情報
	 * @param confirmState 確認フラグ ConfirmConstant.TYPE_UNCONFIRMED / ConfirmConstant.TYPE_CONFIRMING/ ConfirmConstant.TYPE_CONFIRMED
	 *
	 * @return
	 */
	public EventLogEntity insertEventLog(OutputBasicInfo output, int confirmState) {
		return insertEventLog(output, confirmState, null);
	}
	
	/**
	 * イベント情報を作成します。
	 * @param output 出力情報
	 * @param confirmState 確認フラグ ConfirmConstant.TYPE_UNCONFIRMED / ConfirmConstant.TYPE_CONFIRMING/ ConfirmConstant.TYPE_CONFIRMED
	 * @param ownerRoleId オーナーロールID
	 * @param userExtenstionItemInfoMap ユーザ拡張イベント項目の設定 
	 * @param ownerRoleIdNotBatch オーナーロールID（BatchInser以外） 
	 *        ※本来はbatchInsertフラグを用意し、そちらを使用すべきだが、
	 *          現在のownerRoleId指定ありの場合、batchInsertするという実装を修正した場合の影響範囲が
	 *          大きいので本IFとした
	 * @return
	 */
	public EventLogEntity insertEventLog(
			OutputBasicInfo output, 
			int confirmState, 
			String ownerRoleId, 
			Map<Integer, EventUserExtensionItemInfo> userExtenstionItemInfoMap,
			String ownerRoleIdNotBatch) {
		if (m_log.isDebugEnabled()) {
			m_log.debug("start event creation. " + eventLogToString(output));
		}
		if (userExtenstionItemInfoMap == null) {
			userExtenstionItemInfoMap = SelectEventHinemosProperty.getEventUserExtensionItemInfo();
		}
		
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			// 出力日時を生成
			Long outputDate = createOutputDate();

			// インスタンス生成
			EventLogEntity entity = null;
			EventLogEntityPK pk = new EventLogEntityPK(
					output.getMonitorId(),
					output.getSubKey(),
					output.getPluginId(),
					outputDate,
					output.getFacilityId());
			entity = new EventLogEntity(pk);
			if (ownerRoleId != null) {
				entity.setOwnerRoleId(ownerRoleId);
			} else if (ownerRoleIdNotBatch != null) {
				entity.setOwnerRoleId(ownerRoleIdNotBatch);
			} else {
				entity.setOwnerRoleId(NotifyUtil.getOwnerRoleId(pk.getPluginId(), pk.getMonitorId(),
						pk.getMonitorDetailId(), pk.getFacilityId(), true));
			}

			entity.setApplication(output.getApplication());
			entity.setComment("");
			entity.setCommentDate(null);
			entity.setCommentUser("");
			entity.setConfirmDate(null);
			entity.setConfirmFlg(confirmState);
			entity.setConfirmUser("");
			entity.setDuplicationCount(0l);
			entity.setGenerationDate(output.getGenerationDate());
			entity.setInhibitedFlg(false);
			String message = output.getMessage();
			entity.setMessage(getNotifyEventMessageMaxString(message));
			entity.setMessageOrg(getNotifyEventMessageOrgMaxString(output.getMessageOrg()));
			entity.setPriority(output.getPriority());
			entity.setScopeText(output.getScopeText());
			entity.setCollectGraphFlg(false);
			entity.setNotifyUUID(output.getNotifyUUID());
			for (int i = 1 ; i <= EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
				//指定された値 OR デフォルト値を設定
				String setValue = getUserItemSetValue(NotifyUtil.getUserItemValue(output, i), userExtenstionItemInfoMap.get(i));
				EventUtil.setUserItemValue(entity, i, setValue);
			}
			
			if (m_log.isDebugEnabled()) {
				m_log.debug("created event successfully. " + eventLogToString(output));
			}
			
			if (ownerRoleId == null) {
				//ownerRoleID = nullの場合
				//EventLogEntityJdbcBatchInsertでinsertしない場合
				//→　persistしてもOK
				//ownerRoleID != nullの場合はEventLogEntityJdbcBatchInsertでSQLを直接実行し、
				//insertするのでpersistはNG
				em.persist(entity);
				em.flush();
			}
			
			jtm.addCallback(new EventCacheModifyCallback(true, entity));

			return entity;
		}
	}
	
	/**
	 * イベントログの文字列表現を取得
	 */
	private static String eventLogToString(OutputBasicInfo output) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append("monitorId=");
		sb.append(output.getMonitorId());
		sb.append(", pluginId=");
		sb.append(output.getPluginId());
		sb.append(", facilityId=");
		sb.append(output.getFacilityId());
		sb.append(", generationDate=");
		sb.append(output.getGenerationDate());
		sb.append(", priority=");
		sb.append(output.getPriority());
		sb.append(", message=");
		sb.append(output.getMessage());
		for (int i = 1; i < EventHinemosPropertyConstant.USER_ITEM_SIZE; i++) {
			sb.append(String.format(", userItem%02d=%s", i, NotifyUtil.getUserItemValue(output, i)));
		}
		sb.append(")");
		
		return sb.toString();
	}
	
	/**
	 * ユーザ拡張イベント項目の値をセット
	 * 
	 * value 通知で指定されたユーザ拡張イベント項目の値
	 * itemInfo ユーザ拡張イベント項目の設定(Hinemosプロパティ)
	 * 
	 */
	private static String getUserItemSetValue(String value, EventUserExtensionItemInfo itemInfo) {
		if (value != null) {
			//指定がある場合、値を使用
			//(空文字の場合も空文字が指定されたと認識する)
			return value;
		}
		if (itemInfo.getRegistInitValue() != null && !"".equals(itemInfo.getRegistInitValue())) {
			//初期値登録されている場合
			return itemInfo.getRegistInitValue();
		}
		return null;
	}

	/**
	 * イベント情報を作成します。
	 * @param output 出力情報
	 * @param confirmState 確認フラグ ConfirmConstant.TYPE_UNCONFIRMED / ConfirmConstant.TYPE_CONFIRMING/ ConfirmConstant.TYPE_CONFIRMED
	 * @param ownerRoleId オーナーロールID
	 * @return
	 */
	public EventLogEntity insertEventLog(OutputBasicInfo output, int confirmState, String ownerRoleId) {
		return insertEventLog(output, confirmState, ownerRoleId, null, null);
	}

	/**
	 * イベントの出力日時を払い出します。
	 * 前回払い出した日時とは重複しないように払い出します。
	 * 
	 * @return 出力日時
	 */
	private static synchronized Long createOutputDate(){
		// 現在時刻を取得
		long now = HinemosTime.currentTimeMillis();
		now = now - now % HinemosManagerMain._instanceCount + HinemosManagerMain._instanceId;
		long outputDateTime = 0;

		if((_lastOutputDateTime - 1000) < now && now <= _lastOutputDateTime){
			// 現在時刻と最終出力日時の誤差が1秒以内であり（時刻同期により大幅にずれた場合を想定）、
			// 現在時刻が最後に払い出した出力日時より昔の場合は、最終出力日時より1msだけ進める
			outputDateTime = _lastOutputDateTime + HinemosManagerMain._instanceCount;
			if (m_log.isDebugEnabled()) {
				m_log.debug("create OutputDate=" + outputDateTime);
			}
		} else {
			outputDateTime = now;
		}

		_lastOutputDateTime = outputDateTime;

		return Long.valueOf(outputDateTime);
	}

	/**
	 * イベント通知のオリジナルメッセージをHinemosプロパティ定義によって切断する。
	 * @param messageOrg イベント通知のオリジナルメッセージ
	 * @return
	 */
	public static String getNotifyEventMessageOrgMaxString(String messageOrg) {
		return getMaxString(HinemosPropertyCommon.notify_event_messageorg_max_length, messageOrg);
	}

	/**
	 * イベント通知のメッセージをHinemosプロパティ定義によって切断する。
	 * @param message イベント通知のメッセージ
	 * @return
	 */
	public static String getNotifyEventMessageMaxString(String message) {
		return getMaxString(HinemosPropertyCommon.notify_event_message_max_length, message);
	}

	/**
	 * 指定された文字列がHinemosプロパティ上で定義されているサイズよりも長い場合に切断する。
	 * @param hinemosPropertyCommon Hinemosプロパティ
	 * @param targetString 対象文字列
	 * @return
	 */
	private static String getMaxString(HinemosPropertyCommon hinemosPropertyCommon, String targetString) {
		if (targetString == null) {
			return targetString;
		}
		int maxLen = hinemosPropertyCommon.getIntegerValue();
		String returnString = null;
		if (targetString.length() <= maxLen) {
			returnString = targetString;
		} else {
			returnString = targetString.substring(0, maxLen);
		}
		return returnString;
	}

	public List<EventLogEntity> notify(List<NotifyRequestMessage> msgList) throws NotifyNotFound {
		if (msgList.isEmpty()) {
			return new ArrayList<EventLogEntity>();
		}
		
		m_log.debug("notify() : ownerRoleId=" + NotifyUtil.getOwnerRoleId(msgList.get(0), true));
		//一括通知で通知するメッセージは同一ロールなので、1個目のロールIDを利用する。
		
		NotifyEventInfo notifyEvent = null;
		if (msgList.get(0).getNotifyId() != null) {
			// NOTIFY_IDを保持しないイベントも通知される（HAにおけるセルフチェック通知など)
			notifyEvent = getNotifyEventInfo(msgList.get(0));
		}
		
		List<EventLogEntity> entities = collectEntities(msgList, NotifyUtil.getOwnerRoleId(msgList.get(0), true), notifyEvent);
		JdbcBatchExecutor.execute(new EventLogEntityJdbcBatchInsert(entities));
		for (EventLogEntity e : entities) {
			new JpaTransactionManager().addCallback(new EventCacheModifyCallback(true, e));
		}
		return entities;
	}

	private List<EventLogEntity> collectEntities(
			List<NotifyRequestMessage> msgList, String ownerRoleId,
			NotifyEventInfo notifyEventInfoEntity) {
		
		//ユーザ拡張イベント項目のHinemosプロパティを取得
		Map<Integer, EventUserExtensionItemInfo> userItemInfoMap = SelectEventHinemosProperty.getEventUserExtensionItemInfo();
		
		List<EventLogEntity> entities = new ArrayList<EventLogEntity>();
		for (NotifyRequestMessage msg : msgList) {
			entities.add(
					outputEvent(
							msg.getOutputInfo(),
							msg.getNotifyId(),
							ownerRoleId,
							notifyEventInfoEntity,
							userItemInfoMap
					)
			);
		}
		return entities;
	}

	private NotifyEventInfo getNotifyEventInfo(
			NotifyRequestMessage notifyRequestMessage) throws NotifyNotFound {
		return QueryUtil.getNotifyEventInfoPK(notifyRequestMessage.getNotifyId());
	}
}
