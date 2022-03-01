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
package org.eclipse.swt.internal.custom.ctabitemkit;

import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.hasChanged;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;

import java.io.IOException;

import org.eclipse.rap.rwt.internal.util.MnemonicUtil;
import org.eclipse.rap.rwt.internal.lifecycle.AbstractWidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.internal.custom.ICTabFolderAdapter;
import org.eclipse.swt.internal.widgets.IWidgetFontAdapter;
import org.eclipse.swt.widgets.Widget;


public final class CTabItemLCA extends AbstractWidgetLCA {

  private static final String TYPE = "rwt.widgets.CTabItem";
  private static final String[] ALLOWED_STYLES = new String[] { "CLOSE" };

  private static final String PROP_TEXT = "text";
  private static final String PROP_MNEMONIC_INDEX = "mnemonicIndex";
  private static final String PROP_IMAGE = "image";
  private static final String PROP_SHOWING = "showing";
  private static final String PROP_SHOW_CLOSE = "showClose";

  @Override
  public void preserveValues( Widget widget ) {
    CTabItem item = ( CTabItem )widget;
    WidgetLCAUtil.preserveCustomVariant( item );
    WidgetLCAUtil.preserveData( item );
    WidgetLCAUtil.preserveToolTipText( item, item.getToolTipText() );
    WidgetLCAUtil.preserveBounds( item, item.getBounds() );
    WidgetLCAUtil.preserveFont( item, getFont( item ) );
    preserveProperty( item, PROP_TEXT, getText( item ) );
    preserveProperty( item, PROP_IMAGE, getImage( item ) );
    preserveProperty( item, PROP_SHOWING, item.isShowing() );
    preserveProperty( item, PROP_SHOW_CLOSE, item.getShowClose() );
  }

  @Override
  public void readData( Widget widget ) {
  }

  @Override
  public void renderInitialization( Widget widget ) throws IOException {
    CTabItem item = ( CTabItem )widget;
    CTabFolder parent = item.getParent();
    RemoteObject remoteObject = createRemoteObject( item, TYPE );
    remoteObject.set( "parent", getId( parent ) );
    remoteObject.set( "index", parent.indexOf( item ) );
    remoteObject.set( "style", createJsonArray( getStyles( item, ALLOWED_STYLES ) ) );
  }

  @Override
  public void renderChanges( Widget widget ) throws IOException {
    CTabItem item = ( CTabItem )widget;
    WidgetLCAUtil.renderCustomVariant( item );
    WidgetLCAUtil.renderData( item );
    WidgetLCAUtil.renderToolTip( item, item.getToolTipText() );
    WidgetLCAUtil.renderBounds( item, item.getBounds() );
    WidgetLCAUtil.renderFont( item, getFont( item ) );
    renderText( item );
    renderMnemonicIndex( item );
    renderProperty( item, PROP_IMAGE, getImage( item ), null );
    renderProperty( item, PROP_SHOWING, item.isShowing(), true );
    renderProperty( item, PROP_SHOW_CLOSE, item.getShowClose(), false );
  }

  ////////////////////////////////////////////
  // Helping methods to obtain item properties

  private static void renderText( CTabItem item ) {
    String newValue = getText( item );
    if( hasChanged( item, PROP_TEXT, newValue, "" ) ) {
      String text = MnemonicUtil.removeAmpersandControlCharacters( newValue );
      getRemoteObject( item ).set( PROP_TEXT, text );
    }
  }

  private static void renderMnemonicIndex( CTabItem item ) {
    String text = getText( item );
    if( hasChanged( item, PROP_TEXT, text, "" ) ) {
      int mnemonicIndex = MnemonicUtil.findMnemonicCharacterIndex( text );
      if( mnemonicIndex != -1 ) {
        getRemoteObject( item ).set( PROP_MNEMONIC_INDEX, mnemonicIndex );
      }
    }
  }

  private static String getText( CTabItem item ) {
    return getCTabFolderAdapter( item ).getShortenedItemText( item );
  }

  private static Image getImage( CTabItem item ) {
    return getCTabFolderAdapter( item ).showItemImage( item ) ? item.getImage() : null;
  }

  private static Font getFont( CTabItem item ) {
    return item.getAdapter( IWidgetFontAdapter.class ).getUserFont();
  }

  private static ICTabFolderAdapter getCTabFolderAdapter( CTabItem item ) {
    return item.getParent().getAdapter( ICTabFolderAdapter.class );
  }

}
