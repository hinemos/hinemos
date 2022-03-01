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
package org.eclipse.swt.internal.widgets.groupkit;

import static org.eclipse.rap.rwt.internal.protocol.JsonUtil.createJsonArray;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.createRemoteObject;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.getStyles;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.hasChanged;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil.preserveProperty;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;

import java.io.IOException;

import org.eclipse.rap.rwt.internal.util.MnemonicUtil;
import org.eclipse.rap.rwt.internal.lifecycle.AbstractWidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.ControlLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCAUtil;
import org.eclipse.rap.rwt.remote.RemoteObject;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Widget;


public class GroupLCA extends AbstractWidgetLCA {

  private static final String TYPE = "rwt.widgets.Group";
  private static final String[] ALLOWED_STYLES = new String[] {
    "SHADOW_ETCHED_IN",
    "SHADOW_ETCHED_OUT",
    "SHADOW_IN",
    "SHADOW_OUT",
    "SHADOW_NONE",
    "NO_RADIO_GROUP",
    "BORDER"
  };

  private static final String PROP_TEXT = "text";
  private static final String PROP_MNEMONIC_INDEX = "mnemonicIndex";

  @Override
  public void preserveValues( Widget widget ) {
    Group group = ( Group )widget;
    ControlLCAUtil.preserveValues( group );
    WidgetLCAUtil.preserveCustomVariant( group );
    preserveProperty( group, PROP_TEXT, group.getText() );
  }

  @Override
  public void renderInitialization( Widget widget ) throws IOException {
    Group group = ( Group )widget;
    RemoteObject remoteObject = createRemoteObject( group, TYPE );
    remoteObject.setHandler( new GroupOperationHandler( group ) );
    remoteObject.set( "parent", getId( group.getParent() ) );
    remoteObject.set( "style", createJsonArray( getStyles( group, ALLOWED_STYLES ) ) );
  }

  @Override
  public void renderChanges( Widget widget ) throws IOException {
    Group group = ( Group )widget;
    ControlLCAUtil.renderChanges( group );
    WidgetLCAUtil.renderCustomVariant( group );
    renderText( group );
    renderMnemonicIndex( group );
  }

  //////////////////
  // Helping methods

  private static void renderText( Group group ) {
    String newValue = group.getText();
    if( hasChanged( group, PROP_TEXT, newValue, "" ) ) {
      String text = MnemonicUtil.removeAmpersandControlCharacters( newValue );
      getRemoteObject( group ).set( PROP_TEXT, text );
    }
  }

  private static void renderMnemonicIndex( Group group ) {
    String text = group.getText();
    if( hasChanged( group, PROP_TEXT, text, "" ) ) {
      int mnemonicIndex = MnemonicUtil.findMnemonicCharacterIndex( text );
      if( mnemonicIndex != -1 ) {
        getRemoteObject( group ).set( PROP_MNEMONIC_INDEX, mnemonicIndex );
      }
    }
  }

}
