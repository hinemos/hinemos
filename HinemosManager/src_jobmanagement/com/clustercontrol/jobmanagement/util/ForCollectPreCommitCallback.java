/*
 * Copyright (c) 2020 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.collect.bean.Sample;
import com.clustercontrol.collect.util.CollectDataUtil;
import com.clustercontrol.commons.util.JpaTransactionCallback;

public class ForCollectPreCommitCallback implements JpaTransactionCallback {
	
	private static Log m_log = LogFactory.getLog(ForCollectPreCommitCallback.class);
	
	private List<Sample> sampleList;
	
	public ForCollectPreCommitCallback(List<Sample> sampleList) {
			this.sampleList = sampleList;
	}
	
	@Override
	public void preFlush() {}

	@Override
	public void postFlush() {}

	@Override
	public void preCommit() {
		if( m_log.isDebugEnabled()){
			m_log.debug("call preCommit(ForCollect) : sampleList id="+ sampleList );
		}
		CollectDataUtil.put(sampleList);
	}

	@Override
	public void postCommit() {}

	@Override
	public void preRollback() {}

	@Override
	public void postRollback() {}

	@Override
	public void preClose() {}

	@Override
	public void postClose() {}

}