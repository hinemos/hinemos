/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.jobmanagement.bean.JobLinkSendProtocol;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.cmdtool.DatetimeTypeParam;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ProcessingMethodEnum;

public class JobLinkSendSettingResponse {
	private String joblinkSendSettingId;
	private String description;
	private String facilityId;
	@RestPartiallyTransrateTarget
	private String scope;
	@RestBeanConvertEnum
	private ProcessingMethodEnum processMode;
	@RestBeanConvertEnum
	private JobLinkSendProtocol protocol;
	private Integer port;
	private String hinemosUserId;
	private String hinemosPassword;
	private Boolean proxyFlg;
	private String proxyHost;
	private Integer proxyPort; 
	private String proxyUser;
	private String proxyPassword;

	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String regDate;
	private String regUser;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String updateDate;
	private String updateUser;
	private String ownerRoleId;
	
	public JobLinkSendSettingResponse() {
	}
	
	public String getRegDate() {
		return regDate;
	}

	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}

	public String getRegUser() {
		return regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	public String getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getJoblinkSendSettingId() {
		return joblinkSendSettingId;
	}

	public void setJoblinkSendSettingId(String joblinkSendSettingId) {
		this.joblinkSendSettingId = joblinkSendSettingId;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public ProcessingMethodEnum getProcessMode() {
		return processMode;
	}

	public void setProcessMode(ProcessingMethodEnum processMode) {
		this.processMode = processMode;
	}

	public JobLinkSendProtocol getProtocol() {
		return protocol;
	}

	public void setProtocol(JobLinkSendProtocol protocol) {
		this.protocol = protocol;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getHinemosUserId() {
		return hinemosUserId;
	}

	public void setHinemosUserId(String hinemosUserId) {
		this.hinemosUserId = hinemosUserId;
	}

	public String getHinemosPassword() {
		return hinemosPassword;
	}

	public void setHinemosPassword(String hinemosPassword) {
		this.hinemosPassword = hinemosPassword;
	}

	public Boolean getProxyFlg() {
		return proxyFlg;
	}

	public void setProxyFlg(Boolean proxyFlg) {
		this.proxyFlg = proxyFlg;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public Integer getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(Integer proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String getProxyUser() {
		return proxyUser;
	}

	public void setProxyUser(String proxyUser) {
		this.proxyUser = proxyUser;
	}

	public String getProxyPassword() {
		return proxyPassword;
	}

	public void setProxyPassword(String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}
}
