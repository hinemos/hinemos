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

package com.clustercontrol.maintenance.view.action;

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

import com.clustercontrol.maintenance.action.GetMaintenanceListTableDefine;
import com.clustercontrol.maintenance.composite.MaintenanceListComposite;
import com.clustercontrol.maintenance.util.MaintenanceEndpointWrapper;
import com.clustercontrol.maintenance.view.MaintenanceListView;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.ws.maintenance.InvalidRole_Exception;

/**
 * メンテナンス[一覧]ビューの削除アクションクラス<BR>
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class MaintenanceDeleteAction extends AbstractHandler implements IElementUpdater {

	// ログ
	private static Log m_log = LogFactory.getLog( MaintenanceDeleteAction.class );

	/** アクションID */
	public static final String ID = MaintenanceDeleteAction.class.getName();

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
		MaintenanceListView view = null;
		try {
			view = (MaintenanceListView) this.viewPart.getAdapter(MaintenanceListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		MaintenanceListComposite composite = (MaintenanceListComposite) view.getListComposite();
		StructuredSelection selection = (StructuredSelection) composite.getTableViewer().getSelection();

		List<?> list = (List<?>)selection.toList();

		Map<String, List<String>> map = new ConcurrentHashMap<String, List<String>>();
		for(Object obj : list) {
			List<?> objList = (List<?>)obj;
			String managerName = (String) objList.get(GetMaintenanceListTableDefine.MANAGER_NAME);
			if(map.get(managerName) == null) {
				map.put(managerName, new ArrayList<String>());
			}
		}

		StringBuffer maintenanceId = new StringBuffer();
		for(Object obj : list) {
			List<?> objList = (List<?>)obj;
			String managerName = (String) objList.get(GetMaintenanceListTableDefine.MANAGER_NAME);
			String id = (String) objList.get(GetMaintenanceListTableDefine.MAINTENANCE_ID);
			map.get(managerName).add(id);
			if(maintenanceId.length() > 0) {
				maintenanceId.append(", ");
			}
			maintenanceId.append(id + "(" + managerName + ")");
		}

		// 選択アイテムがある場合に、削除処理を呼び出す
		if(map.isEmpty()){
			MessageDialog.openWarning(
					null,
					Messages.getString("warning"),
					Messages.getString("message.maintenance.9"));
			return null;
		}

		String[] args = { maintenanceId.toString() };

		if (MessageDialog.openConfirm(
				null,
				Messages.getString("confirmed"),
				Messages.getString("message.maintenance.7", args)) == false) {

			return null;
		}

		Map<String, String> errorMsgs = new ConcurrentHashMap<>();
		for(Map.Entry<String, List<String>> entry : map.entrySet()) {
			String managerName = entry.getKey();
			MaintenanceEndpointWrapper wrapper = MaintenanceEndpointWrapper.getWrapper(managerName);
			for(String val : entry.getValue()) {
				try {
					wrapper.deleteMaintenance(val);
				} catch (InvalidRole_Exception e) {
					errorMsgs.put(managerName, Messages.getString("message.accesscontrol.16"));
				} catch (Exception e) {
					m_log.warn("run(), " + e.getMessage(), e);
					errorMsgs.put(managerName, Messages.getString("message.hinemos.failure.unexpected") + HinemosMessage.replace(e.getMessage()));
				}
			}
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		} else {
			MessageDialog.openInformation(
					null,
					Messages.getString("successful"),
					Messages.getString("message.maintenance.5", args));
		}

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
				if(part instanceof MaintenanceListView){
					// Enable button when 1 item is selected
					MaintenanceListView view = (MaintenanceListView)part;

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
