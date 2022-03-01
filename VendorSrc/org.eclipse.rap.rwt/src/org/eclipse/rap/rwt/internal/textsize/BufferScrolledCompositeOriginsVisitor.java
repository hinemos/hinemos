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

class BufferScrolledCompositeOriginsVisitor extends AllWidgetTreeVisitor {

  public boolean doVisit( Widget widget ) {
    if( widget instanceof ScrolledComposite ) {
      bufferOrigin( ( ScrolledComposite )widget );
      bufferContentSize( ( ScrolledComposite )widget );
    }
    return true;
  }

  private void bufferOrigin( ScrolledComposite composite ) {
    Point origin = composite.getOrigin();
    composite.setData( TextSizeRecalculation.KEY_SCROLLED_COMPOSITE_ORIGIN, origin );
  }

  private void bufferContentSize( ScrolledComposite composite ) {
    Control content = composite.getContent();
    if( content != null ) {
      Point size = content.getSize();
      content.setData( TextSizeRecalculation.KEY_SCROLLED_COMPOSITE_CONTENT_SIZE, size );
    }
  }
}
