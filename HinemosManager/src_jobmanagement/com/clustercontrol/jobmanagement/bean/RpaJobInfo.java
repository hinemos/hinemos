/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;
import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.annotation.EnumerateConstant;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.deserializer.EnumToConstantDeserializer;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.PrioritySelectEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ProcessingMethodEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.RpaJobReturnCodeConditionEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.RpaJobTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.RpaStopTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.serializer.ConstantToEnumSerializer;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.serializer.LanguageTranslateSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * RPAシナリオジョブに関する情報を保持するクラス
 */
/* 
 * 本クラスのRestXXアノテーション、correlationCheckを修正する場合は、Requestクラスも同様に修正すること。
 * (ジョブユニットの登録/更新はInfoクラス、ジョブ単位の登録/更新の際はRequestクラスが使用される。)
 * refs #13882
 */
public class RpaJobInfo implements Serializable, RequestDto {
	/** シリアライズ可能クラスに定義するUID */
	@JsonIgnore
	private static final long serialVersionUID = 1L;
	/** RPAジョブ種別 */
	@JsonDeserialize(using=EnumToConstantDeserializer.class)
	@JsonSerialize(using=ConstantToEnumSerializer.class)
	@EnumerateConstant(enumDto=RpaJobTypeEnum.class)
	private Integer rpaJobType;

	// 直接実行
	/** ファシリティID */
	private String facilityID;

	/** スコープ */
	@JsonSerialize(using=LanguageTranslateSerializer.class)
	private String scope;

	/** スコープ処理 */
	@JsonDeserialize(using=EnumToConstantDeserializer.class)
	@JsonSerialize(using=ConstantToEnumSerializer.class)
	@EnumerateConstant(enumDto=ProcessingMethodEnum.class)
	private Integer processingMethod;

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
	@JsonDeserialize(using=EnumToConstantDeserializer.class)
	@JsonSerialize(using=ConstantToEnumSerializer.class)
	@EnumerateConstant(enumDto=RpaJobReturnCodeConditionEnum.class)
	private Integer rpaScreenshotEndValueCondition;

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
	private Integer commandRetryEndStatus;

	/** 実行オプション */
	private List<RpaJobOptionInfo> rpaJobOptionInfos;

	/** 終了値判定条件 */
	private List<RpaJobEndValueConditionInfo> rpaJobEndValueConditionInfos;

	/** ログインされていない場合 通知 */
	private Boolean rpaNotLoginNotify;

	/** ログインされていない場合 通知重要度 */
	@JsonDeserialize(using=EnumToConstantDeserializer.class)
	@JsonSerialize(using=ConstantToEnumSerializer.class)
	@EnumerateConstant(enumDto=PrioritySelectEnum.class)
	private Integer rpaNotLoginNotifyPriority;

	/** ログインされていない場合 終了値 */
	private Integer rpaNotLoginEndValue;

	/** RPAツールが既に動作している場合 通知 */
	private Boolean rpaAlreadyRunningNotify;

	/** RPAツールが既に動作している場合 通知重要度 */
	@JsonDeserialize(using=EnumToConstantDeserializer.class)
	@JsonSerialize(using=ConstantToEnumSerializer.class)
	@EnumerateConstant(enumDto=PrioritySelectEnum.class)
	private Integer rpaAlreadyRunningNotifyPriority;

	/** RPAツールが既に動作している場合 終了値 */
	private Integer rpaAlreadyRunningEndValue;

	/** RPAツールが異常終了した場合 通知 */
	private Boolean rpaAbnormalExitNotify;

	/** RPAツールが異常終了した場合 通知重要度 */
	@JsonDeserialize(using=EnumToConstantDeserializer.class)
	@JsonSerialize(using=ConstantToEnumSerializer.class)
	@EnumerateConstant(enumDto=PrioritySelectEnum.class)
	private Integer rpaAbnormalExitNotifyPriority;

	/** RPAツールが異常終了した場合 終了値 */
	private Integer rpaAbnormalExitEndValue;
	
	// 間接実行
	/** RPAスコープID */
	private String rpaScopeId;
	
	/** RPA管理ツール 実行種別 */
	private Integer rpaRunType;

	/** 起動パラメータ */
	private List<RpaJobRunParamInfo> rpaJobRunParamInfos;
	
	/** シナリオ入力パラメータ */
	private String rpaScenarioParam;

	/** 停止種別 */
	@JsonDeserialize(using=EnumToConstantDeserializer.class)
	@JsonSerialize(using=ConstantToEnumSerializer.class)
	@EnumerateConstant(enumDto=RpaStopTypeEnum.class)
	private Integer rpaStopType;

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

	/** シナリオ実行 実行できない場合の終了値 */
	private Integer rpaRunEndValue;

	/** シナリオ実行結果確認 コネクションタイムアウト */
	private Integer rpaCheckConnectTimeout;

	/** シナリオ実行結果確認 リクエストタイムアウト */
	private Integer rpaCheckRequestTimeout;

	/** シナリオ実行結果確認 実行できない場合に終了する */
	private Boolean rpaCheckEndFlg;

	/** シナリオ実行結果確認 リトライ回数 */
	private Integer rpaCheckRetry;

	/** シナリオ実行結果確認 結果が確認できない場合の終了値 */
	private Integer rpaCheckEndValue;

	/** 終了値判定条件 */
	private List<RpaJobCheckEndValueInfo> rpaJobCheckEndValueInfos;

	/**
	 * @return RPAジョブ種別を返します。
	 */
	public Integer getRpaJobType() {
		return rpaJobType;
	}

	/**
	 * @param rpaJobType
	 *            RPAジョブ種別を設定します。
	 */
	public void setRpaJobType(Integer rpaJobType) {
		this.rpaJobType = rpaJobType;
	}

	/**
	 * @return ファシリティIDを返します。
	 */
	public String getFacilityID() {
		return facilityID;
	}

	/**
	 * @param facilityID
	 *            ファシリティIDを設定します。
	 */
	public void setFacilityID(String facilityID) {
		this.facilityID = facilityID;
	}

	/**
	 * @return スコープを返します。
	 */
	public String getScope() {
		return scope;
	}

	/**
	 * @param scope
	 *            スコープを設定します。
	 */
	public void setScope(String scope) {
		this.scope = scope;
	}

	/**
	 * @return スコープ処理を返します。
	 */
	public Integer getProcessingMethod() {
		return processingMethod;
	}

