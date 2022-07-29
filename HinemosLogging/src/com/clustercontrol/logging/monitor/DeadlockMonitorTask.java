/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.logging.monitor;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.clustercontrol.log.internal.InternalLogManager;

public class DeadlockMonitorTask implements Callable<List<ThreadInfo>> {
	private static final InternalLogManager.Logger log = InternalLogManager.getLogger(DeadlockMonitorTask.class);

	@Override
	public List<ThreadInfo> call() {
		log.trace("call : start.");
		List<ThreadInfo> list = new ArrayList<>();

		ThreadMXBean tMXBean = ManagementFactory.getThreadMXBean();
		long[] lockedThreadIds = tMXBean.findDeadlockedThreads();
		if (lockedThreadIds == null) {
			log.debug("call : not detected.");
			return null;
		}
		for (long id : lockedThreadIds) {
			list.add(tMXBean.getThreadInfo(id));
		}

		log.trace("call : end.");
		return list;
	}

}
