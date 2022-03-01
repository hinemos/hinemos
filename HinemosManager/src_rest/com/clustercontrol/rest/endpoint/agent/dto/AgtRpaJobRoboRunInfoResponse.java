/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.agent.dto;

import com.clustercontrol.jobmanagement.rpa.bean.RoboRunInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.RpaJobReturnCodeConditionEnum;

@RestBeanConvertAssertion(from = RoboRunInfo.class)
public class AgtRpaJobRoboRunInfoResponse {
	
	// -- from RoboInfo
	private Long datetime;
	private String sessionId;
	private String jobunitId;
	private String jobId;
	private String facilityId;

	// ---- from RoboRunInfo
	private String execCommand;
	private String destroyCommand;
	private Boolean login;
	private Boolean logout;
	private Boolean screenshotOnAbnormalExit;
	private String abnormalReturnCode;
	@RestBeanConvertEnum
	private RpaJobReturnCodeConditionEnum abnormalReturnCodeCondition;
	private Boolean destroy;

	public AgtRpaJobRoboRunInfoResponse() {
	}

	public Long getDatetime() {
		return datetime;
	}

	public void setDatetime(Long datetime) {
		this.datetime = datetime;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getJobunitId() {
		return jobunitId;
	}

	public void setJobunitId(String jobunitId) {
		this.jobunitId = jobunitId;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getExecCommand() {
		return execCommand;
	}

	public void setExecCommand(String execCommand) {
		this.execCommand = execCommand;
	}

	public String getDestroyCommand() {
		return destroyCommand;
	}

	public void setDestroyCommand(String destroyCommand) {
		this.destroyCommand = destroyCommand;
	}
	
	public Boolean getLogin() {
		return login;
	}

	public void setLogin(Boolean login) {
		this.login = login;
	}

	public Boolean getLogout() {
		return logout;
	}

	public void setLogout(Boolean logout) {
		this.logout = logout;
	}

	public Boolean getScreenshotOnAbnormalExit() {
		return screenshotOnAbnormalExit;
	}

	public void setScreenshotOnAbnormalExit(Boolean screenshotOnAbnormalExit) {
		this.screenshotOnAbnormalExit = screenshotOnAbnormalExit;
	}

	public String getAbnormalReturnCode() {
		return abnormalReturnCode;
	}

	public void setAbnormalReturnCode(String abnormalReturnCode) {
		this.abnormalReturnCode = abnormalReturnCode;
	}

	public RpaJobReturnCodeConditionEnum getAbnormalReturnCodeCondition() {
		return abnormalReturnCodeCondition;
	}

	public void setAbnormalReturnCodeCondition(RpaJobReturnCodeConditionEnum abnormalReturnCodeCondition) {
		this.abnormalReturnCodeCondition = abnormalReturnCodeCondition;
	}

	public Boolean getDestroy() {
		return destroy;
	}

	public void setDestroy(Boolean destroy) {
		this.destroy = destroy;
	}
}
