/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.util;

import com.clustercontrol.commons.util.JpaTransactionCallback;

public class MonitorJudgementInfoCacheRemoveCallback implements JpaTransactionCallback {

	public final String monitorId;
	
	public boolean isCommit = false;

	public MonitorJudgementInfoCacheRemoveCallback(String monitorId) {
		this.monitorId = monitorId;
	}

	@Override
	public boolean isTransaction() {
		return false;
	}

	@Override
	public void preFlush() { }

	@Override
	public void postFlush() { }

	@Override
	public void preCommit() { }

	@Override
	public void postCommit() {
		isCommit = true;
	}

	@Override
	public void preRollback() { }

	@Override
	public void postRollback() { }

	@Override
	public void preClose() { }

	@Override
	public void postClose() {
		if (isCommit) {
				MonitorJudgementInfoCache.remove(monitorId);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((monitorId == null) ? 0 : monitorId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MonitorJudgementInfoCacheRemoveCallback other = (MonitorJudgementInfoCacheRemoveCallback) obj;
		if (monitorId == null) {
			if (other.monitorId != null)
				return false;
		} else if (!monitorId.equals(other.monitorId))
			return false;
		return true;
	}

}
