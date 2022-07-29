/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.commons.util;

import com.clustercontrol.commons.util.JpaTransactionCallback;
import com.clustercontrol.maintenance.factory.HinemosPropertyInfoCache;

public class HinemosPropertyInfoCacheRefreshCallback implements JpaTransactionCallback {

	@Override
	public void preFlush() { }

	@Override
	public void postFlush() { }

	@Override
	public void preCommit() { }

	@Override
	public void postCommit() {
		HinemosPropertyInfoCache.refresh();
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
		return obj instanceof HinemosPropertyInfoCacheRefreshCallback;
	}
	
	
}
