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
package org.eclipse.rap.rwt.internal.lifecycle;

import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.remote.RemoteObject;


/**
 * This interface represents the life cycle of a request. It is not intended to be implemented by
 * clients.
 * <p>
 * RWT divides the life cycle of an HTTP request into different phases that are executed
 * sequentially. Each phase has its special purpose and creates the prerequisites needed by the
 * following phases for proper execution.
 * </p>
 * <p>
 * The phases are:
 * <dl>
 * <dt>Prepare UI Root</dt>
 * <dd>Responsible for invoking entry points.</dd>
 * <dt>Read Data</dt>
 * <dd>Reading request parameters and applying the contained status information to the corresponding
 * widgets. As an example, if a user has entered some characters into a Text control, the characters
 * are transmitted and applied to the text attribute of the Text instance.</dd>
 * <dt>Process Action</dt>
 * <dd>Events are processed which trigger user actions. As an example, when a Button has been
 * pushed, the SelectionListeners attached to the Button are called.</dd>
 * <dt>Render</dt>
 * <dd>JavaScript code is generated for the response, that applies the state changes to the client.
 * Only those widget attributes that were changed during the processing of the current request are
 * being rendered. This results in a minimal amount of data that needs to be transferred to the
 * client. The widget tree is not manipulated in this phase anymore.</dd>
 * </dl>
 * </p>
 *
 * @since 2.0
 * @deprecated As of 2.0, PhaseListeners should only be registered in an
 *             {@link ApplicationConfiguration}. For new applications and custom widgets, consider
 *             the (still internal) {@link RemoteObject} API which is going to replace
 *             PhaseListener.
 */
@Deprecated
public interface ILifeCycle {

  /**
   * Registers a <code>PhaseListener</code> with the life cycle.
   *
   * @param listener the listener to be added, must not be <code>null</code>
   */
  void addPhaseListener( PhaseListener listener );

  /**
   * Removes a <code>PhaseListener</code> from the life cycle. Has no effect
   * if an identical listener is not yet registered.
   *
   * @param listener the listener to be removed, must not be <code>null</code>
   */
  void removePhaseListener( PhaseListener listener );
}
