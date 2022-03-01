/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.jobmanagement.bean.RpaJobInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.CommandRetryEndStatusEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.PrioritySelectEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ProcessingMethodEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.RpaJobReturnCodeConditionEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.RpaJobTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.RpaStopTypeEnum;

@RestBeanConvertAssertion(to = RpaJobInfo.class)
public class JobRpaInfoRequest implements RequestDto {
	
	/** RPAジョブ種別 */
	@RestBeanConvertEnum
	private RpaJobTypeEnum rpaJobType;

	// 直接実行
	/** ファシリティID */
	private String facilityID;

	/** スコープ */
	private String scope;

	/** スコープ処理 */
	@RestBeanConvertEnum
	private ProcessingMethodEnum processingMethod;

	/** RPAツールID */
	private String rpaToolId;

	/** 実行ファイルパス */
	private String rpaExeFilepath;

	/** シナリオファイルパス */
	private String rpaScenarioFilepath;

	/** 終了値判定用ログファイルディレクトリ */
	private String rpaLogDirectory;

	/** 終了値判定用ログファイル名 */
	private String rpaLogFileName;

	/** 終了値判定用ログファイルエンコーディング */
	private String rpaLogEncoding;

	/** 終了値判定用ログファイル区切り文字 */
	private String rpaLogReturnCode;

	/** 終了値判定用ログファイル先頭パターン */
	private String rpaLogPatternHead;

	/** 終了値判定用ログファイル終端パターン */
	private String rpaLogPatternTail;

	/** 終了値判定用ログファイル最大読み取り文字数 */
	private Integer rpaLogMaxBytes;

	/** いずれの判定にも一致しない場合の終了値 */
	private Integer rpaDefaultEndValue;

	/** シナリオ実行前後でOSへログイン・ログアウトする */
	private Boolean rpaLoginFlg;

	/** ログインユーザID */
	private String rpaLoginUserId;

	/** ログインパスワード */
	private String rpaLoginPassword;

	/** ログインリトライ回数 */
	private Integer rpaLoginRetry;

	/** ログインできない場合の終了値 */
	private Integer rpaLoginEndValue;

	/** ログインの解像度 */
	private String rpaLoginResolution;

	/** 異常終了時もログアウトする */
	private Boolean rpaLogoutFlg;

	/** 終了遅延発生時にスクリーンショットを取得する */
	private Boolean rpaScreenshotEndDelayFlg;

	/** 特定の終了値の場合にスクリーンショットを取得する */
	private Boolean rpaScreenshotEndValueFlg;

	/** スクリーンショットを取得する終了値 */
	private String rpaScreenshotEndValue;

	/** スクリーンショットを取得する終了値判定条件 */
	@RestBeanConvertEnum
	private RpaJobReturnCodeConditionEnum rpaScreenshotEndValueCondition;

	/** リトライ回数 */
	private Integer messageRetry;

	/** コマンド実行失敗時終了フラグ */
	private Boolean messageRetryEndFlg;

	/** コマンド実行失敗時終了値 */
	private Integer messageRetryEndValue;

	/** 繰り返し実行フラグ */
	private Boolean commandRetryFlg;

	/** 繰り返し実行回数 */
	private Integer commandRetry;

	/** 繰り返し完了状態 */
	@RestBeanConvertEnum
	private CommandRetryEndStatusEnum commandRetryEndStatus;

	/** 実行オプション */
	private List<JobRpaOptionInfoRequest> rpaJobOptionInfos;

	/** 終了値判定条件 */
	private List<JobRpaEndValueConditionInfoRequest> rpaJobEndValueConditionInfos;

	/** ログインされていない場合 通知 */
	private Boolean rpaNotLoginNotify;

	/** ログインされていない場合 通知重要度 */
	@RestBeanConvertEnum
	private PrioritySelectEnum rpaNotLoginNotifyPriority;

	/** ログインされていない場合 終了値 */
	private Integer rpaNotLoginEndValue;

	/** RPAツールが既に動作している場合 通知 */
	private Boolean rpaAlreadyRunningNotify;

