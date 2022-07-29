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

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.sdml.dialog.SdmlControlSettingCreateDialog;
import com.clustercontrol.sdml.view.SdmlControlSettingListView;
import com.clustercontrol.util.RestConnectManager;

/**
 * SDML制御設定を追加するビューアクション
 *
 */
public class SdmlControlSettingAddAction extends AbstractHandler {
	private static Log logger = LogFactory.getLog(SdmlControlSettingAddAction.class);

	/** アクションID */
	public static final String ID = SdmlControlSettingAddAction.class.getName();

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

		String managerName = RestConnectManager.getActiveManagerNameList().get(0);
		// ダイアログを生成
		SdmlControlSettingCreateDialog dialog = new SdmlControlSettingCreateDialog(this.viewPart.getSite().getShell(),
				managerName, null, PropertyDefineConstant.MODE_ADD);

		// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
		if (dialog.open() == IDialogConstants.OK_ID) {
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
			view.update();
		}
		return null;
	}
}
