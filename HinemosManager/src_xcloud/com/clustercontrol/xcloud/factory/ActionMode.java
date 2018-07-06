/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import com.clustercontrol.xcloud.Session;

public class ActionMode {
	private ActionMode(){}

	public static void enterAutoDetection() {
		Session.current().setState(ActionMode.class, Boolean.TRUE);
	}

	public static boolean isAutoDetection() {
		return Session.current().isState(ActionMode.class);	
	}

	public static void leaveAutoDetection() {
		Session.current().setState(ActionMode.class, Boolean.FALSE);
	}
}
