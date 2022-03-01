/*******************************************************************************
 * Copyright (c) 2008, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.sliderkit;

import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveListenSelection;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListenSelection;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import java.io.IOException;

import org.eclipse.rap.rwt.internal.lifecycle.AbstractWidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.ControlLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Widget;


public class SliderLCA extends AbstractWidgetLCA {

  private static final String TYPE = "rwt.widgets.Slider";
  private static final String[] ALLOWED_STYLES = new String[] {
    "HORIZONTAL", "VERTICAL", "BORDER"
  };

  // Property names for preserveValues
  static final String PROP_MINIMUM = "minimum";
  static final String PROP_MAXIMUM = "maximum";
  static final String PROP_SELECTION = "selection";
  static final String PROP_INCREMENT = "increment";
  static final String PROP_PAGE_INCREMENT = "pageIncrement";
  static final String PROP_THUMB = "thumb";

  // Default values
  private static final int DEFAULT_MINIMUM = 0;
  private static final int DEFAULT_MAXIMUM = 100;
  private static final int DEFAULT_SELECTION = 0;
  private static final int DEFAULT_INCREMENT = 1;
  private static final int DEFAULT_PINCREMENT = 10;
  private static final int DEFAULT_THUMB = 10;

  @Override
  public void preserveValues( Widget widget ) {
    Slider slider = ( Slider )widget;
    ControlLCAUtil.preserveValues( slider );
    WidgetLCAUtil.preserveCustomVariant( slider );
    preserveProperty( slider, PROP_MINIMUM, slider.getMinimum() );
    preserveProperty( slider, PROP_MAXIMUM, slider.getMaximum() );
    preserveProperty( slider, PROP_SELECTION, slider.getSelection() );
    preserveProperty( slider, PROP_INCREMENT, slider.getIncrement() );
    preserveProperty( slider, PROP_PAGE_INCREMENT, slider.getPageIncrement() );
    preserveProperty( slider, PROP_THUMB, slider.getThumb() );
    preserveListenSelection( slider );
  }

  @Override
  public void renderInitialization( Widget widget ) throws IOException {
    Slider slider = ( Slider )widget;
    RemoteObject remoteObject = createRemoteObject( slider, TYPE );
    remoteObject.setHandler( new SliderOperationHandler( slider ) );
    remoteObject.set( "parent", getId( slider.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( slider, ALLOWED_STYLES ) ) );
  }

  @Override
  public void renderChanges( Widget widget ) throws IOException {
    Slider slider = ( Slider )widget;
    ControlLCAUtil.renderChanges( slider );
    WidgetLCAUtil.renderCustomVariant( widget );
    renderProperty( slider, PROP_MINIMUM, slider.getMinimum(), DEFAULT_MINIMUM );
    renderProperty( slider, PROP_MAXIMUM, slider.getMaximum(), DEFAULT_MAXIMUM );
    renderProperty( slider, PROP_SELECTION, slider.getSelection(), DEFAULT_SELECTION );
    renderProperty( slider, PROP_INCREMENT, slider.getIncrement(), DEFAULT_INCREMENT );
    renderProperty( slider, PROP_PAGE_INCREMENT, slider.getPageIncrement(), DEFAULT_PINCREMENT );
    renderProperty( slider, PROP_THUMB, slider.getThumb(), DEFAULT_THUMB );
    renderListenSelection( slider );
  }

}
