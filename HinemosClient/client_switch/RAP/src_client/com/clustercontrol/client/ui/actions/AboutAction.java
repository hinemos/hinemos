/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

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
