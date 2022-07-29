/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.Map;

import com.clustercontrol.custom.bean.CommandResultDTO;
import com.clustercontrol.custom.bean.Type;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIgnore;

@RestBeanConvertAssertion(to = CommandResultDTO.class)
public class AgtCustomResultDTORequest extends AgentRequestDto {

	// ---- from CommandResultDTO
	private String monitorId;
	private String facilityId;
	private Boolean timeout;
	private String command;
	private String user;
	private Integer exitCode;
	private String stdout;
	private String stderr;
	// private Map<String, Object> results;       // 変換が必要
	// private Map<Integer, String> invalidLines; // 変換が必要
	private Long collectDate;
	private Long executeDate;
	private Long exitDate;
	private AgtRunInstructionInfoRequest runInstructionInfo;
	private Type type;

	// ---- alternatives
	@RestBeanConvertIgnore
	private Map<String, String> results;
	@RestBeanConvertIgnore
	private Map<String, String> invalidLines;

	public AgtCustomResultDTORequest() {
	}

	// ---- accessors

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

	public Boolean getTimeout() {
		return timeout;
	}

	public void setTimeout(Boolean timeout) {
		this.timeout = timeout;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public Integer getExitCode() {
		return exitCode;
	}

	public void setExitCode(Integer exitCode) {
		this.exitCode = exitCode;
	}

	public String getStdout() {
		return stdout;
	}

	public void setStdout(String stdout) {
		this.stdout = stdout;
	}

	public String getStderr() {
		return stderr;
	}

	public void setStderr(String stderr) {
		this.stderr = stderr;
	}

	public Map<String, String> getResults() {
		return results;
	}

	public void setResults(Map<String, String> results) {
		this.results = results;
	}

	public Map<String, String> getInvalidLines() {
		return invalidLines;
	}

	public void setInvalidLines(Map<String, String> invalidLines) {
		this.invalidLines = invalidLines;
	}

	public Long getCollectDate() {
		return collectDate;
	}

	public void setCollectDate(Long collectDate) {
		this.collectDate = collectDate;
	}

	public Long getExecuteDate() {
		return executeDate;
	}

	public void setExecuteDate(Long executeDate) {
		this.executeDate = executeDate;
	}

	public Long getExitDate() {
		return exitDate;
	}

	public void setExitDate(Long exitDate) {
		this.exitDate = exitDate;
	}

	public AgtRunInstructionInfoRequest getRunInstructionInfo() {
		return runInstructionInfo;
	}

	public void setRunInstructionInfo(AgtRunInstructionInfoRequest runInstructionInfo) {
		this.runInstructionInfo = runInstructionInfo;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

}
