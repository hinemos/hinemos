/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.util;

import com.clustercontrol.commons.bean.SettingUpdateInfo;
import com.clustercontrol.commons.util.JpaTransactionCallback;
import com.clustercontrol.util.HinemosTime;

public class UserRoleCacheRefreshCallback implements JpaTransactionCallback {

	@Override
	public void preFlush() { }

	@Override
	public void postFlush() { }

	@Override
	public void preCommit() { }

	@Override
	public void postCommit() {
		// ユーザ管理系のキャッシュ情報を更新する
		UserRoleCache.refresh();
		
		// ユーザ情報の更新ででリポジトリ参照情報も変わるため、更新時刻をリフレッシュ
		SettingUpdateInfo.getInstance().setRepositoryUpdateTime(HinemosTime.currentTimeMillis());
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
		return obj instanceof UserRoleCacheRefreshCallback;
	}
	
	
}
