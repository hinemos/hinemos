/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.commons.bean;

import java.util.Date;

/**
 * スレッドの情報を格納するクラス<br/>
 */
public class ThreadInfo {

	// Thread
	public final Thread thread;

	// Running Class Name (or null)
	public final String taskClassName;
	// Running Task Startup Time
	public final long taskStartTime;

	public ThreadInfo(Thread thread, String taskClassName, long taskStartTime) {
		this.thread = thread;
		this.taskClassName = taskClassName;
		this.taskStartTime = taskStartTime;
	}

	@Override
	public String toString() {
		return String.format("%s [tid=%d, name=%s, class=%s, startTime=%5$tY-%5$tm-%5$td %5$tH:%5$tM:%5$tS]",
				this.getClass().getSimpleName(), thread.getId(), thread.getName(), taskClassName, new Date(taskStartTime));
	}
}
