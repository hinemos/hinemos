/*******************************************************************************
 * Copyright (c) 2002, 2011 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Innoopract Informationssysteme GmbH - initial API and implementation
 *     EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.widgets;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;


/**
 * @since 1.4
 */
public class ControlPaintHandler implements ControlListener {

  private final Control ctrl;
  private java.util.List paintListeners;

  public ControlPaintHandler(Control ctrl) {
    this.ctrl = ctrl;
    ctrl.addControlListener(this);
  }

  private void notifyPaintListeners () {
    if ( paintListeners != null ) {
      Event e = new Event();
      e.widget = ctrl;
      e.gc = new GC( ctrl );
      e.height = ctrl.getSize().y;
      e.width = ctrl.getSize().x;
      e.x = ctrl.getLocation().x;
      e.y = ctrl.getLocation().y;
      e.data = ctrl.getData();
      for ( int i = 0; i < paintListeners.size(); i++ ) {
        ( ( PaintListener )paintListeners.get( i ) ).paintControl( new PaintEvent( e ) );
      }
    }
  }
  
  /**
   * Adds the listener to the collection of listeners who will
   * be notified when the receiver needs to be painted, by sending it
   * one of the messages defined in the <code>PaintListener</code>
   * interface.
   *
   * @param listener the listener which should be notified
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see PaintListener
   * @see #removePaintListener
   * 
   * @since 1.4
   */
  public void addPaintListener( final PaintListener listener ) {
    ctrl.getData();  //proxy to checkWidget() since checkWidget is package private.
    if ( listener == null ) ctrl.error( SWT.ERROR_NULL_ARGUMENT );
    if ( paintListeners == null ) { 
      paintListeners = new ArrayList();
    }
    if ( !paintListeners.contains( listener ) ) { 
      paintListeners.add( listener );
    }
  }
  
  /**
   * Removes the listener from the collection of listeners who will
   * be notified when the receiver needs to be painted.
   *
   * @param listener the listener which should no longer be notified
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see PaintListener
   * @see #addPaintListener
   * 
   * @since 1.4
   */
  public void removePaintListener( final PaintListener listener ) {
    ctrl.getData();  //proxy to checkWidget() since checkWidget is package private.
    if ( listener == null ) ctrl.error ( SWT.ERROR_NULL_ARGUMENT );
    if ( paintListeners != null ) {
      paintListeners.remove( listener );
    }
  }

  public void controlMoved(ControlEvent e) {
    notifyPaintListeners();
  }

  public void controlResized(ControlEvent e) {
    notifyPaintListeners();
  }
  
}
