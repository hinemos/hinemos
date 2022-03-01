/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.view.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.rpa.dialog.RpaScenarioDialog;
import com.clustercontrol.rpa.view.RpaScenarioListView;
import com.clustercontrol.util.RestConnectManager;

/**
 * RPAシナリオ実績[シナリオ一覧]ビューの作成アクションクラス
 */
public class RpaScenarioAddAction extends AbstractHandler{

	/** ログ */
	private static Log log = LogFactory.getLog(RpaScenarioAddAction.class);

	/** アクションID */
	public static final String ID = RpaScenarioAddAction.class.getName();

	private IWorkbenchWindow window;
	private IWorkbenchPart viewPart;
	
	/**
	 * Handler execution
	 */
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == this.window || !isEnabled() ){
			return null;
		}

		this.viewPart = HandlerUtil.getActivePart(event);
		RpaScenarioListView view = (RpaScenarioListView) this.viewPart
				.getAdapter(RpaScenarioListView.class);

		if (view == null) {
			log.info("execute: view is null"); 
			return null;
		}
		String managerName = RestConnectManager.getActiveManagerNameList().get(0);

		// ダイアログを生成
		RpaScenarioDialog dialog = new RpaScenarioDialog(this.viewPart.getSite()
				.getShell(), managerName, null, PropertyDefineConstant.MODE_ADD);
		// ダイアログにて変更が選択された場合、入力内容をもって登録を行う。
		dialog.open();

		view.update();

		return null;
	}
	
	/**
	 * Dispose
	 */
	@Override
	public void dispose(){
		this.viewPart = null;
		this.window = null;
	}
	
}
