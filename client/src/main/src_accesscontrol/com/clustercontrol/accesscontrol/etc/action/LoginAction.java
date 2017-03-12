/**********************************************************************
 * Copyright (C) 2014 NTT DATA Corporation
 * This program is free software; you can redistribute it and/or
 * Modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2.
 * 
 * This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *********************************************************************/

package com.clustercontrol.accesscontrol.etc.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.util.LoginManager;

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

		LoginManager.login( );

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
