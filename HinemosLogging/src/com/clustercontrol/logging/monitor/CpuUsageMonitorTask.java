/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.logging.monitor;

import java.lang.management.ManagementFactory;
import java.util.concurrent.Callable;

import com.clustercontrol.log.internal.InternalLogManager;
import com.sun.management.OperatingSystemMXBean;

public class CpuUsageMonitorTask implements Callable<Integer> {
	private static final InternalLogManager.Logger log = InternalLogManager.getLogger(CpuUsageMonitorTask.class);

	@Override
	public Integer call() {
		log.trace("call : start.");

		OperatingSystemMXBean osMx = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
		Double val = osMx.getProcessCpuLoad();
		Integer rtn = (int) ((val * 1000) / 10.0);

		log.trace("call : end.");
		return rtn;
	}

}
