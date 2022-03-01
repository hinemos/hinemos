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
package org.eclipse.swt.internal.widgets.linkkit;

import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.hasChanged;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveListenSelection;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListenSelection;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import java.io.IOException;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.internal.lifecycle.AbstractWidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.ControlLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.widgets.ILinkAdapter;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Widget;


public class LinkLCA extends AbstractWidgetLCA {

  private static final String TYPE = "rwt.widgets.Link";
  private static final String[] ALLOWED_STYLES = new String[] { "BORDER" };

  static final String PROP_TEXT = "text";

  @Override
  public void preserveValues( Widget widget ) {
    Link link = ( Link )widget;
    ControlLCAUtil.preserveValues( link );
    WidgetLCAUtil.preserveCustomVariant( link );
    preserveProperty( link, PROP_TEXT, link.getText() );
    preserveListenSelection( link );
  }

  @Override
  public void renderInitialization( Widget widget ) throws IOException {
    Link link = ( Link )widget;
    RemoteObject remoteObject = createRemoteObject( link, TYPE );
    remoteObject.setHandler( new LinkOperationHandler( link ) );
    remoteObject.set( "parent", getId( link.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( link, ALLOWED_STYLES ) ) );
  }

  @Override
  public void renderChanges( Widget widget ) throws IOException {
    Link link = ( Link )widget;
    ControlLCAUtil.renderChanges( link );
    WidgetLCAUtil.renderCustomVariant( link );
    renderText( link );
    renderListenSelection( link );
  }

  ///////////////////////////////////////////////////
  // Helping methods to render the changed properties

  private static void renderText( Link link ) {
    String newValue = link.getText();
    if( hasChanged( link, PROP_TEXT, newValue, "" ) ) {
      getRemoteObject( link ).set( PROP_TEXT, getTextObject( link ) );
    }
  }

  //////////////////
  // Helping methods

  private static JsonArray getTextObject( Link link ) {
    ILinkAdapter adapter = link.getAdapter( ILinkAdapter.class );
    String displayText = adapter.getDisplayText();
    Point[] offsets = adapter.getOffsets();
    JsonArray result = new JsonArray();
    int length = displayText.length();
    int pos = 0;
    for( int i = 0; i < offsets.length; i++ ) {
      int start = offsets[ i ].x;
      int end = offsets[ i ].y + 1;
      // before link
      if( pos < start ) {
        result.add( new JsonArray().add( displayText.substring( pos, start ) )
                                   .add( JsonObject.NULL ) );
      }
      // link itself
      if( start < end ) {
        result.add( new JsonArray().add( displayText.substring( start, end ) ).add( i ) );
      }
      pos = end;
    }
    // after last link
    if( pos < length ) {
      result.add( new JsonArray().add( displayText.substring( pos, length ) )
                                 .add( JsonObject.NULL ) );
    }
    return result;
  }

}
