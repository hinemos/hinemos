/*******************************************************************************
 * Copyright (c) 2011, 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.widgets;

import org.eclipse.rap.rwt.internal.util.ParamCheck;
import org.eclipse.rap.rwt.internal.widgets.IDialogAdapter;
import org.eclipse.swt.widgets.Dialog;


/**
 * Utility class to work with non-blocking dialogs.
 *
 * @see Dialog
 * @since 2.0
 */
public final class DialogUtil {

  /**
   * Opens the given <code>dialog</code> in a non-blocking way and brings it to the front of the
   * display. If given, the <code>dialogCallback</code> is notified when the dialog is closed.
   * <p>
   * Use this method instead of the <code>open()</code> method from the respective
   * <code>Dialog</code> implementation when running a <em>life cycle</em> without a dedicated UI
   * thread where <code>open()</code> does not block the program execution flow.
   * </p>
   *
   * @param dialog the dialog to open, must not be <code>null</code>.
   * @param dialogCallback the callback to be notified when the dialog was closed or
   *   <code>null</code> if no callback should be notified.
   *
   * @see Dialog
   * @see DialogCallback
   * @see org.eclipse.rap.rwt.application.Application.OperationMode
   */
  public static void open( Dialog dialog, DialogCallback dialogCallback ) {
    ParamCheck.notNull( dialog, "dialog" );
    IDialogAdapter adapter = dialog.getAdapter( IDialogAdapter.class );
    adapter.openNonBlocking( dialogCallback );
  }

  private DialogUtil() {
    // prevent instantiation
  }
}
