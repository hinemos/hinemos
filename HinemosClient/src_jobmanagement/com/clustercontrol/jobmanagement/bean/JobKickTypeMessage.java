/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import org.openapitools.client.model.JobKickResponse;

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
	/** ジョブ連携受信実行契機（文字列） */
	public static final String STRING_JOBLINKRCV = Messages.getString("joblink.rcv.jobkick");
	/** マニュアル実行契機（文字列） */
	public static final String STRING_MANUAL = Messages.getString("job.manual");

	/**
	 * 種別(EnumValue)から文字列に変換する
	 * 
	 * @param type
	 * @return
	 */
	public static String typeEnumValueToString(String type) {
		if (type.equals(JobKickResponse.TypeEnum.SCHEDULE.getValue())) {
			return STRING_SCHEDULE;
		} else if (type.equals(JobKickResponse.TypeEnum.FILECHECK.getValue())) {
			return STRING_FILECHECK;
		} else if (type.equals(JobKickResponse.TypeEnum.JOBLINKRCV.getValue())) {
			return STRING_JOBLINKRCV;
		} else if (type.equals(JobKickResponse.TypeEnum.MANUAL.getValue())) {
			return STRING_MANUAL;
		}
		return "";
	}

	/**
	 * 文字列から種別(EnumValue)に変換する
	 * 
	 * @param type
	 * @return
	 */
	public static String stringToTypeEnumValue(String string) {
		if (string.equals(STRING_SCHEDULE)) {
			return JobKickResponse.TypeEnum.SCHEDULE.getValue();
		} else if (string.equals(STRING_FILECHECK)) {
			return JobKickResponse.TypeEnum.FILECHECK.getValue();
		} else if (string.equals(STRING_JOBLINKRCV)) {
			return JobKickResponse.TypeEnum.JOBLINKRCV.getValue();
		} else if (string.equals(STRING_MANUAL)) {
			return JobKickResponse.TypeEnum.MANUAL.getValue();
		}
		return "";
	}

}
