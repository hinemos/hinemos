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

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;

import com.clustercontrol.HinemosManagerMain;
import com.clustercontrol.snmptrap.bean.SnmpTrap;
import com.clustercontrol.snmptrap.util.SnmpTrapNotifier;

/**
 * SNMP Trap 監視のメインクラス。
 * snmp4J のセッションを開始し、受信したトラップをタスクとして並行処理する。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class SnmpTrapMonitorService {
	private Logger logger = Logger.getLogger(this.getClass());

	private SnmpTrapNotifier notifier = new SnmpTrapNotifier();

	private SnmpTrapSession session;
	private ThreadPoolExecutor executor;
	private Charset defaultCharset;

	private TrapProcCounter counter = new TrapProcCounter();

	public void start() {
		if (session == null || executor == null)
			throw new IllegalStateException("Session and Executer must be set before calling open method.");

		if (! HinemosManagerMain._isClustered) {
			session.open();
		}
	}

	public void shutdown() {
		if (! HinemosManagerMain._isClustered) {
			session.close();
		}
	}

	public SnmpTrapMonitorService setSession(SnmpTrapSession session) {
		this.session = session;
		session.registReceiver(new SnmpTrapReceiver() {
			@Override
			public void onReceived(List<SnmpTrap> receivedTrapList) {
				for (int i = 0; i < receivedTrapList.size(); i++) {
					counter.countupReceived();
				}
				executor.execute(new ReceivedTrapFilterTask(receivedTrapList, notifier, counter, defaultCharset));
			}
		});
		return this;
	}

	public SnmpTrapMonitorService setExecutor(ThreadPoolExecutor executor) {
		this.executor = executor;
		final RejectedExecutionHandler handler = executor.getRejectedExecutionHandler();
		executor.setRejectedExecutionHandler(
			new RejectedExecutionHandler() {
				@Override
				public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
					if (handler != null)
						handler.rejectedExecution(r, executor);

					if (r instanceof ReceivedTrapFilterTask) {
						counter.countupDiscarded();
						logger.warn("too many snmptrap. snmptrap discarded : " + r);
					}
				}
			});
		return this;
	}

	public void snmptrapReceivedSync(List<SnmpTrap> receivedTrapList) {
		for (int i = 0; i < receivedTrapList.size(); i++) {
			counter.countupReceived();
		}
		new ReceivedTrapFilterTask(receivedTrapList, notifier, counter, defaultCharset).run();
	}

	public long getReceivedCount() {
		return counter.getReceivedCount();
	}

	public long getNotifiedCount() {
		return counter.getNotifiedCount();
	}

	public long getDiscardedCount() {
		return counter.getDiscardedCount();
	}

	public int getQueuedCount() {
		return executor.getQueue().size();
	}

	public SnmpTrapMonitorService setDefaultCharset(Charset defaultCharset) {
		this.defaultCharset = defaultCharset;
		return this;
	}
}