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
 * ジョブパラメータの定数クラス<BR>
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class JobParamTypeMessage {
	/** ユーザパラメータ */
	public static final String STRING_USER = Messages.getString("user");
	/** ランタイムパラメータ */
	public static final String STRING_RUNTIME = Messages.getString("runtime");
	/** システムパラメータ（ジョブ） */
	public static final String STRING_SYSTEM_JOB = Messages.getString("job.parameter.system.job");

	/**
	 * 種別から文字列に変換します。<BR>
	 *
	 * @param type
	 * @return
	 */
	public static String typeToString(int type) {
		if (type == JobParamTypeConstant.TYPE_USER) {
			return STRING_USER;
		} else if (type == JobParamTypeConstant.TYPE_RUNTIME) {
			return STRING_RUNTIME;
		} else if (type == JobParamTypeConstant.TYPE_SYSTEM_JOB) {
			return STRING_SYSTEM_JOB;
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
		if (string.equals(STRING_USER)) {
			return JobParamTypeConstant.TYPE_USER;
		} else if (string.equals(STRING_RUNTIME)) {
			return JobParamTypeConstant.TYPE_RUNTIME;
		} else if (string.equals(STRING_SYSTEM_JOB)) {
			return JobParamTypeConstant.TYPE_SYSTEM_JOB;
		}
		return -1;
	}
}