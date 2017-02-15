package com.clustercontrol.plugin.util.scheduler;


public interface Job {
	public void execute(JobDetail jd) throws JobExecutionException;
}