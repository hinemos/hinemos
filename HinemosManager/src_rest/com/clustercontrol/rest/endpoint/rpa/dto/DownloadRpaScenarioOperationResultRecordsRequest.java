/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.rpa.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class DownloadRpaScenarioOperationResultRecordsRequest implements RequestDto {

	public DownloadRpaScenarioOperationResultRecordsRequest() {
	}

	private SearchRpaScenarioOperationResultRequest searchRpaScenarioOperationResultRequest;
	private String filename;
	private String clientName;
	private Boolean scenarioOperationResultFlg;
	private Boolean scenarioFlg;
	private Boolean scenarioTagFlg;
	private Boolean scopeFlg;

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public Boolean getScenarioOperationResultFlg() {
		return scenarioOperationResultFlg;
	}

	public void setScenarioOperationResultFlg(Boolean scenarioOperationResultFlg) {
		this.scenarioOperationResultFlg = scenarioOperationResultFlg;
	}

	public Boolean getScenarioFlg() {
		return scenarioFlg;
	}

	public void setScenarioFlg(Boolean scenarioFlg) {
		this.scenarioFlg = scenarioFlg;
	}

	public Boolean getScenarioTagFlg() {
		return scenarioTagFlg;
	}

	public void setScenarioTagFlg(Boolean scenarioTagFlg) {
		this.scenarioTagFlg = scenarioTagFlg;
	}

	public Boolean getScopeFlg() {
		return scopeFlg;
	}

	public void setScopeFlg(Boolean scopeFlg) {
		this.scopeFlg = scopeFlg;
	}

	public SearchRpaScenarioOperationResultRequest getSearchRpaScenarioOperationResultRequest() {
		return searchRpaScenarioOperationResultRequest;
	}

	public void setSearchRpaScenarioOperationResultRequest(SearchRpaScenarioOperationResultRequest searchRpaScenarioOperationResultRequest) {
		this.searchRpaScenarioOperationResultRequest = searchRpaScenarioOperationResultRequest;
	}
	
	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
