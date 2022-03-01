/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.util;

import java.util.ArrayList;

import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rap.rwt.SingletonUtil;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.openapitools.client.model.EventCustomCommandInfoResponse;
import org.openapitools.client.model.EventLogInfoRequest;
import org.openapitools.client.model.EventLogInfoResponse;
import org.openapitools.client.model.ExecEventCustomCommandRequest;
import org.openapitools.client.model.ExecEventCustomCommandResponse;
import org.openapitools.client.model.GetEventCustomCommandResultResponse;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.monitor.dialog.EventCustomCommandResultDialog;
import com.clustercontrol.monitor.run.bean.MultiManagerEventDisplaySettingInfo;
import com.clustercontrol.util.HinemosMessage;

import com.clustercontrol.util.Messages;
/**
 * イベントカスタムコマンドの実行結果をポーリングし、コマンドが終了している場合、ダイアログを表示する<BR>
 * 
 */
public class EventCustomCommandResultPoller {
	// ログ
	private static Log m_log = LogFactory.getLog( EventCustomCommandResultPoller.class );
	//同一クライアントでの同時実行可能数
	private static final int m_max_command_execute_num;
	
	static {
		int max_command_execute_num = 5;
		try {
			max_command_execute_num = Integer.parseInt(System.getProperty("maximum.customcmd.exec.num", "5"));
		} catch (NumberFormatException e) {
			m_log.info("System environment value \"maximum.customcmd.exec.num\" is not correct.");
		} finally {
			m_max_command_execute_num = max_command_execute_num;
			m_log.info("max_command_execute_num = " + m_max_command_execute_num);
		}
	}

	//現在の実行数
	private int currentExecuteCount;
	private List<ResultPollingTimer> resultPollingTimerList;
	
	/**
	 * コンストラクタ
	 */
	private EventCustomCommandResultPoller() {
		this.currentExecuteCount = 0;
		this.resultPollingTimerList = new ArrayList<>();
	}

	/**
	 * Singleton
	 */
	public static EventCustomCommandResultPoller getInstance(){
		return SingletonUtil.getSessionInstance( EventCustomCommandResultPoller.class );
	}
	
	public int getCurrentExecute() {
		return this.currentExecuteCount;
	}
	
	/**
	 * イベントカスタムコマンドを実行登録する
	 * 
	 */
	public boolean startEventCustomCommand(String managerName, int commandNo, 
			EventCustomCommandInfoResponse customCommnadInfo, List<EventLogInfoRequest> eventList, MultiManagerEventDisplaySettingInfo eventDisplaySettingInfo) {
		
		synchronized(this) {
			//終了済みのタイマーを開放
			releaseTimer();
			
			if (this.currentExecuteCount >= m_max_command_execute_num) {
				//すでに最大実行数実行中の時、エラーメッセージを表示し、イベントカスタムコマンドは実行しない
				String[] args = { String.valueOf(this.currentExecuteCount) };
				
				MessageDialog.openInformation(
						null,
						Messages.getString("message"),
						Messages.getString("message.monitor.event.customcommand.executing", args));
				return false;
			}
			this.currentExecuteCount++;
		}
		
		String resultId = null;
		
		try {
			MonitorResultRestClientWrapper wrapper = MonitorResultRestClientWrapper.getWrapper(managerName);
			ExecEventCustomCommandRequest execEventCustomCommandRequest = new ExecEventCustomCommandRequest();
			execEventCustomCommandRequest.setCommandNo(commandNo);
			execEventCustomCommandRequest.setEventList(eventList);
			ExecEventCustomCommandResponse res = wrapper.execEventCustomCommand(execEventCustomCommandRequest);
			resultId = res.getCommandResultID();

		} catch (InvalidRole e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
			return false;
		} catch (Exception e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.monitor.event.customcommand.failure") + ", " + HinemosMessage.replace(e.getMessage()));
			return false;
		}
		
		ResultPollingTimer timer = new ResultPollingTimer(managerName, commandNo, eventList, 
			customCommnadInfo, resultId, eventDisplaySettingInfo, Display.getCurrent());
		this.resultPollingTimerList.add(timer);
		timer.start();
		
		return true;
	}

