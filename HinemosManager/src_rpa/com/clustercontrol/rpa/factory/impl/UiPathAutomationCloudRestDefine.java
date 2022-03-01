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

import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.log4j.Logger;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.rpa.factory.RpaManagementRestDefine;
import com.clustercontrol.rpa.factory.RpaManagementRestResponseHandler;
import com.clustercontrol.rpa.util.RpaUtil;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * UiPath Automation CloudのREST API向け処理を定義するクラス
 * Automation Orchestrator(オンプレミス)との差分となるアクセス認証を定義
 */
public class UiPathAutomationCloudRestDefine extends UiPathOrchestratorRestDefine {
	private static final Logger m_log = Logger.getLogger(UiPathOrchestratorRestDefine.class);

	// token取得用URL
	// https://docs.uipath.com/orchestrator/lang-ja/reference/consuming-cloud-api
	private final static String generateTokenUrl = "https://account.uipath.com/oauth/token";

	public UiPathAutomationCloudRestDefine(int apiVersion) {
		super(apiVersion);
	}

	@Override
	protected HttpUriRequest createGenerateTokenRequest(String baseUrl) {
		return new HttpPost(generateTokenUrl);
	}

	@Override
	protected Header[] createGenerateTokenHeader(String tenantName) {
		return new Header[]{new BasicHeader("X-UIPATH-TenantName", tenantName)};
	}

	@Override
	protected HttpEntity createGenerateTokenEntity(String accountId, String password, String tenantName) {
		JsonFactory jsonFactory = new JsonFactory();
		ObjectMapper objectMapper = new ObjectMapper(jsonFactory);

		// Request Body
		// accountId:client_id
		// password:user_key
		Map<String, String> requestData = new HashMap<>();
		requestData.put("grant_type", "refresh_token");
		requestData.put("client_id", accountId);
		requestData.put("refresh_token", password);
		try {
			return new StringEntity(objectMapper.writeValueAsString(requestData), ContentType.APPLICATION_JSON);
		} catch (JsonProcessingException e) {
			// ここには来ない想定
			m_log.warn("getGenerateTokenRequest(): " + e.getClass().getSimpleName() +  ", " + e.getMessage());
			return null;
		}
	}

	@Override
	protected RpaManagementRestResponseHandler<String> getGenerateTokenResponseHandler() {
		return new RpaManagementRestResponseHandler<String>(){
			@Override
			public String handleRpaManagementResponse(ClassicHttpResponse response) throws IOException {
				final HttpEntity responseEntity = response.getEntity();
				JsonFactory jsonFactory = new JsonFactory();
				ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
				try (InputStream inputStream = responseEntity.getContent()) {
					// "access_token"を取得
					return objectMapper.readTree(inputStream).get("access_token").textValue();
			    } catch (UnsupportedOperationException | IOException e1) {
			    	throw new ClientProtocolException(getMessage(response));
				}	
			}
		};
	}

	@Override
	public long getTokenExpiredMillis() {
		return HinemosPropertyCommon.rpa_management_rest_uipath_orchestrator_cloud_token_expired_millis
				.getIntegerValue();
	}
	
	public static void main(String[] args) {
		int apiVersion = Integer.parseInt(args[0]);
		String baseUrl = args[1];
		String accountId = args[2];
		String password = args[3];
		String tenantName = args[4];

		RpaManagementRestDefine define = new UiPathAutomationCloudRestDefine(apiVersion);
		
		try (CloseableHttpClient client = RpaUtil.createHttpClient(10000, 10000, null, null, null, null, true)) {
			String token = define.getToken(baseUrl, accountId, password, tenantName, client);
			System.out.println(token);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
