package com.clustercontrol.plugin.util.scheduler;

import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

import com.clustercontrol.util.HinemosTime;

public class CronTrigger extends AbstractTrigger {
	private CronExpression cronEx = null;
	private TimeZone timeZone;

	@Override
	public long computeFirstFireTime(long currentTimeMillis) {
		if (getEndTime() >= 0 && getEndTime() <= currentTimeMillis) {
			setPreviousFireTime(getNextFireTime());
			setNextFireTime(-1);
			return -1;
		} else {
			long startTime = getStartTime() >= 0 ? getStartTime(): currentTimeMillis;
			setNextFireTime(getFireTimeAfter(startTime));
			return getNextFireTime();
		}
	}

	@Override
	public void triggered(long currentTimeMillis) {
		if (getEndTime() >= 0 && getEndTime() <= currentTimeMillis) {
			setPreviousFireTime(getNextFireTime());
			setNextFireTime(-1);
		} else {
			setPreviousFireTime(getNextFireTime());
			setNextFireTime(getFireTimeAfter(getNextFireTime()));
		}
	}

	@Override
	public void updateAfterMisfire(long currentTimeMillis) {
		if (getEndTime() >= 0 && getEndTime() <= currentTimeMillis) {
			setPreviousFireTime(getNextFireTime());
			setNextFireTime(-1);
		} else {
			int instr = getMisfireInstruction();
	
			if(instr == Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY)
				return;
	
			if (instr == MISFIRE_INSTRUCTION_DO_NOTHING) {
				setPreviousFireTime(getNextFireTime());
				setNextFireTime(getFireTimeAfter(currentTimeMillis));
			} else {
				throw new InternalError();
			}
		}
	}

	protected long getFireTimeAfter(long afterTime) {
		if (afterTime < 0)
			return -1;

		if (getStartTime() > afterTime)
			afterTime = getStartTime() - 1000l;

		if (getEndTime() >= 0 && afterTime >= getEndTime())
			return -1;

		long pot = getTimeAfter(afterTime);
		if (getEndTime() >= 0 && pot >= 0 && pot >= getEndTime())
			return -1;

		return pot;
	}

	protected long getTimeAfter(long afterTime) {
		if (cronEx == null)
			return -1;

		Date after = cronEx.getTimeAfter(new Date(afterTime));

		return after == null ? -1 : after.getTime();
	}

	public String getCronExpression() {
		return cronEx.getCronExpression();
	}
	
	public void setCronExpression(String cronExpression) throws ParseException {
		TimeZone origTz = getTimeZone();
		this.cronEx = new CronExpression(cronExpression);
		this.cronEx.setTimeZone(origTz);
		this.cronEx = new CronExpression(cronExpression);
	}

	public TimeZone getTimeZone() {
		if(cronEx != null) {
			return cronEx.getTimeZone();
		}
		if (timeZone == null) {
			timeZone = HinemosTime.getTimeZone();
		}
		return timeZone;
	}
}