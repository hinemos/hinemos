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
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.RpaJobReturnCodeConditionEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.RpaScreenshotTriggerTypeEnum;

@RestBeanConvertAssertion(from = RunInstructionInfo.class)
public class AgtRunInstructionInfoResponse {

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
	private List<AgtJobEnvVariableInfoResponse> jobEnvVariableInfoList;
	private AgtJobOutputInfoResponse normalJobOutputInfo;
	private AgtJobOutputInfoResponse errorJobOutputInfo;

	// ---- from RunInstructionInfo
	private String runType;
	private String inputFile;
	private String filePath;
	private AgtRunInstructionFileCheckInfoResponse runInstructionFileCheckInfo;
	private String rpaLogDirectory;
	private String rpaLogFileName;
	private String rpaLogFileEncoding;
	private String rpaLogFileReturnCode;
	private String rpaLogPatternHead; 
	private String rpaLogPatternTail; 
	private Integer rpaLogMaxBytes;  
	private Integer rpaDefaultEndValue;
	private Integer rpaLoginWaitMills;
	@RestBeanConvertEnum
	private RpaScreenshotTriggerTypeEnum rpaScreenshotTriggerType;
	private String rpaScreenshotEndValue;
	@RestBeanConvertEnum
	private RpaJobReturnCodeConditionEnum rpaScreenshotEndValueCondition;
	private List<AgtRpaJobEndValueConditionInfoResponse> rpaEndValueConditionInfoList;
	private Integer rpaNormalEndValueFrom;
	private Integer rpaNormalEndValueTo;
	private Integer rpaWarnEndValueFrom;
	private Integer rpaWarnEndValueTo;
	private String rpaExeName;
	private AgtRpaJobRoboRunInfoResponse rpaRoboRunInfo;

