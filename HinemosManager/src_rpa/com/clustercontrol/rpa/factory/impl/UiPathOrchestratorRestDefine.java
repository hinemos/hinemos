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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.log4j.Logger;

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
 * UiPath OrchestratorのREST API向け処理を定義するクラス
 * On-Premise, Automation Cloudで共通する処理を定義 
 */
public abstract class UiPathOrchestratorRestDefine extends RpaManagementRestDefine {
	private static final Logger m_log = Logger.getLogger(UiPathOrchestratorOnPremiseRestDefine.class);

	// シナリオ実行に必要なヘッダリスト
	private static final List<String> HEADER_LIST_FOR_RUN = Arrays.asList("X-UIPATH-OrganizationUnitId", "X-UIPATH-FolderPath");
	
	public UiPathOrchestratorRestDefine(int apiVersion) {
		super(apiVersion);
	}
	private Header getAuthTokenHeader(String token) {
		return new BasicHeader("Authorization", "Bearer " + token);
	}
	
	private List<String> getFolderIds(String baseUrl, String token, HttpClient client) throws IOException {
		// GETリクエスト
		HttpUriRequest getFolderReqest = 
				createRequest(
						new HttpGet(baseUrl + "/odata/Folders"),
						new Header[] {getAuthTokenHeader(token)},
						null);
		// ハンドラ
		RpaManagementRestResponseHandler<List<String>> getFolderResponseHandler = 
				new RpaManagementRestResponseHandler<List<String>>() {
					@Override
					public List<String> handleRpaManagementResponse(ClassicHttpResponse response) throws IOException {
						final HttpEntity responseEntity = response.getEntity();
						JsonFactory jsonFactory = new JsonFactory();
						ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
						try (InputStream inputStream = responseEntity.getContent()) {
							// "value"から各フォルダの"Id"を取得
							JsonNode value = objectMapper.readTree(inputStream).get("value");
							List<String> ret = new ArrayList<>();
							
							for (Iterator<JsonNode> itr = value.elements();itr.hasNext();) {
								JsonNode folder = itr.next();
								ret.add(String.valueOf(folder.get("Id").intValue()));
							}
							return ret;
					    } catch (UnsupportedOperationException | IOException e1) {
					    	throw new ClientProtocolException(getMessage(response));
						}	
					}
				};
		
		return client.execute(getFolderReqest, getFolderResponseHandler);
	}
	
	private List<String> getMachineIds(String baseUrl, String token, HttpClient client) throws IOException {
		// GETリクエスト
		HttpUriRequest getFolderReqest = 
				createRequest(
						new HttpGet(baseUrl + "/odata/Machines"),
						new Header[] {getAuthTokenHeader(token)},
						null);
		// ハンドラ
		RpaManagementRestResponseHandler<List<String>> getFolderResponseHandler = 
				new RpaManagementRestResponseHandler<List<String>>() {
					@Override
					public List<String> handleRpaManagementResponse(ClassicHttpResponse response) throws IOException {
						final HttpEntity responseEntity = response.getEntity();
						JsonFactory jsonFactory = new JsonFactory();
						ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
						try (InputStream inputStream = responseEntity.getContent()) {
							// "value"から各マシンの"Id"を取得
							JsonNode value = objectMapper.readTree(inputStream).get("value");
							List<String> ret = new ArrayList<>();
							
							for (Iterator<JsonNode> itr = value.elements();itr.hasNext();) {
								JsonNode folder = itr.next();
								String type = folder.get("Type").textValue();
								if (!type.equals("Template")) {
									// "Type"が"Template"以外のものを返す。
									ret.add(String.valueOf(folder.get("Id").intValue()));
								}
							}
							return ret;
					    } catch (UnsupportedOperationException | IOException e1) {
					    	throw new ClientProtocolException(getMessage(response));
						}	
					}
				};
		
		return client.execute(getFolderReqest, getFolderResponseHandler);
		
	}
	
