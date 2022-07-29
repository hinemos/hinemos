/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.hub.dto;

import java.util.ArrayList;
import java.util.List;

import org.openapitools.client.model.TagResponse;

public class DataResponse {
	private String facilityId;
	private String monitorId;
	private Long time;
	private String data;
	private List<TagResponse> tagList = new ArrayList<>();
	private Long collectId;
	private Long dataId;
	private String recordKey;

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

	public List<TagResponse> getTagList() {
		return tagList;
	}

	public void setTagList(List<TagResponse> tagList) {
		this.tagList = tagList;
	}

	public Long getCollectId() {
		return this.collectId;
	}

	public void setCollectId(Long collectId) {
		this.collectId = collectId;
	}

	public Long getDataId() {
		return this.dataId;
	}

	public void setDataId(Long dataId) {
		this.dataId = dataId;
	}

	public String getRecordKey() {
		return recordKey;
	}

	public void setRecordKey(String recordKey) {
		this.recordKey = recordKey;
	}
}
