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
package org.eclipse.swt.internal.widgets.menukit;

import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_SHOW;
import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveListenHelp;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveListener;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListenHelp;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListener;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.wasEventSent;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.swt.internal.events.EventLCAUtil.isListening;

import java.io.IOException;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.internal.lifecycle.AbstractWidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.widgets.IMenuAdapter;
import org.eclipse.swt.internal.widgets.IShellAdapter;
import org.eclipse.swt.internal.widgets.Props;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;


public final class MenuLCA extends AbstractWidgetLCA {

  private static final String TYPE = "rwt.widgets.Menu";
  private static final String[] ALLOWED_STYLES = new String[] {
    "BAR", "DROP_DOWN", "POP_UP", "NO_RADIO_GROUP"
  };

  private static final String PROP_ENABLED = "enabled";
  private static final String PROP_SHOW_LISTENER = "Show";
  private static final String PROP_HIDE_LISTENER = "Hide";
  private static final String METHOD_UNHIDE_ITEMS = "unhideItems";
  private static final String METHOD_SHOW_MENU = "showMenu";

  private static final Rectangle DEFAULT_BOUNDS = new Rectangle( 0, 0, 0, 0 );

  @Override
  public void preserveValues( Widget widget ) {
    Menu menu = ( Menu )widget;
    preserveProperty( menu, PROP_ENABLED, menu.getEnabled() );
    preserveListener( menu, PROP_SHOW_LISTENER, hasShowListener( menu ) );
    preserveListener( menu, PROP_HIDE_LISTENER, hasHideListener( menu ) );
    WidgetLCAUtil.preserveCustomVariant( menu );
    preserveListenHelp( menu );
  }

  @Override
  public void renderInitialization( Widget widget ) throws IOException {
    Menu menu = ( Menu )widget;
    RemoteObject remoteObject = createRemoteObject( menu , TYPE );
    remoteObject.setHandler( new MenuOperationHandler( menu ) );
    remoteObject.set( "parent", getId( menu.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( menu, ALLOWED_STYLES ) ) );
  }

  @Override
  public void renderChanges( Widget widget ) throws IOException {
    Menu menu = ( Menu )widget;
    renderProperty( menu, PROP_ENABLED, menu.getEnabled(), true );
    renderListener( menu, PROP_SHOW_LISTENER, hasShowListener( menu ), false );
    renderListener( menu, PROP_HIDE_LISTENER, hasHideListener( menu ), false );
    WidgetLCAUtil.renderCustomVariant( menu );
    renderListenHelp( menu );
    renderBounds( menu );
    renderShow( menu );
    renderUnhideItems( menu );
  }

  @Override
  public void renderDispose( Widget widget ) throws IOException {
    // TODO [tb] : The menu can currently not be destroyed automatically on the client
    getRemoteObject( widget ).destroy();
  }

  private static void renderBounds( Menu menu ) {
    if( isMenuBar( menu ) ) {
      // Bounds are preserved in ShellLCA#preserveMenuBounds
      renderProperty( menu, Props.BOUNDS, getBounds( menu ), DEFAULT_BOUNDS );
    }
  }

  private static void renderShow( Menu menu ) {
    if( isPopupMenu( menu ) &&  menu.isVisible() ) {
      IMenuAdapter adapter = menu.getAdapter( IMenuAdapter.class );
      Point location = adapter.getLocation();
      JsonObject parameters = new JsonObject().add( "x", location.x ).add( "y", location.y );
      getRemoteObject( menu ).call( METHOD_SHOW_MENU, parameters );
      menu.setVisible( false );
    }
  }

  /* (intentionally non-JavaDoc'ed)
   * Activates the menu if a menu event was received (in this case, only a
   * preliminary menu is displayed).
   */
  private static void renderUnhideItems( Menu menu ) {
    if( ( isPopupMenu( menu ) || isDropDownMenu( menu ) ) && wasEventSent( menu, EVENT_SHOW ) ) {
      boolean reveal = menu.getItemCount() > 0;
      JsonObject parameters = new JsonObject().add( "reveal", reveal );
      getRemoteObject( menu ).call( METHOD_UNHIDE_ITEMS, parameters );
    }
  }

  private static boolean hasShowListener( Menu menu ) {
    boolean result = false;
    if( !isMenuBar( menu ) ) {
      result = isListening( menu, SWT.Show );
      if( !result ) {
        MenuItem[] items = menu.getItems();
        for( int i = 0; !result && i < items.length && !result; i++ ) {
          result = isListening( items[ i ], SWT.Arm );
        }
      }
    }
    return result;
  }

  private static boolean hasHideListener( Menu menu ) {
    return isMenuBar( menu ) ? false : isListening( menu, SWT.Hide );
  }

  private static Rectangle getBounds( Menu menu ) {
    Rectangle result = new Rectangle( 0, 0, 0, 0 );
    Decorations parent = getBoundingShell( menu );
    if( parent != null ) {
      result = parent.getAdapter( IShellAdapter.class ).getMenuBounds();
    }
    return result;
  }

  private static Decorations getBoundingShell( Menu menu ) {
    Decorations result = null;
    if( menu.getParent().getMenuBar() == menu ) {
      result = menu.getParent();
    }
    return result;
  }

  private static boolean isMenuBar( Menu menu ) {
    return ( menu.getStyle() & SWT.BAR ) != 0;
  }

  private static boolean isPopupMenu( Menu menu ) {
    return ( menu.getStyle() & SWT.POP_UP ) != 0;
  }

  private static boolean isDropDownMenu( Menu menu ) {
    return ( menu.getStyle() & SWT.DROP_DOWN ) != 0;
  }

}
