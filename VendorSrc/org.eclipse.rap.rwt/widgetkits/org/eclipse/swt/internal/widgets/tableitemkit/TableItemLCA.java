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
package org.eclipse.swt.internal.widgets.tableitemkit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;

import java.io.IOException;

import org.eclipse.rap.rwt.internal.lifecycle.AbstractWidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.RemoteAdapter;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.internal.widgets.ITableAdapter;
import org.eclipse.swt.internal.widgets.ITableItemAdapter;
import org.eclipse.swt.internal.widgets.IWidgetColorAdapter;
import org.eclipse.swt.internal.widgets.IWidgetFontAdapter;
import org.eclipse.swt.internal.widgets.WidgetRemoteAdapter;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;


public final class TableItemLCA extends AbstractWidgetLCA {

  private static interface IRenderRunnable {
    void run() throws IOException;
  }

  private static final String TYPE = "rwt.widgets.GridItem";

  static final String PROP_INDEX = "index";
  static final String PROP_TEXTS = "texts";
  static final String PROP_IMAGES = "images";
  static final String PROP_CELL_BACKGROUNDS = "cellBackgrounds";
  static final String PROP_CELL_FOREGROUNDS = "cellForegrounds";
  static final String PROP_CELL_FONTS = "cellFonts";
  static final String PROP_CHECKED = "checked";
  static final String PROP_GRAYED = "grayed";
  static final String PROP_CACHED = "cached";

  @Override
  public void preserveValues( Widget widget ) {
    TableItem item = ( TableItem )widget;
    preserveProperty( item, PROP_INDEX, getIndex( item ) );
    preserveProperty( item, PROP_CACHED, isCached( item ) );
    if( isCached( item ) ) {
      preserveProperty( item, PROP_TEXTS, getTexts( item ) );
      preserveProperty( item, PROP_IMAGES, getImages( item ) );
      WidgetLCAUtil.preserveBackground( item, getUserBackground( item ) );
      WidgetLCAUtil.preserveForeground( item, getUserForeground( item ) );
      WidgetLCAUtil.preserveFont( item, getUserFont( item ) );
      WidgetLCAUtil.preserveCustomVariant( item );
      WidgetLCAUtil.preserveData( item );
      preserveProperty( item, PROP_CELL_BACKGROUNDS, getCellBackgrounds( item ) );
      preserveProperty( item, PROP_CELL_FOREGROUNDS, getCellForegrounds( item ) );
      preserveProperty( item, PROP_CELL_FONTS, getCellFonts( item ) );
      preserveProperty( item, PROP_CHECKED, item.getChecked() );
      preserveProperty( item, PROP_GRAYED, item.getGrayed() );
    }
  }

  @Override
  public void renderInitialization( Widget widget ) throws IOException {
    TableItem item = ( TableItem )widget;
    Table parent = item.getParent();
    RemoteObject remoteObject = createRemoteObject( item, TYPE );
    remoteObject.setHandler( new TableItemOperationHandler( item ) );
    remoteObject.set( "parent", WidgetUtil.getId( parent ) );
  }

  @Override
  public void renderChanges( Widget widget ) throws IOException {
    final TableItem item = ( TableItem )widget;
    renderProperty( item, PROP_INDEX, getIndex( item ), -1 );
    if( wasCleared( item ) ) {
      renderClear( item );
    } else {
      if( isCached( item ) ) {
        preservingInitialized( item, new IRenderRunnable() {
          @Override
          public void run() throws IOException {
            // items that were uncached and are now cached (materialized) are
            // handled as if they were just created (initialized = false)
            if( !wasCached( item ) ) {
              setInitialized( item, false );
            }
            renderProperties( item );
          }
        } );
      }
    }
  }

  ///////////////////////
  // RenderChanges helper