	private List<RpaResourceInfo> getRobots(String baseUrl, String token, String folderId, List<String> machineIds, HttpClient client) throws IOException {
		// GETリクエスト
		HttpUriRequest getRobotsRequest = 
				createRequest(new HttpGet(baseUrl + "/odata/Robots"),
						new Header[] {getAuthTokenHeader(token),
								new BasicHeader("X-UIPATH-OrganizationUnitId", folderId)}, null);

		// ハンドラ
		RpaManagementRestResponseHandler<List<RpaResourceInfo>> getRobotsResponseHandler = 
				new RpaManagementRestResponseHandler<List<RpaResourceInfo>>() {
					@Override
					public List<RpaResourceInfo> handleRpaManagementResponse(ClassicHttpResponse response) throws IOException {
						final HttpEntity responseEntity = response.getEntity();
						JsonFactory jsonFactory = new JsonFactory();
						ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
						try (InputStream inputStream = responseEntity.getContent()) {
							// "value"から各ロボットの情報を取得
							JsonNode value = objectMapper.readTree(inputStream).get("value");
							List<RpaResourceInfo> ret = new ArrayList<>();
							
							for (Iterator<JsonNode> itr = value.elements();itr.hasNext();) {
								// RpaResourceInfoに格納
								JsonNode robot = itr.next();
								String machineId = String.valueOf(robot.get("MachineId").intValue());
								
								// マシンIDリストにない(マシンがテンプレート)の場合は、スキップ
								if (!machineIds.contains(machineId)) {
									continue;
								}
																
								String machineName = robot.get("MachineName").textValue();
								String userName = robot.get("Username").textValue();
								String robotId = String.valueOf(robot.get("Id").intValue());
								
								RpaResourceInfo resourceInfo = new RpaResourceInfo();
								resourceInfo.setFacilityName(machineName);
								resourceInfo.setIpAddress("");
								resourceInfo.setNodeName(machineName);
								resourceInfo.setHostName(machineName);
								resourceInfo.setRpaUser(userName);
								resourceInfo.setRpaExecEnvId(machineId + "/" + robotId);
								
								ret.add(resourceInfo);
							}
							return ret;
					    } catch (UnsupportedOperationException | IOException e1) {
					    	throw new ClientProtocolException(getMessage(response));
						}	
					}
				};

		return client.execute(getRobotsRequest, getRobotsResponseHandler);
	}


	@Override
	public List<RpaResourceInfo> getRpaResourceInfo(String baseUrl, String token, HttpClient client) throws IOException {
		// フォルダIDの取得
		List<String> folderIds = getFolderIds(baseUrl, token, client);
		
		// マシンIDの取得
		List<String> machineIds = getMachineIds(baseUrl, token, client);

		List<RpaResourceInfo> ret = new ArrayList<>();
		// フォルダIDからロボット一覧を取得し、RpaResourceInfoに格納
		for (String folderId : folderIds) {
			try {
				List<RpaResourceInfo> robotsInfo = getRobots(baseUrl, token, folderId, machineIds, client);
				ret.addAll(robotsInfo);
			} catch(IOException e) {
				// ロボット一覧が取得できなかった場合は、スキップし次のフォルダを確認。
				m_log.info(String.format("getRpaResourceInfo(): getRobots failure in a folder, folderId = %s, %s", folderId, e.getMessage()));
			}
		}
		
		return ret;
	}

	@Override
	public boolean enabledRpaResourceDetection() {
		return true;
	}

	@Override
	protected HttpUriRequest createHealthCheckRequest(String baseUrl) {
		return new HttpGet(baseUrl + "/odata/Jobs");
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
		return new RpaManagementRestResponseHandler<String>() {
			@Override
			public String handleRpaManagementResponse(ClassicHttpResponse response) throws IOException {
				final HttpEntity responseEntity = response.getEntity();
				JsonFactory jsonFactory = new JsonFactory();
				ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
				try (InputStream inputStream = responseEntity.getContent()) {
					// "@odata.count"を取得
					return String.valueOf(objectMapper.readTree(inputStream).get("@odata.count").longValue());
			    } catch (UnsupportedOperationException | IOException e1) {
			    	throw new ClientProtocolException(getMessage(response));
				}
			}
		};
	}

