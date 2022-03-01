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
package org.eclipse.swt.internal.widgets.displaykit;

import static org.eclipse.rap.rwt.internal.lifecycle.DisplayUtil.getAdapter;
import static org.eclipse.rap.rwt.internal.lifecycle.DisplayUtil.getId;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getAdapter;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getId;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getLCA;
import static org.eclipse.rap.rwt.internal.protocol.ClientMessageConst.EVENT_RESIZE;
import static org.eclipse.rap.rwt.internal.protocol.ProtocolUtil.handleOperation;
import static org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory.getRemoteObject;

import java.io.IOException;
import java.util.List;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.ExitConfirmation;
import org.eclipse.rap.rwt.internal.lifecycle.ControlLCAUtil;
import org.eclipse.rap.rwt.internal.lifecycle.DisplayLifeCycleAdapter;
import org.eclipse.rap.rwt.internal.lifecycle.DisposedWidgets;
import org.eclipse.rap.rwt.internal.lifecycle.RemoteAdapter;
import org.eclipse.rap.rwt.internal.lifecycle.ReparentedControls;
import org.eclipse.rap.rwt.internal.lifecycle.UITestUtil;
import org.eclipse.rap.rwt.internal.protocol.ClientMessage;
import org.eclipse.rap.rwt.internal.protocol.Operation;
import org.eclipse.rap.rwt.internal.protocol.ProtocolUtil;
import org.eclipse.rap.rwt.internal.protocol.RemoteObjectFactory;
import org.eclipse.rap.rwt.internal.remote.RemoteObjectLifeCycleAdapter;
import org.eclipse.rap.rwt.internal.textsize.MeasurementUtil;
import org.eclipse.rap.rwt.internal.util.ActiveKeysUtil;
import org.eclipse.rap.rwt.remote.OperationHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.widgets.IDisplayAdapter;
import org.eclipse.swt.internal.widgets.WidgetRemoteAdapter;
import org.eclipse.swt.internal.widgets.WidgetTreeVisitor;
import org.eclipse.swt.internal.widgets.WidgetTreeVisitor.AllWidgetTreeVisitor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;


public class DisplayLCA implements DisplayLifeCycleAdapter {

  static final String PROP_FOCUS_CONTROL = "focusControl";
  static final String PROP_EXIT_CONFIRMATION = "exitConfirmation";
  private static final String METHOD_BEEP = "beep";
  private static final String PROP_RESIZE_LISTENER = "listener_Resize";

  @Override
  public void readData( Display display ) {
    handleOperations( display );
    visitWidgets( display );
    DNDSupport.handleOperations();
    RemoteObjectLifeCycleAdapter.readData( ProtocolUtil.getClientMessage() );
  }

  @Override
  public void preserveValues( Display display ) {
    RemoteAdapter adapter = getAdapter( display );
    adapter.preserve( PROP_FOCUS_CONTROL, display.getFocusControl() );
    adapter.preserve( PROP_EXIT_CONFIRMATION, getExitConfirmation() );
    adapter.preserve( PROP_RESIZE_LISTENER, Boolean.valueOf( hasResizeListener( display ) ) );
    ActiveKeysUtil.preserveActiveKeys( display );
    ActiveKeysUtil.preserveCancelKeys( display );
    ActiveKeysUtil.preserveMnemonicActivator( display );
    if( adapter.isInitialized() ) {
      for( Shell shell : getShells( display ) ) {
        WidgetTreeVisitor.accept( shell, new AllWidgetTreeVisitor() {
          @Override
          public boolean doVisit( Widget widget ) {
            getLCA( widget ).preserveValues( widget );
            return true;
          }
        } );
      }
    }
  }

  @Override
  public void render( Display display ) throws IOException {
    renderReparentControls();
    renderDisposeWidgets();
    renderExitConfirmation( display );
    renderEnableUiTests( display );
    renderShells( display );
    renderFocus( display );
    renderBeep( display );
    renderResizeListener( display );
    renderUICallBack( display );
    ActiveKeysUtil.renderActiveKeys( display );
    ActiveKeysUtil.renderCancelKeys( display );
    ActiveKeysUtil.renderMnemonicActivator( display );
    RemoteObjectLifeCycleAdapter.render();
    MeasurementUtil.renderMeasurementItems();
    runRenderRunnables( display );
    markInitialized( display );
  }

  @Override
  public void clearPreserved( Display display ) {
    ( ( WidgetRemoteAdapter )getAdapter( display ) ).clearPreserved();
    for( Shell shell : getShells( display ) ) {
      WidgetTreeVisitor.accept( shell, new AllWidgetTreeVisitor() {
        @Override
        public boolean doVisit( Widget widget ) {
          ( ( WidgetRemoteAdapter )getAdapter( widget ) ).clearPreserved();
          return true;
        }
      } );
    }
  }

  private static void handleOperations( Display display ) {
    ClientMessage clientMessage = ProtocolUtil.getClientMessage();
    List<Operation> operations = clientMessage .getAllOperationsFor( getId( display ) );
    if( !operations.isEmpty() ) {
      OperationHandler handler = new DisplayOperationHandler( display );
      for( Operation operation : operations ) {
        handleOperation( handler, operation );
      }
    }
  }

