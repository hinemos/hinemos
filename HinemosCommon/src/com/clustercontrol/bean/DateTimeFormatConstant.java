/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.bean;

/**
 * 日時フォーマットの定数クラス<BR>
 * 
 * 各コンポーネントで統一したフォーマットを使用したい場合に利用する。<BR>
 */
public class DateTimeFormatConstant {

	/** 時のみ */
	public static final String HR = "HH";
	/** 分のみ */
	public static final String MIN = "mm";
	/** 秒のみ */
	public static final String SEC = "ss";
	/** 時と分 */
	public static final String HR_MIN = "HH:mm";
	/** 分と秒 */
	public static final String MIN_SEC = "mm:ss";

	/** 標準的な時間のフォーマット */
	public static final String COMMON_TIME = "HH:mm:ss";

}
