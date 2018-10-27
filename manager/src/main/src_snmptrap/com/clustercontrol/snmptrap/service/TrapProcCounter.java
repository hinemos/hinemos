/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.snmptrap.service;

import org.apache.log4j.Logger;

import com.clustercontrol.commons.util.HinemosPropertyCommon;

/**
 * SNMP Trap 監視で処理したトラップの数を管理するクラス。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class TrapProcCounter {
	private Logger logger = Logger.getLogger(this.getClass());

	private long receivedCount = 0;
	private long discardedCount = 0;
	private long notifiedCount = 0;

	public long getReceivedCount() {
		return receivedCount;
	}

	public long getDiscardedCount() {
		return discardedCount;
	}

	public long getNotifiedCount() {
		return notifiedCount;
	}

	public synchronized void countupReceived() {
		receivedCount = receivedCount >= Long.MAX_VALUE ? 0 : receivedCount + 1;
		int _statsInterval = HinemosPropertyCommon.monitor_snmptrap_stats_interval.getIntegerValue();
		if (receivedCount % _statsInterval == 0) {
			logger.info("The number of snmptrap (received) : " + receivedCount);
		}
	}

	public synchronized void countupDiscarded() {
		discardedCount = discardedCount >= Long.MAX_VALUE ? 0 : discardedCount + 1;
		int _statsInterval = HinemosPropertyCommon.monitor_snmptrap_stats_interval.getIntegerValue();
		if (discardedCount % _statsInterval == 0) {
			logger.info("The number of snmptrap (discarded) : " + discardedCount);
		}
	}

	public synchronized void countupNotified() {
		notifiedCount = notifiedCount >= Long.MAX_VALUE ? 0 : notifiedCount + 1;
		int _statsInterval = HinemosPropertyCommon.monitor_snmptrap_stats_interval.getIntegerValue();
		if (notifiedCount % _statsInterval == 0) {
			logger.info("The number of snmptrap (notified) : " + notifiedCount);
		}
	}
}