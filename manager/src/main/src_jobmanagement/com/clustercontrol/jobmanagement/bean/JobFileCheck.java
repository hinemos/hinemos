/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;

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
	private String m_facilityId;

	/** スコープ */
	private String m_scope;

	/** ディレクトリ */
	private String m_directory;

	/** ファイル名 */
	private String m_fileName;

	/** ファイルチェック種別 */
	private Integer m_eventType;

	/** ファイルチェック種別 - 変更種別 */
	private Integer m_modifyType;

	/**
	 * ファイルチェック設定定義
	 */
	public JobFileCheck(){
		this.m_type = JobKickConstant.TYPE_FILECHECK;
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
	 */
	public JobFileCheck(String facilityId,
			String directory, String fileName, String fileVariable,
			int checkInterval, int eventType ,int modifyType) {
		super();
		
		this.m_type = JobKickConstant.TYPE_FILECHECK;
		this.m_facilityId = facilityId;
		this.m_directory = directory;
		this.m_fileName = fileName;
		this.m_eventType = eventType;
		this.m_modifyType = modifyType;
	}

	/**
	 * ファシリティIDを返す<BR>
	 * @return ファシリティID
	 */
	public String getFacilityId() {
		return m_facilityId;
	}
	/**
	 * ファシリティIDを設定する<BR>
	 * @param facilityId ファシリティID
	 */
	public void setFacilityId(String facilityId) {
		this.m_facilityId = facilityId;
	}

	/**
	 * スコープを返す<BR>
	 * @return ファシリティID
	 */
	public String getScope() {
		return m_scope;
	}
	/**
	 * スコープを設定する<BR>
	 * @param scope スコープ
	 */
	public void setScope(String scope) {
		this.m_scope = scope;
	}

	/**
	 * ディレクトリを返す<BR>
	 * @return ディレクトリ
	 */
	public String getDirectory() {
		return m_directory;
	}
	/**
	 * ディレクトリを設定する<BR>
	 * @param directory ディレクトリ
	 */
	public void setDirectory(String directory) {
		this.m_directory = directory;
	}

	/**
	 * ファイル名を返す<BR>
	 * @return ファイル名
	 */
	public String getFileName() {
		return m_fileName;
	}
	/**
	 * ファイル名を設定する<BR>
	 * @param fileName ファイル名
	 */
	public void setFileName(String fileName) {
		this.m_fileName = fileName;
	}

	/**
	 * ファイルチェック種別を返す<BR>
	 * @return ファイルチェック種別
	 */
	public Integer getEventType() {
		return m_eventType;
	}
	/**
	 * ファイルチェック種別を設定する<BR>
	 * @param eventType ファイルチェック種別
	 */
	public void setEventType(Integer eventType) {
		this.m_eventType = eventType;
	}
	/**
	 * ファイルチェック種別 - 変更種別を返す<BR>
	 * @return ファイルチェック種別 - 変更種別
	 */
	public Integer getModifyType() {
		return m_modifyType;
	}
	/**
	 * ファイルチェック種別 - 変更種別を設定する<BR>
	 * @param modifyType ファイルチェック種別 - 変更種別
	 */
	public void setModifyType(Integer modifyType) {
		this.m_modifyType = modifyType;
	}
	@Override
	public String toString() {
		String str = null;
		str += "m_type=" + m_type;
		str += " ,m_facilityId=" + m_facilityId;
		str += " ,m_directory=" + m_directory;
		str += " ,m_fileName=" + m_fileName;
		str += " ,m_eventType=" + m_eventType;
		str += " ,m_modifyType=" + m_modifyType;
		return str;
	}
}