/*

Copyright (C) 2013 NTT DATA Corporation

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
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.jobmanagement.bean.JobKickConstant;
import com.clustercontrol.jobmanagement.composite.JobKickListComposite;
import com.clustercontrol.jobmanagement.dialog.JobKickDialog;
import com.clustercontrol.jobmanagement.view.JobKickListView;
import com.clustercontrol.util.EndpointManager;

/**
 * ジョブ[実行契機]ビューの「実行契機マニュアル」のクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 4.1.0
 */
public class CreateJobManualAction extends AbstractHandler {
	/** ログ */
	private static Log m_log = LogFactory.getLog(CreateJobManualAction.class);
	/** アクションID */
	public static final String ID = CreateJobManualAction.class.getName();
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
	 * ジョブ[実行契機]ビューの「マニュアル実行契機」が押された場合に、実行契機[マニュアル実行契機]を作成します。
	 * <p>
	 * <ol>
	 * <li>ジョブ[マニュアル実行契機の作成・変更]ダイアログを表示します。</li>
	 * <li>ジョブ[実行契機]ビューを更新します。</li>
	 * </ol>
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.jobmanagement.dialog.FileCheckDialog
	 * @see com.clustercontrol.jobmanagement.view.JobKickListView
	 * @see com.clustercontrol.jobmanagement.composite.JobKickListComposite
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

		if (viewPart instanceof JobKickListView) {
			JobKickListView view = null;
			try {
				view = (JobKickListView) viewPart.getAdapter(JobKickListView.class);
			} catch (Exception e) { 
				m_log.info("execute " + e.getMessage()); 
				return null; 
			}

			if (view == null) {
				m_log.info("execute: view is null"); 
				return null;
			}

			JobKickListComposite composite = view.getComposite();

			String managerName = EndpointManager.getActiveManagerNameList().get(0);
			
			//ダイアログ表示
			JobKickDialog dialog = new JobKickDialog(HandlerUtil.getActiveWorkbenchWindow( event ).getShell(),
					managerName, null, JobKickConstant.TYPE_MANUAL, PropertyDefineConstant.MODE_ADD);
			dialog.open();
			composite.update();
		}
		return null;
	}
}
