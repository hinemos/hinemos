/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.bean;

import com.clustercontrol.utility.settings.SettingConstants;

/**
 * 設定インポートエクスポートのAction実行結果を受け取るためのBean
 *
 */
public class SdmlUtilityActionResult {
	private int result = SettingConstants.ERROR_INPROCESS;
	private String stdOut = null;
	private String errOut = null;

	public SdmlUtilityActionResult() {
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public String getStdOut() {
		return stdOut;
	}

	public void setStdOut(String stdOut) {
		this.stdOut = stdOut;
	}

	public String getErrOut() {
		return errOut;
	}

	public void setErrOut(String errOut) {
		this.errOut = errOut;
	}

	@Override
	public String toString() {
		return "ActionResult[result=" + result + ", stdOut=" + stdOut + ", errOut=" + errOut + "]";
	}
}
