/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.etc.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * パースペクティブを選択するクライアント側アクションクラス<BR>
 * 
 * @version 5.0.0
 * @since 1.0.0
 */
public class OpenPerspectiveAction extends AbstractHandler{

	private IWorkbenchWindow window;

	/**
	 * Handler execution
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == this.window || !isEnabled() ){
			return null;
		}

		IWorkbenchAction openPerspectiveAction = ActionFactory.OPEN_PERSPECTIVE_DIALOG.create(this.window);
		openPerspectiveAction.run();

		return null;
	}

	/**
	 * Dispose
	 */
	@Override
	public void dispose(){
		this.window = null;
	}

}