	@Override
	public MessageConstant getHealthCheckInfo() {
		return MessageConstant.JOB_STATUS;
	}

	@Override
	protected HttpUriRequest createRunRequest(String baseUrl, Integer runTypel) {
		String url = StringUtils.join(new String[]{baseUrl, "odata", "Jobs", "UiPath.Server.Configuration.OData.StartJobs"}, "/");
		return new HttpPost(url);
	}

	@Override
	protected Header[] createRunHeader(String token, Map<String, Object> headerData) {
		List<Header> headerList = new ArrayList<>();
		headerList.add(getAuthTokenHeader(token));
		if(!headerData.isEmpty()) {
			for (Map.Entry<String, Object> e : headerData.entrySet()) {
				headerList.add(new BasicHeader(e.getKey(), e.getValue()));
			}
		}
		return headerList.toArray(new Header[headerList.size()]);
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
	protected Map<String, Object> adjustRunRequestData(Map<String, Object> requestData) {
		// パラメータをstartInfoの要素に詰め直す
		Map<String, Object> startInfo = new HashMap<>();
		for (Map.Entry<String, Object> entry : requestData.entrySet()) {
			startInfo.put(entry.getKey(), entry.getValue());
		}
		requestData.clear();
		requestData.put("startInfo", startInfo);
		return requestData;
	}

	@Override
	protected Map<String, Object> adjustRunHeaderData(Map<String, Object> requestData) {
		// requestDataでヘッダに関する情報を別マップに切り出す。その際requestDataにあったヘッダ情報は削除する。
		Map<String, Object> headerData = new HashMap<String, Object>();
		for(String headerKey : HEADER_LIST_FOR_RUN) {
			if(requestData.containsKey(headerKey)) {
				headerData.put(headerKey, requestData.get(headerKey));
				requestData.remove(headerKey);
			}
		}
		
		m_log.debug("adjustRunHeaderData(); : headerData=" + headerData);
		return headerData;
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
					if (rootNode.has("errorCode")) {
						throw new ClientProtocolException(getMessage(response));
					}
					// "Id"を取得
					String key = rootNode.get("value").get(0).get("Id").asText();
					return key;
				} catch (UnsupportedOperationException | IOException e1) {
					throw new ClientProtocolException(getMessage(response));
				}
			}
		};
	}

	@Override
	protected HttpUriRequest createCheckRequest(String baseUrl, String runIdentifier) {
		StringBuilder url = new StringBuilder();
		url.append(StringUtils.join(new String[]{baseUrl, "odata", "Jobs"}, "/"));
		url.append("(" + runIdentifier + ")");
		HttpGet httpGet = new HttpGet(url.toString());
		try {
			httpGet.setUri(new URIBuilder(httpGet.getUri()).build());
		} catch (URISyntaxException e) {
			// ここには来ない想定
			m_log.warn("createCheckRequest(): " + e.getClass().getSimpleName() + ", " + e.getMessage());
			return null;
		}
		return httpGet;
	}

	@Override
	protected Header[] createCheckHeader(String token) {
		return new Header[] {getAuthTokenHeader(token)};
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
					// "state"を取得
					String state = rootNode.get("State").textValue();
					m_log.debug("handleRpaManagementResponse() : state=" + state);
					return state;
				} catch (UnsupportedOperationException | IOException e1) {
					throw new ClientProtocolException(getMessage(response));
				}
			}
		};
	}

	@Override
	protected HttpUriRequest createCancelRequest(String baseUrl, String runIdentifier) {
		StringBuilder url = new StringBuilder();
		url.append(StringUtils.join(new String[]{baseUrl, "odata", "Jobs"}, "/"));
		url.append("(" + runIdentifier + ")");
		url.append("/UiPath.Server.Configuration.OData.StopJob");
		HttpPost httpPost = new HttpPost(url.toString());
		try {
			httpPost.setUri(new URIBuilder(httpPost.getUri()).build());
		} catch (URISyntaxException e) {
			// ここには来ない想定
			m_log.warn("createCheckRequest(): " + e.getClass().getSimpleName() + ", " + e.getMessage());
			return null;
		}
		return httpPost;
	}

	@Override
	protected Header[] createCancelHeader(String token) {
		return new Header[] {getAuthTokenHeader(token)};
	}

	@Override
	protected HttpEntity createCancelEntity(String runIdentifier, Integer stopMode) {
		Map<String, String> requestData = new HashMap<>();
		String mode;
		if (stopMode == 1) {
			mode = "SoftStop";  // 停止
		} else {
			mode = "Kill";  // 強制終了
		}
		requestData.put("strategy", mode);
		JsonFactory jsonFactory = new JsonFactory();
		ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
		String requestJson = null;
		try {
			requestJson = objectMapper.writeValueAsString(requestData);
			m_log.debug("createCancelEntity() : requestJson=" + requestJson);
		} catch (JsonProcessingException e) {
			// ここには来ない想定
			m_log.warn("createCancelEntity() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			return null;
		}
		return new StringEntity(requestJson, ContentType.APPLICATION_JSON);
	}

	@Override
	protected RpaManagementRestResponseHandler<String> getCancelResponseHandler() {
		return new RpaManagementRestResponseHandler<String>() {
			@Override
			public String handleRpaManagementResponse(ClassicHttpResponse response) throws IOException {
				return "";  // 正常の場合レスポンスはステータスコードのみのため空文字列を返す
			}
		};
	}

	// for debug
	public static void main(String[] args) {
		int apiVersion = Integer.parseInt(args[0]);
		String baseUrl = args[1];
		String accountId = args[2];
		String password = args[3];
		String tenantName = args[4];
		Integer stopMode = Integer.valueOf(args[5]);
		int runType = 1;

		UiPathOrchestratorRestDefine define = new UiPathOrchestratorOnPremiseRestDefine(apiVersion);
		
		try (CloseableHttpClient client = RpaUtil.createHttpClient(10000, 10000, null, null, null, null, true)) {
			String token = define.getToken(baseUrl, accountId, password, tenantName, client);
			List<String> machineIds = define.getMachineIds(baseUrl, token, client);
			System.out.println(machineIds);
			String status = define.healthCheck(baseUrl, token, client);
			System.out.println(status);
			Map<String, Object> runRequestData = new HashMap<>();
			runRequestData.put("ReleaseKey", "1e5ddbbd-5877-4fd0-82bd-c9904f257a37");
			runRequestData.put("Strategy", "Specific");
			runRequestData.put("RobotIds", Arrays.asList(3));
			runRequestData.put("NoOfRobots", 0);
			runRequestData.put("JobsCount", 0);
			runRequestData.put("JobPriority", "Low");
			runRequestData.put("InputArguments", "{\"foo\":\"bar\"}");
			String jobId = define.run(baseUrl, token, runRequestData, runType, client);
			System.out.println(jobId);
			List<String> endStatusList = Arrays.asList("Successful", "Faulted", "Stopped");
			while (true) {
				String jobStatus = define.check(baseUrl, token, jobId, client);
				System.out.println(jobStatus);
				if (endStatusList.contains(jobStatus)) {
					break;
				}
				System.out.println("waiting...");
				Thread.sleep(10000L);
				define.cancel(baseUrl, token, jobId, stopMode, client);
				break;
			}
		} catch (InterruptedException | IOException | KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			e.printStackTrace();
		}
	}
}
