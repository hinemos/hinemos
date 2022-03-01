/*******************************************************************************
 * Copyright (c) 2008, 2014 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.datetimekit;

import java.io.IOException;

import org.eclipse.rap.rwt.internal.lifecycle.AbstractWidgetLCA;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Widget;


public final class DateTimeLCA extends AbstractWidgetLCA {

  private static final AbstractDateTimeLCADelegate DATE_LCA = new DateTimeDateLCA();
  private static final AbstractDateTimeLCADelegate TIME_LCA = new DateTimeTimeLCA();
  private static final AbstractDateTimeLCADelegate CALENDAR_LCA = new DateTimeCalendarLCA();

  @Override
  public void preserveValues( Widget widget ) {
    getDelegate( widget ).preserveValues( ( DateTime )widget );
  }

  @Override
  public void renderInitialization( Widget widget ) throws IOException {
    getDelegate( widget ).renderInitialization( ( DateTime )widget );
  }

  @Override
  public void renderChanges( Widget widget ) throws IOException {
    getDelegate( widget ).renderChanges( ( DateTime )widget );
  }

  private static AbstractDateTimeLCADelegate getDelegate( Widget widget ) {
    AbstractDateTimeLCADelegate result;
    if( ( widget.getStyle() & SWT.DATE ) != 0 ) {
      result = DATE_LCA;
    } else if( ( widget.getStyle() & SWT.TIME ) != 0 ) {
      result = TIME_LCA;
    } else {
      result = CALENDAR_LCA;
    }
    return result;
  }
}
