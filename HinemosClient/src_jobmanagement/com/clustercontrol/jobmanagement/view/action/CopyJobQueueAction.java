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
import org.eclipse.jface.window.Window;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.clustercontrol.dialog.ApiResultDialog;
import com.clustercontrol.jobmanagement.dialog.JobQueueSettingDialog;
import com.clustercontrol.jobmanagement.dialog.JobQueueSettingDialog.EditMode;
import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
import com.clustercontrol.jobmanagement.view.action.JobQueueEditor.JobQueueEditTarget;
import com.clustercontrol.util.ViewUtil;
import com.clustercontrol.util.LogUtil;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.jobmanagement.JobQueueSetting;

/**
 * ジョブ同時実行制御キューのコピーコマンドを実行します。
 *
 * @since 6.2.0
 */
public class CopyJobQueueAction extends AbstractHandler implements IElementUpdater {
	public static final String ID = CopyJobQueueAction.class.getName();

	private static Log log = LogFactory.getLog(CopyJobQueueAction.class);

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

			// 変更前の設定情報を取得
			JobQueueSetting setting;
			try {
				JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(target.getManagerName());
				setting = wrapper.getJobQueue(target.getQueueId());
			} catch (Throwable t) {
				log.info(LogUtil.filterWebFault("execute: ", t));
				new ApiResultDialog().addFailure(target.getManagerName(), t,
						Messages.get("message.jobqueue.id", target.getQueueId())).show();
				return;
			}

			JobQueueSettingDialog dialog = new JobQueueSettingDialog(
					HandlerUtil.getActiveWorkbenchWindow(event).getShell(), EditMode.CREATE, setting,
					target.getManagerName(),
					// OK時の処理
					(managerName) -> {
						log.debug("execute: API call.");
						ApiResultDialog resultDialog = new ApiResultDialog();
						boolean shouldClose;
						try {
							JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(managerName);
							wrapper.addJobQueue(setting);

							resultDialog.addSuccess(managerName,
									Messages.get("message.jobqueue.created", setting.getQueueId()));
							shouldClose = true;
						} catch (Throwable t) {
							log.info(LogUtil.filterWebFault("execute: ", t));
							resultDialog.addFailure(managerName, t,
									Messages.get("message.jobqueue.id", setting.getQueueId()));
							shouldClose = false;
						}
						resultDialog.show();
						return shouldClose;
					});
			if (dialog.open() == Window.OK) {
				// 関連ビューを更新する
				ViewUtil.executeWith(JobQueueEditor.class, v -> v.onJobQueueEdited());
			}
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
