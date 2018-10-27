/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package org.eclipse.swt.custom;

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * Dummy StyledTextEvent for RAP
 */ 
class StyledTextEvent extends Event {
	// used by LineStyleEvent
	int[] ranges;
	StyleRange[] styles;
	int alignment;
	int indent;
	int wrapIndent;
	boolean justify;
	int bulletIndex;
	int[] tabStops;
	// used by LineBackgroundEvent
	Color lineBackground;
	// used by TextChangedEvent
	int replaceCharCount; 	
	int newCharCount; 
	int replaceLineCount;
	int newLineCount;
	// used by PaintObjectEvent
	int x;
	int y;
	int ascent;
	int descent;
	StyleRange style;

	StyledTextEvent (Object content) {
		super();
		data = content;	
	}
}
