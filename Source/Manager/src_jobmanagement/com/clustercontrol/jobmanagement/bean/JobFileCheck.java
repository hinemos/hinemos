/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;



/**
 * ジョブ実行契機[ファイルチェック]に関する情報を保持するクラス<BR>
 * 
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobFileCheck extends JobKick implements Serializable {
	/** シリアライズ可能クラスに定義するUID */
	private static final long serialVersionUID = 3475488553405827413L;

	/** ファシリティID */
	private String facilityId;

	/** スコープ */
	private String scope;

	/** ディレクトリ */
	private String directory;

	/** ファイル名 */
	private String fileName;

	/** ファイルチェック種別 */
	private Integer eventType;

	/** ファイルチェック種別 - 変更種別 */
	private Integer modifyType;

	/** ファイルが使用されている場合判定を持ち越す */
	private Boolean carryOverJudgmentFlg;

	/**
	 * ファイルチェック設定定義
	 */
	public JobFileCheck(){
		this.type = JobKickConstant.TYPE_FILECHECK;
	}
	/**
	 * ファイルチェック設定定義
	 * @param facilityId
	 * @param directory
	 * @param fileName
	 * @param fileVariable
	 * @param checkInterval
	 * @param eventType
	 * @param modifyType
	 * @param carryOverJudgmentFlg
	 */
	public JobFileCheck(String facilityId,
			String directory, String fileName, String fileVariable,
			int checkInterval, int eventType ,int modifyType, boolean carryOverJudgmentFlg) {
		super();
		
		this.type = JobKickConstant.TYPE_FILECHECK;
		this.facilityId = facilityId;
		this.directory = directory;
		this.fileName = fileName;
		this.eventType = eventType;
		this.modifyType = modifyType;
		this.carryOverJudgmentFlg = carryOverJudgmentFlg;
	}

	/**
	 * ファシリティIDを返す<BR>
	 * @return ファシリティID
	 */
	public String getFacilityId() {
		return facilityId;
	}
	/**
	 * ファシリティIDを設定する<BR>
	 * @param facilityId ファシリティID
	 */
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	/**
	 * スコープを返す<BR>
	 * @return ファシリティID
	 */
	public String getScope() {
		return scope;
	}
	/**
	 * スコープを設定する<BR>
	 * @param scope スコープ
	 */
	public void setScope(String scope) {
		this.scope = scope;
	}

	/**
	 * ディレクトリを返す<BR>
	 * @return ディレクトリ
	 */
	public String getDirectory() {
		return directory;
	}
	/**
	 * ディレクトリを設定する<BR>
	 * @param directory ディレクトリ
	 */
	public void setDirectory(String directory) {
		this.directory = directory;
	}

	/**
	 * ファイル名を返す<BR>
	 * @return ファイル名
	 */
	public String getFileName() {
		return fileName;
	}
	/**
	 * ファイル名を設定する<BR>
	 * @param fileName ファイル名
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * ファイルチェック種別を返す<BR>
	 * @return ファイルチェック種別
	 */
	public Integer getEventType() {
		return eventType;
	}
	/**
	 * ファイルチェック種別を設定する<BR>
	 * @param eventType ファイルチェック種別
	 */
	public void setEventType(Integer eventType) {
		this.eventType = eventType;
	}
	/**
	 * ファイルチェック種別 - 変更種別を返す<BR>
	 * @return ファイルチェック種別 - 変更種別
	 */
	public Integer getModifyType() {
		return modifyType;
	}
	/**
	 * ファイルチェック種別 - 変更種別を設定する<BR>
	 * @param modifyType ファイルチェック種別 - 変更種別
	 */
	public void setModifyType(Integer modifyType) {
		this.modifyType = modifyType;
	}
	/**
	 * ファイルが使用されている場合判定を持ち越すを返す<BR>
	 * @return ファイルが使用されている場合判定を持ち越す
	 */
	@XmlTransient
	public Boolean getCarryOverJudgmentFlg() {
		return carryOverJudgmentFlg;
	}
	/**
	 * ファイルが使用されている場合判定を持ち越すを設定する<BR>
	 * @param carryOverJudgmentFlg ファイルが使用されている場合判定を持ち越す
	 */
	public void setCarryOverJudgmentFlg(Boolean carryOverJudgmentFlg) {
		this.carryOverJudgmentFlg = carryOverJudgmentFlg;
	}
	@Override
	public String toString() {
		String str = null;
		str += "type=" + type;
		str += " ,facilityId=" + facilityId;
		str += " ,directory=" + directory;
		str += " ,fileName=" + fileName;
		str += " ,eventType=" + eventType;
		str += " ,modifyType=" + modifyType;
		str += " ,carryOverJudgmentFlg=" + carryOverJudgmentFlg;
		return str;
	}
}