/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.filecheck;

import java.util.Objects;

import org.openapitools.client.model.AgtJobFileCheckResponse;

/**
 * ファイルチェックの情報を管理するクラス<BR>
 * 
 * 実行契機とジョブで共通で利用する<BR>
 */
public class FileCheckInfo {
	private String key;
	/** ファイル名（ファイル名判定用の文字列） */
	private String fileNamePattern;
	/** チェック種別 - 作成 */
	private boolean createValidFlg = false;
	/** チェック開始前に存在するファイルを判定に含めるか */
	private boolean createBeforeInitFlg = false;
	/** チェック種別 - 削除 */
	private boolean deleteValidFlg = false;
	/** チェック種別 - 変更 */
	private boolean modifyValidFlg = false;
	/** 変更判定（タイムスタンプ変更/ファイルサイズ変更） */
	private Integer modifyType;
	/** ファイルの使用中は判定しないか */
	private boolean notJudgeFileInUseFlg = false;

	/** ファイルチェック実行契機情報 */
	private AgtJobFileCheckResponse agtJobFileCheckResponse;

	// -----以下のフィールドは条件を満たしてから格納される
	/** ファイル名（条件を満たした実ファイル名） */
	private String passedFileName;
	/** 条件を満たしたチェック種別 */
	private int passedEventType;
	/** 条件に一致したファイルのファイル更新日時 */
	private Long fileTimestamp = null;
	/** 条件に一致したファイルのファイルサイズ */
	private Long fileSize = null;

	/**
	 * コンストラクタ
	 */
	public FileCheckInfo(String key) {
		Objects.requireNonNull(key, "key");
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	/**
	 * 判定持ち越し用のKey<BR>
	 * 実際に判定を通過したファイル名＋keyの値で管理する。<BR>
	 * ※判定を通過していない場合には用いないこと<BR>
	 * 
	 * @return
	 */
	public String getCarryOverKey() {
		Objects.requireNonNull(passedFileName, "passedFileName");
		return passedFileName + "_" + key;
	}

	public String getFileNamePattern() {
		return fileNamePattern;
	}

	public void setFileNamePattern(String fileNamePattern) {
		this.fileNamePattern = fileNamePattern;
	}

	public boolean isCreateValidFlg() {
		return createValidFlg;
	}

	public void setCreateValidFlg(boolean createValidFlg) {
		this.createValidFlg = createValidFlg;
	}

	public boolean isCreateBeforeInitFlg() {
		return createBeforeInitFlg;
	}

	public void setCreateBeforeInitFlg(boolean createBeforeInitFlg) {
		this.createBeforeInitFlg = createBeforeInitFlg;
	}

	public boolean isDeleteValidFlg() {
		return deleteValidFlg;
	}

	public void setDeleteValidFlg(boolean deleteValidFlg) {
		this.deleteValidFlg = deleteValidFlg;
	}

	public boolean isModifyValidFlg() {
		return modifyValidFlg;
	}

	public void setModifyValidFlg(boolean modifyValidFlg) {
		this.modifyValidFlg = modifyValidFlg;
	}

	public Integer getModifyType() {
		return modifyType;
	}

	public void setModifyType(Integer modifyType) {
		this.modifyType = modifyType;
	}

	public boolean isNotJudgeFileInUseFlg() {
		return notJudgeFileInUseFlg;
	}

	public void setNotJudgeFileInUseFlg(boolean notJudgeFileInUseFlg) {
		this.notJudgeFileInUseFlg = notJudgeFileInUseFlg;
	}

	public AgtJobFileCheckResponse getAgtJobFileCheckResponse() {
		return agtJobFileCheckResponse;
	}

	public void setAgtJobFileCheckResponse(AgtJobFileCheckResponse agtJobFileCheckResponse) {
		this.agtJobFileCheckResponse = agtJobFileCheckResponse;
	}

	public String getPassedFileName() {
		return passedFileName;
	}

	public void setPassedFileName(String passedFileName) {
		this.passedFileName = passedFileName;
	}

	public int getPassedEventType() {
		return passedEventType;
	}

	public void setPassedEventType(int passedEventType) {
		this.passedEventType = passedEventType;
	}

	public Long getFileTimestamp() {
		return fileTimestamp;
	}

	public void setFileTimestamp(Long fileTimestamp) {
		this.fileTimestamp = fileTimestamp;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}
}
