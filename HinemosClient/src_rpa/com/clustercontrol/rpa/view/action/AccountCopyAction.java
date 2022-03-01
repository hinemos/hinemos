/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.view.action;

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
import com.clustercontrol.rpa.composite.RpaManagementToolAccountListComposite;
import com.clustercontrol.rpa.composite.RpaManagementToolAccountListComposite.RpaManagementToolAccountViewColumn;
import com.clustercontrol.rpa.dialog.RpaManagementToolAccountDialog;
import com.clustercontrol.rpa.view.RpaManagementToolAccountView;

/**
 * RPA設定[RPA管理ツールアカウント]ビューのコピーアクションクラス<BR>
 * 
 */
public class AccountCopyAction extends AbstractHandler implements IElementUpdater{
	
	private IWorkbenchWindow window;
	private IWorkbenchPart viewPart;

	/** ログ */
	private static Log m_log = LogFactory.getLog(AccountCopyAction.class);
	
	/** アクションID */
	public static final String ID = AccountCopyAction.class.getName();
	
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
		
		// テンプレートセット一覧より、選択されているスケジュールIDを取得
		
		RpaManagementToolAccountView view = (RpaManagementToolAccountView) this.viewPart
				.getAdapter(RpaManagementToolAccountView.class);
		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}
		RpaManagementToolAccountListComposite composite = (RpaManagementToolAccountListComposite) view.getListComposite();
		StructuredSelection selection = (StructuredSelection) composite.getTableViewer().getSelection();
		
		RpaManagementToolAccountViewColumn account = (RpaManagementToolAccountViewColumn) selection.getFirstElement();
		
		if(account != null) {
			String managerName = account.getManagerName();
			String rpaScopeId = account.getRpaManagementToolAccount().getRpaScopeId();

			// ダイアログを生成
			RpaManagementToolAccountDialog dialog = new RpaManagementToolAccountDialog(
					this.viewPart.getSite().getShell(), managerName, rpaScopeId, PropertyDefineConstant.MODE_COPY);
			
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

				if( part instanceof RpaManagementToolAccountView ){
					// Enable button when 1 item is selected
					this.setBaseEnabled( 1 == ((RpaManagementToolAccountView) part).getSelectedNum() );
				}else{
					this.setBaseEnabled( false );
				}
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
