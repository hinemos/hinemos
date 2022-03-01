/*******************************************************************************
 * Copyright (c) 2008, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.scalekit;

import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveListenSelection;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderClientListeners;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListenSelection;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import java.io.IOException;

import org.eclipse.rap.rwt.internal.lifecycle.AbstractWidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.ControlLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Widget;


public final class ScaleLCA extends AbstractWidgetLCA {

  private static final String TYPE = "rwt.widgets.Scale";
  private static final String[] ALLOWED_STYLES = new String[] {
    "HORIZONTAL", "VERTICAL", "BORDER"
  };

  // Property names for preserveValues
  static final String PROP_MINIMUM = "minimum";
  static final String PROP_MAXIMUM = "maximum";
  static final String PROP_SELECTION = "selection";
  static final String PROP_INCREMENT = "increment";
  static final String PROP_PAGE_INCREMENT = "pageIncrement";

  // Default values
  private  static final int DEFAULT_MINIMUM = 0;
  private static final int DEFAULT_MAXIMUM = 100;
  private static final int DEFAULT_SELECTION = 0;
  private static final int DEFAULT_INCREMENT = 1;
  private static final int DEFAULT_PAGE_INCREMENT = 10;

  @Override
  public void preserveValues( Widget widget ) {
    Scale scale = ( Scale )widget;
    ControlLCAUtil.preserveValues( scale );
    WidgetLCAUtil.preserveCustomVariant( scale );
    preserveProperty( scale, PROP_MINIMUM, scale.getMinimum() );
    preserveProperty( scale, PROP_MAXIMUM, scale.getMaximum() );
    preserveProperty( scale, PROP_SELECTION, scale.getSelection() );
    preserveProperty( scale, PROP_INCREMENT, scale.getIncrement() );
    preserveProperty( scale, PROP_PAGE_INCREMENT, scale.getPageIncrement() );
    preserveListenSelection( scale );
  }

  @Override
  public void renderInitialization( Widget widget ) throws IOException {
    Scale scale = ( Scale )widget;
    RemoteObject remoteObject = createRemoteObject( scale, TYPE );
    remoteObject.setHandler( new ScaleOperationHandler( scale ) );
    remoteObject.set( "parent", getId( scale.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( scale, ALLOWED_STYLES ) ) );
  }

  @Override
  public void renderChanges( Widget widget ) throws IOException {
    Scale scale = ( Scale )widget;
    ControlLCAUtil.renderChanges( scale );
    WidgetLCAUtil.renderCustomVariant( scale );
    renderProperty( scale, PROP_MINIMUM, scale.getMinimum(), DEFAULT_MINIMUM );
    renderProperty( scale, PROP_MAXIMUM, scale.getMaximum(), DEFAULT_MAXIMUM );
    renderProperty( scale, PROP_SELECTION, scale.getSelection(), DEFAULT_SELECTION );
    renderProperty( scale, PROP_INCREMENT, scale.getIncrement(), DEFAULT_INCREMENT );
    renderProperty( scale, PROP_PAGE_INCREMENT, scale.getPageIncrement(), DEFAULT_PAGE_INCREMENT );
    renderListenSelection( scale );
    renderClientListeners( scale );
  }

}
