/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmap.view.action;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.clustercontrol.jobmap.view.JobMapHistoryView;

/**
 * ジョブマップ履歴ビューの「更新」のクライアント側アクションクラス<BR>
 * 
 */
public class JobMapHistoryRefreshAction extends BaseAction {
	public static final String ID = ActionIdBase + JobMapHistoryRefreshAction.class.getSimpleName();
	
	/**
	 * ジョブ[履歴]ビューの「更新」が押された場合に、ジョブ[履歴]ビューを更新します。
	 *
	 * @see org.eclipse.core.commands.IHandler#execute
	 * @see com.clustercontrol.jobmanagement.view.JobHistoryView
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		super.execute(event);

		//ジョブ履歴ビュー更新
		JobMapHistoryView view = (JobMapHistoryView) viewPart
				.getAdapter(JobMapHistoryView.class);
		view.update(false);
		return null;
	}
}