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
package org.eclipse.rap.rwt.internal.lifecycle;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.rwt.internal.util.ClassInstantiationException;
import org.eclipse.rap.rwt.internal.util.ClassUtil;
import org.eclipse.swt.internal.widgets.displaykit.DisplayLCA;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;


public final class LifeCycleAdapterFactory {

  private final Object displayAdapterLock;
  private final Object widgetAdaptersLock;
  // Holds the single display life cycle adapter. MUST be created lazily because its constructor
  // needs a resource manager to be in place
  private DisplayLifeCycleAdapter displayAdapter;
  // Maps widget classes to their respective life cycle adapters
  private final Map<Class, WidgetLifeCycleAdapter> widgetAdapters;

  public LifeCycleAdapterFactory() {
    displayAdapterLock = new Object();
    widgetAdaptersLock = new Object();
    widgetAdapters = new HashMap<Class, WidgetLifeCycleAdapter>();
  }

  public Object getAdapter( Object adaptable ) {
    Object result = null;
    if( adaptable instanceof Display ) {
      result = getDisplayLCA();
    } else if( adaptable instanceof Widget ) {
      result = getWidgetLCA( adaptable.getClass() );
    }
    return result;
  }

  ///////////////////////////////////////////////////////////
  // Helping methods to obtain life cycle adapter for display

  private DisplayLifeCycleAdapter getDisplayLCA() {
    synchronized( displayAdapterLock ) {
      if( displayAdapter == null ) {
        displayAdapter = new DisplayLCA();
      }
      return displayAdapter;
    }
  }

  ////////////////////////////////////////////////////////////
  // Helping methods to obtain life cycle adapters for widgets

  private WidgetLifeCycleAdapter getWidgetLCA( Class clazz ) {
    // [fappel] This code is performance critical, don't change without checking against a profiler
    WidgetLifeCycleAdapter result;
    synchronized( widgetAdaptersLock ) {
      result = widgetAdapters.get( clazz );
      if( result == null ) {
        WidgetLifeCycleAdapter adapter = null;
        Class superClass = clazz;
        while( !Object.class.equals( superClass ) && adapter == null ) {
          adapter = loadWidgetLCA( superClass );
          if( adapter == null ) {
            superClass = superClass.getSuperclass();
          }
        }
        widgetAdapters.put( clazz, adapter );
        result = adapter;
      }
    }
    if( result == null ) {
      String msg = "Failed to obtain life cycle adapter for: " + clazz.getName();
      throw new LifeCycleAdapterException( msg );
    }
    return result;
  }

  private static WidgetLifeCycleAdapter loadWidgetLCA( Class clazz ) {
    WidgetLifeCycleAdapter result = null;
    String className = LifeCycleAdapterUtil.getSimpleClassName( clazz );
    String[] variants = LifeCycleAdapterUtil.getKitPackageVariants( clazz );
    for( int i = 0; result == null && i < variants.length; i++ ) {
      StringBuilder buffer = new StringBuilder();
      buffer.append( variants[ i ] );
      buffer.append( "." );
      buffer.append( className );
      buffer.append( "LCA" );
      String classToLoad = buffer.toString();
      ClassLoader loader = clazz.getClassLoader();
      try {
        result = ( WidgetLifeCycleAdapter )ClassUtil.newInstance( loader, classToLoad );
      } catch( ClassInstantiationException ignore ) {
        // ignore and try to load next package name variant
      }
    }
    return result;
  }
}
