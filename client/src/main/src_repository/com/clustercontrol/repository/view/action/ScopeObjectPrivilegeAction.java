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

import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.accesscontrol.util.ClientSession;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.repository.view.ScopeListView;
import com.clustercontrol.view.action.ObjectPrivilegeAction;

/**
 * スコープのオブジェクト権限設定を行うクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 4.1.0
 */
public class ScopeObjectPrivilegeAction extends ObjectPrivilegeAction {

	public static final String ID = ScopeObjectPrivilegeAction.class.getName();

	/**
	 * Handler execution
	 */
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		super.execute(event);

		// リポジトリキャッシュの更新
		ClientSession.doCheck();

		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		if( null == window ){
			return;
		}

		IWorkbenchPage page = window.getActivePage();
		if( null == page ){
			return;
		}

		boolean editEnable = true;
		IWorkbenchPart part = page.getActivePart();

		if(part instanceof ScopeListView){
			// Enable button when 1 item is selected
			ScopeListView view = (ScopeListView)part;

			if(view.getBuiltin() == false &&
					(view.getScopeTreeComposite().getTree().isFocusControl() ||
							view.getComposite().getTable().isFocusControl())) {

				switch(view.getType()) {
				case FacilityConstant.TYPE_MANAGER:
					editEnable = false;
					break;
				case FacilityConstant.TYPE_SCOPE:
					editEnable = !view.getNotReferFlg();
					break;
				case FacilityConstant.TYPE_NODE:
					editEnable = false;
					break;
				default: // 既定の対処はスルー。
					break;
				}
			} else {
				if(view.getType() == FacilityConstant.TYPE_COMPOSITE) {
					editEnable = false;
				}
			}
		} else {
			editEnable = false;
		}
		this.setBaseEnabled(editEnable);
	}

}
