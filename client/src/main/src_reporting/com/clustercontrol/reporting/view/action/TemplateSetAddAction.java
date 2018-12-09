/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.view.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.reporting.dialog.TemplateSetDialog;
import com.clustercontrol.reporting.view.ReportingTemplateSetView;
import com.clustercontrol.util.EndpointManager;

/**
 * レポーティング[テンプレートセット]ビューの作成アクションクラス<BR>
 * 
 * @version 1.0.0
 * @since 1.0.0
 */
public class TemplateSetAddAction extends AbstractHandler{

	/** ログ */
	private static Log m_log = LogFactory.getLog(TemplateSetAddAction.class);

	/** アクションID */
	public static final String ID = TemplateSetAddAction.class.getName();

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
		ReportingTemplateSetView view = (ReportingTemplateSetView) this.viewPart
				.getAdapter(ReportingTemplateSetView.class);

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}
		String managerName = EndpointManager.getActiveManagerNameList().get(0);

		// ダイアログを生成
		TemplateSetDialog dialog = new TemplateSetDialog(this.viewPart.getSite()
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