	/**
	 * @param processingMethod
	 *            スコープ処理を設定します。
	 */
	public void setProcessingMethod(Integer processingMethod) {
		this.processingMethod = processingMethod;
	}

	/**
	 * @return RPAツールIDを返します。
	 */
	public String getRpaToolId() {
		return rpaToolId;
	}

	/**
	 * @param rpaToolId
	 *            RPAツールIDを設定します。
	 */
	public void setRpaToolId(String rpaToolId) {
		this.rpaToolId = rpaToolId;
	}

	/**
	 * @return 実行ファイルパスを返します。
	 */
	public String getRpaExeFilepath() {
		return rpaExeFilepath;
	}

	/**
	 * @param rpaExeFilepath
	 *            実行ファイルパスを設定します。
	 */
	public void setRpaExeFilepath(String rpaExeFilepath) {
		this.rpaExeFilepath = rpaExeFilepath;
	}

	/**
	 * @return シナリオファイルパスを返します。
	 */
	public String getRpaScenarioFilepath() {
		return rpaScenarioFilepath;
	}

	/**
	 * @param rpaScenarioFilepath
	 *            シナリオファイルパスを設定します。
	 */
	public void setRpaScenarioFilepath(String rpaScenarioFilepath) {
		this.rpaScenarioFilepath = rpaScenarioFilepath;
	}

	/**
	 * @return 終了値判定用ログファイルディレクトリを返します。
	 */
	public String getRpaLogDirectory() {
		return rpaLogDirectory;
	}

	/**
	 * @param rpaLogDirectory
	 *            終了値判定用ログファイルディレクトリを設定します。
	 */
	public void setRpaLogDirectory(String rpaLogDirectory) {
		this.rpaLogDirectory = rpaLogDirectory;
	}

	/**
	 * @return 終了値判定用ログファイル名を返します。
	 */
	public String getRpaLogFileName() {
		return rpaLogFileName;
	}

	/**
	 * @param rpaLogFileName
	 *            終了値判定用ログファイル名を設定します。
	 */
	public void setRpaLogFileName(String rpaLogFileName) {
		this.rpaLogFileName = rpaLogFileName;
	}

	/**
	 * @return 終了値判定用ログファイルエンコーディングを返します。
	 */
	public String getRpaLogEncoding() {
		return rpaLogEncoding;
	}

	/**
	 * @param rpaLogEncoding
	 *            終了値判定用ログファイルエンコーディングを設定します。
	 */
	public void setRpaLogEncoding(String rpaLogEncoding) {
		this.rpaLogEncoding = rpaLogEncoding;
	}

	/**
	 * @return 終了値判定用ログファイル区切り文字を返します。
	 */
	public String getRpaLogReturnCode() {
		return rpaLogReturnCode;
	}

	/**
	 * @param rpaLogReturnCode
	 *            終了値判定用ログファイル区切り文字を設定します。
	 */
	public void setRpaLogReturnCode(String rpaLogReturnCode) {
		this.rpaLogReturnCode = rpaLogReturnCode;
	}

	/**
	 * @return 終了値判定用ログファイル先頭パターンを返します。
	 */
	public String getRpaLogPatternHead() {
		return rpaLogPatternHead;
	}

	/**
	 * @param rpaLogPatternHead
	 *            終了値判定用ログファイル先頭パターンを設定します。
	 */
	public void setRpaLogPatternHead(String rpaLogPatternHead) {
		this.rpaLogPatternHead = rpaLogPatternHead;
	}

	/**
	 * @return 終了値判定用ログファイル終端パターンを返します。
	 */
	public String getRpaLogPatternTail() {
		return rpaLogPatternTail;
	}

	/**
	 * @param rpaLogPatternTail
	 *            終了値判定用ログファイル終端パターンを設定します。
	 */
	public void setRpaLogPatternTail(String rpaLogPatternTail) {
		this.rpaLogPatternTail = rpaLogPatternTail;
	}

	/**
	 * @return 終了値判定用ログファイル最大読み取り文字数を返します。
	 */
	public Integer getRpaLogMaxBytes() {
		return rpaLogMaxBytes;
	}

	/**
	 * @param rpaLogMaxBytes
	 *            終了値判定用ログファイル最大読み取り文字数を設定します。
	 */
	public void setRpaLogMaxBytes(Integer rpaLogMaxBytes) {
		this.rpaLogMaxBytes = rpaLogMaxBytes;
	}

	/**
	 * @return いずれの判定にも一致しない場合の終了値を返します。
	 */
	public Integer getRpaDefaultEndValue() {
		return rpaDefaultEndValue;
	}

	/**
	 * @param rpaDefaultEndValue
	 *            いずれの判定にも一致しない場合の終了値を設定します。
	 */
	public void setRpaDefaultEndValue(Integer rpaDefaultEndValue) {
		this.rpaDefaultEndValue = rpaDefaultEndValue;
	}

	/**
	 * @return シナリオ実行前後でOSへログイン・ログアウトするを返します。
	 */
	public Boolean getRpaLoginFlg() {
		return rpaLoginFlg;
	}

	/**
	 * @param rpaLoginFlg
	 *            シナリオ実行前後でOSへログイン・ログアウトするを設定します。
	 */
	public void setRpaLoginFlg(Boolean rpaLoginFlg) {
		this.rpaLoginFlg = rpaLoginFlg;
	}

	/**
	 * @return ログインユーザIDを返します。
	 */
	public String getRpaLoginUserId() {
		return rpaLoginUserId;
	}

	/**
	 * @param rpaLoginUserId
	 *            ログインユーザIDを設定します。
	 */
	public void setRpaLoginUserId(String rpaLoginUserId) {
		this.rpaLoginUserId = rpaLoginUserId;
	}

	/**
	 * @return ログインパスワードを返します。
	 */
	public String getRpaLoginPassword() {
		return rpaLoginPassword;
	}

	/**
	 * @param rpaLoginPassword
	 *            ログインパスワードを設定します。
	 */
	public void setRpaLoginPassword(String rpaLoginPassword) {
		this.rpaLoginPassword = rpaLoginPassword;
	}

	/**
	 * @return ログインリトライ回数を返します。
	 */
	public Integer getRpaLoginRetry() {
		return rpaLoginRetry;
	}

