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
 * ジョブ実行契機 ランタイムジョブ変数情報詳細を保持するクラス<BR>
 * 
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobRuntimeParamDetail implements Serializable {

	/**
	 * シリアライズ可能クラスに定義するUID
	 */
	private static final long serialVersionUID = 1L;

	/** 値 */
	private String m_paramValue;
	/** 説明 */
	private String m_description;

	public String getParamValue() {
		return m_paramValue;
	}
	public void setParamValue(String paramValue) {
		this.m_paramValue = paramValue;
	}

	public String getDescription() {
		return m_description;
	}
	public void setDescription(String description) {
		this.m_description = description;
	}

	@Override
	public String toString() {
		String str = null;
		str += "m_paramValue=" + m_paramValue;
		str += " ,m_description=" + m_description;
		return str;
	}
}