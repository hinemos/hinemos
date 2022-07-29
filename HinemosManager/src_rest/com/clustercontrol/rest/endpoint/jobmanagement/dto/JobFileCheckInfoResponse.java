/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.FileCheckModifyTypeEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ScopeJudgmentTargetEnum;

public class JobFileCheckInfoResponse {

	/** ファシリティID */
	private String facilityID;

	/** スコープ */
	@RestPartiallyTransrateTarget
	private String scope;

	/** スコープ処理 */
	@RestBeanConvertEnum
	private ScopeJudgmentTargetEnum processingMethod;

	/** 条件を満たした場合の終了値 */
	private Integer successEndValue;

	/** 条件を満たさない場合に終了するか */
	private Boolean failureEndFlg;

	/** タイムアウト（分） */
	private Integer failureWaitTime;

	/** 条件を満たさない場合の終了値 */
	private Integer failureEndValue;

	/** ディレクトリ */
	private String directory;

	/** ファイル名（正規表現） */
	private String fileName;

	/** チェック種別 - 作成 */
	private Boolean createValidFlg;

	/** ジョブ開始前に作成されたファイルも対象とする */
	private Boolean createBeforeJobStartFlg;

	/** チェック種別 - 削除 */
	private Boolean deleteValidFlg;

	/** チェック種別 - 変更 */
	private Boolean modifyValidFlg;

	/** 変更判定（タイムスタンプ変更/ファイルサイズ変更） */
	@RestBeanConvertEnum
	private FileCheckModifyTypeEnum modifyType;

	/** ファイルの使用中は判定しないか */
	private Boolean notJudgeFileInUseFlg;

	/** リトライ回数 */
	private Integer messageRetry;

	/** コマンド実行失敗時終了フラグ */
	private Boolean messageRetryEndFlg = false;

	/** コマンド実行失敗時終了値 */
	private Integer messageRetryEndValue = 0;

	/**
	 * ファシリティIDを返す。<BR>
	 * @return ファシリティID
	 */
	public String getFacilityID() {
		return facilityID;
	}

	/**
	 * ファシリティIDを設定する。<BR>
	 * @param facilityID ファシリティID
	 */
	public void setFacilityID(String facilityID) {
		this.facilityID = facilityID;
	}

	/**
	 * スコープを返す。<BR>
	 * @return スコープ
	 */
	public String getScope() {
		return scope;
	}

	/**
	 * スコープを設定する。<BR>
	 * @param scope スコープ
	 */
	public void setScope(String scope) {
		this.scope = scope;
	}

	/**
	 * スコープ処理を返す。<BR>
	 * @return スコープ処理
	 */
	public ScopeJudgmentTargetEnum getProcessingMethod() {
		return processingMethod;
	}

	/**
	 * スコープ処理を設定する。<BR>
	 * @param processingMethod スコープ処理
	 */
	public void setProcessingMethod(ScopeJudgmentTargetEnum processingMethod) {
		this.processingMethod = processingMethod;
	}

	/**
	 * 条件を満たした場合の終了値を返す。<BR>
	 * @return 条件を満たした場合の終了値
	 */
	public Integer getSuccessEndValue() {
		return successEndValue;
	}

	/**
	 * 条件を満たした場合の終了値を設定する。<BR>
	 * @param successEndValue 条件を満たした場合の終了値
	 */
	public void setSuccessEndValue(Integer successEndValue) {
		this.successEndValue = successEndValue;
	}

	/**
	 * 条件を満たさない場合に終了するかを返す。<BR>
	 * @return 条件を満たさない場合に終了するか
	 */
	public Boolean getFailureEndFlg() {
		return failureEndFlg;
	}

	/**
	 * 条件を満たさない場合に終了するかを設定する。<BR>
	 * @param failureEndFlg 条件を満たさない場合に終了するか
	 */
	public void setFailureEndFlg(Boolean failureEndFlg) {
		this.failureEndFlg = failureEndFlg;
	}

	/**
	 * タイムアウト（分）を返す。<BR>
	 * @return タイムアウト（分）
	 */
	public Integer getFailureWaitTime() {
		return failureWaitTime;
	}

	/**
	 * タイムアウト（分）を設定する。<BR>
	 * @param failureWaitTime タイムアウト（分）
	 */
	public void setFailureWaitTime(Integer failureWaitTime) {
		this.failureWaitTime = failureWaitTime;
	}

	/**
	 * 条件を満たさない場合の終了値を返す。<BR>
	 * @return 条件を満たさない場合の終了値
	 */
	public Integer getFailureEndValue() {
		return failureEndValue;
	}

	/**
	 * 条件を満たさない場合の終了値を設定する。<BR>
	 * @param failureEndValue 条件を満たさない場合の終了値
	 */
	public void setFailureEndValue(Integer failureEndValue) {
		this.failureEndValue = failureEndValue;
	}

	/**
	 * ディレクトリを返す。<BR>
	 * @return ディレクトリ
	 */
	public String getDirectory() {
		return directory;
	}

