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
package org.eclipse.swt.widgets;

import org.eclipse.rap.rwt.internal.lifecycle.ProcessActionRunner;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.SerializableCompatibility;
import org.eclipse.swt.internal.widgets.ControlHolder;
import org.eclipse.swt.internal.widgets.ICompositeAdapter;
import org.eclipse.swt.internal.widgets.IControlHolderAdapter;


/**
 * Instances of this class are controls which are capable
 * of containing other controls.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>NO_FOCUS, NO_RADIO_GROUP</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * This class may be subclassed by custom control implementors
 * who are building controls that are constructed from aggregates
 * of other controls.
 * </p>
 */
public class Composite extends Scrollable {

  private final ICompositeAdapter compositeAdapter;
  private Layout layout;
  int layoutCount;
  private final ControlHolder controlHolder;
  private Control[] tabList;
  int backgroundMode;

  Composite( Composite parent ) {
    // prevent instantiation from outside this package
    super( parent );
    controlHolder = new ControlHolder();
    compositeAdapter = new CompositeAdapter();
  }

  /**
   * Constructs a new instance of this class given its parent
   * and a style value describing its behavior and appearance.
   * <p>
   * The style value is either one of the style constants defined in
   * class <code>SWT</code> which is applicable to instances of this
   * class, or must be built by <em>bitwise OR</em>'ing together
   * (that is, using the <code>int</code> "|" operator) two or more
   * of those <code>SWT</code> style constants. The class description
   * lists the style constants that are applicable to the class.
   * Style bits are also inherited from superclasses.
   * </p>
   *
   * @param parent a widget which will be the parent of the new instance (cannot be null)
   * @param style the style of widget to construct
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
   * </ul>
   *
   * @see SWT#NO_FOCUS
   * @see SWT#NO_RADIO_GROUP
   * @see Widget#getStyle
   */
  public Composite( Composite parent, int style ) {
    super( parent, style );
    controlHolder = new ControlHolder();
    compositeAdapter = new CompositeAdapter();
  }

  @Override
  void initState() {
    if( ( style & ( SWT.H_SCROLL | SWT.V_SCROLL ) ) == 0 ) {
      state |= THEME_BACKGROUND;
    }
  }

  /**
   * Returns a (possibly empty) array containing the receiver's children.
   * Children are returned in the order that they are drawn.  The topmost
   * control appears at the beginning of the array.  Subsequent controls
   * draw beneath this control and appear later in the array.
   * <p>
   * Note: This is not the actual structure used by the receiver
   * to maintain its list of children, so modifying the array will
   * not affect the receiver.
   * </p>
   *
   * @return an array of children
   *
   * @see Control#moveAbove
   * @see Control#moveBelow
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public Control[] getChildren() {
    checkWidget();
    return controlHolder.getControls();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getAdapter( Class<T> adapter ) {
    T result;
    if( adapter == IControlHolderAdapter.class ) {
      result = ( T )controlHolder;
    } else if( adapter == ICompositeAdapter.class ) {
      result = ( T )compositeAdapter;
    } else {
      result = super.getAdapter( adapter );
    }
    return result;
  }

  //////////////////
  // Layout methods

  /**
   * Sets the layout which is associated with the receiver to be
   * the argument which may be null.
   *
   * @param layout the receiver's new layout or null
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setLayout( Layout layout ) {
    checkWidget();
    this.layout = layout;
  }

  /**
   * Returns layout which is associated with the receiver, or
   * null if one has not been set.
   *
   * @return the receiver's layout or null
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public Layout getLayout() {
    checkWidget();
    return layout;
  }

  /**
   * If the argument is <code>true</code>, causes subsequent layout
   * operations in the receiver or any of its children to be ignored.
   * No layout of any kind can occur in the receiver or any of its
   * children until the flag is set to false.
   * Layout operations that occurred while the flag was
   * <code>true</code> are remembered and when the flag is set to
   * <code>false</code>, the layout operations are performed in an
   * optimized manner.  Nested calls to this method are stacked.
   *
   * @param defer the new defer state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #layout(boolean)
   * @see #layout(Control[])
   *
   * @since 1.3
   */
  public void setLayoutDeferred( boolean defer ) {
    checkWidget();
    if( !defer ) {
      if( --layoutCount == 0 ) {
        if( ( state & LAYOUT_CHILD ) != 0 || ( state & LAYOUT_NEEDED ) != 0 ) {
          updateLayout( true, true );
        }
      }
    } else {
      layoutCount++;
    }
  }

