/*******************************************************************************
 * Copyright (c) 2002, 2016 Innoopract Informationssysteme GmbH and others.
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

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.changed;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveListener;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListener;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;
import static org.eclipse.rap.rwt.internal.util.MnemonicUtil.removeAmpersandControlCharacters;
import static org.eclipse.rap.rwt.remote.JsonMapping.toJson;
import static org.eclipse.swt.internal.events.EventLCAUtil.isListening;
import static org.eclipse.swt.internal.widgets.MarkupUtil.isToolTipMarkupEnabledFor;

import java.lang.reflect.Field;

import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.util.ActiveKeysUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.widgets.ControlRemoteAdapter;
import org.eclipse.swt.internal.widgets.ControlUtil;
import org.eclipse.swt.internal.widgets.IControlAdapter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;


public class ControlLCAUtil {

  // Property names to preserve widget property values
  private static final String PROP_PARENT = "parent";
  private static final String PROP_CHILDREN = "children";
  private static final String PROP_BOUNDS = "bounds";
  private static final String PROP_TAB_INDEX = "tabIndex";
  private static final String PROP_TOOLTIP_TEXT = "toolTip";
  private static final String PROP_MENU = "menu";
  private static final String PROP_VISIBLE = "visibility";
  private static final String PROP_ENABLED = "enabled";
  private static final String PROP_FOREGROUND = "foreground";
  private static final String PROP_BACKGROUND = "background";
  private static final String PROP_BACKGROUND_IMAGE = "backgroundImage";
  private static final String PROP_FONT = "font";
  private static final String PROP_CURSOR = "cursor";
  private static final String PROP_ACTIVATE_LISTENER = "Activate";
  private static final String PROP_DEACTIVATE_LISTENER = "Deactivate";
  private static final String PROP_FOCUS_IN_LISTENER = "FocusIn";
  private static final String PROP_FOCUS_OUT_LISTENER = "FocusOut";
  private static final String PROP_MOUSE_DOWN_LISTENER = "MouseDown";
  private static final String PROP_MOUSE_DOUBLE_CLICK_LISTENER = "MouseDoubleClick";
  private static final String PROP_MOUSE_UP_LISTENER = "MouseUp";
  private static final String PROP_KEY_LISTENER = "KeyDown";
  private static final String PROP_TRAVERSE_LISTENER = "Traverse";
  private static final String PROP_MENU_DETECT_LISTENER = "MenuDetect";
  private static final String PROP_HELP_LISTENER = "Help";

  private static final String CURSOR_UPARROW
    = "rwt-resources/resource/widget/rap/cursors/up_arrow.cur";

  private ControlLCAUtil() {
    // prevent instance creation
  }

  public static void preserveValues( Control control ) {
    preserveParent( control );
    preserveChildren( control );
    preserveBounds( control );
    preserveTabIndex( control );
    preserveToolTipText( control );
    preserveMenu( control );
    preserveVisible( control );
    preserveEnabled( control );
    preserveForeground( control );
    preserveBackground( control );
    preserveBackgroundImage( control );
    preserveFont( control );
    preserveCursor( control );
    preserveData( control );
    ActiveKeysUtil.preserveActiveKeys( control );
    ActiveKeysUtil.preserveCancelKeys( control );
    preserveListenActivate( control );
    preserveListenMouse( control );
    preserveListenFocus( control );
    preserveListenKey( control );
    preserveListenTraverse( control );
    preserveListenMenuDetect( control );
    preserveListenHelp( control );
  }

  public static void renderChanges( Control control ) {
    renderChildren( control );
    renderBounds( control );
    renderTabIndex( control );
    renderToolTipText( control );
    renderMenu( control );
    renderVisible( control );
    renderEnabled( control );
    renderForeground( control );
    renderBackground( control );
    renderBackgroundImage( control );
    renderFont( control );
    renderCursor( control );
    renderData( control );
    ActiveKeysUtil.renderActiveKeys( control );
    ActiveKeysUtil.renderCancelKeys( control );
    renderListenActivate( control );
    renderListenMouse( control );
    renderListenFocus( control );
    renderListenKey( control );
    renderListenTraverse( control );
    renderListenMenuDetect( control );
    renderListenHelp( control );
  }

  private static void preserveParent( Control control ) {
    Composite parent = control.getParent();
    if( parent != null ) {
      getRemoteAdapter( control ).preserveParent( parent );
    }
  }

  public static void renderParent( Control control ) {
    ControlRemoteAdapter remoteAdapter = getRemoteAdapter( control );
    Composite actual = control.getParent();
    if( remoteAdapter.isInitialized() && actual != null ) {
      Composite preserved = remoteAdapter.getPreservedParent();
      if( changed( control, actual, preserved, null ) ) {
        getRemoteObject( control ).set( PROP_PARENT, getId( actual ) );
      }
    }
  }

  private static void preserveChildren( Control control ) {
    if( control instanceof Composite ) {
      Composite composite = ( Composite )control;
      getRemoteAdapter( control ).preserveChildren( composite.getChildren() );
    }
  }

  private static void renderChildren( Control control ) {
    if( control instanceof Composite ) {
      Composite composite = ( Composite )control;
      Control[] actual = composite.getChildren();
      Control[] preserved = getRemoteAdapter( control ).getPreservedChildren();
      if( changed( control, actual, preserved, null ) ) {
        getRemoteObject( control ).set( PROP_CHILDREN, getIdsAsJson( actual ) );
      }
    }
  }

  private static void preserveBounds( Control control ) {
    Rectangle bounds = ControlUtil.getControlAdapter( control ).getBounds();
    getRemoteAdapter( control ).preserveBounds( bounds );
  }

  private static void renderBounds( Control control ) {
    Rectangle actual = ControlUtil.getControlAdapter( control ).getBounds();
    Rectangle preserved = getRemoteAdapter( control ).getPreservedBounds();
    if( changed( control, actual, preserved, null ) ) {
      getRemoteObject( control ).set( PROP_BOUNDS, toJson( actual ) );
    }
  }

  private static void preserveTabIndex( Control control ) {
    getRemoteAdapter( control ).preserveTabIndex( getTabIndex( control ) );
  }

  private static void renderTabIndex( Control control ) {
    if( control instanceof Shell ) {
      resetTabIndices( ( Shell )control );
      // tabIndex must be a positive value
      computeTabIndices( ( Shell )control, 1 );
    }
    ControlRemoteAdapter remoteAdapter = getRemoteAdapter( control );
    if( takesFocus( control ) ) {
      int actual = getTabIndex( control );
      int preserved = remoteAdapter.getPreservedTabIndex();
      if( !remoteAdapter.isInitialized() || actual != preserved ) {
        getRemoteObject( control ).set( PROP_TAB_INDEX, actual );
      }
    }
  }

  private static void preserveToolTipText( Control control ) {
    getRemoteAdapter( control ).preserveToolTipText( control.getToolTipText() );
  }

  private static void renderToolTipText( Control control ) {
    WidgetLCAUtil.renderToolTipMarkupEnabled( control );
    String actual = control.getToolTipText();
    String preserved = getRemoteAdapter( control ).getPreservedToolTipText();
    if( changed( control, actual, preserved, null ) ) {
      String text = actual == null ? "" : actual;
      if( !isToolTipMarkupEnabledFor( control ) ) {
        text = removeAmpersandControlCharacters( text );
      }
      getRemoteObject( control ).set( PROP_TOOLTIP_TEXT, text );
    }
  }

  private static void preserveMenu( Control control ) {
    getRemoteAdapter( control ).preserveMenu( control.getMenu() );
  }

  private static void renderMenu( Control control ) {
    Menu actual = control.getMenu();
    Menu preserved = getRemoteAdapter( control ).getPreservedMenu();
    if( changed( control, actual, preserved, null ) ) {
      String actualMenuId = actual == null ? null : getId( actual );
      getRemoteObject( control ).set( PROP_MENU, actualMenuId );
    }
  }

  private static void preserveVisible( Control control ) {
    getRemoteAdapter( control ).preserveVisible( getVisible( control ) );
  }

  private static void renderVisible( Control control ) {
    boolean actual = getVisible( control );
    boolean preserved = getRemoteAdapter( control ).getPreservedVisible();
    boolean defaultValue = control instanceof Shell ? false : true;
    if( changed( control, actual, preserved, defaultValue ) ) {
      getRemoteObject( control ).set( PROP_VISIBLE, actual );
    }
  }

  private static void preserveEnabled( Control control ) {
    getRemoteAdapter( control ).preserveEnabled( control.getEnabled() );
  }

  private static void renderEnabled( Control control ) {
    // Using isEnabled() would result in unnecessarily updating child widgets of
    // enabled/disabled controls.
    boolean actual = control.getEnabled();
    boolean preserved = getRemoteAdapter( control ).getPreservedEnabled();
    if( changed( control, actual, preserved, true ) ) {
      getRemoteObject( control ).set( PROP_ENABLED, actual );
    }
  }

  private static void preserveForeground( Control control ) {
    IControlAdapter controlAdapter = ControlUtil.getControlAdapter( control );
    getRemoteAdapter( control ).preserveForeground( controlAdapter.getUserForeground() );
  }

  private static void renderForeground( Control control ) {
    IControlAdapter controlAdapter = ControlUtil.getControlAdapter( control );
    Color actual = controlAdapter.getUserForeground();
    Color preserved = getRemoteAdapter( control ).getPreservedForeground();
    if( changed( control, actual, preserved, null ) ) {
      getRemoteObject( control ).set( PROP_FOREGROUND, toJson( actual ) );
    }
  }

  private static void preserveBackground( Control control ) {
    ControlRemoteAdapter adapter = getRemoteAdapter( control );
    IControlAdapter controlAdapter = ControlUtil.getControlAdapter( control );
    adapter.preserveBackground( controlAdapter.getUserBackground() );
    adapter.preserveBackgroundTransparency( controlAdapter.getBackgroundTransparency() );
  }

  private static void renderBackground( Control control ) {
    IControlAdapter controlAdapter = ControlUtil.getControlAdapter( control );
    Color actualBackground = controlAdapter.getUserBackground();
    boolean actualTransparency = controlAdapter.getBackgroundTransparency();
    ControlRemoteAdapter remoteAdapter = getRemoteAdapter( control );
    boolean colorChanged = changed( control,
                                    actualBackground,
                                    remoteAdapter.getPreservedBackground(),
                                    null );
    boolean transparencyChanged = changed( control,
                                           actualTransparency,
                                           remoteAdapter.getPreservedBackgroundTransparency(),
                                           false );
    if( transparencyChanged || colorChanged ) {
      JsonValue color = actualTransparency && actualBackground == null
                      ? toJson( new RGB( 0, 0, 0 ), 0 )
                      : toJson( actualBackground, actualTransparency ? 0 : 255 );
      getRemoteObject( control ).set( PROP_BACKGROUND, color );
    }
  }

  private static void preserveBackgroundImage( Control control ) {
    IControlAdapter controlAdapter = ControlUtil.getControlAdapter( control );
    Image image = controlAdapter.getUserBackgroundImage();
    getRemoteAdapter( control ).preserveBackgroundImage( image );
  }

  private static void renderBackgroundImage( Control control ) {
    IControlAdapter controlAdapter = ControlUtil.getControlAdapter( control );
    Image actual = controlAdapter.getUserBackgroundImage();
    Image preserved = getRemoteAdapter( control ).getPreservedBackgroundImage();
    if( changed( control, actual, preserved, null ) ) {
      getRemoteObject( control ).set( PROP_BACKGROUND_IMAGE, toJson( actual ) );
    }
  }

  private static void preserveFont( Control control ) {
    IControlAdapter controlAdapter = ControlUtil.getControlAdapter( control );
    getRemoteAdapter( control ).preserveFont( controlAdapter.getUserFont() );
  }

  private static void renderFont( Control control ) {
    IControlAdapter controlAdapter = ControlUtil.getControlAdapter( control );
    Font actual = controlAdapter.getUserFont();
    Font preserved = getRemoteAdapter( control ).getPreservedFont();
    if( changed( control, actual, preserved, null ) ) {
      getRemoteObject( control ).set( PROP_FONT, toJson( actual ) );
    }
  }

  private static void preserveCursor( Control control ) {
    getRemoteAdapter( control ).preserveCursor( control.getCursor() );
  }

  private static void renderCursor( Control control ) {
    Cursor actual = control.getCursor();
    Object preserved = getRemoteAdapter( control ).getPreservedCursor();
    if( changed( control, actual, preserved, null ) ) {
      getRemoteObject( control ).set( PROP_CURSOR, getQxCursor( actual ) );
    }
  }

  private static void preserveData( Control control ) {
    WidgetLCAUtil.preserveData( control );
  }

  private static void renderData( Control control ) {
    WidgetLCAUtil.renderData( control );
  }

  private static void preserveListenActivate( Control control ) {
    // Note: Shell "Activate" event is handled by ShellLCA
    if( !( control instanceof Shell ) ) {
      preserveListener( control, SWT.Activate );
      preserveListener( control, SWT.Deactivate );
    }
  }

  private static void renderListenActivate( Control control ) {
    // Note: Shell "Activate" event is handled by ShellLCA
    if( !( control instanceof Shell ) ) {
      renderListener( control, SWT.Activate, PROP_ACTIVATE_LISTENER );
      renderListener( control, SWT.Deactivate, PROP_DEACTIVATE_LISTENER );
    }
  }

  private static void preserveListenMouse( Control control ) {
    preserveListener( control, SWT.MouseDown );
    preserveListener( control, SWT.MouseUp );
    preserveListener( control, SWT.MouseDoubleClick );
  }

  private static void renderListenMouse( Control control ) {
    renderListener( control, SWT.MouseDown, PROP_MOUSE_DOWN_LISTENER );
    renderListener( control, SWT.MouseUp, PROP_MOUSE_UP_LISTENER );
    renderListener( control, SWT.MouseDoubleClick, PROP_MOUSE_DOUBLE_CLICK_LISTENER );
  }

  private static void preserveListenFocus( Control control ) {
    if( ( control.getStyle() & SWT.NO_FOCUS ) == 0 ) {
      preserveListener( control, SWT.FocusIn );
      preserveListener( control, SWT.FocusOut );
    }
  }

  private static void renderListenFocus( Control control ) {
    if( ( control.getStyle() & SWT.NO_FOCUS ) == 0 ) {
      renderListener( control, SWT.FocusIn, PROP_FOCUS_IN_LISTENER );
      renderListener( control, SWT.FocusOut, PROP_FOCUS_OUT_LISTENER );
    }
  }

  private static void preserveListenKey( Control control ) {
    preserveListener( control, SWT.KeyDown, hasKeyListener( control ) );
  }

  private static void renderListenKey( Control control ) {
    renderListener( control, SWT.KeyDown, PROP_KEY_LISTENER, hasKeyListener( control ) );
  }

  private static void preserveListenTraverse( Control control ) {
    preserveListener( control, SWT.Traverse );
  }

  private static void renderListenTraverse( Control control ) {
    renderListener( control, SWT.Traverse, PROP_TRAVERSE_LISTENER );
  }

  private static void preserveListenMenuDetect( Control control ) {
    preserveListener( control, SWT.MenuDetect );
  }

  private static void renderListenMenuDetect( Control control ) {
    renderListener( control, SWT.MenuDetect, PROP_MENU_DETECT_LISTENER );
  }

  private static void preserveListenHelp( Control control ) {
    preserveListener( control, SWT.Help );
  }

  private static void renderListenHelp( Control control ) {
    renderListener( control, SWT.Help, PROP_HELP_LISTENER );
  }

  // [if] Fix for bug 263025, 297466, 223873 and more
  // some qooxdoo widgets with size (0,0) are not invisible
  private static boolean getVisible( Control control ) {
    Rectangle bounds = ControlUtil.getControlAdapter( control ).getBounds();
    return control.getVisible() && bounds.width > 0 && bounds.height > 0;
  }

  // TODO [rh] Eliminate instance checks. Let the respective classes always return NO_FOCUS
  private static boolean takesFocus( Control control ) {
    boolean result = true;
    result &= ( control.getStyle() & SWT.NO_FOCUS ) == 0;
    result &= control.getClass() != Composite.class;
    result &= control.getClass() != SashForm.class;
    return result;
  }

  private static int getTabIndex( Control control ) {
    int result = -1;
    if( takesFocus( control ) ) {
      result = ControlUtil.getControlAdapter( control ).getTabIndex();
    }
    return result;
  }

  private static void resetTabIndices( Composite composite ) {
    for( Control control : composite.getChildren() ) {
      ControlUtil.getControlAdapter( control ).setTabIndex( -1 );
      if( control instanceof Composite ) {
        resetTabIndices( ( Composite )control );
      }
    }
  }

  private static int computeTabIndices( Composite composite, int startIndex ) {
    int result = startIndex;
    for( Control control : composite.getTabList() ) {
      IControlAdapter controlAdapter = ControlUtil.getControlAdapter( control );
      controlAdapter.setTabIndex( result );
      // for Links, leave a range out to be assigned to hrefs on the client
      result += control instanceof Link ? 300 : 1;
      if( control instanceof Composite ) {
        result = computeTabIndices( ( Composite )control, result );
      }
    }
    return result;
  }

  private static JsonValue getIdsAsJson( Control[] controls ) {
    String[] controlIds = new String[ controls.length ];
    for( int i = 0; i < controls.length; i++ ) {
      controlIds[ i ] = getId( controls[ i ] );
    }
    // TODO [rst] Can we also render an empty array instead of null?
    return controlIds.length == 0 ? JsonValue.NULL : createJsonArray( controlIds );
  }

  private static String getQxCursor( Cursor newValue ) {
    String result = null;
    if( newValue != null ) {
      // TODO [rst] Find a better way of obtaining the Cursor value
      // TODO [tb] adjust strings to match name of constants
      int value = 0;
      try {
        Class cursorClass = Cursor.class;
        Field field = cursorClass.getDeclaredField( "value" );
        field.setAccessible( true );
        value = field.getInt( newValue );
      } catch( Exception e ) {
        throw new RuntimeException( e );
      }
      switch( value ) {
        case SWT.CURSOR_ARROW:
          result = "default";
        break;
        case SWT.CURSOR_WAIT:
          result = "wait";
        break;
        case SWT.CURSOR_APPSTARTING:
          result = "progress";
          break;
        case SWT.CURSOR_CROSS:
          result = "crosshair";
        break;
        case SWT.CURSOR_HELP:
          result = "help";
        break;
        case SWT.CURSOR_SIZEALL:
          result = "move";
        break;
        case SWT.CURSOR_SIZENS:
          result = "row-resize";
        break;
        case SWT.CURSOR_SIZEWE:
          result = "col-resize";
        break;
        case SWT.CURSOR_SIZEN:
          result = "n-resize";
        break;
        case SWT.CURSOR_SIZES:
          result = "s-resize";
        break;
        case SWT.CURSOR_SIZEE:
          result = "e-resize";
        break;
        case SWT.CURSOR_SIZEW:
          result = "w-resize";
        break;
        case SWT.CURSOR_SIZENE:
        case SWT.CURSOR_SIZENESW:
          result = "ne-resize";
        break;
        case SWT.CURSOR_SIZESE:
          result = "se-resize";
        break;
        case SWT.CURSOR_SIZESW:
          result = "sw-resize";
        break;
        case SWT.CURSOR_SIZENW:
        case SWT.CURSOR_SIZENWSE:
          result = "nw-resize";
        break;
        case SWT.CURSOR_IBEAM:
          result = "text";
        break;
        case SWT.CURSOR_HAND:
          result = "pointer";
        break;
        case SWT.CURSOR_NO:
          result = "not-allowed";
        break;
        case SWT.CURSOR_UPARROW:
          result = CURSOR_UPARROW;
        break;
      }
    }
    return result;
  }

  private static boolean hasKeyListener( Control control ) {
    return isListening( control, SWT.KeyUp ) || isListening( control, SWT.KeyDown );
  }

  private static ControlRemoteAdapter getRemoteAdapter( Control control ) {
    return ( ControlRemoteAdapter )control.getAdapter( RemoteAdapter.class );
  }

}
