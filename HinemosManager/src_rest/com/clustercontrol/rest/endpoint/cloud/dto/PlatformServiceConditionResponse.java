/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.xcloud.factory.ICloudOption.PlatformServiceStatus;

public class PlatformServiceConditionResponse {
	private String id;
	private String serviceName;
	private PlatformServiceStatus status;
	private String message;
	private String detail;
	@RestBeanConvertDatetime
	private String beginDate;
	@RestBeanConvertDatetime
	private String lastDate;
	@RestBeanConvertDatetime
	private String recordDate;

	public PlatformServiceConditionResponse() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public PlatformServiceStatus getStatus() {
		return status;
	}

	public void setStatus(PlatformServiceStatus status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(String beginDate) {
		this.beginDate = beginDate;
	}

	public String getLastDate() {
		return lastDate;
	}

	public void setLastDate(String lastDate) {
		this.lastDate = lastDate;
	}

	public String getRecordDate() {
		return recordDate;
	}

	public void setRecordDate(String recordDate) {
		this.recordDate = recordDate;
	}

}
