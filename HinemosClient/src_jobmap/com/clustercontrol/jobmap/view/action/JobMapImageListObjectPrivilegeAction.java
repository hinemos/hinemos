/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.view.action;

import java.util.Map;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.jobmap.view.JobMapImageListView;
import com.clustercontrol.view.action.ObjectPrivilegeAction;

/**
 * オブジェクト権限設定を行うクライアント側アクションクラス<BR>
 *
 * @version 6.0.a
 */
public class JobMapImageListObjectPrivilegeAction extends ObjectPrivilegeAction {
	public static final String ID = BaseAction.ActionIdBase + JobMapImageListObjectPrivilegeAction.class.getSimpleName();

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		if( null != window ){
			IWorkbenchPage page = window.getActivePage();
			if( null != page ){
				IWorkbenchPart part = page.getActivePart();
				if( part instanceof JobMapImageListView  ){
					this.setBaseEnabled( 0 < ((JobMapImageListView) part).getSelectedNum() );
				}else{
					this.setBaseEnabled( false );
				}
			} else {
				this.setBaseEnabled(false);
			}
		}
	}

}
