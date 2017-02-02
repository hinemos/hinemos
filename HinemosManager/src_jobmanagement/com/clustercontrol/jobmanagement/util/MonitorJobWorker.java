/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.util;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.bean.RunInterval;
import com.clustercontrol.commons.bean.SettingUpdateInfo;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.custom.util.CustomManagerUtil;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobInfoNotFound;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;
import com.clustercontrol.jobmanagement.bean.CommandConstant;
import com.clustercontrol.jobmanagement.bean.CommandStopTypeConstant;
import com.clustercontrol.jobmanagement.bean.CommandTypeConstant;
import com.clustercontrol.jobmanagement.bean.MonitorJobConstant;
import com.clustercontrol.jobmanagement.bean.QuartzConstant;
import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.jobmanagement.bean.RunResultInfo;
import com.clustercontrol.jobmanagement.bean.RunStatusConstant;
import com.clustercontrol.jobmanagement.factory.JobSessionJobImpl;
import com.clustercontrol.jobmanagement.model.JobInfoEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.session.JobRunManagementBean;
import com.clustercontrol.logfile.util.LogfileManagerUtil;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.plugin.impl.SchedulerPlugin;
import com.clustercontrol.plugin.impl.SchedulerPlugin.SchedulerType;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.winevent.util.WinEventManagerUtil;

/**
 * HinemosManager上でジョブを実行するクラス<BR>
 */
public class MonitorJobWorker {

	// Logger
	private static Log m_log = LogFactory.getLog(MonitorJobWorker.class);
	
	private static ExecutorService service;
	private static String workerName = "MonitorJobWorker";

	// 監視ジョブ情報を保持（監視種別ID、監視情報）
	private static ConcurrentHashMap<String, ConcurrentHashMap<RunInstructionInfo, MonitorInfo>> monitorJobMap
		= new ConcurrentHashMap<>();

	// カスタム監視（数値）前回値を保持（指示情報、値）
	private static ConcurrentHashMap<RunInstructionInfo, Object> prevMonitorValueMap
		= new ConcurrentHashMap<>();

