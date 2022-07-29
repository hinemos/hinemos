/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import com.clustercontrol.util.Messages;


/**
 * ジョブ実行契機のタイプの定数を定義するクラス<BR>
 *
 * @version 4.1.0
 * @since 2.4.0
 */
public class JobTriggerTypeMessage {
	/** 不明（文字列） */
	public static final String STRING_UNKOWN = Messages.getString("unknown");
	/** スケジュール（文字列） */
	public static final String STRING_SCHEDULE = Messages.getString("schedule");
	/** ファイルチェック（文字列） */
	public static final String STRING_FILECHECK = Messages.getString("file.check");
	/** ジョブ連携受信実行契機（文字列） */
	public static final String STRING_JOBLINKRCV = Messages.getString("joblink.rcv.jobkick");
	/** 手動実行（文字列） */
	public static final String STRING_MANUAL = Messages.getString("trigger.manual");
	/** 監視連動（文字列） */
	public static final String STRING_MONITOR = Messages.getString("trigger.monitor");

	/**
	 * 種別から文字列に変換する
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToString(int type) {
		if (type == JobTriggerTypeConstant.TYPE_SCHEDULE) {
			return STRING_SCHEDULE;
		} else if (type == JobTriggerTypeConstant.TYPE_FILECHECK){
			return STRING_FILECHECK;
		} else if (type == JobTriggerTypeConstant.TYPE_JOBLINKRCV){
			return STRING_JOBLINKRCV;
		} else if (type == JobTriggerTypeConstant.TYPE_MANUAL) {
			return STRING_MANUAL;
		} else if (type == JobTriggerTypeConstant.TYPE_MONITOR) {
			return STRING_MONITOR;
		}
		return STRING_UNKOWN;
	}

	/**
	 * 文字列から種別に変換する
	 * 
	 * @param type
	 * @return
	 */
	public static int stringToType(String string) {
		if (string.equals(STRING_SCHEDULE)) {
			return JobTriggerTypeConstant.TYPE_SCHEDULE;
		} else if (string.equals(STRING_FILECHECK)) {
			return JobTriggerTypeConstant.TYPE_FILECHECK;
		} else if (string.equals(STRING_JOBLINKRCV)) {
			return JobTriggerTypeConstant.TYPE_JOBLINKRCV;
		} else if (string.equals(STRING_MANUAL)) {
			return JobTriggerTypeConstant.TYPE_MANUAL;
		} else if (string.equals(STRING_MONITOR)) {
			return JobTriggerTypeConstant.TYPE_MONITOR;
		}
		return -1;
	}

}
