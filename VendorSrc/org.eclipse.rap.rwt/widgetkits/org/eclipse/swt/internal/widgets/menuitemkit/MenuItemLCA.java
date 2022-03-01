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
package org.eclipse.swt.internal.widgets.menuitemkit;

import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveListenHelp;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveListenSelection;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListenHelp;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderListenSelection;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import java.io.IOException;

import org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory;
import org.eclipse.rap.rwt.internal.util.MnemonicUtil;
import org.eclipse.rap.rwt.internal.lifecycle.AbstractWidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;


public final class MenuItemLCA extends AbstractWidgetLCA {

  private static final String TYPE = "rwt.widgets.MenuItem";
  private static final String[] ALLOWED_STYLES = new String[] {
    "CHECK", "CASCADE", "PUSH", "RADIO", "SEPARATOR"
  };

  private static final String PROP_TEXT = "text";
  private static final String PROP_MNEMONIC_INDEX = "mnemonicIndex";
  private static final String PROP_IMAGE = "image";
  private static final String PROP_MENU = "menu";
  private static final String PROP_ENABLED = "enabled";
  private static final String PROP_SELECTION = "selection";

  @Override
  public void preserveValues( Widget widget ) {
    MenuItem item = ( MenuItem )widget;
    WidgetLCAUtil.preserveCustomVariant( item );
    WidgetLCAUtil.preserveData( item );
    preserveProperty( item, PROP_TEXT, item.getText() );
    preserveProperty( item, PROP_IMAGE, item.getImage() );
    preserveProperty( item, PROP_MENU, item.getMenu() );
    preserveProperty( item, PROP_ENABLED, item.getEnabled() );
    preserveProperty( item, PROP_SELECTION, item.getSelection() );
    preserveListenSelection( item );
    preserveListenHelp( item );
  }

  @Override
  public void renderInitialization( Widget widget ) throws IOException {
    MenuItem item = ( MenuItem )widget;
    RemoteObject remoteObject = RemoteObjectFactory.createRemoteObject( item, TYPE );
    remoteObject.setHandler( new MenuItemOperationHandler( item ) );
    Menu parent = item.getParent();
    remoteObject.set( "parent", getId( parent ) );
    remoteObject.set( "style", createJsonArray( getStyles( item, ALLOWED_STYLES ) ) );
    remoteObject.set( "index", parent.indexOf( item ) );
  }

  @Override
  public void renderChanges( Widget widget ) throws IOException {
    MenuItem item = ( MenuItem )widget;
    WidgetLCAUtil.renderCustomVariant( item );
    WidgetLCAUtil.renderData( item );
    renderText( item );
    renderMnemonicIndex( item );
    renderProperty( item, PROP_IMAGE, item.getImage(), null );
    WidgetLCAUtil.renderMenu( item, item.getMenu() );
    renderProperty( item, PROP_ENABLED, item.getEnabled(), true );
    renderProperty( item, PROP_SELECTION, item.getSelection(), false );
    renderListenSelection( item );
    renderListenHelp( item );
  }

  private static void renderText( MenuItem item ) {
    String newValue = item.getText();
    if( WidgetLCAUtil.hasChanged( item, PROP_TEXT, newValue, "" ) ) {
      String text = MnemonicUtil.removeAmpersandControlCharacters( newValue );
      getRemoteObject( item ).set( PROP_TEXT, text );
    }
  }

  private static void renderMnemonicIndex( MenuItem item ) {
    if( ( item.getStyle() & SWT.SEPARATOR ) == 0 ) {
      String text = item.getText();
      if( WidgetLCAUtil.hasChanged( item, PROP_TEXT, text, "" ) ) {
        int mnemonicIndex = MnemonicUtil.findMnemonicCharacterIndex( text );
        if( mnemonicIndex != -1 ) {
          getRemoteObject( item ).set( PROP_MNEMONIC_INDEX, mnemonicIndex );
        }
      }
    }
  }

}