	public AgtRunInstructionInfoResponse() {
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

	public List<AgtJobEnvVariableInfoResponse> getJobEnvVariableInfoList() {
		return jobEnvVariableInfoList;
	}

	public void setJobEnvVariableInfoList(List<AgtJobEnvVariableInfoResponse> jobEnvVariableInfoList) {
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

	public AgtJobOutputInfoResponse getNormalJobOutputInfo() {
		return normalJobOutputInfo;
	}

	public void setNormalJobOutputInfo(AgtJobOutputInfoResponse normalJobOutputInfo) {
		this.normalJobOutputInfo = normalJobOutputInfo;
	}

	public AgtJobOutputInfoResponse getErrorJobOutputInfo() {
		return errorJobOutputInfo;
	}

	public void setErrorJobOutputInfo(AgtJobOutputInfoResponse errorJobOutputInfo) {
		this.errorJobOutputInfo = errorJobOutputInfo;
	}

	public AgtRunInstructionFileCheckInfoResponse getRunInstructionFileCheckInfo() {
		return runInstructionFileCheckInfo;
	}

	public void setRunInstructionFileCheckInfo(AgtRunInstructionFileCheckInfoResponse runInstructionFileCheckInfo) {
		this.runInstructionFileCheckInfo = runInstructionFileCheckInfo;
	}

	public String getRpaLogDirectory() {
		return rpaLogDirectory;
	}

	public void setRpaLogDirectory(String rpaLogDirectory) {
		this.rpaLogDirectory = rpaLogDirectory;
	}

	public String getRpaLogFileName() {
		return rpaLogFileName;
	}

	public void setRpaLogFileName(String rpaLogFileName) {
		this.rpaLogFileName = rpaLogFileName;
	}

	public String getRpaLogFileEncoding() {
		return rpaLogFileEncoding;
	}

	public void setRpaLogFileEncoding(String rpaLogFileEncoding) {
		this.rpaLogFileEncoding = rpaLogFileEncoding;
	}

	public String getRpaLogFileReturnCode() {
		return rpaLogFileReturnCode;
	}

	public void setRpaLogFileReturnCode(String rpaLogFileReturnCode) {
		this.rpaLogFileReturnCode = rpaLogFileReturnCode;
	}

	public String getRpaLogPatternHead() {
		return rpaLogPatternHead;
	}

	public void setRpaLogPatternHead(String rpaLogPatternHead) {
		this.rpaLogPatternHead = rpaLogPatternHead;
	}

	public String getRpaLogPatternTail() {
		return rpaLogPatternTail;
	}

	public void setRpaLogPatternTail(String rpaLogPatternTail) {
		this.rpaLogPatternTail = rpaLogPatternTail;
	}

	public Integer getRpaLogMaxBytes() {
		return rpaLogMaxBytes;
	}

	public void setRpaLogMaxBytes(Integer rpaLogMaxBytes) {
		this.rpaLogMaxBytes = rpaLogMaxBytes;
	}

	public Integer getRpaDefaultEndValue() {
		return rpaDefaultEndValue;
	}

	public void setRpaDefaultEndValue(Integer rpaDefaultEndValue) {
		this.rpaDefaultEndValue = rpaDefaultEndValue;
	}

	public Integer getRpaLoginWaitMills() {
		return rpaLoginWaitMills;
	}

	public void setRpaLoginWaitMills(Integer rpaLoginWaitMills) {
		this.rpaLoginWaitMills = rpaLoginWaitMills;
	}

	public RpaScreenshotTriggerTypeEnum getRpaScreenshotTriggerType() {
		return rpaScreenshotTriggerType;
	}

	public void setRpaScreenshotTriggerType(RpaScreenshotTriggerTypeEnum rpaScreenshotTriggerType) {
		this.rpaScreenshotTriggerType = rpaScreenshotTriggerType;
	}

	public String getRpaScreenshotEndValue() {
		return rpaScreenshotEndValue;
	}

	public void setRpaScreenshotEndValue(String rpaScreenshotEndValue) {
		this.rpaScreenshotEndValue = rpaScreenshotEndValue;
	}

	public RpaJobReturnCodeConditionEnum getRpaScreenshotEndValueCondition() {
		return rpaScreenshotEndValueCondition;
	}

	public void setRpaScreenshotEndValueCondition(RpaJobReturnCodeConditionEnum rpaScreenshotEndValueCondition) {
		this.rpaScreenshotEndValueCondition = rpaScreenshotEndValueCondition;
	}

	public List<AgtRpaJobEndValueConditionInfoResponse> getRpaEndValueConditionInfoList() {
		return rpaEndValueConditionInfoList;
	}

	public void setRpaEndValueConditionInfoList(List<AgtRpaJobEndValueConditionInfoResponse> rpaEndValueConditionInfoList) {
		this.rpaEndValueConditionInfoList = rpaEndValueConditionInfoList;
	}

	public Integer getRpaNormalEndValueFrom() {
		return rpaNormalEndValueFrom;
	}

	public void setRpaNormalEndValueFrom(Integer rpaNormalEndValueFrom) {
		this.rpaNormalEndValueFrom = rpaNormalEndValueFrom;
	}

	public Integer getRpaNormalEndValueTo() {
		return rpaNormalEndValueTo;
	}

	public void setRpaNormalEndValueTo(Integer rpaNormalEndValueTo) {
		this.rpaNormalEndValueTo = rpaNormalEndValueTo;
	}

	public Integer getRpaWarnEndValueFrom() {
		return rpaWarnEndValueFrom;
	}

	public void setRpaWarnEndValueFrom(Integer rpaWarnEndValueFrom) {
		this.rpaWarnEndValueFrom = rpaWarnEndValueFrom;
	}

	public Integer getRpaWarnEndValueTo() {
		return rpaWarnEndValueTo;
	}

	public void setRpaWarnEndValueTo(Integer rpaWarnEndValueTo) {
		this.rpaWarnEndValueTo = rpaWarnEndValueTo;
	}

	public String getRpaExeName() {
		return rpaExeName;
	}

	public void setRpaExeName(String rpaExeName) {
		this.rpaExeName = rpaExeName;
	}

	public AgtRpaJobRoboRunInfoResponse getRpaRoboRunInfo() {
		return rpaRoboRunInfo;
	}

	public void setRpaRoboRunInfo(AgtRpaJobRoboRunInfoResponse rpaRoboRunInfo) {
		this.rpaRoboRunInfo = rpaRoboRunInfo;
	}
}
