/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.Agent;
import com.clustercontrol.agent.SendQueue;
import com.clustercontrol.agent.log.ReadingStatusRoot.ReadingStatus;
import com.clustercontrol.agent.log.ReadingStatusRoot.ReadingStatusDir;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.ws.agent.OutputBasicInfo;
import com.clustercontrol.ws.jobmanagement.RunInstructionInfo;
import com.clustercontrol.ws.monitor.LogfileCheckInfo;
import com.clustercontrol.ws.monitor.MonitorInfo;

/**
 * ログ転送スレッドを管理するクラス<BR>
 * 
 * 転送対象ログファイル情報を受け取り、ログ転送スレッドを制御します。
 * 
 */
public class LogfileMonitorManager {
	private static Log log = LogFactory.getLog(LogfileMonitorManager.class);

	/** ファイルパスとファイルの読み込み状態を保持しているマップ */
	private static Map<String, LogfileMonitor> logfileMonitorCache =
			new HashMap<String, LogfileMonitor>();
	
	/** Queue送信  */
	private static SendQueue sendQueue;

	/** ログファイル監視間隔 */
	private static int runInterval = 10000; // 10sec
	
	/** 読み込み状態 */
	private static ReadingStatusRoot statusRoot;
	
	/** ログファイル監視スレッド */
	private static LogfileThread thread;
	
	/** ログファイル監視設定リスト(監視ジョブ含む) */
	private static List<MonitorInfoWrapper> monitorList;
	
	/** 前回監視実行時のログファイル監視設定リスト(読込状態ディレクトリの削除用) */
	private static List<MonitorInfoWrapper> beforeMonitorList;
	
	
	private static boolean isRunning = false;
	
	/**
	 * コンストラクタ
	 * 
	 * @param ejbConnectionManager EJBコネクション管理
	 * @param sendQueue 監視管理Queue送信
	 * @param props ログ転送エージェントプロパティ
	 */
	public static void setSendQueue(SendQueue sendQueue) {
		LogfileMonitorManager.sendQueue = sendQueue;
	}

	static {
		String key1 = "monitor.logfile.filter.interval";
		try {
			String runIntervalStr = AgentProperties.getProperty(key1, Integer.toString(runInterval));
			runInterval = Integer.parseInt(runIntervalStr);
		} catch (Exception e) {
			log.warn("LogfileThread : " + e.getMessage());
		}
		log.info(key1 + "=" + runInterval);
	}

	/**
	 * 監視設定をスレッドに反映します。<BR>
	 * 
	 * @param list 転送対象ログファイル情報一覧
	 */
	public static synchronized void pushMonitorInfoList(
			List<MonitorInfo> monitorList,
			Map<RunInstructionInfo, MonitorInfo> monitorMap) {
		List<MonitorInfoWrapper> wrapperList = new ArrayList<>();
		
		String fileName = "";
		for (MonitorInfo info : monitorList) {
			LogfileCheckInfo check = info.getLogfileCheckInfo();
			fileName += "[" + check.getDirectory() + "," + check.getFileName() + "]";
			
			wrapperList.add(new MonitorInfoWrapper(info, null));
			
			// 監視設定の対象ディレクトリ存在チェック
			File directory = new File(check.getDirectory());
			log.debug("setLogfileMonitor() : directoryExistsMap put monitorId = " + check.getMonitorId() + 
					", directoryStr = " + check.getDirectory() +
					", exists = " + directory.isDirectory());
		}
		log.info("setLogfileMonitor() : m_monitorList=" + fileName);
		
		for (Map.Entry<RunInstructionInfo, MonitorInfo> entry : monitorMap.entrySet()) {
			LogfileCheckInfo check = entry.getValue().getLogfileCheckInfo();
			fileName += "[" + check.getDirectory() + "," + check.getFileName() + "]";
			
			wrapperList.add(new MonitorInfoWrapper(entry.getValue(), entry.getKey()));

			// 監視設定の対象ディレクトリ存在チェック
			File directory = new File(check.getDirectory());
			log.debug("setMonitorInfoListForMonitorJob() : directoryExistsMap put monitorId = " + check.getMonitorId() + 
					", directoryStr = " + check.getDirectory() +
					", exists = " + directory.isDirectory());
			
		}
		log.info("setLogfileMonitor() : m_monitorList=" + fileName);
		LogfileMonitorManager.monitorList = wrapperList;
	}

