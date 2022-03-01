/*******************************************************************************
 * Copyright (c) 2008, 2012 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.theme;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;


class ThemeableWidgetHolder {
  private final Map<Class,ThemeableWidget> themeableWidgets;

  ThemeableWidgetHolder() {
    themeableWidgets = new LinkedHashMap<Class,ThemeableWidget>();
  }

  void add( ThemeableWidget widget ) {
    themeableWidgets.put( widget.widget, widget );
  }

  ThemeableWidget get( Class widget ) {
    return themeableWidgets.get( widget );
  }

  ThemeableWidget[] getAll() {
    Collection<ThemeableWidget> values = themeableWidgets.values();
    return values.toArray( new ThemeableWidget[ values.size() ] );
  }

  void reset() {
    themeableWidgets.clear();
  }
}