  /**
   * Returns <code>true</code> if the receiver has deferred
   * the performing of layout, and <code>false</code> otherwise.
   *
   * @return the receiver's deferred layout state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #setLayoutDeferred(boolean)
   * @see #isLayoutDeferred()
   */
  public boolean getLayoutDeferred() {
    checkWidget();
    return layoutCount > 0;
  }

  /**
   * Returns <code>true</code> if the receiver or any ancestor
   * up to and including the receiver's nearest ancestor shell
   * has deferred the performing of layouts.  Otherwise, <code>false</code>
   * is returned.
   *
   * @return the receiver's deferred layout state
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #setLayoutDeferred(boolean)
   * @see #getLayoutDeferred()
   */
  public boolean isLayoutDeferred() {
    checkWidget();
    return findDeferredControl() != null;
  }

  /**
   * If the receiver has a layout, asks the layout to <em>lay out</em>
   * (that is, set the size and location of) the receiver's children.
   * If the receiver does not have a layout, do nothing.
   * <p>
   * This is equivalent to calling <code>layout(true)</code>.
   * </p>
   * <p>
   * Note: Layout is different from painting. If a child is
   * moved or resized such that an area in the parent is
   * exposed, then the parent will paint. If no child is
   * affected, the parent will not paint.
   * </p>
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void layout() {
    checkWidget();
    layout( true );
  }

  /**
   * If the receiver has a layout, asks the layout to <em>lay out</em>
   * (that is, set the size and location of) the receiver's children.
   * If the argument is <code>true</code> the layout must not rely
   * on any information it has cached about the immediate children. If it
   * is <code>false</code> the layout may (potentially) optimize the
   * work it is doing by assuming that none of the receiver's
   * children has changed state since the last layout.
   * If the receiver does not have a layout, do nothing.
   * <p>
   * If a child is resized as a result of a call to layout, the
   * resize event will invoke the layout of the child.  The layout
   * will cascade down through all child widgets in the receiver's widget
   * tree until a child is encountered that does not resize.  Note that
   * a layout due to a resize will not flush any cached information
   * (same as <code>layout(false)</code>).
   * </p>
   * <p>
   * Note: Layout is different from painting. If a child is
   * moved or resized such that an area in the parent is
   * exposed, then the parent will paint. If no child is
   * affected, the parent will not paint.
   * </p>
   *
   * @param changed <code>true</code> if the layout must flush its caches, and <code>false</code> otherwise
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void layout( boolean changed ) {
    checkWidget();
    if( layout != null ) {
      layout( changed, false );
    }
  }

  /**
   * If the receiver has a layout, asks the layout to <em>lay out</em>
   * (that is, set the size and location of) the receiver's children.
   * If the changed argument is <code>true</code> the layout must not rely
   * on any information it has cached about its children. If it
   * is <code>false</code> the layout may (potentially) optimize the
   * work it is doing by assuming that none of the receiver's
   * children has changed state since the last layout.
   * If the all argument is <code>true</code> the layout will cascade down
   * through all child widgets in the receiver's widget tree, regardless of
   * whether the child has changed size.  The changed argument is applied to
   * all layouts.  If the all argument is <code>false</code>, the layout will
   * <em>not</em> cascade down through all child widgets in the receiver's widget
   * tree.  However, if a child is resized as a result of a call to layout, the
   * resize event will invoke the layout of the child.  Note that
   * a layout due to a resize will not flush any cached information
   * (same as <code>layout(false)</code>).
   * </p>
   * <p>
   * Note: Layout is different from painting. If a child is
   * moved or resized such that an area in the parent is
   * exposed, then the parent will paint. If no child is
   * affected, the parent will not paint.
   * </p>
   *
   * @param changed <code>true</code> if the layout must flush its caches, and <code>false</code> otherwise
   * @param all <code>true</code> if all children in the receiver's widget tree should be laid out, and <code>false</code> otherwise
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void layout( boolean changed, boolean all ) {
    checkWidget();
    if( layout != null || all ) {
      markLayout( changed, all );
      updateLayout( true, all );
    }
  }

  /**
   * Forces a lay out (that is, sets the size and location) of all widgets that
   * are in the parent hierarchy of the changed control up to and including the
   * receiver.  The layouts in the hierarchy must not rely on any information
   * cached about the changed control or any of its ancestors.  The layout may
   * (potentially) optimize the work it is doing by assuming that none of the
   * peers of the changed control have changed state since the last layout.
   * If an ancestor does not have a layout, skip it.
   * <p>
   * Note: Layout is different from painting. If a child is
   * moved or resized such that an area in the parent is
   * exposed, then the parent will paint. If no child is
   * affected, the parent will not paint.
   * </p>
   *
   * @param changed a control that has had a state change which requires a recalculation of its size
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the changed array is null any of its controls are null or have been disposed</li>
   *    <li>ERROR_INVALID_PARENT - if any control in changed is not in the widget tree of the receiver</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void layout( Control[] changed ) {
    checkWidget();
    if( changed == null ) {
      error( SWT.ERROR_INVALID_ARGUMENT );
    }
    layout( changed, SWT.NONE );
  }

  /**
   * Forces a lay out (that is, sets the size and location) of all widgets that
   * are in the parent hierarchy of the changed control up to and including the
   * receiver.
   * <p>
   * The parameter <code>flags</code> may be a combination of:
   * <dl>
   * <dt><b>SWT.ALL</b></dt>
   * <dd>all children in the receiver's widget tree should be laid out</dd>
   * <dt><b>SWT.CHANGED</b></dt>
   * <dd>the layout must flush its caches</dd>
   * <dt><b>SWT.DEFER</b></dt>
   * <dd>layout will be deferred</dd>
   * </dl>
   * </p>
   * <p>
   * When the <code>changed</code> array is specified, the flags <code>SWT.ALL</code>
   * and <code>SWT.CHANGED</code> have no effect. In this case, the layouts in the
   * hierarchy must not rely on any information cached about the changed control or
   * any of its ancestors.  The layout may (potentially) optimize the
   * work it is doing by assuming that none of the peers of the changed
   * control have changed state since the last layout.
   * If an ancestor does not have a layout, skip it.
   * </p>
   * <p>
   * When the <code>changed</code> array is not specified, the flag <code>SWT.ALL</code>
   * indicates that the whole widget tree should be laid out. And the flag
   * <code>SWT.CHANGED</code> indicates that the layouts should flush any cached
   * information for all controls that are laid out.
   * </p>
   * <p>
   * The <code>SWT.DEFER</code> flag always causes the layout to be deferred by
   * calling <code>Composite.setLayoutDeferred(true)</code> and scheduling a call
   * to <code>Composite.setLayoutDeferred(false)</code>, which will happen when
   * appropriate (usually before the next event is handled). When this flag is set,
   * the application should not call <code>Composite.setLayoutDeferred()</code>.
   * </p>
   * <p>
   * Note: Layout is different from painting. If a child is
   * moved or resized such that an area in the parent is
   * exposed, then the parent will paint. If no child is
   * affected, the parent will not paint.
   * </p>
   *
   * @param changed a control that has had a state change which requires a recalculation of its size
   * @param flags the flags specifying how the layout should happen
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if any of the controls in changed is null or has been disposed</li>
   *    <li>ERROR_INVALID_PARENT - if any control in changed is not in the widget tree of the receiver</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.3
   */
  // [if]: This is a verbatim copy of SWT, thus no reformatting was done.
  public void layout (Control [] changed, int flags) {
    checkWidget ();
    if (changed != null) {
      for (int i=0; i<changed.length; i++) {
        Control control = changed [i];
        if (control == null) {
          error (SWT.ERROR_INVALID_ARGUMENT);
        }
        if (control.isDisposed ()) {
          error (SWT.ERROR_INVALID_ARGUMENT);
        }
        boolean ancestor = false;
        Composite composite = control.parent;
        while (composite != null) {
          ancestor = composite == this;
          if (ancestor) {
            break;
          }
          composite = composite.parent;
        }
        if (!ancestor) {
          error (SWT.ERROR_INVALID_PARENT);
        }
      }
      int updateCount = 0;
      Composite [] update = new Composite [16];
      for (int i=0; i<changed.length; i++) {
        Control child = changed [i];
        Composite composite = child.parent;
        while (child != this) {
          if (composite.layout != null) {
            composite.state |= LAYOUT_NEEDED;
            if (!composite.layout.flushCache (child)) {
              composite.state |= LAYOUT_CHANGED;
            }
          }
          if (updateCount == update.length) {
            Composite [] newUpdate = new Composite [update.length + 16];
            System.arraycopy (update, 0, newUpdate, 0, update.length);
            update = newUpdate;
          }
          child = update [updateCount++] = composite;
          composite = child.parent;
        }
      }
      if ((flags & SWT.DEFER) != 0) {
        setLayoutDeferred (true);
        display.addLayoutDeferred (this);
      }
      for (int i=updateCount-1; i>=0; i--) {
        update [i].updateLayout (false);
      }
    } else {
      if (layout == null && (flags & SWT.ALL) == 0) {
        return;
      }
      markLayout ((flags & SWT.CHANGED) != 0, (flags & SWT.ALL) != 0);
      if ((flags & SWT.DEFER) != 0) {
        setLayoutDeferred (true);
        display.addLayoutDeferred (this);
      }
      updateLayout ((flags & SWT.ALL) != 0);
    }
  }

