/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hub.view.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.hub.dialog.LogFormatDialog;
import com.clustercontrol.hub.view.LogFormatView;
import com.clustercontrol.util.RestConnectManager;

/**
 * ログフォーマットの作成・変更ダイアログによる、ログフォーマット登録を行うクライアント側アクションクラス<BR>
 *
 */
public class LogFormatAddAction extends AbstractHandler{
	/** ログ */
	private static Log m_log = LogFactory.getLog(LogFormatAddAction.class);
	public static final String ID = LogFormatAddAction.class.getName();

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
		LogFormatView view = (LogFormatView) this.viewPart
				.getAdapter(LogFormatView.class);

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		String managerName = RestConnectManager.getActiveManagerNameList().get(0);

		// ダイアログを生成
		LogFormatDialog dialog = new LogFormatDialog(this.viewPart.getSite()
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
