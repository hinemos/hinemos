/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.jobmanagement.bean.RunOutputResultInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(to = RunOutputResultInfo.class)
public class SetJobOutputResultRequest extends AgentRequestDto {

	// ---- from RunOutputResultInfo
	private List<Integer> erorrTargetTypeList = new ArrayList<>();
	private String stdoutFileName = "";
	private String stderrFileName = "";
	private String stdoutErrorMessage = "";
	private String stderrErrorMessage = "";

	public SetJobOutputResultRequest() {
	}

	// ---- accessors
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
