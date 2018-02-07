/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import com.clustercontrol.bean.JobApprovalStatusConstant;
import com.clustercontrol.util.Messages;


/**
 * 承認状態のタイプの定数を定義するクラス<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class JobApprovalStatusMessage {
	/** 承認待（文字列） */
	public static final String STRING_PENDING = Messages.getString("approval.pending");
	/** 未承認（文字列） */
	public static final String STRING_STILL = Messages.getString("approval.still");
	/** 中断中（文字列） */
	public static final String STRING_SUSPEND = Messages.getString("approval.suspend");
	/** 停止(取り下げ)（文字列） */
	public static final String STRING_STOP = Messages.getString("approval.stop");
	/** 承認済（文字列） */
	public static final String STRING_FINISHED = Messages.getString("approval.finished");

	/**
	 * 種別から文字列に変換する
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToString(int type) {
		if (type == JobApprovalStatusConstant.TYPE_PENDING) {
			return STRING_PENDING;
		} else if (type == JobApprovalStatusConstant.TYPE_STILL){
			return STRING_STILL;
		} else if (type == JobApprovalStatusConstant.TYPE_SUSPEND){
			return STRING_SUSPEND;
		} else if (type == JobApprovalStatusConstant.TYPE_STOP){
			return STRING_STOP;
		} else if (type == JobApprovalStatusConstant.TYPE_FINISHED){
			return STRING_FINISHED;
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
		if (string.equals(STRING_PENDING)) {
			return JobApprovalStatusConstant.TYPE_PENDING;
		} else if (string.equals(STRING_STILL)) {
			return JobApprovalStatusConstant.TYPE_STILL;
		} else if (string.equals(STRING_SUSPEND)) {
			return JobApprovalStatusConstant.TYPE_SUSPEND;
		} else if (string.equals(STRING_STOP)) {
			return JobApprovalStatusConstant.TYPE_STOP;
		} else if (string.equals(STRING_FINISHED)) {
			return JobApprovalStatusConstant.TYPE_FINISHED;
		}
		return -1;
	}
}
