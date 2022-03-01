/*******************************************************************************
 * Copyright (c) 2009, 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TypedListener;


public class ControlDecorator extends Decorator {

  private final Composite parent;
  private Image image;
  private String text;
  private boolean visible;
  private boolean showOnlyOnFocus;
  private boolean showHover;
  private boolean hasFocus;
  private int marginWidth;
  private FocusListener focusListener;

  public ControlDecorator( Control control, int style, Composite composite ) {
    super( control, checkStyle( style ) );
    text = "";
    visible = true;
    showHover = true;
    parent = getParent( control, composite );
    addDisposeListener( new DisposeListener() {
      public void widgetDisposed( DisposeEvent event ) {
        removeFocusListener();
      }
    } );
  }

  public Image getImage() {
    checkWidget();
    return image;
  }

  public void setImage( Image image ) {
    checkWidget();
    this.image = image;
  }

  public String getText() {
    checkWidget();
    return text;
  }

  public void setText( String text ) {
    checkWidget();
    if( text == null ) {
      this.text = "";
    } else {
      this.text = text;
    }
  }

  public boolean getShowOnlyOnFocus() {
    checkWidget();
    return showOnlyOnFocus;
  }

  public void setShowOnlyOnFocus( boolean showOnlyOnFocus ) {
    checkWidget();
    if( this.showOnlyOnFocus != showOnlyOnFocus ) {
      this.showOnlyOnFocus = showOnlyOnFocus;
      if( showOnlyOnFocus ) {
        addFocusListener();
      } else {
        removeFocusListener();
      }
    }
  }

  public boolean getShowHover() {
    checkWidget();
    return showHover;
  }

  public void setShowHover( boolean showHover ) {
    checkWidget();
    this.showHover = showHover;
  }

  public int getMarginWidth() {
    checkWidget();
    return marginWidth;
  }

  public void setMarginWidth( int marginWidth ) {
    checkWidget();
    this.marginWidth = marginWidth;
  }

  public void show() {
    checkWidget();
    visible = true;
  }

  public void hide() {
    checkWidget();
    visible = false;
  }

  public boolean isVisible() {
    checkWidget();
    boolean result = true;
    if( !visible ) {
      result = false;
    }
    Control control = ( Control )getDecoratedWidget();
    if( control == null || control.isDisposed() || image == null ) {
      result = false;
    }
    if( control != null && !control.isVisible() ) {
      result = false;
    }
    if( showOnlyOnFocus ) {
      result = result && hasFocus;
    }
    return result;
  }

  public Rectangle getBounds() {
    checkWidget();
    Rectangle result;
    Control control = ( Control )getDecoratedWidget();
    if( image != null && control != null ) {
      // Compute the bounds first relative to the control's parent.
      Rectangle imageBounds = image.getBounds();
      Rectangle controlBounds = control.getBounds();
      int left;
      if( ( getStyle() & SWT.RIGHT ) == SWT.RIGHT ) {
        left = controlBounds.x + controlBounds.width + marginWidth;
      } else {
        // default is left
        left = controlBounds.x - imageBounds.width - marginWidth;
      }
      int top;
      if( ( getStyle() & SWT.TOP ) == SWT.TOP ) {
        top = controlBounds.y;
      } else if( ( getStyle() & SWT.BOTTOM ) == SWT.BOTTOM ) {
        top = controlBounds.y + controlBounds.height - imageBounds.height;
      } else {
        // default is center
        top
          = controlBounds.y
          + ( controlBounds.height - imageBounds.height ) / 2;
      }
      // Now convert to coordinates relative to the target control.
      Point globalPoint = control.getParent().toDisplay( left, top );
      Point targetPoint = parent.toControl( globalPoint );
      result = new Rectangle( targetPoint.x,
                              targetPoint.y,
                              imageBounds.width,
                              imageBounds.height );
    } else {
      result = new Rectangle( 0, 0, 0, 0 );
    }
    return result;
  }

  public Control getControl() {
    checkWidget();
    return ( Control )getDecoratedWidget();
  }

  public Composite getParent() {
    checkWidget();
    return parent;
  }

  public void addSelectionListener( SelectionListener listener ) {
    checkWidget();
    if( listener == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    TypedListener typedListener = new TypedListener( listener );
    addListener( SWT.Selection, typedListener );
    addListener( SWT.DefaultSelection, typedListener );
  }

  public void removeSelectionListener( SelectionListener listener ) {
    checkWidget();
    if( listener == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    removeListener( SWT.Selection, listener );
    removeListener( SWT.DefaultSelection, listener );
  }

  //////////////////
  // Helping methods

  private static int checkStyle( int style ) {
    int result = SWT.NONE;
    if( ( style & SWT.RIGHT ) != 0 ) {
      result |= SWT.RIGHT;
    } else {
      result |= SWT.LEFT;
    }
    if( ( style & SWT.TOP ) != 0 ) {
      result |= SWT.TOP;
    } else if( ( style & SWT.BOTTOM ) != 0 ) {
      result |= SWT.BOTTOM;
    } else {
      result |= SWT.CENTER;
    }
    return result;
  }

  private static Composite getParent( Control control, Composite composite ) {
    Composite result = composite;
    if( composite == null ) {
      result = control.getParent();
    }
    return result;
  }

  private void addFocusListener() {
    if( focusListener == null ) {
      focusListener = new FocusListener() {
        public void focusGained( FocusEvent event ) {
          hasFocus = true;
        }
        public void focusLost( FocusEvent event ) {
          hasFocus = false;
        }
      };
    }
    Control control = ( Control )getDecoratedWidget();
    if( control != null && !control.isDisposed() ) {
      control.addFocusListener( focusListener );
    }
  }

  private void removeFocusListener() {
    Control control = ( Control )getDecoratedWidget();
    if( focusListener != null && control != null && !control.isDisposed() ) {
      control.removeFocusListener( focusListener );
    }
  }
}
