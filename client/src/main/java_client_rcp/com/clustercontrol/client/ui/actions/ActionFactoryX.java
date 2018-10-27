/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

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
