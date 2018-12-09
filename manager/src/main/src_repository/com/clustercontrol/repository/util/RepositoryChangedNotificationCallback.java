/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.util;

import com.clustercontrol.commons.bean.SettingUpdateInfo;
import com.clustercontrol.commons.util.JpaTransactionCallback;
import com.clustercontrol.util.HinemosTime;

public class RepositoryChangedNotificationCallback implements JpaTransactionCallback {

	@Override
	public void preFlush() { }

	@Override
	public void postFlush() { }

	@Override
	public void preCommit() { }

	@Override
	public void postCommit() {
		// リポジトリ更新時刻リフレッシュ
		SettingUpdateInfo.getInstance().setRepositoryUpdateTime(HinemosTime.currentTimeMillis());
		
		// 接続中のHinemosAgentに対する更新通知
		RepositoryManagerUtil.broadcastConfigured();
	}

	@Override
	public void preRollback() { }

	@Override
	public void postRollback() { }

	@Override
	public void preClose() { }

	@Override
	public void postClose() {}
	
	@Override
	public int hashCode() {
		return 1;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		return obj instanceof RepositoryChangedNotificationCallback;
	}
	
}