	private void releaseTimer() {
		Iterator<ResultPollingTimer> ite = this.resultPollingTimerList.iterator();
		
		while (ite.hasNext()) {
			ResultPollingTimer timer = ite.next();
			if (timer.isTerminate()) {
				ite.remove();
				this.currentExecuteCount--;
			}
		}
	}
	
	/**
	 * イベントカスタムコマンドの実行結果をポーリングするタイマー
	 *
	 */
	public static class ResultPollingTimer {
		private String managerName;
		private EventCustomCommandInfoResponse customCommnadInfo;
		private String resultId;
		private Timer timer;
		private boolean isTerminate = false; 
		private MultiManagerEventDisplaySettingInfo eventDspSettingInfo = null;
		private Display display;
		
		public ResultPollingTimer(
				String managerName,
				int commandNo,
				List<EventLogInfoRequest> eventList,
				EventCustomCommandInfoResponse customCommnadInfo,
				String resultId,
				MultiManagerEventDisplaySettingInfo eventDspSettingInfo,
				Display display
				) {
			this.managerName = managerName;
			this.customCommnadInfo = customCommnadInfo;
			this.resultId = resultId;
			this.eventDspSettingInfo = eventDspSettingInfo;
			this.display = display;
		}
		
		public void start() {
			this.timer = new Timer(true);
			
			this.timer.schedule(
				new PollingEventCustomCommandTask(this),
				this.customCommnadInfo.getResultPollingDelay(),
				this.customCommnadInfo.getResultPollingInterval());
			
		}
		
		public void pollingResult() {
			//RAPの場合、Settion毎のUIThraed処理から各種処理を実行する必要があるため、
			//実行元SessionのThreadで実行する
			//※以下の処理で invalid thread accessのエラーとなる
			//Endpoint実行、メッセージダイアログ表示、実行結果ダイアログ表示
			display.syncExec(
					new Runnable() {
						@Override
						public void run() {
							pollingResultImpl();
						}
				}
			);
		}
		
		private void pollingResultImpl() {
			GetEventCustomCommandResultResponse result = null;
			
			try {
				MonitorResultRestClientWrapper wrapper = MonitorResultRestClientWrapper.getWrapper(managerName);
				result = wrapper.getEventCustomCommandResult(resultId);
				
				if (result == null) {
					//コマンドが実行中の場合、何もしない
					return;
				}
			} catch (InvalidRole e) {
				// アクセス権なしの場合、エラーダイアログを表示する
				MessageDialog.openInformation(null, Messages.getString("message"), 
						Messages.getString("message.accesscontrol.16"));
			} catch (HinemosUnknown e) {
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.monitor.customcommand.failure") + ", " + HinemosMessage.replace(e.getMessage()));
			} catch (Exception e) {
				MessageDialog.openInformation(null, Messages.getString("message"),
						Messages.getString("message.monitor.customcommand.failure") + ", " + HinemosMessage.replace(e.getMessage()));
			}
			
			//結果が取得できた場合、または異常終了の場合、Timerを停止
			clearTimer();
			
			if (result == null) {
				return;
			}
			
			//結果が取得できた場合はダイアログを表示
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			EventCustomCommandResultDialog resultDialog = new EventCustomCommandResultDialog(
				shell, managerName, customCommnadInfo, result.getEventCustomCommandResultRoot(), eventDspSettingInfo);
			
			resultDialog.open();
		}
		
		
		private void clearTimer() {
			this.timer.cancel();
			this.isTerminate = true;
		}
		
		public boolean isTerminate() {
			return this.isTerminate;
		}
	}
	
	/**
	 * イベントカスタムコマンドをポーリングするタスク
	 *
	 */
	public static class PollingEventCustomCommandTask extends TimerTask {
		// ログ
		private static Log m_log = LogFactory.getLog( PollingEventCustomCommandTask.class );
		
		private ResultPollingTimer timer;
		
		public PollingEventCustomCommandTask(ResultPollingTimer timer) {
			this.timer = timer;
		}
		
		@Override
		public void run() {
			try {
				this.timer.pollingResult();
			} catch (Exception e) {
				m_log.warn("polling event custom command result fail", e);
			}
		}
		
	}
}
