/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

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

	public void addLineBackgroundListener(Object lineBackgroundListener) {
		// Do nothing
	}

	public void setHorizontalIndex(int i) {
		// Do nothing
	}

	public int getTabs() {
		return 4;
	}
}
