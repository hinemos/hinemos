/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.util.scheduler;


public interface Trigger {
	public static final int MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY = 1;
	public static final int MISFIRE_INSTRUCTION_DO_NOTHING = 2;

	public TriggerKey getTriggerKey();

	public long getStartTime();

	public void setStartTime(long startTime);

	public long getEndTime();

	public void setEndTime(long endTime);

	public int getMisfireInstruction();

	public long getNextFireTime();
	
	public long getPreviousFireTime();

	public long computeFirstFireTime(long currentTimeMillis);

	public void triggered(long currentTimeMillis);

	public void updateAfterMisfire(long currentTimeMillis);
}