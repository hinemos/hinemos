/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.hub.session;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.hub.bean.PropertyConstants;
import com.clustercontrol.hub.model.CollectDataTag;
import com.clustercontrol.hub.model.TransferDestProp;
import com.clustercontrol.hub.model.TransferInfo;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.jobmanagement.model.JobSessionJobEntity;
import com.clustercontrol.jobmanagement.model.JobSessionNodeEntity;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Fluentd 用の転送プラグイン。
 *
 */
public class FluentdTransferFactory implements TransferFactory {
	private static final Logger logger = Logger.getLogger(FluentdTransferFactory.class);
	
	private static final Integer DEFAULT_CONNECT_TIMEOUT = 10000;
	private static final Integer DEFAULT_REQUEST_TIMEOUT = 60000;

	/**
	 * 転送プラグインID
	 */
	public static final String transfer_id = "fluentd";
	
	/**
	 * URL の識別子
	 */
	public static final String prop_url = "fluentd.url";
	/**
	 * 接続タイムアウトの識別子
	 */
	public static final String prop_connect_timeout = "fluentd.connect_timeout";
	/**
	 * リクエストタイムアウトの識別子
	 */
	public static final String prop_request_timeout = "fluentd.request_timeout";

	private boolean keepAlive = true;
	
	public FluentdTransferFactory() {
	}
	
	@Override
	public String getDestTypeId() {
		return transfer_id;
	}
	
