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
package com.clustercontrol.infra.util;

public class WinRsCommandOutput {
	private String stdout;
	private String stderr;
	private long exitCode;
	private WinRsCommandState state;
	
	public WinRsCommandOutput(String stdout, String stderr, long exitCode,
			WinRsCommandState state) {
		this.stdout = stdout;
		this.stderr = stderr;
		this.exitCode = exitCode;
		this.state = state;
	}
	public String getStdout() {
		return stdout;
	}
	public void setStdout(String stdout) {
		this.stdout = stdout;
	}
	public String getStderr() {
		return stderr;
	}
	public void setStderr(String stderr) {
		this.stderr = stderr;
	}
	public long getExitCode() {
		return exitCode;
	}
	public void setExitCode(long exitCode) {
		this.exitCode = exitCode;
	}
	public WinRsCommandState getState() {
		return state;
	}
	public void setState(WinRsCommandState state) {
		this.state = state;
	}
}
