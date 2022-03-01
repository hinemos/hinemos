/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.CommandRetryEndStatusEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ProcessingMethodEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.StopTypeEnum;

public class JobCommandInfoResponse {
	/** ファシリティID */
	private String facilityID;

	/** スコープ */
	@RestPartiallyTransrateTarget
	private String scope;

	/** スコープ処理 */
	@RestBeanConvertEnum
	private ProcessingMethodEnum processingMethod;

	/** マネージャから配布 */
	private Boolean managerDistribution = false;
	
	/** スクリプト名 */
	private String scriptName;
	
	/** スクリプトエンコーディング */
	private String scriptEncoding;
	
	/** スクリプト */
	private String scriptContent;
	
	/** 起動コマンド */
	private String startCommand;

	/** コマンド停止方式 */
	@RestBeanConvertEnum
	private StopTypeEnum stopType;

	/** 停止コマンド */
	private String stopCommand;

	/** ユーザ種別 */
	private Boolean specifyUser = false;

	/** 実効ユーザ */
	private String user;

	/** リトライ回数 */
	private Integer messageRetry;

	/** コマンド実行失敗時終了フラグ */
	private Boolean messageRetryEndFlg = false;

	/** コマンド実行失敗時終了値 */
	private Integer messageRetryEndValue = 0;

	/** 繰り返し実行フラグ */
	private Boolean commandRetryFlg = false;

	/** 繰り返し実行回数 */
	private Integer commandRetry;
	
	/** 繰り返し完了状態 */
	@RestBeanConvertEnum
	private CommandRetryEndStatusEnum commandRetryEndStatus;

	/** 標準出力のファイル出力情報 - 標準出力 */
	private JobOutputInfoResponse normalJobOutputInfo;

	/** 標準出力のファイル出力情報 - 標準エラー出力 */
	private JobOutputInfoResponse errorJobOutputInfo;

	/** ランタイムジョブ変数詳細情報 */
	private ArrayList<JobCommandParamResponse> jobCommandParamList;

	/** 環境変数 */
	private List<JobEnvVariableInfoResponse> envVariable;

	public JobCommandInfoResponse() {
	}

	public String getFacilityID() {
		return facilityID;
	}

	public void setFacilityID(String facilityID) {
		this.facilityID = facilityID;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public ProcessingMethodEnum getProcessingMethod() {
		return processingMethod;
	}

	public void setProcessingMethod(ProcessingMethodEnum processingMethod) {
		this.processingMethod = processingMethod;
	}

	public Boolean getManagerDistribution() {
		return managerDistribution;
	}

	public void setManagerDistribution(Boolean managerDistribution) {
		this.managerDistribution = managerDistribution;
	}

	public String getScriptName() {
		return scriptName;
	}

	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}

	public String getScriptEncoding() {
		return scriptEncoding;
	}

	public void setScriptEncoding(String scriptEncoding) {
		this.scriptEncoding = scriptEncoding;
	}

	public String getScriptContent() {
		return scriptContent;
	}

	public void setScriptContent(String scriptContent) {
		this.scriptContent = scriptContent;
	}

	public String getStartCommand() {
		return startCommand;
	}

	public void setStartCommand(String startCommand) {
		this.startCommand = startCommand;
	}

	public StopTypeEnum getStopType() {
		return stopType;
	}

	public void setStopType(StopTypeEnum stopType) {
		this.stopType = stopType;
	}

	public String getStopCommand() {
		return stopCommand;
	}

	public void setStopCommand(String stopCommand) {
		this.stopCommand = stopCommand;
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

	public Integer getMessageRetry() {
		return messageRetry;
	}

	public void setMessageRetry(Integer messageRetry) {
		this.messageRetry = messageRetry;
	}

	public Boolean getMessageRetryEndFlg() {
		return messageRetryEndFlg;
	}

	public void setMessageRetryEndFlg(Boolean messageRetryEndFlg) {
		this.messageRetryEndFlg = messageRetryEndFlg;
	}

	public Integer getMessageRetryEndValue() {
		return messageRetryEndValue;
	}

	public void setMessageRetryEndValue(Integer messageRetryEndValue) {
		this.messageRetryEndValue = messageRetryEndValue;
	}

	public Boolean getCommandRetryFlg() {
		return commandRetryFlg;
	}

	public void setCommandRetryFlg(Boolean commandRetryFlg) {
		this.commandRetryFlg = commandRetryFlg;
	}

	public Integer getCommandRetry() {
		return commandRetry;
	}

	public void setCommandRetry(Integer commandRetry) {
		this.commandRetry = commandRetry;
	}

	public CommandRetryEndStatusEnum getCommandRetryEndStatus() {
		return commandRetryEndStatus;
	}

	public void setCommandRetryEndStatus(CommandRetryEndStatusEnum commandRetryEndStatus) {
		this.commandRetryEndStatus = commandRetryEndStatus;
	}

	public JobOutputInfoResponse getNormalJobOutputInfo() {
		return normalJobOutputInfo;
	}

	public void setNormalJobOutputInfo(JobOutputInfoResponse normalJobOutputInfo) {
		this.normalJobOutputInfo = normalJobOutputInfo;
	}

	public JobOutputInfoResponse getErrorJobOutputInfo() {
		return errorJobOutputInfo;
	}

	public void setErrorJobOutputInfo(JobOutputInfoResponse errorJobOutputInfo) {
		this.errorJobOutputInfo = errorJobOutputInfo;
	}

	public ArrayList<JobCommandParamResponse> getJobCommandParamList() {
		return jobCommandParamList;
	}

	public void setJobCommandParamList(ArrayList<JobCommandParamResponse> jobCommandParamList) {
		this.jobCommandParamList = jobCommandParamList;
	}

	public List<JobEnvVariableInfoResponse> getEnvVariable() {
		return envVariable;
	}

	public void setEnvVariable(List<JobEnvVariableInfoResponse> envVariable) {
		this.envVariable = envVariable;
	}



}
