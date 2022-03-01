/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.bean;

import org.openapitools.client.model.GetApprovalJobListRequest;
import com.clustercontrol.approval.util.JobApprovalInfoWrapper;

import com.clustercontrol.bean.JobApprovalResultConstant;
import com.clustercontrol.util.Messages;


/**
 * 承認状態のタイプの定数を定義するクラス<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class JobApprovalResultMessage {
	/** 承認（文字列） */
	public static final String STRING_APPROVAL = Messages.getString("approval");
	/** 否認（文字列） */
	public static final String STRING_DENIAL = Messages.getString("approval.denial");
	
	/**
	 * 種別から文字列に変換する
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToString(int type) {
		if (type == JobApprovalResultConstant.TYPE_APPROVAL) {
			return STRING_APPROVAL;
		} else if (type == JobApprovalResultConstant.TYPE_DENIAL){
			return STRING_DENIAL;
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
		if (string.equals(STRING_APPROVAL)) {
			return JobApprovalResultConstant.TYPE_APPROVAL;
		} else if (string.equals(STRING_DENIAL)) {
			return JobApprovalResultConstant.TYPE_DENIAL;
		}
		return -1;
	}

	/**
	 * 文字列からGetApprovalJobListRequest.ResultEnumに変換する
	 * 
	 * @param type
	 * @return
	 */
	public static GetApprovalJobListRequest.ResultEnum stringToEnum(String string) {
		if (string.equals(STRING_APPROVAL)) {
			return GetApprovalJobListRequest.ResultEnum.APPROVAL;
		} else if (string.equals(STRING_DENIAL)) {
			return GetApprovalJobListRequest.ResultEnum.DENIAL;
		}
		return null;
	}

	/**
	 * Enum（GetApprovalJobListRequest.ResultEnum）から文字列に変換する
	 * @param type
	 * @return
	 */
	public static String typeEnumToString(JobApprovalInfoWrapper.ResultEnum type) {
		if (type == JobApprovalInfoWrapper.ResultEnum.APPROVAL) {
			return STRING_APPROVAL;
		} else if (type == JobApprovalInfoWrapper.ResultEnum.DENIAL){
			return STRING_DENIAL;
		}
		return "";
	}
}
