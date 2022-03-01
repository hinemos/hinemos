/*******************************************************************************
 * Copyright (c) 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.scripting;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rap.rwt.internal.scripting.ClientListenerOperation.AddListener;
import org.eclipse.rap.rwt.internal.scripting.ClientListenerOperation.RemoveListener;
import org.eclipse.rap.rwt.scripting.ClientListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Widget;


public class ClientListenerUtil {

  private static final String OPERATIONS = "rwt.clientListenerOperations";

  public static String getRemoteId( ClientFunction function ) {
    return function.getRemoteId();
  }

  public static String getEventType( int bindingType ) {
    String result = null;
    switch( bindingType ) {
      case SWT.KeyUp:
        result = "KeyUp";
      break;
      case SWT.KeyDown:
        result = "KeyDown";
      break;
      case SWT.FocusIn:
        result = "FocusIn";
      break;
      case SWT.FocusOut:
        result = "FocusOut";
      break;
      case SWT.MouseDown:
        result = "MouseDown";
      break;
      case SWT.MouseUp:
        result = "MouseUp";
      break;
      case SWT.MouseEnter:
        result = "MouseEnter";
      break;
      case SWT.MouseExit:
        result = "MouseExit";
      break;
      case SWT.MouseMove:
        result = "MouseMove";
      break;
      case SWT.MouseDoubleClick:
        result = "MouseDoubleClick";
      break;
      case SWT.Modify:
        result = "Modify";
      break;
      case SWT.Show:
        result = "Show";
      break;
      case SWT.Hide:
        result = "Hide";
      break;
      case SWT.Verify:
        result = "Verify";
      break;
      case SWT.Paint:
        result = "Paint";
      break;
      case SWT.Resize:
        result = "Resize";
      break;
      case SWT.Selection:
        result = "Selection";
      break;
      case SWT.DefaultSelection:
        result = "DefaultSelection";
      break;
      case SWT.MouseWheel:
        result = "MouseWheel";
      break;
    }
    if( result == null ) {
      throw new IllegalArgumentException( "Unsupported event type " + bindingType );
    }
    return result;
  }

  public static void clientListenerAdded( Widget widget, int eventType, ClientListener listener ) {
    List<ClientListenerOperation> operations = getClientListenerOperations( widget );
    if( operations == null ) {
      operations = new ArrayList<ClientListenerOperation>( 1 );
      widget.setData( OPERATIONS, operations );
    }
    operations.add( new AddListener( eventType, listener ) );
  }

  public static void clientListenerRemoved( Widget widget, int eventType, ClientListener listener )
  {
    List<ClientListenerOperation> operations = getClientListenerOperations( widget );
    if( operations == null ) {
      operations = new ArrayList<ClientListenerOperation>( 1 );
      widget.setData( OPERATIONS, operations );
    }
    operations.add( new RemoveListener( eventType, listener ) );
  }

  @SuppressWarnings( "unchecked" )
  public static List<ClientListenerOperation> getClientListenerOperations( Widget widget ) {
    return ( List<ClientListenerOperation> )widget.getData( OPERATIONS );
  }

  public static void clearClientListenerOperations( Widget widget ) {
    widget.setData( OPERATIONS, null );
  }

}
