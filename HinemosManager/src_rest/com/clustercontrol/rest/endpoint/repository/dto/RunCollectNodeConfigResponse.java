/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto;

public class RunCollectNodeConfigResponse {

	private String settingId;
	private Long loadDistributionTime;
	private Boolean result;

	public RunCollectNodeConfigResponse() {
	}

	public String getSettingId() {
		return settingId;
	}

	public void setSettingId(String settingId) {
		this.settingId = settingId;
	}

	public Long getLoadDistributionTime() {
		return loadDistributionTime;
	}

	public void setLoadDistributionTime(Long loadDistributionTime) {
		this.loadDistributionTime = loadDistributionTime;
	}

	public Boolean getResult() {
		return result;
	}

	public void setResult(Boolean result) {
		this.result = result;
	}
}
