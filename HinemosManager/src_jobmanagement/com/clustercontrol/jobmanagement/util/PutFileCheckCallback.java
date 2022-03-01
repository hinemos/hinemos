/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import com.clustercontrol.commons.util.JpaTransactionCallback;

public class PutFileCheckCallback implements JpaTransactionCallback {
		
	public PutFileCheckCallback() {
	}
	
	@Override
	public void preFlush() {}

	@Override
	public void postFlush() {}

	@Override
	public void preCommit() {
	}

	@Override
	public void postCommit() {
		SendTopic.putFileCheck(null);
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