	/**
	 * 転送設定に合わせた転送用インスタンスを作成する。
	 * 
	 */
	@Override
	public Transfer createTansfer(final TransferInfo info, final PropertyBinder binder) throws TransferException {
		// HTTP 接続のためのパラメータを取得
		TransferDestProp urlProp = null;
		TransferDestProp connectTimeoutProp = null;
		TransferDestProp requestTimeoutProp = null;
		for (TransferDestProp prop: info.getDestProps()) {
			switch(prop.getName()) {
			case prop_url:
				urlProp = prop;
				break;
			case prop_connect_timeout:
				connectTimeoutProp = prop;
				break;
			case prop_request_timeout:
				requestTimeoutProp = prop;
				break;
			default:
				logger.warn(String.format("createTansfer() : unknown property(%s)", prop.getValue()));
				break;
			}
		}
		
		if (urlProp == null || urlProp.getValue() == null)
			throw new TransferException(String.format("createTansfer() : Value of \"%s\" must be set.", prop_url));
		
		if (!urlPattern.matcher(urlProp.getValue()).matches())
			throw new TransferException(String.format("createTansfer() : invalid url format. url=%s", urlProp.getValue()));
		
		final String urlStr = urlProp.getValue();
		
		Integer timeout;
		try {
			timeout = Integer.valueOf(connectTimeoutProp.getValue());
		} catch(NumberFormatException e) {
			timeout = DEFAULT_CONNECT_TIMEOUT;
			logger.warn(String.format("createTansfer() : can't regognize connectTimeout(%s) as number.", connectTimeoutProp.getValue()));
		} catch (NullPointerException e) {
			timeout = DEFAULT_CONNECT_TIMEOUT;
			logger.warn(String.format("createTansfer() : connectTimeout is null, then use default value as connectTimeout(%s).", timeout));
		}
		final Integer connectTimeout = timeout;
		
		try {
			timeout = Integer.valueOf(requestTimeoutProp.getValue());
		} catch(NumberFormatException e) {
			timeout = DEFAULT_REQUEST_TIMEOUT;
			logger.warn(String.format("createTansfer() : can't regognize requestTimeout(%s) as number.", requestTimeoutProp.getValue()));
		} catch (NullPointerException e) {
			timeout = DEFAULT_CONNECT_TIMEOUT;
			logger.warn(String.format("createTansfer() : requestTimeout is null, then use default value as requestTimeout(%s).", timeout));
		}
		final Integer requestTimeout = timeout;
		
		// 転送用インスタンスを作成。
		return new Transfer() {
			private static final int BUFF_SIZE = 1024 * 1024;
			private static final int BODY_MAX_SIZE = 5 * BUFF_SIZE;

			private CloseableHttpClient client = null;
			
			/*
			 * 数値情報を転送する。
			 * 
			 */
			@Override
			public TransferNumericData transferNumerics(Iterable<TransferNumericData> numerics, TrasferCallback<TransferNumericData> callback) throws TransferException {
				TransferNumericData lastPosition = null;
				for (TransferNumericData numeric: numerics) {
					ObjectNode root = JsonNodeFactory.instance.objectNode();
					root.put("item_name", numeric.key.getItemName());
					root.put("display_name", numeric.key.getDisplayName());
					root.put("monitor_id", numeric.key.getMonitorId());
					root.put("facility_id", numeric.key.getFacilityid());
					root.put("time", numeric.data.getTime());
					root.put("value", numeric.data.getValue());
					root.put("position", numeric.data.getPosition());
					
					String url = binder.bind(numeric.key, numeric.data, urlStr);
					String data = root.toString();
					try {
						send(url, data);
						lastPosition = numeric;
						
						if (callback != null)
							callback.onTransferred(lastPosition);
					} catch(Exception e) {
						logger.warn(e.getMessage(), e);
						internalError_monitor(numeric.key.getMonitorId(), data, e, url);
						break;
					}
				}
				return lastPosition;
			}
			
			/*
			 * 文字列情報を転送する。
			 * 
			 */
			@Override
			public TransferStringData transferStrings(Iterable<TransferStringData> strings, TrasferCallback<TransferStringData> callback) throws TransferException {
				TransferStringData lastPosition = null;
				for (TransferStringData string: strings) {
					ObjectNode root = JsonNodeFactory.instance.objectNode();
					root.put("target_name", string.key.getTargetName());
					root.put("monitor_id", string.key.getMonitorId());
					root.put("facility_id", string.key.getFacilityId());
					root.put("log_format_id", string.data.getLogformatId());
					root.put("time", string.data.getTime());
					root.put("source", string.data.getValue());
					
					for (CollectDataTag t: string.data.getTagList()) {
						root.put(t.getKey(), t.getValue());
					}
					
					root.put("position", string.data.getDataId());
					
					String url = binder.bind(string.key, string.data, urlStr);
					String data = root.toString();
					try {
						send(url, data);
						lastPosition = string;

						if (callback != null)
							callback.onTransferred(lastPosition);
					} catch(Exception e) {
						logger.warn(e.getMessage(), e);
						internalError_monitor(string.key.getMonitorId(), data, e, url);
						break;
					}
				}
				return lastPosition;
			}
			
			/*
			 * ジョブを転送する。
			 * 
			 */
			@Override
			public JobSessionEntity transferJobs(Iterable<JobSessionEntity> sessions, TrasferCallback<JobSessionEntity> callback) throws TransferException {
				JobSessionEntity lastPosition = null;
				for (JobSessionEntity session: sessions) {
					ObjectNode sessionNode = JsonNodeFactory.instance.objectNode();
					sessionNode.put("ssession_id", session.getSessionId());
					sessionNode.put("job_id", session.getJobId());
					sessionNode.put("jobunit_id", session.getJobunitId());
					sessionNode.put("schedule_date", session.getScheduleDate());
					sessionNode.put("position", session.getPosition());
					
					ArrayNode jobArray = sessionNode.putArray("jobs");
					for (JobSessionJobEntity job: session.getJobSessionJobEntities()) {
						ObjectNode jobNode = jobArray.addObject();
						jobNode.put("job_id", job.getId().getJobId());
						jobNode.put("jobunit_id", job.getId().getJobunitId());
						if (job.getScopeText() != null)
							jobNode.put("scope_text", job.getScopeText());
						if (job.getStatus() != null)
							jobNode.put("status", job.getStatus());
						if (job.getStartDate() != null)
							jobNode.put("start_date", job.getStartDate());
						if (job.getEndDate() != null)
							jobNode.put("end_date", job.getEndDate());
						if (job.getEndValue() != null)
							jobNode.put("end_value", job.getEndValue());
						if (job.getEndStatus() != null)
							jobNode.put("end_status", job.getEndStatus());
						if (job.getResult() != null)
							jobNode.put("result", job.getResult());
						if (job.getJobInfoEntity() != null)
							jobNode.put("job_type", job.getJobInfoEntity().getJobType());
						
						if (!job.getJobSessionNodeEntities().isEmpty()) {
							ArrayNode nodeArray = jobNode.putArray("nodes");
							for (JobSessionNodeEntity node: job.getJobSessionNodeEntities()) {
								ObjectNode nodeNode = nodeArray.addObject();
								nodeNode.put("facility_id", node.getId().getFacilityId());
								nodeNode.put("node_name", node.getNodeName());
								nodeNode.put("status", node.getStatus());
								nodeNode.put("start_date", node.getStartDate());
								nodeNode.put("end_date", node.getEndDate());
								nodeNode.put("end_value", node.getEndValue());
								nodeNode.put("message", node.getMessage());
								nodeNode.put("result", node.getResult());
								nodeNode.put("start_date", node.getStartDate());
								nodeNode.put("startup_time", node.getStartupTime());
								nodeNode.put("instance_id", node.getInstanceId());
							}
						}
					}
					
					String url = binder.bind(session, urlStr);
					String data = sessionNode.toString();
					try {
						send(url, data);
						lastPosition = session;
						
						if (callback != null)
							callback.onTransferred(lastPosition);
					} catch(Exception e) {
						logger.warn(e.getMessage(), e);
						internalError_session(session.getSessionId(), data, e, url);
						break;
					}
				}
				return lastPosition;
			}
			
			/*
			 * イベントを転送する。
			 * 
			 */
			@Override
			public EventLogEntity transferEvents(Iterable<EventLogEntity> events, TrasferCallback<EventLogEntity> callback) throws TransferException {
				EventLogEntity lastPosition = null;
				for (EventLogEntity event: events) {
					ObjectNode eventNode = JsonNodeFactory.instance.objectNode();
					eventNode.put("monitor_id", event.getId().getMonitorId());
					eventNode.put("monitor_detail_id", event.getId().getMonitorDetailId());
					eventNode.put("plugin_id", event.getId().getPluginId());
					eventNode.put("generation_date", event.getGenerationDate());
					eventNode.put("facility_id", event.getId().getFacilityId());
					eventNode.put("scope_text", event.getScopeText());
					eventNode.put("application", event.getApplication());
					eventNode.put("message", event.getMessage());
					eventNode.put("message_org", event.getMessageOrg());
					eventNode.put("priority", event.getPriority());
					eventNode.put("confirm_flg", event.getConfirmFlg());
					eventNode.put("confirm_date", event.getCommentDate());
					eventNode.put("confirm_user", event.getCommentUser());
					eventNode.put("duplication_count", event.getDuplicationCount());
					eventNode.put("output_date", event.getId().getOutputDate());
					eventNode.put("inhibited_flg", event.getInhibitedFlg());
					eventNode.put("comment_date", event.getCommentDate());
					eventNode.put("comment_user", event.getCommentUser());
					eventNode.put("comment", event.getComment());
					eventNode.put("position", event.getPosition());
					
					String url = binder.bind(event, urlStr);
					String data = eventNode.toString();
					try {
						send(url, data);
						lastPosition = event;
						
						if (callback != null)
							callback.onTransferred(lastPosition);
					} catch(Exception e) {
						logger.warn(e.getMessage(), e);
						internalError_monitor(event.getId().getMonitorId(), data, e, url);
						break;
					}
				}
				return lastPosition;
			}
			
			/*
			 * プラグイン ID を取得する。
			 * 
			 */
			@Override
			public String getDestTypeId() {
				return transfer_id;
			}
			
			/*
			 * Fluentd へ送信するための HttpClient を取得する。
			 * 未作成の場合は、同時に作成も行う。
			 * 
			 */
			private CloseableHttpClient getHttpClient() {
				if (client == null) {
					client = createHttpClient();
				}
				return client;
			}
			
			/*
			 * Fluentd へ送信するための HttpClient を作成する
			 * 
			 */
			private CloseableHttpClient createHttpClient() {
				String proxyHost = null;
				Integer proxyPort = null;
				CredentialsProvider cledentialProvider = null;
				List<String> ignoreHostList = new ArrayList<>();
				
				try {
					// Hinemos マネージャのプロキシ設定を参照
					proxyHost = HinemosPropertyUtil.getHinemosPropertyStr("hub.fluentd.proxy.host", null);
					Long proxyPortLong = HinemosPropertyUtil.getHinemosPropertyNum("hub.fluentd.proxy.port", null);
					if (proxyPortLong != null)
						proxyPort = proxyPortLong.intValue();
					if (proxyPort == null)
						proxyPort = 80;
					String proxyUser = HinemosPropertyUtil.getHinemosPropertyStr("hub.fluentd.proxy.user", null);
					String proxyPassword = HinemosPropertyUtil.getHinemosPropertyStr("hub.fluentd.proxy.password", null);
					
					if (proxyHost != null && proxyPort != null) {
						logger.debug("initializing fluentd proxy : proxyHost = " + proxyHost + ", port = " + proxyPort);
						String ignoreHostStr = HinemosPropertyUtil.getHinemosPropertyStr("hub.fluentd.proxy.ignorehosts", null);
						if (ignoreHostStr != null) {
							ignoreHostList = Arrays.asList(ignoreHostStr.split(","));
						}
						
						if (proxyUser != null && proxyPassword != null) {
							cledentialProvider = new BasicCredentialsProvider();
							cledentialProvider.setCredentials(new AuthScope(proxyHost, proxyPort), new UsernamePasswordCredentials(proxyUser, proxyPassword));
						}
					}
				} catch (Throwable t) {
					logger.warn("invalid proxy configuration.", t);
					proxyHost = null;
					proxyPort = null;
					cledentialProvider = null;
					ignoreHostList = Collections.emptyList();
				}
				
				List<Header> headers = new ArrayList<>();
				HttpClientBuilder builder = HttpClients.custom()
						.setDefaultCredentialsProvider(cledentialProvider)
						.setDefaultHeaders(headers);
				
				Builder requestBuilder = RequestConfig.custom().setCookieSpec(CookieSpecs.DEFAULT);
				if (connectTimeout != null)
					requestBuilder.setConnectTimeout(connectTimeout);
				if (connectTimeout != null)
					requestBuilder.setSocketTimeout(requestTimeout);
				
				builder.setDefaultRequestConfig(requestBuilder.build());
				
				if (proxyHost != null) {
					Matcher m = urlPattern.matcher(urlStr);
					if (!m.matches())
						throw new InternalError(String.format("invalid url(%s)", urlStr));
					
					m.toMatchResult();
					
					
					boolean ignore = false;
					String host = m.group("host");
					for (String ignoreHost: ignoreHostList) {
						if (ignoreHost.equals(host)) {
							ignore = true;
							break;
						}
					}
					
					if (!ignore) {
						HttpHost proxy = new HttpHost(proxyHost, proxyPort, "http");
						builder.setProxy(proxy);
					}
				}
				
				if (keepAlive) {
					headers.add(new BasicHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE));
				} else {
					headers.add(new BasicHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_CLOSE));
				}
				return builder.build();
			}
			
