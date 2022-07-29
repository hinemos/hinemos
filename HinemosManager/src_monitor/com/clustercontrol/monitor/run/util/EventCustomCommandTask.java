/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */


package com.clustercontrol.monitor.run.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.EventLogNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.monitor.bean.EventCustomCommandResultRoot;
import com.clustercontrol.monitor.bean.EventCustomCommandInfo;
import com.clustercontrol.monitor.bean.EventCustomCommandResult;
import com.clustercontrol.monitor.bean.EventCustomCommandStatusConstant;
import com.clustercontrol.monitor.bean.EventDataInfo;
import com.clustercontrol.monitor.bean.EventLogHistoryTypeConstant;
import com.clustercontrol.monitor.run.util.EventCustomCommandExecutor.ExecutorResult;
import com.clustercontrol.monitor.session.EventCustomCommandBean.LockManager;
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.notify.monitor.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * イベントカスタムコマンド実行クラス
 * 
 * 指定された複数件のイベントを１件ずつコマンドに渡して処理する
 *
 */
public class EventCustomCommandTask implements Runnable {
	
	private static Log m_log = LogFactory.getLog(EventCustomCommandTask.class);
	
	/** タイムアウトでキャンセル */
	private static final int CANCELTYPE_TIMEOUT = 1;
	/** マネージャ終了でキャンセル */
	private static final int CANCELTYPE_MANANGER_TERMNATE = 2;
	
	private LockManager lockManager = null;
	private EventCustomCommandInfo commandInfo;
	private List<EventDataInfo> eventList;
	private EventCustomCommandResultRoot result;
	private String operationUser;
	private Long commandKickTime;
	private String commandResultId;
	
	/**
	 * コンストラクタ
	 * 
	 * @param lockManager
	 * @param comamndInfo
	 * @param eventList
	 * @param operationUser
	 * @param commandKickTime
	 */
	public EventCustomCommandTask(
			LockManager lockManager,
			int commandNo,
			EventCustomCommandInfo comamndInfo, 
			List<EventDataInfo> eventList,
			String operationUser,
			Long commandKickTime
			) {
		this.lockManager = lockManager;
		this.commandInfo = comamndInfo;
		this.eventList = eventList;
		this.operationUser = operationUser;
		this.commandKickTime = commandKickTime;
		
		//結果オブジェクトの初期化
		this.result = new EventCustomCommandResultRoot();
		this.result.setCommandNo(commandNo);
		this.result.setEventResultList(new ArrayList<>());
		this.result.setCount(eventList.size());
		this.result.setCommandKickTime(commandKickTime);
		
		for (int i = 0; i < eventList.size() ;i++) {
			//DBからのレコード取得用のキー、イベント番号のみコピーする
			//それ以外の情報はコマンドでの操作で変更される可能性もあり、
			//ユーザに表示できる内容が担保できないため、使用しない／画面表示しない
			
			EventCustomCommandResult res = new EventCustomCommandResult();
			this.result.getEventResultList().add(res);
			EventDataInfo eventSrc = eventList.get(i);
			EventDataInfo eventKey = new EventDataInfo();
			eventKey.setMonitorId(eventSrc.getMonitorId());
			eventKey.setMonitorDetailId(eventSrc.getMonitorDetailId());
			eventKey.setPluginId(eventSrc.getPluginId());
			eventKey.setOutputDate(eventSrc.getOutputDate());
			eventKey.setFacilityId(eventSrc.getFacilityId());
			eventKey.setPosition(eventSrc.getPosition());

			res.setEvent(eventKey);
		}
	}

	/**
	 * タスクと紐づけられたコマンド実行結果ID
	 * 
	 * @param commandResultId
	 */
	public void setResultId(String commandResultId) {
		this.commandResultId = commandResultId;
	}
	
	@Override
	public void run() {
		try {
			m_log.debug("run()");
			
			this.result.setCommandStartTime(HinemosTime.currentTimeMillis());
			
			int timeoutIndex = -1;
			
			for (int i = 0; i < eventList.size() ;i++) {
				EventDataInfo info = eventList.get(i);
				EventCustomCommandResult res = this.result.getEventResultList().get(i);
				String key = this.createKey(info);
				
				//同一イベントに対して、ロック取得（同一のイベントに対して、他で実行中の場合、ここで待つ）
				lockManager.getLock(key);
				try {
					
					/* イベント毎の処理を開始 */
					boolean timeout = runEvent(info, res);
					
					if (timeout) {
						//タイムアウトでコマンドがキャンセルされた場合、以降のイベントは処理しない
						timeoutIndex = i;
						break;
					}
				} finally {
					//イベントに対するロックをリリース
					lockManager.releaseLock(key);
				}
				
				if (commandInfo.getRunInterval() > 0 && i != (eventList.size() - 1)) {
					//流量制御
					//実行間隔の指定がある場合 かつ 最後のイベント以外の場合、Sleepする
					//runintervalを使用する場合はthreadが1に設定してもらう前提
					
					try {
						Thread.sleep(commandInfo.getRunInterval());
					} catch (InterruptedException e) {
						//ignore
					}
				}
			}
			
			if (timeoutIndex >= 0) {
				//タイムアウトが発生した場合、以降のコマンドの実行結果をキャンセルに変更する
				for (int i = timeoutIndex + 1; i < eventList.size();i++) {
					EventDataInfo info = eventList.get(i);
					EventCustomCommandResult res = this.result.getEventResultList().get(i);
					this.cancelEvent(info, res, CANCELTYPE_TIMEOUT);
				}
			}
			
			//実行結果をチェックし、実行結果エラー、キャンセルのコマンドがある場合、通知する
			for (EventCustomCommandResult res : this.result.getEventResultList())
				if (res.getStatus() == EventCustomCommandStatusConstant.STATUS_ERROR
					|| res.getStatus() == EventCustomCommandStatusConstant.STATUS_CANCEL) {
					internalNotify();
					break;
			}
			
			this.result.setCommandEndTime(HinemosTime.currentTimeMillis());
		} catch (RuntimeException e) {
			//findbugs対応 RuntimeException のキャッチを明示化
			m_log.warn(e);
		} catch (Exception e) {
			m_log.warn(e);
		}
	}
	
