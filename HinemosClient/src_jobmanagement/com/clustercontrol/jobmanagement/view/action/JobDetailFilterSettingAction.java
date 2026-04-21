/*
 * Copyright (c) 2026 NTT DATA INTELLILINK Corporation.
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
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.clustercontrol.jobmanagement.dialog.JobDetailFilterDialog;
import com.clustercontrol.jobmanagement.view.JobDetailView;

/*
 * ジョブのフィルター設定ダイアログの表示
 */
public class JobDetailFilterSettingAction extends AbstractHandler {
	/* ログ */
	private static Log log = LogFactory.getLog(JobDetailFilterSettingAction.class);
	
	/* アクションID */
	public static final String ID = JobDetailFilterSettingAction.class.getName();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// ジョブ履歴[ジョブ詳細]ビューのイベントか判定
		IWorkbenchPart wp = HandlerUtil.getActivePart(event);
		JobDetailView view = (JobDetailView) wp.getAdapter(JobDetailView.class);
		if (view == null) {
			log.info("The Event did not originate from a JobDetailView."); 
			return null;
		}
		
		// ジョブのフィルター設定ダイアログ作成
		JobDetailFilterDialog dialog = new JobDetailFilterDialog(
				HandlerUtil.getActiveWorkbenchWindow(event).getShell(),
				view.getComposite().getJobDetailViewModel().getFilterCondition()
				);
		
		try {
			if (dialog.open() == Window.OK) {
				view.getComposite().getJobDetailViewModel().setFilterCondition(dialog.getFilterCondition().orElse(null));
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e); 
			throw new ExecutionException(e.getMessage(), e);
		}
		return null;
	}
	
	@Override
	public void dispose() {
	}
}
