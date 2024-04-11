/*
 * Copyright (c) 2023 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.util;

import com.clustercontrol.commons.util.JpaTransactionCallback;

public class MonitorStatusCacheRemoveByFacilityIdCallback implements JpaTransactionCallback {

	public final String facilityId;

	public MonitorStatusCacheRemoveByFacilityIdCallback(String facilityId) {
		this.facilityId = facilityId;
	}

	@Override
	public void preFlush() { }

	@Override
	public void postFlush() { }

	@Override
	public void preCommit() { }

	@Override
	public void postCommit() {
		MonitorStatusCache.remove(facilityId);
	}

	@Override
	public void preRollback() { }

	@Override
	public void postRollback() { }

	@Override
	public void preClose() { }

	@Override
	public void postClose() { }

	@Override
	public int hashCode() {
		return 1;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		return obj instanceof MonitorStatusCacheRemoveByFacilityIdCallback;
	}

}
