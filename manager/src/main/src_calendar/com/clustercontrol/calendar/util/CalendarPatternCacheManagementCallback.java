/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.util;

import com.clustercontrol.commons.util.JpaTransactionCallback;

public class CalendarPatternCacheManagementCallback implements JpaTransactionCallback {
	
	public final String calendarPatternId;
	
	public CalendarPatternCacheManagementCallback(String calendarPatternId) {
		this.calendarPatternId = calendarPatternId;
	}
	
	@Override
	public void preFlush() { }

	@Override
	public void postFlush() { }

	@Override
	public void preCommit() { }

	@Override
	public void postCommit() {
		CalendarPatternCache.remove(calendarPatternId);
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
		h = h * 31 + (calendarPatternId == null ? 0 : calendarPatternId.hashCode());
		return h;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof CalendarPatternCacheManagementCallback) {
			CalendarPatternCacheManagementCallback cast = (CalendarPatternCacheManagementCallback)obj;
			if (calendarPatternId != null && calendarPatternId.equals(cast.calendarPatternId)) {
				return true;
			}
		}
		return false;
	}
	
}
