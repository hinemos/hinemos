/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.jobmanagement.bean.JobLinkInheritKeyInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;

public class JobLinkInheritInfoResponse {

	/** ジョブ変数 */
	private String paramId;

	/** メッセージ情報 */
	@RestBeanConvertEnum
	private JobLinkInheritKeyInfo keyInfo;

	/** 拡張情報キー */
	private String expKey;

	/**
	 * ジョブ変数を返す。<BR>
	 * @return ジョブ変数
	 */
	public String getParamId() {
		return paramId;
	}

	/**
	 * ジョブ変数を設定する。<BR>
	 * @param value ジョブ変数
	 */
	public void setParamId(String paramId) {
		this.paramId = paramId;
	}

	/**
	 * メッセージ情報を返す。<BR>
	 * @return メッセージ情報
	 */
	public JobLinkInheritKeyInfo getKeyInfo() {
		return keyInfo;
	}
	/**
	 * メッセージ情報を設定する。<BR>
	 * @param key メッセージ情報
	 */
	public void setKeyInfo(JobLinkInheritKeyInfo keyInfo) {
		this.keyInfo = keyInfo;
	}

	/**
	 * 拡張情報キーを返す。<BR>
	 * @return 拡張情報キー
	 */
	public String getExpKey() {
		return expKey;
	}

	/**
	 * 拡張情報キーを設定する。<BR>
	 * @param value 拡張情報キー
	 */
	public void setExpKey(String expKey) {
		this.expKey = expKey;
	}
}
