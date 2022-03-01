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
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.log4j.Logger;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.rpa.factory.RpaManagementRestDefine;
import com.clustercontrol.rpa.factory.RpaManagementRestResponseHandler;
import com.clustercontrol.rpa.factory.bean.RpaResourceInfo;
import com.clustercontrol.rpa.util.RpaUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * WinActor Manager on CloudのREST API向けの処理を定義するクラス
 */
public class WinactorManagerOnCloudRestDefine extends RpaManagementRestDefine {
	private static final Logger m_log = Logger.getLogger(WinactorManagerOnCloudRestDefine.class);

	// URLに含まれるAPIバージョン
	private final String winActorApiVersion;

	public WinactorManagerOnCloudRestDefine(int apiVersion) {
		super(apiVersion);
		if (apiVersion == 1) {
			winActorApiVersion = "v1.2";
		} else {
			winActorApiVersion = "";
		}		
	}

	protected HttpUriRequest createGenerateTokenRequest(String baseUrl) {
		String url = StringUtils.join(new String[]{baseUrl, winActorApiVersion, "tokens"}, "/");
		return new HttpPost(url);
	}

	@Override
	protected Header[] createGenerateTokenHeader(String tenantName) {
		return null;
	}

	protected HttpEntity createGenerateTokenEntity(String accountId, String password, String tenantName) {
		JsonFactory jsonFactory = new JsonFactory();
		ObjectMapper objectMapper = new ObjectMapper(jsonFactory);

		// Request Body
		Map<String, String> requestData = new HashMap<>();
		requestData.put("name", accountId);
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
	protected RpaManagementRestResponseHandler<String> getGenerateTokenResponseHandler() {
		return new RpaManagementRestResponseHandler<String>(){
			@Override
			public String handleRpaManagementResponse(ClassicHttpResponse response) throws IOException {
				final HttpEntity responseEntity = response.getEntity();
				JsonFactory jsonFactory = new JsonFactory();
				ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
				try (InputStream inputStream = responseEntity.getContent()) {
					// "token"を取得
					return objectMapper.readTree(inputStream).get("token").textValue();
			    } catch (UnsupportedOperationException | IOException e1) {
			    	throw new ClientProtocolException(getMessage(response));
				}	
			}
		};
	}

	@Override
	public long getTokenExpiredMillis() {
		return HinemosPropertyCommon.rpa_management_rest_winactor_manager_on_cloud_token_expired_millis
				.getIntegerValue();
	}

	private Header getAuthTokenHeader(String token) {
		return new BasicHeader("Authorization", token);
	}
	
	@Override
	public List<RpaResourceInfo> getRpaResourceInfo(String baseUrl, String token, HttpClient client) throws IOException {
		// GETリクエスト		
		HttpUriRequest getDevicesRequest = 
		createRequest(new HttpGet(StringUtils.join(new String[]{baseUrl, winActorApiVersion, "winactors"}, "/")),
				new Header[] {getAuthTokenHeader(token)}, null);

		// ハンドラ
		RpaManagementRestResponseHandler<List<RpaResourceInfo>> getDevicesResponseHandler = 
				new RpaManagementRestResponseHandler<List<RpaResourceInfo>>() {
					@Override
					public List<RpaResourceInfo> handleRpaManagementResponse(ClassicHttpResponse response) throws IOException {
						final HttpEntity responseEntity = response.getEntity();
						JsonFactory jsonFactory = new JsonFactory();
						ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
						try (InputStream inputStream = responseEntity.getContent()) {
							// "items"から各WinActorの情報を取得
							JsonNode value = objectMapper.readTree(inputStream).get("items");
							List<RpaResourceInfo> ret = new ArrayList<>();
							
							for (Iterator<JsonNode> itr = value.elements();itr.hasNext();) {
								// RpaResourceInfoに格納
								JsonNode winactor = itr.next();
								String pcName = winactor.get("pcName").textValue();
								String userName =  winactor.get("userName").textValue();
								String id = winactor.get("id").textValue();
								
								RpaResourceInfo resourceInfo = new RpaResourceInfo();
								resourceInfo.setFacilityName(pcName);
								resourceInfo.setIpAddress("");
								resourceInfo.setNodeName(pcName);
								resourceInfo.setHostName(pcName);
								resourceInfo.setRpaUser(userName);
								resourceInfo.setRpaExecEnvId(id);
								
								ret.add(resourceInfo);
							}
							return ret;
					    } catch (UnsupportedOperationException | IOException e1) {
					    	throw new ClientProtocolException(getMessage(response));
						}	
					}
				};

		return client.execute(getDevicesRequest, getDevicesResponseHandler);
	}

	@Override
	public boolean enabledRpaResourceDetection() {
		return true;
	}

	@Override
	protected HttpUriRequest createHealthCheckRequest(String baseUrl) {
		return new HttpGet(StringUtils.join(new String[]{baseUrl, winActorApiVersion, "winactors"}, "/"));
	}

	@Override
	protected Header[] createHealthCheckHeader(String token) {
		return new Header[] {getAuthTokenHeader(token)};
	}

	@Override
	protected HttpEntity createHealthCheckEntity() {
		return null;
	}

	@Override
	protected RpaManagementRestResponseHandler<String> getHealthCheckResponseHandler() {
		return new RpaManagementRestResponseHandler<String>(){
			@Override
			public String handleRpaManagementResponse(ClassicHttpResponse response) throws IOException {
				final HttpEntity responseEntity = response.getEntity();
				JsonFactory jsonFactory = new JsonFactory();
				ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
				try (InputStream inputStream = responseEntity.getContent()) {
					// "total"(WinActor数)を取得
					return String.valueOf(objectMapper.readTree(inputStream).get("total").longValue());
			    } catch (UnsupportedOperationException | IOException e1) {
			    	throw new ClientProtocolException(getMessage(response));
				}	
			}
		};
	}

	@Override
	public MessageConstant getHealthCheckInfo() {
		return MessageConstant.REGISTERED_WINACTOR_NUMBER;
	}

	@Override
	protected HttpUriRequest createRunRequest(String baseUrl, Integer runType) {
		String url = StringUtils.join(new String[]{baseUrl, winActorApiVersion, "schedules"}, "/");
		return new HttpPost(url);
	}

	@Override
	protected Header[] createRunHeader(String token) {
		return new Header[] { getAuthTokenHeader(token) };
	}

	@Override
	protected HttpEntity createRunEntity(Map<String, Object> requestData, Integer runType) {
		JsonFactory jsonFactory = new JsonFactory();
		ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
		String requestJson = null;
		try {
			requestJson = objectMapper.writeValueAsString(requestData);
			m_log.debug("createRunEntity() : requestJson=" + requestJson);
		} catch (JsonProcessingException e) {
			// ここには来ない想定
			m_log.warn("createRunEntity(): " + e.getClass().getSimpleName() + ", " + e.getMessage());
			return null;
		}
		return new StringEntity(requestJson, ContentType.APPLICATION_JSON);
	}

	@Override
	protected RpaManagementRestResponseHandler<String> getRunResponseHandler(Map<String, Object> requestData) {
		return new RpaManagementRestResponseHandler<String>() {
			@Override
			public String handleRpaManagementResponse(ClassicHttpResponse response) throws IOException {
				final HttpEntity responseEntity = response.getEntity();
				JsonFactory jsonFactory = new JsonFactory();
				ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
				try (InputStream inputStream = responseEntity.getContent()) {
					JsonNode rootNode = objectMapper.readTree(inputStream);
					// エラーの場合、例外をthrow
					if (rootNode.has("error")) {
						throw new ClientProtocolException(getMessage(response));
					}
					// 実行結果確認ではタスク名を使用する
					return String.valueOf(requestData.get("name"));
				} catch (UnsupportedOperationException | IOException e1) {
					throw new ClientProtocolException(getMessage(response));
				}
			}
		};
	}

	/**
	 * WinActor Manager on Cloudではシナリオ実行、実行結果確認は以下のように行います。
	 * <dl>
	 * <dt>1. シナリオ実行</dt>
	 * <dd>
	 * 即時実行スケジュールAPIからスケジュールを登録する。
	 * WinActor Manager on Cloud上でスケジュール名と同名のタスクが生成し、シナリオが実行される。
	 * </dd>
	 * <dt>2. シナリオ実行結果確認</dt>
	 * <dd>
	 * タスク情報取得APIからタスクの状態を取得する。
	 * 1のレスポンスにはタスクIDは含まれないため、タスク名を指定してタスクの状態を取得する。
	 * そのためタスク名が一意となるようにタイムスタンプを付与してスケジュールを登録する。
	 * </dd>
	 * </dl>
	 */
	@Override
	protected Map<String, Object> adjustRunRequestData(Map<String, Object> requestData) {
		// スケジュール名を一意にする必要があるためタイムスタンプを付与する
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		requestData.put("name", requestData.get("name") + "-" + sdf.format(HinemosTime.currentTimeMillis()));
		return requestData;
	}

	@Override
	protected HttpUriRequest createCheckRequest(String baseUrl, String runIdentifier) {
		HttpGet httpGet = new HttpGet(StringUtils.join(new String[]{baseUrl, winActorApiVersion, "tasks"}, "/"));
		// リクエストパラメータを設定
		List<NameValuePair> nameValuePairs = new ArrayList<>();
		// タスク名の完全一致を条件に指定する
		nameValuePairs.add(new BasicNameValuePair("name", runIdentifier));
		nameValuePairs.add(new BasicNameValuePair("nameType", "perfect"));
		try {
			httpGet.setUri(new URIBuilder(httpGet.getUri()).addParameters(nameValuePairs).build());
		} catch (URISyntaxException e) {
			// ここには来ない想定
			m_log.warn("createCheckRequest(): " + e.getClass().getSimpleName() + ", " + e.getMessage());
			return null;
		}
		return httpGet;
	}

	@Override
	protected Header[] createCheckHeader(String token) {
		return new Header[] { getAuthTokenHeader(token) };
	}

	@Override
	protected HttpEntity createCheckEntity(String runIdentifier) {
		return null;
	}

	@Override
	protected RpaManagementRestResponseHandler<String> getCheckResponseHandler() {
		return new RpaManagementRestResponseHandler<String>() {
			@Override
			public String handleRpaManagementResponse(ClassicHttpResponse response) throws IOException {
				final HttpEntity responseEntity = response.getEntity();
				JsonFactory jsonFactory = new JsonFactory();
				ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
				try (InputStream inputStream = responseEntity.getContent()) {
					JsonNode rootNode = objectMapper.readTree(inputStream);
					// エラーの場合、例外をthrow
					if (rootNode.has("error")) {
						throw new ClientProtocolException(getMessage(response));
					}
					// "status"を取得
					String status = rootNode.get("items").get(0).get("status").textValue();
					m_log.debug("handleRpaManagementResponse() : status=" + status);
					return status;
				} catch (UnsupportedOperationException | IOException e1) {
					throw new ClientProtocolException(getMessage(response));
				}
			}
		};
	}

	@Override
	protected HttpUriRequest createCancelRequest(String baseUrl, String runIdentifier) {
		HttpPut httpPut = new HttpPut(StringUtils.join(new String[]{baseUrl, winActorApiVersion, "tasks", runIdentifier, "cancel"}, "/"));
		try {
			httpPut.setUri(new URIBuilder(httpPut.getUri()).build());
		} catch (URISyntaxException e) {
			// ここには来ない想定
			m_log.warn("createCheckRequest(): " + e.getClass().getSimpleName() + ", " + e.getMessage());
			return null;
		}
		return httpPut;
	}

	@Override
	protected Header[] createCancelHeader(String token) {
		return new Header[] { getAuthTokenHeader(token) };
	}

	@Override
	protected HttpEntity createCancelEntity(String runIdentifier, Integer stopMode) {
		return null;
	}

	@Override
	protected RpaManagementRestResponseHandler<String> getCancelResponseHandler() {
		return new RpaManagementRestResponseHandler<String>() {
			@Override
			public String handleRpaManagementResponse(ClassicHttpResponse response) throws IOException {
				final HttpEntity responseEntity = response.getEntity();
				JsonFactory jsonFactory = new JsonFactory();
				ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
				try (InputStream inputStream = responseEntity.getContent()) {
					JsonNode rootNode = objectMapper.readTree(inputStream);
					// エラーの場合、例外をthrow
					if (rootNode.has("error")) {
						throw new ClientProtocolException(getMessage(response));
					}
					return "";  // 正常の場合レスポンスはステータスコードのみのため空文字列を返す
				} catch (UnsupportedOperationException | IOException e1) {
					throw new ClientProtocolException(getMessage(response));
				}
			}
		};
	}
	
	@Override
	public String cancel(String baseUrl, String token, String runIdentifier, Integer stopMode, HttpClient client) throws IOException {
		// cancelのためにtaskIdの指定が必要なため、checkを実行しtaskIdを取得する
		HttpUriRequest checkRequest = this.getCheckRequest(baseUrl, token, runIdentifier);
		String taskId = client.execute(checkRequest, getCheckResponseHandlerForId());
		HttpUriRequest cancelRequest = this.getCancelRequest(baseUrl, token, taskId, stopMode);
		return client.execute(cancelRequest, this.getCancelResponseHandler());
	}

	/**
	 * 実行結果確認APIのレスポンスからタスクIDを取得するハンドラを返します。
	 * @return タスクID取得用ハンドラ
	 */
	private RpaManagementRestResponseHandler<String> getCheckResponseHandlerForId() {
		return new RpaManagementRestResponseHandler<String>() {
			@Override
			public String handleRpaManagementResponse(ClassicHttpResponse response) throws IOException {
				final HttpEntity responseEntity = response.getEntity();
				JsonFactory jsonFactory = new JsonFactory();
				ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
				try (InputStream inputStream = responseEntity.getContent()) {
					JsonNode rootNode = objectMapper.readTree(inputStream);
					// エラーの場合、例外をthrow
					if (rootNode.has("error")) {
						throw new ClientProtocolException(getMessage(response));
					}
					// キャンセルする際に必要なため"id"を取得する
					String taskId = rootNode.get("items").get(0).get("id").textValue();
					m_log.debug("handleRpaManagementResponse() : taskId=" + taskId);
					return taskId;
				} catch (UnsupportedOperationException | IOException e1) {
					throw new ClientProtocolException(getMessage(response));
				}
			}
		};
	}

	public static void main(String[] args) {
		int apiVersion = Integer.parseInt(args[0]);
		String baseUrl = args[1];
		String accountId = args[2];
		String password = args[3];
		int runType = 1;
		
		
		RpaManagementRestDefine define = new WinactorManagerOnCloudRestDefine(apiVersion);
		
		try (CloseableHttpClient client = RpaUtil.createHttpClient(10000, 10000, null, null, null, null, true)) {
			String token = define.getToken(baseUrl, accountId, password, "", client);
			System.out.println(token);
			String status = define.healthCheck(baseUrl, token, client);
			System.out.println(status);
			Map<String, Object> runRequestData = new HashMap<>();
			runRequestData.put("name", "テスト");
			runRequestData.put("scenarioId", "yT01TW8hAExsRcCiXxKQIA");
			runRequestData.put("winactors", Arrays.asList("DrpCzKyR9wX1e1cnu4FZIQ"));
			runRequestData.put("kind", "immediately");
			runRequestData.put("status", "enable");
			String taskName = define.run(baseUrl, token, runRequestData, runType, client);
			System.out.println(taskName);
			List<String> endStatusList = Arrays.asList("FINISH", "ERROR");
			while (true) {
				String jobStatus = define.check(baseUrl, token, taskName, client);
				System.out.println(jobStatus);
				if (endStatusList.contains(jobStatus)) {
					break;
				}
				System.out.println("waiting...");
				Thread.sleep(10000L);
				define.cancel(baseUrl, token, taskName, null, client);
				break;
			}
		} catch (InterruptedException | IOException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			e.printStackTrace();
		}
	}
}
