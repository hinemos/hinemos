/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.bean;

import com.clustercontrol.util.Messages;

/**
 * Hinemosの機能を定数として格納するクラス<BR>
 *
 * @version 4.1.0
 * @since 2.1.2
 */
public class HinemosModuleMessage {

	/**
	 * 名前から文字列に変換します。<BR>
	 *
	 * @param string
	 * @return
	 */
	public static String nameToString(String string) {
		return Messages.getString(HinemosModuleConstant.nameToMessageCode(string));
	}
}
