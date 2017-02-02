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
	/** スケジュール（文字列） */
	public static final String STRING_FILECHECK = Messages.getString("file.check");
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
		} else if (string.equals(STRING_MANUAL)) {
			return JobTriggerTypeConstant.TYPE_MANUAL;
		} else if (string.equals(STRING_MONITOR)) {
			return JobTriggerTypeConstant.TYPE_MONITOR;
		}
		return -1;
	}
}
