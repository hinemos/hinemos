/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.view.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.jobmanagement.dialog.JobLinkSendSettingDialog;
import com.clustercontrol.jobmanagement.view.JobLinkSendSettingListView;
import com.clustercontrol.util.RestConnectManager;

/**
 * ジョブ連携送信設定の新規作成コマンドを実行します。
 *
 */
public class CreateJobLinkSendSettingAction extends AbstractHandler {
	/** ログ */
	private static Log m_log = LogFactory.getLog(CreateJobLinkSendSettingAction.class);
	/** アクションID */
	public static final String ID = CreateJobLinkSendSettingAction.class.getName();
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
	 * ジョブ設定[ジョブ連携送信設定]ビューの「作成」が押された場合に、ジョブ連携送信設定を作成します。
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.jobmanagement.dialog.JobLinkSendSettingDialog
	 * @see com.clustercontrol.jobmanagement.view.JobLinkSendSettingListView
	 * @see com.clustercontrol.jobmanagement.composite.JobLinkSendSettingListComposite
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

		if (!(viewPart instanceof JobLinkSendSettingListView)) {
			return null;
		}

		JobLinkSendSettingListView view = null;
		try {
			view = (JobLinkSendSettingListView) viewPart.getAdapter(JobLinkSendSettingListView.class);
		} catch (Exception e) { 
			m_log.info("execute " + e.getMessage()); 
			return null; 
		}

		if (view == null) {
			m_log.info("execute: view is null"); 
			return null;
		}

		String managerName = RestConnectManager.getActiveManagerNameList().get(0);

		//ダイアログ表示
		JobLinkSendSettingDialog dialog = new JobLinkSendSettingDialog(this.viewPart.getSite().getShell(),
				managerName, null, PropertyDefineConstant.MODE_ADD);
		dialog.open();
		view.update();
		return null;
	}
}