	/** RPAツールが既に動作している場合 通知重要度 */
	@RestBeanConvertEnum
	private PrioritySelectEnum rpaAlreadyRunningNotifyPriority;

	/** RPAツールが既に動作している場合 終了値 */
	private Integer rpaAlreadyRunningEndValue;

	/** RPAツールが異常終了した場合 通知 */
	private Boolean rpaAbnormalExitNotify;

	/** RPAツールが異常終了した場合 通知重要度 */
	@RestBeanConvertEnum
	private PrioritySelectEnum rpaAbnormalExitNotifyPriority;

	/** RPAツールが異常終了した場合 終了値 */
	private Integer rpaAbnormalExitEndValue;
	
	// 間接実行
	/** RPAスコープID */
	private String rpaScopeId;
	
	/** RPA管理ツール 実行種別 */
	private Integer rpaRunType;

	/** 起動パラメータ */
	private List<JobRpaRunParamInfoRequest> rpaJobRunParamInfos;
	
	/** シナリオ入力パラメータ */
	private String rpaScenarioParam;

	/** 停止種別 */
	@RestBeanConvertEnum
	private RpaStopTypeEnum rpaStopType;

	/** 停止方法 */
	private Integer rpaStopMode;

	/** シナリオ実行 コネクションタイムアウト */
	private Integer rpaRunConnectTimeout;

	/** シナリオ実行 リクエストタイムアウト */
	private Integer rpaRunRequestTimeout;

	/** シナリオ実行 実行できない場合に終了する */
	private Boolean rpaRunEndFlg;

	/** シナリオ実行 リトライ回数 */
	private Integer rpaRunRetry;

	/** シナリオ実行 終了値 */
	private Integer rpaRunEndValue;

	/** シナリオ実行結果確認 コネクションタイムアウト */
	private Integer rpaCheckConnectTimeout;

	/** シナリオ実行結果確認 リクエストタイムアウト */
	private Integer rpaCheckRequestTimeout;

	/** シナリオ実行結果確認 実行できない場合に終了する */
	private Boolean rpaCheckEndFlg;

	/** シナリオ実行結果確認 リトライ回数 */
	private Integer rpaCheckRetry;

	/** シナリオ実行結果確認 終了値 */
	private Integer rpaCheckEndValue;

	/** 終了値判定条件 */
	private List<JobRpaCheckEndValueInfoRequest> rpaJobCheckEndValueInfos;

	public JobRpaInfoRequest() {
	}

	/**
	 * @return the rpaJobType
	 */
	public RpaJobTypeEnum getRpaJobType() {
		return rpaJobType;
	}

	/**
	 * @param rpaJobType the rpaJobType to set
	 */
	public void setRpaJobType(RpaJobTypeEnum rpaJobType) {
		this.rpaJobType = rpaJobType;
	}

	/**
	 * @return the facilityID
	 */
	public String getFacilityID() {
		return facilityID;
	}

	/**
	 * @param facilityID
	 *            the facilityID to set
	 */
	public void setFacilityID(String facilityID) {
		this.facilityID = facilityID;
	}

	/**
	 * @return the scope
	 */
	public String getScope() {
		return scope;
	}

	/**
	 * @param scope
	 *            the scope to set
	 */
	public void setScope(String scope) {
		this.scope = scope;
	}

	/**
	 * @return the processingMethod
	 */
	public ProcessingMethodEnum getProcessingMethod() {
		return processingMethod;
	}

	/**
	 * @param processingMethod
	 *            the processingMethod to set
	 */
	public void setProcessingMethod(ProcessingMethodEnum processingMethod) {
		this.processingMethod = processingMethod;
	}

	/**
	 * @return the rpaToolId
	 */
	public String getRpaToolId() {
		return rpaToolId;
	}

	/**
	 * @param rpaToolId
	 *            the rpaToolId to set
	 */
	public void setRpaToolId(String rpaToolId) {
		this.rpaToolId = rpaToolId;
	}

	/**
	 * @return the rpaExeFilepath
	 */
	public String getRpaExeFilepath() {
		return rpaExeFilepath;
	}

