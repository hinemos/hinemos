/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.List;

import com.clustercontrol.jobmanagement.bean.RunInstructionInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(to = RunInstructionInfo.class)
public class AgtRunInstructionInfoRequest extends AgentRequestDto {

	// ---- from RunInfo
	private String facilityId;
	private String sessionId;
	private String jobunitId;
	private String jobId;
	private Integer commandType;
	private String command;
	private Boolean specifyUser;
	private String user;
	private Integer stopType;
	private String publicKey;
	private String checkSum;
	private List<AgtJobEnvVariableInfoRequest> jobEnvVariableInfoList;

	// ---- from RunInstructionInfo
	private String runType;
	private String inputFile;
	private String filePath;
	private AgtRunInstructionFileCheckInfoRequest runInstructionFileCheckInfo;

	public AgtRunInstructionInfoRequest() {
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getJobunitId() {
		return jobunitId;
	}

	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

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

	public String getRunType() {
		return runType;
	}

	public void setRunType(String runType) {
		this.runType = runType;
	}

	public String getInputFile() {
		return inputFile;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public AgtRunInstructionFileCheckInfoRequest getRunInstructionFileCheckInfo() {
		return runInstructionFileCheckInfo;
	}

	public void setRunInstructionFileCheckInfo(AgtRunInstructionFileCheckInfoRequest runInstructionFileCheckInfo) {
		this.runInstructionFileCheckInfo = runInstructionFileCheckInfo;
	}
}
