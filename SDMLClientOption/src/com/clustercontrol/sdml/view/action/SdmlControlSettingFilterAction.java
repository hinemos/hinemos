/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.view.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RegistryToggleState;

import com.clustercontrol.bean.Property;
import com.clustercontrol.sdml.dialog.SdmlControlSettingFilterDialog;
import com.clustercontrol.sdml.view.SdmlControlSettingListView;

/**
 * SDML制御設定一覧のフィルタを行うビューアクション
 *
 */
public class SdmlControlSettingFilterAction extends AbstractHandler {
	private static Log logger = LogFactory.getLog(SdmlControlSettingFilterAction.class);

	/** アクションID */
	public static final String ID = SdmlControlSettingFilterAction.class.getName();

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

		boolean isChecked = !HandlerUtil.toggleCommandState(event.getCommand());

		if (isChecked) {
			// ダイアログを生成
			SdmlControlSettingFilterDialog dialog = new SdmlControlSettingFilterDialog(
					HandlerUtil.getActiveWorkbenchWindow(event).getShell());

			// ダイアログにて検索が選択された場合、検索結果をビューに表示
			if (dialog.open() == IDialogConstants.OK_ID) {
				Property condition = dialog.getInputData();
				view.update(condition);
			} else {
				event.getCommand().getState(RegistryToggleState.STATE_ID).setValue(false);
			}
		} else {
			// 検索条件クリア
			view.update(null);
		}
		return null;
	}
}
