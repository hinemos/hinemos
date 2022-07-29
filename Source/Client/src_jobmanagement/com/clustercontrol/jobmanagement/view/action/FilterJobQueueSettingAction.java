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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RegistryToggleState;

import com.clustercontrol.jobmanagement.dialog.JobQueueFilterDialog;
import com.clustercontrol.jobmanagement.view.JobQueueSettingView;
import com.clustercontrol.util.ViewUtil;

/**
 * {@link JobQueueSettingView}をフィルタリングするコマンドを実行します。
 *
 * @version 6.2.0
 */
public class FilterJobQueueSettingAction extends AbstractHandler {
	public static final String ID = FilterJobQueueSettingAction.class.getName();

	private static Log log = LogFactory.getLog(FilterJobQueueSettingAction.class);

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

	private Object execute0(ExecutionEvent event) throws Exception {
		if (!isEnabled()) {
			log.debug("execute: Disbaled.");
			return null;
		}
		log.debug("execute:");

		JobQueueSettingView view = ViewUtil.findActive(JobQueueSettingView.class);
		if (view == null) return null;

		boolean beforeValue = HandlerUtil.toggleCommandState(event.getCommand());
		if (beforeValue) {
			// on -> off
			view.disableFilter();
			view.update();
		} else {
			// off -> on
			JobQueueFilterDialog dialog = new JobQueueFilterDialog(
					HandlerUtil.getActiveWorkbenchWindow(event).getShell(), view.getManagerFilter(),
					view.getQueueFilter());
			if (dialog.open() == IDialogConstants.OK_ID) {
				view.enableFilter(dialog.getManagerFilter(), dialog.getQueueFilter());
				view.update();
			} else {
				event.getCommand().getState(RegistryToggleState.STATE_ID).setValue(false);
			}
		}
		return null;
	}
}
