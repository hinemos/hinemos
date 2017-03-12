/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.jobmanagement.view.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.jobmanagement.view.JobListView;

/**
 * ジョブ[一覧]ビューの「ジョブ階層ペインの表示」のクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 1.0.0
 */
public class JobTreeShowAction extends AbstractHandler {
	/** ログ */
	private static Log m_log = LogFactory.getLog(JobTreeShowAction.class);
	/** アクションID */
	public static final String ID = JobTreeShowAction.class.getName();
	private IWorkbenchWindow window;
	/** ビュー */
	private IWorkbenchPart viewPart;

	/**
	 * Dispose
	 */
	@Override
	public void dispose() {
		this.viewPart = null;
		this.window = null;
	}

	/**
	 * ジョブ[一覧]ビューの「ジョブ階層ペインの表示」が押された場合に、<BR>
	 * ジョブ[一覧]ビューのジョブツリーの表示・非表示を切り替えます。
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.jobmanagement.view.RunScheduleListView
	 * @see com.clustercontrol.jobmanagement.view.RunScheduleDetailListView
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		this.window = HandlerUtil.getActiveWorkbenchWindow(event);
		// In case this action has been disposed
		if( null == this.window || !isEnabled() ){
			return null;
		}

		// 選択アイテムの取得
		this.viewPart = HandlerUtil.getActivePart(event);
		// ジョブが正しく選択されていないときに警告を出すためのフラグ

		if (!(viewPart instanceof JobListView)) {
			return null;
		}

		JobListView view = null;
		try {
			view = (JobListView) viewPart.getAdapter(JobListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		ICommandService commandService = (ICommandService)window.getService(ICommandService.class);
		Command command = commandService.getCommand(ID);
		boolean isChecked = !HandlerUtil.toggleCommandState(command);

		if (isChecked) {
			view.show();
		} else {
			view.hide();
		}
		view.setFocus();
		return null;
	}
}
