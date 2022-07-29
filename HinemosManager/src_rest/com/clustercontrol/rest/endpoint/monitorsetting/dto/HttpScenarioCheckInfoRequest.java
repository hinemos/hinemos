/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class HttpScenarioCheckInfoRequest implements RequestDto {
	private String authType;
	private String authUser;
	private String authPassword;
	private Boolean proxyFlg;
	private String proxyUrl;
	private Integer proxyPort;
	private String proxyUser;
	private String proxyPassword;
	private Boolean monitoringPerPageFlg;
	private String userAgent;
	private Integer connectTimeout;
	private Integer requestTimeout;
	private List<PageRequest> pages = new ArrayList<>();
	public HttpScenarioCheckInfoRequest() {
	}

	
	public String getAuthType() {
		return authType;
	}


	public void setAuthType(String authType) {
		this.authType = authType;
	}


	public String getAuthUser() {
		return authUser;
	}


	public void setAuthUser(String authUser) {
		this.authUser = authUser;
	}


	public String getAuthPassword() {
		return authPassword;
	}


	public void setAuthPassword(String authPassword) {
		this.authPassword = authPassword;
	}


	public Boolean getProxyFlg() {
		return proxyFlg;
	}


	public void setProxyFlg(Boolean proxyFlg) {
		this.proxyFlg = proxyFlg;
	}


	public String getProxyUrl() {
		return proxyUrl;
	}


	public void setProxyUrl(String proxyUrl) {
		this.proxyUrl = proxyUrl;
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


	public Boolean getMonitoringPerPageFlg() {
		return monitoringPerPageFlg;
	}


	public void setMonitoringPerPageFlg(Boolean monitoringPerPageFlg) {
		this.monitoringPerPageFlg = monitoringPerPageFlg;
	}


	public String getUserAgent() {
		return userAgent;
	}


	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}


	public Integer getConnectTimeout() {
		return connectTimeout;
	}


	public void setConnectTimeout(Integer connectTimeout) {
		this.connectTimeout = connectTimeout;
	}


	public Integer getRequestTimeout() {
		return requestTimeout;
	}


	public void setRequestTimeout(Integer requestTimeout) {
		this.requestTimeout = requestTimeout;
	}


	public List<PageRequest> getPages() {
		return pages;
	}


	public void setPages(List<PageRequest> pages) {
		this.pages = pages;
	}


	@Override
	public String toString() {
		return "HttpScenarioCheckInfoRequest [authType=" + authType + ", authUser=" + authUser + ", authPassword="
				+ authPassword + ", proxyFlg=" + proxyFlg + ", proxyUrl=" + proxyUrl + ", proxyPort=" + proxyPort
				+ ", proxyUser=" + proxyUser + ", proxyPassword=" + proxyPassword + ", monitoringPerPageFlg="
				+ monitoringPerPageFlg + ", userAgent=" + userAgent + ", connectTimeout=" + connectTimeout
				+ ", requestTimeout=" + requestTimeout + ", pages=" + pages + "]";
	}


	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}