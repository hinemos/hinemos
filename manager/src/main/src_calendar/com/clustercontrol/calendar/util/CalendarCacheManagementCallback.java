/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.calendar.util;

import com.clustercontrol.commons.util.JpaTransactionCallback;

public class CalendarCacheManagementCallback implements JpaTransactionCallback {
	
	public final String calendarId;
	
	public CalendarCacheManagementCallback(String calendarId) {
		this.calendarId = calendarId;
	}
	
	@Override
	public void preBegin() { }

	@Override
	public void postBegin() { }

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
