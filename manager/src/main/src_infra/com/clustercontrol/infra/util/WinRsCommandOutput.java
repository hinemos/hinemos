/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
