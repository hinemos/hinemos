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
package org.eclipse.draw2d.rap.swt.graphics;

import java.util.Arrays;

import org.eclipse.swt.*;

/**
 * <code>LineAttributes</code> defines a set of line attributes that
 * can be modified in a GC.
 * <p>
 * Application code does <em>not</em> need to explicitly release the
 * resources managed by each instance when those instances are no longer
 * required, and thus no <code>dispose()</code> method is provided.
 * </p>
 *
 * @see GC#getLineAttributes()
 * @see GC#setLineAttributes(LineAttributes)
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 1.3
 */
public class LineAttributes extends org.eclipse.swt.graphics.LineAttributes {

	private static int hashCode(float[] array) {
		int prime = 31;
		if (array == null)
			return 0;
		int result = 1;
		for (int index = 0; index < array.length; index++) {
			result = prime * result + Float.floatToIntBits(array[index]);
		}
		return result;
	}

	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + LineAttributes.hashCode(dash);
		result = prime * result + Float.floatToIntBits(dashOffset);
		result = prime * result + Float.floatToIntBits(miterLimit);
		result = prime * result + style;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		LineAttributes other = (LineAttributes) obj;
		if (!Arrays.equals(dash, other.dash))
			return false;
		if (Float.floatToIntBits(dashOffset) != Float
				.floatToIntBits(other.dashOffset))
			return false;
		if (Float.floatToIntBits(miterLimit) != Float
				.floatToIntBits(other.miterLimit))
			return false;
		if (style != other.style)
			return false;
		return true;
	}

	/**
   * The line style.
   * 
   * @see org.eclipse.swt.SWT#LINE_CUSTOM
   * @see org.eclipse.swt.SWT#LINE_DASH
   * @see org.eclipse.swt.SWT#LINE_DASHDOT
   * @see org.eclipse.swt.SWT#LINE_DASHDOTDOT
   * @see org.eclipse.swt.SWT#LINE_DOT
   * @see org.eclipse.swt.SWT#LINE_SOLID
   */
  public int style;

	/**
   * The line dash style for SWT.LINE_CUSTOM.
   */
  public float[] dash;
  
  /**
   * The line dash style offset for SWT.LINE_CUSTOM.
   */
  public float dashOffset;

  /**
   * The line miter limit.
   */
  public float miterLimit;

  /**
   * Create a new line attributes with the specified line width.
   *
   * @param width the line width
   */
  public LineAttributes( final float width ) {
  	super( width );
  }

  /**
   * Create a new line attributes with the specified line cap, join and width.
   *
   * @param width the line width
   * @param cap the line cap style
   * @param join the line join style
   */
  public LineAttributes( final float width, final int cap, final int join ) {
    super(width,cap,join);
  }
  
  /**
   * Create a new line attributes with the specified arguments.
   * @param width
   * @param cap
   * @param join
   * @param style
   * @param dash
   * @param dashOffset
   * @param miterLimit
   */
  public LineAttributes(float width, int cap, int join, int style, float[] dash, float dashOffset, float miterLimit) {
    this(width,cap,join);
    this.style = style;
    this.dash = dash;
    this.dashOffset = dashOffset;
    this.miterLimit = miterLimit;
  }
  
}
