/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.List;

import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ControlEnum;

public class JobOperationPropResponse {
	
	private List<ControlEnum> availableOperationList;

	public List<ControlEnum> getAvailableOperationList() {
		return availableOperationList;
	}

	public void setAvailableOperationList(List<ControlEnum> availableOperationList) {
		this.availableOperationList = availableOperationList;
	}

}
