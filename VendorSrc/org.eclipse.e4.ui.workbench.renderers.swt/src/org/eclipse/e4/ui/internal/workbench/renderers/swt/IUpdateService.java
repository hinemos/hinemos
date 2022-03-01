/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench.renderers.swt;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;

/**
 * A bridging interface with the 3.x ICommandService for registering element
 * item update callbacks.
 * <p>
 * See bug 366568.
 * </p>
 */
public interface IUpdateService {

	public Runnable registerElementForUpdate(
			ParameterizedCommand parameterizedCommand, MItem item);

}
