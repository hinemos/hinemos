/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.utility.settings.ui.util;

import org.eclipse.rap.rwt.SingletonUtil;

public class CommmandCallMode {
	private static CommmandCallMode instance = SingletonUtil.getSessionInstance(CommmandCallMode.class);

	private boolean isCommandLine = false;
	public static synchronized CommmandCallMode getInstance() {
		return instance;
	}

	public static boolean isCommandLine() {
		return getInstance().isCommandLine;
	}

	public static void setCommandLine() {
		getInstance().isCommandLine = true;
	}
}