  @Override
  void markLayout( boolean changed, boolean all ) {
    if( layout != null ) {
      state |= LAYOUT_NEEDED;
      if( changed ) {
        state |= LAYOUT_CHANGED;
      }
    }
    if( all ) {
      Control[] children = controlHolder.getControls();
      for( int i = 0; i < children.length; i++ ) {
        children[ i ].markLayout( changed, all );
      }
    }
  }

  void updateLayout( boolean all ) {
    updateLayout( true, all );
  }

  @Override
  void updateLayout( boolean resize, boolean all ) {
    Composite parent = findDeferredControl();
    if( parent != null ) {
      parent.state |= LAYOUT_CHILD;
      return;
    }
    if( ( state & LAYOUT_NEEDED ) != 0 ) {
      boolean changed = ( state & LAYOUT_CHANGED ) != 0;
      state &= ~( LAYOUT_NEEDED | LAYOUT_CHANGED );
// if (resize) setResizeChildren (false);
      layout.layout( this, changed );
// if (resize) setResizeChildren (true);
    }
    if( all ) {
      state &= ~LAYOUT_CHILD;
      Control[] children = controlHolder.getControls();
      for( int i = 0; i < children.length; i++ ) {
        children[ i ].updateLayout( resize, all );
      }
    }
  }

