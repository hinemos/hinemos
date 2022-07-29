/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorresult.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.filtersetting.dto.EventFilterBaseRequest;
import com.clustercontrol.rest.endpoint.monitorresult.dto.enumtype.ConfiremTypeEnum;

public class ModifyBatchConfirmRequest implements RequestDto {

	@RestValidateObject(notNull = true)
	private EventFilterBaseRequest filter;

	@RestValidateObject(notNull = true)
	private ConfiremTypeEnum confirmType;

	public ModifyBatchConfirmRequest() {
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		// NOP
	}

	public EventFilterBaseRequest getFilter() {
		return filter;
	}

	public void setFilter(EventFilterBaseRequest filter) {
		this.filter = filter;
	}

	public ConfiremTypeEnum getConfirmType() {
		return this.confirmType;
	}

	public void setConfirmType(ConfiremTypeEnum confirmType) {
		this.confirmType = confirmType;
	}
}
