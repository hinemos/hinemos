/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.endpoint.monitorsetting.dto.enumtype.PortServiceProtocolEnum;

public class PortCheckInfoResponse {
	private Integer portNo;
	private Integer runCount;
	private Integer runInterval;
	private Integer timeout;
	@RestBeanConvertEnum
	private PortServiceProtocolEnum serviceId;

	public PortCheckInfoResponse() {
	}

	public Integer getPortNo() {
		return portNo;
	}
	public void setPortNo(Integer portNo) {
		this.portNo = portNo;
	}
	public Integer getRunCount() {
		return runCount;
	}
	public void setRunCount(Integer runCount) {
		this.runCount = runCount;
	}
	public Integer getRunInterval() {
		return runInterval;
	}
	public void setRunInterval(Integer runInterval) {
		this.runInterval = runInterval;
	}
	public Integer getTimeout() {
		return timeout;
	}
	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}
	public PortServiceProtocolEnum getServiceId() {
		return serviceId;
	}
	public void setServiceId(PortServiceProtocolEnum serviceId) {
		this.serviceId = serviceId;
	}
	@Override
	public String toString() {
		return "PortCheckInfoResponse [portNo=" + portNo + ", runCount=" + runCount + ", runInterval=" + runInterval
				+ ", timeout=" + timeout + ", serviceId=" + serviceId + "]";
	}

	
}