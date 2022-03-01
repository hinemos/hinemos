/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ファイルチェックジョブに関する情報を保持するクラス
 *
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobFileCheckInfo implements Serializable {

	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = -6373704427623639627L;

	private static Log m_log = LogFactory.getLog( JobFileCheckInfo.class );

	/** ファシリティID */
	private String facilityID;

	/** スコープ */
	private String scope;

	/** スコープ処理 */
	private Integer processingMethod;

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
	private Integer modifyType;

	/** ファイルの使用中は判定しないか */
	private Boolean notJudgeFileInUseFlg;

	/** リトライ回数 */
	private Integer messageRetry;

	/** 実行失敗時終了フラグ */
	private Boolean messageRetryEndFlg = false;

	/** 実行失敗時終了値 */
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
	public Integer getProcessingMethod() {
		return processingMethod;
	}

	/**
	 * スコープ処理を設定する。<BR>
	 * @param processingMethod スコープ処理
	 */
	public void setProcessingMethod(Integer processingMethod) {
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
	public Integer getModifyType() {
		return modifyType;
	}

	/**
	 * 変更判定（タイムスタンプ変更/ファイルサイズ変更）を設定する。<BR>
	 * @param modifyType 変更判定（タイムスタンプ変更/ファイルサイズ変更）
	 */
	public void setModifyType(Integer modifyType) {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((createBeforeJobStartFlg == null) ? 0 : createBeforeJobStartFlg.hashCode());
		result = prime * result + ((createValidFlg == null) ? 0 : createValidFlg.hashCode());
		result = prime * result + ((deleteValidFlg == null) ? 0 : deleteValidFlg.hashCode());
		result = prime * result + ((directory == null) ? 0 : directory.hashCode());
		result = prime * result + ((facilityID == null) ? 0 : facilityID.hashCode());
		result = prime * result + ((failureEndFlg == null) ? 0 : failureEndFlg.hashCode());
		result = prime * result + ((failureEndValue == null) ? 0 : failureEndValue.hashCode());
		result = prime * result + ((failureWaitTime == null) ? 0 : failureWaitTime.hashCode());
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + ((modifyType == null) ? 0 : modifyType.hashCode());
		result = prime * result + ((modifyValidFlg == null) ? 0 : modifyValidFlg.hashCode());
		result = prime * result + ((notJudgeFileInUseFlg == null) ? 0 : notJudgeFileInUseFlg.hashCode());
		result = prime * result + ((processingMethod == null) ? 0 : processingMethod.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		result = prime * result + ((successEndValue == null) ? 0 : successEndValue.hashCode());
		result = prime * result + ((messageRetry == null) ? 0 : messageRetry.hashCode());
		result = prime * result + ((messageRetryEndFlg == null) ? 0 : messageRetryEndFlg.hashCode());
		result = prime * result + ((messageRetryEndValue == null) ? 0 : messageRetryEndValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof JobFileCheckInfo)) {
			return false;
		}
		JobFileCheckInfo o1 = this;
		JobFileCheckInfo o2 = (JobFileCheckInfo) obj;

		boolean ret = false;
		ret = equalsSub(o1.getFacilityID(), o2.getFacilityID()) &&
			equalsSub(o1.getScope(), o2.getScope()) &&
			equalsSub(o1.getProcessingMethod(), o2.getProcessingMethod()) &&
			equalsSub(o1.getSuccessEndValue(), o2.getSuccessEndValue()) &&
			equalsSub(o1.getFailureEndFlg(), o2.getFailureEndFlg()) &&
			equalsSub(o1.getFailureWaitTime(), o2.getFailureWaitTime()) &&
			equalsSub(o1.getFailureEndValue(), o2.getFailureEndValue()) &&
			equalsSub(o1.getDirectory(), o2.getDirectory()) &&
			equalsSub(o1.getFileName(), o2.getFileName()) &&
			equalsSub(o1.getCreateValidFlg(), o2.getCreateValidFlg()) &&
			equalsSub(o1.getCreateBeforeJobStartFlg(), o2.getCreateBeforeJobStartFlg()) &&
			equalsSub(o1.getDeleteValidFlg(), o2.getDeleteValidFlg()) &&
			equalsSub(o1.getModifyValidFlg(), o2.getModifyValidFlg()) &&
			equalsSub(o1.getModifyType(), o2.getModifyType()) &&
			equalsSub(o1.getNotJudgeFileInUseFlg(), o2.getNotJudgeFileInUseFlg()) &&
			equalsSub(o1.getMessageRetryEndFlg(), o2.getMessageRetryEndFlg()) &&
			equalsSub(o1.getMessageRetryEndValue(), o2.getMessageRetryEndValue()) &&
			equalsSub(o1.getMessageRetry(), o2.getMessageRetry());
		return ret;
	}

	private boolean equalsSub(Object o1, Object o2) {
		if (o1 == o2) {
			return true;
		}
		if (o1 == null) {
			return false;
		}
		boolean ret = o1.equals(o2);
		if (!ret) {
			if (m_log.isTraceEnabled()) {
				m_log.trace("equalsSub : " + o1 + "!=" + o2);
			}
		}
		return ret;
	}
}