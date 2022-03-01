/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class ModifyJobQueueRequest implements RequestDto {

	private String name;
	private Integer concurrency;

	public ModifyJobQueueRequest(){
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getConcurrency() {
		return concurrency;
	}
	public void setConcurrency(Integer concurrency) {
		this.concurrency = concurrency;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
