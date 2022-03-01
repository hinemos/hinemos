/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.List;

import com.clustercontrol.custom.bean.CommandExecuteDTO;
import com.clustercontrol.custom.bean.Type;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(from = CommandExecuteDTO.class)
public class AgtCustomMonitorInfoResponse {

	// ---- from CommandExecuteDTO
	private String monitorId;
	private String facilityId;
	private Boolean specifyUser;
	private String effectiveUser;
	private String command;
	private Integer timeout;
	private Integer interval;
	private AgtCalendarInfoResponse calendar;
	private AgtRunInstructionInfoResponse runInstructionInfo;
	private Type type;
	private List<AgtCustomMonitorVarsInfoResponse> variables;

	public AgtCustomMonitorInfoResponse() {
	}

	// ---- settes and getters

	public String getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public Boolean getSpecifyUser() {
		return specifyUser;
	}

	public void setSpecifyUser(Boolean specifyUser) {
		this.specifyUser = specifyUser;
	}

	public String getEffectiveUser() {
		return effectiveUser;
	}

	public void setEffectiveUser(String effectiveUser) {
		this.effectiveUser = effectiveUser;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public Integer getTimeout() {
		return timeout;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	public Integer getInterval() {
		return interval;
	}

	public void setInterval(Integer interval) {
		this.interval = interval;
	}

	public AgtCalendarInfoResponse getCalendar() {
		return calendar;
	}

	public void setCalendar(AgtCalendarInfoResponse calendar) {
		this.calendar = calendar;
	}

	public AgtRunInstructionInfoResponse getRunInstructionInfo() {
		return runInstructionInfo;
	}

	public void setRunInstructionInfo(AgtRunInstructionInfoResponse runInstructionInfo) {
		this.runInstructionInfo = runInstructionInfo;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public List<AgtCustomMonitorVarsInfoResponse> getVariables() {
		return variables;
	}

	public void setVariables(List<AgtCustomMonitorVarsInfoResponse> variables) {
		this.variables = variables;
	}

}
