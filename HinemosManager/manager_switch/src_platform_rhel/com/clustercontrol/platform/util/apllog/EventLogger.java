/*

Copyright (C) 2017 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.platform.util.apllog;

import com.clustercontrol.notify.bean.OutputBasicInfo;

public class EventLogger {
	
	public static void internal(Integer priority, OutputBasicInfo info) {
		// do nothing.
	}

	public static void info(Object o) {
		// do nothing.
	}
	
	public static void info(Object o, Throwable t) {
		// do nothing.
	}
	
	public static void warn(Object o) {
		// do nothing.
	}
	
	public static void warn(Object o, Throwable t) {
		// do nothing.
	}
	
	public static void error(Object o) {
		// do nothing.
	}
	
	public static void error(Object o, Throwable t) {
		// do nothing.
	}
}
