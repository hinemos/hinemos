/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.restaccess.view.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.notify.restaccess.dialog.RestAccessInfoCreateDialog;
import com.clustercontrol.notify.restaccess.view.RestAccessInfoListView;
import com.clustercontrol.util.RestConnectManager;

/**
 * RESTアクセス情報[一覧]ビューの作成アクションクラス<BR>
 *
 */
public class RestAccessInfoAddAction extends AbstractHandler {
	/** ログ */
	private static Log m_log = LogFactory.getLog(RestAccessInfoAddAction.class);

	/** アクションID */
	public static final String ID = RestAccessInfoAddAction.class.getName();

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
	 * RESTアクセス情報作成ダイアログを表示します
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.viewPart = HandlerUtil.getActivePart(event);
		String managerName = RestConnectManager.getActiveManagerNameList().get(0);
		RestAccessInfoCreateDialog dialog = new RestAccessInfoCreateDialog(this.viewPart.getSite().getShell(),
				managerName, null, PropertyDefineConstant.MODE_ADD);
		dialog.open();
		// ビューの更新
		RestAccessInfoListView view = null;
		try {	
			view = (RestAccessInfoListView) this.viewPart.getAdapter(RestAccessInfoListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}
		view.update();
		return null;
	}
}
