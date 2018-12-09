/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.maintenance.InvalidRole_Exception;

/**
 * メンテナンス[一覧]ビューの[有効]アクションクラス<BR>
 *
 * @version 4.0.0
 * @since 4.0.0
 */

public class MaintenanceEnableAction extends AbstractHandler implements IElementUpdater {

	// ログ
	private static Log m_log = LogFactory.getLog( MaintenanceEnableAction.class );

	/** アクションID */
	public static final String ID = MaintenanceEnableAction.class.getName();

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
			view = (MaintenanceListView)this.viewPart.getAdapter(MaintenanceListView.class);
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

		Object [] objs = selection.toArray();

		// 1つも選択されていない場合
		if(objs.length == 0){
			MessageDialog.openConfirm(
					null,
					Messages.getString("confirmed"),
					Messages.getString("message.maintenance.9"));
			return null;
		}

		// 1つ以上選択されている場合
		String[] args;
		StringBuffer targetList = new StringBuffer();
		StringBuffer successList = new StringBuffer();
		StringBuffer failureList = new StringBuffer();
		Map<String, List<String>> map = new ConcurrentHashMap<String, List<String>>();
		for (Object o : objs) {
			if (targetList.length() != 0) {
				targetList.append(", ");
			}
			String managerName = (String) ((ArrayList<?>)o).get(GetMaintenanceListTableDefine.MANAGER_NAME);
			if(map.get(managerName) == null) {
				map.put(managerName, new ArrayList<String>());
			}
		}
		for (Object o : objs) {
			if (targetList.length() != 0) {
				targetList.append(", ");
			}
			String managerName = (String) ((ArrayList<?>)o).get(GetMaintenanceListTableDefine.MANAGER_NAME);
			String maintenanceId = (String) ((ArrayList<?>)o).get(GetMaintenanceListTableDefine.MAINTENANCE_ID);
			targetList.append(maintenanceId);
			map.get(managerName).add(maintenanceId);
		}

		// 実行確認(NG→終了)
		args = new String[]{ targetList.toString() } ;
		if (!MessageDialog.openConfirm(
				null,
				Messages.getString("confirmed"),
				Messages.getString("message.maintenance.14", args))) {
			return null;
		}

		boolean hasRole = true;
		// 実行
		for(Map.Entry<String, List<String>> entry : map.entrySet()) {
			String managerName = entry.getKey();
			MaintenanceEndpointWrapper wrapper = MaintenanceEndpointWrapper.getWrapper(managerName);
			for(String maintenanceId : entry.getValue()){
				try{
					wrapper.setMaintenanceStatus(maintenanceId, true);
					successList.append(maintenanceId + "(" + managerName + ")" + "\n");
				} catch (InvalidRole_Exception e) {
					failureList.append(maintenanceId + "\n");
					m_log.warn("run() setMaintenanceStatus maintenanceId=" + maintenanceId + ", " + e.getMessage(), e);
					hasRole = false;
				}catch (Exception e) {
					failureList.append(maintenanceId + "\n");
					m_log.warn("run() setMaintenanceStatus maintenanceId=" + maintenanceId + ", " + e.getMessage(), e);
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
					Messages.getString("message.maintenance.18", args));
		}

		// 失敗ダイアログ
		if(failureList.length() != 0){
			args = new String[]{ failureList.toString() } ;
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.maintenance.19", args));
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