	static {
		int maxThreadPoolSize = HinemosPropertyUtil.getHinemosPropertyNum("job.monitor.thread.pool.size", Long.valueOf(5)).intValue();

		service = new MonitoredThreadPoolExecutor(maxThreadPoolSize, maxThreadPoolSize,
				0L, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<Runnable>(),
				new ThreadFactory() {
			private volatile int _count = 0;
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, workerName + "-" + _count++);
			}
		}, new ThreadPoolExecutor.AbortPolicy());

		// Mapの設定
		monitorJobMap.put(HinemosModuleConstant.MONITOR_SYSTEMLOG, 
				new ConcurrentHashMap<RunInstructionInfo, MonitorInfo>());
		monitorJobMap.put(HinemosModuleConstant.MONITOR_SNMPTRAP, 
				new ConcurrentHashMap<RunInstructionInfo, MonitorInfo>());
		monitorJobMap.put(HinemosModuleConstant.MONITOR_LOGFILE, 
				new ConcurrentHashMap<RunInstructionInfo, MonitorInfo>());
		monitorJobMap.put(HinemosModuleConstant.MONITOR_WINEVENT, 
				new ConcurrentHashMap<RunInstructionInfo, MonitorInfo>());
		monitorJobMap.put(HinemosModuleConstant.MONITOR_CUSTOM_N, 
				new ConcurrentHashMap<RunInstructionInfo, MonitorInfo>());
		monitorJobMap.put(HinemosModuleConstant.MONITOR_CUSTOM_S, 
				new ConcurrentHashMap<RunInstructionInfo, MonitorInfo>());
		monitorJobMap.put(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N, 
				new ConcurrentHashMap<RunInstructionInfo, MonitorInfo>());
		monitorJobMap.put(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S, 
				new ConcurrentHashMap<RunInstructionInfo, MonitorInfo>());
	}

	/**
	 * ジョブ実行処理
	 * 
	 * @param runInstructionInfo 実行指示
	 */
	public static void runJob (RunInstructionInfo runInstructionInfo) {

		m_log.info("runJob() SessionID=" + runInstructionInfo.getSessionId()
				+ ", JobunitID=" + runInstructionInfo.getJobunitId()
				+ ", JobID=" + runInstructionInfo.getJobId()
				+ ", FacilityID=" + runInstructionInfo.getFacilityId()
				+ ", CommandType=" + runInstructionInfo.getCommandType());
		//実行履歴チェック
		try {
			// タスクを実行する
			service.execute(new JobMonitorTask(runInstructionInfo));
		} catch(Throwable e) {
			m_log.warn("runJob() Error : " + e.getMessage());
		}
	}

	/**
	 * 監視ジョブを終了する。
	 * 
	 * @param runInstructionInfo 実行指示
	 * @param priority 優先度
	 * @param messageOrg オリジナルメッセージ
	 * @param monitorTypeId 監視種別ID
	 */
	public static void endMonitorJob(
			RunInstructionInfo runInstructionInfo,
			String monitorTypeId,
			String message,
			String errorMessage,
			Integer status,
			Integer endValue) {

		// メッセージ送信
		execJobEndNode(runInstructionInfo, status,
				message, errorMessage, endValue);

		if (monitorTypeId != null) {
			if (monitorTypeId.equals(HinemosModuleConstant.MONITOR_LOGFILE)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_SNMPTRAP)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_SYSTEMLOG)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_WINEVENT)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_CUSTOM_N)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_CUSTOM_S)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S)) {
				// キャッシュから削除
				removeMonitorJobMap(monitorTypeId, runInstructionInfo);
			}

			if (monitorTypeId.equals(HinemosModuleConstant.MONITOR_CUSTOM_N)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_JMX)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_SNMP_N)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_PERFORMANCE)) {
				// 前回値削除
				removePrevMonitorValue(runInstructionInfo);
				// スケジューラ削除
				if (!monitorTypeId.equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N)) {
					try {
						deleteSchedule(runInstructionInfo);
					} catch (HinemosUnknown e) {
						// エラーとしない
						m_log.debug("schedule is not found.");
					}
				}
			}
		}
		// 履歴削除
		RunHistoryUtil.delRunHistory(runInstructionInfo);
	}

	/**
	 * endNode()を実行する
	 * 
	 * @param runInstructionInfo 実行指示
	 * @param status ステータス
	 * @param endValue 終了値
	 * @param message メッセージ
	 * @param errorMessage エラーメッセージ
	 * @return 処理結果
	 */
	private static boolean execJobEndNode(
			RunInstructionInfo runInstructionInfo,
			Integer status,
			String message,
			String errorMessage,
			Integer endValue) {

		boolean rtn = false;

		// メッセージ作成
		RunResultInfo resultInfo = new RunResultInfo();
		resultInfo.setSessionId(runInstructionInfo.getSessionId());
		resultInfo.setJobunitId(runInstructionInfo.getJobunitId());
		resultInfo.setJobId(runInstructionInfo.getJobId());
		resultInfo.setFacilityId(runInstructionInfo.getFacilityId());
		resultInfo.setCommand(runInstructionInfo.getCommand());
		resultInfo.setCommandType(runInstructionInfo.getCommandType());
		resultInfo.setStopType(runInstructionInfo.getStopType());
		resultInfo.setStatus(status);
		resultInfo.setMessage(message);
		resultInfo.setErrorMessage(errorMessage);
		resultInfo.setTime(HinemosTime.getDateInstance().getTime());
		resultInfo.setEndValue(endValue);
		try {
			boolean isMonitorNestedEm = false;
			JpaTransactionManager jtm = new JpaTransactionManager();
			if (jtm.isNestedEm()) {
				// トランザクションが開始されている場合はトランザクション終了
				isMonitorNestedEm = true;
				try {
					jtm.commit(true);
				} catch (Throwable e) {
					// ここは通らないはず
					m_log.error("execJobEndNode() jtm.commit ", e);
				} finally {
					if (jtm != null) {
						jtm.close();
					}
				}
			}
			// メッセージ送信
			rtn = new JobRunManagementBean().endNode(resultInfo);
			if (isMonitorNestedEm) {
				// トランザクション開始
				jtm = new JpaTransactionManager();
				jtm.begin();
			}
		} catch (HinemosUnknown | JobInfoNotFound | InvalidRole e) {
			m_log.error("endNode() is error : "
				+ ", SessionID=" + runInstructionInfo.getSessionId() 
				+ ", JobunitID=" + runInstructionInfo.getJobunitId()
				+ ", JobID=" + runInstructionInfo.getJobId()
				+ ", FacilityID=" + runInstructionInfo.getFacilityId());
		}
		return rtn;
	}

	/**
	 * 監視ジョブ情報をスケジューラに登録する
	 * @param runInstructionInfo 指示情報
	 * @throws HinemosUnknown
	 */
	public static void updateSchedule(RunInstructionInfo runInstructionInfo) throws HinemosUnknown {

		// nullチェック
		if (runInstructionInfo == null) {
			HinemosUnknown e = new HinemosUnknown("runInstructionInfo is null.");
			m_log.warn("updateSchedule() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw e;
		}

		//JobDetailに呼び出すメソッドの引数を設定
		// 監視対象IDを設定
		Serializable[] jdArgs = new Serializable[1];
		@SuppressWarnings("unchecked")
		Class<? extends Serializable>[] jdArgsType = new Class[1];
		// 引数：指示情報
		jdArgsType[0] = RunInstructionInfo.class;
		jdArgs[0] = runInstructionInfo;
		
		// 起動開始時間
		long startTime = HinemosTime.currentTimeMillis()
				+ RunInterval.TYPE_MIN_01.toSec() * 1000;

		// SimpleTrigger でジョブをスケジューリング登録
		SchedulerPlugin.scheduleSimpleJob(
			SchedulerType.RAM,
			getKey(runInstructionInfo),
			QuartzConstant.GROUP_NAME_FOR_MONITORJOB,
			startTime,
			RunInterval.TYPE_MIN_01.toSec(),
			true,
			JobRunManagementBean.class.getName(),
			QuartzConstant.METHOD_NAME_FOR_MONITORJOB,
			jdArgsType,
			jdArgs);
	}

	/**
	 * 監視ジョブ情報をスケジューラから削除
	 * @param runInstructionInfo 指示情報
	 * @throws HinemosUnknown
	 */
	public static void deleteSchedule(RunInstructionInfo runInstructionInfo) throws HinemosUnknown {

		// nullチェック
		if (runInstructionInfo == null) {
			HinemosUnknown e = new HinemosUnknown("runInstructionInfo is null.");
			m_log.warn("deleteSchedule() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw e;
		}
		SchedulerPlugin.deleteJob(SchedulerType.RAM, getKey(runInstructionInfo),
				QuartzConstant.GROUP_NAME_FOR_MONITORJOB);
	}

	/**
	 * 指示情報を元にキー文字列を返す
	 * 
	 * @param runInstructionInfo 指示情報
	 * @return キー文字列
	 */
	private static String getKey(RunInstructionInfo runInstructionInfo) {
		return runInstructionInfo.getSessionId()
				+ runInstructionInfo.getJobunitId()
				+ runInstructionInfo.getJobId()
				+ runInstructionInfo.getFacilityId();
	}

	/**
	 * 優先度に応じたジョブ戻り値を返す。
	 * 
	 * @param runInstructionInfo 指示情報
	 * @param priority 優先度
	 * @return ジョブ戻り値
	 */
	public static Integer getReturnValue(RunInstructionInfo runInstructionInfo, Integer priority) {
		try {
			// ジョブセッション情報取得
			JobInfoEntity jobInfoEntity = QueryUtil.getJobInfoEntityPK(
					runInstructionInfo.getSessionId(), 
					runInstructionInfo.getJobunitId(), 
					runInstructionInfo.getJobId());

			if (priority == null) {
				return jobInfoEntity.getMonitorUnknownEndValue();
			} else if (priority == PriorityConstant.TYPE_INFO) {
				return jobInfoEntity.getMonitorInfoEndValue();
			} else if(priority == PriorityConstant.TYPE_WARNING) {
				return jobInfoEntity.getMonitorWarnEndValue();
			} else if(priority == PriorityConstant.TYPE_CRITICAL) {
				return jobInfoEntity.getMonitorCriticalEndValue();
			} else {
				return jobInfoEntity.getMonitorUnknownEndValue();
			}
		} catch (JobInfoNotFound | InvalidRole e) {
			return MonitorJobConstant.INITIAL_END_VALUE_UNKNOWN;
		}
	}

	/**
	 * 監視ジョブマップ取得
	 * @param monitorTypeId 監視種別ID
	 * @return 監視ジョブマップ
	 */
	public static Map<RunInstructionInfo, MonitorInfo> getMonitorJobMap(String monitorTypeId) {
		return monitorJobMap.get(monitorTypeId);
	}

	/**
	 * 監視ジョブマップ登録
	 * @param runInstructionInfo 入力情報
	 * @param monitorInfo 監視情報
	 */
	private static void addMonitorJobMap(
			RunInstructionInfo runInstructionInfo,
			MonitorInfo monitorInfo) {
		monitorJobMap.get(monitorInfo.getMonitorTypeId()).put(runInstructionInfo, monitorInfo);

		// 接続中のHinemosAgentに対する更新通知
		if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_LOGFILE)) {
			// ログファイル監視
			SettingUpdateInfo.getInstance().setLogFileMonitorUpdateTime(HinemosTime.currentTimeMillis());
			LogfileManagerUtil.broadcastConfigured();
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_WINEVENT)) {
			// Windowsイベント監視
			SettingUpdateInfo.getInstance().setWinEventMonitorUpdateTime(HinemosTime.currentTimeMillis());
			WinEventManagerUtil.broadcastConfigured();
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOM_N)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOM_S)) {
			// 接続中のHinemosAgentに対する更新通知
			SettingUpdateInfo.getInstance().setCustomMonitorUpdateTime(HinemosTime.currentTimeMillis());
			CustomManagerUtil.broadcastConfigured();
		}
	}

	/**
	 * 監視ジョブマップ削除
	 * @param runInstructionInfo 入力情報
	 */
	public static void removeMonitorJobMap(String monitorTypeId, RunInstructionInfo runInstructionInfo) {
		if (monitorJobMap.get(monitorTypeId) == null) {
			return;
		}

		for (Map.Entry<RunInstructionInfo, MonitorInfo> entry : monitorJobMap.get(monitorTypeId).entrySet()) {
			if (getKey(entry.getKey()).equals(getKey(runInstructionInfo))) {
				monitorJobMap.get(monitorTypeId).remove(entry.getKey());
				break;
			}
		}

		// 接続中のHinemosAgentに対する更新通知
		if (monitorTypeId.equals(HinemosModuleConstant.MONITOR_LOGFILE)) {
			// ログファイル監視
			SettingUpdateInfo.getInstance().setLogFileMonitorUpdateTime(HinemosTime.currentTimeMillis());
			LogfileManagerUtil.broadcastConfigured();
		} else if (monitorTypeId.equals(HinemosModuleConstant.MONITOR_WINEVENT)) {
			// Windowsイベント監視
			SettingUpdateInfo.getInstance().setWinEventMonitorUpdateTime(HinemosTime.currentTimeMillis());
			WinEventManagerUtil.broadcastConfigured();
		} else if (monitorTypeId.equals(HinemosModuleConstant.MONITOR_CUSTOM_N)
				|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_CUSTOM_S)) {
			// 接続中のHinemosAgentに対する更新通知
			SettingUpdateInfo.getInstance().setCustomMonitorUpdateTime(HinemosTime.currentTimeMillis());
			CustomManagerUtil.broadcastConfigured();
		}
	}

	/**
	 * 前回値取得
	 * @param runInstructionInfo 指示情報
	 * @return 前回値情報
	 */
	public static Object getPrevMonitorValue(RunInstructionInfo runInstructionInfo) {
		return prevMonitorValueMap.get(runInstructionInfo);
	}

	/**
	 * 前回値登録
	 * @param runInstructionInfo 指示情報
	 * @param prevMonitorVale 前回値
	 */
	public static void addPrevMonitorValue(
			RunInstructionInfo runInstructionInfo,
			Object prevMonitorValue) {
		prevMonitorValueMap.put(runInstructionInfo, prevMonitorValue);
	}

	/**
	 * 前回値削除
	 * @param runInstructionInfo 指示情報
	 */
	public static void removePrevMonitorValue(RunInstructionInfo runInstructionInfo) {
		for (Map.Entry<RunInstructionInfo, Object> entry : prevMonitorValueMap.entrySet()) {
			if (getKey(entry.getKey()).equals(getKey(runInstructionInfo))) {
				prevMonitorValueMap.remove(entry.getKey());
				break;
			}
		}
		prevMonitorValueMap.remove(runInstructionInfo);
	}

	/**
	 * 監視ジョブマップ削除
	 * @param sessionId セッションID
	 */
	public static void removeInfoBySessionId(String sessionId) {
		if (sessionId == null || sessionId.isEmpty()) {
			return;
		}

		/** スケジュール削除 */
		List<RunInstructionInfo> runInstructionInfoList 
			= RunHistoryUtil.findRunHistoryBySessionId(sessionId);
		for (RunInstructionInfo runInstructionInfo : runInstructionInfoList) {
			try {
				deleteSchedule(runInstructionInfo);
			} catch (HinemosUnknown e) {
				// エラーとしない
				m_log.debug("schedule is not found.");
			}
		}

		/** 監視ジョブマップ削除 */
		HashSet<String> keySet = new HashSet<>();
		for (Map.Entry<String, ConcurrentHashMap<RunInstructionInfo, MonitorInfo>> entry 
				: monitorJobMap.entrySet()) {
			for (Map.Entry<RunInstructionInfo, MonitorInfo> childEntry : entry.getValue().entrySet()) {
				if (sessionId.equals(childEntry.getKey().getSessionId())) {
					monitorJobMap.get(childEntry.getValue().getMonitorTypeId()).remove(childEntry.getKey());
					keySet.add(childEntry.getValue().getMonitorTypeId());
				}
			}
		}

		// 接続中のHinemosAgentに対する更新通知
		if (keySet.contains(HinemosModuleConstant.MONITOR_LOGFILE)) {
			// ログファイル監視
			SettingUpdateInfo.getInstance().setLogFileMonitorUpdateTime(HinemosTime.currentTimeMillis());
			LogfileManagerUtil.broadcastConfigured();
		} else if (keySet.contains(HinemosModuleConstant.MONITOR_WINEVENT)) {
			// Windowsイベント監視
			SettingUpdateInfo.getInstance().setWinEventMonitorUpdateTime(HinemosTime.currentTimeMillis());
			WinEventManagerUtil.broadcastConfigured();
		} else if (keySet.contains(HinemosModuleConstant.MONITOR_CUSTOM_N)
				|| keySet.contains(HinemosModuleConstant.MONITOR_CUSTOM_S)) {
			// 接続中のHinemosAgentに対する更新通知
			SettingUpdateInfo.getInstance().setCustomMonitorUpdateTime(HinemosTime.currentTimeMillis());
			CustomManagerUtil.broadcastConfigured();
		}

		/** 前回値削除 */
		for (Map.Entry<RunInstructionInfo, Object> entry : prevMonitorValueMap.entrySet()) {
			if (sessionId.equals(entry.getKey().getSessionId())) {
				prevMonitorValueMap.remove(entry.getKey());
			}
		}

		/** RunHistory削除 */
		for (RunInstructionInfo runInstructionInfo : runInstructionInfoList) {
			RunHistoryUtil.delRunHistory(runInstructionInfo);
		}
	}

	/**
	 * 監視ジョブの監視部分を実行するスレッドクラス
	 */
	private static class JobMonitorTask extends Thread {

		// Logger
		static private Log m_log = LogFactory.getLog(JobMonitorTask.class);

		// 入力情報
		private RunInstructionInfo m_runInstructionInfo = null;

		/**
		 * コンストラクタ
		 * 
		 * @param runInstructionInfo 実行指示
		 */
		public JobMonitorTask(RunInstructionInfo runInstructionInfo) {
			// 入力情報
			this.m_runInstructionInfo = runInstructionInfo;
		}

		/**
		 * 監視ジョブを実行するクラス<BR>
		 */
		@Override
		public void run() {

			m_log.info("run() SessionID=" + this.m_runInstructionInfo.getSessionId()
			+ ", JobunitID=" + this.m_runInstructionInfo.getJobunitId()
			+ ", JobID=" + this.m_runInstructionInfo.getJobId()
			+ ", FacilityID=" + this.m_runInstructionInfo.getFacilityId()
			+ ", CommandType=" + this.m_runInstructionInfo.getCommandType());

			JpaTransactionManager jtm = null;
			MonitorInfo monitorInfo = null;
			JobSessionJobEntity sessionJob = null;
			JobInfoEntity jobInfoEntity = null;
			try {
				jtm = new JpaTransactionManager();
				jtm.begin();

				// ジョブセッション情報の取得
				//セッションIDとジョブIDから、セッションジョブを取得
				sessionJob = QueryUtil.getJobSessionJobPK(
						m_runInstructionInfo.getSessionId(), 
						m_runInstructionInfo.getJobunitId(), 
						m_runInstructionInfo.getJobId());
				jobInfoEntity = sessionJob.getJobInfoEntity();
				// 監視情報の取得
				monitorInfo = com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK_OR(
					jobInfoEntity.getMonitorId(), sessionJob.getOwnerRoleId());

				if (RunHistoryUtil.findRunHistory(m_runInstructionInfo) == null) {
					if(m_runInstructionInfo.getCommand().equals(CommandConstant.MONITOR)){
						// メッセージ送信
						if (!execJobEndNode(m_runInstructionInfo, RunStatusConstant.START, 
								"", "", MonitorJobConstant.INITIAL_END_VALUE_INFO)) {
							// ジョブがすでに起動している場合
							m_log.warn("This job already run by other agent. "
									+ "SessionID=" + m_runInstructionInfo.getSessionId()
									+ ", JobunitID=" + m_runInstructionInfo.getJobunitId()
									+ ", JobID=" + m_runInstructionInfo.getJobId()
									+ ", FacilityID=" + m_runInstructionInfo.getFacilityId());
							return;
						}
						// Hinemosエージェントで行う監視は、Hinemosエージェントにアクセスできない場合エラー
						if ((monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_LOGFILE)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_WINEVENT)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOM_N)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOM_S))
								&& !AgentConnectUtil.isValidAgent(m_runInstructionInfo.getFacilityId())) {
							// メッセージ作成
							execJobEndNode(m_runInstructionInfo, RunStatusConstant.ERROR, "",
									MessageConstant.MESSAGE_AGENT_IS_NOT_AVAILABLE.getMessage(),
									getReturnValue(m_runInstructionInfo, PriorityConstant.TYPE_UNKNOWN));
							return;
						}

						// 実行履歴に追加
						RunHistoryUtil.addRunHistory(m_runInstructionInfo);
						// 監視実行
						if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SYSTEMLOG)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SNMPTRAP)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_LOGFILE)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_WINEVENT)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOM_N)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOM_S)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S)){
							// 監視対象の情報を設定する。
							addMonitorJobMap(m_runInstructionInfo, monitorInfo);
						} else {
							// 上記以外の監視
							new JobRunManagementBean().runMonitorJob(m_runInstructionInfo);
						}

						// トラップ系監視の場合は強制的にチェック実行
						if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SYSTEMLOG)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SNMPTRAP)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_LOGFILE)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_WINEVENT)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S)){
							JobSessionJobImpl.addForceCheck(m_runInstructionInfo.getSessionId());
						}
					} else {
						//ここは通らない
						m_log.warn("runJob() : command is not specified correctly.");
					}
				}else {
					// 処理を終了する。
					if (m_runInstructionInfo.getCommandType() == CommandTypeConstant.STOP
						&& m_runInstructionInfo.getStopType() == CommandStopTypeConstant.DESTROY_PROCESS) {

						// 実行履歴が存在しない場合にはエラーを返す
						if (RunHistoryUtil.findRunHistory(m_runInstructionInfo) == null) {
							// メッセージ送信
							endMonitorJob(m_runInstructionInfo, null, "Internal Error : Ex. Job already terminated.", 
									"", RunStatusConstant.ERROR, getReturnValue(m_runInstructionInfo, PriorityConstant.TYPE_UNKNOWN));
						} else {
							// メッセージ送信
							if (!execJobEndNode(m_runInstructionInfo, RunStatusConstant.START, 
								"", "", MonitorJobConstant.INITIAL_END_VALUE_INFO)) {
								// ジョブがすでに起動している場合
								m_log.warn("This job already run by other. "
									+ ", SessionID=" + m_runInstructionInfo.getSessionId() 
									+ ", JobunitID=" + m_runInstructionInfo.getJobunitId()
									+ ", JobID=" + m_runInstructionInfo.getJobId()
									+ ", FacilityID=" + m_runInstructionInfo.getFacilityId());
								return;
							}

							// 終了処理
							// キャンセル処理
							endMonitorJob(m_runInstructionInfo, monitorInfo.getMonitorTypeId(), "",
									"", RunStatusConstant.END, MonitorJobConstant.INITIAL_END_VALUE_INFO);
						}
					}
				}
				// 終了処理
				jtm.commit();

			} catch (JobInfoNotFound | InvalidRole | MonitorNotFound | FacilityNotFound | HinemosUnknown e) {
				// 監視実行時に失敗
				// メッセージ作成
				String monitorTypeId = null;
				if (monitorInfo != null) {
					monitorTypeId = monitorInfo.getMonitorTypeId();
				}
				endMonitorJob(m_runInstructionInfo, monitorTypeId, "", e.getMessage(), 
							RunStatusConstant.ERROR, getReturnValue(m_runInstructionInfo, PriorityConstant.TYPE_UNKNOWN));
				if (jtm != null)
					jtm.rollback();
			} finally {
				if (jtm != null)
					jtm.close();
			}
		}
	}

}
