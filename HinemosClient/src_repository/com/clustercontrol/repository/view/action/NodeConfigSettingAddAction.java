/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.view.action;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.repository.dialog.NodeConfigSettingCreateDialog;
import com.clustercontrol.repository.view.NodeConfigSettingListView;
import com.clustercontrol.util.RestConnectManager;

/**
 * 構成情報取得設定の作成・変更ダイアログによる、構成情報取得設定を行うクライアント側アクションクラス<BR>
 *
 * @version 6.2.0
 * @since 6.2.0
 */
public class NodeConfigSettingAddAction extends AbstractHandler {
	public static final String ID = NodeConfigSettingAddAction.class.getName();

	//	 ----- instance フィールド ----- //
	/** ログ */
	private static Log m_log = LogFactory.getLog(NodeConfigSettingAddAction.class);
	
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
	 * @see org.eclipse.core.commands.IHandler#execute
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.viewPart = HandlerUtil.getActivePart(event);

		String managerName = RestConnectManager.getActiveManagerNameList().get(0);

		// ダイアログを生成
		NodeConfigSettingCreateDialog dialog = new NodeConfigSettingCreateDialog(this.viewPart.getSite().getShell(), managerName, null, false);

		// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
		if( dialog.open() == IDialogConstants.OK_ID ){
			NodeConfigSettingListView view = null;
			try {
				view = (NodeConfigSettingListView) this.viewPart.getAdapter( NodeConfigSettingListView.class );
			} catch (Exception e) { 
				m_log.info("execute " + e.getMessage()); 
				return null; 
			}
			if (view == null) {
				m_log.info("execute: view is null"); 
				return null;
			}
			view.update();
		}
		return null;
	}
}
