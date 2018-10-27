/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.bean;

public class TopicFlagConstant {
	public static long REPOSITORY_CHANGED = 1 << 0;
	public static long CALENDAR_CHANGED = 1 << 1;
	public static long NEW_FACILITY = 1 << 2;
	public static long LOGFILE_CHANGED = 1 << 3;
	public static long CUSTOM_CHANGED = 1 << 4;
	public static long WINEVENT_CHANGED = 1 << 5;
	public static long FILECHECK_CHANGED = 1 << 6;
	public static long BINARY_CHANGED = 1 << 7;
}
