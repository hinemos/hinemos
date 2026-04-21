/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.customtrap.service;

import org.apache.log4j.Logger;

import com.clustercontrol.commons.util.HinemosPropertyCommon;

/**
 * カスタムトラップ監視で処理したトラップの数を管理するクラス。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class TrapProcCounter {
	private Logger logger = Logger.getLogger(this.getClass());

	private long receivedCount = 0;
	private long discardedCount = 0;
	private long notifiedCount = 0;

	public synchronized long getReceivedCount() {
		return receivedCount;
	}

	public synchronized long getDiscardedCount() {
		return discardedCount;
	}

	public synchronized long getNotifiedCount() {
		return notifiedCount;
	}

	public synchronized void countupReceived() {
		receivedCount = receivedCount >= Long.MAX_VALUE ? 0 : receivedCount + 1;
		logCount("received" , notifiedCount);
	}

	public synchronized void countupDiscarded() {
		discardedCount = discardedCount >= Long.MAX_VALUE ? 0 : discardedCount + 1;
		logCount("discarded" , notifiedCount);
	}

	public synchronized void countupNotified() {
		notifiedCount = notifiedCount >= Long.MAX_VALUE ? 0 : notifiedCount + 1;
		logCount("notified" , notifiedCount);
	}

	private void logCount(String type, long count) {
		int _statsInterval = HinemosPropertyCommon.monitor_customtrap_stats_interval.getIntegerValue();
		if (count % _statsInterval == 0) {
			logger.info("The number of customtrap (" + type + ") : " + count);
		}
	}
}