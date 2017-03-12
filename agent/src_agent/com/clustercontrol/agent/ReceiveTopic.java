/*

Copyright (C) 2011 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.agent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.bean.TopicFlagConstant;
import com.clustercontrol.agent.custom.CommandCollector;
import com.clustercontrol.agent.filecheck.FileCheckManager;
import com.clustercontrol.agent.job.CheckSumThread;
import com.clustercontrol.agent.job.CommandThread;
import com.clustercontrol.agent.job.DeleteProcessThread;
import com.clustercontrol.agent.job.FileListThread;
import com.clustercontrol.agent.job.PublicKeyThread;
import com.clustercontrol.agent.job.RunHistoryUtil;
import com.clustercontrol.agent.log.LogfileMonitorManager;
import com.clustercontrol.agent.update.UpdateModuleUtil;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.agent.util.CollectorId;
import com.clustercontrol.agent.util.CollectorManager;
import com.clustercontrol.agent.util.CommandMonitoringWSUtil;
import com.clustercontrol.agent.winevent.WinEventMonitor;
import com.clustercontrol.agent.winevent.WinEventMonitorManager;
import com.clustercontrol.jobmanagement.bean.CommandConstant;
import com.clustercontrol.jobmanagement.bean.CommandStopTypeConstant;
import com.clustercontrol.jobmanagement.bean.CommandTypeConstant;
import com.clustercontrol.repository.bean.AgentCommandConstant;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.ws.agent.CustomInvalid_Exception;
import com.clustercontrol.ws.agent.HashMapInfo;
import com.clustercontrol.ws.agent.HinemosTopicInfo;
import com.clustercontrol.ws.agent.HinemosUnknown_Exception;
import com.clustercontrol.ws.agent.InvalidRole_Exception;
import com.clustercontrol.ws.agent.InvalidUserPass_Exception;
import com.clustercontrol.ws.agent.JobMasterNotFound_Exception;
import com.clustercontrol.ws.agent.MonitorNotFound_Exception;
import com.clustercontrol.ws.agent.SettingUpdateInfo;
import com.clustercontrol.ws.agent.TopicInfo;
import com.clustercontrol.ws.agent.HashMapInfo.Map8;
import com.clustercontrol.ws.jobmanagement.JobFileCheck;
import com.clustercontrol.ws.jobmanagement.RunInstructionInfo;
import com.clustercontrol.ws.monitor.CommandExecuteDTO;
import com.clustercontrol.ws.monitor.MonitorInfo;

/**
 * Topicを受信するクラス<BR>
 * Topicへの接続と、メッセージの受信を行います。
 * 
 * Topicでマネージャからのジョブ実行指示を受け取ります。
 */
public class ReceiveTopic extends Thread {

	//ロガー
	private static Log m_log = LogFactory.getLog(ReceiveTopic.class);

	//protected ArrayList<String> m_facilityIdList;
	private SendQueue m_sendQueue;

	private static int m_topicInterval = 30000;
	private long m_runhistoryClearDelay = 604800000;
	
	// Topicを4.1モードで動作させる。
	private boolean newTopicMode = true; 

	// マネージャと接続できている場合、フラグはtrueになる。
	private long disconnectCounter = 1;
	private static boolean m_clearFlg = false;
	private static boolean m_reloadFlg = false;
	
	// topic受信とエージェント終了時の通信コンフリクトを防ぐためのロック
	public static final Object lockTopicReceiveTiming = new Object();
	// エージェン終了フラグ
	private static boolean isTerminated = false;
	public static void terminate() {
		isTerminated = true;
	}

	// 最後に受信した設定情報更新日時
	private static SettingUpdateInfo settingLastUpdateInfo = null;

