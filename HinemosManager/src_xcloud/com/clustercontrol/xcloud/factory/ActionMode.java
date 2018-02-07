/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

public class ActionMode {
	public static enum ActionKind {
		Normal,
		AutoDetection;
	}

	private static ThreadLocal<ActionKind> current = new ThreadLocal<ActionKind>() {
		public ActionKind initialValue() {
			return ActionKind.Normal;
		}
	};

	private ActionMode(){}

	public static void enterAutoDetection() {
		current.set(ActionKind.AutoDetection);
	}

	public static boolean isAutoDetection() {
		return current.get().equals(ActionKind.AutoDetection);	
	}

	public static void leaveAutoDetection() {
		current.set(ActionKind.Normal);
	}
}
