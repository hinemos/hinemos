/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import com.clustercontrol.xcloud.factory.ICloudOption.PlatformServiceStatus;

@MappedSuperclass
public abstract class ServiceConditionEntity extends EntityBase {
	private String serviceId;
	private String serviceName;
	private PlatformServiceStatus status;
	
	private String message;
	private String detail;

	private Long beginDate;
	private Long lastDate;
	private Long recordDate;

	@Id
	@Column(name="service_id")
	public String getServiceId() {
		return this.serviceId;
	}
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	
	@Column(name="service_name")
	public String getServiceName() {
		return this.serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	
	@Column(name="status")
	@Enumerated(EnumType.STRING)
	public PlatformServiceStatus getStatus() {
		return status;
	}
	public void setStatus(PlatformServiceStatus status) {
		this.status = status;
	}
	
	@Column(name="begin_date")
	public Long getBeginDate() {
		return this.beginDate;
	}
	public void setBeginDate(Long beginDate) {
		this.beginDate = beginDate;
	}
	
	@Column(name="last_date")
	public Long getLastDate() {
		return this.lastDate;
	}
	public void setLastDate(Long lastDate) {
		this.lastDate = lastDate;
	}
	
	@Column(name="message")
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

	@Column(name="detail")
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}

	@Column(name="record_date")
	public Long getRecordDate() {
		return recordDate;
	}
	public void setRecordDate(Long recordDate) {
		this.recordDate = recordDate;
	}
}
