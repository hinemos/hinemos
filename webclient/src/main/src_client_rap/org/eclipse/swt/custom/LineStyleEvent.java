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

import org.eclipse.swt.events.TypedEvent;

/**
 * LineStyleEvent for RAP
 * 
 * @since 5.0.0
 */
public class LineStyleEvent extends TypedEvent {

	private static final long serialVersionUID = 1L;

	/**
	 * line start offset (input)
	 */
	public int lineOffset;

	/**
	 * line text (input)
	 */
	public String lineText;

	/**
	 * line styles (output)
	 * 
	 * Note: Because a StyleRange includes the start and length, the
	 * same instance cannot occur multiple times in the array of styles.
	 * If the same style attributes, such as font and color, occur in
	 * multiple StyleRanges, <code>ranges</code> can be used to share
	 * styles and reduce memory usage.
	 */
	public StyleRange[] styles;

	public LineStyleEvent(StyledTextEvent e) {
		super(e);
		// Do nothing
	}
}
