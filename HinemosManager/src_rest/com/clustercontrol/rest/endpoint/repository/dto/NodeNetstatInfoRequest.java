/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.repository.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.repository.model.NodeNetstatInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIdClassSet;
import com.clustercontrol.rest.dto.RequestDto;

@RestBeanConvertIdClassSet(infoClass = NodeNetstatInfo.class, idName = "id")
public class NodeNetstatInfoRequest implements RequestDto {

	private String protocol;
	private String localIpAddress;
	private String localPort;
	private String foreignIpAddress;
	private String foreignPort;
	private String processName;
	private Integer pid;

	private String status;

	public NodeNetstatInfoRequest() {
	}

	public String getStatus() {
		return status;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getLocalIpAddress() {
		return localIpAddress;
	}

	public void setLocalIpAddress(String localIpAddress) {
		this.localIpAddress = localIpAddress;
	}

	public String getLocalPort() {
		return localPort;
	}

	public void setLocalPort(String localPort) {
		this.localPort = localPort;
	}

	public String getForeignIpAddress() {
		return foreignIpAddress;
	}

	public void setForeignIpAddress(String foreignIpAddress) {
		this.foreignIpAddress = foreignIpAddress;
	}

	public String getForeignPort() {
		return foreignPort;
	}

	public void setForeignPort(String foreignPort) {
		this.foreignPort = foreignPort;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public Integer getPid() {
		return pid;
	}

	public void setPid(Integer pid) {
		this.pid = pid;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
