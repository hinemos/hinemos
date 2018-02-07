/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.util.scheduler;

public abstract class AbstractTrigger implements Trigger, Comparable<AbstractTrigger> {

	private TriggerKey key;
	private long startTime = -1;
	private long endTime = -1;
	private long nextFireTime = -1;
	private long previousFireTime = -1;

	private int misfireInstruction;

	public AbstractTrigger() {
	}

	@Override
	public long getStartTime() {
		return startTime;
	}
	@Override
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	@Override
	public long getEndTime() {
		return endTime;
	}
	@Override
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	@Override
	public int getMisfireInstruction() {
		return misfireInstruction;
	}
	public void setMisfireInstruction(int misfireInstruction) {
		this.misfireInstruction = misfireInstruction;
	}
	
	@Override
	public long getNextFireTime() {
		return nextFireTime;
	}
	public void setNextFireTime(long nextFireTime) {
		this.nextFireTime = nextFireTime;
	}

	@Override
	public long getPreviousFireTime() {
		return previousFireTime;
	}
	public void setPreviousFireTime(long previousFireTime) {
		this.previousFireTime = previousFireTime;
	}

	@Override
	public TriggerKey getTriggerKey() {
		return key;
	}
	public void setTriggerKey(TriggerKey key) {
		this.key = key;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (endTime ^ (endTime >>> 32));
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + misfireInstruction;
		result = prime * result + (int) (nextFireTime ^ (nextFireTime >>> 32));
		result = prime * result + (int) (previousFireTime ^ (previousFireTime >>> 32));
		result = prime * result + (int) (startTime ^ (startTime >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractTrigger other = (AbstractTrigger) obj;
		if (endTime != other.endTime)
			return false;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (misfireInstruction != other.misfireInstruction)
			return false;
		if (nextFireTime != other.nextFireTime)
			return false;
		if (previousFireTime != other.previousFireTime)
			return false;
		if (startTime != other.startTime)
			return false;
		return true;
	}
	
	@Override
	public synchronized int compareTo(AbstractTrigger o) {
		AbstractTrigger other = (AbstractTrigger)o;
		long diff;
		
		// 最初にNextFireTimeを比較する必要あり
		diff = this.getNextFireTime() - other.getNextFireTime();
		if (0 < diff) {
			return 1;
		} else if (diff < 0){
			return -1;
		}
		
		diff = this.getPreviousFireTime() - other.getPreviousFireTime();
		if (0 < diff) {
			return 1;
		} else if (diff < 0){
			return -1;
		}
		
		diff = this.getStartTime() - other.getStartTime();
		if (0 < diff) {
			return 1;
		} else if (diff < 0){
			return -1;
		}
		
		diff = this.getEndTime() - other.getEndTime();
		if (0 < diff) {
			return 1;
		} else if (diff < 0){
			return -1;
		}
		
		diff = this.getMisfireInstruction() - other.getMisfireInstruction();
		if (0 < diff) {
			return 1;
		} else if (diff < 0){
			return -1;
		}
		
		return this.getTriggerKey().compareTo(other.getTriggerKey());
	}
}