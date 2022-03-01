/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

/**
 * ファイル出力の実行結果情報を保持するクラス<BR>
 *
 */
@XmlType(namespace = "http://jobmanagement.ws.clustercontrol.com")
public class RunOutputResultInfo implements Serializable {
	private static final long serialVersionUID = -5920913289024178396L;
	
	private List<Integer> erorrTargetTypeList = new ArrayList<>();
	/** 出力ファイル名 */
	private String stdoutFileName = "";
	private String stderrFileName = "";
	/** 標準出力をファイル出力時のエラーメッセージ */
	private String stdoutErrorMessage;
	/** 標準エラー出力をファイル出力時のエラーメッセージ */
	private String stderrErrorMessage;

	public String getStdoutFileName() {
		return stdoutFileName;
	}

	public void setStdoutFileName(String stdoutFileName) {
		this.stdoutFileName = stdoutFileName;
	}

	public String getStderrFileName() {
		return stderrFileName;
	}

	public void setStderrFileName(String stderrFileName) {
		this.stderrFileName = stderrFileName;
	}

	public List<Integer> getErorrTargetTypeList() {
		return erorrTargetTypeList;
	}

	public void setErorrTargetTypeList(List<Integer> erorrTargetTypeList) {
		this.erorrTargetTypeList = erorrTargetTypeList;
	}

	public String getStdoutErrorMessage() {
		return stdoutErrorMessage;
	}

	public void setStdoutErrorMessage(String stdoutErrorMessage) {
		this.stdoutErrorMessage = stdoutErrorMessage;
	}

	public String getStderrErrorMessage() {
		return stderrErrorMessage;
	}

	public void setStderrErrorMessage(String stderrErrorMessage) {
		this.stderrErrorMessage = stderrErrorMessage;
	}
}
