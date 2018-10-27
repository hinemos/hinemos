/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.mail.view.action;

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
import com.clustercontrol.notify.mail.action.GetMailTemplateListTableDefine;
import com.clustercontrol.notify.mail.composite.MailTemplateListComposite;
import com.clustercontrol.notify.mail.dialog.MailTemplateCreateDialog;
import com.clustercontrol.notify.mail.view.MailTemplateListView;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * メールテンプレート[一覧]ビューの編集アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 4.0.0
 */
public class MailTemplateCopyAction extends AbstractHandler implements IElementUpdater {
	/** ログ */
	private static Log m_log = LogFactory.getLog(MailTemplateCopyAction.class);
	
	/** アクションID */
	public static final String ID = MailTemplateCopyAction.class.getName();

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
		
		MailTemplateListView view = null;
		try {
			view = (MailTemplateListView) this.viewPart.getAdapter(MailTemplateListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}
		
		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		MailTemplateListComposite composite = (MailTemplateListComposite) view.getListComposite();
		WidgetTestUtil.setTestId(this, null, composite);
		WidgetTestUtil.setTestId(this, null, composite);

		StructuredSelection selection = (StructuredSelection) composite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.getFirstElement();
		String managerName = null;
		String mailTemplateId = null;
		if(list != null && list.size() > 0){
			managerName = (String) list.get(GetMailTemplateListTableDefine.MANAGER_NAME);
			mailTemplateId = (String) list.get(GetMailTemplateListTableDefine.MAIL_TEMPLATE_ID);
		}

		Table table = composite.getTableViewer().getTable();
		WidgetTestUtil.setTestId(this, null, table);

		// 選択アイテムがある場合に、編集ダイアログを表示する
		if(mailTemplateId != null){
			MailTemplateCreateDialog dialog = new MailTemplateCreateDialog(view
					.getListComposite().getShell(), managerName,
					mailTemplateId, PropertyDefineConstant.MODE_ADD);
			if (dialog.open() == IDialogConstants.OK_ID) {
				int selectIndex = table.getSelectionIndex();
				composite.update();
				table.setSelection(selectIndex);
			}
		}
		else{
			MessageDialog.openWarning(
					null,
					Messages.getString("warning"),
					Messages.getString("message.notify.mail.8"));
		}
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
				if(part instanceof MailTemplateListView){
					// Enable button when 1 item is selected
					MailTemplateListView view = (MailTemplateListView)part;

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
