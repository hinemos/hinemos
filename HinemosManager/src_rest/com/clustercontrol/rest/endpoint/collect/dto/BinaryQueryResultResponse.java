/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.collect.dto;

import java.util.List;
import java.util.Map;

public class BinaryQueryResultResponse {

	public BinaryQueryResultResponse(){
	}
	
	private Integer count;
	private Integer offset;
	private Integer size;
	private Long time;
	
	private List<BinaryDataResponse> dataList;

	/**
	 * 検索結果数取得。
	 * 
	 * @return the count
	 */
	public Integer getCount() {
		return count;
	}

	/**
	 * 検索結果数設定。
	 * 
	 * @param count the count to set
	 */
	public void setCount(Integer count) {
		this.count = count;
	}

	/**
	 * 検索結果取得開始位置取得。
	 * 
	 * @return the offset
	 */
	public Integer getOffset() {
		return offset;
	}

	/**
	 * 検索結果取得開始位置設定。
	 * 
	 * @param offset the offset to set
	 */
	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	/**
	 * 検索結果取得数取得。
	 * 
	 * @return the size
	 */
	public Integer getSize() {
		return size;
	}

	/**
	 * 検索結果取得数設定。
	 * 
	 * @param size the size to set
	 */
	public void setSize(Integer size) {
		this.size = size;
	}

	/**
	 * 検索実施時間取得。
	 * 
	 * @return the time
	 */
	public Long getTime() {
		return time;
	}

	/**
	 * 検索実施時間設定。
	 * 
	 * @param time the time to set
	 */
	public void setTime(Long time) {
		this.time = time;
	}

	/**
	 * 検索結果取得。
	 * 
	 * @return the dataList
	 */
	public List<BinaryDataResponse> getDataList() {
		return dataList;
	}

	/**
	 * 検索結果設定。
	 * 
	 * @param dataList the dataList to set
	 */
	public void setDataList(List<BinaryDataResponse> dataList) {
		this.dataList = dataList;
	}
}
