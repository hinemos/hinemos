/*******************************************************************************
 * Copyright (c) 2002, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.sashkit;

import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveListenSelection;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListenSelection;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import java.io.IOException;

import org.eclipse.rap.rwt.internal.lifecycle.AbstractWidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.ControlLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Widget;


public final class SashLCA extends AbstractWidgetLCA {

  private static final String TYPE = "rwt.widgets.Sash";
  private static final String[] ALLOWED_STYLES = new String[] {
    "HORIZONTAL", "VERTICAL", "SMOOTH", "BORDER"
  };

  @Override
  public void preserveValues( Widget widget ) {
    Sash sash = ( Sash )widget;
    ControlLCAUtil.preserveValues( sash );
    WidgetLCAUtil.preserveCustomVariant( sash );
    preserveListenSelection( sash );
  }

  @Override
  public void renderInitialization( Widget widget ) throws IOException {
    Sash sash = ( Sash )widget;
    RemoteObject remoteObject = createRemoteObject( sash, TYPE );
    remoteObject.setHandler( new SashOperationHandler( sash ) );
    remoteObject.set( "parent", getId( sash.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( sash, ALLOWED_STYLES ) ) );
  }

  @Override
  public void renderChanges( Widget widget ) throws IOException {
    Sash sash = ( Sash )widget;
    ControlLCAUtil.renderChanges( sash );
    WidgetLCAUtil.renderCustomVariant( sash );
    renderListenSelection( sash );
  }

}
