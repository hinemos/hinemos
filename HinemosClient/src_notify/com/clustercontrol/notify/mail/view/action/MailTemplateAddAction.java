/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.mail.view.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.notify.mail.dialog.MailTemplateCreateDialog;
import com.clustercontrol.notify.mail.view.MailTemplateListView;
import com.clustercontrol.util.RestConnectManager;

/**
 * メールテンプレート[一覧]ビューの作成アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 4.0.0
 */
public class MailTemplateAddAction extends AbstractHandler {
	/** ログ */
	private static Log m_log = LogFactory.getLog(MailTemplateAddAction.class);

	/** アクションID */
	public static final String ID = MailTemplateAddAction.class.getName();

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
	 * メールテンプレート作成ダイアログを表示します
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.viewPart = HandlerUtil.getActivePart(event);
		String managerName = RestConnectManager.getActiveManagerNameList().get(0);
		MailTemplateCreateDialog dialog = new MailTemplateCreateDialog(this.viewPart.getSite().getShell(),
				managerName, null, PropertyDefineConstant.MODE_ADD);
		dialog.open();
		// ビューの更新
		MailTemplateListView view = null;
		try {	
			view = (MailTemplateListView) this.viewPart.getAdapter(MailTemplateListView.class);
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