  private static void visitWidgets( Display display ) {
    WidgetTreeVisitor visitor = new AllWidgetTreeVisitor() {
      @Override
      public boolean doVisit( Widget widget ) {
        getLCA( widget ).readData( widget );
        return true;
      }
    };
    for( Shell shell : getShells( display ) ) {
      WidgetTreeVisitor.accept( shell, visitor );
    }
  }

  private static void renderShells( Display display ) throws IOException {
    RenderVisitor visitor = new RenderVisitor();
    for( Shell shell : getShells( display ) ) {
      WidgetTreeVisitor.accept( shell, visitor );
      visitor.reThrowProblem();
    }
  }

  private static void renderExitConfirmation( Display display ) {
    String exitConfirmation = getExitConfirmation();
    RemoteAdapter adapter = getAdapter( display );
    Object oldExitConfirmation = adapter.getPreserved( PROP_EXIT_CONFIRMATION );
    boolean hasChanged = exitConfirmation == null
                       ? oldExitConfirmation != null
                       : !exitConfirmation.equals( oldExitConfirmation );
    if( hasChanged ) {
      getRemoteObject( display ).set( PROP_EXIT_CONFIRMATION, exitConfirmation );
    }
  }

  private static String getExitConfirmation() {
    ExitConfirmation exitConfirmation = RWT.getClient().getService( ExitConfirmation.class );
    return exitConfirmation == null ? null : exitConfirmation.getMessage();
  }

  private static void renderReparentControls() {
    for( Control control : ReparentedControls.getAll() ) {
      ControlLCAUtil.renderParent( control );
    }
  }

  private static void renderDisposeWidgets() throws IOException {
    for( Widget widget : DisposedWidgets.getAll() ) {
      getLCA( widget ).renderDispose( widget );
    }
  }

  private static void renderFocus( Display display ) {
    if( !display.isDisposed() ) {
      IDisplayAdapter displayAdapter = getDisplayAdapter( display );
      RemoteAdapter widgetAdapter = getAdapter( display );
      Object oldValue = widgetAdapter.getPreserved( PROP_FOCUS_CONTROL );
      if(    !widgetAdapter.isInitialized()
          || oldValue != display.getFocusControl()
          || displayAdapter.isFocusInvalidated() )
      {
        // TODO [rst] Added null check as a NPE occurred in some rare cases
        Control focusControl = display.getFocusControl();
        if( focusControl != null ) {
          getRemoteObject( display ).set( PROP_FOCUS_CONTROL, getId( display.getFocusControl() ) );
        }
      }
    }
  }

  private static void renderBeep( Display display ) {
    IDisplayAdapter displayAdapter = getDisplayAdapter( display );
    if( displayAdapter.isBeepCalled() ) {
      displayAdapter.resetBeep();
      getRemoteObject( display ).call( METHOD_BEEP, null );
    }
  }

  private static void renderResizeListener( Display display ) {
    RemoteAdapter adapter = getAdapter( display );
    Boolean oldValue = ( Boolean )adapter.getPreserved( PROP_RESIZE_LISTENER );
    if( oldValue == null ) {
      oldValue = Boolean.FALSE;
    }
    Boolean newValue = Boolean.valueOf( hasResizeListener( display ) );
    if( !oldValue.equals( newValue ) ) {
      getRemoteObject( display ).listen( EVENT_RESIZE, newValue.booleanValue() );
    }
  }

  private static void renderUICallBack( Display display ) {
    new ServerPushRenderer().render();
  }

  private static void renderEnableUiTests( Display display ) {
    if( UITestUtil.isEnabled() ) {
      if( !getAdapter( display ).isInitialized() ) {
        RemoteObjectFactory.getRemoteObject( display ).set( "enableUiTests", true );
      }
    }
  }

  private static void runRenderRunnables( Display display ) {
    WidgetRemoteAdapter adapter = ( WidgetRemoteAdapter )getAdapter( display );
    for( Runnable runnable : adapter.getRenderRunnables() ) {
      runnable.run();
    }
    adapter.clearRenderRunnables();
  }

  private static void markInitialized( Display display ) {
    ( ( WidgetRemoteAdapter )getAdapter( display ) ).setInitialized( true );
  }

  private static boolean hasResizeListener( Display display ) {
    return getDisplayAdapter( display ).isListening( SWT.Resize );
  }

  private static IDisplayAdapter getDisplayAdapter( Display display ) {
    return display.getAdapter( IDisplayAdapter.class );
  }

  private static Shell[] getShells( Display display ) {
    return getDisplayAdapter( display ).getShells();
  }

  private static final class RenderVisitor extends AllWidgetTreeVisitor {

    private IOException ioProblem;

    @Override
    public boolean doVisit( Widget widget ) {
      ioProblem = null;
      boolean result = true;
      try {
        render( widget );
        runRenderRunnables( widget );
      } catch( IOException ioe ) {
        ioProblem = ioe;
        result = false;
      }
      return result;
    }

    private void reThrowProblem() throws IOException {
      if( ioProblem != null ) {
        throw ioProblem;
      }
    }

    private static void render( Widget widget ) throws IOException {
      getLCA( widget ).render( widget );
    }

    private static void runRenderRunnables( Widget widget ) {
      WidgetRemoteAdapter adapter = ( WidgetRemoteAdapter )getAdapter( widget );
      for( Runnable runnable : adapter.getRenderRunnables() ) {
        runnable.run();
      }
      adapter.clearRenderRunnables();
    }
  }

}