	/**
	 * @param rpaExeFilepath
	 *            the rpaExeFilepath to set
	 */
	public void setRpaExeFilepath(String rpaExeFilepath) {
		this.rpaExeFilepath = rpaExeFilepath;
	}

	/**
	 * @return the rpaScenarioFilepath
	 */
	public String getRpaScenarioFilepath() {
		return rpaScenarioFilepath;
	}

	/**
	 * @param rpaScenarioFilepath
	 *            the rpaScenarioFilepath to set
	 */
	public void setRpaScenarioFilepath(String rpaScenarioFilepath) {
		this.rpaScenarioFilepath = rpaScenarioFilepath;
	}

	/**
	 * @return the rpaLogDirectory
	 */
	public String getRpaLogDirectory() {
		return rpaLogDirectory;
	}

	/**
	 * @param rpaLogDirectory
	 *            the rpaLogDirectory to set
	 */
	public void setRpaLogDirectory(String rpaLogDirectory) {
		this.rpaLogDirectory = rpaLogDirectory;
	}

	/**
	 * @return the rpaLogFileName
	 */
	public String getRpaLogFileName() {
		return rpaLogFileName;
	}

	/**
	 * @param rpaLogFileName
	 *            the rpaLogFileName to set
	 */
	public void setRpaLogFileName(String rpaLogFileName) {
		this.rpaLogFileName = rpaLogFileName;
	}

	/**
	 * @return the rpaLogEncoding
	 */
	public String getRpaLogEncoding() {
		return rpaLogEncoding;
	}

	/**
	 * @param rpaLogEncoding
	 *            the rpaLogEncoding to set
	 */
	public void setRpaLogEncoding(String rpaLogEncoding) {
		this.rpaLogEncoding = rpaLogEncoding;
	}

	/**
	 * @return the rpaLogReturnCode
	 */
	public String getRpaLogReturnCode() {
		return rpaLogReturnCode;
	}

	/**
	 * @param rpaLogReturnCode
	 *            the rpaLogReturnCode to set
	 */
	public void setRpaLogReturnCode(String rpaLogReturnCode) {
		this.rpaLogReturnCode = rpaLogReturnCode;
	}

	/**
	 * @return the rpaLogPatternHead
	 */
	public String getRpaLogPatternHead() {
		return rpaLogPatternHead;
	}

	/**
	 * @param rpaLogPatternHead
	 *            the rpaLogPatternHead to set
	 */
	public void setRpaLogPatternHead(String rpaLogPatternHead) {
		this.rpaLogPatternHead = rpaLogPatternHead;
	}

	/**
	 * @return the rpaLogPatternTail
	 */
	public String getRpaLogPatternTail() {
		return rpaLogPatternTail;
	}

	/**
	 * @param rpaLogPatternTail
	 *            the rpaLogPatternTail to set
	 */
	public void setRpaLogPatternTail(String rpaLogPatternTail) {
		this.rpaLogPatternTail = rpaLogPatternTail;
	}

	/**
	 * @return the rpaLogMaxBytes
	 */
	public Integer getRpaLogMaxBytes() {
		return rpaLogMaxBytes;
	}

	/**
	 * @param rpaLogMaxBytes
	 *            the rpaLogMaxBytes to set
	 */
	public void setRpaLogMaxBytes(Integer rpaLogMaxBytes) {
		this.rpaLogMaxBytes = rpaLogMaxBytes;
	}

	/**
	 * @return the rpaDefaultEndValue
	 */
	public Integer getRpaDefaultEndValue() {
		return rpaDefaultEndValue;
	}

	/**
	 * @param rpaDefaultEndValue
	 *            the rpaDefaultEndValue to set
	 */
	public void setRpaDefaultEndValue(Integer rpaDefaultEndValue) {
		this.rpaDefaultEndValue = rpaDefaultEndValue;
	}

	/**
	 * @return the rpaLoginFlg
	 */
	public Boolean getRpaLoginFlg() {
		return rpaLoginFlg;
	}

