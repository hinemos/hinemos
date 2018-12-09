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
 * ランタイムジョブ変数の定数クラス<BR>
 *
 * @version 5.1.0
 */
public class JobRuntimeParamTypeMessage {

	/** 入力 */
	public static final String STRING_INPUT = Messages.getString("job.manual.type.input");
	/** 選択（ラジオボタン） */
	public static final String STRING_RADIO = Messages.getString("job.manual.type.radio");
	/** 選択（コンボボックス） */
	public static final String STRING_COMBO = Messages.getString("job.manual.type.combo");
	/** 固定 */
	public static final String STRING_FIXED = Messages.getString("job.manual.type.fixed");

	/**
	 * 種別から文字列に変換します。<BR>
	 *
	 * @param type
	 * @return
	 */
	public static String typeToString(int type) {
		if (type == JobRuntimeParamTypeConstant.TYPE_INPUT) {
			return STRING_INPUT;
		} else if (type == JobRuntimeParamTypeConstant.TYPE_RADIO) {
			return STRING_RADIO;
		} else if (type == JobRuntimeParamTypeConstant.TYPE_COMBO) {
			return STRING_COMBO;
		} else if (type == JobRuntimeParamTypeConstant.TYPE_FIXED) {
			return STRING_FIXED;
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
		if (string.equals(STRING_INPUT)) {
			return JobRuntimeParamTypeConstant.TYPE_INPUT;
		} else if (string.equals(STRING_RADIO)) {
			return JobRuntimeParamTypeConstant.TYPE_RADIO;
		} else if (string.equals(STRING_COMBO)) {
			return JobRuntimeParamTypeConstant.TYPE_COMBO;
		} else if (string.equals(STRING_FIXED)) {
			return JobRuntimeParamTypeConstant.TYPE_FIXED;
		}
		return -1;
	}
}