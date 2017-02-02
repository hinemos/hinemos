/*

 Copyright (C) 2014 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.infra.bean;

import com.clustercontrol.util.Messages;

/**
 * 環境構築機能のチェックと実行のタイプを定数として格納するクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class RunCheckTypeMessage {
	/** 実行 */
	public static final String STRING_RUN = Messages.getString("infra.module.run");
	/** チェック */
	public static final String STRING_CHECK = Messages.getString("infra.module.check");
	/** プレチェック */
	public static final String STRING_PRECHECK = Messages.getString("infra.module.precheck");
	/**
	 * 種別から文字列に変換します。<BR>
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToString(int type) {
		if (type == RunCheckTypeConstant.TYPE_RUN) {
			return STRING_RUN;
		} else if (type == RunCheckTypeConstant.TYPE_CHECK) {
			return STRING_CHECK;
		} else if (type == RunCheckTypeConstant.TYPE_PRECHECK) {
			return STRING_PRECHECK;
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
		if (string.equals(STRING_RUN)) {
			return RunCheckTypeConstant.TYPE_RUN;
		} else if (string.equals(STRING_CHECK)) {
			return RunCheckTypeConstant.TYPE_CHECK;
		} else if (string.equals(STRING_PRECHECK)) {
			return RunCheckTypeConstant.TYPE_PRECHECK;
		}
		return -1;
	}
	
}
