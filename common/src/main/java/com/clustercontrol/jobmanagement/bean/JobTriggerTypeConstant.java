/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
