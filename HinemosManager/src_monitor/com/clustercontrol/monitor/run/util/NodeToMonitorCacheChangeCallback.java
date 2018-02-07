/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.util;

import com.clustercontrol.commons.util.JpaTransactionCallback;

public class NodeToMonitorCacheChangeCallback implements JpaTransactionCallback {

	private final String monitorTypeId;
	
	/**
	 * リポジトリ設定が変更された場合に呼び出すコールバックのコンストラクタ
	 */
	public NodeToMonitorCacheChangeCallback() {
		this.monitorTypeId = null;
	}
	
	/**
	 * 監視設定が変更された場合に呼び出すコールバックのコンストラクタ
	 * @param monitorTypeId
	 */
	public NodeToMonitorCacheChangeCallback(String monitorTypeId) {
		this.monitorTypeId = monitorTypeId;
	}
	
	@Override
	public void preFlush() {
	}

	@Override
	public void postFlush() {
	}

	@Override
	public void preCommit() {
	}

	@Override
	public void postCommit() {
		if (monitorTypeId != null) {
			NodeToMonitorCache.getInstance(monitorTypeId).refresh();
		} else {
			NodeToMonitorCache.refreshAll();
		}
	}

	@Override
	public void preRollback() {
	}

	@Override
	public void postRollback() {
	}

	@Override
	public void preClose() {
	}

	@Override
	public void postClose() {
	}
}
