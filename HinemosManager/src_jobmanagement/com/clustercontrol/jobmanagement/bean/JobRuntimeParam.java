/*

Copyright (C) 2016 NTT DATA Corporation

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
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlType;


/**
 * ジョブ実行契機 ランタイムジョブ変数情報を保持するクラス<BR>
 * 
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class JobRuntimeParam implements Serializable {

	/**
	 * シリアライズ可能クラスに定義するUID
	 */
	private static final long serialVersionUID = 1L;

	/** 名前 */
	private String m_paramId;
	/** 種別 */
	private Integer m_paramType;
	/** 値（デフォルト値） */
	private String m_value;
	/** 説明 */
	private String m_description;
	/** 必須フラグ */
	private Boolean m_requiredFlg = false;
	/** ランタイムジョブ変数詳細情報 */
	private ArrayList<JobRuntimeParamDetail> m_jobRuntimeParamDetailList;


	public String getParamId() {
		return m_paramId;
	}
	public void setParamId(String paramId) {
		this.m_paramId = paramId;
	}

	public Integer getParamType() {
		return m_paramType;
	}
	public void setParamType(Integer paramType) {
		this.m_paramType = paramType;
	}

	public String getValue() {
		return m_value;
	}
	public void setValue(String value) {
		this.m_value = value;
	}

	public String getDescription() {
		return m_description;
	}
	public void setDescription(String description) {
		this.m_description = description;
	}

	public Boolean getRequiredFlg() {
		return m_requiredFlg;
	}
	public void setRequiredFlg(Boolean requiredFlg) {
		this.m_requiredFlg = requiredFlg;
	}

	public ArrayList<JobRuntimeParamDetail> getJobRuntimeParamDetailList() {
		return m_jobRuntimeParamDetailList;
	}
	public void setJobRuntimeParamDetailList(ArrayList<JobRuntimeParamDetail> jobRuntimeParamDetailList) {
		this.m_jobRuntimeParamDetailList = jobRuntimeParamDetailList;
	}

	@Override
	public String toString() {
		String str = null;
		str += "m_paramId=" + m_paramId;
		str += "m_paramType=" + m_paramType;
		str += " ,m_value=" + m_value;
		str += " ,m_description=" + m_description;
		str += " ,m_requiredFlg=" + m_requiredFlg;
		return str;
	}
}