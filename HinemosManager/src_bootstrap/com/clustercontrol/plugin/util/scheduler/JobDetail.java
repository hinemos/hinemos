/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.util.scheduler;

import java.io.Serializable;

public class JobDetail implements Serializable, Cloneable {

	private static final long serialVersionUID = -6069784757781506897L;
	private static final String DEFAULT_GROUP = "DEFAULT";

	private String name;
	private String group = DEFAULT_GROUP;
	private String description;
	private Class<? extends Job> jobClass;
	private JobDataMap jobDataMap;
	private boolean durability = false;
	private transient JobKey key = null;
	private Long executeTime;
	private Long previousFireTime;

	public JobDetail() {
	}

	public JobDetail(String name, Class<? extends Job> jobClass) {
		this(name, null, jobClass);
	}

	public JobDetail(String name, String group, Class<? extends Job> jobClass) {
		setName(name);
		setGroup(group);
		setJobClass(jobClass);
	}

	public JobDetail(String name, String group, Class<? extends Job> jobClass,
			boolean durability, boolean recover) {
		setName(name);
		setGroup(group);
		setJobClass(jobClass);
		setDurability(durability);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name == null || name.trim().length() == 0) {
			throw new IllegalArgumentException("Job name cannot be empty.");
		}

		this.name = name;
		this.key = null;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		if (group != null && group.trim().length() == 0) {
			throw new IllegalArgumentException(
					"Group name cannot be empty.");
		}

		if (group == null) {
			group = DEFAULT_GROUP;
		}

		this.group = group;
		this.key = null;
	}

	public String getFullName() {
		return group + "." + name;
	}

	public JobKey getKey() {
		if(key == null) {
			if(getName() == null)
				return null;
			key = new JobKey(getName(), getGroup());
		}

		return key;
	}

	public void setKey(JobKey key) {
		if(key == null)
			throw new IllegalArgumentException("Key cannot be null!");

		setName(key.getName());
		setGroup(key.getGroup());
		this.key = key;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Class<? extends Job> getJobClass() {
		return jobClass;
	}

	public void setJobClass(Class<? extends Job> jobClass) {
		if (jobClass == null) {
			throw new IllegalArgumentException("Job class cannot be null.");
		}

		if (!Job.class.isAssignableFrom(jobClass)) {
			throw new IllegalArgumentException(
					"Job class must implement the Job interface.");
		}

		this.jobClass = jobClass;
	}

	public JobDataMap getJobDataMap() {
		if (jobDataMap == null) {
			jobDataMap = new JobDataMap();
		}
		return jobDataMap;
	}

	public void setJobDataMap(JobDataMap jobDataMap) {
		this.jobDataMap = jobDataMap;
	}

	public void setDurability(boolean durability) {
		this.durability = durability;
	}

	public boolean isDurable() {
		return durability;
	}
	public Long getExecuteTime() {
		return executeTime;
	}
	public void setExecuteTime(Long executeTime) {
		this.executeTime = executeTime;
	}
	public Long getPreviousFireTime() {
		return previousFireTime;
	}
	public void setPreviousFireTime(Long previousFireTime) {
		this.previousFireTime = previousFireTime;
	}

	@Override
	public String toString() {
		return "JobDetail '" + getFullName() + "':  jobClass: '"
				+ ((getJobClass() == null) ? null : getJobClass().getName())
				+ " isDurable: " + isDurable();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof JobDetail)) {
			return false;
		}

		JobDetail other = (JobDetail) obj;

		if(other.getKey() == null || getKey() == null)
			return false;

		if (!other.getKey().equals(getKey())) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return getKey().hashCode();
	}

	@Override
	public JobDetail clone() {
		try {
			JobDetail cloneInfo = (JobDetail)super.clone();
			cloneInfo.setKey(this.getKey());
			cloneInfo.setDescription(this.description);
			cloneInfo.setJobClass(this.jobClass);
			cloneInfo.setJobDataMap(this.jobDataMap);
			cloneInfo.setExecuteTime(this.executeTime);
			cloneInfo.setPreviousFireTime(this.previousFireTime);
			return cloneInfo;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}
}