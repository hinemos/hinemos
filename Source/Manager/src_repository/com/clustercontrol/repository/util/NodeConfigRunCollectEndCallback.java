/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.util;

import com.clustercontrol.commons.util.JpaTransactionCallback;
import com.clustercontrol.repository.factory.NodeConfigRunCollectManager;

public class NodeConfigRunCollectEndCallback implements JpaTransactionCallback {

	public final String facilityId;
	public final String settingId;

	public NodeConfigRunCollectEndCallback(String facilityId, String settingId) {
		this.facilityId = facilityId;
		this.settingId = settingId;
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
		NodeConfigRunCollectManager.endRunCollect(this.facilityId, this.settingId);
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
		int h = 1;
		h = h * 31 + (facilityId == null ? 0 : facilityId.hashCode());
		h = h * 31 + (settingId == null ? 0 : settingId.hashCode());
		return h;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof NodeConfigRunCollectEndCallback) {
			NodeConfigRunCollectEndCallback cast = (NodeConfigRunCollectEndCallback) obj;
			if (((facilityId == null && cast.facilityId == null)
					|| (facilityId != null && facilityId.equals(cast.facilityId)))
					&& ((settingId == null && cast.settingId == null)
							|| (settingId != null && settingId.equals(cast.settingId)))) {
				return true;
			}
		}
		return false;
	}
}
