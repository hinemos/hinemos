/**********************************************************************
 * Copyright (C) 2006 NTT DATA Corporation
 * This program is free software; you can redistribute it and/or
 * Modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2.
 * 
 * This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *********************************************************************/

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
