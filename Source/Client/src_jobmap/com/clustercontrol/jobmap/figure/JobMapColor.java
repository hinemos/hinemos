/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.figure;

import org.eclipse.swt.graphics.Color;

public class JobMapColor {
	private static final int tmpColor = 120;
	public static final Color red = new Color (null, 255, tmpColor, tmpColor);
	public static final Color green = new Color (null, tmpColor, 255, tmpColor);
	public static final Color blue = new Color (null, tmpColor, tmpColor, 255);
	public static final Color yellow = new Color (null, 255, 255, tmpColor);
	public static final Color lightgray = new Color ( null, 200, 200, 200);
	public static final Color darkgray = new Color ( null, 110, 110, 110);
	public static final Color black = new Color ( null, 0, 0, 0);
}
