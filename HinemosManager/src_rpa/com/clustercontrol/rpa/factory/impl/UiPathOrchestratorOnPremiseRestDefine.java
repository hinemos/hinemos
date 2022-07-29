/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.factory.impl;


import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.log4j.Logger;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.rpa.factory.RpaManagementRestDefine;
import com.clustercontrol.rpa.factory.RpaManagementRestResponseHandler;
import com.clustercontrol.rpa.util.RpaUtil;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * UiPath Orchestrator(オンプレミス)のREST API向け処理を定義するクラス
 * Automation Cloudとの差分となるアクセス認証を定義
 */
public class UiPathOrchestratorOnPremiseRestDefine extends UiPathOrchestratorRestDefine {
	private static final Logger m_log = Logger.getLogger(UiPathOrchestratorOnPremiseRestDefine.class);

	public UiPathOrchestratorOnPremiseRestDefine(int apiVersion) {
		super(apiVersion);
	}

	@Override
	public RpaManagementRestResponseHandler<String> getGenerateTokenResponseHandler() {
		return new RpaManagementRestResponseHandler<String>(){
			@Override
			public String handleRpaManagementResponse(ClassicHttpResponse response) throws IOException {
				final HttpEntity responseEntity = response.getEntity();
				JsonFactory jsonFactory = new JsonFactory();
				ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
				try (InputStream inputStream = responseEntity.getContent()) {
					// "result"を取得
					return objectMapper.readTree(inputStream).get("result").textValue();
			    } catch (UnsupportedOperationException | IOException e1) {
			    	throw new ClientProtocolException(getMessage(response));
				}	
			}
		};	}

	@Override
	protected HttpUriRequest createGenerateTokenRequest(String baseUrl) {
		String url = StringUtils.join(new String[]{baseUrl, "api", "account", "authenticate"}, "/");
		return new HttpPost(url);
	}

	@Override
	protected Header[] createGenerateTokenHeader(String tenantName) {
		return new Header[]{};
	}

	@Override
	protected HttpEntity createGenerateTokenEntity(String accountId, String password, String tenantName) {
		JsonFactory jsonFactory = new JsonFactory();
		ObjectMapper objectMapper = new ObjectMapper(jsonFactory);

		Map<String, String> requestData = new HashMap<>();
		requestData.put("tenancyName", tenantName);
		requestData.put("usernameOrEmailAddress", accountId);
		requestData.put("password", password);
		try {
			return new StringEntity(objectMapper.writeValueAsString(requestData), ContentType.APPLICATION_JSON);
		} catch (JsonProcessingException e) {
			// ここには来ない想定
			m_log.warn("getGenerateTokenRequest(): " + e.getClass().getSimpleName() +  ", " + e.getMessage());
			return null;
		}
	}

	@Override
	public long getTokenExpiredMillis() {
		return HinemosPropertyCommon.rpa_management_rest_uipath_orchestrator_token_expired_millis
				.getIntegerValue();
	}
	
	// for debug
	public static void main(String[] args) {
		int apiVersion = Integer.parseInt(args[0]);
		String baseUrl = args[1];
		String accountId = args[2];
		String password = args[3];
		String tenantName = args[4];

		RpaManagementRestDefine define = new UiPathOrchestratorOnPremiseRestDefine(apiVersion);
		
		try (CloseableHttpClient client = RpaUtil.createHttpClient(10000, 10000, null, null, null, null, true)) {
			String token = define.getToken(baseUrl, accountId, password, tenantName, client);
			System.out.println(token);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
