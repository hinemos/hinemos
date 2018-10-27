/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package org.eclipse.swt.custom;

import org.eclipse.swt.graphics.Color;

/**
 * StyleRange for RAP
 * 
 * @since 5.0.0
 */
public class StyleRange {
	/**
	 * Foreground of the style
	 * 
	 * @see org.eclipse.swt.graphics.TextStyle#foreground
	 */
	public Color foreground;

	/**
	 * the start offset of the range, zero-based from the document start
	 */
	public int start;

	/**
	 * the length of the range
	 */
	public int length;

}
