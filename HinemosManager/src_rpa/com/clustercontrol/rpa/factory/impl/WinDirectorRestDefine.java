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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.StringBody;
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
import com.clustercontrol.jobmanagement.bean.RpaStopTypeConstant;
import com.clustercontrol.rpa.factory.RpaManagementRestDefine;
import com.clustercontrol.rpa.factory.RpaManagementRestResponseHandler;
import com.clustercontrol.rpa.factory.bean.RpaResourceInfo;
import com.clustercontrol.rpa.util.RpaUtil;
import com.clustercontrol.util.MessageConstant;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * WinDirectorのREST API向けの処理を定義するクラス
 */
public class WinDirectorRestDefine extends RpaManagementRestDefine {
	private static final Logger m_log = Logger.getLogger(WinDirectorRestDefine.class);

	// token取得時に含めるidentify_token_cd
	private static final String identifyTokenCd = "Hinemos_WinDirector";
	// WinDirector REST APIのベースURLパス
	private static final String winDirectorRestPath = "/windirector/restapi";

	public WinDirectorRestDefine(int apiVersion) {
		super(apiVersion);
	}

	@Override
	protected HttpUriRequest createGenerateTokenRequest(String baseUrl) {
		String url = baseUrl + winDirectorRestPath + "/token/";
		return new HttpPost(url);
	}

	@Override
	protected Header[] createGenerateTokenHeader(String tenantName) {
		return null;
	}

	@Override
	protected HttpEntity createGenerateTokenEntity(String accountId, String password, String tenantName) {
		JsonFactory jsonFactory = new JsonFactory();
		ObjectMapper objectMapper = new ObjectMapper(jsonFactory);

		// Request Body
		Map<String, String> requestData = new HashMap<>();
		requestData.put("identify_token_cd", identifyTokenCd);
		requestData.put("user_id", accountId);
		requestData.put("password", password);
		try {
			return new StringEntity(objectMapper.writeValueAsString(requestData), ContentType.APPLICATION_JSON);
		} catch (JsonProcessingException e) {
			// ここには来ない想定
			m_log.warn("getGenerateTokenRequest(): " + e.getClass().getSimpleName() + ", " + e.getMessage());
			return null;
		}
	}

