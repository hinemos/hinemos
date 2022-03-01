/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.notify.restaccess.model;

import java.util.List;

import com.clustercontrol.accesscontrol.annotation.HinemosObjectPrivilege;
import com.clustercontrol.accesscontrol.model.ObjectPrivilegeTargetInfo;
import com.clustercontrol.bean.HinemosModuleConstant;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * The persistent class for the cc_rest_access_info database table.
 * 
 */
@Entity
@Table(name="cc_rest_access_info", schema="setting")
@Cacheable(true)
@HinemosObjectPrivilege(
		objectType=HinemosModuleConstant.PLATFORM_REST_ACCESS ,
		isModifyCheck=true)
@AttributeOverride(name="objectId",
column=@Column(name="rest_access_id", insertable=false, updatable=false))
public class RestAccessInfo extends ObjectPrivilegeTargetInfo {
	private static final long serialVersionUID = 1L;
	private String restAccessId;
	private String description;
	private Long httpConnectTimeout;
	private Long httpRequestTimeout;
	private Integer httpRetryNum;
	private Boolean useWebProxy;
	private String webProxyUrlString;
	private Integer webProxyPort;
	private String webProxyAuthUser;
	private String webProxyAuthPassword;
	private Integer sendHttpMethodType;
	private String sendUrlString;
	private List<RestAccessSendHttpHeader> sendHttpHeaders;
	private String sendHttpBody;
	private Integer authType;
	private String authBasicUser;
	private String authBasicPassword;
	private Integer authUrlMethodType;
	private String authUrlString;
	private List<RestAccessAuthHttpHeader> authHttpHeaders;
	private String authUrlBody;
	private String authUrlGetRegex;
	private Long authUrlValidTerm;
	private Long regDate;
	private String regUser;
	private Long updateDate;
	private String updateUser;
	

	public RestAccessInfo() {
		super();
	}

