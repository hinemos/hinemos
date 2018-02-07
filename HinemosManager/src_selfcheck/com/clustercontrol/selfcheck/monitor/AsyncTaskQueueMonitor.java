/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.selfcheck.monitor;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.plugin.impl.AsyncWorkerPlugin;
import com.clustercontrol.selfcheck.AsyncTaskQueueConfig;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * 処理待ち非同期処理数を確認する処理の実装クラス
 */
public class AsyncTaskQueueMonitor extends SelfCheckMonitorBase {

	private static Log m_log = LogFactory.getLog( AsyncTaskQueueMonitor.class );

	public final String monitorId = "SYS_ASYNC_TASK";
	public final String application = "SELFCHECK (asynchronous task)";

	/**
	 * コンストラクタ
	 * @param validationQuery 動作確認クエリ
	 */
	public AsyncTaskQueueMonitor() {
	}

	/**
	 * 監視項目ID
	 */
	@Override
	public String getMonitorId() {
		return monitorId;
	}

	/**
	 * 処理待ち非同期処理数の確認メソッド
	 * @return 通知情報
	 */
	@Override
	public void execute() {
		if (!HinemosPropertyCommon.selfcheck_monitoring_asynctask_queue.getBooleanValue()) {
			m_log.debug("skip");
			return;
		}

		/** ローカル変数 */
		String asyncTaskRaw = HinemosPropertyCommon.selfcheck_monitoring_asynctask_queue_list.getStringValue();
		List<AsyncTaskQueueConfig> asyncTasks = new ArrayList<AsyncTaskQueueConfig>();
		for (String task : asyncTaskRaw.split(",")) {
			String[] pair = task.split(":");
			if (pair.length == 2) {
				asyncTasks.add(new AsyncTaskQueueConfig(pair[0], Integer.parseInt(pair[1])));
			}
		}
		List<AsyncTaskQueueConfig> asyncTaskList = Collections.unmodifiableList(asyncTasks);
		for (AsyncTaskQueueConfig config : asyncTaskList) {
			String worker = config.worker;
			int threshold = config.queueThreshold;

			int queueSize = 0;
			boolean warn = true;
			String subKey = worker;

			/** メイン処理 */
			try {
				queueSize = getTaskCount(worker);
			} catch (Exception e) {
				m_log.warn("access failure to async worker plugin. (worker = " + worker + ")");
			}
			if (queueSize <= threshold) {
				m_log.debug("asynchronous task queue is normal. (worker = " + worker + ", queueSize = " + queueSize + ")");
				warn = false;
			}

			if (warn) {
				m_log.info("asynchronous task queue is too large. (worker = " + worker + ", queueSize = " + queueSize + ")");
			}

			if (!isNotify(subKey, warn)) {
				return;
			}
			String[] msgAttr1 = { Integer.toString(queueSize), Integer.toString(threshold) };
			AplLogger.put(PriorityConstant.TYPE_WARNING, PLUGIN_ID, MessageConstant.MESSAGE_SYS_011_SYS_SFC, msgAttr1,
					"too many asynchronous task in Hinemos Manager. (queued task " +
							queueSize +
							" > threshold " +
							threshold +
					")");
		}

		return;
	}
	
	/**
	 * 非同期タスクの蓄積数を返す。<br/>
	 * @return 非同期タスクの蓄積数
	 * @throws HinemosUnknown
	 */
	public static int getTaskCount(String worker) throws HinemosUnknown {
		return AsyncWorkerPlugin.getTaskCount(worker);
	}

}