	@Override
	protected RpaManagementRestResponseHandler<String> getGenerateTokenResponseHandler() {
		return new RpaManagementRestResponseHandler<String>() {
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

	private Header getAuthTokenHeader(String token) {
		return new BasicHeader("Authorization", token);
	}

	@Override
	public long getTokenExpiredMillis() {
		return HinemosPropertyCommon.rpa_management_rest_windirector_token_expired_millis.getIntegerValue();
	}

	@Override
	public List<RpaResourceInfo> getRpaResourceInfo(String baseUrl, String token, HttpClient client)
			throws IOException {
		if (apiVersion == 1) {
			// apiVersionが1(WinDirector ver 2.2 or 2.3)は非対応のため、空のリストを返す。
			return Collections.emptyList();
		}

		// GETリクエスト
		HttpUriRequest getDevicesRequest = createRequest(new HttpGet(baseUrl + winDirectorRestPath + "/devices/"),
				new Header[] { getAuthTokenHeader(token) }, null);

		// ハンドラ
		RpaManagementRestResponseHandler<List<RpaResourceInfo>> getDevicesResponseHandler = new RpaManagementRestResponseHandler<List<RpaResourceInfo>>() {
			@Override
			public List<RpaResourceInfo> handleRpaManagementResponse(ClassicHttpResponse response) throws IOException {
				final HttpEntity responseEntity = response.getEntity();
				JsonFactory jsonFactory = new JsonFactory();
				ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
				try (InputStream inputStream = responseEntity.getContent()) {
					// "device_list"から各デバイスの情報を取得
					JsonNode value = objectMapper.readTree(inputStream).get("device_list");
					List<RpaResourceInfo> ret = new ArrayList<>();

					for (Iterator<JsonNode> itr = value.elements(); itr.hasNext();) {
						// RpaResourceInfoに格納
						JsonNode device = itr.next();
						String fullComputerName = device.get("full_computer_name").textValue();
						String ipAddress = device.get("ip_address").textValue();
						String account = device.get("account").textValue();
						String deviceId = device.get("device_id").textValue();

						// hostName = fullComputerName or ipAddress
						// (値が入っているのはどちらか一方のみ)
						String hostName;
						RpaResourceInfo resourceInfo = new RpaResourceInfo();
						if (!fullComputerName.isEmpty()) {
							hostName = fullComputerName;
							resourceInfo.setFacilityName(fullComputerName);
							resourceInfo.setIpAddress("");
							resourceInfo.setNodeName(hostName);
							resourceInfo.setHostName(hostName);
						} else {
							hostName = ipAddress;
							resourceInfo.setFacilityName("");
							resourceInfo.setIpAddress(ipAddress);
							resourceInfo.setNodeName("");
							resourceInfo.setHostName("");
						}

						resourceInfo.setRpaUser(account);
						resourceInfo.setRpaExecEnvId(deviceId);

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
		if (apiVersion == 1) {
			// apiVersionが1(WinDirector ver 2.2 or 2.3)は非対応のため、false
			return false;
		} else {
			return true;
		}
	}

	@Override
	protected HttpUriRequest createHealthCheckRequest(String baseUrl) {
		return new HttpGet(baseUrl + winDirectorRestPath + "/scenarios/");
	}

	@Override
	protected Header[] createHealthCheckHeader(String token) {
		return new Header[] { getAuthTokenHeader(token) };
	}

	@Override
	protected HttpEntity createHealthCheckEntity() {
		return null;
	}

	@Override
	protected RpaManagementRestResponseHandler<String> getHealthCheckResponseHandler() {
		return new RpaManagementRestResponseHandler<String>() {
			@Override
			public String handleRpaManagementResponse(ClassicHttpResponse response) throws IOException {
				final HttpEntity responseEntity = response.getEntity();
				JsonFactory jsonFactory = new JsonFactory();
				ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
				try (InputStream inputStream = responseEntity.getContent()) {
					// "result"を取得
					int result = objectMapper.readTree(inputStream).get("result").intValue();
					if (result != 0) {
						// result=0でなければ、例外をthrow
						throw new ClientProtocolException(getMessage(response));
					}
					return String.valueOf(result);
				} catch (UnsupportedOperationException | IOException e1) {
					throw new ClientProtocolException(getMessage(response));
				}
			}
		};
	}

	@Override
	public MessageConstant getHealthCheckInfo() {
		return MessageConstant.SCENARIO_LIST;
	}

	@Override
	protected HttpUriRequest createRunRequest(String baseUrl, Integer runType) {
		if (apiVersion == 1) {
			// ver.2.2 or ver.2.3
			// シナリオの実行依頼API
			return new HttpPost(baseUrl + winDirectorRestPath + "/jobs/");
		} else {
			// ver.2.4
			if (runType == 1) {
				// シナリオの実行依頼API
				return new HttpPost(baseUrl + winDirectorRestPath + "/jobs/");
			} else {
				// ジョブ登録API
				return new HttpPost(baseUrl + winDirectorRestPath + "/jobs/insert/");
			}
		}
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
		if (apiVersion == 1) {
			// ver.2.2 or ver.2.3
			// シナリオの実行依頼API
			return new StringEntity(requestJson, ContentType.APPLICATION_JSON);
		} else {
			// ver.2.4
			if (runType == 1) {
				// シナリオの実行依頼API
				return new StringEntity(requestJson, ContentType.APPLICATION_JSON);
			} else {
				// ジョブ登録API
				// ファイルを用いるパラメータは非対応
				MultipartEntityBuilder builder = MultipartEntityBuilder.create();
				return builder.addPart("job_insert_json", new StringBody(requestJson, ContentType.APPLICATION_JSON))
						.build();
			}
		}
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
					// "result"を取得
					int result = rootNode.get("result").intValue();
					if (result != 0) {
						// result=0でなければ、例外をthrow
						throw new ClientProtocolException(getMessage(response));
					}
					// "job_id"を取得
					String jobId = rootNode.get("job_id").textValue();
					return jobId;
				} catch (UnsupportedOperationException | IOException e1) {
					throw new ClientProtocolException(getMessage(response));
				}
			}
		};
	}

	@Override
	protected Map<String, Object> adjustRunRequestData(Map<String, Object> requestData) {
		// シナリオ入力パラメータがある場合、パラメータに"data_names"を追加する
		if (requestData.containsKey("datas")) {
			@SuppressWarnings("unchecked")
			List<Map<String, String>> datas = (List<Map<String, String>>) requestData.get("datas");
			// "datas"のkeyのリストを追加
			requestData.put("data_names", datas.get(0).keySet());
		}
		return requestData;
	}

	@Override
	protected HttpUriRequest createCheckRequest(String baseUrl, String runIdentifier) {
		HttpGet httpGet = new HttpGet(baseUrl + winDirectorRestPath + "/jobs/status/");
		// リクエストパラメータを設定
		List<NameValuePair> nameValuePairs = new ArrayList<>();
		nameValuePairs.add(new BasicNameValuePair("job_id", runIdentifier));
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
					// "result"を取得
					int result = rootNode.get("result").intValue();
					if (result != 0) {
						// result=0でなければ、例外をthrow
						throw new ClientProtocolException(getMessage(response));
					}
					// "job_status"を取得
					String jobStatus = rootNode.get("job_status").textValue();
					m_log.debug("handleRpaManagementResponse() : jobStatus=" + jobStatus);
					return jobStatus;
				} catch (UnsupportedOperationException | IOException e1) {
					throw new ClientProtocolException(getMessage(response));
				}
			}
		};
	}

