/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.rpa.dto;

import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.cmdtool.DatetimeTypeParam;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rpa.scenario.model.RpaScenarioOperationResult.OperationResultStatus;

public class SearchRpaScenarioOperationResultRequest implements RequestDto {
	
	public SearchRpaScenarioOperationResultRequest (){}
	
	/** 実行時刻検索開始時刻 */
	@DatetimeTypeParam
	private Long startDateFrom;
	
	/** 実行時刻検索終了時刻 */
	@DatetimeTypeParam
	private Long startDateTo;
	
	/** シナリオID */
	private String scenarioId;
	
	/** シナリオタグID */
	private List<String> tagIdList;
	
	/** ステータス */
	private List<OperationResultStatus> statusList;
	
	/** ファシリティID */
	private String facilityId;
	
	/** 検索結果取得開始位置 */
	private Integer offset;

	/** 検索結果取得数 */
	private Integer size;

	/** 検索結果最大数取得フラグ */
	private Boolean needCount;

	/** 実行時刻検索開始時刻 */
	public Long getStartDateFrom() {
		return startDateFrom;
	}
	public void setStartDateFrom(Long startDateFrom) {
		this.startDateFrom = startDateFrom;
	}

	/** 実行時刻検索終了時刻 */
	public Long getStartDateTo() {
		return startDateTo;
	}
	public void setStartDateTo(Long startDateTo) {
		this.startDateTo = startDateTo;
	}

	/** シナリオID */
	public String getScenarioId() {
		return scenarioId;
	}
	public void setScenarioId(String scenarioId) {
		this.scenarioId = scenarioId;
	}
	
	/** シナリオタグID */
	public List<String> getTagIdList() {
		return tagIdList;
	}
	public void setTagIdList(List<String> tagIdList) {
		this.tagIdList = tagIdList;
	}

	/** ステータス */
	public List<OperationResultStatus> getStatusList() {
		return this.statusList;
	}
	public void setStatusList(List<OperationResultStatus> statusList) {
		this.statusList = statusList;
	}
	
	/**　ファシリティID取得 */
	public String getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}
	
	/** 検索結果取得開始位置 */
	public Integer getOffset() {
		return offset;
	}
	public void setOffset(Integer offset) {
		this.offset = offset;
	}
	
	/** 検索結果取得数 */
	public Integer getSize() {
		return size;
	}
	public void setSize(Integer size) {
		this.size = size;
	}
	
	/** 検索結果最大数取得フラグ */
	public Boolean isNeedCount() {
		return needCount;
	}
	public void setNeedCount(Boolean needCount) {
		this.needCount = needCount;
	}
	
	@Override
	public String toString() {
		return "SearchRpaScenarioOperationResultRequest [startDateFrom=" + startDateFrom + ", startDateTo=" + startDateTo
				+ ", scenarioId=" + scenarioId + ", tagIdList=" + tagIdList + ", statusList=" + statusList 
				+ ", facilityId=" + facilityId + ", offset=" + offset + ", size=" + size + ", needCount=" + needCount
				+ "]";
	}
	
	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
