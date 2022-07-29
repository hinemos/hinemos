/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
		return getSimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	}

	/**
	 *  クライアントが保持するタイムゾーンに基づく日付フォーマットを返す
	 */
	public static SimpleDateFormat getSimpleDateFormat(String pattern){
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
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
