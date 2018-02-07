/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.bean;

import java.util.List;
import java.util.Map;

/**
 * 文字列収集値の検索結果を格納
 *
 */
public class StringQueryResult {
	private Integer count;
	private Integer offset;
	private Integer size;
	private Long time;
	
	private List<StringData> dataList;

	private Map<String, Integer> tagCountMap;

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
	public List<StringData> getDataList() {
		return dataList;
	}

	/**
	 * 検索結果設定。
	 * 
	 * @param dataList the dataList to set
	 */
	public void setDataList(List<StringData> dataList) {
		this.dataList = dataList;
	}

	/**
	 * 検索結果取得。
	 * タグごとの集計結果を返す。
	 * 
	 * @return the tagCountMap
	 */
	public Map<String, Integer> getTagCountMap() {
		return tagCountMap;
	}

	/**
	 * 検索結果設定。
	 * 
	 * @param tagCountMap the tagCountMap to set
	 */
	public void setTagCountMap(Map<String, Integer> tagCountMap) {
		this.tagCountMap = tagCountMap;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "StringQueryResult [count=" + count + ", offset=" + offset + ", size=" + size + ", time=" + time
				+ ", dataList=" + dataList + "]";
	}
	
	public String toResultString() {
		return "StringQueryResult [count=" + count + ", offset=" + offset + ", size=" + size + ", time=" + time + "]";
	}
}