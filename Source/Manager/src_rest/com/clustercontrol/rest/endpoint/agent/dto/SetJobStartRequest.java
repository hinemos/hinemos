/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.List;

import com.clustercontrol.jobmanagement.bean.RunResultInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(to = RunResultInfo.class)
public class SetJobStartRequest extends AgentRequestDto {

	// ---- from RunInfo

	//    private String facilityId;
	//    private String sessionId;
	//    private String jobunitId;
	//    private String jobId;
	private Integer commandType;
	private String command;
	private Boolean specifyUser;
	private String user;
	private Integer stopType;
	private String publicKey;
	private String checkSum;
	private List<AgtJobEnvVariableInfoRequest> jobEnvVariableInfoList;

	// ---- from RunResultInfo
	private Integer status;
	private Long time;
	private List<String> fileList;
	private Integer endValue;
	private String message;
	private String errorMessage;

	public SetJobStartRequest() {
	}

	// ---- accessors

	public Integer getCommandType() {
		return commandType;
	}

	public void setCommandType(Integer commandType) {
		this.commandType = commandType;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public Boolean getSpecifyUser() {
		return specifyUser;
	}

	public void setSpecifyUser(Boolean specifyUser) {
		this.specifyUser = specifyUser;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public Integer getStopType() {
		return stopType;
	}

	public void setStopType(Integer stopType) {
		this.stopType = stopType;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getCheckSum() {
		return checkSum;
	}

	public void setCheckSum(String checkSum) {
		this.checkSum = checkSum;
	}

	public List<AgtJobEnvVariableInfoRequest> getJobEnvVariableInfoList() {
		return jobEnvVariableInfoList;
	}

	public void setJobEnvVariableInfoList(List<AgtJobEnvVariableInfoRequest> jobEnvVariableInfoList) {
		this.jobEnvVariableInfoList = jobEnvVariableInfoList;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public List<String> getFileList() {
		return fileList;
	}

	public void setFileList(List<String> fileList) {
		this.fileList = fileList;
	}

	public Integer getEndValue() {
		return endValue;
	}

	public void setEndValue(Integer endValue) {
		this.endValue = endValue;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
