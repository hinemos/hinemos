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

package com.clustercontrol.util;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.eclipse.rap.rwt.SingletonUtil;

public class TimezoneUtil {

	/**
	 * クライアントが保持するタイムゾーンおよびオフセット
	 */
	private Integer offset = null;
	private TimeZone timeZone = TimeZone.getDefault();

	/**
	 * コンストラクタ
	 */
	private TimezoneUtil() {}

	/**
	 * Session Singleton
	 */
	private static TimezoneUtil getInstance(){
		return SingletonUtil.getSessionInstance( TimezoneUtil.class );
	}

	private void setClientTimeZone(int offset) {
		this.offset = offset;
		this.timeZone.setRawOffset(offset);
	}
	
	private void releaseClientTimeZone() {
		this.offset = null;
		this.timeZone.setRawOffset(0);
	}

	public static void setTimeZoneOffset(int offset){
		getInstance().setClientTimeZone(offset);
	}

	public static void releaseTimeZone(){
		getInstance().releaseClientTimeZone();
	}

	/**
	 * クライアントが保持するタイムゾーンを返す(未設定の場合はオフセット0でのタイムゾーンを返す)
	 */
	public static TimeZone getTimeZone(){
		return getInstance().timeZone;
	}

	/**
	 *  クライアントが保持するタイムゾーンに基づく日付フォーマットを返す
	 */
	public static SimpleDateFormat getSimpleDateFormat(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		sdf.setTimeZone(getTimeZone());
		return sdf;
	}

	/**
	 * クライアントが保持するタイムゾーンのオフセット(ミリ秒)を返す(未設定の場合はnullを返す)
	 */
	public static Integer getTimeZoneOffset(){
		return getInstance().offset;
	}
}
