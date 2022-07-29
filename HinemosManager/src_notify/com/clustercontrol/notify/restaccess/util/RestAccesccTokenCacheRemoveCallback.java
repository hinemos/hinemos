/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.restaccess.util;

import com.clustercontrol.commons.util.JpaTransactionCallback;
import com.clustercontrol.notify.util.AccessRest;

public class RestAccesccTokenCacheRemoveCallback implements JpaTransactionCallback {

	public final String m_restAccessId;

	public RestAccesccTokenCacheRemoveCallback(String restAccessId ) {
		this.m_restAccessId = restAccessId;
	}

	@Override
	public void preFlush() { }

	@Override
	public void postFlush() { }

	@Override
	public void preCommit() { }

	@Override
	public void postCommit() {
		AccessRest.clearRestAccessIdCache(m_restAccessId);
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
		if (obj instanceof RestAccesccTokenCacheRemoveCallback) {
			String targetId = ((RestAccesccTokenCacheRemoveCallback) obj).m_restAccessId;
			if (m_restAccessId.equals(targetId)) {
				return true;
			}
		}
		return false;
	}
	
	
}