	/**
	 * @param rpaLoginFlg
	 *            the rpaLoginFlg to set
	 */
	public void setRpaLoginFlg(Boolean rpaLoginFlg) {
		this.rpaLoginFlg = rpaLoginFlg;
	}

	/**
	 * @return the rpaLoginUserId
	 */
	public String getRpaLoginUserId() {
		return rpaLoginUserId;
	}

	/**
	 * @param rpaLoginUserId
	 *            the rpaLoginUserId to set
	 */
	public void setRpaLoginUserId(String rpaLoginUserId) {
		this.rpaLoginUserId = rpaLoginUserId;
	}

	/**
	 * @return the rpaLoginPassword
	 */
	public String getRpaLoginPassword() {
		return rpaLoginPassword;
	}

	/**
	 * @param rpaLoginPassword
	 *            the rpaLoginPassword to set
	 */
	public void setRpaLoginPassword(String rpaLoginPassword) {
		this.rpaLoginPassword = rpaLoginPassword;
	}

	/**
	 * @return the rpaLoginRetry
	 */
	public Integer getRpaLoginRetry() {
		return rpaLoginRetry;
	}

	/**
	 * @param rpaLoginRetry
	 *            the rpaLoginRetry to set
	 */
	public void setRpaLoginRetry(Integer rpaLoginRetry) {
		this.rpaLoginRetry = rpaLoginRetry;
	}

	/**
	 * @return the rpaLoginEndValue
	 */
	public Integer getRpaLoginEndValue() {
		return rpaLoginEndValue;
	}

	/**
	 * @param rpaLoginEndValue
	 *            the rpaLoginEndValue to set
	 */
	public void setRpaLoginEndValue(Integer rpaLoginEndValue) {
		this.rpaLoginEndValue = rpaLoginEndValue;
	}

	/**
	 * @return the rpaLoginResolution
	 */
	public String getRpaLoginResolution() {
		return rpaLoginResolution;
	}

	/**
	 * @param rpaLoginResolution
	 *            the rpaLoginResolution to set
	 */
	public void setRpaLoginResolution(String rpaLoginResolution) {
		this.rpaLoginResolution = rpaLoginResolution;
	}

	/**
	 * @return the rpaLogoutFlg
	 */
	public Boolean getRpaLogoutFlg() {
		return rpaLogoutFlg;
	}

	/**
	 * @param rpaLogoutFlg
	 *            the rpaLogoutFlg to set
	 */
	public void setRpaLogoutFlg(Boolean rpaLogoutFlg) {
		this.rpaLogoutFlg = rpaLogoutFlg;
	}

	/**
	 * @return the rpaScreenshotEndDelayFlg
	 */
	public Boolean getRpaScreenshotEndDelayFlg() {
		return rpaScreenshotEndDelayFlg;
	}

	/**
	 * @param rpaScreenshotEndDelayFlg
	 *            the rpaScreenshotEndDelayFlg to set
	 */
	public void setRpaScreenshotEndDelayFlg(Boolean rpaScreenshotEndDelayFlg) {
		this.rpaScreenshotEndDelayFlg = rpaScreenshotEndDelayFlg;
	}

	/**
	 * @return the rpaScreenshotEndValueFlg
	 */
	public Boolean getRpaScreenshotEndValueFlg() {
		return rpaScreenshotEndValueFlg;
	}

	/**
	 * @param rpaScreenshotEndValueFlg
	 *            the rpaScreenshotEndValueFlg to set
	 */
	public void setRpaScreenshotEndValueFlg(Boolean rpaScreenshotEndValueFlg) {
		this.rpaScreenshotEndValueFlg = rpaScreenshotEndValueFlg;
	}

	/**
	 * @return the rpaScreenshotEndValue
	 */
	public String getRpaScreenshotEndValue() {
		return rpaScreenshotEndValue;
	}

	/**
	 * @param rpaScreenshotEndValue
	 *            the rpaScreenshotEndValue to set
	 */
	public void setRpaScreenshotEndValue(String rpaScreenshotEndValue) {
		this.rpaScreenshotEndValue = rpaScreenshotEndValue;
	}