	/**
	 * @param rpaLoginRetry
	 *            ログインリトライ回数を設定します。
	 */
	public void setRpaLoginRetry(Integer rpaLoginRetry) {
		this.rpaLoginRetry = rpaLoginRetry;
	}

	/**
	 * @return ログインできない場合の終了値を返します。
	 */
	public Integer getRpaLoginEndValue() {
		return rpaLoginEndValue;
	}

	/**
	 * @param rpaLoginEndValue
	 *            ログインできない場合の終了値を設定します。
	 */
	public void setRpaLoginEndValue(Integer rpaLoginEndValue) {
		this.rpaLoginEndValue = rpaLoginEndValue;
	}

	/**
	 * @return ログインの解像度を返します。
	 */
	public String getRpaLoginResolution() {
		return rpaLoginResolution;
	}

	/**
	 * @param rpaLoginResolution
	 *            ログインの解像度を設定します。
	 */
	public void setRpaLoginResolution(String rpaLoginResolution) {
		this.rpaLoginResolution = rpaLoginResolution;
	}

	/**
	 * @return 異常終了時もログアウトするを返します。
	 */
	public Boolean getRpaLogoutFlg() {
		return rpaLogoutFlg;
	}

	/**
	 * @param rpaLogoutFlg
	 *            異常終了時もログアウトするを設定します。
	 */
	public void setRpaLogoutFlg(Boolean rpaLogoutFlg) {
		this.rpaLogoutFlg = rpaLogoutFlg;
	}

	/**
	 * @return 終了遅延発生時にスクリーンショットを取得するを返します。
	 */
	public Boolean getRpaScreenshotEndDelayFlg() {
		return rpaScreenshotEndDelayFlg;
	}

	/**
	 * @param rpaScreenshotEndDelayFlg
	 *            終了遅延発生時にスクリーンショットを取得するを設定します。
	 */
	public void setRpaScreenshotEndDelayFlg(Boolean rpaScreenshotEndDelayFlg) {
		this.rpaScreenshotEndDelayFlg = rpaScreenshotEndDelayFlg;
	}

	/**
	 * @return 特定の終了値の場合にスクリーンショットを取得するを返します。
	 */
	public Boolean getRpaScreenshotEndValueFlg() {
		return rpaScreenshotEndValueFlg;
	}

	/**
	 * @param rpaScreenshotEndValueFlg
	 *            特定の終了値の場合にスクリーンショットを取得するを設定します。
	 */
	public void setRpaScreenshotEndValueFlg(Boolean rpaScreenshotEndValueFlg) {
		this.rpaScreenshotEndValueFlg = rpaScreenshotEndValueFlg;
	}

	/**
	 * @return スクリーンショットを取得する終了値を返します。
	 */
	public String getRpaScreenshotEndValue() {
		return rpaScreenshotEndValue;
	}

	/**
	 * @param rpaScreenshotEndValue
	 *            スクリーンショットを取得する終了値を設定します。
	 */
	public void setRpaScreenshotEndValue(String rpaScreenshotEndValue) {
		this.rpaScreenshotEndValue = rpaScreenshotEndValue;
	}

	/**
	 * @return スクリーンショットを取得する終了値判定条件を返します。
	 */
	public Integer getRpaScreenshotEndValueCondition() {
		return rpaScreenshotEndValueCondition;
	}

	/**
	 * @param rpaScreenshotEndValueCondition
	 *            スクリーンショットを取得する終了値判定条件を設定します。
	 */
	public void setRpaScreenshotEndValueCondition(Integer rpaScreenshotEndValueCondition) {
		this.rpaScreenshotEndValueCondition = rpaScreenshotEndValueCondition;
	}

	/**
	 * @return リトライ回数を返します。
	 */
	public Integer getMessageRetry() {
		return messageRetry;
	}

	/**
	 * @param messageRetry
	 *            リトライ回数を設定します。
	 */
	public void setMessageRetry(Integer messageRetry) {
		this.messageRetry = messageRetry;
	}

	/**
	 * @return コマンド実行失敗時終了フラグを返します。
	 */
	public Boolean getMessageRetryEndFlg() {
		return messageRetryEndFlg;
	}

	/**
	 * @param messageRetryEndFlg
	 *            コマンド実行失敗時終了フラグを設定します。
	 */
	public void setMessageRetryEndFlg(Boolean messageRetryEndFlg) {
		this.messageRetryEndFlg = messageRetryEndFlg;
	}

	/**
	 * @return コマンド実行失敗時終了値を返します。
	 */
	public Integer getMessageRetryEndValue() {
		return messageRetryEndValue;
	}

	/**
	 * @param messageRetryEndValue
	 *            コマンド実行失敗時終了値を設定します。
	 */
	public void setMessageRetryEndValue(Integer messageRetryEndValue) {
		this.messageRetryEndValue = messageRetryEndValue;
	}

	/**
	 * @return 繰り返し実行フラグを返します。
	 */
	public Boolean getCommandRetryFlg() {
		return commandRetryFlg;
	}

	/**
	 * @param commandRetryFlg
	 *            繰り返し実行フラグを設定します。
	 */
	public void setCommandRetryFlg(Boolean commandRetryFlg) {
		this.commandRetryFlg = commandRetryFlg;
	}

	/**
	 * @return 繰り返し実行回数を返します。
	 */
	public Integer getCommandRetry() {
		return commandRetry;
	}

	/**
	 * @param commandRetry
	 *            繰り返し実行回数を設定します。
	 */
	public void setCommandRetry(Integer commandRetry) {
		this.commandRetry = commandRetry;
	}

	/**
	 * @return 繰り返し完了状態を返します。
	 */
	public Integer getCommandRetryEndStatus() {
		return commandRetryEndStatus;
	}

	/**
	 * @param commandRetryEndStatus
	 *            繰り返し完了状態を設定します。
	 */
	public void setCommandRetryEndStatus(Integer commandRetryEndStatus) {
		this.commandRetryEndStatus = commandRetryEndStatus;
	}

	/**
	 * @return 実行オプションを返します。
	 */
	public List<RpaJobOptionInfo> getRpaJobOptionInfos() {
		return rpaJobOptionInfos;
	}

	/**
	 * @param rpaJobOptionInfos
	 *            実行オプションを設定します。
	 */
	public void setRpaJobOptionInfos(List<RpaJobOptionInfo> rpaJobOptionInfos) {
		this.rpaJobOptionInfos = rpaJobOptionInfos;
	}

