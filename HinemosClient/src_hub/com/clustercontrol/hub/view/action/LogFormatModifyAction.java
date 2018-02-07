/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.view.action;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.hub.dialog.LogFormatDialog;
import com.clustercontrol.hub.view.LogFormatView;
/**
 * ログフォーマットの作成・変更ダイアログによる、ログフォーマット変更を行うクライアント側アクションクラス<BR>
 *
 */
public class LogFormatModifyAction extends AbstractHandler implements IElementUpdater{
	/** ログ */
	private static Log m_log = LogFactory.getLog(LogFormatModifyAction.class);

	/** CommandId */
	public static final String ID = LogFormatModifyAction.class.getName();

	private IWorkbenchWindow window;
	private IWorkbenchPart viewPart;

	/**
	 * Handler execution
	 */
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == this.window || !isEnabled() ){
			return null;
		}

		this.viewPart = HandlerUtil.getActivePart(event);

		// ログ[フォーマット]の一覧より、選択されているフォーマットIDを取得
		LogFormatView view = (LogFormatView) this.viewPart
				.getAdapter(LogFormatView.class);
		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}
		String managerName = view.getSelectedManagerNameList().get(0);
		String id = view.getSelectedIdList().get(0);

		if (id != null) {
			// ダイアログを生成
			LogFormatDialog dialog = new LogFormatDialog(this.viewPart.getSite()
					.getShell(), managerName, id,
					PropertyDefineConstant.MODE_MODIFY);

			// ダイアログにて変更が選択された場合、入力内容をもって更新を行う。
			if (dialog.open() == IDialogConstants.OK_ID) {
				view.update();
			}
		}

		return null;
	}

	/**
	 * Dispose
	 */
	@Override
	public void dispose(){
		this.viewPart = null;
		this.window = null;
	}

	/**
	 * Update handler status
	 */
	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		if( null != window ){
			IWorkbenchPage page = window.getActivePage();
			if( null != page ){
				IWorkbenchPart part = page.getActivePart();

				if( part instanceof LogFormatView  ){
					// Enable button when 1 item is selected
					this.setBaseEnabled( 1 == ((LogFormatView) part).getSelectedNum() );
				}else{
					this.setBaseEnabled( false );
				}
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
