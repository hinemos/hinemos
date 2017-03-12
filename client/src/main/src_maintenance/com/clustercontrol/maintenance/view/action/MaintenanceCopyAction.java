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
import com.clustercontrol.maintenance.action.GetMaintenanceListTableDefine;
import com.clustercontrol.maintenance.composite.MaintenanceListComposite;
import com.clustercontrol.maintenance.dialog.MaintenanceDialog;
import com.clustercontrol.maintenance.view.MaintenanceListView;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * メンテナンス[一覧]ビューのコピーアクションクラス<BR>
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class MaintenanceCopyAction extends AbstractHandler implements IElementUpdater {

	// ログ
	private static Log m_log = LogFactory.getLog( MaintenanceCopyAction.class );

	/** アクションID */
	public static final String ID = MaintenanceCopyAction.class.getName();

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

		List<?> list = (List<?>) selection.getFirstElement();
		String maintenanceId = null;
		String managerName = null;
		if(list != null && list.size() > 0){
			managerName = (String) list.get(GetMaintenanceListTableDefine.MANAGER_NAME);
			maintenanceId = (String) list.get(GetMaintenanceListTableDefine.MAINTENANCE_ID);
		}
		Table table = composite.getTableViewer().getTable();
		WidgetTestUtil.setTestId(this, null, table);

		//選択アイテムがある場合に、編集ダイアログを表示する
		if(maintenanceId != null){

			try{
				// ダイアログを生成
				MaintenanceDialog dialog = new MaintenanceDialog(this.viewPart
						.getSite().getShell(), managerName, maintenanceId,
						PropertyDefineConstant.MODE_ADD);
				// MaintenanceSchedule scheduleList = new GetSchedule().getSchedule(maintenanceId);
				// dialog.setSchedule(scheduleList);
				// ダイアログにて変更が選択された場合、入力内容をもって更新を行う。
				if (dialog.open() == IDialogConstants.OK_ID) {
					int selectIndex = table.getSelectionIndex();
					view.update();
					table.setSelection(selectIndex);
				}
			}
			catch (Exception e1) {
				m_log.warn("run(), " + e1.getMessage(), e1);
			}
		}
		else{
			MessageDialog.openWarning(
					null,
					Messages.getString("warning"),
					Messages.getString("message.maintenance.8"));
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
				if(part instanceof MaintenanceListView){
					// Enable button when 1 item is selected
					MaintenanceListView view = (MaintenanceListView)part;

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
