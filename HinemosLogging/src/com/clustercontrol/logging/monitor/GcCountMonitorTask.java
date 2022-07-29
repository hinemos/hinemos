/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.logging.monitor;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.Callable;

import com.clustercontrol.log.internal.InternalLogManager;

public class GcCountMonitorTask implements Callable<List<GarbageCollectorMXBean>> {
	private static final InternalLogManager.Logger log = InternalLogManager.getLogger(GcCountMonitorTask.class);

	@Override
	public List<GarbageCollectorMXBean> call() {
		log.trace("call : start.");

		List<GarbageCollectorMXBean> rtn = ManagementFactory.getGarbageCollectorMXBeans();

		log.trace("call : end.");
		return rtn;
	}

}
