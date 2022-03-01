/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.JpaTransactionCallback;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntityPK;

public class FromRunningAfterCommitCallback implements JpaTransactionCallback {
	
	private static Log m_log = LogFactory.getLog(FromRunningAfterCommitCallback.class);
	
	private JobSessionNodeEntityPK pk;
	
	public FromRunningAfterCommitCallback(JobSessionNodeEntityPK pk) {
		this.pk = pk.clone();
	}
	
	@Override
	public void preFlush() {}

	@Override
	public void postFlush() {}

	@Override
	public void preCommit() {}

	@Override
	public void postCommit() {
		m_log.debug("call postCommit(fromRunning) : " + pk);
		JobMultiplicityCache.fromRunning(pk);
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
