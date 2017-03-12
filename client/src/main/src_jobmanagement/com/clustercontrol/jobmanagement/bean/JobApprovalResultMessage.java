/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.bean;

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
}
