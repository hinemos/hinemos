/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.graphics;

import org.eclipse.swt.SWT;


public final class GCData {
	public Device device;
	public int style, state = -1;
	public Color foreground;
	public Color background;
	public Pattern foregroundPattern;
	public Pattern backgroundPattern;
	public Font font;
	public int alpha = 0xFF;
	public float lineWidth;
	public int lineStyle = org.eclipse.draw2d.rap.swt.SWT.LINE_SOLID;
	public int lineCap = SWT.CAP_FLAT;
	public int lineJoin = SWT.JOIN_MITER;
	public float lineDashesOffset;
	public float[] lineDashes;
	public float lineMiterLimit = 10;
	public boolean xorMode;
	public int antialias = SWT.DEFAULT;
	public int textAntialias = SWT.DEFAULT;
//	public int fillRule = SWT.FILL_EVEN_ODD;
//	public Matrix matrix;
//	public DisplayObject clip;
	
//	public intrinsic.flash.geom.Rectangle paintRect;
	public Image image;
//	public Sprite sprite;
}
