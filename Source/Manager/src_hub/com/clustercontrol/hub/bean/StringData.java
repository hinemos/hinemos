/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.bean;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.hub.model.CollectStringDataPK;

/**
 * 文字列収集値の検索結果を格納
 *
 */
public class StringData {

	// クライアント表示項目.
	private String facilityId;
	private String monitorId;
	private Long time;
	private String data;
	private List<Tag> tagList = new ArrayList<>();

	// ダウンロード処理向けの引継項目.
	/** 主キー */
	private CollectStringDataPK primaryKey;
	/** レコードキー(バイナリのみ・ファイル統合時の整列用) */
	private String recordKey;
	
	// 以下setter and getter.
	public String getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getMonitorId() {
		return monitorId;
	}
	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}
	
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}

	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}

	public List<Tag> getTagList() {
		return tagList;
	}
	public void setTagList(List<Tag> tagList) {
		this.tagList = tagList;
	}
	
	public CollectStringDataPK getPrimaryKey() {
		return primaryKey;
	}
	public void setPrimaryKey(CollectStringDataPK primaryKey) {
		this.primaryKey = primaryKey;
	}

	public String getRecordKey() {
		return recordKey;
	}
	public void setRecordKey(String recordKey) {
		this.recordKey = recordKey;
	}

}