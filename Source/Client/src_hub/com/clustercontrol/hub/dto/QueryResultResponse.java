/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.hub.dto;

import java.util.List;
import java.util.Map;

import org.openapitools.client.model.BinaryQueryResultResponse;
import org.openapitools.client.model.StringQueryResultResponse;

import com.clustercontrol.fault.HinemosException;
import com.clustercontrol.util.RestClientBeanUtil;

public class QueryResultResponse {

	private Integer count;
	private Integer offset;
	private Integer size;
	private Long time;

	private List<DataResponse> dataList;

	private Map<String, Integer> tagCountMap;

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Integer getOffset() {
		return offset;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public List<DataResponse> getDataList() {
		return dataList;
	}

	public void setDataList(List<DataResponse> dataList) {
		this.dataList = dataList;
	}

	public Map<String, Integer> getTagCountMap() {
		return tagCountMap;
	}

	public void setTagCountMap(Map<String, Integer> tagCountMap) {
		this.tagCountMap = tagCountMap;
	}

	public static QueryResultResponse convertFromStringQueryResultResponse(StringQueryResultResponse dto)
			throws HinemosException {
		QueryResultResponse res = new QueryResultResponse();
		RestClientBeanUtil.convertBean(dto, res);
		return res;
	}

	public static QueryResultResponse convertFromBinaryQueryResultResponse(BinaryQueryResultResponse dto)
			throws HinemosException {
		QueryResultResponse res = new QueryResultResponse();
		RestClientBeanUtil.convertBean(dto, res);
		return res;
	}
}
