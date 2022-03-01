/*******************************************************************************
 * Copyright (c) 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.widgets;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.widgets.Widget;


public class MarkupUtil {

  public static boolean isMarkupEnabledFor( Widget widget ) {
    return Boolean.TRUE.equals( widget.getData( RWT.MARKUP_ENABLED ) );
  }

  public static boolean isToolTipMarkupEnabledFor( Widget widget ) {
    return Boolean.TRUE.equals( widget.getData( RWT.TOOLTIP_MARKUP_ENABLED ) );
  }

  private MarkupUtil() {
    // prevent instantiation
  }

}
