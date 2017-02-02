/*
 Copyright (C) 2014 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.http.util;

import java.util.List;

import org.apache.http.Header;

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
}