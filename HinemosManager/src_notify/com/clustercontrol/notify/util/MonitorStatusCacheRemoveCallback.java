/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.util;

import com.clustercontrol.commons.util.JpaTransactionCallback;

public class MonitorStatusCacheRemoveCallback implements JpaTransactionCallback {

	public final String m_pluginId;

	public final String m_monitorId;

	public MonitorStatusCacheRemoveCallback(String pluginId, String monitorId) {
		this.m_pluginId = pluginId;
		this.m_monitorId = monitorId;
	}

	@Override
	public void preFlush() { }

	@Override
	public void postFlush() { }

	@Override
	public void preCommit() { }

	@Override
	public void postCommit() {
		MonitorStatusCache.remove(m_pluginId, m_monitorId);
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
		return obj instanceof MonitorStatusCacheRemoveCallback;
	}
	
	
}
