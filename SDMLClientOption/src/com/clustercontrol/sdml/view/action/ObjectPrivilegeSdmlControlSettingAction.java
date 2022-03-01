/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.view.action;

import java.util.Map;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.sdml.view.SdmlControlSettingListView;
import com.clustercontrol.view.action.ObjectPrivilegeAction;

/**
 * オブジェクト権限設定を行うクライアント側アクションクラス<BR>
 *
 */
public class ObjectPrivilegeSdmlControlSettingAction extends ObjectPrivilegeAction {
	public static final String ID = ObjectPrivilegeSdmlControlSettingAction.class.getName();

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		if (null != window) {
			IWorkbenchPage page = window.getActivePage();
			if (null != page) {
				IWorkbenchPart part = page.getActivePart();

				if (part instanceof SdmlControlSettingListView) {
					this.setBaseEnabled(0 < ((SdmlControlSettingListView) part).getSelectedNum());
				} else {
					this.setBaseEnabled(false);
				}
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
