/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.view.action;

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.infra.action.GetInfraManagementTableDefine;
import com.clustercontrol.infra.dialog.InfraManagementDialog;
import com.clustercontrol.infra.view.InfraManagementView;

public class CopyInfraManagementAction extends AbstractHandler implements IElementUpdater {
	/** ログ */
	private static Log m_log = LogFactory.getLog(CopyInfraManagementAction.class);
	/** アクションID */
	public static final String ID = CopyInfraManagementAction.class.getName();
	/** */
	private IWorkbenchWindow window;
	/** ビュー */
	private IWorkbenchPart viewPart;



	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == this.window || !isEnabled() ){
			return null;
		}

		this.viewPart = HandlerUtil.getActivePart(event);

		InfraManagementView view = null;
		if(viewPart instanceof InfraManagementView){
			view = (InfraManagementView) viewPart;
		}
		
		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		//	viewから選択されている部分を取り出す
		StructuredSelection selection = null;
		if(view.getComposite().getTableViewer().getSelection() instanceof StructuredSelection){
			selection = (StructuredSelection) view.getComposite().getTableViewer().getSelection();
		}

		String managerName = null;
		String managementId = null;
		if(selection != null){
			managerName = (String)((ArrayList<?>)selection.getFirstElement()).get(GetInfraManagementTableDefine.MANAGER_NAME);
			managementId = (String)((ArrayList<?>)selection.getFirstElement()).get(GetInfraManagementTableDefine.MANAGEMENT_ID);
		}

		if(managementId != null){
			// 環境構築[構築・チェックの作成・変更]ダイアログを開く
			InfraManagementDialog dialog = new InfraManagementDialog(this.viewPart.getSite().getShell(), managerName, managementId, PropertyDefineConstant.MODE_COPY);

			// ダイアログにて変更が選択された場合、入力内容をもって更新を行う。
			if (dialog.open() == IDialogConstants.OK_ID) {
				view.update();
			}
		}
		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		boolean enable = false;
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		if( null != window ){
			IWorkbenchPage page = window.getActivePage();
			if( null != page ){
				IWorkbenchPart part = page.getActivePart();


				if(part instanceof InfraManagementView){
					InfraManagementView view = (InfraManagementView) part;
					// Enable button when 1 item is selected
					StructuredSelection selection = null;
					if(view.getComposite().getTableViewer().getSelection() instanceof StructuredSelection){
						selection = (StructuredSelection) view.getComposite().getTableViewer().getSelection();
					}
					if(selection != null && selection.size() == 1){
						enable = true;
					}
				}
				this.setBaseEnabled(enable);
			} else {
				this.setBaseEnabled(false);
			}
		}
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
