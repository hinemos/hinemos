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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.internal.SerializableCompatibility;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Widget;

public final class MenuHolder implements SerializableCompatibility {

  public static interface IMenuHolderAdapter {
    // marker interface
  }

  public static boolean isMenuHolder( Widget widget ) {
    return widget.getAdapter( IMenuHolderAdapter.class ) != null;
  }

  public static void addMenu( Widget widget, Menu menu ) {
    getMenuHolder( widget ).addMenu( menu );
  }

  public static void removeMenu( Widget widget, Menu menu ) {
    getMenuHolder( widget ).removeMenu( menu );
  }

  public static int getMenuCount( Widget widget ) {
    return getMenuHolder( widget ).getMenuCount();
  }

  public static Menu[] getMenus( Widget widget ) {
    return getMenuHolder( widget ).getMenus();
  }

  private final List<Menu> menus;

  public MenuHolder() {
    menus = new ArrayList<Menu>();
  }

  private void addMenu( Menu menu ) {
    menus.add( menu );
  }

  private void removeMenu( Menu menu ) {
    menus.remove( menu );
  }

  private Menu[] getMenus() {
    return menus.toArray( new Menu[ menus.size() ] );
  }

  private int getMenuCount() {
    return menus.size();
  }

  private static MenuHolder getMenuHolder( Widget widget ) {
    Object adapter = widget.getAdapter( IMenuHolderAdapter.class );
    return ( MenuHolder )adapter;
  }

}
