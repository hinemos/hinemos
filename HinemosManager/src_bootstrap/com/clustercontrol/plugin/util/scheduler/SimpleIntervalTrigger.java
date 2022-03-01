/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.util.scheduler;

public class SimpleIntervalTrigger extends AbstractTrigger {

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
			setNextFireTime(currentTimeMillis + getPeriod());
		}
	}

	@Override
	public void updateAfterMisfire(long currentTimeMillis) {
		if (getEndTime() >= 0 && getEndTime() <= currentTimeMillis) {
			setPreviousFireTime(getNextFireTime());
			setNextFireTime(-1);
		} else {
			if (getMisfireInstruction() == Trigger.MISFIRE_INSTRUCTION_DO_NOTHING) {
				if (getNextFireTime() == 0L
						&& getPreviousFireTime() <= 0L) {
					// スケジュール開始日時
					// 現在時刻を過ぎていたら、翌日にする
					if (getStartTime() < currentTimeMillis) {
						setStartTime(getStartTime() + (24 * 60 * 60 * 1000));
					}
					setNextFireTime(getStartTime());
				} else {
					setPreviousFireTime(getNextFireTime());
					setNextFireTime(currentTimeMillis + getPeriod());
				}
			} else if (getMisfireInstruction() == Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY) {
			} else {
				throw new InternalError();
			}
		}
	}
}