	@Override
	protected HttpUriRequest createCancelRequest(String baseUrl, String runIdentifier) {
		if (apiVersion == 1) {
			m_log.warn("createCancelRequest() : unsupported apiVersion=" + apiVersion);
			return null;
		}
		HttpPut httpPut = new HttpPut(baseUrl + winDirectorRestPath + "/jobs/forceend/");
		// リクエストパラメータを設定
		List<NameValuePair> nameValuePairs = new ArrayList<>();
		nameValuePairs.add(new BasicNameValuePair("job_id", runIdentifier));
		try {
			httpPut.setUri(new URIBuilder(httpPut.getUri()).addParameters(nameValuePairs).build());
		} catch (URISyntaxException e) {
			// ここには来ない想定
			m_log.warn("createCancelRequest(): " + e.getClass().getSimpleName() + ", " + e.getMessage());
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
					// "result"を取得
					int result = rootNode.get("result").intValue();
					if (result != 0) {
						// result=0でなければ、例外をthrow
						throw new ClientProtocolException(getMessage(response));
					}
					return String.valueOf(result);
				} catch (UnsupportedOperationException | IOException e1) {
					throw new ClientProtocolException(getMessage(response));
				}
			}
		};
	}

	@Override
	public boolean checkRpaStopType(int stopType) {
		if (apiVersion == 1 && stopType != RpaStopTypeConstant.STOP_JOB) {
			// apiVersion=1の場合はジョブのみ終了する以外選択不可
			return false;
		}
		return true;
	}

	// for debug
	public static void main(String[] args) {
		int apiVersion = Integer.parseInt(args[0]);
		int runType = Integer.parseInt(args[1]);
		String baseUrl = args[2];
		String accountId = args[3];
		String password = args[4];
		String proxyUrl = null;
		Integer proxyPort = null;
		String proxyUser = null;
		String proxyPassword = null;

		if (args.length > 5) {
			proxyUrl = args[5];
			proxyPort = Integer.parseInt(args[6]);
		}
		if (args.length > 7) {
			proxyUser = args[7];
			proxyPassword = args[8];
		}

		RpaManagementRestDefine define = new WinDirectorRestDefine(apiVersion);

		try (CloseableHttpClient client = RpaUtil.createHttpClient(100000, 100000, proxyUrl, proxyPort, proxyUser,
				proxyPassword, true)) {
			String token = define.getToken(baseUrl, accountId, password, "", client);
			System.out.println(token);
			String status = define.healthCheck(baseUrl, token, client);
			System.out.println(status);
			Map<String, Object> runRequestData = new HashMap<>();
			// runType = 1
			runRequestData.put("job_name", "テスト");
			runRequestData.put("scenario_id", "3");
			runRequestData.put("robo_group", "テストグループ");
			// runType = 2
			// runRequestData.put("job_name", "テスト");
			// runRequestData.put("scenario_id_1", "3");
			// runRequestData.put("datalist_file_1", "");
			// runRequestData.put("device_id", "2");
			String jobId = define.run(baseUrl, token, runRequestData, runType, client);
			System.out.println(jobId);
			List<String> endStatusList = Arrays.asList("3", "5");
			while (true) {
				String jobStatus = define.check(baseUrl, token, jobId, client);
				System.out.println(status);
				if (endStatusList.contains(status)) {
					System.out.println(jobStatus);
					break;
				}
				System.out.println("waiting...");
				Thread.sleep(10000L);
				define.cancel(baseUrl, token, jobId, null, client);
				break;
			}
		} catch (InterruptedException | IOException | KeyManagementException | NoSuchAlgorithmException
				| KeyStoreException e) {
			e.printStackTrace();
		}
	}
}
