/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
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
import com.clustercontrol.binary.util.BinaryManagerUtil;
import com.clustercontrol.commons.bean.SettingUpdateInfo;
import com.clustercontrol.commons.util.EmptyJpaTransactionCallback;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.commons.util.MonitoredThreadPoolExecutor;
import com.clustercontrol.custom.bean.CustomConstant;
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
import com.clustercontrol.monitor.bean.ConvertValueConstant;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.plugin.impl.SchedulerPlugin;
import com.clustercontrol.plugin.impl.SchedulerPlugin.SchedulerType;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.winevent.util.WinEventManagerUtil;
import com.clustercontrol.xcloud.factory.monitors.CloudLogManagerUtil;

/**
 * HinemosManager上でジョブを実行するクラス<BR>
 */
public class MonitorJobWorker {

	// Logger
	private static Log m_log = LogFactory.getLog(MonitorJobWorker.class);
	
	private static ExecutorService service;
	private static String workerName = "MonitorJobWorker";

	// 監視ジョブ情報を保持（監視種別ID、監視情報(キー情報, MonitorJobInfo)）
	private static ConcurrentHashMap<String, ConcurrentHashMap<String, MonitorJobInfo>> monitorJobMap
		= new ConcurrentHashMap<>();

	// カスタム監視（数値）前回値を保持（指示情報、値）
	private static ConcurrentHashMap<RunInstructionInfo, Object> prevMonitorValueMap
		= new ConcurrentHashMap<>();

	static {
		int maxThreadPoolSize = HinemosPropertyCommon.job_monitor_thread_pool_size.getIntegerValue();

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
				new ConcurrentHashMap<String, MonitorJobInfo>());
		monitorJobMap.put(HinemosModuleConstant.MONITOR_SNMPTRAP, 
				new ConcurrentHashMap<String, MonitorJobInfo>());
		monitorJobMap.put(HinemosModuleConstant.MONITOR_LOGFILE, 
				new ConcurrentHashMap<String, MonitorJobInfo>());
		monitorJobMap.put(HinemosModuleConstant.MONITOR_RPA_LOGFILE, 
				new ConcurrentHashMap<String, MonitorJobInfo>());
		monitorJobMap.put(HinemosModuleConstant.MONITOR_BINARYFILE_BIN,
				new ConcurrentHashMap<String, MonitorJobInfo>());
		monitorJobMap.put(HinemosModuleConstant.MONITOR_PCAP_BIN,
				new ConcurrentHashMap<String, MonitorJobInfo>());
		monitorJobMap.put(HinemosModuleConstant.MONITOR_WINEVENT, 
				new ConcurrentHashMap<String, MonitorJobInfo>());
		monitorJobMap.put(HinemosModuleConstant.MONITOR_CUSTOM_N, 
				new ConcurrentHashMap<String, MonitorJobInfo>());
		monitorJobMap.put(HinemosModuleConstant.MONITOR_CUSTOM_S, 
				new ConcurrentHashMap<String, MonitorJobInfo>());
		monitorJobMap.put(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N, 
				new ConcurrentHashMap<String, MonitorJobInfo>());
		monitorJobMap.put(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S, 
				new ConcurrentHashMap<String, MonitorJobInfo>());
		monitorJobMap.put(HinemosModuleConstant.MONITOR_CLOUD_LOG, 
				new ConcurrentHashMap<String, MonitorJobInfo>());
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

		m_log.debug("endMonitorJob() : sessionId=" + runInstructionInfo.getSessionId() + 
					", JobunitId=" + runInstructionInfo.getJobunitId() + 
					", JobId=" + runInstructionInfo.getJobId());

		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();

