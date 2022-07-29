/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.view.action;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.dialog.ApiResultDialog;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.jobmanagement.view.action.JobQueueEditor.JobQueueEditTarget;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.ViewUtil;

/**
 * ジョブ同時実行制御キューの削除コマンドを実行します。
 *
 * @since 6.2.0
 */
public class DeleteJobQueueAction extends AbstractHandler implements IElementUpdater {
	public static final String ID = DeleteJobQueueAction.class.getName();

	private static Log log = LogFactory.getLog(DeleteJobQueueAction.class);

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
		if (!isEnabled()) {
			log.debug("execute: Disbaled.");
			return null;
		}
		log.debug("execute:");

		ViewUtil.executeWithActive(JobQueueEditor.class, view -> {
			List<JobQueueEditTarget> targets = view.getJobQueueEditTargets();
			if (targets.size() == 0) return;

			// 削除確認
			String confirmMessage;
			if (targets.size() == 1) {
				confirmMessage = Messages.get("message.jobqueue.delete.confirm", targets.get(0).getQueueId());
			} else {
				confirmMessage = Messages.get("message.jobqueue.delete.multiple.confirm", targets.size());
			}

			if (!MessageDialog.openQuestion(null, Messages.getString("confirmed"), confirmMessage)) {
				return;
			}

			// 削除実行
			log.debug("execute: API call.");
			ApiResultDialog resultDialog = new ApiResultDialog();
			for (JobQueueEditTarget target : targets) {
				try {
					JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(target.getManagerName());
					wrapper.deleteJobQueue(target.getQueueId());

					resultDialog.addSuccess(target.getManagerName(),
							Messages.get("message.jobqueue.deleted", target.getQueueId()));
				} catch (Throwable t) {
					log.info("execute: " + t.getClass().getName() + ", " + t.getMessage());
					resultDialog.addFailure(target.getManagerName(), t,
							Messages.get("message.jobqueue.id", target.getQueueId()));
				}
			}
			resultDialog.show();

			// 関連ビューを更新する
			ViewUtil.executeWith(JobQueueEditor.class, v -> v.onJobQueueEdited());
		});
		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		JobQueueEditor view = ViewUtil.findActive(JobQueueEditor.class);
		if (view == null) {
			setBaseEnabled(false);
		} else {
			setBaseEnabled(view.getSelectedJobQueueCount() == 1);
		}
	}
}
