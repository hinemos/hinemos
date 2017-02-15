/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
}
