/**********************************************************************
 * Copyright (C) 2006 NTT DATA Corporation
 * This program is free software; you can redistribute it and/or
 * Modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2.
 * 
 * This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *********************************************************************/

package com.clustercontrol.client.ui.actions;

import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;

/**
 * Middle Class for ActionFactory on RAP
 */

public abstract class ActionFactoryX extends ActionFactory {

	protected ActionFactoryX(String actionId) {
		super(actionId);
	}

	public static final ActionFactory ABOUT = new ActionFactory("about", //$NON-NLS-1$
			IWorkbenchCommandConstants.HELP_ABOUT) {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.actions.ActionFactory#create(org.eclipse.ui.
		 * IWorkbenchWindow)
		 */
		public IWorkbenchAction create(IWorkbenchWindow window) {
			return ActionFactory.ABOUT.create(window);
		}
	};

	/**
	 * @param window
	 * 			the workbench window
	 * @return the workbench action
	 * @see org.eclipse.ui.actions.ActionFactory#create(IWorkbenchWindow window)
	 */
	public abstract IWorkbenchAction create(IWorkbenchWindow window);

}
