/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.jobmanagement.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.jobmanagement.bean.JobLinkSendProtocol;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.validation.RestValidateInteger;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.jobmanagement.dto.enumtype.ProcessingMethodEnum;
import com.clustercontrol.rest.util.RestItemNameResolver;
import com.clustercontrol.util.MessageConstant;

public abstract class AbstractJobLinkSendSettingRequest implements RequestDto {

	@RestItemName(value = MessageConstant.DESCRIPTION)
	@RestValidateString(maxLen = 256)
	private String description;

	@RestItemName(value = MessageConstant.DESTINATION_SCOPE)
	@RestValidateString(notNull = true)
	private String facilityId;

	@RestItemName(value = MessageConstant.DESTINATION_SCOPE_SUCCESS_ANY_NODE)
	@RestBeanConvertEnum
	@RestValidateObject(notNull = true)
	private ProcessingMethodEnum processMode;

	@RestItemName(value = MessageConstant.DESTINATION_PROTOCOL)
	@RestBeanConvertEnum
	@RestValidateObject(notNull = true)
	private JobLinkSendProtocol protocol;

	@RestItemName(value = MessageConstant.DESTINATION_PORT)
	@RestValidateInteger(notNull = true, minVal = 1, maxVal = 65535)
	private Integer port;

	@RestItemName(value = MessageConstant.HINEMOS_USER)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 64)
	private String hinemosUserId;

	@RestItemName(value = MessageConstant.HINEMOS_PASSWORD)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 64)
	private String hinemosPassword;

	private Boolean proxyFlg = Boolean.FALSE;

	@RestItemName(value = MessageConstant.HTTP_PROXY_HOST)
	@RestValidateString(maxLen = 1024)
	private String proxyHost;

	@RestItemName(value = MessageConstant.HTTP_PROXY_PORT)
	@RestValidateInteger(minVal = 1, maxVal = 65535)
	private Integer proxyPort;

	@RestItemName(value = MessageConstant.HTTP_PROXY_USER)
	@RestValidateString(maxLen = 64)
	private String proxyUser;

	@RestItemName(value = MessageConstant.HTTP_PROXY_PASSWORD)
	@RestValidateString(maxLen = 128)
	private String proxyPassword;

	public AbstractJobLinkSendSettingRequest() {
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	@Override
	public void correlationCheck() throws InvalidSetting {
		// [HTTP Proxyを使用する]がtrueの場合
		if (proxyFlg) {
			// [HTTP Proxyホスト]必須
			if (proxyHost == null || proxyHost.isEmpty()) {
				String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "proxyHost");
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
			}
			// [HTTP Proxyポート]必須
			if (proxyPort == null) {
				String r1 = RestItemNameResolver.resolveItenName(this.getClass(), "proxyPort");
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_INPUT.getMessage(r1));
			}
		}
	}
}
