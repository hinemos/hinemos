/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.bean;

/**
 * イベントに関するHinemosプロパティの定数を格納するクラス<BR>
 * 
 */
public class EventHinemosPropertyConstant {
	/** ユーザ拡張イベント項目の数 */
	public static final int USER_ITEM_SIZE = 40;
	/** イベントカスタムコマンドの数 */
	public static final int COMMAND_SIZE = 10;
	/** ユーザ拡張イベント項目のバリデーション種別 ：正規表現*/
	public static final String USER_ITEM_VALIDATION_TYPE_REGEXP = "regexp";
	/** ユーザ拡張イベント項目のバリデーション種別 ：日付書式*/
	public static final String USER_ITEM_VALIDATION_TYPE_DATEFORMAT = "dateformat";
	
	public static boolean isValidatonType(String value) {
		if (value == null) {
			return false;
		}
		
		switch (value) {
		case USER_ITEM_VALIDATION_TYPE_REGEXP:
		case USER_ITEM_VALIDATION_TYPE_DATEFORMAT:
			return true;
		default:
			return false;
		}
	}
}