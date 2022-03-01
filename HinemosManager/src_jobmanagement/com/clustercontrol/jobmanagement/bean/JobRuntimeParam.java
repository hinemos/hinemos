/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
	private String paramId;
	/** 種別 */
	private Integer paramType;
	/** 値（デフォルト値） */
	private String value;
	/** 説明 */
	private String description;
	/** 必須フラグ */
	private Boolean requiredFlg = false;
	/** ランタイムジョブ変数詳細情報 */
	private ArrayList<JobRuntimeParamDetail> jobRuntimeParamDetailList;


	public String getParamId() {
		return paramId;
	}
	public void setParamId(String paramId) {
		this.paramId = paramId;
	}

	public Integer getParamType() {
		return paramType;
	}
	public void setParamType(Integer paramType) {
		this.paramType = paramType;
	}

	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getRequiredFlg() {
		return requiredFlg;
	}
	public void setRequiredFlg(Boolean requiredFlg) {
		this.requiredFlg = requiredFlg;
	}

	public ArrayList<JobRuntimeParamDetail> getJobRuntimeParamDetailList() {
		return jobRuntimeParamDetailList;
	}
	public void setJobRuntimeParamDetailList(ArrayList<JobRuntimeParamDetail> jobRuntimeParamDetailList) {
		this.jobRuntimeParamDetailList = jobRuntimeParamDetailList;
	}

	@Override
	public String toString() {
		String str = null;
		str += "paramId=" + paramId;
		str += "paramType=" + paramType;
		str += " ,value=" + value;
		str += " ,description=" + description;
		str += " ,requiredFlg=" + requiredFlg;
		return str;
	}
}