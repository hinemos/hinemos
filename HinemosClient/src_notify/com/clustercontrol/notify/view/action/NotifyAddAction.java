/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.view.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.notify.dialog.NotifyTypeDialog;
import com.clustercontrol.notify.view.NotifyListView;
import com.clustercontrol.util.EndpointManager;

/**
 * 通知[一覧]ビューの作成アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 4.0.0
 */
public class NotifyAddAction extends AbstractHandler {
	/** ログ */
	private static Log m_log = LogFactory.getLog(NotifyAddAction.class);
	
	/** アクションID */
	public static final String ID = NotifyAddAction.class.getName();

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
	 * 通知種別一覧ダイアログを表示します
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		NotifyListView view = null;
		try {
			view = this.viewPart.getAdapter(NotifyListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}	
		if( view != null ){
			String managerName = EndpointManager.getActiveManagerNameList().get(0);
			new NotifyTypeDialog(this.viewPart.getSite().getShell(), ((NotifyListView)view).getListComposite(), managerName)
				.open();
		} else {
			m_log.info("execute: view is null");
		}
		return null;
	}
}