	static {
		// 再接続処理実行間隔取得
		String interval, str;
		str = "topic.interval";
		interval = AgentProperties.getProperty(str);
		if (interval != null) {
			try {
				// プロパティファイルにはmsecで記述
				m_topicInterval = Integer.parseInt(interval);
				m_log.info(str + " = " + m_topicInterval + " msec");
			} catch (NumberFormatException e) {
				m_log.error(str,e);
			}
		}
	}
	
	/**
	 * コンストラクタ
	 * @param agent ジョブエージェント
	 * @param facilityIdList ファシリティIDのリスト
	 * @param sendQueue　メッセージ送信用クラス
	 * @param props　プロパティファイル情報
	 */
	public ReceiveTopic(SendQueue sendQueue) {
		super();
		m_sendQueue = sendQueue;

		m_log.info("create ReceiveTopic ");

		// ジョブ実行履歴の削除実行時間を取得
		String delay = AgentProperties.getProperty("job.history.period");
		if (delay != null) {
			try {
				// プロパティファイルにはmsecで記述
				m_runhistoryClearDelay = Long.parseLong(delay);
				m_log.info("job.history.period = " + m_runhistoryClearDelay + " msec");
			} catch (NumberFormatException e) {
				m_log.error("job.history.period",e);
			}
		}

		// Topicのモードを4.0モードにするか4.1モードにするか。
		String mode = AgentProperties.getProperty("topic.mode");
		if (mode != null && "4.0".equals(mode)) {
			newTopicMode = false;
		}
		m_log.info("newTopicMode=" + newTopicMode);
		
		// マネージャとの接続queueを設定
		LogfileMonitorManager.setSendQueue(m_sendQueue);
		WinEventMonitorManager.setSendQueue(m_sendQueue);
	}


	/**
	 * マネージャからのTopic(即時実行など)が発行された際に、ラッチを開放する。
	 */
	private CountDownLatch countDownLatch = null;

	public void releaseLatch() {
		if (countDownLatch == null) {
			m_log.info("latch is null");
			throw new InternalError("CountDownLatch is null");
		}
		countDownLatch.countDown();
	}

