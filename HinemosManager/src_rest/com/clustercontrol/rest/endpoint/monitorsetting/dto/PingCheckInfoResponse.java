/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class PingCheckInfoResponse {
	private Integer runCount;
	private Integer runInterval;
	private Integer timeout;

	public PingCheckInfoResponse() {
	}

	public Integer getRunCount() {
		return runCount;
	}
	public void setRunCount(Integer runCount) {
		this.runCount = runCount;
	}
	public Integer getRunInterval() {
		return runInterval;
	}
	public void setRunInterval(Integer runInterval) {
		this.runInterval = runInterval;
	}
	public Integer getTimeout() {
		return timeout;
	}
	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}
	@Override
	public String toString() {
		return "PingCheckInfo [runCount=" + runCount
				+ ", runInterval=" + runInterval + ", timeout=" + timeout + "]";
	}

}