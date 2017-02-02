/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.notify.view.action;

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
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.monitor.action.NotifyTableDefineNoCheckBox;
import com.clustercontrol.notify.action.DeleteNotify;
import com.clustercontrol.notify.composite.NotifyListComposite;
import com.clustercontrol.notify.view.NotifyListView;
import com.clustercontrol.util.Messages;

/**
 * 通知[一覧]ビューの削除アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 4.0.0
 */
public class NotifyDeleteAction extends AbstractHandler implements IElementUpdater {
	/** ログ */
	private static Log m_log = LogFactory.getLog(NotifyDeleteAction.class);

	/** アクションID */
	public static final String ID = NotifyDeleteAction.class.getName();

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
		NotifyListView view = null;
		try {
			view = (NotifyListView) this.viewPart.getAdapter(NotifyListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		NotifyListComposite composite = (NotifyListComposite) view.getListComposite();
		StructuredSelection selection = (StructuredSelection) composite.getTableViewer().getSelection();
		List<?> list = (List<?>) selection.toList();

		Map<String, List<String>> deleteMap = new ConcurrentHashMap<String, List<String>>();
		int size = 0;
		if(list != null && list.size() > 0){
			for (Object obj : list) {
				List<?> objList = (List<?>)obj;
				String managerName = (String) objList.get(NotifyTableDefineNoCheckBox.MANAGER_NAME);
				if(deleteMap.get(managerName) != null) {
					continue;
				}
				deleteMap.put(managerName, new ArrayList<String>());
			}
			String notifyId = null;
			for (Object obj : list) {
				List<?> objList = (List<?>)obj;
				notifyId = (String) objList.get(NotifyTableDefineNoCheckBox.NOTIFY_ID);
				String managerName = (String) objList.get(NotifyTableDefineNoCheckBox.MANAGER_NAME);
				deleteMap.get(managerName).add(notifyId);
				size++;
			}

			String[] args = new String[1];
			String msg = null;
			if(size > 0) {
				if (size == 1) {
					args[0] = notifyId;
					msg = "message.notify.7";
				} else {
					args[0] = Integer.toString(size);
					msg = "message.notify.51";
				}
			}

			// 選択アイテムがある場合に、削除処理を呼び出す
			DeleteNotify deleteNotify = new DeleteNotify();

			boolean check = true;
			for(Map.Entry<String, List<String>> map : deleteMap.entrySet()) {
				String managerName = map.getKey();
				// 対象の通知IDがどの監視で使用されているかを確認
				if (deleteNotify.useCheck(managerName, map.getValue()) != Window.OK) {
					check = false;
				}
			}
			
			if (check) {
			
				if(MessageDialog.openConfirm(
					null,
					Messages.getString("confirmed"),
					Messages.getString(msg, args)) == false) {
	
					return null;
				}
	
				boolean result = false;
				for(Map.Entry<String, List<String>> map : deleteMap.entrySet()) {
					result = result | deleteNotify.delete(map.getKey(), map.getValue());
				}
				if(result){
					composite.update();
				}
			}
		}
		else{
			MessageDialog.openWarning(
					null,
					Messages.getString("warning"),
					Messages.getString("message.notify.9"));
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
				if(part instanceof NotifyListView){
					// Enable button when 1 item is selected
					NotifyListView view = (NotifyListView)part;

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
