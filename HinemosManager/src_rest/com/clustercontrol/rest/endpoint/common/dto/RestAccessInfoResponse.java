/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.common.dto;

import java.util.List;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.cmdtool.DatetimeTypeParam;
import com.clustercontrol.rest.endpoint.common.dto.enumtype.HttpMethodTypeEnum;
import com.clustercontrol.rest.endpoint.common.dto.enumtype.RestAccessAuthTypeEnum;

public class RestAccessInfoResponse {
	private String restAccessId;
	private String ownerRoleId;
	private String description;
	private Long httpConnectTimeout;
	private Long httpRequestTimeout;
	private Integer httpRetryNum;
	private Boolean useWebProxy;
	private String webProxyUrlString;
	private Integer webProxyPort;
	private String webProxyAuthUser;
	private String webProxyAuthPassword;
	@RestBeanConvertEnum
	private HttpMethodTypeEnum sendHttpMethodType;
	private String sendUrlString;
	private List<RestAccessSendHttpHeaderResponse> sendHttpHeaders;
	private String sendHttpBody;
	@RestBeanConvertEnum
	private RestAccessAuthTypeEnum authType;
	private String authBasicUser;
	private String authBasicPassword;
	@RestBeanConvertEnum
	private HttpMethodTypeEnum authUrlMethodType;
	private String authUrlString;
	private List<RestAccessAuthHttpHeaderResponse> authHttpHeaders;
	private String authUrlBody;
	private String authUrlGetRegex;
	private Long authUrlValidTerm;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String regDate;
	private String regUser;
	@RestBeanConvertDatetime
	@DatetimeTypeParam
	private String updateDate;
	private String updateUser;

	public RestAccessInfoResponse() {
	}
	
	public String getRestAccessId() {
		return restAccessId;
	}
	public void setRestAccessId(String restAccessId) {
		this.restAccessId = restAccessId;
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
	public Long getHttpConnectTimeout() {
		return httpConnectTimeout;
	}
	public void setHttpConnectTimeout(Long httpConnectTimeout) {
		this.httpConnectTimeout = httpConnectTimeout;
	}
	public Long getHttpRequestTimeout() {
		return httpRequestTimeout;
	}
	public void setHttpRequestTimeout(Long httpRequestTimeout) {
		this.httpRequestTimeout = httpRequestTimeout;
	}
	public Integer getHttpRetryNum() {
		return httpRetryNum;
	}
	public void setHttpRetryNum(Integer httpRetryNum) {
		this.httpRetryNum = httpRetryNum;
	}
	public Boolean getUseWebProxy() {
		return useWebProxy;
	}
	public void setUseWebProxy(Boolean useWebProxy) {
		this.useWebProxy = useWebProxy;
	}
	public String getWebProxyUrlString() {
		return webProxyUrlString;
	}
	public void setWebProxyUrlString(String webProxyUrlString) {
		this.webProxyUrlString = webProxyUrlString;
	}
	public Integer getWebProxyPort() {
		return webProxyPort;
	}
	public void setWebProxyPort(Integer webProxyPort) {
		this.webProxyPort = webProxyPort;
	}
	public String getWebProxyAuthUser() {
		return webProxyAuthUser;
	}
	public void setWebProxyAuthUser(String webProxyAuthUser) {
		this.webProxyAuthUser = webProxyAuthUser;
	}
	public String getWebProxyAuthPassword() {
		return webProxyAuthPassword;
	}
	public void setWebProxyAuthPassword(String webProxyAuthPassword) {
		this.webProxyAuthPassword = webProxyAuthPassword;
	}
	public HttpMethodTypeEnum getSendHttpMethodType() {
		return sendHttpMethodType;
	}
	public void setSendHttpMethodType(HttpMethodTypeEnum sendHttpMethodType) {
		this.sendHttpMethodType = sendHttpMethodType;
	}
	public String getSendUrlString() {
		return sendUrlString;
	}
	public void setSendUrlString(String sendUrlString) {
		this.sendUrlString = sendUrlString;
	}
	public List<RestAccessSendHttpHeaderResponse> getSendHttpHeaders() {
		return sendHttpHeaders;
	}
	public void setSendHttpHeaders(List<RestAccessSendHttpHeaderResponse> sendHttpHeaders) {
		this.sendHttpHeaders = sendHttpHeaders;
	}
	public String getSendHttpBody() {
		return sendHttpBody;
	}
	public void setSendHttpBody(String sendHttpBody) {
		this.sendHttpBody = sendHttpBody;
	}
	public RestAccessAuthTypeEnum getAuthType() {
		return authType;
	}
	public void setAuthType(RestAccessAuthTypeEnum authType) {
		this.authType = authType;
	}
	public String getAuthBasicUser() {
		return authBasicUser;
	}
	public void setAuthBasicUser(String authBasicUser) {
		this.authBasicUser = authBasicUser;
	}
	public String getAuthBasicPassword() {
		return authBasicPassword;
	}
	public void setAuthBasicPassword(String authBasicPassword) {
		this.authBasicPassword = authBasicPassword;
	}
	public HttpMethodTypeEnum getAuthUrlMethodType() {
		return authUrlMethodType;
	}
	public void setAuthUrlMethodType(HttpMethodTypeEnum authUrlMethodType) {
		this.authUrlMethodType = authUrlMethodType;
	}
	public String getAuthUrlString() {
		return authUrlString;
	}
	public void setAuthUrlString(String authUrlString) {
		this.authUrlString = authUrlString;
	}
	public List<RestAccessAuthHttpHeaderResponse> getAuthHttpHeaders() {
		return authHttpHeaders;
	}
	public void setAuthHttpHeaders(List<RestAccessAuthHttpHeaderResponse> authHttpHeaders) {
		this.authHttpHeaders = authHttpHeaders;
	}
	public String getAuthUrlBody() {
		return authUrlBody;
	}
	public void setAuthUrlBody(String authUrlBody) {
		this.authUrlBody = authUrlBody;
	}
	public String getAuthUrlGetRegex() {
		return authUrlGetRegex;
	}
	public void setAuthUrlGetRegex(String authUrlGetRegex) {
		this.authUrlGetRegex = authUrlGetRegex;
	}
	public Long getAuthUrlValidTerm() {
		return authUrlValidTerm;
	}
	public void setAuthUrlValidTerm(Long authUrlValidTerm) {
		this.authUrlValidTerm = authUrlValidTerm;
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

}
