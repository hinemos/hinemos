/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
import org.openapitools.client.model.FacilityInfoResponse.FacilityTypeEnum;

import com.clustercontrol.accesscontrol.util.ClientSession;
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
				case MANAGER:
					editEnable = false;
					break;
				case SCOPE:
					editEnable = !view.getNotReferFlg();
					break;
				case NODE:
					editEnable = false;
					break;
				default: // 既定の対処はスルー。
					break;
				}
			} else {
				if(view.getType() == FacilityTypeEnum.COMPOSITE) {
					editEnable = false;
				}
			}
		} else {
			editEnable = false;
		}
		this.setBaseEnabled(editEnable);
	}

}
