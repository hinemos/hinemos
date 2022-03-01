/*******************************************************************************
 * Copyright (c) 2009, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.internal.widgets.controldecoratorkit;

import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveListenDefaultSelection;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveListenSelection;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import java.io.IOException;

import org.eclipse.rap.rwt.internal.lifecycle.AbstractWidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.internal.widgets.ControlDecorator;
import org.eclipse.swt.widgets.Widget;


public class ControlDecoratorLCA extends AbstractWidgetLCA {

  private static final String TYPE = "rwt.widgets.ControlDecorator";
  private static final String[] ALLOWED_STYLES = new String[] {
    "TOP", "BOTTOM", "LEFT", "RIGHT", "CENTER"
  };

  private static final String PROP_TEXT = "text";
  private static final String PROP_IMAGE = "image";
  private static final String PROP_VISIBLE = "visible";
  private static final String PROP_SHOW_HOVER = "showHover";

  @Override
  public void preserveValues( Widget widget ) {
    ControlDecorator decorator = ( ControlDecorator )widget;
    WidgetLCAUtil.preserveBounds( decorator, decorator.getBounds() );
    preserveProperty( decorator, PROP_TEXT, decorator.getText() );
    preserveProperty( decorator, PROP_IMAGE, decorator.getImage() );
    preserveProperty( decorator, PROP_VISIBLE, decorator.isVisible() );
    preserveProperty( decorator, PROP_SHOW_HOVER, decorator.getShowHover() );
    preserveListenSelection( decorator );
    preserveListenDefaultSelection( decorator );
  }

  @Override
  public void renderInitialization( Widget widget ) throws IOException {
    ControlDecorator decorator = ( ControlDecorator )widget;
    RemoteObject remoteObject = createRemoteObject( decorator, TYPE );
    remoteObject.setHandler( new ControlDecoratorOperationHandler( decorator ) );
    remoteObject.set( "parent", getId( decorator.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( decorator, ALLOWED_STYLES ) ) );
  }

  @Override
  public void renderChanges( Widget widget ) throws IOException {
    ControlDecorator decorator = ( ControlDecorator )widget;
    WidgetLCAUtil.renderBounds( decorator, decorator.getBounds() );
    renderProperty( decorator, PROP_TEXT, decorator.getText(), "" );
    renderProperty( decorator, PROP_IMAGE, decorator.getImage(), null );
    renderProperty( decorator, PROP_VISIBLE, decorator.isVisible(), false );
    renderProperty( decorator, PROP_SHOW_HOVER, decorator.getShowHover(), true );
    WidgetLCAUtil.renderListenSelection( decorator );
    WidgetLCAUtil.renderListenDefaultSelection( decorator );
  }

  @Override
  public void renderDispose( Widget widget ) throws IOException {
    getRemoteObject( widget ).destroy();
  }

}
