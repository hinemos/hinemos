/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

 */

package com.clustercontrol.poller.util;

import java.io.Serializable;
import java.util.Date;

public final class TableEntry implements Serializable {
	private static final long serialVersionUID = -5926453308072828591L;

	private final String key;
	private final long date;
	private final Serializable value;
	private final Exception exception;
	private final ErrorType errorType;

	public static enum ErrorType {
		/**
		 * 正常にデータが取得できている
		 */
		NO_ERROR,
		/**
		 * 当該キーのデータがレスポンス内に存在しない
		 */
		RESPONSE_NOT_FOUND,
		/**
		 * IOエラーによりレスポンス自体を得られていない
		 */
		IO_ERROR,
		/**
		 * Hinemosが認識・実装していない収集プロトコルが指定されている
		 */
		ILLEGAL_PROTOCOL,
	}
	
	public TableEntry(String key, long date, Serializable value){
		this.key = key;
		this.date = date;
		this.value = value;
		this.errorType = ErrorType.NO_ERROR;
		this.exception = null;
	}
	
	public TableEntry(String key, long date, ErrorType errorType, Exception errorDetail) {
		this.key = key;
		this.date = date;
		this.value = null;
		this.errorType = errorType;
		this.exception = errorDetail;
	}
	
	/**
	 * 有効な値が格納されているか
	 * @return getValid()で有効な値が取れる場合はtrue、getExceptionで例外を取れる場合にはfalse
	 */
	public boolean isValid() {
		return errorType == ErrorType.NO_ERROR;
	}
	
	/**
	 * 時刻を取得
	 * @return 時刻
	 */
	public long getDate() {
		return date;
	}
	
	/**
	 * 値を取得
	 * @return 値のオブジェクト
	 */
	public Object getValue() {
		return value;
	}
	
	/**
	 * キーを取得
	 * @return キー
	 */
	public String getKey() {
		return key;
	}
	
	public ErrorType getErrorType() {
		return errorType;
	}
	
	/**
	 * 例外を取得
	 * @return 例外
	 */
	public Exception getErrorDetail() {
		return exception;
	}
	
	/**
	 * 設定されている時刻と値の文字列表現を返します。
	 */
	@Override
	public String toString(){
		if (errorType == ErrorType.NO_ERROR) {
			return new Date(date) + " : " + key + " = " + value.toString();
		} else {
			return new Date(date) + " : " + key + " = N/A, error = " + exception;
		}
	}
}