			if (monitorTypeId != null) {
				if (monitorTypeId.equals(HinemosModuleConstant.MONITOR_LOGFILE)
						|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_BINARYFILE_BIN)
						|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_PCAP_BIN)
						|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_SNMPTRAP)
						|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_SYSTEMLOG)
						|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_WINEVENT)
						|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_CUSTOM_N)
						|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_CUSTOM_S)
						|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N)
						|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S)
						|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_CLOUD_LOG)) {
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
				}
			}
			// 履歴削除
			RunHistoryUtil.delRunHistory(runInstructionInfo);

			// メッセージ送信
			execJobEndNode(runInstructionInfo, status,
					message, errorMessage, endValue);

			// スケジューラ削除(execJobEndNode()でjtm.begin()時にコールバックメソッドが削除されるためここで行う)
			if (monitorTypeId != null
				&& (monitorTypeId.equals(HinemosModuleConstant.MONITOR_JMX)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_SNMP_N)
					|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_PERFORMANCE))) {
				try {
					deleteSchedule(runInstructionInfo);
				} catch (HinemosUnknown e) {
					// エラーとしない
					m_log.debug("schedule is not found.");
				}
			}
			jtm.commit();
		} catch (Exception e) {
			if (jtm != null){
				jtm.rollback();
			}
			throw e;
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
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
					m_log.debug("execJobEndNode() jtm.commit");
					// 外側のトランザクションをcommitする
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
			SchedulerType.RAM_JOB,
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
		
		// まさにスケジューラで動いている自分自身を削除するのでスレッドがinterrupt状態になってしまう
		// Thread.interrupted()で消費してもよいがそもそも削除を最後におこなうようにpostCloseで削除する
		// このコールバックが実行されると、同一スレッドでThread.sleep()等実行時にInterruptExceptionが発生するので注意
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			jtm.addCallback(new EmptyJpaTransactionCallback() {
				@Override
				public void postClose() {
					try {
						SchedulerPlugin.deleteJob(SchedulerType.RAM_JOB, getKey(runInstructionInfo),
								QuartzConstant.GROUP_NAME_FOR_MONITORJOB);
					} catch (HinemosUnknown e) {
						m_log.error(e.getMessage(), e);
					}
				}
			});
		}
		
	}

	/**
	 * 指示情報を元にキー文字列を返す
	 * 
	 * @param runInstructionInfo 指示情報
	 * @return キー文字列
	 */
	public static String getKey(RunInstructionInfo runInstructionInfo) {
		return runInstructionInfo.getSessionId() + "#"
				+ runInstructionInfo.getJobunitId() + "#"
				+ runInstructionInfo.getJobId() + "#"
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
		ConcurrentHashMap<RunInstructionInfo, MonitorInfo> map = new ConcurrentHashMap<>();
		for (Map.Entry<String, MonitorJobInfo> entry : monitorJobMap.get(monitorTypeId).entrySet()) {
			map.put(entry.getValue().runInstructionInfo, entry.getValue().monitorInfo);
		}
		return map;
	}

	/**
	 * 監視ジョブマップ登録
	 * @param runInstructionInfo 入力情報
	 * @param monitorInfo 監視情報
	 */
	private static void addMonitorJobMap(
			RunInstructionInfo runInstructionInfo,
			MonitorInfo monitorInfo) {

		if (m_log.isTraceEnabled()) {
			m_log.trace(String.format("addMonitorJobMap() : monitorTypeId=%s, key=%s", 
					monitorInfo.getMonitorTypeId(), getKey(runInstructionInfo)));
		}
		monitorJobMap.get(monitorInfo.getMonitorTypeId())
			.put(getKey(runInstructionInfo), new MonitorJobInfo(runInstructionInfo, monitorInfo));

		// 接続中のHinemosAgentに対する更新通知
		if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_LOGFILE)) {
			// ログファイル監視
			SettingUpdateInfo.getInstance().setLogFileMonitorUpdateTime(HinemosTime.currentTimeMillis());
			LogfileManagerUtil.specificcastConfigured(runInstructionInfo.getFacilityId());
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_WINEVENT)) {
			// Windowsイベント監視
			SettingUpdateInfo.getInstance().setWinEventMonitorUpdateTime(HinemosTime.currentTimeMillis());
			WinEventManagerUtil.specificcastConfigured(runInstructionInfo.getFacilityId());
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOM_N)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOM_S)) {
			// 接続中のHinemosAgentに対する更新通知
			SettingUpdateInfo.getInstance().setCustomMonitorUpdateTime(HinemosTime.currentTimeMillis());
			CustomManagerUtil.specificcastConfigured(getCustomFacilityId(monitorInfo, runInstructionInfo));
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_BINARYFILE_BIN)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_PCAP_BIN)) {
			// バイナリ監視.
			SettingUpdateInfo.getInstance().setBinaryMonitorUpdateTime(HinemosTime.currentTimeMillis());
			BinaryManagerUtil.specificcastConfigured(runInstructionInfo.getFacilityId());
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CLOUD_LOG)) {
			//　クラウドログ監視
			SettingUpdateInfo.getInstance().setCloudLogMonitorUpdateTime(HinemosTime.currentTimeMillis());
			CloudLogManagerUtil.specificcastConfigured(runInstructionInfo.getFacilityId());
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

		String customFacilityId = "";
		String monitorJobKey = getKey(runInstructionInfo);
		if (m_log.isTraceEnabled()) {
			m_log.trace(String.format("removeMonitorJobMap() : monitorTypeId=%s, key=%s", monitorTypeId, monitorJobKey));
		}
		if (monitorJobMap.get(monitorTypeId).containsKey(monitorJobKey)) {
			if (m_log.isTraceEnabled()) {
				m_log.trace(String.format("removeMonitorJobMap() : before map(%s) count=%d", 
						monitorTypeId, monitorJobMap.get(monitorTypeId).size()));
			}
			if (monitorTypeId.equals(HinemosModuleConstant.MONITOR_CUSTOM_N)
				|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_CUSTOM_S)) {
				// 接続中のHinemosAgentに対する更新通知
				MonitorJobInfo tmp = monitorJobMap.get(monitorTypeId).get(monitorJobKey);
				if (tmp != null) {
					customFacilityId = getCustomFacilityId(tmp.monitorInfo, runInstructionInfo);
				}
			}
			monitorJobMap.get(monitorTypeId).remove(monitorJobKey);
			if (m_log.isTraceEnabled()) {
				m_log.trace(String.format("removeMonitorJobMap() : after map(%s) count=%d", 
						monitorTypeId, monitorJobMap.get(monitorTypeId).size()));
			}
		}

		// 接続中のHinemosAgentに対する更新通知
		if (monitorTypeId.equals(HinemosModuleConstant.MONITOR_LOGFILE)) {
			// ログファイル監視
			SettingUpdateInfo.getInstance().setLogFileMonitorUpdateTime(HinemosTime.currentTimeMillis());
			LogfileManagerUtil.specificcastConfigured(runInstructionInfo.getFacilityId());
		} else if (monitorTypeId.equals(HinemosModuleConstant.MONITOR_WINEVENT)) {
			// Windowsイベント監視
			SettingUpdateInfo.getInstance().setWinEventMonitorUpdateTime(HinemosTime.currentTimeMillis());
			WinEventManagerUtil.specificcastConfigured(runInstructionInfo.getFacilityId());
		} else if (monitorTypeId.equals(HinemosModuleConstant.MONITOR_CUSTOM_N)
				|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_CUSTOM_S)) {
			// 接続中のHinemosAgentに対する更新通知
			SettingUpdateInfo.getInstance().setCustomMonitorUpdateTime(HinemosTime.currentTimeMillis());
			CustomManagerUtil.specificcastConfigured(customFacilityId);
		} else if (monitorTypeId.equals(HinemosModuleConstant.MONITOR_BINARYFILE_BIN)
				|| monitorTypeId.equals(HinemosModuleConstant.MONITOR_PCAP_BIN)) {
			// バイナリ監視.
			SettingUpdateInfo.getInstance().setBinaryMonitorUpdateTime(HinemosTime.currentTimeMillis());
			BinaryManagerUtil.specificcastConfigured(runInstructionInfo.getFacilityId());
		} else if (monitorTypeId.equals(HinemosModuleConstant.MONITOR_CLOUD_LOG)) {
			// クラウドログ監視
			SettingUpdateInfo.getInstance().setCloudLogMonitorUpdateTime(HinemosTime.currentTimeMillis());
			CloudLogManagerUtil.specificcastConfigured(runInstructionInfo.getFacilityId());
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
		HashSet<String> logfileFacilityIds = new HashSet<>();
		HashSet<String> winEventFacilityIds = new HashSet<>();
		HashSet<String> customFacilityIds = new HashSet<>();
		HashSet<String> binaryFacilityIds = new HashSet<>();
		HashSet<String> cloudlogFacilityIds = new HashSet<>();

		for (Map.Entry<String, ConcurrentHashMap<String, MonitorJobInfo>> entry 
				: monitorJobMap.entrySet()) {
			Iterator<Map.Entry<String, MonitorJobInfo>> iter = entry.getValue().entrySet().iterator();
			if (m_log.isTraceEnabled()) {
				m_log.trace(String.format("removeInfoBySessionId() : before map(%s) count=%d", 
						entry.getKey(), entry.getValue().size()));
			}
			while(iter.hasNext()) {
				Map.Entry<String, MonitorJobInfo> childEntry = iter.next();
				RunInstructionInfo runInstructionInfo = childEntry.getValue().runInstructionInfo;
				MonitorInfo monitorInfo = (MonitorInfo)childEntry.getValue().monitorInfo;
				if (sessionId.equals(runInstructionInfo.getSessionId())) {
					if (entry.getKey().equals(HinemosModuleConstant.MONITOR_LOGFILE)) {
						// ログファイル監視
						logfileFacilityIds.add(runInstructionInfo.getFacilityId());
					} else if (entry.getKey().equals(HinemosModuleConstant.MONITOR_WINEVENT)) {
						// Windowsイベント監視
						winEventFacilityIds.add(runInstructionInfo.getFacilityId());
					} else if (entry.getKey().equals(HinemosModuleConstant.MONITOR_CUSTOM_N)
							|| entry.getKey().equals(HinemosModuleConstant.MONITOR_CUSTOM_S)) {
						// カスタム監視
						customFacilityIds.add(getCustomFacilityId(monitorInfo, runInstructionInfo));
					} else if (entry.getKey().equals(HinemosModuleConstant.MONITOR_BINARYFILE_BIN)
							|| entry.getKey().equals(HinemosModuleConstant.MONITOR_PCAP_BIN)) {
						// バイナリ監視.
						binaryFacilityIds.add(runInstructionInfo.getFacilityId());
					}else if (entry.getKey().equals(HinemosModuleConstant.MONITOR_CLOUD_LOG)) {
						// クラウドログ監視
						cloudlogFacilityIds.add(runInstructionInfo.getFacilityId());
					}
					if (m_log.isTraceEnabled()) {
						m_log.trace(String.format("removeInfoBySessionId() : monitorTypeId=%s, key=%s",
								entry.getKey(), childEntry.getKey()));
					}
					iter.remove();
				}
			}
			if (m_log.isTraceEnabled()) {
				m_log.trace(String.format("removeInfoBySessionId() : after map(%s) count=%d", 
						entry.getKey(), entry.getValue().size()));
			}
		}

		// 接続中のHinemosAgentに対する更新通知
		if (logfileFacilityIds.size() > 0) {
			// ログファイル監視
			SettingUpdateInfo.getInstance().setLogFileMonitorUpdateTime(HinemosTime.currentTimeMillis());
			LogfileManagerUtil.specificcastConfigured(logfileFacilityIds);
		}
		if (winEventFacilityIds.size() > 0) {
			// Windowsイベント監視
			SettingUpdateInfo.getInstance().setWinEventMonitorUpdateTime(HinemosTime.currentTimeMillis());
			WinEventManagerUtil.specificcastConfigured(winEventFacilityIds);
		}
		if (customFacilityIds.size() > 0) {
			// カスタム監視
			SettingUpdateInfo.getInstance().setCustomMonitorUpdateTime(HinemosTime.currentTimeMillis());
			CustomManagerUtil.specificcastConfigured(customFacilityIds);
		}
		if (binaryFacilityIds.size() > 0) {
			// バイナリ監視.
			SettingUpdateInfo.getInstance().setBinaryMonitorUpdateTime(HinemosTime.currentTimeMillis());
			BinaryManagerUtil.specificcastConfigured(binaryFacilityIds);
		}
		if (cloudlogFacilityIds.size() > 0) {
			// クラウドログ監視
			SettingUpdateInfo.getInstance().setCloudLogMonitorUpdateTime(HinemosTime.currentTimeMillis());
			CloudLogManagerUtil.specificcastConfigured(cloudlogFacilityIds);
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
	 * コマンド監視の監視ジョブ対象ノード取得
	 * 
	 * @param monitorInfo 監視設定
	 * @param runInstructionInfo 実行指示情報
	 * @return 監視ジョブ対象ノードのファシリティID
	 */
	private static String getCustomFacilityId(MonitorInfo monitorInfo, RunInstructionInfo runInstructionInfo) {
		if ((monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOM_N)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOM_S))
				&& monitorInfo.getCustomCheckInfo().getCommandExecType() == CustomConstant.CommandExecType.SELECTED
				&& monitorInfo.getCustomCheckInfo().getSelectedFacilityId() != null) {
			return monitorInfo.getCustomCheckInfo().getSelectedFacilityId();
		} else {
			return runInstructionInfo.getFacilityId();
		} 
	}

	/**
	 * 監視ジョブ情報を格納する
	 */
	private static class MonitorJobInfo {
		// 実行指示情報
		RunInstructionInfo runInstructionInfo;
		// 監視設定情報
		MonitorInfo monitorInfo;
		MonitorJobInfo(RunInstructionInfo runInstructionInfo, MonitorInfo monitorInfo) {
			this.runInstructionInfo = runInstructionInfo;
			this.monitorInfo = monitorInfo;
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

				m_log.debug("JobMonitorTask: RunHistoryUtil.findRunHistory(m_runInstructionInfo) == " 
							+ RunHistoryUtil.findRunHistory(m_runInstructionInfo));
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
						boolean isValidAgent=true;
						//  カスタム監視の場合
						if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOM_N)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOM_S)) {
							//監視側で別途実行ファシリティを指定できる（まとめ実行）ので考慮
							if (monitorInfo.getCustomCheckInfo().getCommandExecType() == CustomConstant.CommandExecType.SELECTED
									&& monitorInfo.getCustomCheckInfo().getSelectedFacilityId() != null) {
								isValidAgent = AgentConnectUtil.isValidAgent(monitorInfo.getCustomCheckInfo().getSelectedFacilityId());
							} else {
								isValidAgent = AgentConnectUtil.isValidAgent(m_runInstructionInfo.getFacilityId());
							}
						}
						
						
						if (!(isValidAgent)) {
							// エージェント利用不可ならメッセージを作って処理打ち切り
							execJobEndNode(m_runInstructionInfo, RunStatusConstant.ERROR, "",
									MessageConstant.MESSAGE_AGENT_IS_NOT_AVAILABLE.getMessage(),
									getReturnValue(m_runInstructionInfo, PriorityConstant.TYPE_UNKNOWN));
							return;
						}
						
						//TODO #8205のマイナー対応(監視ジョブでカスタム監視を実施する際の根本対処時に見直すこと)
						// カスタム監視(数値・差分あり)の場合でタイムアウト設定が1分を超えているとジョブが実行中のままになるので、この時点でエラー終了させ処理を打ち切り
						if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOM_N)) {
							if (monitorInfo.getCustomCheckInfo().getConvertFlg() == ConvertValueConstant.TYPE_DELTA) {
								Integer timeout = monitorInfo.getCustomCheckInfo().getTimeout();
								if (60000 < timeout) {
									m_log.warn("monitor job canceled. "
											+ "SessionID=" + m_runInstructionInfo.getSessionId()
											+ ", JobunitID=" + m_runInstructionInfo.getJobunitId()
											+ ", JobID=" + m_runInstructionInfo.getJobId()
											+ ", FacilityID=" + m_runInstructionInfo.getFacilityId()
											+ ", TargetMonitorID=" +monitorInfo.getMonitorId());
									execJobEndNode(m_runInstructionInfo, RunStatusConstant.ERROR, "",
											MessageConstant.MESSAGE_JOB_MONITOR_SNMP_N_ABORT_RUN.getMessage(monitorInfo.getMonitorId() ,String.valueOf(timeout)),
											getReturnValue(m_runInstructionInfo, PriorityConstant.TYPE_UNKNOWN));
									return;
								}
							}
						}

						// 実行履歴に追加
						RunHistoryUtil.addRunHistory(m_runInstructionInfo);
						// 監視実行
						if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SYSTEMLOG)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SNMPTRAP)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_LOGFILE)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_BINARYFILE_BIN)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_PCAP_BIN)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_WINEVENT)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOM_N)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOM_S)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CLOUD_LOG)){
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
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_BINARYFILE_BIN)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_PCAP_BIN)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_WINEVENT)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S)
								|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CLOUD_LOG)){
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
