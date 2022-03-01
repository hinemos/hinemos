/*******************************************************************************
 * Copyright (c) 2009, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 *    Frank Appel - removed singletons and static fields (Bug 227787)
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.theme;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.rwt.internal.lifecycle.LifeCycleAdapterUtil;
import org.eclipse.rap.rwt.internal.util.ClassInstantiationException;
import org.eclipse.rap.rwt.internal.util.ClassUtil;
import org.eclipse.swt.internal.widgets.controlkit.ControlThemeAdapterImpl;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;


public final class ThemeAdapterManager {
  private final Map<Class,ThemeAdapter> themeAdapters;

  public ThemeAdapterManager() {
    themeAdapters = new HashMap<Class, ThemeAdapter>();
  }

  public void reset() {
    themeAdapters.clear();
  }

  public ThemeAdapter getThemeAdapter( Widget widget ) {
    Class widgetClass = widget.getClass();
    ThemeAdapter result;
    synchronized( themeAdapters ) {
      result = themeAdapters.get( widgetClass );
      if( result == null ) {
        ThemeAdapter adapter = findThemeAdapter( widgetClass );
        themeAdapters.put( widgetClass, adapter );
        result = adapter;
      }
    }
    ensureThemeAdapterWasFound( widgetClass, result );
    return result;
  }

  private static ThemeAdapter findThemeAdapter( Class widgetClass ) {
    ThemeAdapter result = null;
    Class superClass = widgetClass;
    while( !Object.class.equals( superClass ) && result == null ) {
      result = loadThemeAdapter( superClass );
      if( result == null ) {
        superClass = superClass.getSuperclass();
      }
    }
    return result;
  }

  private static ThemeAdapter loadThemeAdapter( Class clazz ) {
    ThemeAdapter result = null;
    if( clazz == Control.class ) {
      result = new ControlThemeAdapterImpl();
    } else {
      String className = LifeCycleAdapterUtil.getSimpleClassName( clazz );
      String[] variants = LifeCycleAdapterUtil.getKitPackageVariants( clazz );
      for( int i = 0; result == null && i < variants.length; i++ ) {
        StringBuilder buffer = new StringBuilder();
        buffer.append( variants[ i ] );
        buffer.append( "." );
        buffer.append( className );
        buffer.append( "ThemeAdapter" );
        String classToLoad = buffer.toString();
        ClassLoader loader = clazz.getClassLoader();
        result = loadThemeAdapter( classToLoad, loader );
      }
    }
    return result;
  }

  private static ThemeAdapter loadThemeAdapter( String className, ClassLoader classLoader ) {
    ThemeAdapter result = null;
    try {
      result = ( ThemeAdapter )ClassUtil.newInstance( classLoader, className );
    } catch( ClassInstantiationException cie ) {
      // ignore, try to load from next package name variant
    }
    return result;
  }

  private static void ensureThemeAdapterWasFound( Class widgetClass, ThemeAdapter result ) {
    if( result == null ) {
      String msg = "Failed to obtain theme adapter for class: " + widgetClass.getName();
      throw new ThemeManagerException( msg );
    }
  }
}
