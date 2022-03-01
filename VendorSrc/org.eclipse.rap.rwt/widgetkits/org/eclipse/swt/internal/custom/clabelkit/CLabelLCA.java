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
package org.eclipse.swt.internal.custom.clabelkit;

import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.hasChanged;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.renderProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;
import static org.eclipse.swt.internal.widgets.MarkupUtil.isMarkupEnabledFor;

import java.io.IOException;

import org.eclipse.rap.rwt.internal.lifecycle.AbstractWidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.ControlLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.internal.theme.ThemeAdapter;
import org.eclipse.rap.rwt.internal.util.MnemonicUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Widget;


public final class CLabelLCA extends AbstractWidgetLCA {

  private static final String TYPE = "rwt.widgets.Label";
  private static final String[] ALLOWED_STYLES = new String[] {
    "SHADOW_IN", "SHADOW_OUT", "SHADOW_NONE", "BORDER"
  };

  private static final String PROP_TEXT = "text";
  private static final String PROP_MNEMONIC_INDEX = "mnemonicIndex";
  private static final String PROP_IMAGE = "image";
  private static final String PROP_ALIGNMENT = "alignment";
  private static final String PROP_LEFT_MARGIN = "leftMargin";
  private static final String PROP_TOP_MARGIN = "topMargin";
  private static final String PROP_RIGHT_MARGIN = "rightMargin";
  private static final String PROP_BOTTOM_MARGIN = "bottomMargin";
  private static final String PROP_MARKUP_ENABLED = "markupEnabled";

  private static final String DEFAULT_ALIGNMENT = "left";

  @Override
  public void preserveValues( Widget widget ) {
    CLabel label = ( CLabel )widget;
    ControlLCAUtil.preserveValues( label );
    WidgetLCAUtil.preserveCustomVariant( label );
    preserveProperty( label, PROP_TEXT, label.getText() );
    preserveProperty( label, PROP_IMAGE, label.getImage() );
    preserveProperty( label, PROP_ALIGNMENT, getAlignment( label ) );
    preserveProperty( label, PROP_LEFT_MARGIN, label.getLeftMargin() );
    preserveProperty( label, PROP_TOP_MARGIN, label.getTopMargin() );
    preserveProperty( label, PROP_RIGHT_MARGIN, label.getRightMargin() );
    preserveProperty( label, PROP_BOTTOM_MARGIN, label.getBottomMargin() );
    WidgetLCAUtil.preserveBackgroundGradient( label );
  }

  @Override
  public void renderInitialization( Widget widget ) throws IOException {
    CLabel clabel = ( CLabel )widget;
    RemoteObject remoteObject = createRemoteObject( clabel, TYPE );
    remoteObject.setHandler( new CLabelOperationHandler( clabel ) );
    remoteObject.set( "parent", getId( clabel.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( clabel, ALLOWED_STYLES ) ) );
    // NOTE : This is consistent with Tree and Table, but might change - See Bug 373764
    remoteObject.set( "appearance", "clabel" );
    renderProperty( clabel, PROP_MARKUP_ENABLED, isMarkupEnabledFor( clabel ), false );
  }

  @Override
  public void renderChanges( Widget widget ) throws IOException {
    CLabel clabel = ( CLabel )widget;
    ControlLCAUtil.renderChanges( clabel );
    WidgetLCAUtil.renderCustomVariant( clabel );
    renderText( clabel );
    renderMnemonicIndex( clabel );
    renderProperty( clabel, PROP_IMAGE, clabel.getImage(), null );
    renderProperty( clabel, PROP_ALIGNMENT, getAlignment( clabel ), DEFAULT_ALIGNMENT );
    renderMargins( clabel );
    WidgetLCAUtil.renderBackgroundGradient( clabel );
  }

  ///////////////////////////////////////////////////
  // Helping methods to render the changed properties

  private static void renderMargins( CLabel clabel ) {
    BoxDimensions padding = getThemeAdapter( clabel ).getPadding( clabel );
    renderProperty( clabel, PROP_LEFT_MARGIN, clabel.getLeftMargin(), padding.left );
    renderProperty( clabel, PROP_TOP_MARGIN, clabel.getTopMargin(), padding.top );
    renderProperty( clabel, PROP_RIGHT_MARGIN, clabel.getRightMargin(), padding.right );
    renderProperty( clabel, PROP_BOTTOM_MARGIN, clabel.getBottomMargin(), padding.bottom );
  }

  //////////////////
  // Helping methods

  private static String getAlignment( CLabel clabel ) {
    int alignment = clabel.getAlignment();
    String result;
    if( ( alignment & SWT.LEFT ) != 0 ) {
      result = "left";
    } else if( ( alignment & SWT.CENTER ) != 0 ) {
      result = "center";
    } else if( ( alignment & SWT.RIGHT ) != 0 ) {
      result = "right";
    } else {
      result = "left";
    }
    return result;
  }

  private static void renderText( CLabel clabel ) {
    String newValue = clabel.getText();
    if( hasChanged( clabel, PROP_TEXT, newValue, null ) ) {
      String text = newValue;
      if( !isMarkupEnabledFor( clabel ) ) {
        text = MnemonicUtil.removeAmpersandControlCharacters( newValue );
      }
      getRemoteObject( clabel ).set( PROP_TEXT, text );
    }
  }

  private static void renderMnemonicIndex( CLabel clabel ) {
    if( !isMarkupEnabledFor( clabel ) ) {
      String text = clabel.getText();
      if( hasChanged( clabel, PROP_TEXT, text, null ) ) {
        int mnemonicIndex = MnemonicUtil.findMnemonicCharacterIndex( text );
        if( mnemonicIndex != -1 ) {
          getRemoteObject( clabel ).set( PROP_MNEMONIC_INDEX, mnemonicIndex );
        }
      }
    }
  }

  private static CLabelThemeAdapter getThemeAdapter( CLabel clabel ) {
    return ( CLabelThemeAdapter )clabel.getAdapter( ThemeAdapter.class );
  }

}
