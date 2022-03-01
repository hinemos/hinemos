/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmap.dto;

import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.JobTypeEnum;

public class JobmapIconIdDefaultListResponse {

	private JobTypeEnum type;
	private String defaultId;
	
	public JobmapIconIdDefaultListResponse() {
	}

	public JobmapIconIdDefaultListResponse(JobTypeEnum type, String defaultId){
		this.type = type;
		this.defaultId = defaultId;
	}

	public JobTypeEnum getType() {
		return type;
	}

	public void setType(JobTypeEnum type) {
		this.type = type;
	}

	public String getDefaultId() {
		return defaultId;
	}

	public void setDefaultId(String defaultId) {
		this.defaultId = defaultId;
	}
}
