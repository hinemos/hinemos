/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.dto.RequestDto;

public class RegisterJobunitRequest implements RequestDto {
	@RestValidateObject(notNull = true)
	JobTreeItemRequest jobTreeItem;

	public RegisterJobunitRequest() {
	}

	public JobTreeItemRequest getJobTreeItem() {
		return jobTreeItem;
	}

	public void setJobTreeItem(JobTreeItemRequest jobTreeItem) {
		this.jobTreeItem = jobTreeItem;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		jobTreeItem.correlationCheck();
	}

}
