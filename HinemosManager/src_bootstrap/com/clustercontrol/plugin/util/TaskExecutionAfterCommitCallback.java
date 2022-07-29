/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.plugin.util;

import com.clustercontrol.commons.util.JpaTransactionCallback;
import com.clustercontrol.plugin.impl.AsyncTask;
import com.clustercontrol.plugin.impl.AsyncWorkerPlugin;

/**
 * (commit -> execution) -> 上位トランザクションにcommitに伴い、executionを発動するJpaTransactionCallbackクラス
 */
public class TaskExecutionAfterCommitCallback implements JpaTransactionCallback {

	private final AsyncTask task;

	public TaskExecutionAfterCommitCallback(AsyncTask task) {
		this.task = task;
	}

	@Override
	public void preFlush() {}

	@Override
	public void postFlush() {}

	@Override
	public void preCommit() {}

	/**
	 * addTaskをコールしたトランザクションにおいてcommitに成功した場合、タスクの非同期実行を確定する
	 */
	@Override
	public void postCommit() {
		AsyncWorkerPlugin.commitTaskExecution(task);
	}

	@Override
	public void preRollback() {}

	@Override
	public void postRollback() {}

	@Override
	public void preClose() {}

	@Override
	public void postClose() {}
	
	@Override
	public int hashCode() {
		int h = 1;
		h = h * 31 + (task == null ? 0 : task.hashCode());
		return h;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof TaskExecutionAfterCommitCallback) {
			TaskExecutionAfterCommitCallback cast = (TaskExecutionAfterCommitCallback)obj;
			if (this.task == cast.task) {
				return true;
			}
		}
		return false;
	}
	
}