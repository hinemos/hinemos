/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.bean;

/**
 * イベントカスタムコマンドのステータスの定義を定数として格納するクラス<BR>
 * 
 */
public class EventCustomCommandStatusConstant {
	
	/** 正常 */
	public static final int STATUS_NORMAL  = 1;

	/** 警告 */
	public static final int STATUS_WARNING = 2;
	
	/** エラー */
	public static final int STATUS_ERROR = 3;
	
	/** キャンセル*/
	public static final int STATUS_CANCEL = 9;
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	public static String statusToMessageCode(int type) {
		switch (type) {
		case STATUS_NORMAL:
			return "COMMON_STATUS_NORMAL";
		case STATUS_WARNING:
			return "COMMON_STATUS_WARNING";
		case STATUS_ERROR:
			return "COMMON_STATUS_ERROR";
		case STATUS_CANCEL:
			return "COMMON_STATUS_CANCEL";
		default:
			return "";
		}
	}
}