			/*
			 * 指定された URL へデータをポストする。
			 * 
			 */
			private void send(String url, String data) throws Exception {
				// URL を エンコードする。
				Matcher m = urlPattern.matcher(url);
				if (!m.matches())
					throw new InternalError(String.format("invalid url(%s)", urlStr));
				m.toMatchResult();
				String path = m.group("path");
				if (path != null && !path.isEmpty()) {
					String host = m.group("host");
					String port = m.group("port");
					
					String[] paths = path.split("/");
					for (int i = 1; i < paths.length; ++i) {
						paths[i] = URLEncoder.encode(paths[i], "utf-8");
					}
					url = "http://" + host + (port == null || port.isEmpty() ? "": (":" + port));
					for (int i = 1; i < paths.length; ++i) {
						url += "/" + paths[i];
					}
				}
				
				HttpPost requestPost = new HttpPost(url);
				requestPost.addHeader("content-type", "application/json");
				requestPost.setEntity(new StringEntity(data, StandardCharsets.UTF_8));
				
				logger.debug(String.format("send() : request start. url=%s", url));

				int count = 0;
				int maxTryCount = PropertyConstants.hub_transfer_max_try_count.number();
				
				while(count < maxTryCount) {
					try {
						long start = HinemosTime.currentTimeMillis();
						try (CloseableHttpResponse response = getHttpClient().execute(requestPost)) {
							long responseTime = HinemosTime.currentTimeMillis() -start;
							logger.debug(String.format("send() : url=%s, responseTime=%d", url, responseTime));
							
							int statusCode = response.getStatusLine().getStatusCode();
							logger.debug(String.format("send() : url=%s, code=%d", url, statusCode));
							if (statusCode == HttpStatus.SC_OK) {
								logger.debug(String.format("send() : url=%s, success=%s", url, response.getStatusLine().toString()));
								if (logger.isDebugEnabled()) {
									ByteArrayOutputStream out = new ByteArrayOutputStream();
									try (InputStream in = response.getEntity().getContent()) {
										byte [] buffer = new byte[BUFF_SIZE];
										while(out.size() < BODY_MAX_SIZE) {
											int len = in.read(buffer);
											if(len < 0) {
												break;
											}
											out.write(buffer, 0, len);
										}
									}
									String res = new String(out.toByteArray(), "UTF-8");
									if (!res.isEmpty())
										logger.debug(String.format("send() : url=%s, response=%s", url, res));
								}
							} else{
								throw new RuntimeException(String.format("http status code isn't 200. code=%d, message=%s", statusCode, response.getStatusLine().toString()));
							}
						}

						logger.debug(String.format("send() : success. url=%s, count=%d", url, count));
						break;
					} catch (RuntimeException e) {
						++count;
						
						if (count < maxTryCount) {
							logger.debug(e.getMessage(), e);
							logger.debug(String.format("send() : fail to send, and then wait to retry. url=%s, count=%d", url, count));
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e1) {
								logger.debug(e.getMessage());
							}
						} else {
							throw new TransferException(String.format("send() : fail to send. url=%s, retry count=%d, error=\"%s\"", url, count, e.getMessage()));
						}
					}
				}
			}

			@Override
			public void close() throws Exception {
				if (client != null)
					client.close();
			}

			private void internalError_session(String sessionId, String data, Exception error, String url) {
				internalError_session(sessionId, data, error.getMessage() == null || error.getMessage().isEmpty() ? error.getClass().getSimpleName(): error.getMessage(), url);
			}

			private void internalError_session(String sessionId, String data, String error, String url) {
				AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.HUB_TRANSFER, MessageConstant.MESSAGE_HUB_DATA_TRANSFER_FAILED, new String[]{info.getTransferId()},
						String.format("error=%s%ntransferId=%s%ndestTypeId=%s%nsessionId=%s%ndata=%s%nurl=%s", error, info.getTransferId(), info.getDestTypeId(), sessionId, data, url));
			}

			private void internalError_monitor(String sessionId, String data, Exception error, String url) {
				internalError_monitor(sessionId, data, error.getMessage() == null || error.getMessage().isEmpty() ? error.getClass().getSimpleName(): error.getMessage(), url);
			}

			private void internalError_monitor(String monitorId, String data, String error, String url) {
				AplLogger.put(PriorityConstant.TYPE_WARNING, HinemosModuleConstant.HUB_TRANSFER, MessageConstant.MESSAGE_HUB_DATA_TRANSFER_FAILED, new String[]{info.getTransferId()},
						String.format("error=%s%ntransferId=%s%ndestTypeId=%s%nmonitorId=%s%ndata=%s%nurl=%s", error, info.getTransferId(), info.getDestTypeId(), monitorId, data, url));
			}
		};
	}
	
	/**
	 * Fluentd 用のパラメータを取得する。
	 *
	 */
	@Override
	public List<Property> properties() {
		List<Property> properties = new ArrayList<>();
		properties.add(new Property(prop_url, "http://127.0.0.1:8888/", prop_url + ".description"));
		properties.add(new Property(prop_connect_timeout, DEFAULT_CONNECT_TIMEOUT, prop_connect_timeout + ".description"));
		properties.add(new Property(prop_request_timeout, DEFAULT_REQUEST_TIMEOUT, prop_request_timeout + ".description"));
		return properties;
	}
	
	/**
	 * http 形式を見分ける正規表現。URL は、変数を使用して作成されるので、URL クラスでのバリデーションはせずに、正規表現で行う。
	 */
	private static final Pattern urlPattern = Pattern.compile("http://(?<host>[^/:]+)(:(?<port>[0-9]+)|)((?<path>/.*)|)");
	
	/**
	 * Fluentd 用のパラメータのバリデーションを実施する
	 *
	 */
	@Override
	public void validate(TransferInfo transferInfo) throws InvalidSetting {
		
		boolean hasUrl = false;
		for (TransferDestProp prop: transferInfo.getDestProps()) {
			switch(prop.getName()) {
			case prop_url:
				// #[] が含まれるので、通常の URL としてのバリデーションはやめる。
				Matcher m = urlPattern.matcher(prop.getValue());
				boolean match = m.matches();
				if (!match)
					throwInvalidSetting("invalid url(%s)", prop.getValue());
				
				hasUrl = true;
				break;
			case prop_connect_timeout:
				try {
					Integer.valueOf(prop.getValue());
				} catch(NumberFormatException e) {
					throwInvalidSetting("invalid number format(%s)", prop.getValue());
				}
				break;
			case prop_request_timeout:
				try {
					Integer.valueOf(prop.getValue());
				} catch(NumberFormatException e) {
					throwInvalidSetting("invalid number format(%s)", prop.getValue());
				}
				break;
			default:
				logger.warn(String.format("createTansfer() : unknown property(%s)", prop.getValue()));
				break;
			}
		}
		
		if (!hasUrl) {
			throwInvalidSetting("url must be set");
		}
	}
	
	private static void throwInvalidSetting(String message, Object...args) throws InvalidSetting {
		InvalidSetting e = new InvalidSetting(String.format(message, args));
		logger.info(String.format("validate() : %s, %s", e.getClass().getSimpleName(), e.getMessage()));
		throw e;
	}
	
	/**
	 * Fluentd 用のプラグイン名を取得する
	 *
	 */
	@Override
	public String getName() {
		return "fluentd";
	}

	/**
	 * Fluentd 用のプラグインの説明を取得する
	 *
	 */
	@Override
	public String getDescription() {
		return "fluentd.description";
	}
}
