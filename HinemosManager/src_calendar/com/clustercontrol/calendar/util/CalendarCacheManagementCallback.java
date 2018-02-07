/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.util;

import com.clustercontrol.commons.util.JpaTransactionCallback;

public class CalendarCacheManagementCallback implements JpaTransactionCallback {
	
	public final String calendarId;
	
	public CalendarCacheManagementCallback(String calendarId) {
		this.calendarId = calendarId;
	}
	
	@Override
	public void preFlush() { }

	@Override
	public void postFlush() { }

	@Override
	public void preCommit() { }

	@Override
	public void postCommit() {
		CalendarCache.remove(calendarId);
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
		h = h * 31 + (calendarId == null ? 0 : calendarId.hashCode());
		return h;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof CalendarCacheManagementCallback) {
			CalendarCacheManagementCallback cast = (CalendarCacheManagementCallback)obj;
			if (calendarId != null && calendarId.equals(cast.calendarId)) {
				return true;
			}
		}
		return false;
	}
	
}
