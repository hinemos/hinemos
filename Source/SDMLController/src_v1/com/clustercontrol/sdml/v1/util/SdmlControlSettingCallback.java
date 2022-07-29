/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.v1.util;

import com.clustercontrol.commons.bean.SettingUpdateInfo;
import com.clustercontrol.commons.util.JpaTransactionCallback;
import com.clustercontrol.sdml.util.SdmlControlSettingManagerUtil;
import com.clustercontrol.sdml.v1.session.SdmlControllerBean;
import com.clustercontrol.util.HinemosTime;

/**
 * SDML制御設定更新時のCallback
 *
 */
public class SdmlControlSettingCallback implements JpaTransactionCallback {

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
		// キャッシュ更新
		SdmlControllerBean.refreshCache();

		SettingUpdateInfo.getInstance().setSdmlControlSettingUpdateTime(HinemosTime.currentTimeMillis());

		// 接続中のHinemosAgentに対する更新通知
		SdmlControlSettingManagerUtil.broadcastConfiguredFlowControl();
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

	@Override
	public int hashCode() {
		return 1;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		return obj instanceof SdmlControlSettingCallback;
	}
}
