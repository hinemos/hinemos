package com.clustercontrol.jobmanagement.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.JpaTransactionCallback;

public class RefreshRunningQueueAfterCommitCallback implements JpaTransactionCallback {

	private static Log m_log = LogFactory.getLog(RefreshRunningQueueAfterCommitCallback.class);

	@Override
	public void preFlush() {}

	@Override
	public void postFlush() {}

	@Override
	public void preCommit() {}

	@Override
	public void postCommit() {
		m_log.debug("call postCommit(refreshRunningQueue)");
		JobMultiplicityCache.refreshRunningQueue();
	}

	@Override
	public void preRollback() {}

	@Override
	public void postRollback() {}

	@Override
	public void preClose() {}

	@Override
	public void postClose() {}

}
