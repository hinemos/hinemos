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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.monitor.action.NotifyTableDefineNoCheckBox;
import com.clustercontrol.notify.composite.NotifyListComposite;
import com.clustercontrol.notify.util.NotifyEndpointWrapper;
import com.clustercontrol.notify.view.NotifyListView;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.notify.InvalidRole_Exception;

/**
 * 通知[一覧]ビューの無効アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 4.0.0
 */
public class NotifyDisableAction extends AbstractHandler implements IElementUpdater {

	// ログ
	private static Log m_log = LogFactory.getLog( NotifyDisableAction.class );

	/** アクションID */
	public static final String ID = NotifyDisableAction.class.getName();

	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
	}

	/**
	 * アクション実行
	 */
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

		Object [] objs = selection.toArray();

		// 1つも選択されていない場合
		if(objs.length == 0){
			MessageDialog.openConfirm(
					null,
					Messages.getString("confirmed"),
					Messages.getString("message.notify.8"));
			return null;
		}

		// 1つ以上選択されている場合
		String notifyId = null;
		String[] args;
		StringBuffer targetList = new StringBuffer();
		StringBuffer successList = new StringBuffer();
		StringBuffer failureList = new StringBuffer();

		Map<String, List<String>> disableMap = new ConcurrentHashMap<String, List<String>>();
		for (int i = 0; i < objs.length; i++) {
			String managerName = (String) ((ArrayList<?>)objs[i]).get(NotifyTableDefineNoCheckBox.MANAGER_NAME);
			disableMap.put(managerName, new ArrayList<String>());
		}
		for (int i = 0; i < objs.length; i++) {
			notifyId = (String) ((ArrayList<?>)objs[i]).get(NotifyTableDefineNoCheckBox.NOTIFY_ID);
			String managerName = (String) ((ArrayList<?>)objs[i]).get(NotifyTableDefineNoCheckBox.MANAGER_NAME);
			disableMap.get(managerName).add(notifyId);
			targetList.append(notifyId + "\n");
		}

		// 実行確認(NG→終了)
		args = new String[]{ targetList.toString() } ;
		if (!MessageDialog.openConfirm(
				null,
				Messages.getString("confirmed"),
				Messages.getString("message.notify.37", args))) {
			return null;
		}

		boolean hasRole = true;
		// 実行
		for(Map.Entry<String, List<String>> map : disableMap.entrySet()) {
			String managerName = map.getKey();
			NotifyEndpointWrapper wrapper = NotifyEndpointWrapper.getWrapper(managerName);
			for(String targetId : map.getValue()) {
				try{
					wrapper.setNotifyStatus(targetId, false);
					successList.append(targetId +"(" + managerName + ")" + "\n");
				} catch (InvalidRole_Exception e) {
					failureList.append(targetId + "\n");
					m_log.warn("run() setNotifyStatus targetId=" + targetId + ", " + e.getMessage(), e);
					hasRole = false;
				}catch (Exception e) {
					failureList.append(targetId + "\n");
					m_log.warn("run() setNotifyStatus targetId=" + targetId + ", " + e.getMessage(), e);
				}
			}
		}

		if (!hasRole) {
			// 権限がない場合にはエラーメッセージを表示する
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		}

		// 成功ダイアログ
		if(successList.length() != 0){
			args = new String[]{ successList.toString() } ;
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.notify.38", args));
		}

		// 失敗ダイアログ
		if(failureList.length() != 0){
			args = new String[]{ failureList.toString() } ;
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.notify.39", args));
		}

		// ビューコンポジット更新
		composite.update();
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
