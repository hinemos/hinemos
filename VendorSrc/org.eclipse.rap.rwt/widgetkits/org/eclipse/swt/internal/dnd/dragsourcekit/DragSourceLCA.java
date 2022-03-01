/*******************************************************************************
 * Copyright (c) 2009, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.dnd.dragsourcekit;

import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.hasChanged;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveListener;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListener;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.swt.internal.dnd.DNDUtil.convertOperations;
import static org.eclipse.swt.internal.dnd.DNDUtil.convertTransferTypes;
import static org.eclipse.swt.internal.dnd.DNDUtil.isCanceled;
import static org.eclipse.swt.internal.events.EventLCAUtil.isListening;

import java.io.IOException;

import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.lifecycle.AbstractWidgetLCA;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Widget;


public final class DragSourceLCA extends AbstractWidgetLCA {

  private static final String TYPE = "rwt.widgets.DragSource";
  private static final String PROP_TRANSFER = "transfer";
  private static final String PROP_DRAG_START_LISTENER = "DragStart";
  private static final String PROP_DRAG_END_LISTENER = "DragEnd";

  private static final Transfer[] DEFAULT_TRANSFER = new Transfer[ 0 ];

  @Override
  public void preserveValues( Widget widget ) {
    DragSource dragSource = ( DragSource )widget;
    preserveProperty( dragSource, PROP_TRANSFER, dragSource.getTransfer() );
    preserveListener( dragSource,
                      PROP_DRAG_START_LISTENER,
                      isListening( dragSource, DND.DragStart ) );
    preserveListener( dragSource, PROP_DRAG_END_LISTENER, isListening( dragSource, DND.DragEnd ) );
  }

  @Override
  public void readData( Widget widget ) {
  }

  @Override
  public void renderInitialization( Widget widget ) throws IOException {
    DragSource dragSource = ( DragSource )widget;
    RemoteObject remoteObject = createRemoteObject( dragSource, TYPE );
    remoteObject.setHandler( new DragSourceOperationHandler( dragSource ) );
    remoteObject.set( "control", getId( dragSource.getControl() ) );
    remoteObject.set( "style", convertOperations( dragSource.getStyle() ) );
  }

  @Override
  public void renderChanges( Widget widget ) throws IOException {
    DragSource dragSource = ( DragSource )widget;
    renderTransfer( dragSource );
    renderCancel( dragSource );
    renderListener( dragSource,
                    PROP_DRAG_START_LISTENER,
                    isListening( dragSource, DND.DragStart ),
                    false );
    renderListener( dragSource,
                    PROP_DRAG_END_LISTENER,
                    isListening( dragSource, DND.DragEnd ),
                    false );
  }

  private static void renderTransfer( DragSource dragSource ) {
    Transfer[] newValue = dragSource.getTransfer();
    if( hasChanged( dragSource, PROP_TRANSFER, newValue, DEFAULT_TRANSFER ) ) {
      JsonValue renderValue = convertTransferTypes( newValue );
      getRemoteObject( dragSource ).set( "transfer", renderValue );
    }
  }

  private static void renderCancel( DragSource dragSource ) {
    // TODO [tb] : would be rendered by all DragSources:
    if( isCanceled() ) {
      getRemoteObject( dragSource ).call( "cancel", null );
    }
  }

}
