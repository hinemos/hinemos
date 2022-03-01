/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * The persistent class for the cc_rest_agent_request database table.
 *
 */
@Entity
@Table(name = "cc_rest_agent_request", schema = "log")
public class RestAgentRequestEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private String requestId;
	private String agentId;
	private String systemFunction;
	private String resourceMethod;
	private Long regDate;

	@Deprecated
	public RestAgentRequestEntity() {
	}

	public RestAgentRequestEntity(String requestId) {
		this.setRequestId(requestId);
	}

	@Id
	@Column(name = "request_id")
	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	@Column(name = "agent_id")
	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	@Column(name = "system_function")
	public String getSystemFunction() {
		return systemFunction;
	}

	public void setSystemFunction(String systemFunction) {
		this.systemFunction = systemFunction;
	}

	@Column(name = "resource_method")
	public String getResourceMethod() {
		return resourceMethod;
	}

	public void setResourceMethod(String resourceMethod) {
		this.resourceMethod = resourceMethod;
	}

	@Column(name = "reg_date")
	public Long getRegDate() {
		return regDate;
	}

	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}

}