	@Id
	@Column(name="rest_access_id")
	public String getRestAccessId() {
		return restAccessId;
	}
	public void setRestAccessId(String restAccessId) {
		this.restAccessId = restAccessId;
	}
	@Column(name="description")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@Column(name="http_connect_timeout")
	public Long getHttpConnectTimeout() {
		return httpConnectTimeout;
	}
	public void setHttpConnectTimeout(Long httpConnectTimeout) {
		this.httpConnectTimeout = httpConnectTimeout;
	}
	@Column(name="http_request_timeout")
	public Long getHttpRequestTimeout() {
		return httpRequestTimeout;
	}
	public void setHttpRequestTimeout(Long httpRequestTimeout) {
		this.httpRequestTimeout = httpRequestTimeout;
	}
	@Column(name="http_retry_num")
	public Integer getHttpRetryNum() {
		return httpRetryNum;
	}
	public void setHttpRetryNum(Integer httpRetryNum) {
		this.httpRetryNum = httpRetryNum;
	}
	@Column(name="use_web_proxy")
	public Boolean getUseWebProxy() {
		return useWebProxy;
	}
	public void setUseWebProxy(Boolean useWebProxy) {
		this.useWebProxy = useWebProxy;
	}
	@Column(name="web_proxy_url_string")
	public String getWebProxyUrlString() {
		return webProxyUrlString;
	}
	public void setWebProxyUrlString(String webProxyUrllString) {
		this.webProxyUrlString = webProxyUrllString;
	}
	@Column(name="web_proxy_port")
	public Integer getWebProxyPort() {
		return webProxyPort;
	}
	public void setWebProxyPort(Integer webProxyPort) {
		this.webProxyPort = webProxyPort;
	}
	@Column(name="web_proxy_auth_user")
	public String getWebProxyAuthUser() {
		return webProxyAuthUser;
	}
	public void setWebProxyAuthUser(String webProxyAuthUser) {
		this.webProxyAuthUser = webProxyAuthUser;
	}
	@Column(name="web_proxy_auth_password")
	public String getWebProxyAuthPassword() {
		return webProxyAuthPassword;
	}
	public void setWebProxyAuthPassword(String webProxyAuthPassword) {
		this.webProxyAuthPassword = webProxyAuthPassword;
	}
	@Column(name="send_http_method_type")
	public Integer getSendHttpMethodType() {
		return sendHttpMethodType;
	}
	public void setSendHttpMethodType(Integer sendHttpMethodType) {
		this.sendHttpMethodType = sendHttpMethodType;
	}
	@Column(name="send_url_string")
	public String getSendUrlString() {
		return sendUrlString;
	}
	public void setSendUrlString(String sendUrllString) {
		this.sendUrlString = sendUrllString;
	}
	@Column(name="send_http_body")
	public String getSendHttpBody() {
		return sendHttpBody;
	}
	public void setSendHttpBody(String sendHttpBody) {
		this.sendHttpBody = sendHttpBody;
	}
	@Column(name="auth_type")
	public Integer getAuthType() {
		return authType;
	}
	public void setAuthType(Integer authType) {
		this.authType = authType;
	}
	@Column(name="auth_basic_user")
	public String getAuthBasicUser() {
		return authBasicUser;
	}
	public void setAuthBasicUser(String authBasicUser) {
		this.authBasicUser = authBasicUser;
	}
	@Column(name="auth_basic_password")
	public String getAuthBasicPassword() {
		return authBasicPassword;
	}
	public void setAuthBasicPassword(String authBasicPassword) {
		this.authBasicPassword = authBasicPassword;
	}
	@Column(name="auth_url_method_type")
	public Integer getAuthUrlMethodType() {
		return authUrlMethodType;
	}
	public void setAuthUrlMethodType(Integer authUrlMethodType) {
		this.authUrlMethodType = authUrlMethodType;
	}
	@Column(name="auth_url_string")
	public String getAuthUrlString() {
		return authUrlString;
	}
	public void setAuthUrlString(String authUrlString) {
		this.authUrlString = authUrlString;
	}
	@Column(name="auth_url_body")
	public String getAuthUrlBody() {
		return authUrlBody;
	}
	public void setAuthUrlBody(String authUrlBody) {
		this.authUrlBody = authUrlBody;
	}
	@Column(name="auth_url_get_regex")
	public String getAuthUrlGetRegex() {
		return authUrlGetRegex;
	}
	public void setAuthUrlGetRegex(String authUrlGetRegex) {
		this.authUrlGetRegex = authUrlGetRegex;
	}
	@Column(name="auth_url_valid_term")
	public Long getAuthUrlValidTerm() {
		return authUrlValidTerm;
	}
	public void setAuthUrlValidTerm(Long authUrlValidTerm) {
		this.authUrlValidTerm = authUrlValidTerm;
	}
	@Column(name="reg_date")
	public Long getRegDate() {
		return this.regDate;
	}
	public void setRegDate(Long regDate) {
		this.regDate = regDate;
	}
	@Column(name="reg_user")
	public String getRegUser() {
		return this.regUser;
	}
	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}
	@Column(name="update_date")
	public Long getUpdateDate() {
		return this.updateDate;
	}
	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}
	@Column(name="update_user")
	public String getUpdateUser() {
		return this.updateUser;
	}
	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	@OneToMany(mappedBy="restAccessInfoEntity", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public List<RestAccessSendHttpHeader> getSendHttpHeaders() {
		return sendHttpHeaders;
	}
	public void setSendHttpHeaders(List<RestAccessSendHttpHeader> sendHttpHeaders) {
		this.sendHttpHeaders = sendHttpHeaders;
	}

	@OneToMany(mappedBy="restAccessInfoEntity", fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	public List<RestAccessAuthHttpHeader> getAuthHttpHeaders() {
		return authHttpHeaders;
	}
	public void setAuthHttpHeaders(List<RestAccessAuthHttpHeader> authHttpHeaders) {
		this.authHttpHeaders = authHttpHeaders;
	}

	@Override
	public String toString() {
		return "RestAccessInfo [restAccessId=" + restAccessId + ", description=" + description + ", httpConnectTimeout="
				+ httpConnectTimeout + ", httpRequestTimeout=" + httpRequestTimeout + ", httpRetryNum=" + httpRetryNum
				+ ", useWebProxy=" + useWebProxy + ", webProxyUrlString=" + webProxyUrlString + ", webProxyPort="
				+ webProxyPort + ", webProxyAuthUser=" + webProxyAuthUser + ", webProxyAuthPassword="
				+ webProxyAuthPassword + ", sendHttpMethodType=" + sendHttpMethodType + ", sendUrlString="
				+ sendUrlString + ", sendHttpHeaders=" + sendHttpHeaders + ", sendHttpBody=" + sendHttpBody
				+ ", authType=" + authType + ", authBasicUser=" + authBasicUser + ", authBasicPassword="
				+ authBasicPassword + ", authUrlMethodType=" + authUrlMethodType + ", authUrlString=" + authUrlString
				+ ", authHttpHeaders=" + authHttpHeaders + ", authUrlBody=" + authUrlBody + ", authUrlGetRegex="
				+ authUrlGetRegex + ", authUrlValidTerm=" + authUrlValidTerm + ", regDate=" + regDate + ", regUser="
				+ regUser + ", updateDate=" + updateDate + ", updateUser=" + updateUser + "]";
	}
	

}
