/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

public class JobLinkExpInfoResponse {
	/** キー */
	private String key;

	/** 値 */
	private String value;

	/**
	 * キーを返す。<BR>
	 * @return キー
	 */
	public String getKey() {
		return key;
	}
	/**
	 * キーを設定する。<BR>
	 * @param key キー
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * 値を返す。<BR>
	 * @return 値
	 */
	public String getValue() {
		return value;
	}

	/**
	 * 値を設定する。<BR>
	 * @param value 値
	 */
	public void setValue(String value) {
		this.value = value;
	}
}