	/**
	 * @return the rpaScreenshotEndValueCondition
	 */
	public RpaJobReturnCodeConditionEnum getRpaScreenshotEndValueCondition() {
		return rpaScreenshotEndValueCondition;
	}

	/**
	 * @param rpaScreenshotEndValueCondition
	 *            the rpaScreenshotEndValueCondition to set
	 */
	public void setRpaScreenshotEndValueCondition(RpaJobReturnCodeConditionEnum rpaScreenshotEndValueCondition) {
		this.rpaScreenshotEndValueCondition = rpaScreenshotEndValueCondition;
	}

	/**
	 * @return the messageRetry
	 */
	public Integer getMessageRetry() {
		return messageRetry;
	}

	/**
	 * @param messageRetry
	 *            the messageRetry to set
	 */
	public void setMessageRetry(Integer messageRetry) {
		this.messageRetry = messageRetry;
	}

	/**
	 * @return the messageRetryEndFlg
	 */
	public Boolean getMessageRetryEndFlg() {
		return messageRetryEndFlg;
	}

	/**
	 * @param messageRetryEndFlg
	 *            the messageRetryEndFlg to set
	 */
	public void setMessageRetryEndFlg(Boolean messageRetryEndFlg) {
		this.messageRetryEndFlg = messageRetryEndFlg;
	}

	/**
	 * @return the messageRetryEndValue
	 */
	public Integer getMessageRetryEndValue() {
		return messageRetryEndValue;
	}

	/**
	 * @param messageRetryEndValue
	 *            the messageRetryEndValue to set
	 */
	public void setMessageRetryEndValue(Integer messageRetryEndValue) {
		this.messageRetryEndValue = messageRetryEndValue;
	}

	/**
	 * @return the commandRetryFlg
	 */
	public Boolean getCommandRetryFlg() {
		return commandRetryFlg;
	}

	/**
	 * @param commandRetryFlg
	 *            the commandRetryFlg to set
	 */
	public void setCommandRetryFlg(Boolean commandRetryFlg) {
		this.commandRetryFlg = commandRetryFlg;
	}

	/**
	 * @return the commandRetry
	 */
	public Integer getCommandRetry() {
		return commandRetry;
	}

	/**
	 * @param commandRetry
	 *            the commandRetry to set
	 */
	public void setCommandRetry(Integer commandRetry) {
		this.commandRetry = commandRetry;
	}

	/**
	 * @return the commandRetryEndStatus
	 */
	public CommandRetryEndStatusEnum getCommandRetryEndStatus() {
		return commandRetryEndStatus;
	}

	/**
	 * @param commandRetryEndStatus
	 *            the commandRetryEndStatus to set
	 */
	public void setCommandRetryEndStatus(CommandRetryEndStatusEnum commandRetryEndStatus) {
		this.commandRetryEndStatus = commandRetryEndStatus;
	}

	/**
	 * @return the rpaJobOptionInfos
	 */
	public List<JobRpaOptionInfoRequest> getRpaJobOptionInfos() {
		return rpaJobOptionInfos;
	}

	/**
	 * @param rpaJobOptionInfos
	 *            the rpaJobOptionInfos to set
	 */
	public void setRpaJobOptionInfos(List<JobRpaOptionInfoRequest> rpaJobOptionInfos) {
		this.rpaJobOptionInfos = rpaJobOptionInfos;
	}

	/**
	 * @return the rpaJobEndValueConditionInfos
	 */
	public List<JobRpaEndValueConditionInfoRequest> getRpaJobEndValueConditionInfos() {
		return rpaJobEndValueConditionInfos;
	}

	/**
	 * @param rpaJobEndValueConditionInfos
	 *            the rpaJobEndValueConditionInfos to set
	 */
	public void setRpaJobEndValueConditionInfos(List<JobRpaEndValueConditionInfoRequest> rpaJobEndValueConditionInfos) {
		this.rpaJobEndValueConditionInfos = rpaJobEndValueConditionInfos;
	}

	/**
	 * @return the rpaNotLoginNotify
	 */
	public Boolean getRpaNotLoginNotify() {
		return rpaNotLoginNotify;
	}

