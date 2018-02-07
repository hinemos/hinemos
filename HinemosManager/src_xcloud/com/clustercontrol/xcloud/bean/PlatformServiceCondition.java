/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import javax.xml.bind.annotation.XmlRootElement;

import com.clustercontrol.xcloud.factory.ICloudOption;

@XmlRootElement(namespace ="http://xcloud.ws.clustercontrol.com") 
public class PlatformServiceCondition {
	private String id;
	private String serviceName;
	private ICloudOption.PlatformServiceStatus status;
	private String message;
	private String detail;

	private Long beginDate;
	private Long lastDate;
	private Long recordDate;

	public PlatformServiceCondition() {
		super();
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
	
	public ICloudOption.PlatformServiceStatus getStatus() {
		return status;
	}
	public void setStatus(ICloudOption.PlatformServiceStatus status) {
		this.status = status;
	}
	
	public Long getBeginDate() {
		return beginDate;
	}
	public void setBeginDate(Long beginDate) {
		this.beginDate = beginDate;
	}
	
	public Long getLastDate() {
		return lastDate;
	}
	public void setLastDate(Long lastDate) {
		this.lastDate = lastDate;
	}
	
	public Long getRecordDate() {
		return recordDate;
	}
	public void setRecordDate(Long recordDate) {
		this.recordDate = recordDate;
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
}
