/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.JpaTransactionCallback;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.monitor.session.MonitorSettingControllerBean;

public class MonitorCollectDataCacheRefreshCallback implements JpaTransactionCallback {

	// Logger
	private static Log m_log = LogFactory.getLog( MonitorSettingControllerBean.class );

	public final String monitorId;

	public final String monitorTypeId;

	public final int range;

	public boolean isCommit = false;

	public MonitorCollectDataCacheRefreshCallback(
			String monitorId, String monitorTypeId, int range) {
		this.monitorId = monitorId;
		this.monitorTypeId = monitorTypeId;
		this.range = range;
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
			JpaTransactionManager jtm = null;
			try {
				jtm = new JpaTransactionManager();
				jtm.begin();
				MonitorCollectDataCache.refreshRange(monitorId, monitorTypeId, range);
				jtm.commit();
			} catch (RuntimeException e) {
				if (jtm != null)
					jtm.rollback();
				m_log.warn("postClose() :"
						+ " monitorId = " + monitorId
						+ ", monitorTypeId = " + monitorTypeId
						+ ", range = " + range
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			} finally {
				if (jtm != null)
					jtm.close();
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((monitorId == null) ? 0 : monitorId.hashCode());
		result = prime * result + ((monitorTypeId == null) ? 0 : monitorTypeId.hashCode());
		result = prime * result + range;
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
		MonitorCollectDataCacheRefreshCallback other = (MonitorCollectDataCacheRefreshCallback) obj;
		if (monitorId == null) {
			if (other.monitorId != null)
				return false;
		} else if (!monitorId.equals(other.monitorId))
			return false;
		if (monitorTypeId == null) {
			if (other.monitorTypeId != null)
				return false;
		} else if (!monitorTypeId.equals(other.monitorTypeId))
			return false;
		if (range != other.range)
			return false;
		return true;
	}

}
