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
package org.eclipse.swt.internal.widgets;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.rwt.internal.lifecycle.DisposedWidgets;
import org.eclipse.rap.rwt.internal.lifecycle.RemoteAdapter;
import org.eclipse.swt.internal.SerializableCompatibility;
import org.eclipse.swt.widgets.Widget;


public class WidgetRemoteAdapter implements RemoteAdapter, SerializableCompatibility {

  private final static Runnable[] EMPTY = new Runnable[ 0 ];

  private final String id;
  private Widget parent;
  private boolean initialized;
  private transient Map<String, Object> preservedValues;
  private transient long preservedListeners;
  private transient Runnable[] renderRunnables;
  private transient Object[] data;
  private transient String variant;

  public WidgetRemoteAdapter( String id ) {
    this.id = id;
    initialize();
  }

  void initialize() {
    preservedValues = new HashMap<>();
  }

  @Override
  public String getId() {
    return id;
  }

  public void setParent( Widget parent ) {
    this.parent = parent;
  }

  @Override
  public Widget getParent() {
    return parent;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  public void setInitialized( boolean initialized ) {
    this.initialized = initialized;
  }

  @Override
  public void preserve( String propertyName, Object value ) {
    preservedValues.put( propertyName, value );
  }

  @Override
  public Object getPreserved( String propertyName ) {
    return preservedValues.get( propertyName );
  }

  public void preserveListener( int eventType, boolean value ) {
    long eventMask = getEventMask( eventType );
    if( value ) {
      preservedListeners |= eventMask;
    } else {
      preservedListeners &= ~eventMask;
    }
  }

  public boolean getPreservedListener( int eventType ) {
    return ( preservedListeners & getEventMask( eventType ) ) != 0;
  }

  public void preserveData( Object[] data ) {
    this.data = data;
  }

  public Object[] getPreservedData() {
    return data;
  }

  public void preserveVariant( String variant ) {
    this.variant = variant;
  }

  public String getPreservedVariant() {
    return variant;
  }

  public void clearPreserved() {
    preservedValues.clear();
    preservedListeners = 0;
    data = null;
    variant = null;
  }

  public void addRenderRunnable( Runnable renderRunnable ) {
    if( renderRunnables == null ) {
      renderRunnables = new Runnable[] { renderRunnable };
    } else {
      Runnable[] newRunnables = new Runnable[ renderRunnables.length + 1 ];
      System.arraycopy( renderRunnables, 0, newRunnables, 0, renderRunnables.length );
      newRunnables[ newRunnables.length - 1 ] = renderRunnable;
      renderRunnables = newRunnables;
    }
  }

  public Runnable[] getRenderRunnables() {
    return renderRunnables == null ? EMPTY : renderRunnables;
  }

  public void clearRenderRunnables() {
    renderRunnables = null;
  }

  @Override
  public void markDisposed( Widget widget ) {
    if( initialized ) {
      DisposedWidgets.add( widget );
    }
  }

  private Object readResolve() {
    initialize();
    return this;
  }

  private static long getEventMask( int eventType ) {
    if( eventType <= 0 || eventType > 64 ) {
      throw new IllegalArgumentException( "eventType not supported: " + eventType );
    }
    return 1L << (eventType - 1);
  }

}
