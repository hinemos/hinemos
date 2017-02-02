/*

Copyright (C) 2008 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.bean;


/**
 * ジョブ実行契機のタイプの定数を定義するクラス<BR>
 *
 * @version 4.1.0
 * @since 2.4.0
 */
public class JobTriggerTypeConstant {
	/** 不明 */
	public static final int TYPE_UNKOWN = 0;
	/** スケジュール */
	public static final int TYPE_SCHEDULE = 1;
	/** ファイルチェック */
	public static final int TYPE_FILECHECK = 4;
	/** 手動実行 */
	public static final int TYPE_MANUAL = 2;
	/** 監視連動 */
	public static final int TYPE_MONITOR = 3;
	
	/**
	 * 種別から文字コードに変換する。
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToMessageCode(int type) {
		if (type == TYPE_UNKOWN) {
			return "UNKNOWN";
		} else if (type == TYPE_SCHEDULE) {
			return "SCHEDULE";
		} else if (type == TYPE_FILECHECK) {
			return "FILECHECK";
		} else if (type == TYPE_MANUAL) {
			return "MANUAL";
		} else if (type == TYPE_MONITOR) {
			return "MONITOR";
		}
		return "UNKNOWN";
	}
}
