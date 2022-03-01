/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.selfcheck.monitor;

import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.jobmanagement.queue.JobQueue;
import com.clustercontrol.jobmanagement.queue.JobQueueContainer;
import com.clustercontrol.util.InternalIdAbstract;
import com.clustercontrol.util.Singletons;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * ジョブキュー用のセルフチェック実行クラスです。
 */
public class JobQueueMonitor extends SelfCheckMonitorBase {
	private static final Log log = LogFactory.getLog(JobQueueMonitor.class);

	private static final String MONITOR_ID = "SYS_JOBQUEUE";
	private static final String SUBKEY_SIZE = "size";
	private static final String SUBKEY_DEADLOCK = "deadlock";

	// 外部依存動作をモックへ置換できるように分離
	private External external;
	static class External {
		boolean checksSize() {
			return HinemosPropertyCommon.selfcheck_monitoring_jobqueue_size.getBooleanValue();
		}

		int getSizeThreshold() {
			return HinemosPropertyCommon.selfcheck_monitoring_jobqueue_size_threshold.getIntegerValue();
		}

		boolean checksDeadlock() {
			return HinemosPropertyCommon.selfcheck_monitoring_jobqueue_deadlock.getBooleanValue();
		}

		int getDeadlockThreshold() {
			return HinemosPropertyCommon.selfcheck_monitoring_jobqueue_deadlock_threshold.getIntegerValue();
		}
		
		// isNotifyがDB依存のHinemosPropertyCommonを使っているため、わざわざラップする必要がある。
		boolean shouldNotify(SelfCheckMonitorBase monitor, String subKey, boolean warnFlag) {
			return monitor.isNotify(subKey, warnFlag);
		}
		
		void putInternalEvent(InternalIdAbstract internalId, String[] messageArgs) {
			AplLogger.put(internalId, messageArgs);
		}
	}

	/**
	 * コンストラクタです。
	 */
	public JobQueueMonitor() {
		this(new External());
	}

	JobQueueMonitor(External external) {
		this.external = external;
	}

	// これどこで使っているんだろう
	@Override
	public String toString() {
		return "monitoring job queue";
	}

	@Override
	public String getMonitorId() {
		return MONITOR_ID;
	}

	@Override
	public void execute() {
		log.debug("execute: Start.");

		boolean checksSize = external.checksSize();
		boolean checksDeadlock = external.checksDeadlock();

		if (!checksSize && !checksDeadlock) {
			log.debug("execute: Nothing to do.");
			return;
		}

		Singletons.get(JobQueueContainer.class).stream().forEach(queue -> {
			if (checksSize) checkSize(queue);
			if (checksDeadlock) checkDeadlock(queue);
		});
	}

	// キューサイズ チェック
	private void checkSize(JobQueue queue) {
		long size = queue.getSize();
		int limit = external.getSizeThreshold();
		boolean harmful = size > limit;

		String logMsg = "checkSize: queueId=" + queue.getId() + ", size=" + size + ", limit=" + limit;
		if (harmful) {
			log.info(logMsg);
		} else {
			log.debug(logMsg);
		}

		if (!external.shouldNotify(this, SUBKEY_SIZE + "[" + queue.getId() + "]", harmful)) return;

		external.putInternalEvent(InternalIdCommon.SYS_SFC_SYS_024,
				new String[] { queue.getId(), String.valueOf(limit) });
	}

	// デッドロック チェック
	private void checkDeadlock(JobQueue queue) {
		long freezingTime = queue.getFreezingTime();
		int interval = external.getDeadlockThreshold();
		boolean harmful = freezingTime > TimeUnit.SECONDS.toMillis(interval);

		String logMsg = "checkDeadlock: queueId=" + queue.getId() + ", freezing=" + freezingTime + ", interval="
				+ interval;
		if (harmful) {
			log.info(logMsg);
		} else {
			log.debug(logMsg);
		}

		if (!external.shouldNotify(this, SUBKEY_DEADLOCK + "[" + queue.getId() + "]", harmful)) return;

		external.putInternalEvent(InternalIdCommon.SYS_SFC_SYS_025,
				new String[] { queue.getId(), String.valueOf(interval) });
	}
}