	/**
	 * トピック受信処理
	 */
	@Override
	public void run() {

		m_log.info("run start");

		while (true) {
			/*
			 * トピックの有無をマネージャにチェックし終わったら、sleepする。
			 * 
			 */
			try {
				int interval = m_topicInterval;
				countDownLatch = new CountDownLatch(1);
				if (!countDownLatch.await(interval, TimeUnit.MILLISECONDS))
					m_log.debug("waiting is over");
			} catch (InterruptedException e) {
				m_log.warn("Interrupt " + e.getMessage());
			} catch (Exception e) {
				m_log.error("run() : " + e.getMessage(), e);
			}

			try {
				List<RunInstructionInfo> runInstructionList = new ArrayList<RunInstructionInfo>();
				m_log.info("getTopic " + Agent.getAgentStr() + " start");
				HinemosTopicInfo hinemosTopicInfo = null;
				List<TopicInfo> topicInfoList = null;
				SettingUpdateInfo updateInfo = null;
				try {
					// エージェントの終了処理が行われているにもかかわらずtopicをとりにいってしまう競合を防ぐためsynchronizedする
					synchronized (lockTopicReceiveTiming) {
						if (isTerminated == true) {
							// エージェントの終了処理が行われている場合、このスレッドを即時終了させる
							return;
						}
						hinemosTopicInfo = AgentEndPointWrapper.getHinemosTopic();
					}
				} catch (Exception e) {
					/*
					 * マネージャが停止している、もしくはマネージャと通信ができないと、
					 * ここに到達する。
					 */
					if (disconnectCounter < Long.MAX_VALUE) {
						disconnectCounter++;
					}
					// 一定時間、マネージャが応答していないと、ジョブ履歴を削除する。
					if (disconnectCounter * m_topicInterval > m_runhistoryClearDelay) {
						clearJobHistory();
					}

					/*
					 * マネージャが停止している時などは、下記のログが出続ける。
					 * StackTraceを出すと、ログファイルが大きくなりすぎるので、
					 * StackTraceはつけない。
					 * どうしても見たかったらdebugにする。
					 */
					String message = "run() getTopic : " + 
							", " + disconnectCounter + "*" + m_topicInterval + ":" + m_runhistoryClearDelay + ", " +
							e.getClass().getSimpleName() + ", " + e.getMessage();
					m_log.warn(message);
					m_log.debug(message, e);
					continue; // whileまで戻る。
				}

				// マネージャから取得した情報を関連クラスに配置する
				topicInfoList = hinemosTopicInfo.getTopicInfoList();
				updateInfo = hinemosTopicInfo.getSettingUpdateInfo();
				Agent.setAwakePort(hinemosTopicInfo.getAwakePort());
				
				// Hinemos時刻を更新
				HinemosTime.setTimeOffsetMillis(updateInfo.getHinemosTimeOffset());
				// Hinemosタイムゾーンを更新
				HinemosTime.setTimeZoneOffset(updateInfo.getHinemosTimeZoneOffset());
				
				/*
				 * マネージャと接続直後の場合、ここで転送ログファイル、カスタム監視情報、Windowsイベント監視情報のリストを受け取り、
				 * リロードフラグをオフにする。
				 */
				m_log.debug("run : disconnectCounter=" + disconnectCounter);
				if (disconnectCounter != 0 || isReloadFlg()) {
					reloadLogfileMonitor(updateInfo, true);
					reloadCustomMonitor(updateInfo, true);
					reloadWinEventMonitor(updateInfo, true);
					reloadJobFileCheck(updateInfo, true);
					UpdateModuleUtil.setAgentLibMd5();
					setReloadFlg(false);
				}
				disconnectCounter = 0;
				setHistoryClear(false);

				m_log.debug("run : topicInfoList.size=" + topicInfoList.size());
				for (TopicInfo topicInfo : topicInfoList) {
					m_log.info("getTopic flag=" + topicInfo.getFlag());

					RunInstructionInfo runInstructionInfo = topicInfo.getRunInstructionInfo();
					if (runInstructionInfo != null) {
						runInstructionList.add(runInstructionInfo);
					}
					
					long topicFlag = topicInfo.getFlag();
					if (topicInfo.getAgentCommand() != 0) {
						int agentCommand = topicInfo.getAgentCommand();
						m_log.debug("agentCommand : " + agentCommand);
						if (agentCommand == AgentCommandConstant.UPDATE) {
							// 1つもファイルをダウンロードしていない場合は、再起動しない。
							if(!UpdateModuleUtil.update()) {
								agentCommand = 0;
							}
						}
						if (agentCommand != 0) {
							Agent.restart(agentCommand);
						}
					}
					if ((topicFlag & TopicFlagConstant.NEW_FACILITY) != 0) {
						UpdateModuleUtil.setAgentLibMd5();
					}
					if (newTopicMode) {
						continue;
					}
					// 以下の処理は不要。
					// topic.mode=4.0の場合のみ動作する。
					if ((topicFlag & TopicFlagConstant.REPOSITORY_CHANGED) != 0 ||
							(topicFlag & TopicFlagConstant.NEW_FACILITY) != 0 ||
							(topicFlag & TopicFlagConstant.CALENDAR_CHANGED) != 0 ||
							(topicFlag & TopicFlagConstant.LOGFILE_CHANGED) != 0) {
						reloadLogfileMonitor(updateInfo, true);
					}
					if ((topicFlag & TopicFlagConstant.REPOSITORY_CHANGED) != 0 ||
							(topicFlag & TopicFlagConstant.NEW_FACILITY) != 0 ||
							(topicFlag & TopicFlagConstant.CALENDAR_CHANGED) != 0 ||
							(topicFlag & TopicFlagConstant.CUSTOM_CHANGED) != 0) {
						reloadCustomMonitor(updateInfo, true);
					}
					if ((topicFlag & TopicFlagConstant.REPOSITORY_CHANGED) != 0 ||
							(topicFlag & TopicFlagConstant.NEW_FACILITY) != 0 ||
							(topicFlag & TopicFlagConstant.CALENDAR_CHANGED) != 0 ||
							(topicFlag & TopicFlagConstant.WINEVENT_CHANGED) != 0) {
						reloadWinEventMonitor(updateInfo, true);
					}
					if ((topicFlag & TopicFlagConstant.REPOSITORY_CHANGED) != 0 ||
							(topicFlag & TopicFlagConstant.NEW_FACILITY) != 0 ||
							(topicFlag & TopicFlagConstant.CALENDAR_CHANGED) != 0 ||
							(topicFlag & TopicFlagConstant.FILECHECK_CHANGED) != 0) {
						reloadJobFileCheck(updateInfo, true);
					}
				}

				reloadLogfileMonitor(updateInfo, false);
				reloadCustomMonitor(updateInfo, false);
				reloadWinEventMonitor(updateInfo, false);
				reloadJobFileCheck(updateInfo, false);

				settingLastUpdateInfo = updateInfo;

				m_log.debug("getTopic " + Agent.getAgentStr() + " end");
				if (runInstructionList.size() > 0) {
					m_log.info("infoList.size() = " + runInstructionList.size());
				} else {
					m_log.debug("infoList.size() = 0");
				}
				for (RunInstructionInfo info : runInstructionList){
					runJob(info);
				}
			} catch (Throwable e) {
				m_log.error("run() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}
		}
	}

	public LogfileMonitorManager m_logManager = null;

	/**
	 *  リロードする必要がある場合はtrueを返す
	 * @param updateInfo
	 * @return
	 */
	private boolean isCustomMonitorReload(SettingUpdateInfo updateInfo) {
		if (updateInfo == null) {
			return false;
		} else if (settingLastUpdateInfo == null) {
			return true;
		} else {
			if (settingLastUpdateInfo.getCustomMonitorUpdateTime() == updateInfo.getCustomMonitorUpdateTime()
					&& settingLastUpdateInfo.getCalendarUpdateTime() == updateInfo.getCalendarUpdateTime()
					&& settingLastUpdateInfo.getRepositoryUpdateTime() == updateInfo.getRepositoryUpdateTime()) {
				return false;
			} else {
				return true;
			}
		}
	}

	private boolean isWinEventMonitorReload(SettingUpdateInfo updateInfo) {
		if (updateInfo == null) {
			return false;
		} else if (settingLastUpdateInfo == null) {
			return true;
		} else {
			if (settingLastUpdateInfo.getWinEventMonitorUpdateTime() == updateInfo.getWinEventMonitorUpdateTime()
					&& settingLastUpdateInfo.getCalendarUpdateTime() == updateInfo.getCalendarUpdateTime()
					&& settingLastUpdateInfo.getRepositoryUpdateTime() == updateInfo.getRepositoryUpdateTime()) {
				return false;
			} else {
				return true;
			}
		}
	}

	private boolean isLogfileMonitorReload(SettingUpdateInfo updateInfo) {
		if (updateInfo == null) {
			return false;
		} else if (settingLastUpdateInfo == null) {
			return true;
		} else {
			if (settingLastUpdateInfo.getLogFileMonitorUpdateTime() == updateInfo.getLogFileMonitorUpdateTime()
					&& settingLastUpdateInfo.getCalendarUpdateTime() == updateInfo.getCalendarUpdateTime()
					&& settingLastUpdateInfo.getRepositoryUpdateTime() == updateInfo.getRepositoryUpdateTime()) {
				return false;
			} else {
				return true;
			}
		}
	}

	private boolean isJobFileCheckReload(SettingUpdateInfo updateInfo) {
		if (updateInfo == null) {
			return false;
		} else if (settingLastUpdateInfo == null) {
			return true;
		} else {
			if (settingLastUpdateInfo.getJobFileCheckUpdateTime() == updateInfo.getJobFileCheckUpdateTime()
					&& settingLastUpdateInfo.getCalendarUpdateTime() == updateInfo.getCalendarUpdateTime()
					&& settingLastUpdateInfo.getRepositoryUpdateTime() == updateInfo.getRepositoryUpdateTime()) {
				return false;
			} else {
				return true;
			}
		}
	}

	private void reloadCustomMonitor(SettingUpdateInfo updateInfo, boolean force) {
		if (!isCustomMonitorReload(updateInfo) && !force) {
			return;
		}
		// Local Variables
		ArrayList<CommandExecuteDTO> dtos = null;

		// MAIN
		m_log.info("reloading configuration of custom monitoring...");
		try {
			dtos = AgentEndPointWrapper.getCommandExecuteDTOs();

			// unregister unnecessary Command Collector
			for (CollectorId collectId : CollectorManager.getAllCollectorIds()) {
				if (collectId.type != CommandCollector._collectorType) {
					continue;
				}
				boolean unnecessary = true;
				for (CommandExecuteDTO dto : dtos) {
					if (collectId.id != null && collectId.id.equals(dto.getMonitorId())) {
						unnecessary = false;
					}
				}
				if (unnecessary) {
					CollectorManager.unregisterCollectorTask(collectId);
				}
			}

			// reset Command Collector
			for (CommandExecuteDTO dto : dtos) {
				m_log.info("reloaded configuration : " + CommandMonitoringWSUtil.toStringCommandExecuteDTO(dto));
				CollectorManager.registerCollectorTask(new CommandCollector(dto));
			}

		} catch (HinemosUnknown_Exception e) {
			m_log.warn("un-expected internal failure occurs...", e);
		} catch (CustomInvalid_Exception e) {
			m_log.warn("monitor configuration is not valid...", e);
		} catch (InvalidRole_Exception e) {
			m_log.warn("reloadCommandMonitoring: " + e.getMessage());
		} catch (InvalidUserPass_Exception e) {
			m_log.warn("reloadCommandMonitoring: " + e.getMessage());
		}

	}

	private void reloadLogfileMonitor (SettingUpdateInfo updateInfo, boolean force) {
		if (!isLogfileMonitorReload(updateInfo) && !force) {
			return;
		}
		m_log.info("reloading configuration of logfile monitoring...");
		try {
			// 監視ジョブ以外
			ArrayList<MonitorInfo> list = AgentEndPointWrapper.getMonitorLogfile();
			for (MonitorInfo info : list) {
				m_log.info("logfile: " +
						"directory=" + info.getLogfileCheckInfo().getDirectory() +
						", filename=" + info.getLogfileCheckInfo().getFileName() +
						", fileencoding=" + info.getLogfileCheckInfo().getFileEncoding() +
						", monitorId=" + info.getMonitorId() +
						", monitorFlg=" + info.isMonitorFlg());
			}

			// 監視ジョブ
			HashMapInfo hashMapInfo = AgentEndPointWrapper.getMonitorJobLogfile();
			Map8 map = hashMapInfo.getMap8();
			HashMap<RunInstructionInfo, MonitorInfo> rtnMap = new HashMap<RunInstructionInfo, MonitorInfo>();
			for (Map8.Entry entry : map.getEntry()) {
				m_log.info("logfile: " +
						"directory=" + entry.getValue().getLogfileCheckInfo().getDirectory() +
						", filename=" + entry.getValue().getLogfileCheckInfo().getFileName() +
						", fileencoding=" + entry.getValue().getLogfileCheckInfo().getFileEncoding() +
						", sessionId=" + entry.getKey().getSessionId() +
						", jobunitId=" + entry.getKey().getJobunitId() +
						", jobId=" + entry.getKey().getJobId() +
						", facilityId=" + entry.getKey().getFacilityId() +
						", monitorId=" + entry.getValue().getMonitorId() +
						", monitorFlg=" + entry.getValue().isMonitorFlg());
				rtnMap.put(entry.getKey(), entry.getValue());
			}			
			
			LogfileMonitorManager.pushMonitorInfoList(list, rtnMap);

		} catch (HinemosUnknown_Exception e) {
			m_log.error(e,e);
		} catch (InvalidRole_Exception e) {
			m_log.warn("realoadLogfileMonitor: " + e.getMessage());
		} catch (InvalidUserPass_Exception e) {
			m_log.warn("realoadLogfileMonitor: " + e.getMessage());
		} catch (MonitorNotFound_Exception e) {
			m_log.warn("realoadLogfileMonitor: " + e.getMessage());
		}
	}

	private void reloadWinEventMonitor (SettingUpdateInfo updateInfo, boolean force) {
		if (!isWinEventMonitorReload(updateInfo) && !force) {
			return;
		}
		// OSがWindows以外の場合、スキップする
		String osName = System.getProperty("os.name");
		if(osName == null || ! osName.startsWith("Windows")){
			return;
		}

		m_log.info("reloading configuration of windows event monitoring...");
		try {
			// 監視ジョブ以外
			ArrayList<MonitorInfo> list = AgentEndPointWrapper.getMonitorWinEvent();
			m_log.debug("windows event monitoring list size : " + list.size());
			
			WinEventMonitorManager.getWinEventMonitorMap().clear();
			ArrayList<String> necessaryBookmarkFileList = new ArrayList<String>();
			
			for (MonitorInfo info : list) {
				m_log.info("winevent: critical=" + info.getWinEventCheckInfo().isLevelCritical() +
						", warning=" + info.getWinEventCheckInfo().isLevelWarning() +
						", verbose=" + info.getWinEventCheckInfo().isLevelVerbose() +
						", error=" + info.getWinEventCheckInfo().isLevelError() +
						", informational=" + info.getWinEventCheckInfo().isLevelInformational() +
						", monitorId=" + info.getMonitorId() +
						", monitorFlg=" + info.isMonitorFlg());
				WinEventMonitor winEventMonitor = new WinEventMonitor(info, null);
				WinEventMonitorManager.getWinEventMonitorMap().put(info.getMonitorId(), winEventMonitor);
				
				// register needed bookmark file
				for(String logName : info.getWinEventCheckInfo().getLogName()) {
					necessaryBookmarkFileList.add(WinEventMonitor.PREFIX + info.getMonitorId() + "-" + logName.replaceAll(WinEventMonitor.INVALID_FILE_CHARACTER, "") + WinEventMonitor.POSTFIX_BOOKMARK + ".xml");
				}
			}

			// 監視ジョブ
			HashMapInfo hashMapInfo = AgentEndPointWrapper.getMonitorJobWinEvent();
			Map8 map = hashMapInfo.getMap8();
			HashMap<RunInstructionInfo, MonitorInfo> rtnMap = new HashMap<RunInstructionInfo, MonitorInfo>();
			for(Map8.Entry entry : map.getEntry()) {
				m_log.info("winevent: critical=" + entry.getValue().getWinEventCheckInfo().isLevelCritical() +
						", warning=" + entry.getValue().getWinEventCheckInfo().isLevelWarning() +
						", verbose=" + entry.getValue().getWinEventCheckInfo().isLevelVerbose() +
						", error=" + entry.getValue().getWinEventCheckInfo().isLevelError() +
						", informational=" + entry.getValue().getWinEventCheckInfo().isLevelInformational() +
						", monitorId=" + entry.getValue().getMonitorId() +
						", monitorFlg=" + entry.getValue().isMonitorFlg());
				WinEventMonitor winEventMonitor = new WinEventMonitor(entry.getValue(), entry.getKey());
				String mapKey = entry.getKey().getSessionId()
						+ entry.getKey().getJobunitId()
						+ entry.getKey().getJobId()
						+ entry.getKey().getFacilityId()
						+ entry.getValue().getMonitorId();
				WinEventMonitorManager.getWinEventMonitorMap().put(mapKey, winEventMonitor);
				rtnMap.put(entry.getKey(), entry.getValue());

				// register needed bookmark file
				for(String logName : entry.getValue().getWinEventCheckInfo().getLogName()) {
					necessaryBookmarkFileList.add(WinEventMonitor.PREFIX + mapKey + "-" + logName.replaceAll(WinEventMonitor.INVALID_FILE_CHARACTER, "") + WinEventMonitor.POSTFIX_BOOKMARK + ".xml");
				}
			}
			m_log.debug("windows event monitoring list size (monitoring job) : " + rtnMap.size());

			// reset WinEvent Collector and delete unnecessary bookmark files
			File[] files = new File(WinEventMonitor.runPath).listFiles();
			// delete unnecessary bookmark file
			if(files != null) {
				for(int i = 0; i < files.length; i++){
					File file = files[i];
					String fileName = file.getName();
					if(fileName.endsWith(WinEventMonitor.POSTFIX_BOOKMARK + ".xml") && !necessaryBookmarkFileList.contains(fileName)){
						m_log.info("deleted unnecessary bookmark file : " + fileName);
						boolean deleted = file.delete();
						if(!deleted){
							m_log.error("could not delete bookmark file : " + fileName);
						}
					}
				}
			} else {
				//WinEventMonitor.runPathは必ずディレクトリのためここにはこないはず
				m_log.error("listFiles returns null. WinEventMonitor.runPath may not be a directory : " + WinEventMonitor.runPath);
			}
		} catch (HinemosUnknown_Exception e) {
			m_log.error(e,e);
		} catch (InvalidRole_Exception e) {
			m_log.warn("realoadWinEventMonitor: " + e.getMessage());
		} catch (InvalidUserPass_Exception e) {
			m_log.warn("realoadWinEventMonitor: " + e.getMessage());
		} catch (MonitorNotFound_Exception e) {
			m_log.warn("realoadWinEventMonitor: " + e.getMessage());
		}
	}

	private void reloadJobFileCheck (SettingUpdateInfo updateInfo, boolean force) {
		if (!isJobFileCheckReload(updateInfo) && !force) {
			return;
		}
		m_log.info("reloading configuration of filecheck monitoring...");
		try {
			ArrayList<JobFileCheck> list = AgentEndPointWrapper.getFileCheckForAgent();

			for (JobFileCheck info : list) {
				m_log.info("filecheck: directory=" + info.getDirectory() +
						", id=" + info.getId() +
						", valid=" + info.isValid());
			}
			FileCheckManager.setFileCheck(list);
		} catch (HinemosUnknown_Exception e) {
			m_log.error(e,e);
		} catch (InvalidRole_Exception e) {
			m_log.warn("realoadLogfileMonitor: " + e.getMessage());
		} catch (InvalidUserPass_Exception e) {
			m_log.warn("realoadLogfileMonitor: " + e.getMessage());
		} catch (JobMasterNotFound_Exception e) {
			m_log.warn("realoadLogfileMonitor: " + e.getMessage());
		} catch (MonitorNotFound_Exception e) {
			m_log.warn("realoadLogfileMonitor: " + e.getMessage());
		}
	}

	private void runJob (RunInstructionInfo info) {
		m_log.debug("onMessage SessionID=" + info.getSessionId()
				+ ", JobID=" + info.getJobId()
				+ ", CommandType=" + info.getCommandType());

		m_log.debug("onMessage CommandType != CHECK");

		//実行履歴チェック
		try{
			if (RunHistoryUtil.findRunHistory(info) == null) {
				if(info.getCommand().equals(CommandConstant.GET_PUBLIC_KEY) ||
						info.getCommand().equals(CommandConstant.ADD_PUBLIC_KEY) ||
						info.getCommand().equals(CommandConstant.DELETE_PUBLIC_KEY)){
					//公開鍵用スレッド実行
					m_log.debug("onMessage CommandType = GET_PUBLIC_KEY or ADD_PUBLIC_KEY or DELETE_PUBLIC_KEY");

					PublicKeyThread thread = new PublicKeyThread(info, m_sendQueue);
					thread.start();
				}else if(info.getCommand().equals(CommandConstant.GET_FILE_LIST)){
					//ファイルリスト用スレッド実行
					m_log.debug("onMessage CommandType = GET_FILE_LIST");

					FileListThread thread = new FileListThread(info, m_sendQueue);
					thread.start();
				}else if(info.getCommand().equals(CommandConstant.GET_CHECKSUM) ||
						info.getCommand().equals(CommandConstant.CHECK_CHECKSUM)){
					//チェックサム用スレッド実行
					m_log.debug("onMessage CommandType = GET_CHECKSUM or CHECK_CHECKSUM");

					CheckSumThread thread = new CheckSumThread(info, m_sendQueue);
					thread.start();
				}else if(info.getCommandType() == CommandTypeConstant.NORMAL ||
						(info.getCommandType() == CommandTypeConstant.STOP && info.getStopType() == CommandStopTypeConstant.EXECUTE_COMMAND)){
					//コマンド実行
					CommandThread thread = new CommandThread(info, m_sendQueue);
					thread.start();
				} else  if (info.getCommandType() == CommandTypeConstant.STOP
						&& info.getStopType() == CommandStopTypeConstant.DESTROY_PROCESS) {
					// ここには普通はこないが、ジョブ実行中に再起動した場合にくる
					m_log.warn("runJob() : logical error, runHistory = null, DESTROY_PROCESS");
					DeleteProcessThread thread = new DeleteProcessThread(info, m_sendQueue);
					thread.start();
				} else {
					//ここは通らないはず
					m_log.warn("runJob() : logical error, runHistory = null");
				}
			}else {
				if (info.getCommandType() == CommandTypeConstant.STOP
						&& info.getStopType() == CommandStopTypeConstant.DESTROY_PROCESS) {
					// プロセス終了
					DeleteProcessThread thread = new DeleteProcessThread(info, m_sendQueue);
					thread.start();
				} else {
					// タイミングによりジョブ実行命令を2つ受信した場合にここに入る
					m_log.info("runJob() : avoid duplicate running");
				}
			}
		} catch(Throwable e) {
			m_log.warn("hoge " + e.getMessage(), e);
		}
	}


	/**
	 * clearFlgの設定
	 * @param clearFlg
	 */
	private static void setHistoryClear(boolean clearFlg) {
		m_clearFlg = clearFlg;
	}

	/**
	 * clearFlgの取得
	 * @return
	 */
	public static boolean isHistoryClear(){
		return m_clearFlg;
	}
	
	/**
	 * reloadFlgの設定
	 * @param reloadFlog
	 */
	public static void setReloadFlg(boolean reloadFlg) {
		m_reloadFlg = reloadFlg;
	}

	/**
	 * reloadFlgの取得
	 * @return
	 */
	public static boolean isReloadFlg(){
		return m_reloadFlg;
	}
	
	
	/**
	 * ジョブ実行履歴削除
	 * 通信エラーとなった場合に、一定時間後、ジョブ履歴情報を削除する
	 */
	public void clearJobHistory() {
		m_log.debug("clearJobHistory start");
		try{
			if (RunHistoryUtil.clearRunHistory()) {
				m_log.info("job history was deleted.");
				setHistoryClear(true);
			}
		} catch (Exception e) {
			m_log.error("clearJobHistory : ", e);
		}
	}

	public static int getTopicInterval() {
		return m_topicInterval;
	}
}
