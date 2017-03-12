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
package com.clustercontrol.approval.view.action;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.approval.dialog.ApprovalDetailDialog;
import com.clustercontrol.approval.view.ApprovalView;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.jobmanagement.JobApprovalInfo;

public class ApprovalDetailAction extends AbstractHandler implements IElementUpdater{
	/** ログ */
	private static Log m_log = LogFactory.getLog(ApprovalDetailAction.class);

	/** アクションID */
	public static final String ID = ApprovalDetailAction.class.getName();

	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		ApprovalView view = null;
		try {
			view = (ApprovalView) this.viewPart.getAdapter(ApprovalView.class);
		} catch (Exception e) { 
			m_log.warn("execute " + e.getMessage()); 
			return null; 
		}
		
		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}
		
		//一覧で選択された承認情報を取得
		JobApprovalInfo info = view.getComposite().getSelectedApprovalInfo();
		if(info != null){
			// 承認詳細ダイアログを開く
			ApprovalDetailDialog dialog = new ApprovalDetailDialog(this.viewPart.getSite().getShell(), info);
			dialog.open();
		} else {
			MessageDialog.openInformation(null,
					Messages.getString("failed"),
					Messages.getString("message.job.1"));
		}
		// ビューの更新
		view.update();

		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		if( null != window ){
			IWorkbenchPage page = window.getActivePage();
			if( null != page ){
				IWorkbenchPart part = page.getActivePart();
				
				boolean editEnable = false;
				if(part instanceof ApprovalView){
					// Enable button when 1 item is selected
					ApprovalView view = (ApprovalView)part;
					if(view.getSelectedNum() == 1) {
						editEnable = true;
					}
				}
				this.setBaseEnabled(editEnable);
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
