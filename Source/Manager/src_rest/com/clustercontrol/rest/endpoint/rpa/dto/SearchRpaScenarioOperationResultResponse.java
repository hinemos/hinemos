/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.rpa.dto;

import java.util.List;

public class SearchRpaScenarioOperationResultResponse {
	
	public SearchRpaScenarioOperationResultResponse() {
	}

	private Integer count;
	private Integer offset;
	private Integer size;
	private Long time;
	
	private List<SearchRpaScenarioOperationResultDataResponse> resultList;

	/** 検索結果数 */
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
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

	/** 検索実施時間 */
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}

	/** 検索結果  */
	public List<SearchRpaScenarioOperationResultDataResponse> getResultList() {
		return resultList;
	}
	public void setResultList(List<SearchRpaScenarioOperationResultDataResponse> resultList) {
		this.resultList = resultList;
	}

	@Override
	public String toString() {
		return "SearchRpaScenarioOperationResultResponse [count=" + count 
				+ ", offset=" + offset + ", size=" + size + ", time=" + time + ", resultList=" + resultList
				+ "]";
	}

}
