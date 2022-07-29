/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

public class MonitorJobInfoResponse extends AbstractJobResponse {

	/** 監視ジョブ情報 */
	private JobMonitorInfoResponse monitor;

	public MonitorJobInfoResponse() {
	}


	public JobMonitorInfoResponse getMonitor() {
		return monitor;
	}


	public void setMonitor(JobMonitorInfoResponse monitor) {
		this.monitor = monitor;
	}


}
