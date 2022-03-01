/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.cloud.log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtLogfileCheckInfoResponse;
import org.openapitools.client.model.AgtMonitorInfoResponse;
import org.openapitools.client.model.AgtMonitorPluginStringInfoResponse;

import com.clustercontrol.logfile.bean.LogfileLineSeparatorConstant;
import com.clustercontrol.xcloud.bean.CloudConstant;

/**
 * クラウドログ監視の実行を管理するクラス
 */
public class CloudLogMonitorManager {

	private static Log log = LogFactory.getLog(CloudLogMonitorManager.class);

	private static final Map<String, CloudLogMonitor> tasks;

	private static final Object lockKey = new Object();

	static {
		tasks = new ConcurrentHashMap<String, CloudLogMonitor>();
	}

	/**
	 * register new Collector Task
	 * 
	 * @param newTask
	 *            new Collector Task
	 */
	public static void registerCloudLogTask(CloudLogMonitor newTask) {

		synchronized (lockKey) {
			if (tasks.containsKey(newTask.getConfig().getMonitorId())) {
				log.info("already collector running. (" + newTask.getConfig().getMonitorId() + ")");
				CloudLogMonitor oldTask = tasks.get(newTask.getConfig().getMonitorId());
				oldTask.update(newTask);
			} else {
				tasks.put(newTask.getConfig().getMonitorId(), newTask);
				log.info("starting new collector. (" + newTask.getConfig().getMonitorId() + ")");
				newTask.start();
			}
		}

	}

	/**
	 * unregister Collector Task
	 * 
	 * @param collectorId
	 *            Collector Id
	 */
	public static void unregisterCloudLogTask(String collectorId) {

		synchronized (lockKey) {
			if (tasks.containsKey(collectorId)) {
				log.info("stopping a collector. (" + collectorId + ")");
				tasks.get(collectorId).shutdown();
				tasks.remove(collectorId);
			} else {
				log.warn("collector is not running. (" + collectorId + ")");
			}
		}

	}
	
	/**
	 * クラウドログ監視のスレッドをすべて停止します。
	 */
	public static void shutdownAllCloudLogTask() {

		synchronized (lockKey) {
			boolean hasTimeout = false;
			for (Entry<String, CloudLogMonitor> task : tasks.entrySet()) {
				hasTimeout = task.getValue().shutdownWorkers();
				tasks.remove(task.getKey());
				// どれか一つのワーカーでタイムアウトが起こっていたら、
				// 残りのワーカーの終了は待たない
				if (hasTimeout) {
					break;
				}
			}
			if (hasTimeout) {
				log.info("shutdownAllCloudLogTask(): Timeout occured. Some workers may have remained task.");
			} else {
				log.info("shutdownAllCloudLogTask(): all workers have been terminated.");
			}
		}
	}
	
	/**
	 * get all Collector Id List
	 * 
	 * @return id list
	 */
	public static List<String> getAllCloudLogIds() {

		synchronized (lockKey) {
			List<String> cloudLogIds = new ArrayList<String>();

			for (String id : tasks.keySet()) {
				cloudLogIds.add(id);
			}
			return cloudLogIds;
		}

	}

	public static void setLogFileInfo(AgtMonitorInfoResponse current) {

		AgtLogfileCheckInfoResponse config = new AgtLogfileCheckInfoResponse();
		String tmpVal = "";

		// ファイル監視で読み取る際の一時ファイルのリターンコードはLFで固定
		config.setFileReturnCode(LogfileLineSeparatorConstant.LF);

		for (AgtMonitorPluginStringInfoResponse sInfo : current.getPluginCheckInfo().getMonitorPluginStringInfoList()) {
			if (sInfo.getKey().equals(CloudConstant.cloudLog_patternHead)) {
				config.setPatternHead(sInfo.getValue());
				continue;
			}
			if (sInfo.getKey().equals(CloudConstant.cloudLog_patternTail)) {
				config.setPatternTail(sInfo.getValue());
				continue;
			}
			if (sInfo.getKey().equals(CloudConstant.cloudLog_maxBytes)) {
				if (sInfo.getValue() != null) {
					tmpVal = sInfo.getValue();
					int maxBytes = 0;
					try {
						maxBytes = Integer.parseInt(tmpVal);
					} catch (Exception e) {
						log.warn("setLogFileInfo(): failed parse max bytes. use 0");
					}

					config.setMaxBytes(maxBytes);
				} else {
					config.setMaxBytes(null);
				}

				continue;
			}
		}
		current.setLogfileCheckInfo(config);

	}

}
