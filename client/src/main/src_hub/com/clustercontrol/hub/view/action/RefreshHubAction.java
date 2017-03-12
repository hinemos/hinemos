/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.hub.view.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.hub.view.TransferView;
import com.clustercontrol.hub.view.LogFormatView;
import com.clustercontrol.hub.view.LogScopeTreeView;

/**
 * ビューの更新を行うクライアント側アクションクラス<BR>
 *
 */
public class RefreshHubAction extends AbstractHandler {

	public static final String ID = RefreshHubAction.class.getName();

	private IWorkbenchWindow window;
	private IWorkbenchPart viewPart;

	/**
	 * Handler execution
	 */
	@Override
	public Object execute(final ExecutionEvent event) {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == this.window || !isEnabled() ){
			return null;
		}

		this.viewPart = HandlerUtil.getActivePart(event);

		//更新
		if (this.viewPart instanceof LogFormatView) {
			((LogFormatView) this.viewPart).update();
		}
		else if (this.viewPart instanceof LogScopeTreeView) {
			((LogScopeTreeView) this.viewPart).update();
		}
		else if (this.viewPart instanceof TransferView) {
			((TransferView) this.viewPart).update();
		} 
//		else if (this.viewPart instanceof LogSearchConditionView){
//			((LogSearchConditionView) this.viewPart).update();
//		}
		return null;
	}

	/**
	 * Dispose
	 */
	@Override
	public void dispose(){
		this.viewPart = null;
		this.window = null;
	}
}
