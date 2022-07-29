/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.util;

import com.clustercontrol.commons.util.JpaTransactionCallback;

public class CalendarChangedNotificationCallback implements JpaTransactionCallback {
	
	@Override
	public void preFlush() { }

	@Override
	public void postFlush() { }

	@Override
	public void preCommit() { }

	@Override
	public void postCommit() {
		// commit後に外部コンポーネントに通知する
		CalendarManagerUtil.broadcastConfiguredFlowControl();
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
		return obj instanceof CalendarChangedNotificationCallback;
	}
	
}
