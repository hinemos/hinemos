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
package org.eclipse.swt.internal.widgets.toolbarkit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;

import java.io.IOException;

import org.eclipse.rap.rwt.internal.lifecycle.AbstractWidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.ControlLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Widget;


public class ToolBarLCA extends AbstractWidgetLCA {

  private static final String TYPE = "rwt.widgets.ToolBar";
  private static final String[] ALLOWED_STYLES = new String[] {
    "FLAT", "HORIZONTAL", "VERTICAL", "NO_RADIO_GROUP", "BORDER", "RIGHT"
  };

  @Override
  public void preserveValues( Widget widget ) {
    ToolBar toolBar = ( ToolBar )widget;
    ControlLCAUtil.preserveValues( toolBar );
    WidgetLCAUtil.preserveCustomVariant( toolBar );
  }

  @Override
  public void renderInitialization( Widget widget ) throws IOException {
    ToolBar toolBar = ( ToolBar )widget;
    RemoteObject remoteObject = createRemoteObject( toolBar, TYPE );
    remoteObject.setHandler( new ToolBarOperationHandler( toolBar ) );
    remoteObject.set( "parent", getId( toolBar.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( toolBar, ALLOWED_STYLES ) ) );
  }

  @Override
  public void renderChanges( Widget widget ) throws IOException {
    ToolBar toolBar = ( ToolBar )widget;
    ControlLCAUtil.renderChanges( toolBar );
    WidgetLCAUtil.renderCustomVariant( toolBar );
  }

}