	/**
	 * @param rpaNotLoginNotify
	 *            the rpaNotLoginNotify to set
	 */
	public void setRpaNotLoginNotify(Boolean rpaNotLoginNotify) {
		this.rpaNotLoginNotify = rpaNotLoginNotify;
	}

	/**
	 * @return the rpaNotLoginNotifyPriority
	 */
	public PrioritySelectEnum getRpaNotLoginNotifyPriority() {
		return rpaNotLoginNotifyPriority;
	}

	/**
	 * @param rpaNotLoginNotifyPriority
	 *            the rpaNotLoginNotifyPriority to set
	 */
	public void setRpaNotLoginNotifyPriority(PrioritySelectEnum rpaNotLoginNotifyPriority) {
		this.rpaNotLoginNotifyPriority = rpaNotLoginNotifyPriority;
	}

	/**
	 * @return the rpaNotLoginEndValue
	 */
	public Integer getRpaNotLoginEndValue() {
		return rpaNotLoginEndValue;
	}

	/**
	 * @param rpaNotLoginEndValue
	 *            the rpaNotLoginEndValue to set
	 */
	public void setRpaNotLoginEndValue(Integer rpaNotLoginEndValue) {
		this.rpaNotLoginEndValue = rpaNotLoginEndValue;
	}

	/**
	 * @return the rpaAlreadyRunningNotify
	 */
	public Boolean getRpaAlreadyRunningNotify() {
		return rpaAlreadyRunningNotify;
	}

	/**
	 * @param rpaAlreadyRunningNotify
	 *            the rpaAlreadyRunningNotify to set
	 */
	public void setRpaAlreadyRunningNotify(Boolean rpaAlreadyRunningNotify) {
		this.rpaAlreadyRunningNotify = rpaAlreadyRunningNotify;
	}

	/**
	 * @return the rpaAlreadyRunningNotifyPriority
	 */
	public PrioritySelectEnum getRpaAlreadyRunningNotifyPriority() {
		return rpaAlreadyRunningNotifyPriority;
	}

	/**
	 * @param rpaAlreadyRunningNotifyPriority
	 *            the rpaAlreadyRunningNotifyPriority to set
	 */
	public void setRpaAlreadyRunningNotifyPriority(PrioritySelectEnum rpaAlreadyRunningNotifyPriority) {
		this.rpaAlreadyRunningNotifyPriority = rpaAlreadyRunningNotifyPriority;
	}

	/**
	 * @return the rpaAlreadyRunningEndValue
	 */
	public Integer getRpaAlreadyRunningEndValue() {
		return rpaAlreadyRunningEndValue;
	}

	/**
	 * @param rpaAlreadyRunningEndValue
	 *            the rpaAlreadyRunningEndValue to set
	 */
	public void setRpaAlreadyRunningEndValue(Integer rpaAlreadyRunningEndValue) {
		this.rpaAlreadyRunningEndValue = rpaAlreadyRunningEndValue;
	}

	/**
	 * @return the rpaAbnormalExitNotify
	 */
	public Boolean getRpaAbnormalExitNotify() {
		return rpaAbnormalExitNotify;
	}

	/**
	 * @param rpaAbnormalExitNotify
	 *            the rpaAbnormalExitNotify to set
	 */
	public void setRpaAbnormalExitNotify(Boolean rpaAbnormalExitNotify) {
		this.rpaAbnormalExitNotify = rpaAbnormalExitNotify;
	}

	/**
	 * @return the rpaAbnormalExitNotifyPriority
	 */
	public PrioritySelectEnum getRpaAbnormalExitNotifyPriority() {
		return rpaAbnormalExitNotifyPriority;
	}

	/**
	 * @param rpaAbnormalExitNotifyPriority
	 *            the rpaAbnormalExitNotifyPriority to set
	 */
	public void setRpaAbnormalExitNotifyPriority(PrioritySelectEnum rpaAbnormalExitNotifyPriority) {
		this.rpaAbnormalExitNotifyPriority = rpaAbnormalExitNotifyPriority;
	}

