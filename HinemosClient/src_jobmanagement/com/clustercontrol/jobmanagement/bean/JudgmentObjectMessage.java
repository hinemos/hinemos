/*

Copyright (C) 2006 NTT DATA Corporation

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
 * ジョブ終了状態の定数クラス<BR>
 * 
 * @version 2.1.0
 * @since 1.0.0
 */
public class JudgmentObjectMessage {
	/** ジョブ終了状態 */
	public static final String STRING_JOB_END_STATUS = Messages.getString("job") + "(" + Messages.getString("end.status") + ")";

	/** ジョブ終了値 */
	public static final String STRING_JOB_END_VALUE = Messages.getString("job") + "(" + Messages.getString("end.value") + ")";

	/** 時刻 */
	public static final String STRING_TIME = Messages.getString("wait.rule.time");

	/** セッション開始時の時間（分） */
	public static final String STRING_START_MINUTE = Messages.getString("time.after.session.start");

	/** ジョブ変数 */
	public static final String STRING_JOB_PARAMETER = Messages.getString("job.parameter");

	/**
	 * 種別から文字列に変換します。<BR>
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToString(int type) {
		if (type == JudgmentObjectConstant.TYPE_JOB_END_STATUS) {
			return STRING_JOB_END_STATUS;
		}
		else if (type == JudgmentObjectConstant.TYPE_JOB_END_VALUE) {
			return STRING_JOB_END_VALUE;
		}
		else if (type == JudgmentObjectConstant.TYPE_TIME) {
			return STRING_TIME;
		}
		else if (type == JudgmentObjectConstant.TYPE_START_MINUTE) {
			return STRING_START_MINUTE;
		}
		else if (type == JudgmentObjectConstant.TYPE_JOB_PARAMETER) {
			return STRING_JOB_PARAMETER;
		}
		return "";
	}

	/**
	 * 文字列から種別に変換します。<BR>
	 * 
	 * @param type
	 * @return
	 */
	public static int stringToType(String string) {
		if (string.equals(STRING_JOB_END_STATUS)) {
			return JudgmentObjectConstant.TYPE_JOB_END_STATUS;
		}
		else if (string.equals(STRING_JOB_END_VALUE)) {
			return JudgmentObjectConstant.TYPE_JOB_END_VALUE;
		}
		else if (string.equals(STRING_TIME)) {
			return JudgmentObjectConstant.TYPE_TIME;
		}
		else if (string.equals(STRING_START_MINUTE)) {
			return JudgmentObjectConstant.TYPE_START_MINUTE;
		}
		else if (string.equals(STRING_JOB_PARAMETER)) {
			return JudgmentObjectConstant.TYPE_JOB_PARAMETER;
		}
		return -1;
	}
}