	/**
	 * @return 終了値判定条件を返します。
	 */
	public List<RpaJobEndValueConditionInfo> getRpaJobEndValueConditionInfos() {
		return rpaJobEndValueConditionInfos;
	}

	/**
	 * @param rpaJobEndValueConditionInfos
	 *            終了値判定条件を設定します。
	 */
	public void setRpaJobEndValueConditionInfos(List<RpaJobEndValueConditionInfo> rpaJobEndValueConditionInfos) {
		this.rpaJobEndValueConditionInfos = rpaJobEndValueConditionInfos;
	}

	/**
	 * @return ログインされていない場合を返します。
	 */
	public Boolean getRpaNotLoginNotify() {
		return rpaNotLoginNotify;
	}

	/**
	 * @param rpaNotLoginNotify
	 *            ログインされていない場合を設定します。
	 */
	public void setRpaNotLoginNotify(Boolean rpaNotLoginNotify) {
		this.rpaNotLoginNotify = rpaNotLoginNotify;
	}

	/**
	 * @return ログインされていない場合を返します。
	 */
	public Integer getRpaNotLoginNotifyPriority() {
		return rpaNotLoginNotifyPriority;
	}

	/**
	 * @param rpaNotLoginNotifyPriority
	 *            ログインされていない場合を設定します。
	 */
	public void setRpaNotLoginNotifyPriority(Integer rpaNotLoginNotifyPriority) {
		this.rpaNotLoginNotifyPriority = rpaNotLoginNotifyPriority;
	}

	/**
	 * @return ログインされていない場合を返します。
	 */
	public Integer getRpaNotLoginEndValue() {
		return rpaNotLoginEndValue;
	}

	/**
	 * @param rpaNotLoginEndValue
	 *            ログインされていない場合を設定します。
	 */
	public void setRpaNotLoginEndValue(Integer rpaNotLoginEndValue) {
		this.rpaNotLoginEndValue = rpaNotLoginEndValue;
	}

	/**
	 * @return RPAツールが既に動作している場合を返します。
	 */
	public Boolean getRpaAlreadyRunningNotify() {
		return rpaAlreadyRunningNotify;
	}

	/**
	 * @param rpaAlreadyRunningNotify
	 *            RPAツールが既に動作している場合を設定します。
	 */
	public void setRpaAlreadyRunningNotify(Boolean rpaAlreadyRunningNotify) {
		this.rpaAlreadyRunningNotify = rpaAlreadyRunningNotify;
	}

	/**
	 * @return RPAツールが既に動作している場合を返します。
	 */
	public Integer getRpaAlreadyRunningNotifyPriority() {
		return rpaAlreadyRunningNotifyPriority;
	}

	/**
	 * @param rpaAlreadyRunningNotifyPriority
	 *            RPAツールが既に動作している場合を設定します。
	 */
	public void setRpaAlreadyRunningNotifyPriority(Integer rpaAlreadyRunningNotifyPriority) {
		this.rpaAlreadyRunningNotifyPriority = rpaAlreadyRunningNotifyPriority;
	}

	/**
	 * @return RPAツールが既に動作している場合を返します。
	 */
	public Integer getRpaAlreadyRunningEndValue() {
		return rpaAlreadyRunningEndValue;
	}

	/**
	 * @param rpaAlreadyRunningEndValue
	 *            RPAツールが既に動作している場合を設定します。
	 */
	public void setRpaAlreadyRunningEndValue(Integer rpaAlreadyRunningEndValue) {
		this.rpaAlreadyRunningEndValue = rpaAlreadyRunningEndValue;
	}

	/**
	 * @return RPAツールが異常終了した場合を返します。
	 */
	public Boolean getRpaAbnormalExitNotify() {
		return rpaAbnormalExitNotify;
	}

	/**
	 * @param rpaAbnormalExitNotify
	 *            RPAツールが異常終了した場合を設定します。
	 */
	public void setRpaAbnormalExitNotify(Boolean rpaAbnormalExitNotify) {
		this.rpaAbnormalExitNotify = rpaAbnormalExitNotify;
	}

	/**
	 * @return RPAツールが異常終了した場合を返します。
	 */
	public Integer getRpaAbnormalExitNotifyPriority() {
		return rpaAbnormalExitNotifyPriority;
	}

	/**
	 * @param rpaAbnormalExitNotifyPriority
	 *            RPAツールが異常終了した場合を設定します。
	 */
	public void setRpaAbnormalExitNotifyPriority(Integer rpaAbnormalExitNotifyPriority) {
		this.rpaAbnormalExitNotifyPriority = rpaAbnormalExitNotifyPriority;
	}

	/**
	 * @return RPAツールが異常終了した場合を返します。
	 */
	public Integer getRpaAbnormalExitEndValue() {
		return rpaAbnormalExitEndValue;
	}

	/**
	 * @param rpaAbnormalExitEndValue
	 *            RPAツールが異常終了した場合を設定します。
	 */
	public void setRpaAbnormalExitEndValue(Integer rpaAbnormalExitEndValue) {
		this.rpaAbnormalExitEndValue = rpaAbnormalExitEndValue;
	}

	/**
	 * @return RPAスコープIDを返します。
	 */
	public String getRpaScopeId() {
		return rpaScopeId;
	}

	/**
	 * @param rpaScopeId
	 *            RPAスコープIDを設定します。
	 */
	public void setRpaScopeId(String rpaScopeId) {
		this.rpaScopeId = rpaScopeId;
	}

	/**
	 * @return RPA管理ツールを返します。
	 */
	public Integer getRpaRunType() {
		return rpaRunType;
	}

	/**
	 * @param rpaRunType
	 *            RPA管理ツールを設定します。
	 */
	public void setRpaRunType(Integer rpaRunType) {
		this.rpaRunType = rpaRunType;
	}

	/**
	 * @return 起動パラメータを返します。
	 */
	public List<RpaJobRunParamInfo> getRpaJobRunParamInfos() {
		return rpaJobRunParamInfos;
	}

	/**
	 * @param rpaJobRunParamInfos
	 *            起動パラメータを設定します。
	 */
	public void setRpaJobRunParamInfos(List<RpaJobRunParamInfo> rpaJobRunParamInfos) {
		this.rpaJobRunParamInfos = rpaJobRunParamInfos;
	}

