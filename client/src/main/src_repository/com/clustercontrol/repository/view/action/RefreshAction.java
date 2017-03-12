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

package com.clustercontrol.repository.view.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.repository.view.NodeAttributeView;
import com.clustercontrol.repository.view.NodeListView;
import com.clustercontrol.repository.view.NodeScopeView;
import com.clustercontrol.repository.view.ScopeListView;

/**
 * ビューの更新を行うクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class RefreshAction extends AbstractHandler {

	public static final String ID = RefreshAction.class.getName();

	//	 ----- instance フィールド ----- //

	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
	}

	/**
	 * @see org.eclipse.core.commands.IHandler#execute
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.viewPart = HandlerUtil.getActivePart(event);

		if (this.viewPart instanceof NodeListView) {
			((NodeListView) this.viewPart).update();
		} else if (this.viewPart instanceof NodeAttributeView) {
			((NodeAttributeView) this.viewPart).update();
		} else if (this.viewPart instanceof NodeScopeView) {
			((NodeScopeView) this.viewPart).update();
		} else if (this.viewPart instanceof ScopeListView) {
			((ScopeListView) this.viewPart).update();
		}
		return null;
	}
}
