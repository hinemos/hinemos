/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateInteger;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class RpaManagementToolServiceCheckInfoRequest implements RequestDto {
	/**
	 * コネクションタイムアウト
	 */
	@RestItemName(MessageConstant.CONNECTION_TIMEOUT)
	@RestValidateInteger(notNull=true, minVal=0, maxVal=60 * 60 * 1000)
	private Integer connectTimeout;
	/**
	 * リクエストタイムアウト
	 */
	@RestItemName(MessageConstant.REQUEST_TIMEOUT)
	@RestValidateInteger(notNull=true, minVal=0, maxVal=60 * 60 * 1000)
	private Integer requestTimeout;
	
	/**
	 * コネクションタイムアウト
	 */
	public Integer getConnectTimeout() {
		return this.connectTimeout;
	}

	public void setConnectTimeout(Integer connectTimeout) {
		this.connectTimeout = connectTimeout;
	}
	
	/**
	 * リクエストタイムアウト
	 */
	public Integer getRequestTimeout() {
		return requestTimeout;
	}

	public void setRequestTimeout(Integer requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

	/* (non-Javadoc)
	 * @see com.clustercontrol.rest.dto.RequestDto#correlationCheck()
	 */
	@Override
	public void correlationCheck() throws InvalidSetting {
	}

	@Override
	public String toString() {
		return "RpaManagementToolServiceCheckInfoRequest [connectTimeout=" + connectTimeout + ", requestTimeout=" + requestTimeout + "]";
	}

}
