/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.validation.RestValidateInteger;
import com.clustercontrol.rest.dto.RequestDto;

public class GetJobLinkMessageListRequest implements RequestDto {

	private JobLinkMessageFilterRequest filterInfo;

	// 取得されるレコードの上限数
	@RestValidateInteger(notNull = false,minVal=0)
	private Integer size ;

	public GetJobLinkMessageListRequest() {
	}

	public JobLinkMessageFilterRequest getFilterInfo() {
		return filterInfo;
	}

	public void setFilterInfo(JobLinkMessageFilterRequest filterInfo) {
		this.filterInfo = filterInfo;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
