/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgentInfoRequest;
import org.openapitools.client.model.AgtCustomMonitorInfoResponse;
import org.openapitools.client.model.AgtJobFileCheckResponse;
import org.openapitools.client.model.AgtMonitorInfoRequest;
import org.openapitools.client.model.AgtMonitorInfoResponse;
import org.openapitools.client.model.AgtNodeConfigRunCollectInfoResponse;
import org.openapitools.client.model.AgtNodeConfigSettingResponse;
import org.openapitools.client.model.AgtNodeInfoResponse;
import org.openapitools.client.model.AgtRunInstructionInfoResponse;
import org.openapitools.client.model.CancelUpdateRequest;
import org.openapitools.client.model.GetFileCheckResponse;
import org.openapitools.client.model.GetHinemosTopicResponse;
import org.openapitools.client.model.GetMonitorForAgentResponse;
import org.openapitools.client.model.GetMonitorJobMapRequest;
import org.openapitools.client.model.GetMonitorJobMapResponse;
import org.openapitools.client.model.GetNodeConfigSettingResponse;
import org.openapitools.client.model.GetNodeInfoListResponse;
import org.openapitools.client.model.SettingUpdateInfoResponse;
import org.openapitools.client.model.TopicInfoResponse;

import com.clustercontrol.agent.bean.TopicFlagConstant;
import com.clustercontrol.agent.binary.BinaryMonitorManager;
import com.clustercontrol.agent.cloud.log.CloudLogMonitor;
import com.clustercontrol.agent.cloud.log.CloudLogMonitorConfig;
import com.clustercontrol.agent.cloud.log.CloudLogMonitorManager;
import com.clustercontrol.agent.cloud.log.CloudLogMonitorProperty;
import com.clustercontrol.agent.cloud.log.CloudLogMonitorUtil;
import com.clustercontrol.agent.cloud.log.util.CloudLogfileMonitorManager;
import com.clustercontrol.agent.custom.CommandCollector;
import com.clustercontrol.agent.filecheck.FileCheckManager;
import com.clustercontrol.agent.job.AgentThread;
import com.clustercontrol.agent.job.CheckSumThread;
import com.clustercontrol.agent.job.CommandThread;
import com.clustercontrol.agent.job.DeleteProcessThread;
import com.clustercontrol.agent.job.FileCheckJobThread;
import com.clustercontrol.agent.job.FileListThread;
import com.clustercontrol.agent.job.PublicKeyThread;
import com.clustercontrol.agent.job.RunHistoryUtil;
import com.clustercontrol.agent.log.LogfileMonitorManager;
import com.clustercontrol.agent.log.MonitorInfoWrapper;
import com.clustercontrol.agent.repository.NodeConfigCollector;
import com.clustercontrol.agent.repository.NodeConfigConstant.Function;
import com.clustercontrol.agent.sdml.SdmlFileMonitorManager;
import com.clustercontrol.agent.sdml.SdmlReceiveTopic;
import com.clustercontrol.agent.repository.NodeRegister;
import com.clustercontrol.agent.selfcheck.SelfCheckManager;
import com.clustercontrol.agent.rpa.RpaLogfileMonitorManager;
import com.clustercontrol.agent.rpa.RpaMonitorInfoWrapper;
import com.clustercontrol.agent.rpa.scenariojob.RpaScenarioThread;
import com.clustercontrol.agent.rpa.scenariojob.ScreenshotThread;
import com.clustercontrol.agent.update.AgentUpdater;
import com.clustercontrol.agent.update.RpaExecuterUpdater;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.agent.util.CollectorId;
import com.clustercontrol.agent.util.CollectorManager;
import com.clustercontrol.agent.util.CommandMonitoringWSUtil;
import com.clustercontrol.agent.util.RestAgentBeanUtil;
import com.clustercontrol.agent.winevent.WinEventMonitor;
import com.clustercontrol.agent.winevent.WinEventMonitorManager;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.NodeConfigSettingNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.jobmanagement.bean.CommandConstant;
import com.clustercontrol.jobmanagement.bean.CommandStopTypeConstant;
import com.clustercontrol.jobmanagement.bean.CommandTypeConstant;
import com.clustercontrol.jobmanagement.rpa.util.RpaWindowsUtil;
import com.clustercontrol.repository.bean.AgentCommandConstant;
import com.clustercontrol.util.EnvUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

