/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import com.clustercontrol.startup.ui.StartUpPerspective;
import com.clustercontrol.util.Messages;

/**
 * 
 * WorkbenchAdvisorクラスを継承するクラス<BR>
 * RCPのWorkbenchの設定などを行います。
 * 
 * @version 2.0.0
 * @since 1.0.0
 */
public class ClusterControlWorkbenchAdvisor extends WorkbenchAdvisor {

	@Override
	public void initialize( IWorkbenchConfigurer configurer ){
		super.initialize( configurer );

		// Save and restore workbench state ( default Off on RAP )
		configurer.setSaveAndRestore(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.application.WorkbenchAdvisor#getInitialWindowPerspectiveId()
	 */
	@Override
	public String getInitialWindowPerspectiveId() {
		return StartUpPerspective.ID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.application.WorkbenchAdvisor#createWorkbenchWindowAdvisor(org.eclipse.ui.application.IWorkbenchWindowConfigurer)
	 */
	@Override
	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		return new ClusterControlWorkbenchWindowAdvisor(configurer);
	}
	
	@Override
	public boolean preShutdown() {
		if (!ClusterControlPlugin.isRAP() && ClusterControlPlugin.isExitConfirm()) {
			return MessageDialog.openConfirm(Display.getDefault().getActiveShell(), Messages.getString("message"), Messages.getString("leave.richclient.confirm"));
		}
		
		return true;
	}
}
