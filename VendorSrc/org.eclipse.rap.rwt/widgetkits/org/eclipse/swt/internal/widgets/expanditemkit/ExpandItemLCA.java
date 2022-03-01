/*******************************************************************************
 * Copyright (c) 2008, 2014 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.expanditemkit;

import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;

import java.io.IOException;

import org.eclipse.rap.rwt.internal.lifecycle.AbstractWidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.widgets.IExpandBarAdapter;
import org.eclipse.swt.internal.widgets.ItemLCAUtil;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Widget;


public final class ExpandItemLCA extends AbstractWidgetLCA {

  private static final String TYPE = "rwt.widgets.ExpandItem";

  public static final String PROP_EXPANDED = "expanded";
  public static final String PROP_HEADER_HEIGHT = "headerHeight";

  public static final int DEFAULT_HEADER_HEIGHT = 24;

  @Override
  public void preserveValues( Widget widget ) {
    ExpandItem item = ( ExpandItem )widget;
    WidgetLCAUtil.preserveCustomVariant( item );
    WidgetLCAUtil.preserveBounds( item, getBounds( item ) );
    ItemLCAUtil.preserve( item );
    preserveProperty( item, PROP_EXPANDED, item.getExpanded() );
    preserveProperty( item, PROP_HEADER_HEIGHT, item.getHeaderHeight() );
  }

  @Override
  public void renderInitialization( Widget widget ) throws IOException {
    ExpandItem item = ( ExpandItem )widget;
    RemoteObject remoteObject = createRemoteObject( item, TYPE );
    remoteObject.setHandler( new ExpandItemOperationHandler( item ) );
    remoteObject.set( "parent", getId( item.getParent() ) );
  }

  @Override
  public void renderChanges( Widget widget ) throws IOException {
    ExpandItem item = ( ExpandItem )widget;
    WidgetLCAUtil.renderCustomVariant( widget );
    WidgetLCAUtil.renderBounds( item, getBounds( item ) );
    ItemLCAUtil.renderChanges( item );
    renderProperty( item, PROP_EXPANDED, item.getExpanded(), false );
    renderProperty( item, PROP_HEADER_HEIGHT, item.getHeaderHeight(), DEFAULT_HEADER_HEIGHT );
  }

  //////////////////
  // Helping methods

  private static Rectangle getBounds( ExpandItem item ) {
    return getExpandBarAdapter( item ).getBounds( item );
  }

  private static IExpandBarAdapter getExpandBarAdapter( ExpandItem item ) {
    return item.getParent().getAdapter( IExpandBarAdapter.class );
  }

}
