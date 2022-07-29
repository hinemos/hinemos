/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorresult.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateInteger;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.filtersetting.dto.StatusFilterBaseRequest;
import com.clustercontrol.util.MessageConstant;

public class GetStatusListRequest implements RequestDto {

	@RestValidateObject(notNull = true)
	private StatusFilterBaseRequest filter;
	
	// 取得されるレコードの上限数
	@RestItemName(value=MessageConstant.SIZE)
	@RestValidateInteger(notNull = false , minVal = 1)
	private Integer size ;

	public GetStatusListRequest() {
	}

	public StatusFilterBaseRequest getFilter() {
		return filter;
	}

	public void setFilter(StatusFilterBaseRequest filter) {
		this.filter = filter;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		filter.correlationCheck();
	}
}
