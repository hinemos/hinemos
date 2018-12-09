/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.view.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.maintenance.dialog.MaintenanceDialog;
import com.clustercontrol.maintenance.view.MaintenanceListView;
import com.clustercontrol.util.EndpointManager;

/**
 * メンテナンス[一覧]ビューの作成アクションクラス<BR>
 *
 * @version 4.0.0
 * @since 4.0.0
 */
public class MaintenanceAddAction extends AbstractHandler {
	/** ログ */
	private static Log m_log = LogFactory.getLog(MaintenanceAddAction.class);
	
	/** アクションID */
	public static final String ID = MaintenanceAddAction.class.getName();

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
	 * メンテナンス一覧ダイアログを表示します
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.viewPart = HandlerUtil.getActivePart(event);

		String managerName = EndpointManager.getActiveManagerNameList().get(0);
		MaintenanceDialog dialog = new MaintenanceDialog(this.viewPart.getSite().getShell(),
				managerName, null, PropertyDefineConstant.MODE_ADD);
		dialog.open();

		// ビューの更新
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

		view.update();
		return null;
	}

}
