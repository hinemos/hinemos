package com.clustercontrol.plugin.util.scheduler;

import java.text.ParseException;

import com.clustercontrol.util.HinemosTime;

public class CronTriggerBuilder {
	private TriggerKey key;
	private long baseTime = HinemosTime.currentTimeMillis();
	private long startTime = baseTime;
	private long endTime = -1;
	private String cronExpression;
	private int misfireInstruction = Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY;

	private CronTriggerBuilder() {
	}

	public static CronTriggerBuilder newTrigger() {
		return new CronTriggerBuilder();
	}

	public CronTriggerBuilder withIdentity(String name) {
		key = new TriggerKey(name, null);
		return this;
	}  

	public CronTriggerBuilder withIdentity(String name, String group) {
		key = new TriggerKey(name, group);
		return this;
	}

	public CronTriggerBuilder withIdentity(TriggerKey triggerKey) {
		this.key = triggerKey;
		return this;
	}

	public CronTriggerBuilder startAt(long startTime) {
		this.startTime = startTime;
		return this;
	}
	public CronTriggerBuilder startNow() {
		this.startTime = HinemosTime.currentTimeMillis();
		return this;
	}

	public CronTriggerBuilder endAt(long endTime) {
		this.endTime = endTime;
		return this;
	}

	public CronTriggerBuilder cronSchedule(String cronExpression) {
		this.cronExpression = cronExpression;
		return this;
	}

	public CronTriggerBuilder withMisfireHandlingInstructionIgnoreMisfires() {
		misfireInstruction = Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY;
		return this;
	}

	public CronTriggerBuilder withMisfireHandlingInstructionDoNothing() {
		misfireInstruction = Trigger.MISFIRE_INSTRUCTION_DO_NOTHING;
		return this;
	}

	public Trigger build() {
		try {
			CronTrigger trig = new CronTrigger();
			trig.setTriggerKey(key);
			trig.setStartTime(startTime);
			trig.setEndTime(endTime);
			trig.setCronExpression(cronExpression);
			trig.setMisfireInstruction(misfireInstruction);
			return trig;
		} catch (ParseException e) {
			// all methods of construction ensure the expression is valid by
			// this point...
			throw new RuntimeException("CronExpression '" + cronExpression
					+ "' is invalid.", e);
		}
	}
}