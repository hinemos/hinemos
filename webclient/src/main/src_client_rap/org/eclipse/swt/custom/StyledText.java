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

import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Composite;

/**
 * StyledText for RAP
 * 
 * @since 5.0.0
 */
// TODO Implement with Canvas for colorful appearance
public class StyledText extends Text {

	public StyledText(Composite parent, int style) {
		super(parent, style);
	}
	
	@Override
	public void setForeground(Color color) {
		super.setForeground(color);
	}

	public void addLineStyleListener(LineStyleListener listener) {
		// Do nothing
	}
	
	public void addPaintListener(PaintListener listener) {
	
	}

	public void setLineBackground(int x, int y, Color c) {
		
	}

	public void setHorizontalPixel(int v) {
		
	}

	public int getCaretOffset() {
		return 0;
	}

	public void addLineBackgroundListener(
			LineBackgroundListener lineBackgroundListener) {
		
	}

	public void setHorizontalIndex(int i) {
		
	}
	
	@Override
	public int getTabs() {
		return 4;
	}
}
