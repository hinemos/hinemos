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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import com.clustercontrol.ClusterControlPlugin;

/**
 * Opens an &quot;About Hinemos Client&quot; message dialog.
 */

public class AboutAction extends Action implements IWorkbenchAction {

	private IWorkbenchWindow window;

	public AboutAction(IWorkbenchWindow window) {
		super( "About" );
		setId(this.getClass().getName());
		this.window = window;
	}

	public void run() {
		if( null != this.window ){
			String title = "About";
			String msg = ClusterControlPlugin.getDefault().getBundle().getHeaders().get("Bundle-Description");
			MessageDialog.openInformation(window.getShell(), title, msg);
		}
	}

	@Override
	public void dispose() {
		this.window = null;
	}
}
