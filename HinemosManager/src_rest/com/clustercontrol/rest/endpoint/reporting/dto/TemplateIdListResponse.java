/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.reporting.dto;

import java.util.List;

public class TemplateIdListResponse {
	
	public TemplateIdListResponse() {
	}

	private List<String> templateIdList;

	public List<String> getTemplateIdList() {
		return templateIdList;
	}

	public void setTemplateIdList(List<String> templateIdList) {
		this.templateIdList = templateIdList;
	}

	@Override
	public String toString() {
		return "TemplateIdListResponse [templateIdList=" + templateIdList + "]";
	}

}