	public void cancelAll() {
		for (int i = 0; i < eventList.size() ;i++) {
			EventDataInfo info = eventList.get(i);
			EventCustomCommandResult res = this.result.getEventResultList().get(i);
			this.cancelEvent(info, res, CANCELTYPE_MANANGER_TERMNATE);
		}
		
		internalNotify();
	}
	
	
	private void internalNotify() {
		String[] msgArgs = {
				this.commandInfo.getDisplayName(),
				this.operationUser,
				new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(this.commandKickTime))};
		
		AplLogger.put(InternalIdCommon.MON_EVT_SYS_001, msgArgs);
	}
	
	/**
	 * 実行結果
	 * @return
	 */
	public EventCustomCommandResultRoot getResult() {
		return this.result;
	}
	
	/**
	 * イベントに対してイベントカスタムコマンドを実行する
	 * 
	 * @param info
	 * @param res
	 * @return
	 */
	private boolean runEvent(EventDataInfo info, EventCustomCommandResult res) {
		boolean isTimeout = false;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			//イベント情報を取得する
			EventLogEntity event = getEventLogEntity(info, res);
			
			if (event == null) {
				return false;
			}
			
			//開始ログを登録
			Long startTime = HinemosTime.currentTimeMillis();
			EventLogOperationHistoryUtil.addEventLogOperationHistory(
					jtm, event, startTime, this.operationUser, EventLogHistoryTypeConstant.TYPE_COMMAND_START,
					MessageConstant.MESSAGE_EVENT_CUSTOM_COMMAND_START.getMessage(commandInfo.getDisplayName())
				);
			jtm.commit();
			
			//コマンドを実行する
			res.setStartTime(startTime);
			isTimeout = executeCommand(event, res);
			
			Long endTime = HinemosTime.currentTimeMillis();
			res.setEndTime(endTime);
			
			//終了ログを登録
			jtm.begin();
			EventLogOperationHistoryUtil.addEventLogOperationHistory(
					jtm, event, endTime, this.operationUser, 
					EventLogHistoryTypeConstant.TYPE_COMMAND_DETAIL,res.getMessage()
					);
			
			EventLogOperationHistoryUtil.addEventLogOperationHistory(
					jtm, event, endTime, this.operationUser, 
					EventLogHistoryTypeConstant.TYPE_COMMAND_END,
					MessageConstant.MESSAGE_EVENT_CUSTOM_COMMAND_END.getMessage(
							commandInfo.getDisplayName(),
							MessageConstant.getMessageId(EventCustomCommandStatusConstant.statusToMessageCode(res.getStatus())),
							res.getReturnCode() != null ? res.getReturnCode().toString() : ""
							)
					);
			
			jtm.commit();
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return isTimeout;
	}
	
	/**
	 * イベントキャンセル処理
	 * 
	 * @param info イベント情報
	 * @param res イベント実行結果格納先
	 * @param cancelType キャンセルの種別(先行タイムアウト OR マネージャ停止)
	 */
	private void cancelEvent(EventDataInfo info, EventCustomCommandResult res, int cancelType) {
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
		
			jtm.begin();
			
			EventLogEntity event = getEventLogEntity(info, res);
			
			String message = "";
			
			switch (cancelType) {
			case CANCELTYPE_TIMEOUT:
				//タイムアウトによる後続処理キャンセルの場合
				message = MessageConstant.MESSAGE_EVENT_CUSTOM_COMMAND_SKIP_BY_TIMEOUT.getMessage();
				break;
			case CANCELTYPE_MANANGER_TERMNATE:
				//マネージャ停止による処理キャンセルの場合
				message = MessageConstant.MESSAGE_EVENT_CUSTOM_COMMAND_SKIP_BY_TERMINATE.getMessage(commandInfo.getDisplayName());
				break;
			default:
				//到達しない
				break;
			}
			
			
			//起動していないため、開始時間、終了時間はセットしない（null）
			res.setStatus(EventCustomCommandStatusConstant.STATUS_CANCEL);
			res.setMessage(message);
			
			if (event == null) {
				return;
			}
			
			Long cancelTime = HinemosTime.currentTimeMillis();
			
			String logMsg = MessageConstant.MESSAGE_EVENT_CUSTOM_COMMAND_NUM.getMessage(commandInfo.getDisplayName()) + ":" + message;
			
			EventLogOperationHistoryUtil.addEventLogOperationHistory(
					jtm, event, cancelTime, this.operationUser, 
					EventLogHistoryTypeConstant.TYPE_COMMAND_SKIP,
					logMsg);
			
			jtm.commit();
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
	}
	
	/**
	 * 引数のイベント情報のキーを元にエンティティのレコードを取得する。
	 * 
	 * @param info
	 * @param res
	 * @return
	 */
	private EventLogEntity getEventLogEntity(EventDataInfo info, EventCustomCommandResult res) {
		EventLogEntity ret = null;
		try {
			ret = QueryUtil.getEventLogPK(
				info.getMonitorId(), info.getMonitorDetailId(), info.getPluginId(),
				info.getOutputDate(), info.getFacilityId(), ObjectPrivilegeMode.MODIFY);
		} catch (EventLogNotFound e) {
			res.setStatus(EventCustomCommandStatusConstant.STATUS_ERROR);
			
			res.setMessage(
					MessageConstant.MESSAGE_EVENT_CUSTOM_COMMAND_EVENT_NOT_FOUND.getMessage(commandInfo.getDisplayName()));
					
			m_log.info("event log not found." 
							+ "custom event = " + commandInfo.getDisplayName() 
							+ " event key = " + info.toString() 
							+ " id = " + commandResultId);
		} catch (InvalidRole e) {
			res.setStatus(EventCustomCommandStatusConstant.STATUS_ERROR);
			res.setMessage(
					MessageConstant.MESSAGE_EVENT_CUSTOM_COMMAND_INVALID_ROLE.getMessage(commandInfo.getDisplayName()));
			
			m_log.info("event log invalid role." 
					+ "custom event = " + commandInfo.getDisplayName() 
					+ " event key = " + info.toString() 
					+ " id = " + commandResultId);
		}
		return ret;
	}
	
	/**
	 * コマンドを実行し、実行結果をEventCustomCommandResultに格納する。
	 * 
	 * @param info イベント情報
	 * @param res 実行結果設定先
	 * @return
	 */
	private boolean executeCommand(EventLogEntity event, EventCustomCommandResult res) {
		
		//コマンド実行
		EventCustomCommandExecutor executor = new EventCustomCommandExecutor(this.commandInfo, event);
		ExecutorResult exectorResult = executor.executeCommand();
		
		//ExecutorResultをEventCustomCommandResultに変換する
		if (exectorResult.getTimeout()) {
			//タイムアウトで中断された場合
			res.setStatus(EventCustomCommandStatusConstant.STATUS_CANCEL);
			res.setReturnCode(null);
			res.setMessage(MessageConstant.MESSAGE_EVENT_CUSTOM_COMMAND_OUT.getMessage(
					"", MessageConstant.MESSAGE_EVENT_CUSTOM_COMMAND_CANCEL_BY_TIMEOUT.getMessage()
					));
		} else if (exectorResult.getError()) {
			//コマンドが正常に実行されなかった場合
			res.setStatus(EventCustomCommandStatusConstant.STATUS_ERROR);
			res.setReturnCode(null);
			res.setMessage(MessageConstant.MESSAGE_EVENT_CUSTOM_COMMAND_OUT.getMessage(
					exectorResult.getStdout(), exectorResult.getStderr()
					));
		} else {
			//コマンドが正常に終了した場合
			
			//リターンコードでステータスを判定
			if (exectorResult.getReturnCode() == null 
					|| exectorResult.getReturnCode() >= this.commandInfo.getErrorRc()) {
				//リターンコードがエラー下限リターンコード以上の場合
				res.setStatus(EventCustomCommandStatusConstant.STATUS_ERROR);
			} else if (exectorResult.getReturnCode() >= this.commandInfo.getWarnRc()) {
				//リターンコードが警告下限リターンコード以上の場合
				res.setStatus(EventCustomCommandStatusConstant.STATUS_WARNING);
			} else {
				//エラー、警告でない場合、正常と判断
				res.setStatus(EventCustomCommandStatusConstant.STATUS_NORMAL);
			}
			res.setReturnCode(exectorResult.getReturnCode());
			res.setMessage(MessageConstant.MESSAGE_EVENT_CUSTOM_COMMAND_OUT.getMessage(
					exectorResult.getStdout(), exectorResult.getStderr()
					));
		}
		
		return exectorResult.getTimeout();
	}

	/**
	 * イベント情報のキーを生成する
	 * 
	 * @param record
	 * @return
	 */
	private String createKey(EventDataInfo record) {
		final String spearator = "|";
		
		return  record.getMonitorId() + spearator +
				record.getMonitorDetailId() + spearator +
				record.getPluginId() + spearator +
				record.getOutputDate() + spearator +
				record.getFacilityId();
	}
}