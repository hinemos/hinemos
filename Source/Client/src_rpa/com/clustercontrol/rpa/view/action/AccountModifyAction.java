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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Table;
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
import com.clustercontrol.util.Messages;

/**
 * RPA設定[RPA管理ツールアカウント]ビューの編集アクションクラス<BR>
 * 
 */
public class AccountModifyAction extends AbstractHandler implements IElementUpdater {

	// ログ
	private static Log m_log = LogFactory.getLog(AccountModifyAction.class);

	/** アクションID */
	public static final String ID = AccountModifyAction.class.getName();

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
		RpaManagementToolAccountView view = (RpaManagementToolAccountView) this.viewPart
				.getAdapter(RpaManagementToolAccountView.class);
		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}
		RpaManagementToolAccountListComposite composite = (RpaManagementToolAccountListComposite) view
				.getListComposite();
		StructuredSelection selection = (StructuredSelection) composite
				.getTableViewer().getSelection();

		RpaManagementToolAccountViewColumn data = (RpaManagementToolAccountViewColumn) selection.getFirstElement();

		// 選択アイテムがある場合に、編集ダイアログを表示する
		if (data != null) {
			String managerName = data.getManagerName();
			String rpaScopeId = data.getRpaManagementToolAccount().getRpaScopeId();
			Table table = composite.getTableViewer().getTable();
			try {
				// ダイアログを生成
				RpaManagementToolAccountDialog dialog = new RpaManagementToolAccountDialog(
						this.viewPart.getSite().getShell(), managerName, rpaScopeId,
						PropertyDefineConstant.MODE_MODIFY);

				// ダイアログにて変更が選択された場合、入力内容をもって更新を行う。
				if (dialog.open() == IDialogConstants.OK_ID) {
					int selectIndex = table.getSelectionIndex();
					view.update();
					table.setSelection(selectIndex);
				}
			} catch (Exception e1) {
				m_log.warn("run(), " + e1.getMessage(), e1);
			}

		} else {
			MessageDialog.openWarning(null, 
					Messages.getString("warning"),
					Messages.getString("message.rpa.account.select"));
		}
		return null;
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