  Composite findDeferredControl() {
    return layoutCount > 0 ? this : parent.findDeferredControl();
  }

  @Override
  public Point computeSize( int wHint, int hHint, boolean changed ) {
    checkWidget();
    Point size;
    boolean hasChanged = changed;
    if( layout != null ) {
      if( wHint == SWT.DEFAULT || hHint == SWT.DEFAULT ) {
        hasChanged |= ( state & LAYOUT_CHANGED ) != 0;
        state &= ~LAYOUT_CHANGED;
        size = layout.computeSize( this, wHint, hHint, hasChanged );
      } else {
        size = new Point( wHint, hHint );
      }
    } else {
      size = minimumSize( wHint, hHint, hasChanged );
      if( size.x == 0 ) {
        size.x = DEFAULT_WIDTH;
      }
      if( size.y == 0 ) {
        size.y = DEFAULT_HEIGHT;
      }
    }
    if( wHint != SWT.DEFAULT ) {
      size.x = wHint;
    }
    if( hHint != SWT.DEFAULT ) {
      size.y = hHint;
    }
    Rectangle trim = computeTrim( 0, 0, size.x, size.y );
    return new Point( trim.width, trim.height );
  }

  /**
   * Clears any data that has been cached by a Layout for all widgets that
   * are in the parent hierarchy of the changed control up to and including the
   * receiver.  If an ancestor does not have a layout, it is skipped.
   *
   * @param changed an array of controls that changed state and require a recalculation of size
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if the changed array is null any of its controls are null or have been disposed</li>
   *    <li>ERROR_INVALID_PARENT - if any control in changed is not in the widget tree of the receiver</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.0
   */
  public void changed( Control[] changed ) {
    checkWidget();
    if( changed == null ) {
      error( SWT.ERROR_INVALID_ARGUMENT );
    }
    for( int i = 0; i < changed.length; i++ ) {
      Control control = changed[ i ];
      if( control == null ) {
        error( SWT.ERROR_INVALID_ARGUMENT );
      }
      if( control.isDisposed() ) {
        error( SWT.ERROR_INVALID_ARGUMENT );
      }
      boolean ancestor = false;
      Composite composite = control.parent;
      while( composite != null ) {
        ancestor = composite == this;
        if( ancestor ) {
          break;
        }
        composite = composite.parent;
      }
      if( !ancestor ) {
        error( SWT.ERROR_INVALID_PARENT );
      }
      }
    for( int i = 0; i < changed.length; i++ ) {
      Control child = changed[ i ];
      Composite composite = child.parent;
      while( child != this ) {
        if( composite.layout == null || !composite.layout.flushCache( child ) )
        {
          composite.state |= LAYOUT_CHANGED;
        }
        child = composite;
        composite = child.parent;
      }
    }
  }

