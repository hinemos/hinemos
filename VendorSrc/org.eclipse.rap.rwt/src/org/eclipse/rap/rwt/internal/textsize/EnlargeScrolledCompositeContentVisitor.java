/*******************************************************************************
 * Copyright (c) 2011, 2012 Frank Appel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Frank Appel - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.textsize;

import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.widgets.WidgetTreeVisitor.AllWidgetTreeVisitor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

class EnlargeScrolledCompositeContentVisitor extends AllWidgetTreeVisitor {

  public boolean doVisit( Widget widget ) {
    if( widget instanceof ScrolledComposite && hasContentControl( widget ) ) {
      enlargeContentControl( widget );
    }
    return true;
  }

  private void enlargeContentControl( Widget widget ) {
    enlargeContentControl( getContentControl( widget ) );
  }

  private void enlargeContentControl( Control content ) {
    Point currentSize = content.getSize();
    int width = currentSize.x + TextSizeRecalculation.RESIZE_OFFSET;
    int height = currentSize.y + TextSizeRecalculation.RESIZE_OFFSET;
    content.setSize( width, height );
  }

  private boolean hasContentControl( Widget widget ) {
    return getContentControl( widget ) != null;
  }

  private Control getContentControl( Widget widget ) {
    return ( ( ScrolledComposite )widget ).getContent();
  }
}
