/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.view.action;

import java.util.List;
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
import com.clustercontrol.reporting.action.GetTemplateSetListTableDefine;
import com.clustercontrol.reporting.composite.TemplateSetListComposite;
import com.clustercontrol.reporting.dialog.TemplateSetDialog;
import com.clustercontrol.reporting.view.ReportingTemplateSetView;
import com.clustercontrol.reporting.view.action.TemplateSetModifyAction;
import com.clustercontrol.util.Messages;

/**
 * レポーティング[テンプレートセット]ビューの編集アクションクラス<BR>
 * 
 * @version 5.0.a
 * @since 5.0.a
 */
public class TemplateSetModifyAction extends AbstractHandler implements IElementUpdater {

	// ログ
	private static Log m_log = LogFactory.getLog(TemplateSetModifyAction.class);

	/** アクションID */
	public static final String ID = TemplateSetModifyAction.class.getName();

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
		ReportingTemplateSetView view = (ReportingTemplateSetView) this.viewPart
				.getAdapter(ReportingTemplateSetView.class);
		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}
		TemplateSetListComposite composite = (TemplateSetListComposite) view
				.getListComposite();
		StructuredSelection selection = (StructuredSelection) composite
				.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.getFirstElement();
		String managerName = null;
		String templateSetId = null;
		if (list != null && list.size() > 0) {
			managerName = (String) list.get(GetTemplateSetListTableDefine.MANAGER_NAME);
			templateSetId = (String) list.get(GetTemplateSetListTableDefine.TEMPLATE_SET_ID);
		}
		Table table = composite.getTableViewer().getTable();

		// 選択アイテムがある場合に、編集ダイアログを表示する
		if (templateSetId != null) {

			try {
				// ダイアログを生成
				TemplateSetDialog dialog = new TemplateSetDialog(
						this.viewPart.getSite().getShell(), managerName, templateSetId,
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
					Messages.getString("message.reporting.43"));
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

				if( part instanceof ReportingTemplateSetView ){
					// Enable button when 1 item is selected
					this.setBaseEnabled( 1 == ((ReportingTemplateSetView) part).getSelectedNum() );
				}else{
					this.setBaseEnabled( false );
				}
			} else {
				this.setBaseEnabled(false);
			}
		}
	}
}
