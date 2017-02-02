package com.clustercontrol.plugin.util.scheduler;

public class JobBuilder {
	private Class<? extends Job> jobClass;
	private JobKey key;
	private JobDataMap jobDataMap = new JobDataMap();
	private boolean durability;

	public static JobBuilder newJob(Class<? extends Job> jobClass) {
		JobBuilder b = new JobBuilder();
		b.ofType(jobClass);
		return b;
	}

	public JobBuilder ofType(Class<? extends Job> jobClass) {
		this.jobClass = jobClass;
		return this;
	}

	public JobBuilder withIdentity(String name) {
		key = new JobKey(name, null);
		return this;
	} 
	public JobBuilder withIdentity(String name, String group) {
		key = new JobKey(name, group);
		return this;
	}
	public JobBuilder withIdentity(JobKey jobKey) {
		this.key = jobKey;
		return this;
	}

	public JobBuilder storeDurably(boolean durability) {
		this.durability = durability;
		return this;
	}

	public JobBuilder usingJobData(String dataKey, String value) {
		jobDataMap.put(dataKey, value);
		return this;
	}

	public JobBuilder usingJobData(String dataKey, Integer value) {
		jobDataMap.put(dataKey, value);
		return this;
	}

	public JobBuilder usingJobData(String dataKey, Long value) {
		jobDataMap.put(dataKey, value);
		return this;
	}

	public JobBuilder usingJobData(String dataKey, Float value) {
		jobDataMap.put(dataKey, value);
		return this;
	}

	public JobBuilder usingJobData(String dataKey, Double value) {
		jobDataMap.put(dataKey, value);
		return this;
	}

	public JobBuilder usingJobData(String dataKey, Boolean value) {
		jobDataMap.put(dataKey, value);
		return this;
	}

	public JobBuilder usingJobData(JobDataMap newJobDataMap) {
		jobDataMap.putAll(newJobDataMap);
		return this;
	}

	public JobDetail build() {
		JobDetail detail = new JobDetail();
		detail.setJobClass(jobClass);
		detail.setKey(key);
		detail.setDurability(durability);
		detail.setJobDataMap(jobDataMap);
		return detail;
	}
}