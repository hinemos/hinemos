/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
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
import org.eclipse.ui.handlers.HandlerUtil;
import org.openapitools.client.model.AddJobQueueRequest;
import org.openapitools.client.model.JobQueueResponse;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.dialog.ApiResultDialog;
import com.clustercontrol.jobmanagement.dialog.JobQueueSettingDialog;
import com.clustercontrol.jobmanagement.dialog.JobQueueSettingDialog.EditMode;
import com.clustercontrol.jobmanagement.util.JobRestClientWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.ViewUtil;

/**
 * ジョブ同時実行制御キューの新規作成コマンドを実行します。
 *
 * @since 6.2.0
 */
public class CreateJobQueueAction extends AbstractHandler {
	public static final String ID = CreateJobQueueAction.class.getName();

	private static Log log = LogFactory.getLog(CreateJobQueueAction.class);

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

		// 初期値を設定
		JobQueueResponse setting = new JobQueueResponse();
		setting.setQueueId("");
		setting.setName("");
		setting.setConcurrency(null);
		setting.setOwnerRoleId(RoleIdConstant.ALL_USERS);

		// 設定ダイアログを呼び出す
		JobQueueSettingDialog dialog = new JobQueueSettingDialog(HandlerUtil.getActiveWorkbenchWindow(event).getShell(),
				EditMode.CREATE, setting, RestConnectManager.getActiveManagerNameList().get(0),
				// OK時の処理
				(managerName) -> {
					log.debug("execute: API call.");
					ApiResultDialog resultDialog = new ApiResultDialog();
					boolean shouldClose;
					try {
						JobRestClientWrapper wrapper = JobRestClientWrapper.getWrapper(managerName);
						AddJobQueueRequest request = new AddJobQueueRequest();
						RestClientBeanUtil.convertBean(setting, request);
						wrapper.addJobQueue(request);

						resultDialog.addSuccess(managerName,
								Messages.get("message.jobqueue.created", setting.getQueueId()));
						shouldClose = true;
					} catch (Throwable t) {
						log.info("execute: " + t.getClass().getName() + ", " + t.getMessage());
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

		return null;
	}
}
