/*******************************************************************************
 * Copyright (c) 2011 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.graphics;

import org.eclipse.swt.SWT;


public class ColorUtil {
  
  
  public static int computeColorNr( int red, int green, int blue ) {
    if( red > 255 || red < 0 || green > 255 || green < 0 || blue > 255 || blue < 0 ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    int result = red | green << 8 | blue << 16;
    return result;
  }

  private ColorUtil() {
    // prevent instantiation
  }
}
