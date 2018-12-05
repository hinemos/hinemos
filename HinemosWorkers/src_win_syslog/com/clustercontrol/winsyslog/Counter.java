/*

Copyright (C) 2017 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.winsyslog;

import org.apache.log4j.Logger;

public class Counter {
	private static Logger logger = Logger.getLogger(Counter.class);
	
	private static final long interval = 1000;

	private String title;
	private long success;
	private long warnning;
	private long failed;
	
	public Counter(String title) {
		this.title = title;
	}
	
	public long getSuccess() {
		synchronized(this) {
			return success;
		}
	}

	public long incrementSuccess() {
		synchronized(this) {
			++success;
			printIf();
			return success;
		}
	}
	
	public long getWarning() {
		synchronized(this) {
			return warnning;
		}
	}

	public long incrementWarning() {
		synchronized(this) {
			++warnning;
			printIf();
			return warnning;
		}
	}
	
	public long getFailed() {
		synchronized(this) {
			return failed;
		}
	}
	
	public long incrementFailed() {
		synchronized(this) {
			++failed;
			printIf();
			return failed;
		}
	}
	
	protected void printIf() {
		synchronized(this) {
			if ((success + warnning + failed) % interval == 0) {
				logger.info(this.toString());
			}
		}
	}

	@Override
	public String toString() {
		return String.format("%s : total=%d, success=%d, warnning=%d, failed=%d",
				title, success + warnning + failed, success, warnning, failed);
	}
}