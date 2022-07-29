/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.view.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.filtersetting.dialog.FilterSettingListDialog;
import com.clustercontrol.filtersetting.dialog.StatusFilterSettingListDialog;
import com.clustercontrol.monitor.view.StatusView;
import com.clustercontrol.util.ViewUtil;

/**
 * ステータス通知結果用のフィルタ設定一覧ダイアログを表示するアクション。
 */
public class StatusFilterSettingAction extends AbstractHandler {
	public static final String ID = StatusFilterSettingAction.class.getName();

	private static Log log = LogFactory.getLog(StatusFilterSettingAction.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// 例外が外へ飛び出して悪さをしないように、ログへ記録した上で収める。
		try {
			return execute0(event);
		} catch (Throwable t) {
			log.warn("execute:", t);
			return null;
		}
	}

	private Object execute0(ExecutionEvent event) {
		ViewUtil.executeWithActive(StatusView.class, view -> {
			// フィルタ設定一覧ダイアログ
			FilterSettingListDialog dlg = new StatusFilterSettingListDialog(
					HandlerUtil.getActiveShell(event).getShell());
			dlg.open();
		});
		return null;
	}
}
