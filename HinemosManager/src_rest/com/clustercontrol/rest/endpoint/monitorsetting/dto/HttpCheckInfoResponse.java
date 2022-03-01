/*
 * 
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

public class HttpCheckInfoResponse {
	private String proxyHost;
	private Integer proxyPort;
	private Boolean proxySet;
	private String requestUrl;
	private Integer timeout;
	private Boolean urlReplace;

	public HttpCheckInfoResponse() {
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
	public Boolean getProxySet() {
		return proxySet;
	}
	public void setProxySet(Boolean proxySet) {
		this.proxySet = proxySet;
	}
	public String getRequestUrl() {
		return requestUrl;
	}
	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}
	public Integer getTimeout() {
		return timeout;
	}
	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}
	public Boolean getUrlReplace() {
		return urlReplace;
	}
	public void setUrlReplace(Boolean urlReplace) {
		this.urlReplace = urlReplace;
	}
	@Override
	public String toString() {
		return "HttpCheckInfo [proxyHost=" + proxyHost
				+ ", proxyPort=" + proxyPort + ", proxySet=" + proxySet + ", requestUrl=" + requestUrl + ", timeout="
				+ timeout + ", urlReplace=" + urlReplace + "]";
	}

}