/**
 * Topicを受信するクラス<BR>
 * Topicへの接続と、メッセージの受信を行います。
 * 
 * Topicでマネージャからのジョブ実行指示を受け取ります。
 * 
 * @version 6.2.0 構成情報関連の対応
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
	
	// ノード自動登録.
	private static boolean retryToRegNode = true;
	private static Object retryToRegNodeLock = new Object();
	
	// リモートアップデート処理オブジェクト
	private AgentUpdater updater = new AgentUpdater();
	private RpaExecuterUpdater rpaExecuterUpdater = new RpaExecuterUpdater();
	
	// topic受信とエージェント終了時の通信コンフリクトを防ぐためのロック
	public static final Object lockTopicReceiveTiming = new Object();
	// エージェン終了フラグ
	private static boolean isTerminated = false;
	public static void terminate() {
		isTerminated = true;
	}

	// 最後に受信した設定情報更新日時
	private static SettingUpdateInfoResponse settingLastUpdateInfo = null;

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
		LogfileMonitorManager.getInstance().setSendQueue(m_sendQueue);
		BinaryMonitorManager.setSendQueue(m_sendQueue);
		WinEventMonitorManager.setSendQueue(m_sendQueue);
		FileCheckManager.setSendQueue(m_sendQueue);
		NodeConfigCollector.setSendQueue(m_sendQueue);
		SdmlFileMonitorManager.getInstance().setSendQueue(m_sendQueue);
		SelfCheckManager.setSendQueue(m_sendQueue);
		RpaLogfileMonitorManager.getInstance().setSendQueue(m_sendQueue);
		CloudLogMonitorUtil.setSendQueue(m_sendQueue);
	}


	/**
	 * マネージャからのTopic(即時実行など)が発行された際に、ラッチを開放する。
	 */
	private CountDownLatch countDownLatch = null;
	// 初期処理中にManagerから呼び出された場合にtrue
	private boolean immediateRelease = false;

	public void releaseLatch() {
		if (countDownLatch == null) {
			m_log.debug("CountDownLatch is null");
			immediateRelease = true;
		} else {
			countDownLatch.countDown();
		}
	}

	/**
	 * トピック受信処理
	 */
	@Override
	public void run() {

		m_log.info("run start");

		while (true) {
			// ノード自動登録、Manager接続に失敗した場合はリトライ.
			if(retryToRegNode){
				synchronized (retryToRegNodeLock) {
					retryToRegNode = NodeRegister.callRegister();
				}
			}
			if(retryToRegNode){
				// リトライ時はノード登録前なのでgetTopic走らせない.
				try {
					Thread.sleep(m_topicInterval);
				} catch (InterruptedException e) {
					m_log.warn("NodeRegister() : " + e.getMessage());
				}
				m_log.info("NodeRegister() : " + "to retry to register node automatically.");
				continue;
			}
			
			/*
			 * トピックの有無をマネージャにチェックし終わったら、sleepする。
			 * 
			 */
			try {
				int interval = m_topicInterval;
				countDownLatch = new CountDownLatch(1);
				if(!immediateRelease){
					boolean awaiting = countDownLatch.await(interval, TimeUnit.MILLISECONDS);
					if (!awaiting) {
						m_log.debug("waiting is over");
					}
				}
			} catch (InterruptedException e) {
				m_log.warn("Interrupt " + e.getMessage());
			} catch (Exception e) {
				m_log.error("run() : " + e.getMessage(), e);
			}
			
			immediateRelease = false;
			try {
				List<AgtRunInstructionInfoResponse> runInstructionList = new ArrayList<AgtRunInstructionInfoResponse>();
				m_log.info("getTopic " + Agent.getAgentStr() + " start");
				GetHinemosTopicResponse hinemosTopicInfo = null;
				List<TopicInfoResponse> topicInfoList = null;
				SettingUpdateInfoResponse updateInfo = null;
				try {
					// エージェントの終了処理が行われているにもかかわらずtopicをとりにいってしまう競合を防ぐためsynchronizedする
					synchronized (lockTopicReceiveTiming) {
						if (isTerminated == true) {
							// エージェントの終了処理が行われている場合、このスレッドを即時終了させる
							return;
						}
						// IPアドレスが更新されたタイミング、またはエージェントがマネージャに認識されるまでは、DHCPサポート機能によりノードを更新する(Agentプロパティで有効にしている場合)
						boolean updateNode = isReloadFlg() || !Agent.isRegistered();
						m_log.debug(String.format("isReloadFlg=%s, isRegistered=%s, updateNode=%s", isReloadFlg(), Agent.isRegistered(), updateNode));
						hinemosTopicInfo = AgentRestClientWrapper.getHinemosTopic(Agent.getAgentInfoRequest(), updateNode);
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
							+ disconnectCounter + "*" + m_topicInterval + ":" + m_runhistoryClearDelay + ", " +
							e.getClass().getSimpleName() + ", " + e.getMessage();
					m_log.warn(message);
					m_log.debug(message, e);
					continue; // whileまで戻る。
				}
				
				// マネージャに認識されたかどうか
				m_log.debug("this agent is regisitered:" + hinemosTopicInfo.getRegistered());
				Agent.setRegistered(hinemosTopicInfo.getRegistered());

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
					reloadRpaLogfileMonitor(updateInfo, true);
					reloadBinaryMonitor(updateInfo, true);
					reloadCustomMonitor(updateInfo, true);
					reloadNodeConfigSetting(updateInfo, true);
					reloadNodeConfigRunCollect(updateInfo, true);
					reloadWinEventMonitor(updateInfo, true);
					reloadJobFileCheck(updateInfo, true);
					SdmlReceiveTopic.getInstance().reloadSdmlSetting(updateInfo, settingLastUpdateInfo, true);
					reloadCloudLogMonitor(updateInfo, true);
					try {
						updater.sendProfile();
						// マネージャとの接続初回での、プロファイル送信成功なら、バックアップを削除する。
						if (disconnectCounter != 0) {
							if (Boolean.parseBoolean(AgentProperties.getProperty("update.backup.keep"))) {
								m_log.info("run: Keep backup.");
							} else {
								updater.sweepBackup();
							}
						}
					} catch (Exception e) {
						m_log.warn("run: Failed to send profile.", e);
					}
					setReloadFlg(false);
				}
				disconnectCounter = 0;
				setHistoryClear(false);

				// topicリストの内容を仕分ける
				m_log.debug("run : topicInfoList.size=" + topicInfoList.size());
				int agentCommand = AgentCommandConstant.NONE;
				for (TopicInfoResponse topicInfo : topicInfoList) {
					long topicFlag = topicInfo.getFlag();
					m_log.info("getTopic flag=" + topicFlag);

					// ジョブ実行指示
					AgtRunInstructionInfoResponse runInstructionInfo = topicInfo.getRunInstructionInfo();
					if (runInstructionInfo != null) {
						runInstructionList.add(runInstructionInfo);
					}
					
					// 複数の「再起動 or アップデート」指示は、アップデート優先で1つにする
					if (topicInfo.getAgentCommand() != AgentCommandConstant.NONE) {
						m_log.info("run: agentCommand=" + topicInfo.getAgentCommand());
						if (agentCommand != AgentCommandConstant.UPDATE) {
							agentCommand = topicInfo.getAgentCommand();
						}
					}

					// プロファイル送信要求にはすぐに応答しておく (アップデート時に必要となるので)
					if ((topicFlag & TopicFlagConstant.NEW_FACILITY) != 0) {
						try {
							updater.sendProfile();
						} catch (Exception e) {
							m_log.warn("run: Failed to send profile.", e);
						}
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
					if ((topicFlag & TopicFlagConstant.REPOSITORY_CHANGED) != 0 ||
							(topicFlag & TopicFlagConstant.NEW_FACILITY) != 0 ||
							(topicFlag & TopicFlagConstant.CALENDAR_CHANGED) != 0 ||
							(topicFlag & TopicFlagConstant.NODE_CONFIG_SETTING_CHANGED) != 0) {
						reloadNodeConfigSetting(updateInfo, true);
					}
					if ((topicFlag & TopicFlagConstant.NODE_CONFIG_RUN_COLLECT) != 0) {
						reloadNodeConfigRunCollect(updateInfo, true);
					}
				}

				reloadLogfileMonitor(updateInfo, false);
				reloadRpaLogfileMonitor(updateInfo, false);
				reloadBinaryMonitor(updateInfo, false);
				reloadCustomMonitor(updateInfo, false);
				reloadNodeConfigSetting(updateInfo, false);
				reloadNodeConfigRunCollect(updateInfo, false);
				reloadWinEventMonitor(updateInfo, false);
				reloadJobFileCheck(updateInfo, false);
				SdmlReceiveTopic.getInstance().reloadSdmlSetting(updateInfo, settingLastUpdateInfo, false);
				reloadCloudLogMonitor(updateInfo, false);
				
				settingLastUpdateInfo = updateInfo;

				m_log.debug("getTopic " + Agent.getAgentStr() + " end");
				if (runInstructionList.size() > 0) {
					m_log.info("infoList.size() = " + runInstructionList.size());
				} else {
					m_log.debug("infoList.size() = 0");
				}

				for (AgtRunInstructionInfoResponse info : runInstructionList) {
					runJob(info);
				}

				// 再起動あるいは更新を実行する
				if (agentCommand == AgentCommandConstant.RESTART) {
					m_log.info("run: Execute restart command.");
					Agent.restart(AgentCommandConstant.RESTART);
				} else if (agentCommand == AgentCommandConstant.UPDATE) {
					m_log.info("run: Execute update command.");
					try {
						if (updater.download()) {
							// Windowsの場合のみrpaExecuterUpdaterを実施
							String osName = System.getProperty("os.name");
							if(osName != null && osName.startsWith("Windows")){
								try {
									rpaExecuterUpdater.updateJarAndScript(m_sendQueue);
								} catch (Throwable e) {
									m_log.warn("run: Failed to update the file of rpa executer.", e);
								}
							}
							Agent.restart(AgentCommandConstant.UPDATE);
						}
					} catch (Throwable e) {
						m_log.warn("run: Failed to download update files.", e);
						try {
							CancelUpdateRequest req = new CancelUpdateRequest();
							req.setAgentInfo(Agent.getAgentInfoRequest());
							req.setCause(e.getClass().getSimpleName() + ", " + e.getMessage());
							AgentRestClientWrapper.cancelUpdate(req);
						} catch (Exception ee) {
							// ログ出力だけして無視する
							m_log.warn("run: Failed to cancel update.", ee);
						}
					}
				}

			} catch (Throwable e) {
				m_log.error("run() : " + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			}
		}
	}

	/**
	 *	リロードする必要がある場合はtrueを返す
	 * @param updateInfo
	 * @return
	 */
	private boolean isCustomMonitorReload(SettingUpdateInfoResponse updateInfo) {
		if (updateInfo == null) {
			return false;
		} else if (settingLastUpdateInfo == null) {
			return true;
		} else {
			if (settingLastUpdateInfo.getCustomMonitorUpdateTime().equals(updateInfo.getCustomMonitorUpdateTime())
					&& settingLastUpdateInfo.getCalendarUpdateTime().equals(updateInfo.getCalendarUpdateTime())
					&& settingLastUpdateInfo.getRepositoryUpdateTime().equals(updateInfo.getRepositoryUpdateTime())) {
				return false;
			} else {
				return true;
			}
		}
	}
	
	private boolean isNodeConfigSettingReload(SettingUpdateInfoResponse updateInfo) {
		if (updateInfo == null) {
			return false;
		} else if (settingLastUpdateInfo == null) {
			return true;
		} else {
			if (settingLastUpdateInfo.getNodeConfigSettingUpdateTime().equals(updateInfo.getNodeConfigSettingUpdateTime())
					&& settingLastUpdateInfo.getCalendarUpdateTime().equals(updateInfo.getCalendarUpdateTime())
					&& settingLastUpdateInfo.getRepositoryUpdateTime().equals(updateInfo.getRepositoryUpdateTime())) {
				return false;
			} else {
				return true;
			}
		}
	}

	private boolean isNodeConfigRunCollectReload(SettingUpdateInfoResponse updateInfo) {
		if (updateInfo == null) {
			return false;
		} else if (settingLastUpdateInfo == null) {
			return true;
		} else {
			if (settingLastUpdateInfo.getNodeConfigRunCollectUpdateTime().equals(updateInfo.getNodeConfigRunCollectUpdateTime())) {
				return false;
			} else {
				return true;
			}
		}
	}
	
	private boolean isWinEventMonitorReload(SettingUpdateInfoResponse updateInfo) {
		if (updateInfo == null) {
			return false;
		} else if (settingLastUpdateInfo == null) {
			return true;
		} else {
			if (settingLastUpdateInfo.getWinEventMonitorUpdateTime().equals(updateInfo.getWinEventMonitorUpdateTime())
					&& settingLastUpdateInfo.getCalendarUpdateTime().equals(updateInfo.getCalendarUpdateTime())
					&& settingLastUpdateInfo.getRepositoryUpdateTime().equals(updateInfo.getRepositoryUpdateTime())) {
				return false;
			} else {
				return true;
			}
		}
	}

	private boolean isLogfileMonitorReload(SettingUpdateInfoResponse updateInfo) {
		if (updateInfo == null) {
			return false;
		} else if (settingLastUpdateInfo == null) {
			return true;
		} else {
			if (settingLastUpdateInfo.getLogFileMonitorUpdateTime().equals(updateInfo.getLogFileMonitorUpdateTime())
					&& settingLastUpdateInfo.getCalendarUpdateTime().equals(updateInfo.getCalendarUpdateTime())
					&& settingLastUpdateInfo.getRepositoryUpdateTime().equals(updateInfo.getRepositoryUpdateTime())) {
				return false;
			} else {
				return true;
			}
		}
	}
	
	/**
	 * RPAログファイル監視監視読込実施判定.
	 * 
	 * @return 読込実施の場合はtrue.
	 */
	private boolean isRpaLogfileMonitorReload(SettingUpdateInfoResponse updateInfo) {
		if (updateInfo == null) {
			return false;
		} else if (settingLastUpdateInfo == null) {
			return true;
		} else {
			if (settingLastUpdateInfo.getRpaLogFileMonitorUpdateTime().equals(updateInfo.getRpaLogFileMonitorUpdateTime())
					&& settingLastUpdateInfo.getCalendarUpdateTime().equals(updateInfo.getCalendarUpdateTime())
					&& settingLastUpdateInfo.getRepositoryUpdateTime().equals(updateInfo.getRepositoryUpdateTime())) {
				return false;
			} else {
				return true;
			}
		}
	}
	
	private boolean isCloudLogMonitorReload(SettingUpdateInfoResponse updateInfo) {
		if (updateInfo == null) {
			return false;
		} else if (settingLastUpdateInfo == null) {
			return true;
		} else {
			if (settingLastUpdateInfo.getCloudLogMonitorUpdateTime().equals(updateInfo.getCloudLogMonitorUpdateTime())
					&& settingLastUpdateInfo.getCalendarUpdateTime().equals(updateInfo.getCalendarUpdateTime())
					&& settingLastUpdateInfo.getRepositoryUpdateTime().equals(updateInfo.getRepositoryUpdateTime())) {
				return false;
			} else {
				return true;
			}
		}
	}
	
	/**
	 * バイナリ監視読込実施判定.
	 * 
	 * @return 読込実施の場合はtrue.
	 */
	private boolean isBinaryMonitorReload(SettingUpdateInfoResponse updateInfo) {
		if (updateInfo == null) {
			// マネージャー更新情報が存在しない場合は実施対象外.
			return false;
		} else if (settingLastUpdateInfo == null) {
			// マネージャーからの更新実施していない場合は実施対象.
			return true;
		} else {
			if (settingLastUpdateInfo.getBinaryMonitorUpdateTime().equals(updateInfo.getBinaryMonitorUpdateTime())
					&& settingLastUpdateInfo.getCalendarUpdateTime().equals(updateInfo.getCalendarUpdateTime())
					&& settingLastUpdateInfo.getRepositoryUpdateTime().equals(updateInfo.getRepositoryUpdateTime())) {
				// バイナリ監視・カレンダー・リポジトリの更新がされていない場合は実施対象外.
				return false;
			} else {
				return true;
			}
		}
	}

	private boolean isJobFileCheckReload(SettingUpdateInfoResponse updateInfo) {
		if (updateInfo == null) {
			return false;
		} else if (settingLastUpdateInfo == null) {
			return true;
		} else {
			if (settingLastUpdateInfo.getJobFileCheckUpdateTime().equals(updateInfo.getJobFileCheckUpdateTime())
					&& settingLastUpdateInfo.getCalendarUpdateTime().equals(updateInfo.getCalendarUpdateTime())
					&& settingLastUpdateInfo.getRepositoryUpdateTime().equals(updateInfo.getRepositoryUpdateTime())) {
				return false;
			} else {
				return true;
			}
		}
	}

	private void reloadCustomMonitor(SettingUpdateInfoResponse updateInfo, boolean force) {
		if (!isCustomMonitorReload(updateInfo) && !force) {
			return;
		}

		m_log.info("reloading configuration of custom monitoring...");
		try {
			List<AgtCustomMonitorInfoResponse> dtos = new ArrayList<>();
			AgentInfoRequest agentInfoRequest = Agent.getAgentInfoRequest();
			dtos.addAll(AgentRestClientWrapper.getMonitorCustom(false, agentInfoRequest).getList());
			dtos.addAll(AgentRestClientWrapper.getMonitorCustom(true, agentInfoRequest).getList());

			// unregister unnecessary Command Collector
			for (CollectorId collectId : CollectorManager.getAllCollectorIds()) {
				if (collectId.type != CommandCollector._collectorType) {
					continue;
				}
				boolean unnecessary = true;
				for (AgtCustomMonitorInfoResponse dto : dtos) {
					if (collectId.id != null && collectId.id.equals(dto.getFacilityId() + dto.getMonitorId())) {
						unnecessary = false;
					}
				}
				if (unnecessary) {
					CollectorManager.unregisterCollectorTask(collectId);
				}
			}

			// reset Command Collector
			for (AgtCustomMonitorInfoResponse dto : dtos) {
				m_log.info("reloaded configuration : " + CommandMonitoringWSUtil.toStringCommandExecuteDTO(dto));
				CollectorManager.registerCollectorTask(new CommandCollector(dto));
			}
		} catch (HinemosUnknown e) {
			m_log.error(e, e);
		} catch (InvalidRole | InvalidUserPass | InvalidSetting | RestConnectFailed e) {
			m_log.warn("reloadCustomMonitor: " + e.getMessage());
		}
	}

	private void reloadNodeConfigSetting(SettingUpdateInfoResponse updateInfo, boolean force) {
		if (!isNodeConfigSettingReload(updateInfo) && !force) {
			return;
		}
		// Local Variables
		List<AgtNodeConfigSettingResponse> dtoList = null;
		List<AgtNodeInfoResponse> nodeInfoList = null;

		// MAIN
		m_log.info("reloading configuration of nodeconfig setting...");
		try {
			AgentInfoRequest agentInfo = Agent.getAgentInfoRequest();
			GetNodeConfigSettingResponse rsp1 = AgentNodeConfigRestClientWrapper.getNodeConfigSetting(agentInfo);
			dtoList = rsp1.getList();
			GetNodeInfoListResponse rsp2 = AgentNodeConfigRestClientWrapper.getNodeInfoList(agentInfo);
			nodeInfoList = rsp2.getList();
			// unregister unnecessary NodeConfig Collector
			for (CollectorId collectId : CollectorManager.getAllCollectorIds()) {
				if (collectId.type != NodeConfigCollector._collectorType) {
					continue;
				}
				boolean unnecessary = true;
				
				for (AgtNodeConfigSettingResponse dto : dtoList) {
					if (collectId.id != null && collectId.id.equals(dto.getFacilityId() + dto.getSettingId())) {
						unnecessary = false;
					}
				}
				if (unnecessary) {
					CollectorManager.unregisterCollectorTask(collectId);
				}
			}
		
			for (AgtNodeConfigSettingResponse dto : dtoList) {
				// reset Command Collector
				m_log.info("reloaded configuration : " + dto.getSettingId());
				CollectorManager.registerCollectorTask(new NodeConfigCollector(dto, Function.REGULAR_COLLECT,nodeInfoList));
			}
		} catch (HinemosUnknown e) {
			m_log.warn("unexpected error.", e);
		} catch (InvalidRole | InvalidUserPass | InvalidSetting | NodeConfigSettingNotFound | RestConnectFailed e) {
			m_log.warn("reloadNodeConfigSetting: " + e.getMessage());
		} catch (FacilityNotFound e) {
			m_log.warn("reloadNodeConfigSetting: FacilityNotFound. " + e.getMessage());
			// マネージャ上にエージェントのノードが存在しないため、構成情報(周期実行)のタスクを削除する
			for (CollectorId collectId : CollectorManager.getAllCollectorIds() ){
				if (collectId.type == NodeConfigCollector._collectorType) {
					CollectorManager.unregisterCollectorTask(collectId);
				}
			}
		}
	}

	private void reloadNodeConfigRunCollect(SettingUpdateInfoResponse updateInfo, boolean force) {
		//
		if (!isNodeConfigRunCollectReload(updateInfo) && !force) {
			return;
		}
		// Local Variables
		AgtNodeConfigRunCollectInfoResponse runCollectInfo = null;
		List<AgtNodeInfoResponse> nodeInfoList = null;
		// Managerから即時実行に関する情報を取得.
		m_log.info("reloading configuration of nodeconfig run-collect info...");
		try {
			AgentInfoRequest agentInfo = Agent.getAgentInfoRequest();
			GetNodeInfoListResponse rsp1 = AgentNodeConfigRestClientWrapper.getNodeInfoList(agentInfo);
			nodeInfoList = rsp1.getList();
			runCollectInfo = AgentNodeConfigRestClientWrapper.getNodeConfigRunCollectInfo(agentInfo);
		} catch (HinemosUnknown e) {
			m_log.warn("unexpected error", e);
			return;
		} catch (InvalidRole | InvalidUserPass | InvalidSetting | FacilityNotFound | RestConnectFailed e) {
			m_log.warn("reloadNodeConfigRunCollectInfo: " + e.getMessage());
			return;
		}

		// Managerからの取得値チェック.
		Long loadDistributionTime = runCollectInfo.getLoadDistributionTime();
		if (loadDistributionTime == null) {
			m_log.warn("failed to reload load-distribution-time in configuration of nodeconfig run-collect info."
					+ " for more information, see 'hinemos_manager.log'.");
			return;
		}
		if (runCollectInfo.getInstructedInfoMapKeys().isEmpty()) {
			m_log.debug("reloaded empty configuration of nodeconfig run-collect info.");
			return;
		}

		// 即時実行情報に従って、即時実行スタート.
		for (int i = 0; i < runCollectInfo.getInstructedInfoMapKeys().size(); ++i) {

			AgtNodeConfigSettingResponse setting = runCollectInfo.getInstructedInfoMapKeys().get(i);
			// 値が不正な場合はスキップ.
			if (setting == null) {
				m_log.warn("failed to reload instructed information in configuration of nodeconfig run-collect info." //
						+ " for more information, see 'hinemos_manager.log'.");
				continue;
			}

			Long instructedDate = runCollectInfo.getInstructedInfoMapValues().get(i);
			// 値が不正な場合はスキップ.
			if (instructedDate == null) {
				m_log.warn("failed to reload instructed-date in configuration of nodeconfig run-collect info." //
						+ " for more information, see 'hinemos_manager.log'.");
				continue;
			}
			// 構成情報収集の設定情報毎に実行させる.
			m_log.info("reloaded configuration : " + setting.getSettingId());
			NodeConfigCollector collector = new NodeConfigCollector(setting, Function.RUN_COLLECT, nodeInfoList);
			collector.runCollect(instructedDate, loadDistributionTime);
		}

	}

	private void reloadLogfileMonitor (SettingUpdateInfoResponse updateInfo, boolean force) throws RestConnectFailed {
		if (!isLogfileMonitorReload(updateInfo) && !force) {
			return;
		}
		m_log.info("reloading configuration of logfile monitoring...");
		try {
			// 監視ジョブ以外
			GetMonitorForAgentResponse res = AgentRestClientWrapper.getMonitorLogfile(Agent.getAgentInfoRequest());
			List<AgtMonitorInfoResponse> list = res.getList();
			for (AgtMonitorInfoResponse info : list) {
				m_log.info("logfile: " +
						"directory=" + info.getLogfileCheckInfo().getDirectory() +
						", filename=" + info.getLogfileCheckInfo().getFileName() +
						", fileencoding=" + info.getLogfileCheckInfo().getFileEncoding() +
						", monitorId=" + info.getMonitorId() +
						", monitorFlg=" + info.getMonitorFlg());
			}

			// 監視ジョブ
			GetMonitorJobMapRequest getMonitorJobMapRequest = new GetMonitorJobMapRequest();
			getMonitorJobMapRequest.setAgentInfo(Agent.getAgentInfoRequest());
			getMonitorJobMapRequest.setMonitorTypeId(HinemosModuleConstant.MONITOR_LOGFILE);
			GetMonitorJobMapResponse response = AgentRestClientWrapper.getMonitorJobMap(getMonitorJobMapRequest);
			Map<AgtRunInstructionInfoResponse, AgtMonitorInfoResponse> monJobMap = new HashMap<>();
			for (int i = 0; i < response.getMonitorInfoList().size(); ++i) {
				AgtMonitorInfoResponse mon = response.getMonitorInfoList().get(i);
				AgtRunInstructionInfoResponse job = response.getRunInstructionInfoList().get(i);
				m_log.info("logfile: " +
						"directory=" + mon.getLogfileCheckInfo().getDirectory() +
						", filename=" + mon.getLogfileCheckInfo().getFileName() +
						", fileencoding=" + mon.getLogfileCheckInfo().getFileEncoding() +
						", sessionId=" + job.getSessionId() +
						", jobunitId=" + job.getJobunitId() +
						", jobId=" + job.getJobId() +
						", facilityId=" + job.getFacilityId() +
						", monitorId=" + mon.getMonitorId() +
						", monitorFlg=" + mon.getMonitorFlg());
				monJobMap.put(job, mon);
			}
			List<MonitorInfoWrapper> monitorInfoList = MonitorInfoWrapper.createMonitorInfoList(list, monJobMap);
			LogfileMonitorManager.getInstance().pushMonitorInfoList(monitorInfoList);

		} catch (InvalidSetting | HinemosUnknown e) {
			m_log.error(e,e);
		} catch (InvalidRole e) {
			m_log.warn("realoadLogfileMonitor: " + e.getMessage());
		} catch (InvalidUserPass e) {
			m_log.warn("realoadLogfileMonitor: " + e.getMessage());
		} catch (MonitorNotFound e) {
			m_log.warn("realoadLogfileMonitor: " + e.getMessage());
		}
	}
	
	/**
	 * RPAログファイル監視読込処理.
	 * @see #reloadLogfileMonitor
	 */
	private void reloadRpaLogfileMonitor (SettingUpdateInfoResponse updateInfo, boolean force) throws RestConnectFailed {
		// OSがWindows以外の場合、スキップ
		if(!EnvUtil.isWindows()){
			return;
		}

		if (!isRpaLogfileMonitorReload(updateInfo) && !force) {
			return;
		}

		m_log.info("reloading configuration of rpalogfile monitoring...");
		try {
			GetMonitorForAgentResponse res = AgentRestClientWrapper.getMonitorRpaLogfile(Agent.getAgentInfoRequest());
			List<AgtMonitorInfoResponse> list = res.getList();
			for (AgtMonitorInfoResponse info : list) {
				m_log.info("rpalogfile: " +
						"directory=" + info.getRpaLogFileCheckInfo().getDirectory() +
						", filename=" + info.getRpaLogFileCheckInfo().getFileName() +
						", fileencoding=" + info.getRpaLogFileCheckInfo().getFileEncoding() +
						", monitorId=" + info.getMonitorId() +
						", monitorFlg=" + info.getMonitorFlg());
			}
			List<RpaMonitorInfoWrapper> monitorInfoList = RpaMonitorInfoWrapper.createMonitorInfoList(list);
			RpaLogfileMonitorManager.getInstance().pushMonitorInfoList(monitorInfoList);

		} catch (InvalidSetting | HinemosUnknown e) {
			m_log.error(e,e);
		} catch (InvalidRole e) {
			m_log.warn("realoadLogfileMonitor: " + e.getMessage());
		} catch (InvalidUserPass e) {
			m_log.warn("realoadLogfileMonitor: " + e.getMessage());
		} catch (MonitorNotFound e) {
			m_log.warn("realoadLogfileMonitor: " + e.getMessage());
		}
	}

	private void reloadCloudLogMonitor (SettingUpdateInfoResponse updateInfo, boolean force) throws RestConnectFailed {
		if (!isCloudLogMonitorReload(updateInfo) && !force) {
			return;
		}
		m_log.info("reloading configuration of cloud log monitoring...");
		try {
			GetMonitorForAgentResponse res = AgentRestClientWrapper.getMonitorCloudLog(Agent.getAgentInfoRequest());
			List<AgtMonitorInfoResponse> list = res.getList();
			// 削除用
			ArrayList<String> runningMonJobID = new ArrayList<String>();

			// 監視一覧を取得
			for (AgtMonitorInfoResponse info : list) {
				m_log.info(
						"cloudLog: " + ", monitorId=" + info.getMonitorId() + ", monitorFlg=" + info.getMonitorFlg());

				runningMonJobID.add(info.getMonitorId());
				// クラウドログ監視固有の設定を追加
				AgtMonitorInfoRequest monReq = new AgtMonitorInfoRequest();
				RestAgentBeanUtil.convertBean(info, monReq);
				CloudLogMonitor cloudLogMonitor = new CloudLogMonitor(info, null);
				CloudLogMonitorManager.setLogFileInfo(info);
				// ログファイル関連
				String storepath = CloudLogMonitorUtil.getFileStorePath(cloudLogMonitor.getConfig().getMonitorId());
				info.getLogfileCheckInfo().setDirectory(storepath);
				// ローテート前のファイルのみ監視対象とする(.tmpファイル)
				info.getLogfileCheckInfo().setFileName(info.getMonitorId() + ".*\\.tmp");
				info.getLogfileCheckInfo().setFileEncoding(CloudLogMonitorProperty.getInstance().getTmpFileEncode());

				CloudLogMonitorManager.registerCloudLogTask(cloudLogMonitor);
			}

			// 監視ジョブ
			GetMonitorJobMapRequest getMonitorJobMapRequest = new GetMonitorJobMapRequest();
			getMonitorJobMapRequest.setAgentInfo(Agent.getAgentInfoRequest());
			getMonitorJobMapRequest.setMonitorTypeId(HinemosModuleConstant.MONITOR_CLOUD_LOG);
			GetMonitorJobMapResponse response = AgentRestClientWrapper.getMonitorJobMap(getMonitorJobMapRequest);
			Map<AgtRunInstructionInfoResponse, AgtMonitorInfoResponse> monJobMap = new HashMap<>();

			for (int i = 0; i < response.getMonitorInfoList().size(); ++i) {
				AgtMonitorInfoResponse mon = response.getMonitorInfoList().get(i);
				AgtRunInstructionInfoResponse job = response.getRunInstructionInfoList().get(i);
				m_log.info("cloudlog: " + ", sessionId=" + job.getSessionId() + ", jobunitId=" + job.getJobunitId()
						+ ", jobId=" + job.getJobId() + ", facilityId=" + job.getFacilityId() + ", monitorId="
						+ mon.getMonitorId() + ", monitorFlg=" + mon.getMonitorFlg());

				AgtMonitorInfoRequest monReq = new AgtMonitorInfoRequest();
				RestAgentBeanUtil.convertBean(mon, monReq);
				CloudLogMonitor cloudLogMonitor = new CloudLogMonitor(mon, job);

				// ログファイル関連
				String storepath = CloudLogMonitorUtil.getFileStorePath(cloudLogMonitor.getConfig().getMonitorId());
				CloudLogMonitorManager.setLogFileInfo(mon);
				mon.getLogfileCheckInfo().setDirectory(storepath);
				mon.getLogfileCheckInfo().setFileName(cloudLogMonitor.getConfig().getMonitorId() + ".*");
				mon.getLogfileCheckInfo().setFileEncoding(CloudLogMonitorProperty.getInstance().getTmpFileEncode());

				// クラウドログ固有
				CloudLogMonitorConfig config = cloudLogMonitor.getConfig();

				// 監視ジョブの場合は最初から
				config.setLastFireTime(0L);
				cloudLogMonitor.setConfig(config);
				runningMonJobID.add(cloudLogMonitor.getConfig().getMonitorId());
				CloudLogMonitorManager.registerCloudLogTask(cloudLogMonitor);

				monJobMap.put(job, mon);
			}

			List<MonitorInfoWrapper> monitorInfoList = MonitorInfoWrapper.createMonitorInfoList(list, monJobMap);
			CloudLogfileMonitorManager.getInstance().pushMonitorInfoList(monitorInfoList);

			// unregister unnecessary Command Collector
			for (String monitorId : CloudLogMonitorManager.getAllCloudLogIds()) {
				boolean unnecessary = true;
				for (String id : runningMonJobID) {
					if (monitorId.equals(id)) {
						unnecessary = false;
					}
				}
				if (unnecessary) {
					CloudLogMonitorManager.unregisterCloudLogTask(monitorId);
				}
			}
			
			// エージェントの起動時にクラウドログ監視設定が存在しない場合、
			// 不要な一時ファイルが存在しないかチェックし、削除
			if (force && list.isEmpty()){
				// 一時ファイルの削除
				CloudLogMonitorUtil.deleteGarbageFiles();
				// リーディングステータスの削除
				CloudLogfileMonitorManager.getInstance().clearReadingStatus();
			} else if (force) {
				// エージェント停止中にクラウドログ監視設定が無効化、削除された可能性を考慮し、
				// 不要な一時ファイルとステータスファイルを削除する
				File rootDir = new File(CloudLogMonitorUtil.getFileStorePathRoot());
				File propRootDir = new File(CloudLogMonitorUtil.getPropFileStorePathRoot());
				File[] rootDirs = { rootDir, propRootDir };

				for (File dir : rootDirs) {
					File[] monDirs = dir.listFiles();
					if (monDirs != null) {
						for (File monDir : monDirs) {
							// フォルダ名(監視設定ID)がマネージャから送られてきた実行対象にいなかった場合、
							// 不要なフォルダとして削除
							if (!CloudLogMonitorManager.getAllCloudLogIds().contains(monDir.getName())) {
								m_log.info("reloadCloudLogMonitor(): remove unnecessary file dir. Path: " + monDir.getAbsolutePath());
								try {
									CloudLogMonitorUtil.deleteDirectoryRecursive(monDir);
								} catch (IOException e) {
									m_log.warn("reloadCloudLogMonitor(): failed to remove unnecessary file dir.", e);
								}
							} 
						}
					}
				}
			}

		} catch (InvalidSetting | HinemosUnknown e) {
			m_log.error("reloadCloudLogMonitor: ", e);
		} catch (InvalidRole | InvalidUserPass | MonitorNotFound e) {
			m_log.warn("reloadCloudLogfileMonitor: ", e);
		}
	}
	
	/**
	 * バイナリ監視読込処理.
	 */
	private void reloadBinaryMonitor(SettingUpdateInfoResponse updateInfo, boolean force) {
		// バイナリ監視読込判定.
		if (!isBinaryMonitorReload(updateInfo) && !force) {
			return;
		}
		m_log.info("reloading configuration of binary monitoring...");
		try {
			// 監視ジョブ以外(監視設定一覧から設定した監視).
			GetMonitorForAgentResponse resRegularMonitors = AgentBinaryRestClientWrapper.getMonitorBinary(Agent.getAgentInfoRequest());
			List<AgtMonitorInfoResponse> list = resRegularMonitors.getList();
			for (AgtMonitorInfoResponse info : list) {
				String monitorTypeId = info.getMonitorTypeId();
				if (HinemosModuleConstant.MONITOR_BINARYFILE_BIN.equals(monitorTypeId)) {
					// バイナリファイル監視の場合.
					m_log.info("binaryfile: " + "monitorId=" + info.getMonitorId() + ", collectType="
							+ info.getBinaryCheckInfo().getCollectType() + ", directory="
							+ info.getBinaryCheckInfo().getDirectory() + ", filename="
							+ info.getBinaryCheckInfo().getFileName() + ", monitorFlg=" + info.getMonitorFlg()
							+ ", collectorFlg=" + info.getCollectorFlg());
				} else if (HinemosModuleConstant.MONITOR_PCAP_BIN.equals(monitorTypeId)) {
					// パケットキャプチャ監視の場合.
					m_log.info("packetcapture: " + "monitorId=" + info.getMonitorId() + ", filter="
							+ info.getPacketCheckInfo().getFilterStr() + ", promiscuousMode="
							+ info.getPacketCheckInfo().getPromiscuousMode() + ", monitorFlg=" + info.getMonitorFlg()
							+ ", collectorFlg=" + info.getCollectorFlg());
				} else {
					// 想定外の監視種別ID.
					m_log.warn("invalid type id of binary monitor: " + "monitorTypeId=" + info.getMonitorTypeId()
							+ ", monitorId=" + info.getMonitorId() + ", monitorFlg=" + info.getMonitorFlg()
							+ ", collectorFlg=" + info.getCollectorFlg());
					continue;
				}
			}

			// 監視ジョブ
			Map<AgtRunInstructionInfoResponse, AgtMonitorInfoResponse> monJobMap = new HashMap<>();

			// バイナリファイル監視ジョブ
			GetMonitorJobMapRequest req = new GetMonitorJobMapRequest();
			req.setAgentInfo(Agent.getAgentInfoRequest());
			req.setMonitorTypeId(HinemosModuleConstant.MONITOR_BINARYFILE_BIN);
			GetMonitorJobMapResponse resJobMonitors = AgentRestClientWrapper.getMonitorJobMap(req);
			for (int i = 0; i < resJobMonitors.getMonitorInfoList().size(); ++i) {
				AgtMonitorInfoResponse mon = resJobMonitors.getMonitorInfoList().get(i);
				AgtRunInstructionInfoResponse job = resJobMonitors.getRunInstructionInfoList().get(i);
				m_log.info("binaryfile: "
						+ "directory=" + mon.getBinaryCheckInfo().getDirectory()
						+ ", filename=" + mon.getBinaryCheckInfo().getFileName()
						+ ", collectType=" + mon.getBinaryCheckInfo().getCollectType()
						+ ", sessionId=" + job.getSessionId()
						+ ", jobunitId=" + job.getJobunitId()
						+ ", jobId=" + job.getJobId()
						+ ", facilityId=" + job.getFacilityId()
						+ ", monitorId=" + mon.getMonitorId()
						+ ", monitorFlg=" + mon.getMonitorFlg());
				monJobMap.put(job, mon);
			}

			// パケットキャプチャ監視ジョブ.
			req = new GetMonitorJobMapRequest();
			req.setAgentInfo(Agent.getAgentInfoRequest());
			req.setMonitorTypeId(HinemosModuleConstant.MONITOR_PCAP_BIN);
			resJobMonitors = AgentRestClientWrapper.getMonitorJobMap(req);
			for (int i = 0; i < resJobMonitors.getMonitorInfoList().size(); ++i) {
				AgtMonitorInfoResponse mon = resJobMonitors.getMonitorInfoList().get(i);
				AgtRunInstructionInfoResponse job = resJobMonitors.getRunInstructionInfoList().get(i);
				m_log.info("packetcapture: "
						+ "filter=" + mon.getPacketCheckInfo().getFilterStr()
						+ ", promiscuousMode=" + mon.getPacketCheckInfo().getPromiscuousMode()
						+ ", sessionId=" + job.getSessionId()
						+ ", jobunitId=" + job.getJobunitId()
						+ ", jobId=" + job.getJobId()
						+ ", facilityId=" + job.getFacilityId()
						+ ", monitorId=" + mon.getMonitorId()
						+ ", monitorFlg=" + mon.getMonitorFlg());
				monJobMap.put(job, mon);
			}

			BinaryMonitorManager.pushMonitorInfoList(list, monJobMap);

		} catch (HinemosUnknown e) {
			m_log.error(e, e);
		} catch (InvalidRole | InvalidUserPass | InvalidSetting | MonitorNotFound | RestConnectFailed e) {
			m_log.warn("realoadLogfileMonitor: " + e.getMessage());
		}
	}

	private void reloadWinEventMonitor (SettingUpdateInfoResponse updateInfo, boolean force) {
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
			GetMonitorForAgentResponse resp = AgentRestClientWrapper.getMonitorWinEvent(Agent.getAgentInfoRequest());
			List<AgtMonitorInfoResponse> list = resp.getList();
			m_log.debug("windows event monitoring list size : " + list.size());
			
			WinEventMonitorManager.getWinEventMonitorMap().clear();
			ArrayList<String> necessaryBookmarkFileList = new ArrayList<String>();
			
			for (AgtMonitorInfoResponse info : list) {
				m_log.info("winevent: critical=" + info.getWinEventCheckInfo().getLevelCritical() +
						", warning=" + info.getWinEventCheckInfo().getLevelWarning() +
						", verbose=" + info.getWinEventCheckInfo().getLevelVerbose() +
						", error=" + info.getWinEventCheckInfo().getLevelError() +
						", informational=" + info.getWinEventCheckInfo().getLevelInformational() +
						", monitorId=" + info.getMonitorId() +
						", monitorFlg=" + info.getMonitorFlg());
				WinEventMonitor winEventMonitor = new WinEventMonitor(info, null);
				WinEventMonitorManager.getWinEventMonitorMap().put(info.getMonitorId(), winEventMonitor);
				
				// register needed bookmark file
				for (String logName : info.getWinEventCheckInfo().getLogName()) {
					necessaryBookmarkFileList.add(WinEventMonitor.PREFIX + info.getMonitorId() + "-" + WinEventMonitor.logNameReplaceCharacter(logName) + WinEventMonitor.POSTFIX_BOOKMARK + ".xml");
				}
			}

			// 監視ジョブ
			Map<AgtRunInstructionInfoResponse, AgtMonitorInfoResponse> rtnMap = new HashMap<>();

			GetMonitorJobMapRequest jobMonReq = new GetMonitorJobMapRequest();
			jobMonReq.setAgentInfo(Agent.getAgentInfoRequest());
			jobMonReq.setMonitorTypeId(HinemosModuleConstant.MONITOR_WINEVENT);
			GetMonitorJobMapResponse jobMonRsp = AgentRestClientWrapper.getMonitorJobMap(jobMonReq);

			for (int i = 0; i < jobMonRsp.getMonitorInfoList().size(); ++i) {
				AgtMonitorInfoResponse mon = jobMonRsp.getMonitorInfoList().get(i);
				AgtRunInstructionInfoResponse run = jobMonRsp.getRunInstructionInfoList().get(i);

				m_log.info("winevent: "
						+ "critical=" + mon.getWinEventCheckInfo().getLevelCritical()
						+ ", warning=" + mon.getWinEventCheckInfo().getLevelWarning()
						+ ", verbose=" + mon.getWinEventCheckInfo().getLevelVerbose()
						+ ", error=" + mon.getWinEventCheckInfo().getLevelError()
						+ ", informational=" + mon.getWinEventCheckInfo().getLevelInformational()
						+ ", monitorId=" + mon.getMonitorId()
						+ ", monitorFlg=" + mon.getMonitorFlg());

				WinEventMonitor winEventMonitor = new WinEventMonitor(mon, run);

				String mapKey = run.getSessionId()
						+ run.getJobunitId()
						+ run.getJobId()
						+ run.getFacilityId()
						+ mon.getMonitorId();

				WinEventMonitorManager.getWinEventMonitorMap().put(mapKey, winEventMonitor);
				rtnMap.put(run, mon);

				// register needed bookmark file
				for (String logName : mon.getWinEventCheckInfo().getLogName()) {
					necessaryBookmarkFileList.add(WinEventMonitor.PREFIX + mapKey + "-" + WinEventMonitor.logNameReplaceCharacter(logName) + WinEventMonitor.POSTFIX_BOOKMARK + ".xml");
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
		} catch (UnsatisfiedLinkError | NoClassDefFoundError e){
			m_log.error("reloadWinEventMonitor:" + e.getClass().getCanonicalName() + ", " +
				e.getMessage(), e);
			WinEventMonitorManager.sendMessage(PriorityConstant.TYPE_CRITICAL,
				MessageConstant.MESSAGE_WINEVENT_STOP_MONITOR_FAILED_TO_GET_WINDOWS_EVENTLOG.getMessage(),
				"Failed to exec reloadWinEventMonitor." + e.getClass().getCanonicalName() + ", "+ e.getMessage(), HinemosModuleConstant.SYSYTEM, null);
		} catch (HinemosUnknown | MonitorNotFound | InvalidRole | InvalidUserPass | RestConnectFailed | InvalidSetting e) {
			m_log.error("reloadWinEventMonitor: Failed to reload.", e);
			WinEventMonitorManager.sendMessage(
					PriorityConstant.TYPE_CRITICAL,
					MessageConstant.MESSAGE_WINEVENT_STOP_MONITOR_FAILED_TO_GET_WINDOWS_EVENTLOG.getMessage(),
					"Failed to exec reloadWinEventMonitor." + e.getClass().getCanonicalName() + ", " + e.getMessage(),
					HinemosModuleConstant.SYSYTEM,
					null);
		}
	}

	private void reloadJobFileCheck(SettingUpdateInfoResponse updateInfo, boolean force) {
		if (!isJobFileCheckReload(updateInfo) && !force) {
			return;
		}
		m_log.info("reloading configuration of filecheck monitoring...");
		try {
			GetFileCheckResponse res = AgentRestClientWrapper.getFileCheck(Agent.getAgentInfoRequest());
			List<AgtJobFileCheckResponse> list = res.getList();

			for (AgtJobFileCheckResponse info : list) {
				m_log.info("filecheck: "
						+ "directory=" + info.getDirectory()
						+ ", id=" + info.getId()
						+ ", valid=" + info.getValid());
			}
			FileCheckManager.setFileCheck(list);
		} catch (HinemosUnknown e) {
			m_log.error(e, e);
		} catch (InvalidRole | InvalidUserPass | InvalidSetting | JobMasterNotFound | RestConnectFailed e) {
			m_log.warn("reloadJobFileCheck: " + e.getMessage());
		}
	}

	private void runJob(AgtRunInstructionInfoResponse info) {
		m_log.debug("onMessage SessionID=" + info.getSessionId()
				+ ", JobID=" + info.getJobId()
				+ ", CommandType=" + info.getCommandType());

		m_log.debug("onMessage CommandType != CHECK");

		//実行履歴チェック
		try{
			RunHistoryUtil.RunHistoryWrapper runHistory = RunHistoryUtil.findRunHistory(info);
			if (runHistory == null) {
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
				} else if (info.getCommand().equals(CommandConstant.FILE_CHECK)) {
					// ファイルチェック用スレッド起動
					FileCheckJobThread thread = new FileCheckJobThread(info, m_sendQueue);
					thread.start();
				}else if(info.getCommand().equals(CommandConstant.RPA)){
					if (info.getCommandType() == CommandTypeConstant.NORMAL) {
						//RPAシナリオジョブのスレッドを実行
						m_log.debug("onMessage CommandType = RPA");
						RpaScenarioThread thread = new RpaScenarioThread(info, m_sendQueue);
						thread.start();
					} else if (info.getCommandType() == CommandTypeConstant.SCREENSHOT) {
						//RPAシナリオスクリーンショットの取得
						m_log.debug("onMessage CommandType = SCREENSHOT");
						List<String> users = RpaWindowsUtil.getActiveUsers();
						if (users != null && users.size() > 0) {
							// 通常、ユーザは1件なので、get(0)を使用する
							ScreenshotThread thread = new ScreenshotThread(info, m_sendQueue, users.get(0));
							thread.start();
						}
					} else {
						//既に終了したジョブに対しての停止コマンドの場合等にここを通る
						m_log.warn("runJob() : rpa job command type is invalid, commandType=" + info.getCommandType());
					}
				}else if(info.getCommandType() == CommandTypeConstant.NORMAL ||
						(info.getCommandType() == CommandTypeConstant.STOP && info.getStopType() == CommandStopTypeConstant.EXECUTE_COMMAND)){
					//コマンド実行
					CommandThread thread = new CommandThread(info, m_sendQueue);
					thread.start();
				} else	if (info.getCommandType() == CommandTypeConstant.STOP
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
						&& info.getCommand().equals(CommandConstant.RPA)) {
					//停止コマンドの場合
					RpaScenarioThread.terminate(info);
				} else if (info.getCommandType() == CommandTypeConstant.STOP
						&& info.getStopType() == CommandStopTypeConstant.DESTROY_PROCESS) {
					if (runHistory.getProcess() != null) {
						// プロセス終了
						DeleteProcessThread thread = new DeleteProcessThread(info, m_sendQueue);
						thread.start();
					} else if (runHistory.getThread() != null) {
						// スレッド停止
						AgentThread thread = runHistory.getThread();
						if (thread instanceof FileCheckJobThread) {
							((FileCheckJobThread) thread).terminate(info);
						} else {
							// 到達しない
							m_log.warn("runJob() : logical error, AgentThread is undefined instance.");
						}
					}
				} else {
					// タイミングによりジョブ実行命令を2つ受信した場合にここに入る
					m_log.info("runJob() : avoid duplicate running");
				}
			}
		} catch(Throwable e) {
			m_log.warn("runJob() : " + e.getMessage(), e);
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
