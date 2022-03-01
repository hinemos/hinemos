/*******************************************************************************
 * Copyright (c) 2002, 2012 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 *    Rüdiger Herrmann - bug 335112
 ******************************************************************************/
package org.eclipse.swt.internal.widgets;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Widget;


/**
 * Utility class that provides a traversal through a widget-tree
 * using the visitor pattern.
 *
 * <p>The traversal through the children will be skipped if the visit call
 * on the parent node returns <code>false</code>.</p>
 */
public class WidgetTreeVisitor {

  public static abstract class AllWidgetTreeVisitor extends WidgetTreeVisitor {

    public final boolean visit( Widget widget ) {
      return doVisit( widget );
    }

    public final boolean visit( Composite composite ) {
      return doVisit( composite );
    }

    public abstract boolean doVisit( Widget widget );
  }

  // TODO [rh] all SWT Menu have shell as their parent
  //      we should visit the menus as part of visiting shell, not on each
  //      control -> could lead to visiting one menu multiple times
  public static void accept( Widget root, WidgetTreeVisitor visitor ) {
    if( root instanceof Composite ) {
      Composite composite = ( Composite )root;
      if( visitor.visit( composite ) ) {
        handleMenus( composite, visitor );
        handleDragDrop( root, visitor );
        handleDecorator( root, visitor );
        handleItems( root, visitor );
        handleChildren( composite, visitor );
        handleToolTips( root, visitor );
      }
    } else if( ItemHolder.isItemHolder( root ) ) {
      if( visitor.visit( root ) ) {
        handleDragDrop( root, visitor );
        handleDecorator( root, visitor );
        handleItems( root, visitor );
      }
    } else {
      if( visitor.visit( root ) ) {
        handleDragDrop( root, visitor );
        handleDecorator( root, visitor );
      }
    }
  }

  public boolean visit( Widget widget ) {
    return true;
  }

  public boolean visit( Composite composite ) {
    return true;
  }

  ///////////////////////////////////////////////////
  // Helping methods to visit particular hierarchies

  private static void handleMenus( Composite composite, WidgetTreeVisitor visitor ) {
    if( MenuHolder.isMenuHolder( composite ) ) {
      Menu[] menus = MenuHolder.getMenus( composite );
      for( int i = 0; i < menus.length; i++ ) {
        accept( menus[ i ], visitor );
      }
    }
  }

  private static void handleDragDrop( Widget widget, WidgetTreeVisitor visitor ) {
    if( widget instanceof Control ) {
      Widget dragSource = ( Widget )widget.getData( DND.DRAG_SOURCE_KEY );
      if( dragSource != null ) {
        visitor.visit( dragSource );
      }
      Widget dropTarget = ( Widget )widget.getData( DND.DROP_TARGET_KEY );
      if( dropTarget != null ) {
        visitor.visit( dropTarget );
      }
    }
  }

  private static void handleDecorator( Widget root, WidgetTreeVisitor visitor ) {
    Decorator[] decorators = Decorator.getDecorators( root );
    for( int i = 0; i < decorators.length; i++ ) {
      visitor.visit( decorators[ i ] );
    }
  }

  private static void handleItems( Widget root, WidgetTreeVisitor visitor ) {
    if( ItemHolder.isItemHolder( root ) ) {
      Item[] items = ItemHolder.getItemHolder( root ).getItems();
      for( int i = 0; i < items.length; i++ ) {
        accept( items[ i ], visitor );
      }
    }
  }

  private static void handleChildren( Composite composite, WidgetTreeVisitor visitor ) {
    IControlHolderAdapter adapter = composite.getAdapter( IControlHolderAdapter.class );
    Control[] children = adapter.getControls();
    for( int i = 0; i < children.length; i++ ) {
      accept( children[ i ], visitor );
    }
  }

  private static void handleToolTips( Widget root, WidgetTreeVisitor visitor ) {
    Object adapter = root.getAdapter( IShellAdapter.class );
    if( adapter != null ) {
      IShellAdapter shellAdapter = ( IShellAdapter )adapter;
      ToolTip[] toolTips = shellAdapter.getToolTips();
      for( int i = 0; i < toolTips.length; i++ ) {
        visitor.visit( toolTips[ i ] );
      }
    }
  }
}