  /**
   * Returns the receiver's background drawing mode. This
   * will be one of the following constants defined in class
   * <code>SWT</code>:
   * <code>INHERIT_NONE</code>, <code>INHERIT_DEFAULT</code>,
   * <code>INHERTIT_FORCE</code>.
   *
   * @return the background mode
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see SWT
   *
   * @since 1.1
   */
  public int getBackgroundMode() {
    checkWidget();
    return backgroundMode;
  }

  /**
   * Sets the background drawing mode to the argument which should be one of the
   * following constants defined in class <code>SWT</code>:
   * <code>INHERIT_NONE</code>, <code>INHERIT_DEFAULT</code>,
   * <code>INHERIT_FORCE</code>.
   *
   * @param mode the new background mode
   * @exception SWTException
   *                <ul>
   *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
   *                disposed</li>
   *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *                thread that created the receiver</li>
   *                </ul>
   * @see SWT
   * @since 1.1
   */
  public void setBackgroundMode( int mode ) {
    checkWidget();
    backgroundMode = mode;
    Control[] children = controlHolder.getControls();
    for( int i = 0; i < children.length; i++ ) {
      children[ i ].updateBackgroundMode();
    }
  }

  @Override
  void updateBackgroundMode() {
    super.updateBackgroundMode();
    Control[] children = controlHolder.getControls();
    for( int i = 0; i < children.length; i++ ) {
      children[ i ].updateBackgroundMode();
    }
  }

  // ///////////////////
  // setFocus override

  @Override
  public boolean setFocus() {
    checkWidget();
    Control[] children = getChildren();
//     for( int i = 0; i < children.length; i++ ) {
//      Control child = children[ i ];
//      if( child.setRadioFocus() )
//        return true;
//    }
    Control focusedChild = null;
    for( int i = 0; focusedChild == null && i < children.length; i++ ) {
      Control child = children[ i ];
      if( child.setFocus() ) {
        focusedChild = child;
      }
    }
    boolean result = true;
    if( focusedChild == null ) {
      result = super.setFocus();
    }
    return result;
  }

  ////////////
  // Tab Order

