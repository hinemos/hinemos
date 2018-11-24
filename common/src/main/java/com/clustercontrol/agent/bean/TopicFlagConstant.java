/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.bean;

public class TopicFlagConstant {
	public static final long REPOSITORY_CHANGED = 1 << 0;
	public static final long CALENDAR_CHANGED = 1 << 1;
	public static final long NEW_FACILITY = 1 << 2;
	public static final long LOGFILE_CHANGED = 1 << 3;
	public static final long CUSTOM_CHANGED = 1 << 4;
	public static final long WINEVENT_CHANGED = 1 << 5;
	public static final long FILECHECK_CHANGED = 1 << 6;
	public static final long BINARY_CHANGED = 1 << 7;
}
