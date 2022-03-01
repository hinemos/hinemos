/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
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
import org.openapitools.client.model.JobKickResponse;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.jobmanagement.composite.JobKickListComposite;
import com.clustercontrol.jobmanagement.dialog.JobKickDialog;
import com.clustercontrol.jobmanagement.view.JobKickListView;
import com.clustercontrol.util.RestConnectManager;

/**
 * ジョブ[実行契機]ビューの「ファイルチェック作成」のクライアント側アクションクラス<BR>
 *
 * @version 5.0.0
 * @since 4.1.0
 */
public class CreateFileCheckAction extends AbstractHandler {
	/** ログ */
	private static Log m_log = LogFactory.getLog(CreateFileCheckAction.class);
	/** アクションID */
	public static final String ID = CreateFileCheckAction.class.getName();
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
	 * ジョブ[実行契機]ビューの「ファイルチェック作成」が押された場合に、実行契機[ファイルチェック]を作成します。
	 * <p>
	 * <ol>
	 * <li>ジョブ[ファイルチェックの作成・変更]ダイアログを表示します。</li>
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

			String managerName = RestConnectManager.getActiveManagerNameList().get(0);
			
			//ダイアログ表示
			JobKickDialog dialog = new JobKickDialog(HandlerUtil.getActiveWorkbenchWindow( event ).getShell(),
					managerName, null, JobKickResponse.TypeEnum.FILECHECK, PropertyDefineConstant.MODE_ADD);
			dialog.open();
			composite.update();
		}
		return null;
	}
}
