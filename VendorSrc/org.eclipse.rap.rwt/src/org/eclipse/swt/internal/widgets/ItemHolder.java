/*******************************************************************************
 * Copyright (c) 2002, 2014 Innoopract Informationssysteme GmbH and others.
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.SerializableCompatibility;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;

public final class ItemHolder<T extends Item>
  implements IItemHolderAdapter<T>, SerializableCompatibility
{

  @SuppressWarnings("unchecked")
  public static <T extends Item> IItemHolderAdapter<T> getItemHolder( Widget widget ) {
    if( !isItemHolder( widget ) ) {
      String txt = "Widget type does not contain items: " + widget.getClass().getName();
      throw new IllegalArgumentException( txt );
    }
    return widget.getAdapter( IItemHolderAdapter.class );
  }

  public static boolean isItemHolder( Widget widget ) {
    return widget.getAdapter( IItemHolderAdapter.class ) != null;
  }

  private final List<T> items;
  private final Class type;

  public ItemHolder( Class<T> type ) {
    this.type = type;
    this.items = new ArrayList<T>();
  }

  public int size() {
    return items.size();
  }

  public void add( T item ) {
    if( item == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( items.contains( item ) ) {
      String msg = "The item was already added.";
      throw new IllegalArgumentException( msg );
    }
    items.add( item );
  }

  public void insert( T item, int index ) {
    if( item == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( index < 0 || index > size() ) {
      SWT.error( SWT.ERROR_INVALID_RANGE );
    }
    if( items.contains( item ) ) {
      throw new IllegalArgumentException( "The item was already added." );
    }
    items.add( index, item );
  }

  public void remove( T item ) {
    if( item == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    if( !items.contains( item ) ) {
      throw new IllegalArgumentException( "The item was not added to this item holder." );
    }
    items.remove( item );
  }

  @SuppressWarnings("unchecked")
  public T[] getItems() {
    T[] result = ( T[] )Array.newInstance( type, items.size() );
    return items.toArray( result );
  }

  public T getItem( int index ) {
    if( index < 0 || index >= items.size() ) {
      SWT.error( SWT.ERROR_INVALID_RANGE );
    }
    return items.get( index );
  }

  public int indexOf ( T item ) {
    return items.indexOf( item );
  }

}
