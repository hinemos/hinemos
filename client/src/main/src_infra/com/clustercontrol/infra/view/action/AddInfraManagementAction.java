/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.infra.view.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.infra.dialog.InfraManagementDialog;
import com.clustercontrol.infra.view.InfraManagementView;

public class AddInfraManagementAction extends AbstractHandler {
	private static Log m_log = LogFactory.getLog(AddInfraManagementAction.class);
	
	/** アクションID */
	public static final String ID = AddInfraManagementAction.class.getName();

	private IWorkbenchWindow window;
	/** ビュー */
	private IWorkbenchPart viewPart;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		if( null == this.window || !isEnabled() ){
			return null;
		}

		this.viewPart = HandlerUtil.getActivePart(event);
		if(!(viewPart instanceof InfraManagementView)) {
			return null;
		}
		
		InfraManagementView infraManagementView = null;
		try {
			infraManagementView = (InfraManagementView) viewPart.getAdapter(InfraManagementView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
			}

		if(infraManagementView != null){
			// 環境構築ダイアログを開く
			InfraManagementDialog dialog = new InfraManagementDialog(this.viewPart.getSite().getShell());
			dialog.open();

			// ビューの更新
			infraManagementView.update();
		} else {
			m_log.info("execute: view is null");
		}
		return null;
	}

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
		this.window = null;
	}
}
