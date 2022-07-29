/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.view.action;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;
import org.openapitools.client.model.JobQueueResponse;

import com.clustercontrol.dialog.ApiResultDialog;
import com.clustercontrol.jobmanagement.dialog.JobQueueSettingDialog;
import com.clustercontrol.jobmanagement.dialog.JobQueueSettingDialog.EditMode;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.jobmanagement.view.action.JobQueueEditor.JobQueueEditTarget;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.ViewUtil;

/**
 * ジョブ同時実行制御キューの参照コマンドを実行します。
 *
 * @since 6.2.0
 */
public class ShowJobQueueAction extends AbstractHandler implements IElementUpdater {
	public static final String ID = ShowJobQueueAction.class.getName();

	private static Log log = LogFactory.getLog(ShowJobQueueAction.class);

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
			JobQueueEditTarget target = view.getJobQueueEditTarget();
			if (target.isEmpty()) return;

			// 設定情報を取得
			JobQueueResponse setting;
			try {
				JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(target.getManagerName());
				setting = wrapper.getJobQueue(target.getQueueId());
			} catch (Throwable t) {
				log.info("execute:", t);
				new ApiResultDialog().addFailure(target.getManagerName(), t,
						Messages.get("message.jobqueue.id", target.getQueueId())).show();
				return;
			}

			JobQueueSettingDialog dialog = new JobQueueSettingDialog(
					HandlerUtil.getActiveWorkbenchWindow(event).getShell(), EditMode.READONLY, setting,
					target.getManagerName(),
					// OK時の処理
					(managerName) -> {
						// クローズするだけ
						return true;
					});
			dialog.open();
		});
		return null;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		ViewUtil.executeWithActive(JobQueueEditor.class, view -> {
			setBaseEnabled(view.getSelectedJobQueueCount() == 1);
		});
	}
}
