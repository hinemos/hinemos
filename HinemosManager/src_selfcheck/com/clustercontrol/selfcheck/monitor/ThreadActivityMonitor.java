/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.selfcheck.monitor;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.bean.ThreadInfo;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.InternalIdCommon;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.apllog.AplLogger;

/**
 * スレッドの活動状況を確認する処理の実装クラス
 */
public class ThreadActivityMonitor extends SelfCheckMonitorBase {

	private static Log log = LogFactory.getLog( ThreadActivityMonitor.class );

	public final ThreadInfo threadInfo;
	public int thresholdSec;

	public final String monitorId = "SYS_THREAD";
	public final String subKey;
	public final String application = "SELFCHECK (Thread)";

	/**
	 * コンストラクタ
	 * @param threadInfo スレッド情報
	 * @param thresholdSec 最大処理時間(この時間より長く実行されている場合、停滞スレッドと判定する)
	 */
	public ThreadActivityMonitor(ThreadInfo threadInfo) {
		this.threadInfo = threadInfo;

		this.subKey = threadInfo.thread.getName();
	}

	@Override
	public String toString() {
		return String.format("monitoring thread (threadInfo = %s, threshold = %s)", threadInfo, thresholdSec + ")");
	}

	@Override
	public String getMonitorId() {
		return monitorId;
	}

	@Override
	public void execute() {
		if (!HinemosPropertyCommon.selfcheck_monitoring_thread_activity.getBooleanValue()) {
			log.debug("skip");
			return;
		}

		/** ローカル変数 */
		Date now = null;
		String startTimeStr = null;
		boolean warn = true;

		thresholdSec = HinemosPropertyCommon.selfcheck_monitoring_thread_activity_threshold.getIntegerValue();

		/** メイン処理 */
		log.debug("monitoring thread. (threadInfo = " + threadInfo + ")");

		now = HinemosTime.getDateInstance();
		startTimeStr =  String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", new Date(threadInfo.taskStartTime));

		if (now.getTime() - threadInfo.taskStartTime <= thresholdSec * 1000) {
			log.debug("thread is active : " + threadInfo);

			warn = false;
		}

		if (warn) {
			log.warn("thread takes long time : " + threadInfo + ", " + "stack trace : " +
					getStackTrace(threadInfo.thread));
		}
		if (!isNotify(subKey, warn)) {
			return;
		}
		String[] msgAttr1 = { Long.toString(threadInfo.thread.getId()), threadInfo.thread.getName(), threadInfo.taskClassName, startTimeStr, Integer.toString(thresholdSec) };
		AplLogger.put(InternalIdCommon.SYS_SFC_SYS_012, msgAttr1,
				"internal logic (tid " +
						threadInfo.thread.getId() +
						", thread name " +
						threadInfo.thread.getName() +
						", class name " +
						threadInfo.taskClassName +
						", start time " +
						startTimeStr +
						") takes more than " +
						thresholdSec +
				" [sec].");

		return;
	}

	private static String getStackTrace(Thread t){
		StackTraceElement[] eList = t.getStackTrace();
		StringBuilder trace = new StringBuilder();
		for (StackTraceElement e : eList) {
			trace.append("\n\tat ");
			trace.append(e.getClassName() + "." + e.getMethodName() + "(" + e.getFileName() + ":" + e.getLineNumber() + ")");
		}
		return trace.toString();
	}

}
