/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;

public class JobQueueContentsViewInfoListItemResponse {

	private JobTreeItemResponseP4 jobTreeItem;
	
	private String sessionId;
	@RestBeanConvertDatetime
	private String regDate;

	public JobQueueContentsViewInfoListItemResponse(){
	}
	public JobTreeItemResponseP4 getJobTreeItem() {
		return jobTreeItem;
	}

	public void setJobTreeItem(JobTreeItemResponseP4 jobTreeItem) {
		this.jobTreeItem = jobTreeItem;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getRegDate() {
		return regDate;
	}

	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}
}