	/**
	 * ディレクトリを設定する。<BR>
	 * @param directory ディレクトリ
	 */
	public void setDirectory(String directory) {
		this.directory = directory;
	}

	/**
	 * ファイル名（正規表現）を返す。<BR>
	 * @return ファイル名（正規表現）
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * ファイル名（正規表現）を設定する。<BR>
	 * @param fileName ファイル名（正規表現）
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * チェック種別 - 作成を返す。<BR>
	 * @return チェック種別 - 作成
	 */
	public Boolean getCreateValidFlg() {
		return createValidFlg;
	}

	/**
	 * チェック種別 - 作成を設定する。<BR>
	 * @param createValidFlg チェック種別 - 作成
	 */
	public void setCreateValidFlg(Boolean createValidFlg) {
		this.createValidFlg = createValidFlg;
	}

	/**
	 * ジョブ開始前に作成されたファイルも対象とするを返す。<BR>
	 * @return ジョブ開始前に作成されたファイルも対象とする
	 */
	public Boolean getCreateBeforeJobStartFlg() {
		return createBeforeJobStartFlg;
	}

	/**
	 * ジョブ開始前に作成されたファイルも対象とするを設定する。<BR>
	 * @param createBeforeJobStartFlg ジョブ開始前に作成されたファイルも対象とする
	 */
	public void setCreateBeforeJobStartFlg(Boolean createBeforeJobStartFlg) {
		this.createBeforeJobStartFlg = createBeforeJobStartFlg;
	}

	/**
	 * チェック種別 - 削除を返す。<BR>
	 * @return チェック種別 - 削除
	 */
	public Boolean getDeleteValidFlg() {
		return deleteValidFlg;
	}

	/**
	 * チェック種別 - 削除を設定する。<BR>
	 * @param deleteValidFlg チェック種別 - 削除
	 */
	public void setDeleteValidFlg(Boolean deleteValidFlg) {
		this.deleteValidFlg = deleteValidFlg;
	}

	/**
	 * チェック種別 - 変更を返す。<BR>
	 * @return チェック種別 - 変更
	 */
	public Boolean getModifyValidFlg() {
		return modifyValidFlg;
	}

	/**
	 * チェック種別 - 変更を設定する。<BR>
	 * @param modifyValidFlg チェック種別 - 変更
	 */
	public void setModifyValidFlg(Boolean modifyValidFlg) {
		this.modifyValidFlg = modifyValidFlg;
	}

	/**
	 * 変更判定（タイムスタンプ変更/ファイルサイズ変更）を返す。<BR>
	 * @return 変更判定（タイムスタンプ変更/ファイルサイズ変更）
	 */
	public FileCheckModifyTypeEnum getModifyType() {
		return modifyType;
	}

	/**
	 * 変更判定（タイムスタンプ変更/ファイルサイズ変更）を設定する。<BR>
	 * @param modifyType 変更判定（タイムスタンプ変更/ファイルサイズ変更）
	 */
	public void setModifyType(FileCheckModifyTypeEnum modifyType) {
		this.modifyType = modifyType;
	}

	/**
	 * ファイルの使用中は判定しないかを返す。<BR>
	 * @return ファイルの使用中は判定しないか
	 */
	public Boolean getNotJudgeFileInUseFlg() {
		return notJudgeFileInUseFlg;
	}

	/**
	 * ファイルの使用中は判定しないかを設定する。<BR>
	 * @param notJudgeFileInUseFlg ファイルの使用中は判定しないか
	 */
	public void setNotJudgeFileInUseFlg(Boolean notJudgeFileInUseFlg) {
		this.notJudgeFileInUseFlg = notJudgeFileInUseFlg;
	}

	/**
	 * 実行失敗時終了フラグを返す。<BR>
	 * @return 実行失敗時終了フラグ
	 */
	public Boolean getMessageRetryEndFlg() {
		return messageRetryEndFlg;
	}

	/**
	 * 実行失敗時終了フラグを設定する。<BR>
	 * @param messageRetryEndFlg 実行失敗時終了フラグ
	 */
	public void setMessageRetryEndFlg(Boolean messageRetryEndFlg) {
		this.messageRetryEndFlg = messageRetryEndFlg;
	}

	/**
	 * 実行失敗時終了値を返す。<BR>
	 * @return 実行失敗時終了値
	 */
	public Integer getMessageRetryEndValue() {
		return messageRetryEndValue;
	}

	/**
	 * 実行失敗時終了値を設定する。<BR>
	 * @param messageRetryEndValue 実行失敗時終了値
	 */
	public void setMessageRetryEndValue(Integer messageRetryEndValue) {
		this.messageRetryEndValue = messageRetryEndValue;
	}

	/**
	 * リトライ回数を返す。<BR>
	 * @return リトライ回数
	 */
	public Integer getMessageRetry() {
		return messageRetry;
	}

	/**
	 * リトライ回数を設定する。<BR>
	 * @param messageRetry リトライ回数
	 */
	public void setMessageRetry(Integer messageRetry) {
		this.messageRetry = messageRetry;
	}
}
