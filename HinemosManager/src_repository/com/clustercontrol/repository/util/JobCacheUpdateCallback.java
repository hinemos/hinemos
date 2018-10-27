/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.util;

import com.clustercontrol.commons.util.JpaTransactionCallback;
import com.clustercontrol.jobmanagement.factory.FullJob;

public class JobCacheUpdateCallback implements JpaTransactionCallback {

	@Override
	public void preFlush() { }

	@Override
	public void postFlush() { }

	@Override
	public void preCommit() { }

	@Override
	public void postCommit() {
			// ファシリティ名が変更されている可能性があるので、スコープパスを更新する
			FullJob.updateScopesInCache();
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
			return obj instanceof JobCacheUpdateCallback;
	}
}