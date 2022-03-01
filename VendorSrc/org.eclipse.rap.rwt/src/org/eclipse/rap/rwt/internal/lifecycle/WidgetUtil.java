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
package org.eclipse.rap.rwt.internal.lifecycle;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.util.ParamCheck;
import org.eclipse.swt.internal.widgets.WidgetTreeVisitor;
import org.eclipse.swt.internal.widgets.WidgetTreeVisitor.AllWidgetTreeVisitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;


public final class WidgetUtil {

  private WidgetUtil() {
    // prevent instantiation
  }

  public static RemoteAdapter getAdapter( Widget widget ) {
    RemoteAdapter adapter = widget.getAdapter( RemoteAdapter.class );
    if( adapter == null ) {
      throw new IllegalStateException( "Could not retrieve an instance of WidgetAdapter." );
    }
    return adapter;
  }

  public static String getId( Widget widget ) {
    return getAdapter( widget ).getId();
  }

  public static String getVariant( Widget widget ) {
    return ( String )widget.getData( RWT.CUSTOM_VARIANT );
  }

  public static AbstractWidgetLCA getLCA( Widget widget ) {
    AbstractWidgetLCA lca = ( AbstractWidgetLCA )widget.getAdapter( WidgetLifeCycleAdapter.class );
    if( lca == null ) {
      throw new IllegalStateException( "Could not retrieve an instance of WidgetLifeCycleAdapter." );
    }
    return lca;
  }

  public static Widget find( Composite root, final String id ) {
    final Widget[] result = { null };
    if( id != null ) {
      WidgetTreeVisitor.accept( root, new AllWidgetTreeVisitor() {
        @Override
        public boolean doVisit( Widget widget ) {
          if( getId( widget ).equals( id ) ) {
            result[ 0 ] = widget;
          }
          return result[ 0 ] == null;
        }
      } );
    }
    return result[ 0 ];
  }

  public static void registerDataKeys( String... keys ) {
    ParamCheck.notNull( keys, "keys" );
    WidgetDataUtil.registerDataKeys( keys );
  }

}