	/**
	 * @return the rpaAbnormalExitEndValue
	 */
	public Integer getRpaAbnormalExitEndValue() {
		return rpaAbnormalExitEndValue;
	}

	/**
	 * @param rpaAbnormalExitEndValue
	 *            the rpaAbnormalExitEndValue to set
	 */
	public void setRpaAbnormalExitEndValue(Integer rpaAbnormalExitEndValue) {
		this.rpaAbnormalExitEndValue = rpaAbnormalExitEndValue;
	}

	/**
	 * @return the rpaScopeId
	 */
	public String getRpaScopeId() {
		return rpaScopeId;
	}

	/**
	 * @param rpaScopeId the rpaScopeId to set
	 */
	public void setRpaScopeId(String rpaScopeId) {
		this.rpaScopeId = rpaScopeId;
	}

	/**
	 * @return the rpaRunType
	 */
	public Integer getRpaRunType() {
		return rpaRunType;
	}

	/**
	 * @param rpaRunType the rpaRunType to set
	 */
	public void setRpaRunType(Integer rpaRunType) {
		this.rpaRunType = rpaRunType;
	}

	/**
	 * @return the rpaJobRunParamInfos
	 */
	public List<JobRpaRunParamInfoRequest> getRpaJobRunParamInfos() {
		return rpaJobRunParamInfos;
	}

	/**
	 * @param rpaJobRunParamInfos the rpaJobRunParamInfos to set
	 */
	public void setRpaJobRunParamInfos(List<JobRpaRunParamInfoRequest> rpaJobRunParamInfos) {
		this.rpaJobRunParamInfos = rpaJobRunParamInfos;
	}

	/**
	 * @return the rpaScenarioParam
	 */
	public String getRpaScenarioParam() {
		return rpaScenarioParam;
	}

	/**
	 * @param rpaScenarioParam the rpaScenarioParam to set
	 */
	public void setRpaScenarioParam(String rpaScenarioParam) {
		this.rpaScenarioParam = rpaScenarioParam;
	}

	/**
	 * @return the rpaStopType
	 */
	public RpaStopTypeEnum getRpaStopType() {
		return rpaStopType;
	}

	/**
	 * @param rpaStopType the rpaStopType to set
	 */
	public void setRpaStopType(RpaStopTypeEnum rpaStopType) {
		this.rpaStopType = rpaStopType;
	}

	/**
	 * @return the rpaStopMode
	 */
	public Integer getRpaStopMode() {
		return rpaStopMode;
	}

	/**
	 * @param rpaStopMode the rpaStopMode to set
	 */
	public void setRpaStopMode(Integer rpaStopMode) {
		this.rpaStopMode = rpaStopMode;
	}

	/**
	 * @return the rpaRunConnectTimeout
	 */
	public Integer getRpaRunConnectTimeout() {
		return rpaRunConnectTimeout;
	}

	/**
	 * @param rpaRunConnectTimeout the rpaRunConnectTimeout to set
	 */
	public void setRpaRunConnectTimeout(Integer rpaRunConnectTimeout) {
		this.rpaRunConnectTimeout = rpaRunConnectTimeout;
	}

	/**
	 * @return the rpaRunRequestTimeout
	 */
	public Integer getRpaRunRequestTimeout() {
		return rpaRunRequestTimeout;
	}

	/**
	 * @param rpaRunRequestTimeout the rpaRunRequestTimeout to set
	 */
	public void setRpaRunRequestTimeout(Integer rpaRunRequestTimeout) {
		this.rpaRunRequestTimeout = rpaRunRequestTimeout;
	}

	/**
	 * @return the rpaRunEndFlg
	 */
	public Boolean getRpaRunEndFlg() {
		return rpaRunEndFlg;
	}

	/**
	 * @param rpaRunEndFlg the rpaRunEndFlg to set
	 */
	public void setRpaRunEndFlg(Boolean rpaRunEndFlg) {
		this.rpaRunEndFlg = rpaRunEndFlg;
	}

	/**
	 * @return the rpaRunRetry
	 */
	public Integer getRpaRunRetry() {
		return rpaRunRetry;
	}