	/**
	 * 登録された監視設定を取り出す。<BR>
	 */
	private static synchronized List<MonitorInfoWrapper> popMonitorInfoList() {
		List<MonitorInfoWrapper> list = LogfileMonitorManager.monitorList;
		LogfileMonitorManager.monitorList = null;
		return list;
	}

	private static void refresh() {
		synchronized(LogfileMonitorManager.class) {
			log.debug("refresh() : start");

			List<MonitorInfoWrapper> monitorList = popMonitorInfoList();
			List<MonitorInfoWrapper> beforeMonitorList = LogfileMonitorManager.beforeMonitorList;
			
			// 新たに監視設定が登録されているなら、読み取るファイルおよび読み取り状態を更新する
			if (monitorList != null) {
				log.debug("refresh() : m_monitorList.size=" + monitorList.size());

				// ファイルの読み込み状態の復元
				if (statusRoot == null) {
					statusRoot = new ReadingStatusRoot(monitorList, getReadingStatesStorePath());
					log.debug("refresh() : ReadingStatusRoot is initialized.");
				} else {
					statusRoot.update(monitorList, beforeMonitorList);
					log.debug("refresh() : ReadingStatusRoot is updated.");
				}
				LogfileMonitorManager.beforeMonitorList = monitorList;
			} else {
				// 監視対象のファイルに対する更新
				if (statusRoot != null)
					statusRoot.update();
			}
			
			if (statusRoot == null) {
				log.debug("refresh() : ReadingStatusRoot is not initialized.");
				return;
			}
			
			log.debug("refresh() : monitoring files.");

			Set<String> newLogfileMonitorCacheKeySet = new HashSet<String>();
			for (ReadingStatusDir miDir: statusRoot.getReadingStatusDirList()) {
				MonitorInfoWrapper wrapper = miDir.getMonitorInfo();
				
				String monitorId = wrapper.monitorInfo.getMonitorId();
				String directoryPath = wrapper.monitorInfo.getLogfileCheckInfo().getDirectory();
				String fileNamePattern = wrapper.monitorInfo.getLogfileCheckInfo().getFileName();
				String fileEncoding = wrapper.monitorInfo.getLogfileCheckInfo().getFileEncoding();
				String fileReturnCode = wrapper.monitorInfo.getLogfileCheckInfo().getFileReturnCode();
				log.debug("refresh() : monitorId=" + monitorId +
						", directory=" + directoryPath +
						", filenamePattern=" + fileNamePattern + 
						", fileEncoding=" + fileEncoding +
						", fileReturnCode=" + fileReturnCode);
				
				for (ReadingStatus status : miDir.list()) {
					log.debug("refresh() : filePath=" + status.filePath.getPath());
					
					String cacheKey = monitorId + status.rsFilePath.getPath();
					LogfileMonitor logfileMonitor = logfileMonitorCache.get(cacheKey);
					if(logfileMonitor == null){
						// ファイル監視オブジェクトを生成。
						logfileMonitor = new LogfileMonitor(wrapper, status);
						logfileMonitorCache.put(cacheKey, logfileMonitor);
						log.debug("refresh() : LogfileMonitor is created.");
					} else {
						// getFilePath には正式なディレクトリおよびファイル名が記録されているがwrapper のファイル名については
						// 正規表現が入力されている可能性があるので、ファイルパスで前方一致を行う
						if (logfileMonitor.getFilePath().startsWith(directoryPath)) { 
							log.debug("refresh() : LogfileMonitor is being cached.");
						} else {
							// ディレクトリが一致していない場合は再作成
							// ファイル監視オブジェクトを生成。
							logfileMonitor = new LogfileMonitor(wrapper, status);
							logfileMonitorCache.put(cacheKey, logfileMonitor);
							log.debug("refresh() : LogfileMonitor is created. Because the directory has been changed.");
						}
					}
					
					logfileMonitor.setMonitor(wrapper);
					
					newLogfileMonitorCacheKeySet.add(cacheKey);
				}
			}

			/*
			 * もう監視対象じゃないログファイルのlogfileMonitorをクリーンしてから、削除
			 */
			Iterator<Entry<String, LogfileMonitor>> it = logfileMonitorCache.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, LogfileMonitor> entry = it.next();
				if (!newLogfileMonitorCacheKeySet.contains(entry.getKey())) {
					entry.getValue().clean();
					it.remove();
				}
			}
		}
	}

	public static synchronized void start() {
		if (thread != null) {
			log.info("start() : LogfileThread is already started.");
			return;
		}

		thread = new LogfileThread();
		thread.setName("LogFileMonitor");
		thread.start();
		
		log.info("start() : LogfileThread is started.");
	}

	public static synchronized void terminate() {
		if (thread == null) {
			log.info("terminate() : LogfileThread is not started.");
			return;
		}
		
		thread.terminate();
		thread = null;

		log.info("terminate() : LogfileThread is terminated.");
	}

	private static class LogfileThread extends Thread {
		
		private boolean loop = true;
		
		@Override
		public void run() {
			log.info("run LogfileThread");
			while (loop) {
				isRunning = true;
				long start = HinemosTime.currentTimeMillis();
				synchronized(LogfileMonitorManager.class) {
					try {
						refresh();
						for (String filePath : logfileMonitorCache.keySet()) {
							LogfileMonitor logfileMonitor = logfileMonitorCache.get(filePath);
							// terminateされた場合次の監視は実行しない
							if(loop) {
								logfileMonitor.run();
							} else {
								break;
							}
						}
					} catch (Exception e) {
						log.warn("LogfileThread : " + e.getClass().getCanonicalName() + ", " +
								e.getMessage(), e);
					} catch (Throwable e) {
						log.error("LogfileThread : " + e.getClass().getCanonicalName() + ", " +
								e.getMessage(), e);
					} finally {
						isRunning = false;
					}
				}
				
				try {
					log.debug(String.format("LogfileThread run() : elapsed=%d ms.", System.currentTimeMillis() - start));
					Thread.sleep(runInterval);
				} catch (InterruptedException e) {
					log.info("LogfileThread is Interrupted");
					break;
				}
			}
			isRunning = false;
			log.info("terminate LogfileThread");
		}
		
		public void terminate() {
			loop = false;
		}
	}

	/**
	 * 監視管理のJMSに情報を通知します。<BR>
	 * 
	 * @param priority 重要度
	 * @param app アプリケーション
	 * @param msg メッセージ
	 * @param msgOrg オリジナルメッセージ
	 */
	public static void sendMessage(String filePath, int priority, String app, String msg, String msgOrg, String monitorId, RunInstructionInfo runInstructionInfo) {
		// ログ出力情報
		OutputBasicInfo output = new OutputBasicInfo();
		output.setPluginId(HinemosModuleConstant.MONITOR_LOGFILE);
		output.setPriority(priority);
		output.setApplication(app);
		output.setMessage(msg);
		output.setMessageOrg(msgOrg);

		output.setGenerationDate(HinemosTime.getDateInstance().getTime());
		output.setMonitorId(monitorId);
		output.setFacilityId(""); // マネージャがセットする。
		output.setScopeText(""); // マネージャがセットする。
		output.setRunInstructionInfo(runInstructionInfo);

		sendQueue.put(output);
	}

	protected static int getRunInterval() {
		return runInterval;
	}
	
	public static String getReadingStatesStorePath() {
		String home = Agent.getAgentHome();
		String storepath = new File(new File(home), "readingstatus").getAbsolutePath();
		return storepath;
	}
	
	public static boolean isRunning() {
		return isRunning;
	}
}
