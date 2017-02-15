/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.snmptrap.service;

import org.apache.log4j.Logger;

import com.clustercontrol.maintenance.util.HinemosPropertyUtil;

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
		int _statsInterval = HinemosPropertyUtil.getHinemosPropertyNum("monitor.snmptrap.stats.interval", Long.valueOf(100)).intValue();
		if (receivedCount % _statsInterval == 0) {
			logger.info("The number of snmptrap (received) : " + receivedCount);
		}
	}

	public synchronized void countupDiscarded() {
		discardedCount = discardedCount >= Long.MAX_VALUE ? 0 : discardedCount + 1;
		int _statsInterval = HinemosPropertyUtil.getHinemosPropertyNum("monitor.snmptrap.stats.interval", Long.valueOf(100)).intValue();
		if (discardedCount % _statsInterval == 0) {
			logger.info("The number of snmptrap (discarded) : " + discardedCount);
		}
	}

	public synchronized void countupNotified() {
		notifiedCount = notifiedCount >= Long.MAX_VALUE ? 0 : notifiedCount + 1;
		int _statsInterval = HinemosPropertyUtil.getHinemosPropertyNum("monitor.snmptrap.stats.interval", Long.valueOf(100)).intValue();
		if (notifiedCount % _statsInterval == 0) {
			logger.info("The number of snmptrap (notified) : " + notifiedCount);
		}
	}
}