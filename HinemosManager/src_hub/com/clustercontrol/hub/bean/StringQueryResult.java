/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.hub.bean;

import java.util.List;

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