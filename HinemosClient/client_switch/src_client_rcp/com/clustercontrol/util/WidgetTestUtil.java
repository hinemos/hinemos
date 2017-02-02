/**********************************************************************
 * Copyright (C) 2014 NTT DATA Corporation
 * This program is free software; you can redistribute it and/or
 * Modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2.
 * 
 * This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *********************************************************************/

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
