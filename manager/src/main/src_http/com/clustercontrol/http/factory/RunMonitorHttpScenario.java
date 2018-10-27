/*

 Copyright (C) 2014 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.http.factory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.persistence.EntityExistsException;

import org.apache.http.Header;
import org.apache.log4j.Logger;

import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.collect.bean.Sample;
import com.clustercontrol.collect.util.CollectDataUtil;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.http.model.HttpScenarioCheckInfo;
import com.clustercontrol.http.model.Page;
import com.clustercontrol.http.model.Variable;
import com.clustercontrol.http.util.CallableTaskHttpScenario;
import com.clustercontrol.http.util.GetHttpResponse;
import com.clustercontrol.http.util.GetHttpResponse.AuthType;
import com.clustercontrol.http.util.QueryUtil;
import com.clustercontrol.http.util.Response;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.monitor.run.bean.MonitorRunResultInfo;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.util.ParallelExecution;
import com.clustercontrol.performance.bean.CollectedDataErrorTypeConstant;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.repository.util.RepositoryUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.StringBinder;

/**
 * HTTP監視 数値監視を実行するクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class RunMonitorHttpScenario extends RunMonitor {
	private static class PageResponse {
		public PageResponse(Page page, Response response) {
			this.page = page;
			this.response = response;
		}
		public final Page page;
		public final Response response;
	}

	private static Logger m_log = Logger.getLogger(RunMonitorHttpScenario.class);

	/** HTTP監視情報 */
	private HttpScenarioCheckInfo m_httpScenarioCheckInfo = null;

	/**
	 * コンストラクタ
	 * 
	 */
	public RunMonitorHttpScenario() {
		super();
	}

	/**
	 * マルチスレッドを実現するCallableTaskに渡すためのインスタンスを作成するメソッド
	 * 
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#runMonitorInfo()
	 * @see com.clustercontrol.monitor.run.util.MonitorExecuteTask
	 */
	@Override
	protected RunMonitor createMonitorInstance() {
		return new RunMonitorHttpScenario();
	}

	/**
	 * [リソース監視用]監視を実行します。（並列処理）
	 *
	 * リソース監視では1つのファシリティIDに対して、複数の収集項目ID及び、デバイスに対するリソースを監視・収集します。
	 * この動作に対応するため、独自のrunMonitorInfoを実装します。
	 *
	 */
	@Override
	protected boolean runMonitorInfo() throws FacilityNotFound, MonitorNotFound, EntityExistsException, InvalidRole, HinemosUnknown {
		m_log.debug("runMonitorInfo()");

		m_now = new Date(HinemosTime.currentTimeMillis());

		m_priorityMap = new HashMap<Integer, ArrayList<String>>();
		m_priorityMap.put(Integer.valueOf(PriorityConstant.TYPE_INFO),		new ArrayList<String>());
		m_priorityMap.put(Integer.valueOf(PriorityConstant.TYPE_WARNING),	new ArrayList<String>());
		m_priorityMap.put(Integer.valueOf(PriorityConstant.TYPE_CRITICAL),	new ArrayList<String>());
		m_priorityMap.put(Integer.valueOf(PriorityConstant.TYPE_UNKNOWN),	new ArrayList<String>());

		try
		{
			// 監視基本情報を設定
			boolean run = this.setMonitorInfo(m_monitorTypeId, m_monitorId);
			if(!run){
				// 処理終了
				return true;
			}

			// 判定情報を設定
			setJudgementInfo();

			// チェック条件情報を設定
			setCheckInfo();

			ArrayList<String> facilityList = null;
			ExecutorCompletionService<ArrayList<MonitorRunResultInfo>> ecs = new ExecutorCompletionService<ArrayList<MonitorRunResultInfo>>(ParallelExecution.instance().getExecutorService());
			int taskCount = 0;

			if (!m_isMonitorJob) {
				// 監視ジョブ以外の場合
				// ファシリティIDの配下全ての一覧を取得
				// 有効/無効フラグがtrueとなっているファシリティIDを取得する
				facilityList = new RepositoryControllerBean().getExecTargetFacilityIdList(m_facilityId, m_monitor.getOwnerRoleId());
				if (facilityList.size() == 0) {
					return true;
				}

				m_isNode = new RepositoryControllerBean().isNode(m_facilityId);

				// 監視対象となっているノードの変数を取得
				nodeInfo = new HashMap<String, NodeInfo>();
				for (String facilityId : facilityList) {
					try {
						synchronized (this) {
							nodeInfo.put(facilityId, new RepositoryControllerBean().getNode(facilityId));
						}
					} catch (FacilityNotFound e) {
						// 何もしない
					}
				}

				m_log.debug("monitor start : monitorTypeId : " + m_monitorTypeId + ", monitorId : " + m_monitorId);

				String facilityId = null;

				/**
				 * 監視の実行
				 */
				// ファシリティIDの数だけ、各監視処理を実行する
				Iterator<String> itr = facilityList.iterator();
				while(itr.hasNext()){
					facilityId = itr.next();
					if(facilityId != null && !"".equals(facilityId)){

						// マルチスレッド実行用に、RunMonitorのインスタンスを新規作成する
						// インスタンスを新規作成するのは、共通クラス部分に監視結果を保持するため
						RunMonitorHttpScenario runMonitor = new RunMonitorHttpScenario();

						// 監視実行に必要な情報を再度セットする
						runMonitor.m_monitorTypeId = this.m_monitorTypeId;
						runMonitor.m_monitorId = this.m_monitorId;
						runMonitor.m_now = this.m_now;
						runMonitor.m_priorityMap = this.m_priorityMap;
						runMonitor.setMonitorInfo(runMonitor.m_monitorTypeId, runMonitor.m_monitorId);
						runMonitor.setJudgementInfo();
						runMonitor.setCheckInfo();
						runMonitor.nodeInfo = this.nodeInfo;
						
						ecs.submit(new CallableTaskHttpScenario(runMonitor, facilityId));
						taskCount++;
					}
					else {
						itr.remove();
					}
				}
			} else {
				// 監視ジョブの場合
				// 監視ジョブ以外の場合
				// ファシリティIDの配下全ての一覧を取得
				// 有効/無効フラグがtrueとなっているファシリティIDを取得する
				facilityList = new RepositoryControllerBean().getExecTargetFacilityIdList(m_facilityId, m_monitor.getOwnerRoleId());
				if (facilityList.size() != 1
						|| !facilityList.get(0).equals(m_facilityId) ) {
					return true;
				}

				m_isNode = new RepositoryControllerBean().isNode(m_facilityId);

				// 監視対象となっているノードの変数を取得
				nodeInfo = new HashMap<String, NodeInfo>();
				for (String facilityId : facilityList) {
					try {
						synchronized (this) {
							nodeInfo.put(facilityId, new RepositoryControllerBean().getNode(m_facilityId));
						}
					} catch (FacilityNotFound e) {
						// 何もしない
					}
				}
				m_log.debug("monitor start : monitorTypeId : " + m_monitorTypeId + ", monitorId : " + m_monitorId);

				/**
				 * 監視の実行
				 */
				// マルチスレッド実行用に、RunMonitorのインスタンスを新規作成する
				// インスタンスを新規作成するのは、共通クラス部分に監視結果を保持するため
				RunMonitorHttpScenario runMonitor = new RunMonitorHttpScenario();

				// 監視実行に必要な情報を再度セットする
				runMonitor.m_isMonitorJob = this.m_isMonitorJob;
				runMonitor.m_monitorTypeId = this.m_monitorTypeId;
				runMonitor.m_monitorId = this.m_monitorId;
				runMonitor.m_now = this.m_now;
				runMonitor.m_priorityMap = this.m_priorityMap;
				runMonitor.setMonitorInfo(runMonitor.m_monitorTypeId, runMonitor.m_monitorId);
				runMonitor.setJudgementInfo();
				runMonitor.setCheckInfo();
				runMonitor.nodeInfo = this.nodeInfo;

				ecs.submit(new CallableTaskHttpScenario(runMonitor, m_facilityId));
				taskCount++;
			}
			/**
			 * 監視結果の集計
			 */
			ArrayList<MonitorRunResultInfo> resultList = null;

			m_log.debug("total start : monitorTypeId : " + m_monitorTypeId + ", monitorId : " + m_monitorId);

			// 収集値の入れ物を作成
			List<Sample> sampleList= new ArrayList<Sample>();
			Sample sample = null;
			if(m_monitor.getCollectorFlg()){
				sample = new Sample(HinemosTime.getDateInstance(), m_monitor.getMonitorId());
			}
			
			for (int i = 0; i < taskCount; i++) {
				Future<ArrayList<MonitorRunResultInfo>> future = ecs.take();
				resultList = future.get();	// 監視結果を取得
				
				for(MonitorRunResultInfo result : resultList){
					m_nodeDate = result.getNodeDate();
					
					String facilityId = result.getFacilityId();
					
					// 監視結果を通知
					if (!m_isMonitorJob) {
						if(result.getMonitorFlg()){
							notify(true, facilityId, result.getCheckResult(), new Date(m_nodeDate), result);
						}
					} else {
						m_monitorRunResultInfo = new MonitorRunResultInfo();
						m_monitorRunResultInfo.setPriority(result.getPriority());
						m_monitorRunResultInfo.setCheckResult(result.getCheckResult());
						m_monitorRunResultInfo.setNodeDate(m_nodeDate);
						m_monitorRunResultInfo.setMessageOrg(makeJobOrgMessage(result.getMessageOrg(), result.getMessage()));
					}

					// 個々の収集値の登録
					if(sample != null && result.getCollectorFlg()){
						int errorCode = -1;
						if(result.isCollectorResult()){
							errorCode = CollectedDataErrorTypeConstant.NOT_ERROR;
						}else{
							errorCode = CollectedDataErrorTypeConstant.UNKNOWN;
						}
						sample.set(facilityId, m_monitor.getItemName(), result.getValue(), errorCode, result.getDisplayName());
					}
				}
			}
			
			// 収集値をまとめて登録
			if(sample != null){
				sampleList.add(sample);
			}
			if(!sampleList.isEmpty()){
				CollectDataUtil.put(sampleList);
			}
			m_log.debug("monitor end : monitorTypeId : " + m_monitorTypeId + ", monitorId : " + m_monitorId);

			return true;

		}
		catch (EntityExistsException e) {
			throw e;
		}
		catch (FacilityNotFound e) {
			throw e;
		}
		catch (InvalidRole e) {
			throw e;
		}
		catch (InterruptedException e) {
			m_log.info("runMonitorInfo() monitorTypeId = " + m_monitorTypeId + ", monitorId = " + m_monitorId + " : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			return false;
		}
		catch (ExecutionException e) {
			m_log.info("runMonitorInfo() monitorTypeId = " + m_monitorTypeId + ", monitorId = " + m_monitorId + " : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			return false;
		}
	}



	private static enum ResultType {
		SUCCESS,
		NOT_FOUND_URL_FOR_REDIRECT,
		NOT_MATCH_EXPECTED_STATUS_CODES,
		NOT_MATCH_PATTERNS,
		MATCH_PATTERN,
		TIMEOUT,
		UNEXPECTED
	}

	/**
	 * HTTP 応答時間（ミリ秒）を取得
	 * 
	 * @param facilityId ファシリティID
	 * @return 値取得に成功した場合、true
	 */
	public List<MonitorRunResultInfo> collectList(String facilityId) {
		// 無効となっている設定はスキップする
		if (!m_isMonitorJob && 
			(!m_httpScenarioCheckInfo.getMonitorInfo().getMonitorFlg() &&
			!m_httpScenarioCheckInfo.getMonitorInfo().getCollectorFlg())) {
			if (m_log.isDebugEnabled()) {
				m_log.debug("http scenario monitor " + m_httpScenarioCheckInfo.getMonitorId()
						+ " is not enabled, skip filtering.");
			}
			return Collections.emptyList();
		}

		if (m_now != null){
			m_nodeDate = m_now.getTime();
		}

		GetHttpResponse.GetHttpResponseBuilder builder = null;
		try {
			builder = GetHttpResponse.custom()
				.setAuthType(m_httpScenarioCheckInfo.getAuthType() == null ? GetHttpResponse.AuthType.NONE: AuthType.valueOf(m_httpScenarioCheckInfo.getAuthType()))
				.setAuthUser(m_httpScenarioCheckInfo.getAuthUser())
				.setAuthPassword(m_httpScenarioCheckInfo.getAuthPassword())
				.setUserAgent(m_httpScenarioCheckInfo.getUserAgent())
				.setConnectTimeout(m_httpScenarioCheckInfo.getConnectTimeout() == null ? 0: m_httpScenarioCheckInfo.getConnectTimeout())
				.setRequestTimeout(m_httpScenarioCheckInfo.getRequestTimeout() == null ? 0: m_httpScenarioCheckInfo.getRequestTimeout())
				.setCancelProxyCache(HinemosPropertyUtil.getHinemosPropertyBool("monitor.http.scenario.disable.proxy.cache", true))
				.setKeepAlive(true)
				.setNeedAuthSSLCert(! HinemosPropertyUtil.getHinemosPropertyBool("monitor.http.ssl.trustall", true));
			if (m_httpScenarioCheckInfo.getProxyFlg()) {
				builder
				.setProxyURL(m_httpScenarioCheckInfo.getProxyUrl())
				.setProxyPort(m_httpScenarioCheckInfo.getProxyPort() == null ? 0: m_httpScenarioCheckInfo.getProxyPort())
				.setProxyUser(m_httpScenarioCheckInfo.getProxyUser())
				.setProxyPassword(m_httpScenarioCheckInfo.getProxyPassword());
			}
		}
		catch (URISyntaxException e) {
			m_log.warn("fail to initialize GetHttpResponse : " + e.getMessage(), e);

			MonitorRunResultInfo info = new MonitorRunResultInfo();
			info.setFacilityId(facilityId);
			info.setMonitorFlg(m_httpScenarioCheckInfo.getMonitorInfo().getMonitorFlg());
			info.setCollectorFlg(false);
			info.setCollectorResult(false);
			info.setCheckResult(-1);
			info.setPriority(PriorityConstant.TYPE_UNKNOWN);
			info.setMessage(MessageConstant.MESSAGE_FAIL_TO_ANALYZE_PROXY_URL.getMessage());
			StringBuffer messageOrg = new StringBuffer();
			messageOrg.append(e.getMessage());
			messageOrg.append("\n");
			messageOrg.append(MessageConstant.MONITOR_HTTP_SCENARIO_PROXYURL.getMessage());
			messageOrg.append(" : ");
			messageOrg.append(m_httpScenarioCheckInfo.getProxyUrl());
			info.setMessageOrg(messageOrg.toString());
			info.setNodeDate(m_nodeDate);
			info.setProcessType(true);
			info.setNotifyGroupId(m_httpScenarioCheckInfo.getMonitorInfo().getNotifyGroupId());

			return Arrays.asList(info);
		}

		int responseTime = 0;
		ResultType endResultType = ResultType.SUCCESS;
		MonitorRunResultInfo errorResultInfo = null;
		List<PageResponse> responses = new ArrayList<PageResponse>();
		try (GetHttpResponse m_request = builder.build()) {
			Map<String, String> variables = RepositoryUtil.createNodeParameter(nodeInfo.get(facilityId));

			List<Page> pages = new ArrayList<>(m_httpScenarioCheckInfo.getPages());
			Collections.sort(pages, new Comparator<Page>() {
				@Override
				public int compare(Page o1, Page o2) {
					return o1.getId().getPageOrderNo().compareTo(o2.getId().getPageOrderNo());
				}
			});

			loopEnd:
			for (Page page: pages) {
				ResultType resultType = ResultType.SUCCESS;
				MonitorRunResultInfo resultInfo = null;
				StringBinder strbinder = new StringBinder(variables);
				String url = page.getUrl();
				url = strbinder.bindParam(url);

				String post = page.getPost();
				if (post != null && !post.isEmpty())
					post = strbinder.bindParam(post);

				if (m_log.isTraceEnabled()) m_log.trace("http request. (nodeInfo = " + nodeInfo + ", facilityId = " + facilityId + ", url = " + url + ")");

				PageResponse response = null;
				List<String> rurls = new ArrayList<>();
				String nextUrl = url;
				String nextPost = post;
				while (true) {
					m_request.execute(nextUrl, nextPost);
					response = new PageResponse(page, m_request.getResult());

					if (response.response.exception == null) {
						// リダイレクトをする必要があるか確認。
						if (
							response.response.statusCode == 301 ||
							response.response.statusCode == 302 ||
							response.response.statusCode == 303 ||
							response.response.statusCode == 307
							) {
							for (Header h: response.response.headers) {
								if (h.getName().equals("Location")) {
									nextUrl = h.getValue();
									nextPost = null;
									break;
								}
							}

							if (nextUrl != null) {
								rurls.add(nextUrl);
							}
							else {
								// リダイレクト先がないので異常
								resultType = ResultType.NOT_FOUND_URL_FOR_REDIRECT;
								break;
							}
						}
						else {
							break;
						}
					}
					else {
						break;
					}
				}

				if (ResultType.NOT_FOUND_URL_FOR_REDIRECT.equals(resultType)) {
					resultInfo = createNotFoundUrlForRedirectMonitorRunResultInfo(page, response.response, url, rurls);
					resultInfo.setFacilityId(facilityId);
				}

				if (ResultType.SUCCESS.equals(resultType) && response.response.exception != null) {
					if (SocketTimeoutException.class.equals(response.response.exception.getClass())) {
						resultType = ResultType.TIMEOUT;
						resultInfo = createTimeoutMonitorRunResultInfo(page, response.response, url, rurls);
						resultInfo.setFacilityId(facilityId);
					}
					else {
						resultType = ResultType.UNEXPECTED;
						resultInfo = createUnexpectedMonitorRunResultInfo(page, response.response, url, rurls);
						resultInfo.setFacilityId(facilityId);
					}
				}

				if (ResultType.SUCCESS.equals(resultType) && !(page.getStatusCode() == null || Pattern.matches("(\\s*|.*,\\s*)" + response.response.statusCode + "(\\s*,.*|\\s*)", page.getStatusCode()))) {
					resultType = ResultType.NOT_MATCH_EXPECTED_STATUS_CODES;
					resultInfo = createUnmatchedStatusCodeMonitorRunResultInfo(page, m_request.getResult(), url);
					resultInfo.setFacilityId(facilityId);
				}

				if (ResultType.SUCCESS.equals(resultType) && !page.getPatterns().isEmpty()) {
					List<com.clustercontrol.http.model.Pattern> patterns = new ArrayList<>(page.getPatterns());
					Collections.sort(patterns, new Comparator<com.clustercontrol.http.model.Pattern>() {
						@Override
						public int compare(com.clustercontrol.http.model.Pattern o1, com.clustercontrol.http.model.Pattern o2) {
							return o1.getId().getPatternOrderNo().compareTo(o2.getId().getPatternOrderNo());
						}
					});

					com.clustercontrol.http.model.Pattern matchedPattern = null;
					Boolean exceptionProcessType = null;
					for (int i = 0; i < patterns.size(); ++i) {
						com.clustercontrol.http.model.Pattern pe = patterns.get(i);

						if (!pe.getValidFlg() || pe.getPattern() == null)
							continue;

						try {
							// 大文字・小文字を区別しない場合
							Pattern pattern = null;
							if(pe.getCaseSensitivityFlg()) {
								pattern = Pattern.compile(pe.getPattern(), Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
							}
							// 大文字・小文字を区別する場合
							else{
								pattern = Pattern.compile(pe.getPattern(), Pattern.DOTALL);
							}

							// パターンマッチ表現でマッチング
							String body = response.response.responseBody;
							if (body == null) { body = ""; }; // 404などの場合はbodyがnullになってしまう。
							Matcher matcher = pattern.matcher(body);
							if (matcher.matches()) {
								matchedPattern = pe;
								break;
							}
						}
						catch (PatternSyntaxException e) {
							m_log.info("collectList(): PatternSyntax is not valid." +
									" description=" + pe.getDescription() +
									", patternSyntax=" + pe.getPattern() + ", value=" + response.response.responseBody + " : "
									+ e.getClass().getSimpleName() + ", " + e.getMessage());
							exceptionProcessType = pe.getProcessType();
						}
						catch (Exception  e) {
							m_log.warn("collectList(): PatternSyntax is not valid." +
									" description="+pe.getDescription() +
									", patternSyntax="+pe.getPattern() + ", value=" + response.response.responseBody + " : "
									+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
							exceptionProcessType = pe.getProcessType();
						}
					}

					if (matchedPattern != null) {
						resultType = ResultType.MATCH_PATTERN;
						resultInfo = createMatchedPatternMonitorRunResultInfo(page, matchedPattern, response.response, url);
						resultInfo.setFacilityId(facilityId);
					}
					else {
						resultType = ResultType.NOT_MATCH_PATTERNS;
						resultInfo = createNotmatchedPatternsMonitorRunResultInfo(page, response.response, url, exceptionProcessType);
						resultInfo.setFacilityId(facilityId);
					}
				}

				// 処理の継続チェック。
				switch(resultType) {
				case NOT_MATCH_EXPECTED_STATUS_CODES:
				case NOT_MATCH_PATTERNS:
				case TIMEOUT:
				case UNEXPECTED:
				case NOT_FOUND_URL_FOR_REDIRECT:
					errorResultInfo = resultInfo;
					endResultType = resultType;
					break loopEnd;
				case MATCH_PATTERN:
					if (resultInfo.getProcessType().booleanValue()) {
						endResultType = resultType;
						errorResultInfo = resultInfo;
						break loopEnd;
					}
					break;
				default:
					// SUCCESS
					break;
				}

				// 変数の生成。
				for (Variable variable: page.getVariables()) {
					String value = null;
					if (variable.getMatchingWithResponseFlg()) {
						if (response.response.responseBody != null) {
							Matcher m = Pattern.compile(variable.getValue(), Pattern.DOTALL).matcher(response.response.responseBody);
							if (m.matches()) {
								try {
									value = m.group(1);
								}
								catch (IndexOutOfBoundsException e) {
									m_log.warn(String.format(
											"not contain group paragraph in pattern for variable. facilityId=%s, monitorId=%s, pageNo=%d, variableName=%s, value=%s",
											facilityId,
											m_httpScenarioCheckInfo.getMonitorId(),
											page.getId().getPageOrderNo(),
											variable.getId().getName(),
											variable.getValue()));
								}
							}
							else {
								// マッチしない。
								m_log.debug(String.format(
										"variable not match. facilityId=%s, monitorId=%s, pageNo=%d, variableName=%s, value=%s",
										facilityId,
										m_httpScenarioCheckInfo.getMonitorId(),
										page.getId().getPageOrderNo(),
										variable.getId().getName(),
										variable.getValue()));
							}
						}
						else {
							// レスポンスがない。
							m_log.warn(String.format(
									"Not foudnd previous post. facilityId=%s, monitorId=%s, pageNo=%d, variableName=%s, value=%s",
									facilityId,
									m_httpScenarioCheckInfo.getMonitorId(),
									page.getId().getPageOrderNo(),
									variable.getId().getName(),
									variable.getValue()));
						}
					} else {
						value = variable.getValue();
					}

					if (value != null) {
						variables.put(variable.getId().getName(), value);
					}
				}

				responses.add(response);
				responseTime += m_request.getResult().responseTime;
			}
		}
		catch (IOException e) {
			m_log.warn("fail to close HttpClient : " + e.getMessage(), e);
		}

		List<MonitorRunResultInfo> resultInfos = new ArrayList<>();
		if (ResultType.SUCCESS.equals(endResultType)) {
			MonitorRunResultInfo info = new MonitorRunResultInfo();
			info.setFacilityId(facilityId);
			info.setMonitorFlg(m_httpScenarioCheckInfo.getMonitorInfo().getMonitorFlg());
			info.setCollectorFlg(m_httpScenarioCheckInfo.getMonitorInfo().getCollectorFlg());
			info.setCollectorResult(true);
			info.setCheckResult(0);
			info.setItemCode("0");
			info.setItemName(m_httpScenarioCheckInfo.getMonitorInfo().getItemName());
			info.setDisplayName("");
			info.setPriority(PriorityConstant.TYPE_INFO);
			info.setMessage(String.format("%s : %s", MessageConstant.MONITOR_HTTP_SCENARIO_TOTAL_RESPONSETIME_MS.getMessage(), NumberFormat.getNumberInstance().format(responseTime)));
			int pageOrderNo = 1;
			StringBuffer messageOrg = new StringBuffer();
			messageOrg.append(MessageConstant.MONITOR_HTTP_SCENARIO_TOTAL_RESPONSETIME.getMessage());
			messageOrg.append(" : ");
			messageOrg.append(responseTime);
			messageOrg.append("\n");
			for (PageResponse pr: responses) {
				messageOrg.append(MessageConstant.MONITOR_HTTP_SCENARIO_PAGE_ORDERNO.getMessage());
				messageOrg.append(" : ");
				messageOrg.append(pageOrderNo++);
				messageOrg.append("\n");
				messageOrg.append(MessageConstant.MONITOR_HTTP_SCENARIO_PAGE_URL.getMessage());
				messageOrg.append(" : ");
				messageOrg.append(pr.response.url);
				messageOrg.append("\n");
				messageOrg.append(MessageConstant.MONITOR_HTTP_SCENARIO_PAGE_STATUSCODE.getMessage());
				messageOrg.append(" : ");
				messageOrg.append(pr.response.statusCode);
				messageOrg.append("\n");
				messageOrg.append(MessageConstant.RESPONSE_TIME_MILLI_SEC.getMessage());
				messageOrg.append(" : ");
				messageOrg.append(pr.response.responseTime);
				messageOrg.append("\n");
			}
			info.setMessageOrg(messageOrg.toString());
			info.setNodeDate(m_nodeDate);
			info.setValue((double)responseTime);
			info.setProcessType(true);
			info.setNotifyGroupId(m_httpScenarioCheckInfo.getMonitorInfo().getNotifyGroupId());
			resultInfos.add(info);

			// ページ毎の収集情報作成。
			if (!m_isMonitorJob &&
				m_httpScenarioCheckInfo.getMonitoringPerPageFlg() &&
				m_httpScenarioCheckInfo.getMonitorInfo().getCollectorFlg()
				) {
				Map<String, Integer> map = new HashMap<String, Integer>();
				for (PageResponse pr: responses) {
					Integer count = map.get(pr.page.getUrl());
					if (count == null) {
						count = 1;
						map.put(pr.page.getUrl(), count);
					}
					else {
						map.put(pr.page.getUrl(), ++count);
					}

					MonitorRunResultInfo pagetResultInfo = new MonitorRunResultInfo();
					pagetResultInfo.setFacilityId(facilityId);
					pagetResultInfo.setMonitorFlg(false);
					pagetResultInfo.setCollectorFlg(true);
					pagetResultInfo.setCollectorResult(true);
					pagetResultInfo.setItemCode(Integer.toString(pr.page.getId().getPageOrderNo() + 1));
					pagetResultInfo.setDisplayName(pr.page.getUrl() + " (" + count + ")");
					pagetResultInfo.setPriority(pr.page.getPriority());
					pagetResultInfo.setNodeDate(m_nodeDate);
					pagetResultInfo.setValue((double)pr.response.responseTime);
					pagetResultInfo.setNotifyGroupId(m_httpScenarioCheckInfo.getMonitorInfo().getNotifyGroupId());
					resultInfos.add(pagetResultInfo);
				}
			}
		}
		else {
			resultInfos.add(errorResultInfo);
		}
		return resultInfos;
	}

	private MonitorRunResultInfo createTimeoutMonitorRunResultInfo(Page page,
			Response response, String url, List<String> rurls) {
		MonitorRunResultInfo info = new MonitorRunResultInfo();
		info.setMonitorFlg(m_httpScenarioCheckInfo.getMonitorInfo().getMonitorFlg());
		info.setCollectorFlg(false);
		info.setCollectorResult(false);
		info.setCheckResult(-1);
		info.setPriority(page.getPriority());
		info.setMessage(page.getMessage());
		StringBuffer messageOrg = new StringBuffer();
		messageOrg.append(MessageConstant.MESSAGE_REQUEST_TIMEOUT.getMessage());
		messageOrg.append("\n");
		messageOrg.append(MessageConstant.MONITOR_HTTP_SCENARIO_PAGE_ORDERNO.getMessage());
		messageOrg.append(" : ");
		messageOrg.append(page.getId().getPageOrderNo() + 1);
		messageOrg.append("\n");
		messageOrg.append(MessageConstant.MONITOR_HTTP_SCENARIO_PAGE_URL.getMessage());
		messageOrg.append(" : ");
		messageOrg.append(url);
		messageOrg.append("\n");
		for (int i = 0; i < rurls.size(); ++i) {
			messageOrg.append(MessageConstant.MONITOR_HTTP_SCENARIO_REDIRECT_URL.getMessage() + (i + 1));
			messageOrg.append(" : ");
			messageOrg.append(rurls.get(i));
			messageOrg.append("\n");
		}
		messageOrg.append(MessageConstant.MONITOR_HTTP_SCENARIO_CONNECTTIMEOUT.getMessage());
		messageOrg.append(" : ");
		messageOrg.append(m_httpScenarioCheckInfo.getConnectTimeout());
		messageOrg.append("\n");
		messageOrg.append(MessageConstant.MONITOR_HTTP_SCENARIO_REQUESTTIMEOUT.getMessage());
		messageOrg.append(" : ");
		messageOrg.append(m_httpScenarioCheckInfo.getRequestTimeout());
		info.setMessageOrg(messageOrg.toString());
		info.setNodeDate(m_nodeDate);
		info.setProcessType(true);
		info.setNotifyGroupId(m_httpScenarioCheckInfo.getMonitorInfo().getNotifyGroupId());
		info.setItemName(m_httpScenarioCheckInfo.getMonitorInfo().getItemName());
		info.setDisplayName(getDisplayName(page, url));
		return info;
	}

	private MonitorRunResultInfo createUnexpectedMonitorRunResultInfo(Page page,
			Response response, String url, List<String> rurls) {
		MonitorRunResultInfo info = new MonitorRunResultInfo();
		info.setMonitorFlg(m_httpScenarioCheckInfo.getMonitorInfo().getMonitorFlg());
		info.setCollectorFlg(false);
		info.setCollectorResult(false);
		info.setCheckResult(-1);
		info.setPriority(page.getPriority());
		info.setMessage(page.getMessage());
		StringBuffer messageOrg = new StringBuffer();
		messageOrg.append(MessageConstant.MESSAGE_UNEXPECTED_EXCEPTION_OCCURRED.getMessage());
		messageOrg.append("\n");
		messageOrg.append(response.errorMessage);
		messageOrg.append("\n");
		messageOrg.append(MessageConstant.MONITOR_HTTP_SCENARIO_PAGE_ORDERNO.getMessage());
		messageOrg.append(" : ");
		messageOrg.append(page.getId().getPageOrderNo() + 1);
		messageOrg.append("\n");
		messageOrg.append(MessageConstant.MONITOR_HTTP_SCENARIO_PAGE_URL.getMessage());
		messageOrg.append(" : ");
		messageOrg.append(url);
		for (int i = 0; i < rurls.size(); ++i) {
			messageOrg.append(MessageConstant.MONITOR_HTTP_SCENARIO_REDIRECT_URL.getMessage() + (i + 1));
			messageOrg.append(" : ");
			messageOrg.append(rurls.get(i));
			messageOrg.append("\n");
		}
		info.setMessageOrg(messageOrg.toString());
		info.setNodeDate(m_nodeDate);
		info.setProcessType(true);
		info.setNotifyGroupId(m_httpScenarioCheckInfo.getMonitorInfo().getNotifyGroupId());
		info.setItemName(m_httpScenarioCheckInfo.getMonitorInfo().getItemName());
		info.setDisplayName(getDisplayName(page, url));
		return info;
	}

	private MonitorRunResultInfo createUnmatchedStatusCodeMonitorRunResultInfo(Page page,
			Response response, String url) {
		MonitorRunResultInfo info = new MonitorRunResultInfo();
		info.setMonitorFlg(m_httpScenarioCheckInfo.getMonitorInfo().getMonitorFlg());
		info.setCollectorFlg(false);
		info.setCollectorResult(false);
		info.setCheckResult(-1);
		info.setPriority(page.getPriority());
		info.setMessage(page.getMessage());
		StringBuffer messageOrg = new StringBuffer();
		messageOrg.append(MessageConstant.MESSAGE_UNEXPECTED_STATUS_CODE_RETURNED.getMessage());
		messageOrg.append("\n");
		messageOrg.append(MessageConstant.MONITOR_HTTP_SCENARIO_PAGE_ORDERNO.getMessage());
		messageOrg.append(" : ");
		messageOrg.append(page.getId().getPageOrderNo() + 1);
		messageOrg.append("\n");
		messageOrg.append(MessageConstant.MONITOR_HTTP_SCENARIO_PAGE_URL.getMessage());
		messageOrg.append(" : ");
		messageOrg.append(url);
		messageOrg.append("\n");
		messageOrg.append(MessageConstant.MONITOR_HTTP_SCENARIO_PAGE_EXPECTED_STATUSCODE.getMessage());
		messageOrg.append(" : ");
		messageOrg.append(page.getStatusCode());
		messageOrg.append("\n");
		messageOrg.append(MessageConstant.MONITOR_HTTP_SCENARIO_PAGE_STATUSCODE.getMessage());
		messageOrg.append(" : ");
		messageOrg.append(response.statusCode);
		info.setMessageOrg(messageOrg.toString());
		info.setNodeDate(m_nodeDate);
		info.setProcessType(true);
		info.setNotifyGroupId(m_httpScenarioCheckInfo.getMonitorInfo().getNotifyGroupId());
		info.setItemName(m_httpScenarioCheckInfo.getMonitorInfo().getItemName());
		info.setDisplayName(getDisplayName(page, url));
		return info;
	}

	private MonitorRunResultInfo createMatchedPatternMonitorRunResultInfo(Page page, com.clustercontrol.http.model.Pattern pattern,
			Response response, String url) {
		MonitorRunResultInfo info = new MonitorRunResultInfo();
		info.setMonitorFlg(m_httpScenarioCheckInfo.getMonitorInfo().getMonitorFlg() && (page.getPriority() != PriorityConstant.TYPE_NONE));
		info.setCollectorFlg(false);
		info.setCollectorResult(false);
		info.setCheckResult(pattern.getId().getPatternOrderNo());
		info.setPriority(page.getPriority());
		info.setMessage(page.getMessage());
		StringBuffer messageOrg = new StringBuffer();
		messageOrg.append(MessageConstant.MESSAGE_MATCH_RESPONSE_OF_PAGE_WITH_STRING_PATTERN.getMessage(new String[]{String.valueOf(page.getId().getPageOrderNo() + 1), String.valueOf(pattern.getId().getPatternOrderNo() + 1)}));
		messageOrg.append("\n");
		messageOrg.append(MessageConstant.MONITOR_HTTP_SCENARIO_PAGE_ORDERNO.getMessage());
		messageOrg.append(" : ");
		messageOrg.append(page.getId().getPageOrderNo() + 1);
		messageOrg.append("\n");
		messageOrg.append(MessageConstant.MONITOR_HTTP_SCENARIO_PAGE_URL.getMessage());
		messageOrg.append(" : ");
		messageOrg.append(url);
		messageOrg.append("\n");
		messageOrg.append(MessageConstant.MONITOR_HTTP_SCENARIO_PAGE_STATUSCODE.getMessage());
		messageOrg.append(" : ");
		messageOrg.append(response.statusCode);
		messageOrg.append("\n");
		messageOrg.append(MessageConstant.RESPONSE_TIME_MILLI_SEC.getMessage());
		messageOrg.append(" : ");
		messageOrg.append(response.responseTime);
		messageOrg.append("\n");
		messageOrg.append(String.format("%s %d", MessageConstant.MONITOR_HTTP_SCENARIO_PATTERN_PATTERN.getMessage(), pattern.getId().getPatternOrderNo() + 1));
		messageOrg.append(" : ");
		messageOrg.append(pattern.getPattern());
		messageOrg.append("\n");
		messageOrg.append(MessageConstant.MONITOR_HTTP_SCENARIO_RESPONSE.getMessage());
		messageOrg.append(" : ");
		messageOrg.append(response.responseBody);
		info.setMessageOrg(messageOrg.toString());
		info.setNodeDate(m_nodeDate);
		info.setProcessType(page.getPriority() != PriorityConstant.TYPE_NONE ? pattern.getProcessType(): false);
		info.setNotifyGroupId(m_httpScenarioCheckInfo.getMonitorInfo().getNotifyGroupId());
		info.setItemName(m_httpScenarioCheckInfo.getMonitorInfo().getItemName());
		info.setDisplayName(getDisplayName(page, url));
		info.setPatternText(pattern.getPattern());
		return info;
	}

	private MonitorRunResultInfo createNotmatchedPatternsMonitorRunResultInfo(Page page,
			Response response, String url, Boolean exceptionProcessType) {
		MonitorRunResultInfo info = new MonitorRunResultInfo();
		info.setMonitorFlg(m_httpScenarioCheckInfo.getMonitorInfo().getMonitorFlg() && (page.getPriority() != PriorityConstant.TYPE_NONE));
		info.setCollectorFlg(false);
		info.setCollectorResult(false);
		info.setCheckResult(exceptionProcessType != null ? -1: -2);
		info.setPriority(page.getPriority());
		info.setMessage(page.getMessage());
		StringBuffer messageOrg = new StringBuffer();
		messageOrg.append(MessageConstant.MESSAGE_NOT_MATCH_RESPONSE_OF_PAGE_WITH_ALL_STRING_PATTERNS.getMessage(new String[]{String.valueOf(page.getId().getPageOrderNo() + 1)}));
		messageOrg.append("\n");
		messageOrg.append(MessageConstant.MONITOR_HTTP_SCENARIO_PAGE_ORDERNO.getMessage());
		messageOrg.append(" : ");
		messageOrg.append(page.getId().getPageOrderNo() + 1);
		messageOrg.append("\n");
		messageOrg.append(MessageConstant.MONITOR_HTTP_SCENARIO_PAGE_URL.getMessage());
		messageOrg.append(" : ");
		messageOrg.append(url);
		messageOrg.append("\n");
		messageOrg.append(MessageConstant.MONITOR_HTTP_SCENARIO_PAGE_STATUSCODE.getMessage());
		messageOrg.append(" : ");
		messageOrg.append(response.statusCode);
		messageOrg.append("\n");
		messageOrg.append(MessageConstant.RESPONSE_TIME_MILLI_SEC.getMessage());
		messageOrg.append(" : ");
		messageOrg.append(response.responseTime);
		messageOrg.append("\n");
		messageOrg.append(MessageConstant.MONITOR_HTTP_SCENARIO_RESPONSE.getMessage());
		messageOrg.append(" : ");
		messageOrg.append(response.responseBody);
		info.setMessageOrg(messageOrg.toString());
		info.setNodeDate(m_nodeDate);
		info.setProcessType(page.getPriority() != PriorityConstant.TYPE_NONE ? (exceptionProcessType != null ? exceptionProcessType: true): false);
		info.setNotifyGroupId(m_httpScenarioCheckInfo.getMonitorInfo().getNotifyGroupId());
		info.setItemName(m_httpScenarioCheckInfo.getMonitorInfo().getItemName());
		info.setDisplayName(getDisplayName(page, url));
		return info;
	}

	private MonitorRunResultInfo createNotFoundUrlForRedirectMonitorRunResultInfo(Page page,
			Response response, String url, List<String> rurls) {
		MonitorRunResultInfo info = new MonitorRunResultInfo();
		info.setMonitorFlg(m_httpScenarioCheckInfo.getMonitorInfo().getMonitorFlg() && (page.getPriority() != PriorityConstant.TYPE_NONE));
		info.setCollectorFlg(false);
		info.setCollectorResult(false);
		info.setCheckResult(-1);
		info.setPriority(page.getPriority());
		info.setMessage(page.getMessage());
		StringBuffer messageOrg = new StringBuffer();
		messageOrg.append(MessageConstant.MESSAGE_NOT_FOUND_A_REDIRECT_URL_ON_PAGE.getMessage());
		messageOrg.append("\n");
		messageOrg.append(MessageConstant.MONITOR_HTTP_SCENARIO_PAGE_ORDERNO.getMessage());
		messageOrg.append(" : ");
		messageOrg.append(page.getId().getPageOrderNo() + 1);
		messageOrg.append("\n");
		messageOrg.append(MessageConstant.MONITOR_HTTP_SCENARIO_PAGE_URL.getMessage());
		messageOrg.append(" : ");
		messageOrg.append(url);
		messageOrg.append("\n");
		for (int i = 0; i < rurls.size(); ++i) {
			messageOrg.append(MessageConstant.MONITOR_HTTP_SCENARIO_REDIRECT_URL.getMessage() + " " + (i + 1));
			messageOrg.append(" : ");
			messageOrg.append(rurls.get(i));
			messageOrg.append("\n");
		}
		info.setMessageOrg(messageOrg.toString());
		info.setNodeDate(m_nodeDate);
		info.setProcessType(true);
		info.setNotifyGroupId(m_httpScenarioCheckInfo.getMonitorInfo().getNotifyGroupId());
		info.setItemName(m_httpScenarioCheckInfo.getMonitorInfo().getItemName());
		info.setDisplayName(getDisplayName(page, url));
		return info;
	}

	private String getDisplayName(Page page, String url) {
		int order = page.getId().getPageOrderNo() + 1;
		return url + " (" + order + ")";
	}
	
	@Override
	protected void setCheckInfo() throws MonitorNotFound {
		if(m_httpScenarioCheckInfo == null)
			// HTTP監視情報を取得
			if (!m_isMonitorJob) {
				m_httpScenarioCheckInfo = QueryUtil.getMonitorHttpScenarioInfoPK(m_monitorId);
			} else {
				m_httpScenarioCheckInfo = QueryUtil.getMonitorHttpScenarioInfoPK(m_monitor.getMonitorId());
			}
	}

	@Override
	public String getMessage(int id) {
		throw new UnsupportedOperationException("forbidden to call getMessage() method");
	}

	@Override
	public String getMessageOrg(int id) {
		throw new UnsupportedOperationException("forbidden to call getMessageOrg() method");
	}

	@Override
	public int getCheckResult(boolean ret) {
		throw new UnsupportedOperationException("forbidden to call getCheckResult() method");
	}

	@Override
	protected void setJudgementInfo() {
	}

	@Override
	public boolean collect(String facilityId) throws FacilityNotFound, HinemosUnknown {
		throw new UnsupportedOperationException("forbidden to call collect() method");
	}

	@Override
	protected String makeJobOrgMessage(String orgMsg, String msg) {
		if (m_monitor == null || m_monitor.getHttpScenarioCheckInfo() == null) {
			return "";
		}
		if (m_monitor.getHttpScenarioCheckInfo().getProxyFlg()) {
			String[] args = {String.valueOf(m_monitor.getHttpScenarioCheckInfo().getConnectTimeout()),
					String.valueOf(m_monitor.getHttpScenarioCheckInfo().getRequestTimeout()),
					m_monitor.getHttpScenarioCheckInfo().getUserAgent(),
					m_monitor.getHttpScenarioCheckInfo().getAuthType(),
					m_monitor.getHttpScenarioCheckInfo().getAuthUser(),
					m_monitor.getHttpScenarioCheckInfo().getProxyUrl(),
					String.valueOf(m_monitor.getHttpScenarioCheckInfo().getProxyPort()),
					String.valueOf(m_monitor.getHttpScenarioCheckInfo().getProxyUrl())};
			return MessageConstant.MESSAGE_JOB_MONITOR_ORGMSG_HTTP_SCENARIO_PROXY.getMessage(args)
					+ "\n" + orgMsg;
		} else {
			String[] args = {String.valueOf(m_monitor.getHttpScenarioCheckInfo().getConnectTimeout()),
					String.valueOf(m_monitor.getHttpScenarioCheckInfo().getRequestTimeout()),
					m_monitor.getHttpScenarioCheckInfo().getUserAgent(),
					m_monitor.getHttpScenarioCheckInfo().getAuthType(),
					m_monitor.getHttpScenarioCheckInfo().getAuthUser(),};
			return MessageConstant.MESSAGE_JOB_MONITOR_ORGMSG_HTTP_SCENARIO_NOPROXY.getMessage(args)
					+ "\n" + orgMsg;
		}
	}
}
