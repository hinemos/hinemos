/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.common.dto;

import java.util.List;

import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.commons.util.StringUtil;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.validation.RestValidateInteger;
import com.clustercontrol.rest.annotation.validation.RestValidateLong;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.endpoint.common.dto.enumtype.HttpMethodTypeEnum;
import com.clustercontrol.rest.endpoint.common.dto.enumtype.RestAccessAuthTypeEnum;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class ModifyRestAccessInfoRequest implements RequestDto {

	@RestItemName(value = MessageConstant.DESCRIPTION)
	private String description;

	@RestItemName(value = MessageConstant.REST_ACCESS_CONNECTTIMEOUT_TIME)
	@RestValidateLong(notNull = true, minVal=1, maxVal=Long.MAX_VALUE)
	private Long httpConnectTimeout;
	
	@RestItemName(value = MessageConstant.REST_ACCESS_REQUESTTIMEOUT_TIME)
	@RestValidateLong(notNull = true, minVal=1, maxVal=Long.MAX_VALUE)
	private Long httpRequestTimeout;

	@RestItemName(value = MessageConstant.REST_ACCESS_REQUEST_RETRY_NUM)
	@RestValidateInteger(notNull = true, minVal=0, maxVal=Short.MAX_VALUE)
	private Integer httpRetryNum;

	@RestItemName(value = MessageConstant.REST_ACCESS_USE_PROXY)
	@RestValidateObject(notNull = true)
	private Boolean useWebProxy;

	@RestItemName(value = MessageConstant.REST_ACCESS_PROXYURL)
	private String webProxyUrlString;

	@RestItemName(value = MessageConstant.REST_ACCESS_PROXYPORT)
	@RestValidateInteger(minVal=0, maxVal=65535)
	private Integer webProxyPort;

	@RestItemName(value = MessageConstant.REST_ACCESS_PROXYUSER)
	private String webProxyAuthUser;

	@RestItemName(value = MessageConstant.REST_ACCESS_PROXYPASSWORD)
	private String webProxyAuthPassword;

	@RestBeanConvertEnum
	@RestItemName(value = MessageConstant.REST_ACCESS_SEND_METHOD)
	@RestValidateObject(notNull = true)
	private HttpMethodTypeEnum sendHttpMethodType;

	@RestItemName(value = MessageConstant.REST_ACCESS_SEND_URL)
	@RestValidateString(notNull = true, minLen = 1)
	private String sendUrlString;

	@RestItemName(value = MessageConstant.REST_ACCESS_SEND_HEADER)
	private List<RestAccessSendHttpHeaderRequest> sendHttpHeaders;

	@RestItemName(value = MessageConstant.REST_ACCESS_SEND_BODY)
	private String sendHttpBody;

	@RestBeanConvertEnum
	@RestItemName(value = MessageConstant.REST_ACCESS_AUTH_TYPE)
	@RestValidateObject(notNull = true)
	private RestAccessAuthTypeEnum authType;

	@RestItemName(value = MessageConstant.BASIC_AUTH_USER)
	private String authBasicUser;

	@RestItemName(value = MessageConstant.BASIC_AUTH_PASSWORD)
	private String authBasicPassword;

	@RestBeanConvertEnum
	@RestItemName(value = MessageConstant.REST_ACCESS_AUTH_METHOD)
	private HttpMethodTypeEnum authUrlMethodType;

	@RestItemName(value = MessageConstant.REST_ACCESS_AUTH_URL)
	private String authUrlString;

	@RestItemName(value = MessageConstant.REST_ACCESS_AUTH_HEADER)
	private List<RestAccessAuthHttpHeaderRequest> authHttpHeaders;

	@RestItemName(value = MessageConstant.REST_ACCESS_AUTH_BODY)
	private String authUrlBody;
	
	@RestItemName(value = MessageConstant.REST_ACCESS_TOKEN_GET_REGEX)
	private String authUrlGetRegex;

	@RestItemName(value = MessageConstant.REST_ACCESS_TOKEN_VALID_TERM)
	@RestValidateLong(minVal=0, maxVal= Long.MAX_VALUE)
	private Long authUrlValidTerm;

	public ModifyRestAccessInfoRequest() {
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

	public List<RestAccessSendHttpHeaderRequest> getSendHttpHeaders() {
		return sendHttpHeaders;
	}

	public void setSendHttpHeaders(List<RestAccessSendHttpHeaderRequest> sendHttpHeaders) {
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

	public List<RestAccessAuthHttpHeaderRequest> getAuthHttpHeaders() {
		return authHttpHeaders;
	}

	public void setAuthHttpHeaders(List<RestAccessAuthHttpHeaderRequest> authHttpHeaders) {
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

	@Override
	public void correlationCheck() throws InvalidSetting {

		// 認証タイプ組み合わせチェック
		if (authType == RestAccessAuthTypeEnum.URL) {
			if (StringUtil.isNullOrEmpty(authUrlString)) {
				String[] args = { MessageConstant.REST_ACCESS_AUTH_TYPE_URL.getMessage(),
						MessageConstant.REST_ACCESS_AUTH_URL.getMessage() };
				throw new InvalidSetting(
						MessageConstant.MESSAGE_PLEASE_SET_REST_ACCESS_FOR_SPECIFIC_AUTH_TYPE.getMessage(args));
			}
			if (authUrlMethodType == null) {
				String[] args = { MessageConstant.REST_ACCESS_AUTH_TYPE_URL.getMessage(),
						MessageConstant.REST_ACCESS_AUTH_METHOD.getMessage() };
				throw new InvalidSetting(
						MessageConstant.MESSAGE_PLEASE_SET_REST_ACCESS_FOR_SPECIFIC_AUTH_TYPE.getMessage(args));
			}
			if (StringUtil.isNullOrEmpty(authUrlGetRegex)) {
				String[] args = { MessageConstant.REST_ACCESS_AUTH_TYPE_URL.getMessage(),
						MessageConstant.REST_ACCESS_TOKEN_GET_REGEX.getMessage() };
				throw new InvalidSetting(
						MessageConstant.MESSAGE_PLEASE_SET_REST_ACCESS_FOR_SPECIFIC_AUTH_TYPE.getMessage(args));
			}
		} else if (authType == RestAccessAuthTypeEnum.BASIC) {
			if (StringUtil.isNullOrEmpty(authBasicUser)) {
				String[] args = { MessageConstant.REST_ACCESS_AUTH_TYPE_BASIC.getMessage(),
						MessageConstant.BASIC_AUTH_USER.getMessage() };
				throw new InvalidSetting(
						MessageConstant.MESSAGE_PLEASE_SET_REST_ACCESS_FOR_SPECIFIC_AUTH_TYPE.getMessage(args));
			}
			if (StringUtil.isNullOrEmpty(authBasicPassword)) {
				String[] args = { MessageConstant.REST_ACCESS_AUTH_TYPE_BASIC.getMessage(),
						MessageConstant.BASIC_AUTH_PASSWORD.getMessage() };
				throw new InvalidSetting(
						MessageConstant.MESSAGE_PLEASE_SET_REST_ACCESS_FOR_SPECIFIC_AUTH_TYPE.getMessage(args));
			}
		}

		// 送信メソッド組み合わせチェック
		if (sendHttpMethodType == HttpMethodTypeEnum.POST || sendHttpMethodType == HttpMethodTypeEnum.PUT) {
			if (sendHttpBody == null || sendHttpBody.isEmpty()) {
				String[] args = { MessageConstant.REST_ACCESS_SEND_BODY.getMessage() };
				throw new InvalidSetting(
						MessageConstant.MESSAGE_PLEASE_SET_REST_ACCESS_FOR_SPECIFIC_SEND_METHOD.getMessage(args));
			}
		}
		// 認証メソッド組み合わせチェック
		if (authUrlMethodType == HttpMethodTypeEnum.POST || authUrlMethodType == HttpMethodTypeEnum.PUT) {
			if (authUrlBody == null || authUrlBody.isEmpty()) {
				String[] args = { MessageConstant.REST_ACCESS_AUTH_BODY.getMessage() };
				throw new InvalidSetting(
						MessageConstant.MESSAGE_PLEASE_SET_REST_ACCESS_FOR_SPECIFIC_AUTH_METHOD.getMessage(args));
			}
		}
		// Proxy利用組み合わせチェック
		if (useWebProxy != null && useWebProxy == true) {
			if (StringUtil.isNullOrEmpty(webProxyUrlString)) {
				String[] args = { MessageConstant.REST_ACCESS_PROXYURL.getMessage() };
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_REST_ACCESS_FOR_USE_PROXY.getMessage(args));
			}
			if (webProxyPort == null) {
				String[] args = { MessageConstant.REST_ACCESS_PROXYPORT.getMessage() };
				throw new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_REST_ACCESS_FOR_USE_PROXY.getMessage(args));
			}
			// Proxy認証組み合わせチェック
			if (!(StringUtil.isNullOrEmpty(webProxyAuthUser)) || !(StringUtil.isNullOrEmpty(webProxyAuthPassword))) {
				if (StringUtil.isNullOrEmpty(webProxyAuthUser) || StringUtil.isNullOrEmpty(webProxyAuthPassword)) {
					String[] args = { MessageConstant.REST_ACCESS_PROXYPASSWORD.getMessage() + ","
							+ MessageConstant.REST_ACCESS_PROXYUSER.getMessage() };
					throw new InvalidSetting(
							MessageConstant.MESSAGE_PLEASE_SET_REST_ACCESS_FOR_PROXY_AUTH.getMessage(args));
				}
			}
		}
		// 特殊文字列チェック （正規表現）
		CommonValidator.validateRegex(MessageConstant.REST_ACCESS_TOKEN_GET_REGEX.getMessage(), authUrlGetRegex, false);

	}

}
