/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import org.eclipse.swt.widgets.Widget;

/**
 * Widget Test Utility (Dummy class)
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class WidgetTestUtil{
	// prevent instantiation
	private WidgetTestUtil() {
	}

	/**
	 * Set TestId attribute by JavaScript
	 * 
	 * @param widget
	 * @param value
	 * @see http://eclipse.org/rap/developers-guide/devguide.php?topic=scripting.html&version=2.3
	 */
	public static void setTestId( Object parent, String value, Widget widget ) {
		// Do nothing
	}

}
