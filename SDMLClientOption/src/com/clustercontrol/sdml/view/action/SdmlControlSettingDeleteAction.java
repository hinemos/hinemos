/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.view.action;

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

import com.clustercontrol.sdml.action.DeleteSdmlControlSetting;
import com.clustercontrol.sdml.action.GetSdmlControlSettingListTableDefine;
import com.clustercontrol.sdml.view.SdmlControlSettingListView;
import com.clustercontrol.util.Messages;

/**
 * SDML制御設定を削除するビューアクション
 *
 */
public class SdmlControlSettingDeleteAction extends AbstractHandler implements IElementUpdater {
	private static Log logger = LogFactory.getLog(SdmlControlSettingDeleteAction.class);

	/** アクションID */
	public static final String ID = SdmlControlSettingDeleteAction.class.getName();

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
		this.viewPart = HandlerUtil.getActivePart(event);

		SdmlControlSettingListView view = null;
		try {
			view = (SdmlControlSettingListView) this.viewPart.getAdapter(SdmlControlSettingListView.class);
		} catch (Exception e) {
			logger.info("execute() : " + e.getMessage());
			return null;
		}
		if (view == null) {
			logger.info("execute() : view is null");
			return null;
		}

		StructuredSelection selection = (StructuredSelection) view.getComposite().getTableViewer().getSelection();

		List<?> list = (List<?>) selection.toList();
		Map<String, List<String>> map = new ConcurrentHashMap<String, List<String>>();
		// 削除対象のMapを作成
		for (Object obj : list) {
			List<?> objList = (List<?>) obj;
			String managerName = (String) objList.get(GetSdmlControlSettingListTableDefine.MANAGER_NAME);
			if (map.get(managerName) != null) {
				continue;
			}
			map.put(managerName, new ArrayList<String>());
		}
		int size = 0;
		String applicationId = null;
		for (Object obj : list) {
			List<?> objList = (List<?>) obj;
			applicationId = (String) objList.get(GetSdmlControlSettingListTableDefine.APPLICATION_ID);
			String managerName = (String) objList.get(GetSdmlControlSettingListTableDefine.MANAGER_NAME);
			map.get(managerName).add(applicationId);
			size++;
		}

		if (size > 0) {
			String[] args = null;
			String msg = null;
			if (size == 1) {
				args = new String[] { applicationId, Messages.getString("delete") };
				msg = "message.sdml.control.action.confirm";
			} else {
				args = new String[] { Integer.toString(size), Messages.getString("delete") };
				msg = "message.sdml.control.count.confirm";
			}

			if (MessageDialog.openConfirm(null, Messages.getString("confirmed"),
					Messages.getString(msg, args)) == false) {

				return null;
			}
			// 確認ダイアログでOKなら削除処理
			DeleteSdmlControlSetting deleter = new DeleteSdmlControlSetting();
			for (Map.Entry<String, List<String>> entry : map.entrySet()) {
				deleter.delete(entry.getKey(), entry.getValue());
			}
		}
		// ビューを更新
		view.update();
		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		// page may not start at state restoring
		if (null != window) {
			IWorkbenchPage page = window.getActivePage();
			if (null != page) {
				IWorkbenchPart part = page.getActivePart();

				boolean editEnable = false;
				if (part instanceof SdmlControlSettingListView) {
					// Enable button when 1 item is selected
					SdmlControlSettingListView view = (SdmlControlSettingListView) part;

					if (view.getSelectedNum() > 0) {
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