	/**
	 * @return シナリオ入力パラメータを返します。
	 */
	public String getRpaScenarioParam() {
		return rpaScenarioParam;
	}

	/**
	 * @param rpaScenarioParam
	 *            シナリオ入力パラメータを設定します。
	 */
	public void setRpaScenarioParam(String rpaScenarioParam) {
		this.rpaScenarioParam = rpaScenarioParam;
	}

	/**
	 * @return 停止種別を返します。
	 */
	public Integer getRpaStopType() {
		return rpaStopType;
	}

	/**
	 * @param rpaStopType
	 *            停止種別を設定します。
	 */
	public void setRpaStopType(Integer rpaStopType) {
		this.rpaStopType = rpaStopType;
	}

	/**
	 * @return 停止方法を返します。
	 */
	public Integer getRpaStopMode() {
		return rpaStopMode;
	}

	/**
	 * @param rpaStopMode
	 *            停止方法を設定します。
	 */
	public void setRpaStopMode(Integer rpaStopMode) {
		this.rpaStopMode = rpaStopMode;
	}

	/**
	 * @return シナリオ実行を返します。
	 */
	public Integer getRpaRunConnectTimeout() {
		return rpaRunConnectTimeout;
	}

	/**
	 * @param rpaRunConnectTimeout
	 *            シナリオ実行を設定します。
	 */
	public void setRpaRunConnectTimeout(Integer rpaRunConnectTimeout) {
		this.rpaRunConnectTimeout = rpaRunConnectTimeout;
	}

	/**
	 * @return シナリオ実行を返します。
	 */
	public Integer getRpaRunRequestTimeout() {
		return rpaRunRequestTimeout;
	}

	/**
	 * @param rpaRunRequestTimeout
	 *            シナリオ実行を設定します。
	 */
	public void setRpaRunRequestTimeout(Integer rpaRunRequestTimeout) {
		this.rpaRunRequestTimeout = rpaRunRequestTimeout;
	}

	/**
	 * @return シナリオ実行を返します。
	 */
	public Boolean getRpaRunEndFlg() {
		return rpaRunEndFlg;
	}

	/**
	 * @param rpaRunEndFlg
	 *            シナリオ実行を設定します。
	 */
	public void setRpaRunEndFlg(Boolean rpaRunEndFlg) {
		this.rpaRunEndFlg = rpaRunEndFlg;
	}

	/**
	 * @return シナリオ実行を返します。
	 */
	public Integer getRpaRunRetry() {
		return rpaRunRetry;
	}

	/**
	 * @param rpaRunRetry
	 *            シナリオ実行を設定します。
	 */
	public void setRpaRunRetry(Integer rpaRunRetry) {
		this.rpaRunRetry = rpaRunRetry;
	}

	/**
	 * @return シナリオ実行を返します。
	 */
	public Integer getRpaRunEndValue() {
		return rpaRunEndValue;
	}

	/**
	 * @param rpaRunEndValue
	 *            シナリオ実行を設定します。
	 */
	public void setRpaRunEndValue(Integer rpaRunEndValue) {
		this.rpaRunEndValue = rpaRunEndValue;
	}

	/**
	 * @return シナリオ実行結果確認を返します。
	 */
	public Integer getRpaCheckConnectTimeout() {
		return rpaCheckConnectTimeout;
	}

	/**
	 * @param rpaCheckConnectTimeout
	 *            シナリオ実行結果確認を設定します。
	 */
	public void setRpaCheckConnectTimeout(Integer rpaCheckConnectTimeout) {
		this.rpaCheckConnectTimeout = rpaCheckConnectTimeout;
	}

	/**
	 * @return シナリオ実行結果確認を返します。
	 */
	public Integer getRpaCheckRequestTimeout() {
		return rpaCheckRequestTimeout;
	}

	/**
	 * @param rpaCheckRequestTimeout
	 *            シナリオ実行結果確認を設定します。
	 */
	public void setRpaCheckRequestTimeout(Integer rpaCheckRequestTimeout) {
		this.rpaCheckRequestTimeout = rpaCheckRequestTimeout;
	}

	/**
	 * @return シナリオ実行結果確認を返します。
	 */
	public Boolean getRpaCheckEndFlg() {
		return rpaCheckEndFlg;
	}

	/**
	 * @param rpaCheckEndFlg
	 *            シナリオ実行結果確認を設定します。
	 */
	public void setRpaCheckEndFlg(Boolean rpaCheckEndFlg) {
		this.rpaCheckEndFlg = rpaCheckEndFlg;
	}

	/**
	 * @return シナリオ実行結果確認を返します。
	 */
	public Integer getRpaCheckRetry() {
		return rpaCheckRetry;
	}

	/**
	 * @param rpaCheckRetry
	 *            シナリオ実行結果確認を設定します。
	 */
	public void setRpaCheckRetry(Integer rpaCheckRetry) {
		this.rpaCheckRetry = rpaCheckRetry;
	}

	/**
	 * @return シナリオ実行結果確認を返します。
	 */
	public Integer getRpaCheckEndValue() {
		return rpaCheckEndValue;
	}

	/**
	 * @param rpaCheckEndValue
	 *            シナリオ実行結果確認を設定します。
	 */
	public void setRpaCheckEndValue(Integer rpaCheckEndValue) {
		this.rpaCheckEndValue = rpaCheckEndValue;
	}

	/**
	 * @return 終了値判定条件を返します。
	 */
	public List<RpaJobCheckEndValueInfo> getRpaJobCheckEndValueInfos() {
		return rpaJobCheckEndValueInfos;
	}

