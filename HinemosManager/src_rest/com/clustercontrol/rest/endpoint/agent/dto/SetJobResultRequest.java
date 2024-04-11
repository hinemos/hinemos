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
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.RpaJobErrorTypeEnum;
import com.clustercontrol.util.XMLUtil;

@RestBeanConvertAssertion(to = RunResultInfo.class)
public class SetJobResultRequest extends AgentRequestDto {

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
	private SetJobResultFileCheckRequest runResultFileCheckInfo;
	@RestBeanConvertEnum
	private RpaJobErrorTypeEnum rpaJobErrorType;
	private Integer rpaJobReturnCode;
	private String rpaJobLogfileName;
	private String rpaJobLogMessage;
	private AgtRpaJobEndValueConditionInfoRequest rpaJobEndValueConditionInfo;

	private SetJobOutputResultRequest jobOutput;
	
	public SetJobResultRequest() {
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
		// NULL等が含まれる場合があるため変換する
		this.message = XMLUtil.ignoreInvalidString(message);
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		// NULL等が含まれる場合があるため変換する
		this.errorMessage = XMLUtil.ignoreInvalidString(errorMessage);
	}

	public SetJobOutputResultRequest getJobOutput() {
		return jobOutput;
	}

	public void setJobOutput(SetJobOutputResultRequest jobOutput) {
		this.jobOutput = jobOutput;
	}

	public SetJobResultFileCheckRequest getRunResultFileCheckInfo() {
		return runResultFileCheckInfo;
	}

	public void setRunResultFileCheckInfo(SetJobResultFileCheckRequest runResultFileCheckInfo) {
		this.runResultFileCheckInfo = runResultFileCheckInfo;
	}

	public RpaJobErrorTypeEnum getRpaJobErrorType() {
		return rpaJobErrorType;
	}

	public void setRpaJobErrorType(RpaJobErrorTypeEnum rpaJobErrorType) {
		this.rpaJobErrorType = rpaJobErrorType;
	}

	public Integer getRpaJobReturnCode() {
		return rpaJobReturnCode;
	}

	public void setRpaJobReturnCode(Integer rpaJobReturnCode) {
		this.rpaJobReturnCode = rpaJobReturnCode;
	}

	public String getRpaJobLogfileName() {
		return rpaJobLogfileName;
	}

	public void setRpaJobLogfileName(String rpaJobLogfileName) {
		this.rpaJobLogfileName = rpaJobLogfileName;
	}

	public String getRpaJobLogMessage() {
		return rpaJobLogMessage;
	}

	public void setRpaJobLogMessage(String rpaJobLogMessage) {
		this.rpaJobLogMessage = rpaJobLogMessage;
	}

	public AgtRpaJobEndValueConditionInfoRequest getRpaJobEndValueConditionInfo() {
		return rpaJobEndValueConditionInfo;
	}

	public void setRpaJobEndValueConditionInfo(AgtRpaJobEndValueConditionInfoRequest rpaJobEndValueConditionInfo) {
		this.rpaJobEndValueConditionInfo = rpaJobEndValueConditionInfo;
	}

}
