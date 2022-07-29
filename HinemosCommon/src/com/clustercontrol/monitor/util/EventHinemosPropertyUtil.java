/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.util;

/**
 * イベントに関するHinemosプロパティのユーティリティクラス<BR>
 * 
 */
public class EventHinemosPropertyUtil {
	
	public static final String DEFAULT_NAME_PATTERN = "UserItem%02d";
	
	public static String getDisplayName(String settingDisplayName, int index) {
		if (settingDisplayName != null && !"".equals(settingDisplayName)) {
			return settingDisplayName;
		}
		return String.format(DEFAULT_NAME_PATTERN, index);
	}
}
