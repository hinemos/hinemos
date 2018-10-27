/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.bean;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.util.Messages;

public class SendMethodMessage {
	/** SCP */
	public static final String STRING_SCP = Messages.getString("infra.module.file.method.scp");
	/** WinRM */
	public static final String STRING_WINRM = Messages.getString("infra.module.file.method.winrm");
	/**
	 * 種別から文字列に変換します。<BR>
	 * 
	 * @param type
	 * @return
	 */
	public static String typeToString(int type) {
		if (type == SendMethodConstant.TYPE_SCP) {
			return STRING_SCP;
		} else if (type == SendMethodConstant.TYPE_WINRM) {
			return STRING_WINRM;
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
		if (string.equals(STRING_SCP)) {
			return SendMethodConstant.TYPE_SCP;
		} else if (string.equals(STRING_WINRM)) {
			return SendMethodConstant.TYPE_WINRM;
		}
		return -1;
	}
	
	public static List<String> getAllStrings(){
		List<String> strings = new ArrayList<String>();
		
		strings.add(STRING_SCP);
		strings.add(STRING_WINRM);
		
		return strings;
	}
}
