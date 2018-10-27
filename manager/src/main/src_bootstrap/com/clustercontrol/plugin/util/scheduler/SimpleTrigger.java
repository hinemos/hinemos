/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.util.scheduler;


public class SimpleTrigger extends AbstractTrigger {
	private long period;

	public long getPeriod() {
		return period;
	}
	public void setPeriod(long period) {
		this.period = period;
	}

	@Override
	public long computeFirstFireTime(long currentTimeMillis) {
		if (getEndTime() >= 0 && getEndTime() <= currentTimeMillis) {
			setPreviousFireTime(getNextFireTime());
			setNextFireTime(-1);
			return -1;
		} else {
			long nextFireTime = currentTimeMillis;
			if (getStartTime() >= 0) {
				nextFireTime = getStartTime();
			}
			setNextFireTime(nextFireTime);
			return nextFireTime;
		}
	}

	@Override
	public void triggered(long currentTimeMillis) {
		if (getEndTime() >= 0 && getEndTime() <= currentTimeMillis) {
			setPreviousFireTime(getNextFireTime());
			setNextFireTime(-1);
		} else {
			setPreviousFireTime(getNextFireTime());
			long nextFireTime = getNextFireTime() + getPeriod();
			while (nextFireTime <= currentTimeMillis) {
				nextFireTime += getPeriod();
			}
			
			setNextFireTime(nextFireTime);
		}
	}

	@Override
	public void updateAfterMisfire(long currentTimeMillis) {
		if (getEndTime() >= 0 && getEndTime() <= currentTimeMillis) {
			setPreviousFireTime(getNextFireTime());
			setNextFireTime(-1);
		} else {
			if (getMisfireInstruction() == Trigger.MISFIRE_INSTRUCTION_DO_NOTHING) {
				setPreviousFireTime(getNextFireTime());
				setNextFireTime(currentTimeMillis + getPeriod());
			} else if (getMisfireInstruction() == Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY) {
			} else {
				throw new InternalError();
			}
		}
	}
}