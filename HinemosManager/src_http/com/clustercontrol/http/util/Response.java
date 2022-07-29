/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.http.util;

import java.util.List;

import org.apache.hc.core5.http.Header;

/**
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class Response {
	/** 成功フラグ */
	public boolean success;

	/** URL */
	public String url;

	/** ステータスコード */
	public int statusCode;

	/** ヘッダ */
	public String headerString;

	/** ボディ */
	public String responseBody;

	/** 応答時間（ミリ秒） */
	public long responseTime;

	/** エラーメッセージ */
	public String errorMessage;

	/** 例外 */
	public Exception exception;

	/** ヘッダー */
	public List<Header> headers;

	public String toString() {
		return "Response [success=" + this.success
				+ "," + " url=" + this.url
				+ "," + " statusCode=" + this.statusCode
				+ "," + " headerString=" + this.headerString
				+ "," + " responseBody=" + this.responseBody
				+ "," + " responseTime=" + this.responseTime
				+ "," + " errorMessage=" + this.errorMessage
				+ "," + " exception=" + this.exception
				+ "," + " headers=" + this.headers
				+ "]";
	}
}