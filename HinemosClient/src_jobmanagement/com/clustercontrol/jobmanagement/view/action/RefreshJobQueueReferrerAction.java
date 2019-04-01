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

import com.clustercontrol.jobmanagement.view.JobQueueReferrerView;
import com.clustercontrol.util.ViewUtil;

/**
 * {@link JobQueueReferrerView}の表示を更新するコマンドを実行します。
 *
 * @since 6.2.0
 */
public class RefreshJobQueueReferrerAction extends AbstractHandler {
	public static final String ID = RefreshJobQueueReferrerAction.class.getName();

	private static Log log = LogFactory.getLog(RefreshJobQueueReferrerAction.class);

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

		ViewUtil.executeWith(JobQueueReferrerView.class, v -> v.refresh());
		return null;
	}

}
