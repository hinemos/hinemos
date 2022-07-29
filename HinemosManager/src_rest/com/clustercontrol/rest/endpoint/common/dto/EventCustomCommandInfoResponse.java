/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.common.dto;

public class EventCustomCommandInfoResponse {

	public EventCustomCommandInfoResponse() {
	}

	private Boolean enable;
	private String displayName;
	private String description;
	private Long maxEventSize;
	private Long resultPollingDelay;
	private Long resultPollingInterval;

	public Boolean getEnable() {
		return enable;
	}

	public void setEnable(Boolean enable) {
		this.enable = enable;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getMaxEventSize() {
		return maxEventSize;
	}

	public void setMaxEventSize(Long maxEventSize) {
		this.maxEventSize = maxEventSize;
	}

	public Long getResultPollingDelay() {
		return resultPollingDelay;
	}

	public void setResultPollingDelay(Long resultPollingDelay) {
		this.resultPollingDelay = resultPollingDelay;
	}

	public Long getResultPollingInterval() {
		return resultPollingInterval;
	}

	public void setResultPollingInterval(Long resultPollingInterval) {
		this.resultPollingInterval = resultPollingInterval;
	}

	@Override
	public String toString() {
		return "EventCustomCommandInfoResponse [enable=" + enable + ", displayName=" + displayName + ", description="
				+ description + ", maxEventSize=" + maxEventSize + ", resultPollingDelay=" + resultPollingDelay
				+ ", resultPollingInterval=" + resultPollingInterval + "]";
	}
}
