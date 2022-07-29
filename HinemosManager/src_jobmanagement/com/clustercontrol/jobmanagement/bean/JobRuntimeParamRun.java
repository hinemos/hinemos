/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;


/**
 * ジョブ実行契機の直接実行時のランタイムジョブ変数情報を保持するクラス<BR>
 * 
 */
public class JobRuntimeParamRun implements Serializable {

	/**
	 * シリアライズ可能クラスに定義するUID
	 */
	private static final long serialVersionUID = 1L;

	/** 変数名 */
	private String paramId;
	/** ランタイムジョブ変数値 */
	private String value;

	public String getParamId() {
		return paramId;
	}
	public void setParamId(String paramId) {
		this.paramId = paramId;
	}

	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		String str = null;
		str += "paramId=" + paramId;
		str += " ,value=" + value;
		return str;
	}
}