	/**
	 * @param rpaJobCheckEndValueInfos
	 *            終了値判定条件を設定します。
	 */
	public void setRpaJobCheckEndValueInfos(List<RpaJobCheckEndValueInfo> rpaJobCheckEndValueInfos) {
		this.rpaJobCheckEndValueInfos = rpaJobCheckEndValueInfos;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((commandRetry == null) ? 0 : commandRetry.hashCode());
		result = prime * result + ((commandRetryEndStatus == null) ? 0 : commandRetryEndStatus.hashCode());
		result = prime * result + ((commandRetryFlg == null) ? 0 : commandRetryFlg.hashCode());
		result = prime * result + ((facilityID == null) ? 0 : facilityID.hashCode());
		result = prime * result + ((messageRetry == null) ? 0 : messageRetry.hashCode());
		result = prime * result + ((messageRetryEndFlg == null) ? 0 : messageRetryEndFlg.hashCode());
		result = prime * result + ((messageRetryEndValue == null) ? 0 : messageRetryEndValue.hashCode());
		result = prime * result + ((processingMethod == null) ? 0 : processingMethod.hashCode());
		result = prime * result + ((rpaAbnormalExitEndValue == null) ? 0 : rpaAbnormalExitEndValue.hashCode());
		result = prime * result + ((rpaAbnormalExitNotify == null) ? 0 : rpaAbnormalExitNotify.hashCode());
		result = prime * result
				+ ((rpaAbnormalExitNotifyPriority == null) ? 0 : rpaAbnormalExitNotifyPriority.hashCode());
		result = prime * result + ((rpaScreenshotEndValue == null) ? 0 : rpaScreenshotEndValue.hashCode());
		result = prime * result
				+ ((rpaScreenshotEndValueCondition == null) ? 0 : rpaScreenshotEndValueCondition.hashCode());
		result = prime * result + ((rpaAlreadyRunningEndValue == null) ? 0 : rpaAlreadyRunningEndValue.hashCode());
		result = prime * result + ((rpaAlreadyRunningNotify == null) ? 0 : rpaAlreadyRunningNotify.hashCode());
		result = prime * result
				+ ((rpaAlreadyRunningNotifyPriority == null) ? 0 : rpaAlreadyRunningNotifyPriority.hashCode());
		result = prime * result + ((rpaCheckConnectTimeout == null) ? 0 : rpaCheckConnectTimeout.hashCode());
		result = prime * result + ((rpaCheckEndFlg == null) ? 0 : rpaCheckEndFlg.hashCode());
		result = prime * result + ((rpaCheckEndValue == null) ? 0 : rpaCheckEndValue.hashCode());
		result = prime * result + ((rpaCheckRequestTimeout == null) ? 0 : rpaCheckRequestTimeout.hashCode());
		result = prime * result + ((rpaCheckRetry == null) ? 0 : rpaCheckRetry.hashCode());
		result = prime * result + ((rpaDefaultEndValue == null) ? 0 : rpaDefaultEndValue.hashCode());
		result = prime * result + ((rpaExeFilepath == null) ? 0 : rpaExeFilepath.hashCode());
		result = prime * result + ((rpaJobCheckEndValueInfos == null) ? 0 : rpaJobCheckEndValueInfos.hashCode());
		result = prime * result
				+ ((rpaJobEndValueConditionInfos == null) ? 0 : rpaJobEndValueConditionInfos.hashCode());
		result = prime * result + ((rpaJobOptionInfos == null) ? 0 : rpaJobOptionInfos.hashCode());
		result = prime * result + ((rpaJobRunParamInfos == null) ? 0 : rpaJobRunParamInfos.hashCode());
		result = prime * result + ((rpaJobType == null) ? 0 : rpaJobType.hashCode());
		result = prime * result + ((rpaToolId == null) ? 0 : rpaToolId.hashCode());
		result = prime * result + ((rpaLogDirectory == null) ? 0 : rpaLogDirectory.hashCode());
		result = prime * result + ((rpaLogEncoding == null) ? 0 : rpaLogEncoding.hashCode());
		result = prime * result + ((rpaLogFileName == null) ? 0 : rpaLogFileName.hashCode());
		result = prime * result + ((rpaLogMaxBytes == null) ? 0 : rpaLogMaxBytes.hashCode());
		result = prime * result + ((rpaLogPatternHead == null) ? 0 : rpaLogPatternHead.hashCode());
		result = prime * result + ((rpaLogPatternTail == null) ? 0 : rpaLogPatternTail.hashCode());
		result = prime * result + ((rpaLogReturnCode == null) ? 0 : rpaLogReturnCode.hashCode());
		result = prime * result + ((rpaLoginEndValue == null) ? 0 : rpaLoginEndValue.hashCode());
		result = prime * result + ((rpaLoginFlg == null) ? 0 : rpaLoginFlg.hashCode());
		result = prime * result + ((rpaLoginPassword == null) ? 0 : rpaLoginPassword.hashCode());
		result = prime * result + ((rpaLoginResolution == null) ? 0 : rpaLoginResolution.hashCode());
		result = prime * result + ((rpaLoginRetry == null) ? 0 : rpaLoginRetry.hashCode());
		result = prime * result + ((rpaLoginUserId == null) ? 0 : rpaLoginUserId.hashCode());
		result = prime * result + ((rpaLogoutFlg == null) ? 0 : rpaLogoutFlg.hashCode());
		result = prime * result + ((rpaNotLoginEndValue == null) ? 0 : rpaNotLoginEndValue.hashCode());
		result = prime * result + ((rpaNotLoginNotify == null) ? 0 : rpaNotLoginNotify.hashCode());
		result = prime * result + ((rpaNotLoginNotifyPriority == null) ? 0 : rpaNotLoginNotifyPriority.hashCode());
		result = prime * result + ((rpaRunConnectTimeout == null) ? 0 : rpaRunConnectTimeout.hashCode());
		result = prime * result + ((rpaRunEndFlg == null) ? 0 : rpaRunEndFlg.hashCode());
		result = prime * result + ((rpaRunEndValue == null) ? 0 : rpaRunEndValue.hashCode());
		result = prime * result + ((rpaRunRequestTimeout == null) ? 0 : rpaRunRequestTimeout.hashCode());
		result = prime * result + ((rpaRunRetry == null) ? 0 : rpaRunRetry.hashCode());
		result = prime * result + ((rpaRunType == null) ? 0 : rpaRunType.hashCode());
		result = prime * result + ((rpaScenarioFilepath == null) ? 0 : rpaScenarioFilepath.hashCode());
		result = prime * result + ((rpaScenarioParam == null) ? 0 : rpaScenarioParam.hashCode());
		result = prime * result + ((rpaScopeId == null) ? 0 : rpaScopeId.hashCode());
		result = prime * result + ((rpaScreenshotEndValueFlg == null) ? 0 : rpaScreenshotEndValueFlg.hashCode());
		result = prime * result + ((rpaScreenshotEndDelayFlg == null) ? 0 : rpaScreenshotEndDelayFlg.hashCode());
		result = prime * result + ((rpaStopMode == null) ? 0 : rpaStopMode.hashCode());
		result = prime * result + ((rpaStopType == null) ? 0 : rpaStopType.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RpaJobInfo other = (RpaJobInfo) obj;
		if (commandRetry == null) {
			if (other.commandRetry != null)
				return false;
		} else if (!commandRetry.equals(other.commandRetry))
			return false;
		if (commandRetryEndStatus == null) {
			if (other.commandRetryEndStatus != null)
				return false;
		} else if (!commandRetryEndStatus.equals(other.commandRetryEndStatus))
			return false;
		if (commandRetryFlg == null) {
			if (other.commandRetryFlg != null)
				return false;
		} else if (!commandRetryFlg.equals(other.commandRetryFlg))
			return false;
		if (facilityID == null) {
			if (other.facilityID != null)
				return false;
		} else if (!facilityID.equals(other.facilityID))
			return false;
		if (messageRetry == null) {
			if (other.messageRetry != null)
				return false;
		} else if (!messageRetry.equals(other.messageRetry))
			return false;
		if (messageRetryEndFlg == null) {
			if (other.messageRetryEndFlg != null)
				return false;
		} else if (!messageRetryEndFlg.equals(other.messageRetryEndFlg))
			return false;
		if (messageRetryEndValue == null) {
			if (other.messageRetryEndValue != null)
				return false;
		} else if (!messageRetryEndValue.equals(other.messageRetryEndValue))
			return false;
		if (processingMethod == null) {
			if (other.processingMethod != null)
				return false;
		} else if (!processingMethod.equals(other.processingMethod))
			return false;
		if (rpaAbnormalExitEndValue == null) {
			if (other.rpaAbnormalExitEndValue != null)
				return false;
		} else if (!rpaAbnormalExitEndValue.equals(other.rpaAbnormalExitEndValue))
			return false;
		if (rpaAbnormalExitNotify == null) {
			if (other.rpaAbnormalExitNotify != null)
				return false;
		} else if (!rpaAbnormalExitNotify.equals(other.rpaAbnormalExitNotify))
			return false;
		if (rpaAbnormalExitNotifyPriority == null) {
			if (other.rpaAbnormalExitNotifyPriority != null)
				return false;
		} else if (!rpaAbnormalExitNotifyPriority.equals(other.rpaAbnormalExitNotifyPriority))
			return false;
		if (rpaScreenshotEndValue == null) {
			if (other.rpaScreenshotEndValue != null)
				return false;
		} else if (!rpaScreenshotEndValue.equals(other.rpaScreenshotEndValue))
			return false;
		if (rpaScreenshotEndValueCondition == null) {
			if (other.rpaScreenshotEndValueCondition != null)
				return false;
		} else if (!rpaScreenshotEndValueCondition.equals(other.rpaScreenshotEndValueCondition))
			return false;
		if (rpaAlreadyRunningEndValue == null) {
			if (other.rpaAlreadyRunningEndValue != null)
				return false;
		} else if (!rpaAlreadyRunningEndValue.equals(other.rpaAlreadyRunningEndValue))
			return false;
		if (rpaAlreadyRunningNotify == null) {
			if (other.rpaAlreadyRunningNotify != null)
				return false;
		} else if (!rpaAlreadyRunningNotify.equals(other.rpaAlreadyRunningNotify))
			return false;
		if (rpaAlreadyRunningNotifyPriority == null) {
			if (other.rpaAlreadyRunningNotifyPriority != null)
				return false;
		} else if (!rpaAlreadyRunningNotifyPriority.equals(other.rpaAlreadyRunningNotifyPriority))
			return false;
		if (rpaCheckConnectTimeout == null) {
			if (other.rpaCheckConnectTimeout != null)
				return false;
		} else if (!rpaCheckConnectTimeout.equals(other.rpaCheckConnectTimeout))
			return false;
		if (rpaCheckEndFlg == null) {
			if (other.rpaCheckEndFlg != null)
				return false;
		} else if (!rpaCheckEndFlg.equals(other.rpaCheckEndFlg))
			return false;
		if (rpaCheckEndValue == null) {
			if (other.rpaCheckEndValue != null)
				return false;
		} else if (!rpaCheckEndValue.equals(other.rpaCheckEndValue))
			return false;
		if (rpaCheckRequestTimeout == null) {
			if (other.rpaCheckRequestTimeout != null)
				return false;
		} else if (!rpaCheckRequestTimeout.equals(other.rpaCheckRequestTimeout))
			return false;
		if (rpaCheckRetry == null) {
			if (other.rpaCheckRetry != null)
				return false;
		} else if (!rpaCheckRetry.equals(other.rpaCheckRetry))
			return false;
		if (rpaDefaultEndValue == null) {
			if (other.rpaDefaultEndValue != null)
				return false;
		} else if (!rpaDefaultEndValue.equals(other.rpaDefaultEndValue))
			return false;
		if (rpaExeFilepath == null) {
			if (other.rpaExeFilepath != null)
				return false;
		} else if (!rpaExeFilepath.equals(other.rpaExeFilepath))
			return false;
		if (!JobInfo.equalsArray(rpaJobCheckEndValueInfos,other.rpaJobCheckEndValueInfos))
			return false;
		if (!JobInfo.equalsArray(rpaJobEndValueConditionInfos,other.rpaJobEndValueConditionInfos))
			return false;
		if (!JobInfo.equalsArray(rpaJobOptionInfos,other.rpaJobOptionInfos))
			return false;
		if (!JobInfo.equalsArray(rpaJobRunParamInfos,other.rpaJobRunParamInfos))
			return false;
		if (rpaJobType == null) {
			if (other.rpaJobType != null)
				return false;
		} else if (!rpaJobType.equals(other.rpaJobType))
			return false;
		if (rpaToolId == null) {
			if (other.rpaToolId != null)
				return false;
		} else if (!rpaToolId.equals(other.rpaToolId))
			return false;
		if (rpaLogDirectory == null) {
			if (other.rpaLogDirectory != null)
				return false;
		} else if (!rpaLogDirectory.equals(other.rpaLogDirectory))
			return false;
		if (rpaLogEncoding == null) {
			if (other.rpaLogEncoding != null)
				return false;
		} else if (!rpaLogEncoding.equals(other.rpaLogEncoding))
			return false;
		if (rpaLogFileName == null) {
			if (other.rpaLogFileName != null)
				return false;
		} else if (!rpaLogFileName.equals(other.rpaLogFileName))
			return false;
		if (rpaLogMaxBytes == null) {
			if (other.rpaLogMaxBytes != null)
				return false;
		} else if (!rpaLogMaxBytes.equals(other.rpaLogMaxBytes))
			return false;
		if (rpaLogPatternHead == null) {
			if (other.rpaLogPatternHead != null)
				return false;
		} else if (!rpaLogPatternHead.equals(other.rpaLogPatternHead))
			return false;
		if (rpaLogPatternTail == null) {
			if (other.rpaLogPatternTail != null)
				return false;
		} else if (!rpaLogPatternTail.equals(other.rpaLogPatternTail))
			return false;
		if (rpaLogReturnCode == null) {
			if (other.rpaLogReturnCode != null)
				return false;
		} else if (!rpaLogReturnCode.equals(other.rpaLogReturnCode))
			return false;
		if (rpaLoginEndValue == null) {
			if (other.rpaLoginEndValue != null)
				return false;
		} else if (!rpaLoginEndValue.equals(other.rpaLoginEndValue))
			return false;
		if (rpaLoginFlg == null) {
			if (other.rpaLoginFlg != null)
				return false;
		} else if (!rpaLoginFlg.equals(other.rpaLoginFlg))
			return false;
		if (rpaLoginPassword == null) {
			if (other.rpaLoginPassword != null)
				return false;
		} else if (!rpaLoginPassword.equals(other.rpaLoginPassword))
			return false;
		if (rpaLoginResolution == null) {
			if (other.rpaLoginResolution != null)
				return false;
		} else if (!rpaLoginResolution.equals(other.rpaLoginResolution))
			return false;
		if (rpaLoginRetry == null) {
			if (other.rpaLoginRetry != null)
				return false;
		} else if (!rpaLoginRetry.equals(other.rpaLoginRetry))
			return false;
		if (rpaLoginUserId == null) {
			if (other.rpaLoginUserId != null)
				return false;
		} else if (!rpaLoginUserId.equals(other.rpaLoginUserId))
			return false;
		if (rpaLogoutFlg == null) {
			if (other.rpaLogoutFlg != null)
				return false;
		} else if (!rpaLogoutFlg.equals(other.rpaLogoutFlg))
			return false;
		if (rpaNotLoginEndValue == null) {
			if (other.rpaNotLoginEndValue != null)
				return false;
		} else if (!rpaNotLoginEndValue.equals(other.rpaNotLoginEndValue))
			return false;
		if (rpaNotLoginNotify == null) {
			if (other.rpaNotLoginNotify != null)
				return false;
		} else if (!rpaNotLoginNotify.equals(other.rpaNotLoginNotify))
			return false;
		if (rpaNotLoginNotifyPriority == null) {
			if (other.rpaNotLoginNotifyPriority != null)
				return false;
		} else if (!rpaNotLoginNotifyPriority.equals(other.rpaNotLoginNotifyPriority))
			return false;
		if (rpaRunConnectTimeout == null) {
			if (other.rpaRunConnectTimeout != null)
				return false;
		} else if (!rpaRunConnectTimeout.equals(other.rpaRunConnectTimeout))
			return false;
		if (rpaRunEndFlg == null) {
			if (other.rpaRunEndFlg != null)
				return false;
		} else if (!rpaRunEndFlg.equals(other.rpaRunEndFlg))
			return false;
		if (rpaRunEndValue == null) {
			if (other.rpaRunEndValue != null)
				return false;
		} else if (!rpaRunEndValue.equals(other.rpaRunEndValue))
			return false;
		if (rpaRunRequestTimeout == null) {
			if (other.rpaRunRequestTimeout != null)
				return false;
		} else if (!rpaRunRequestTimeout.equals(other.rpaRunRequestTimeout))
			return false;
		if (rpaRunRetry == null) {
			if (other.rpaRunRetry != null)
				return false;
		} else if (!rpaRunRetry.equals(other.rpaRunRetry))
			return false;
		if (rpaRunType == null) {
			if (other.rpaRunType != null)
				return false;
		} else if (!rpaRunType.equals(other.rpaRunType))
			return false;
		if (rpaScenarioFilepath == null) {
			if (other.rpaScenarioFilepath != null)
				return false;
		} else if (!rpaScenarioFilepath.equals(other.rpaScenarioFilepath))
			return false;
		if (rpaScenarioParam == null) {
			if (other.rpaScenarioParam != null)
				return false;
		} else if (!rpaScenarioParam.equals(other.rpaScenarioParam))
			return false;
		if (rpaScopeId == null) {
			if (other.rpaScopeId != null)
				return false;
		} else if (!rpaScopeId.equals(other.rpaScopeId))
			return false;
		if (rpaScreenshotEndValueFlg == null) {
			if (other.rpaScreenshotEndValueFlg != null)
				return false;
		} else if (!rpaScreenshotEndValueFlg.equals(other.rpaScreenshotEndValueFlg))
			return false;
		if (rpaScreenshotEndDelayFlg == null) {
			if (other.rpaScreenshotEndDelayFlg != null)
				return false;
		} else if (!rpaScreenshotEndDelayFlg.equals(other.rpaScreenshotEndDelayFlg))
			return false;
		if (rpaStopMode == null) {
			if (other.rpaStopMode != null)
				return false;
		} else if (!rpaStopMode.equals(other.rpaStopMode))
			return false;
		if (rpaStopType == null) {
			if (other.rpaStopType != null)
				return false;
		} else if (!rpaStopType.equals(other.rpaStopType))
			return false;
		if (scope == null) {
			if (other.scope != null)
				return false;
		} else if (!scope.equals(other.scope))
			return false;
		return true;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
		if (rpaJobOptionInfos != null) {
			for (RpaJobOptionInfo req : rpaJobOptionInfos) {
				req.correlationCheck();
			}
		}
		if (rpaJobEndValueConditionInfos != null) {
			for (RpaJobEndValueConditionInfo req : rpaJobEndValueConditionInfos) {
				req.correlationCheck();
			}
		}
		if (rpaJobRunParamInfos != null) {
			for (RpaJobRunParamInfo req : rpaJobRunParamInfos) {
				req.correlationCheck();
			}
		}
		if (rpaJobCheckEndValueInfos != null) {
			for (RpaJobCheckEndValueInfo req : rpaJobCheckEndValueInfos) {
				req.correlationCheck();
			}
		}
	}
}
