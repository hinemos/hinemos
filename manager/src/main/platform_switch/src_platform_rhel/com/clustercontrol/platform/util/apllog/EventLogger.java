/*
* Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
*
* Hinemos (http://www.hinemos.info/)
*
* See the LICENSE file for licensing information.
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
