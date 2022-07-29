/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

public class SettingUpdateInfoResponse {

	// ---- from SettingUpdateInfo
	private long binaryMonitorUpdateTime;
	private long calendarUpdateTime;
	private long customMonitorUpdateTime;
	private long customTrapMonitorUpdateTime;
	private long hinemosTimeOffset;
	private int hinemosTimeZoneOffset;
	private long jobFileCheckUpdateTime;
	private long logFileMonitorUpdateTime;
	private long nodeConfigRunCollectUpdateTime;
	private long nodeConfigSettingUpdateTime;
	private long repositoryUpdateTime;
	private long snmptrapMonitorUpdateTime;
	private long systemLogMonitorUpdateTime;
	private long winEventMonitorUpdateTime;
	private long sdmlControlSettingUpdateTime;
	private long rpaLogFileMonitorUpdateTime;
	private long cloudLogMonitorUpdateTime;

	public SettingUpdateInfoResponse() {
	}

	public long getBinaryMonitorUpdateTime() {
		return binaryMonitorUpdateTime;
	}

	public void setBinaryMonitorUpdateTime(long binaryMonitorUpdateTime) {
		this.binaryMonitorUpdateTime = binaryMonitorUpdateTime;
	}

	public long getCalendarUpdateTime() {
		return calendarUpdateTime;
	}

	public void setCalendarUpdateTime(long calendarUpdateTime) {
		this.calendarUpdateTime = calendarUpdateTime;
	}

	public long getCustomMonitorUpdateTime() {
		return customMonitorUpdateTime;
	}

	public void setCustomMonitorUpdateTime(long customMonitorUpdateTime) {
		this.customMonitorUpdateTime = customMonitorUpdateTime;
	}

	public long getCustomTrapMonitorUpdateTime() {
		return customTrapMonitorUpdateTime;
	}

	public void setCustomTrapMonitorUpdateTime(long customTrapMonitorUpdateTime) {
		this.customTrapMonitorUpdateTime = customTrapMonitorUpdateTime;
	}

	public long getHinemosTimeOffset() {
		return hinemosTimeOffset;
	}

	public void setHinemosTimeOffset(long hinemosTimeOffset) {
		this.hinemosTimeOffset = hinemosTimeOffset;
	}

	public int getHinemosTimeZoneOffset() {
		return hinemosTimeZoneOffset;
	}

	public void setHinemosTimeZoneOffset(int hinemosTimeZoneOffset) {
		this.hinemosTimeZoneOffset = hinemosTimeZoneOffset;
	}

	public long getJobFileCheckUpdateTime() {
		return jobFileCheckUpdateTime;
	}

	public void setJobFileCheckUpdateTime(long jobFileCheckUpdateTime) {
		this.jobFileCheckUpdateTime = jobFileCheckUpdateTime;
	}

	public long getLogFileMonitorUpdateTime() {
		return logFileMonitorUpdateTime;
	}

	public void setLogFileMonitorUpdateTime(long logFileMonitorUpdateTime) {
		this.logFileMonitorUpdateTime = logFileMonitorUpdateTime;
	}

	public long getNodeConfigRunCollectUpdateTime() {
		return nodeConfigRunCollectUpdateTime;
	}

	public void setNodeConfigRunCollectUpdateTime(long nodeConfigRunCollectUpdateTime) {
		this.nodeConfigRunCollectUpdateTime = nodeConfigRunCollectUpdateTime;
	}

	public long getNodeConfigSettingUpdateTime() {
		return nodeConfigSettingUpdateTime;
	}

	public void setNodeConfigSettingUpdateTime(long nodeConfigSettingUpdateTime) {
		this.nodeConfigSettingUpdateTime = nodeConfigSettingUpdateTime;
	}

	public long getRepositoryUpdateTime() {
		return repositoryUpdateTime;
	}

	public void setRepositoryUpdateTime(long repositoryUpdateTime) {
		this.repositoryUpdateTime = repositoryUpdateTime;
	}

	public long getSnmptrapMonitorUpdateTime() {
		return snmptrapMonitorUpdateTime;
	}

	public void setSnmptrapMonitorUpdateTime(long snmptrapMonitorUpdateTime) {
		this.snmptrapMonitorUpdateTime = snmptrapMonitorUpdateTime;
	}

	public long getSystemLogMonitorUpdateTime() {
		return systemLogMonitorUpdateTime;
	}

	public void setSystemLogMonitorUpdateTime(long systemLogMonitorUpdateTime) {
		this.systemLogMonitorUpdateTime = systemLogMonitorUpdateTime;
	}

	public long getWinEventMonitorUpdateTime() {
		return winEventMonitorUpdateTime;
	}

	public void setWinEventMonitorUpdateTime(long winEventMonitorUpdateTime) {
		this.winEventMonitorUpdateTime = winEventMonitorUpdateTime;
	}

	public long getSdmlControlSettingUpdateTime() {
		return sdmlControlSettingUpdateTime;
	}

	public void setSdmlControlSettingUpdateTime(long sdmlControlSettingUpdateTime) {
		this.sdmlControlSettingUpdateTime = sdmlControlSettingUpdateTime;
	}

	public long getRpaLogFileMonitorUpdateTime() {
		return rpaLogFileMonitorUpdateTime;
	}

	public void setRpaLogFileMonitorUpdateTime(long rpaLogFileMonitorUpdateTime) {
		this.rpaLogFileMonitorUpdateTime = rpaLogFileMonitorUpdateTime;
	}
	
	public long getCloudLogMonitorUpdateTime() {
		return cloudLogMonitorUpdateTime;
	}

	public void setCloudLogMonitorUpdateTime(long cloudLogMonitorUpdateTime) {
		this.cloudLogMonitorUpdateTime = cloudLogMonitorUpdateTime;
	}
}
