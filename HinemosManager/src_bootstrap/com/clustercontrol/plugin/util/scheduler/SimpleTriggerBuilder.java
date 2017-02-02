package com.clustercontrol.plugin.util.scheduler;

import com.clustercontrol.util.HinemosTime;

public class SimpleTriggerBuilder {
	private TriggerKey key;
	private long baseTime = HinemosTime.currentTimeMillis();
	private long startTimeMillis = baseTime;
	private long endTime = -1;
	private long period = -1;
	private int misfireInstruction = Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY;

	private SimpleTriggerBuilder() {
	}

	public static SimpleTriggerBuilder newTrigger() {
		return new SimpleTriggerBuilder();
	}
	
    public SimpleTriggerBuilder withIdentity(String name) {
        key = new TriggerKey(name, null);
        return this;
    }  
    
    public SimpleTriggerBuilder withIdentity(String name, String group) {
        key = new TriggerKey(name, group);
        return this;
    }
    
    public SimpleTriggerBuilder withIdentity(TriggerKey triggerKey) {
        this.key = triggerKey;
        return this;
    }

	public SimpleTriggerBuilder startAt(long startTimeMillis) {
		this.startTimeMillis = startTimeMillis;
		return this;
	}
	public SimpleTriggerBuilder startNow() {
		this.startTimeMillis = HinemosTime.currentTimeMillis();
		return this;
	}

	public SimpleTriggerBuilder endAt(long endTime) {
		this.endTime = endTime;
		return this;
	}

	public SimpleTriggerBuilder setPeriod(long period) {
		this.period = period;
		return this;
	}
	
    public SimpleTriggerBuilder withMisfireHandlingInstructionIgnoreMisfires() {
        misfireInstruction = Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY;
        return this;
    }

    public SimpleTriggerBuilder withMisfireHandlingInstructionDoNothing() {
        misfireInstruction = Trigger.MISFIRE_INSTRUCTION_DO_NOTHING;
        return this;
    }

	public Trigger build() {
		SimpleTrigger trig = new SimpleTrigger();
		trig.setTriggerKey(key);
		trig.setStartTime(startTimeMillis);
		trig.setEndTime(endTime);
		trig.setPeriod(period);
		trig.setMisfireInstruction(misfireInstruction);
		return trig;
	}
}