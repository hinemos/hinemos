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

import org.eclipse.swt.internal.widgets.ControlUtil;
import org.eclipse.swt.internal.widgets.IColumnAdapter;
import org.eclipse.swt.internal.widgets.WidgetTreeVisitor.AllWidgetTreeVisitor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.Widget;


public class RePackVisitor extends AllWidgetTreeVisitor {

  public boolean doVisit( Widget widget ) {
    if( widget instanceof Control ) {
      Control control = ( Control )widget;
      if( ControlUtil.getControlAdapter( control ).isPacked() ) {
        control.pack();
      }
    } else if( widget instanceof TableColumn ) {
      TableColumn column = ( TableColumn )widget;
      if( getAdapter( column ).isPacked() ) {
        column.pack();
      }
    } else if( widget instanceof TreeColumn ) {
      TreeColumn column = ( TreeColumn )widget;
      if( getAdapter( column ).isPacked() ) {
        column.pack();
      }
    }
    return true;
  }

  private static IColumnAdapter getAdapter( Item column ) {
    Object adapter = column.getAdapter( IColumnAdapter.class );
    return ( IColumnAdapter )adapter;
  }
}
