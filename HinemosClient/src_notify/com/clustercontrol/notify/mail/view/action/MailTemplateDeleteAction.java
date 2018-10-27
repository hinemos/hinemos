/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.mail.view.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.monitor.action.NotifyTableDefineNoCheckBox;
import com.clustercontrol.notify.mail.action.DeleteMailTemplate;
import com.clustercontrol.notify.mail.action.GetMailTemplateListTableDefine;
import com.clustercontrol.notify.mail.composite.MailTemplateListComposite;
import com.clustercontrol.notify.mail.view.MailTemplateListView;
import com.clustercontrol.util.Messages;

/**
 * メールテンプレート[一覧]ビューの削除アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 4.0.0
 */
public class MailTemplateDeleteAction extends AbstractHandler implements IElementUpdater {
	/** ログ */
	private static Log m_log = LogFactory.getLog(MailTemplateDeleteAction.class);
	
	/** アクションID */
	public static final String ID = MailTemplateDeleteAction.class.getName();

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
		StructuredSelection selection = (StructuredSelection) composite.getTableViewer().getSelection();

		List<?> list = (List<?>) selection.toList();

		Map<String, List<String>> deleteMap = new ConcurrentHashMap<String, List<String>>();
		int size = 0;
		StringBuffer buf = new StringBuffer();
		if(list != null && list.size() > 0){
			for (Object obj : list) {
				List<?> objList = (List<?>)obj;
				String managerName = (String) objList.get(GetMailTemplateListTableDefine.MANAGER_NAME);
				if(deleteMap.get(managerName) != null) {
					continue;
				}
				deleteMap.put(managerName, new ArrayList<String>());
			}
			for (Object obj : list) {
				List<?> objList = (List<?>)obj;
				String mailTemplateId = (String) objList.get(NotifyTableDefineNoCheckBox.NOTIFY_ID);
				String managerName = (String) objList.get(NotifyTableDefineNoCheckBox.MANAGER_NAME);
				if(size > 0) {
					buf.append(", ");
				}
				buf.append(mailTemplateId);
				deleteMap.get(managerName).add(mailTemplateId);
				size++;
			}
		}

		String[] args = {buf.toString()};

		// 選択アイテムがある場合に、削除処理を呼び出す
		if(size == 0){
			MessageDialog.openWarning(
					null,
					Messages.getString("warning"),
					Messages.getString("message.notify.mail.8"));
			return null;
		}

		if (MessageDialog.openConfirm(
					null,
					Messages.getString("confirmed"),
					Messages.getString("message.notify.mail.7", args)) == false) {
			return null;
		}

		boolean result = false;
		for(Map.Entry<String, List<String>> entry : deleteMap.entrySet()) {
			String managerName = entry.getKey();
			for(String mailTemplateId : entry.getValue()) {
				result = result | new DeleteMailTemplate().delete(managerName, mailTemplateId);
			}
		}

		if(result){
			composite.update();
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

					if(view.getSelectedNum() > 0) {
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