	/**
	 * @param rpaRunRetry the rpaRunRetry to set
	 */
	public void setRpaRunRetry(Integer rpaRunRetry) {
		this.rpaRunRetry = rpaRunRetry;
	}

	/**
	 * @return the rpaRunEndValue
	 */
	public Integer getRpaRunEndValue() {
		return rpaRunEndValue;
	}

	/**
	 * @param rpaRunEndValue the rpaRunEndValue to set
	 */
	public void setRpaRunEndValue(Integer rpaRunEndValue) {
		this.rpaRunEndValue = rpaRunEndValue;
	}

	/**
	 * @return the rpaCheckConnectTimeout
	 */
	public Integer getRpaCheckConnectTimeout() {
		return rpaCheckConnectTimeout;
	}

	/**
	 * @param rpaCheckConnectTimeout the rpaCheckConnectTimeout to set
	 */
	public void setRpaCheckConnectTimeout(Integer rpaCheckConnectTimeout) {
		this.rpaCheckConnectTimeout = rpaCheckConnectTimeout;
	}

	/**
	 * @return the rpaCheckRequestTimeout
	 */
	public Integer getRpaCheckRequestTimeout() {
		return rpaCheckRequestTimeout;
	}

	/**
	 * @param rpaCheckRequestTimeout the rpaCheckRequestTimeout to set
	 */
	public void setRpaCheckRequestTimeout(Integer rpaCheckRequestTimeout) {
		this.rpaCheckRequestTimeout = rpaCheckRequestTimeout;
	}

	/**
	 * @return the rpaCheckEndFlg
	 */
	public Boolean getRpaCheckEndFlg() {
		return rpaCheckEndFlg;
	}

	/**
	 * @param rpaCheckEndFlg the rpaCheckEndFlg to set
	 */
	public void setRpaCheckEndFlg(Boolean rpaCheckEndFlg) {
		this.rpaCheckEndFlg = rpaCheckEndFlg;
	}

	/**
	 * @return the rpaCheckRetry
	 */
	public Integer getRpaCheckRetry() {
		return rpaCheckRetry;
	}

	/**
	 * @param rpaCheckRetry the rpaCheckRetry to set
	 */
	public void setRpaCheckRetry(Integer rpaCheckRetry) {
		this.rpaCheckRetry = rpaCheckRetry;
	}

	/**
	 * @return the rpaCheckEndValue
	 */
	public Integer getRpaCheckEndValue() {
		return rpaCheckEndValue;
	}

	/**
	 * @param rpaCheckEndValue the rpaCheckEndValue to set
	 */
	public void setRpaCheckEndValue(Integer rpaCheckEndValue) {
		this.rpaCheckEndValue = rpaCheckEndValue;
	}

	/**
	 * @return the rpaJobCheckEndValueInfos
	 */
	public List<JobRpaCheckEndValueInfoRequest> getRpaJobCheckEndValueInfos() {
		return rpaJobCheckEndValueInfos;
	}

	/**
	 * @param rpaJobCheckEndValueInfos the rpaJobCheckEndValueInfos to set
	 */
	public void setRpaJobCheckEndValueInfos(List<JobRpaCheckEndValueInfoRequest> rpaJobCheckEndValueInfos) {
		this.rpaJobCheckEndValueInfos = rpaJobCheckEndValueInfos;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		if (rpaJobOptionInfos != null) {
			for (JobRpaOptionInfoRequest req : rpaJobOptionInfos) {
				req.correlationCheck();
			}
		}
		if (rpaJobEndValueConditionInfos != null) {
			for (JobRpaEndValueConditionInfoRequest req : rpaJobEndValueConditionInfos) {
				req.correlationCheck();
			}
		}
		if (rpaJobRunParamInfos != null) {
			for (JobRpaRunParamInfoRequest req : rpaJobRunParamInfos) {
				req.correlationCheck();
			}
		}
		if (rpaJobCheckEndValueInfos != null) {
			for (JobRpaCheckEndValueInfoRequest req : rpaJobCheckEndValueInfos) {
				req.correlationCheck();
			}
		}
	}
}
