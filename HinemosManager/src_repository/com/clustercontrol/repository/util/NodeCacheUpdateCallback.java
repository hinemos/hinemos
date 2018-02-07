/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.util;

import com.clustercontrol.commons.util.JpaTransactionCallback;
import com.clustercontrol.repository.factory.NodeProperty;

public class NodeCacheUpdateCallback implements JpaTransactionCallback {
	
	public final String facilityId;
	
	public NodeCacheUpdateCallback(String facilityId) {
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
		NodeProperty.updateNode(facilityId);
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
		int h = 1;
		h = h * 31 + (facilityId == null ? 0 : facilityId.hashCode());
		return h;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof NodeCacheUpdateCallback) {
			NodeCacheUpdateCallback cast = (NodeCacheUpdateCallback)obj;
			if (facilityId != null && facilityId.equals(cast.facilityId)) {
				return true;
			}
		}
		return false;
	}
	
}
