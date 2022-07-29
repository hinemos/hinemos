/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.logging.monitor;

import java.util.concurrent.Callable;

import com.clustercontrol.log.internal.InternalLogManager;

public class HeapRemainingMonitorTask implements Callable<Long> {
	private static final InternalLogManager.Logger log = InternalLogManager.getLogger(HeapRemainingMonitorTask.class);

	@Override
	public Long call() {
		log.trace("call : start.");

		Long rtn = (Runtime.getRuntime().freeMemory() / 1024 / 1024);

		log.trace("call : end.");
		return rtn;
	}

}
