/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.etc.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.util.RestLoginManager;

/**
 * ログインを行うアクションクラス<BR>
 * 
 * @version 5.0.0
 * @since 2.0.0
 */
public class LoginAction extends AbstractHandler{

	private IWorkbenchWindow window;

	/**
	 * Handler execution
	 * 接続[ログイン]ダイアログを表示し、ログインを行います。
	 */
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == this.window || !isEnabled() ){
			return null;
		}

		RestLoginManager.login(window);

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
