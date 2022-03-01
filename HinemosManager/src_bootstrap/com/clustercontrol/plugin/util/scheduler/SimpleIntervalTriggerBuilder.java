/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.util.scheduler;

import com.clustercontrol.util.HinemosTime;

public class SimpleIntervalTriggerBuilder {

	private TriggerKey key;
	private long baseTime = HinemosTime.currentTimeMillis();
	private long startTimeMillis = baseTime;
	private long endTime = -1;
	private long period = -1;
	private int misfireInstruction = Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY;

	private SimpleIntervalTriggerBuilder() {
	}

	public static SimpleIntervalTriggerBuilder newTrigger() {
		return new SimpleIntervalTriggerBuilder();
	}

	public SimpleIntervalTriggerBuilder withIdentity(String name) {
		key = new TriggerKey(name, null);
		return this;
	}

	public SimpleIntervalTriggerBuilder withIdentity(String name, String group) {
		key = new TriggerKey(name, group);
		return this;
	}

	public SimpleIntervalTriggerBuilder withIdentity(TriggerKey triggerKey) {
		this.key = triggerKey;
		return this;
	}

	public SimpleIntervalTriggerBuilder startAt(long startTimeMillis) {
		this.startTimeMillis = startTimeMillis;
		return this;
	}

	public SimpleIntervalTriggerBuilder startNow() {
		this.startTimeMillis = HinemosTime.currentTimeMillis();
		return this;
	}

	public SimpleIntervalTriggerBuilder endAt(long endTime) {
		this.endTime = endTime;
		return this;
	}

	public SimpleIntervalTriggerBuilder setPeriod(long period) {
		this.period = period;
		return this;
	}

	public SimpleIntervalTriggerBuilder withMisfireHandlingInstructionIgnoreMisfires() {
		misfireInstruction = Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY;
		return this;
	}

	public SimpleIntervalTriggerBuilder withMisfireHandlingInstructionDoNothing() {
		misfireInstruction = Trigger.MISFIRE_INSTRUCTION_DO_NOTHING;
		return this;
	}

	public Trigger build() {
		SimpleIntervalTrigger trig = new SimpleIntervalTrigger();
		trig.setTriggerKey(key);
		trig.setStartTime(startTimeMillis);
		trig.setEndTime(endTime);
		trig.setPeriod(period);
		trig.setMisfireInstruction(misfireInstruction);
		return trig;
	}
}