  private static void renderProperties( TableItem item ) {
    renderProperty( item, PROP_TEXTS, getTexts( item ), getDefaultTexts( item ) );
    renderProperty( item, PROP_IMAGES, getImages( item ), new Image[ getColumnCount( item ) ] );
    WidgetLCAUtil.renderBackground( item, getUserBackground( item ) );
    WidgetLCAUtil.renderForeground( item, getUserForeground( item ) );
    WidgetLCAUtil.renderFont( item, getUserFont( item ) );
    WidgetLCAUtil.renderCustomVariant( item );
    WidgetLCAUtil.renderData( item );
    renderProperty( item,
                    PROP_CELL_BACKGROUNDS,
                    getCellBackgrounds( item ),
                    new Color[ getColumnCount( item ) ] );
    renderProperty( item,
                    PROP_CELL_FOREGROUNDS,
                    getCellForegrounds( item ),
                    new Color[ getColumnCount( item ) ] );
    renderProperty( item,
                    PROP_CELL_FONTS,
                    getCellFonts( item ),
                    new Font[ getColumnCount( item ) ] );
    renderProperty( item, PROP_CHECKED, item.getChecked(), false );
    renderProperty( item, PROP_GRAYED, item.getGrayed(), false );
  }

  private static void renderClear( TableItem item ) {
    getRemoteObject( item ).call( "clear", null );
  }

  //////////////////
  // Helping methods

  private static int getIndex( TableItem item ) {
    return item.getParent().indexOf( item );
  }

  private static boolean isCached( TableItem item ) {
    Table table = item.getParent();
    ITableAdapter adapter = table.getAdapter( ITableAdapter.class );
    return !adapter.isItemVirtual( table.indexOf( item ) );
  }

  static String[] getTexts( TableItem item ) {
    int columnCount = getColumnCount( item );
    String[] result = new String[ columnCount ];
    for( int i = 0; i < columnCount; i++ ) {
      result[ i ] = item.getText( i );
    }
    return result;
  }

  private static String[] getDefaultTexts( TableItem item ) {
    String[] result = new String[ getColumnCount( item ) ];
    for( int i = 0; i < result.length; i++ ) {
      result[ i ] = "";
    }
    return result;
  }

  static Image[] getImages( TableItem item ) {
    int columnCount = getColumnCount( item );
    Image[] result = new Image[ columnCount ];
    for( int i = 0; i < columnCount; i++ ) {
      result[ i ] = item.getImage( i );
    }
    return result;
  }

  private static Color getUserBackground( TableItem item ) {
    IWidgetColorAdapter colorAdapter = item.getAdapter( IWidgetColorAdapter.class );
    return colorAdapter.getUserBackground();
  }

  private static Color getUserForeground( TableItem item ) {
    IWidgetColorAdapter colorAdapter = item.getAdapter( IWidgetColorAdapter.class );
    return colorAdapter.getUserForeground();
  }

  private static Font getUserFont( TableItem item ) {
    IWidgetFontAdapter fontAdapter = item.getAdapter( IWidgetFontAdapter.class );
    return fontAdapter.getUserFont();
  }

  private static Color[] getCellBackgrounds( TableItem item ) {
    ITableItemAdapter itemAdapter = item.getAdapter( ITableItemAdapter.class );
    return itemAdapter.getCellBackgrounds();
  }

  private static Color[] getCellForegrounds( TableItem item ) {
    ITableItemAdapter itemAdapter = item.getAdapter( ITableItemAdapter.class );
    return itemAdapter.getCellForegrounds();
  }

  private static Font[] getCellFonts( TableItem item ) {
    ITableItemAdapter itemAdapter = item.getAdapter( ITableItemAdapter.class );
    return itemAdapter.getCellFonts();
  }

  private static int getColumnCount( TableItem item ) {
    return Math.max( 1, item.getParent().getColumnCount() );
  }

  private static boolean wasCleared( TableItem item ) {
    boolean cached = isCached( item );
    boolean wasCached = wasCached( item );
    return !cached && wasCached;
  }

  private static boolean wasCached( TableItem item ) {
    boolean wasCached;
    RemoteAdapter adapter = WidgetUtil.getAdapter( item );
    if( adapter.isInitialized() ) {
      Boolean preserved = ( Boolean )adapter.getPreserved( PROP_CACHED );
      wasCached = Boolean.TRUE.equals( preserved );
    } else {
      wasCached = true;
    }
    return wasCached;
  }

  private static void preservingInitialized( TableItem item, IRenderRunnable runnable )
    throws IOException
  {
    boolean initialized = WidgetUtil.getAdapter( item ).isInitialized();
    runnable.run();
    setInitialized( item, initialized );
  }

  private static void setInitialized( TableItem item, boolean initialized ) {
    WidgetRemoteAdapter adapter = ( WidgetRemoteAdapter )WidgetUtil.getAdapter( item );
    adapter.setInitialized( initialized );
  }

}
