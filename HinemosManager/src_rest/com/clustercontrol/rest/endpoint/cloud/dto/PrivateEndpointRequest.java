/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class PrivateEndpointRequest implements RequestDto {
	@RestItemName(MessageConstant.XCLOUD_CORE_ENDPOINT_ID)
	@RestValidateString(notNull = true, maxLen = 256)
	private String endpointId;
	@RestItemName(MessageConstant.XCLOUD_CORE_URL)
	@RestValidateString(notNull = true, maxLen = 256)
	private String url;

	public PrivateEndpointRequest() {
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

	public String getEndpointId() {
		return endpointId;
	}

	public void setEndpointId(String endpointId) {
		this.endpointId = endpointId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
