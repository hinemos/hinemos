/*******************************************************************************
 * Copyright (c) 2002, 2014 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.coolitemkit;

import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;

import java.io.IOException;

import org.eclipse.rap.rwt.internal.lifecycle.AbstractWidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.internal.widgets.Props;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Widget;


public class CoolItemLCA extends AbstractWidgetLCA {

  private static final String TYPE = "rwt.widgets.CoolItem";
  private static final String[] ALLOWED_STYLES = new String[] { "DROP_DOWN", "VERTICAL" };

  static final String PROP_CONTROL = "control";

  /* (intentionally not JavaDoc'ed)
   * Unnecesary to call ItemLCAUtil.preserve, CoolItem does neither use text
   * nor image
   */
  @Override
  public void preserveValues( Widget widget ) {
    CoolItem item = ( CoolItem )widget;
    preserveProperty( item, PROP_CONTROL, item.getControl() );
    preserveProperty( item, Props.BOUNDS, item.getBounds() );
    WidgetLCAUtil.preserveCustomVariant( item );
    WidgetLCAUtil.preserveData( item );
  }

  @Override
  public void renderInitialization( Widget widget ) throws IOException {
    CoolItem item = ( CoolItem )widget;
    RemoteObject remoteObject = createRemoteObject( item, TYPE );
    remoteObject.setHandler( new CoolItemOperationHandler( item ) );
    remoteObject.set( "parent", getId( item.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( item, ALLOWED_STYLES ) ) );
  }

  @Override
  public void renderChanges( Widget widget ) throws IOException {
    CoolItem item = ( CoolItem )widget;
    WidgetLCAUtil.renderBounds( item, item.getBounds() );
    renderProperty( item, PROP_CONTROL, item.getControl(), null );
    WidgetLCAUtil.renderCustomVariant( item );
    WidgetLCAUtil.renderData( item );
  }

}
