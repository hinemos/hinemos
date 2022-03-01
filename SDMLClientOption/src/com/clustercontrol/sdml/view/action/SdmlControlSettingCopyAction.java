/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.view.action;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.sdml.action.GetSdmlControlSettingListTableDefine;
import com.clustercontrol.sdml.dialog.SdmlControlSettingCreateDialog;
import com.clustercontrol.sdml.view.SdmlControlSettingListView;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * SDML制御設定をコピーするビューアクション
 *
 */
public class SdmlControlSettingCopyAction extends AbstractHandler implements IElementUpdater {
	private static Log logger = LogFactory.getLog(SdmlControlSettingCopyAction.class);

	/** アクションID */
	public static final String ID = SdmlControlSettingCopyAction.class.getName();

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
		List<?> list = (List<?>) selection.getFirstElement();
		if (list == null) {
			return null;
		}
		String managerName = (String) list.get(GetSdmlControlSettingListTableDefine.MANAGER_NAME);
		String applicationId = (String) list.get(GetSdmlControlSettingListTableDefine.APPLICATION_ID);

		if (applicationId != null) {
			// ダイアログを生成
			SdmlControlSettingCreateDialog dialog = new SdmlControlSettingCreateDialog(
					this.viewPart.getSite().getShell(), managerName, applicationId, PropertyDefineConstant.MODE_COPY);

			// ダイアログにて変更が選択された場合、入力内容をもって更新を行う。
			if (dialog.open() == IDialogConstants.OK_ID) {
				view.update();

				// 選択させることで、他のビューの更新を促す。
				CommonTableViewer viewer = view.getComposite().getTableViewer();
				viewer.setSelection(viewer.getSelection());
			}
		}
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

					if (view.getSelectedNum() == 1) {
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
