/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.common.dto;

import com.clustercontrol.notify.restaccess.model.RestAccessSendHttpHeader;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.annotation.validation.RestValidateLong;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.util.MessageConstant;

@RestBeanConvertIdClassSet(infoClass = RestAccessSendHttpHeader.class, idName = "id")
public class RestAccessSendHttpHeaderRequest {

	@RestItemName(value = MessageConstant.ORDER_NO)
	@RestValidateLong(notNull = true, minVal = 1, maxVal = Long.MAX_VALUE )
	private Long headerOrderNo;

	@RestItemName(value = MessageConstant.RECORD_KEY)
	@RestValidateString(notNull = true, minLen = 1)
	private String key;

	@RestItemName(value = MessageConstant.VALUE)
	@RestValidateString(notNull = true, minLen = 1)
	private String value;

	public RestAccessSendHttpHeaderRequest() {
	}

	public Long getHeaderOrderNo() {
		return headerOrderNo;
	}
	public void setHeaderOrderNo(Long headerOrderNo) {
		this.headerOrderNo = headerOrderNo;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

}
