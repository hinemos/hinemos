/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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