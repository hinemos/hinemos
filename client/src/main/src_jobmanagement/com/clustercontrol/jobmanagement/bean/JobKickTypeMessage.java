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
 * ジョブ実行契機種別の定数を定義するクラス<BR>
 *
 * @version 5.1.0
 */
public class JobKickTypeMessage {
	/** スケジュール（文字列） */
	public static final String STRING_SCHEDULE = Messages.getString("schedule");
	/** ファイルチェック（文字列） */
	public static final String STRING_FILECHECK = Messages.getString("file.check");
	/** マニュアル実行契機（文字列） */
	public static final String STRING_MANUAL = Messages.getString("job.manual");

	/**
	 * 種別から文字列に変換する
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToString(int type) {
		if (type == JobKickConstant.TYPE_SCHEDULE) {
			return STRING_SCHEDULE;
		} else if (type == JobKickConstant.TYPE_FILECHECK){
			return STRING_FILECHECK;
		} else if (type == JobKickConstant.TYPE_MANUAL) {
			return STRING_MANUAL;
		}
		return "";
	}

	/**
	 * 文字列から種別に変換する
	 * 
	 * @param type
	 * @return
	 */
	public static int stringToType(String string) {
		if (string.equals(STRING_SCHEDULE)) {
			return JobKickConstant.TYPE_SCHEDULE;
		} else if (string.equals(STRING_FILECHECK)) {
			return JobKickConstant.TYPE_FILECHECK;
		} else if (string.equals(STRING_MANUAL)) {
			return JobKickConstant.TYPE_MANUAL;
		}
		return -1;
	}
}