  /**
   * Sets the tabbing order for the specified controls to
   * match the order that they occur in the argument list.
   *
   * @param tabList the ordered list of controls representing the tab order or null
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_INVALID_ARGUMENT - if a widget in the tabList is null or has been disposed</li>
   *    <li>ERROR_INVALID_PARENT - if widget in the tabList is not in the same widget tree</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public void setTabList( Control[] tabList ) {
    checkWidget();
    Control[] newList = tabList;
    if( tabList != null ) {
      for( int i = 0; i < tabList.length; i++ ) {
        Control control = tabList[ i ];
        if( control == null ) {
          error( SWT.ERROR_INVALID_ARGUMENT );
        }
        if( control.isDisposed() ) {
          error( SWT.ERROR_INVALID_ARGUMENT );
        }
        if( control.parent != this ) {
          error( SWT.ERROR_INVALID_PARENT );
        }
      }
      newList = new Control[ tabList.length ];
      System.arraycopy( tabList, 0, newList, 0, tabList.length );
    }
    this.tabList = newList;
  }

  /**
   * Gets the (possibly empty) tabbing order for the control.
   *
   * @return tabList the ordered list of controls representing the tab order
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see #setTabList
   */
  // returns only tabGroups
  public Control[] getTabList() {
    checkWidget();
    Control[] result = _getTabList();
    if( result == null ) {
      int count = 0;
      Control[] list = controlHolder.getControls();
      for( int i = 0; i < list.length; i++ ) {
        if( list[ i ].isTabGroup() ) {
          count++;
        }
      }
      result = new Control[ count ];
      int index = 0;
      for( int i = 0; i < list.length; i++ ) {
        if( list[ i ].isTabGroup() ) {
          result[ index++ ] = list[ i ];
        }
      }
    }
    return result;
  }

  // filters disposed controls out
  Control[] _getTabList() {
    if( tabList != null ) {
      int count = 0;
      for( int i = 0; i < tabList.length; i++ ) {
        if( !tabList[ i ].isDisposed() ) {
          count++;
        }
      }
      if( count != tabList.length ) {
        Control[] newList = new Control[ count ];
        int index = 0;
        for( int i = 0; i < tabList.length; i++ ) {
          if( !tabList[ i ].isDisposed() ) {
            newList[ index++ ] = tabList[ i ];
          }
        }
        tabList = newList;
      }
    }
    return tabList;
  }

  @Override
  boolean isTabGroup() {
    return true;
  }

  /////////////////////////////////////
  // Helping method used by computeSize

  Point minimumSize( int wHint, int hHint, boolean changed ) {
    Control[] children = getChildren();
    Rectangle clientArea = getClientArea();
    int width = 0, height = 0;
    for( int i = 0; i < children.length; i++ ) {
      Rectangle rect = children[ i ].getBounds();
      width = Math.max( width, rect.x - clientArea.x + rect.width );
      height = Math.max( height, rect.y - clientArea.y + rect.height );
    }
    return new Point( width, height );
  }

  /////////////////////////////////////////////////
  // Internal methods to maintain the child controls

  @Override
  void releaseChildren() {
    super.releaseChildren();
    Control[] children = controlHolder.getControls();
    for( int i = 0; i < children.length; i++ ) {
      children[ i ].dispose();
    }
  }

  void removeControl( Control control ) {
    if( controlHolder.contains( control ) ) {
      controlHolder.remove( control );
    }
  }

  ////////////////
  // Resize helper

  @Override
  void notifyResize( Point oldSize ) {
    // TODO [rh] revise this: the SWT code (method sendResize) first calls
    //      'super' (fires resize events) and *then* does the layouting
    if( !oldSize.equals( getSize() ) || isLayoutNeeded() ) {
      ProcessActionRunner.add( new Runnable() {
        public void run() {
          if( !isDisposed() && layout != null ) {
            markLayout( false, false );
            updateLayout( false, false );
          }
        }
      } );
    }
    super.notifyResize( oldSize );
  }

  private boolean isLayoutNeeded() {
    return ( state & LAYOUT_NEEDED ) != 0;
  }

  ///////////////////
  // Skinning support

  @Override
  void reskinChildren( int flags ) {
    super.reskinChildren( flags );
    Control[] children = controlHolder.getControls();
    for( int i = 0; i < children.length; i++ ) {
      Control child = children[ i ];
      if( child != null ) {
        child.reskin( flags );
      }
    }
  }

  ////////////////
  // Inner classes

  private final class CompositeAdapter implements ICompositeAdapter, SerializableCompatibility {

    public void markLayoutNeeded() {
      markLayout( false, false );
    }

  }
}