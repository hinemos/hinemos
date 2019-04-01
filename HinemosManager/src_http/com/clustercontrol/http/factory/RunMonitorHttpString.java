/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.http.factory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.http.model.HttpCheckInfo;
import com.clustercontrol.http.util.GetHttpResponse;
import com.clustercontrol.http.util.QueryUtil;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.factory.RunMonitorStringValueType;
import com.clustercontrol.repository.util.RepositoryUtil;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.StringBinder;

/**
 * HTTP監視 文字列監視を実行するクラス<BR>
 *
 * @version 5.0.0
 * @since 2.1.0
 */
public class RunMonitorHttpString extends RunMonitorStringValueType {

	private static Log m_log = LogFactory.getLog( RunMonitorHttpString.class );

	/** HTTP監視情報 */
	private HttpCheckInfo m_http = null;

	/** URL */
	private String m_requestUrl = null;

	/** タイムアウト */
	private int m_httpTimeout;

	/** 不明メッセージ */
	private String m_unKnownMessage = null;

	/** オリジナルメッセージ */
	private String m_messageOrg = null;

	/**
	 * コンストラクタ
	 * 
	 */
	public RunMonitorHttpString() {
		super();
	}

	/**
	 * マルチスレッドを実現するCallableTaskに渡すためのインスタンスを作成するメソッド
	 * 
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#runMonitorInfo()
	 * @see com.clustercontrol.monitor.run.util.MonitorExecuteTask
	 */
	@Override
	public RunMonitor createMonitorInstance() {
		return new RunMonitorHttpString();
	}

	/**
	 * HTTP数を取得
	 * 
	 * @param facilityId ファシリティID
	 * @return 値取得に成功した場合、true
	 */
	@Override
	public boolean collect(String facilityId) {

		if (m_now != null) {
			m_nodeDate = m_now.getTime();
		}
		m_value = null;
		m_unKnownMessage = "";
		m_messageOrg = "";

		String url = m_requestUrl;
		// 変数を置換したURLの生成
		if (nodeInfo != null && nodeInfo.containsKey(facilityId)) {
			int maxReplaceWord = HinemosPropertyCommon.replace_param_max.getIntegerValue().intValue();
			ArrayList<String> inKeyList = StringBinder.getKeyList(url, maxReplaceWord);
			Map<String, String> nodeParameter = RepositoryUtil.createNodeParameter(nodeInfo.get(facilityId), inKeyList);
			StringBinder strbinder = new StringBinder(nodeParameter);
			url = strbinder.bindParam(m_requestUrl);
			if (m_log.isTraceEnabled()) m_log.trace("http request. (nodeInfo = " + nodeInfo + ", facilityId = " + facilityId + ", url = " + url + ")");
		}

		boolean result = false;
		try (GetHttpResponse m_request = GetHttpResponse.custom()
				.setConnectTimeout(m_httpTimeout)
				.setRequestTimeout(m_httpTimeout)
				.setNeedAuthSSLCert(! HinemosPropertyCommon.monitor_http_ssl_trustall.getBooleanValue())
				.build()) {
			result = m_request.execute(url);
			if(result &&
					(m_request.getErrorMessage() == null || m_request.getErrorMessage().equals(""))){
				m_value = m_request.getResponseBody();
				if (m_value == null) { // 404などの場合はbodyがnullになってしまう。
					m_value = "";
				}

				StringBuffer response = new StringBuffer();
				response.append(MessageConstant.REQUEST_URL.getMessage() + " : " + url);
				response.append("\n" + MessageConstant.STATUS_CODE.getMessage() + " : " + m_request.getStatusCode());
				if(m_request.getHeaderString() != null && !"".equals(m_request.getHeaderString().trim())){
					response.append("\n" + MessageConstant.HEADER.getMessage() + " :\n" + m_request.getHeaderString().trim());
				}
				if(m_request.getResponseBody() != null && !"".equals(m_request.getResponseBody().trim())){
					response.append("\n" + MessageConstant.RESPONSE_BODY.getMessage() + " :\n" + m_request.getResponseBody().trim());
				}
				m_messageOrg = response.toString();
			}
			else{
				m_unKnownMessage = MessageConstant.MESSAGE_COULD_NOT_GET_VALUE_HTTP.getMessage();

				StringBuffer response = new StringBuffer();
				response.append(m_request.getErrorMessage());
				response.append("\n");
				response.append("\n" + MessageConstant.REQUEST_URL.getMessage() + " : " + url);
				response.append("\n" + MessageConstant.STATUS_CODE.getMessage() + " :\n" + m_request.getStatusCode());
				if(m_request.getHeaderString() != null && !"".equals(m_request.getHeaderString().trim())){
					response.append("\n" + MessageConstant.HEADER.getMessage() + " :\n" + m_request.getHeaderString().trim());
				}
				m_messageOrg = response.toString();
			}
		}
		catch (IOException e) {
			m_log.warn("fail to close HttpClient : " + e.getMessage(), e);
		}
		return result;
	}

	/* (non-Javadoc)
	 * HTTP監視情報を設定
	 * @see com.clustercontrol.monitor.run.factory.OperationNumericValueInfo#setMonitorAdditionInfo()
	 */
	@Override
	protected void setCheckInfo() throws MonitorNotFound {
		if(m_http == null){
			// HTTP監視情報を取得
			if (!m_isMonitorJob) {
				m_http = QueryUtil.getMonitorHttpInfoPK(m_monitorId);
			} else {
				m_http = QueryUtil.getMonitorHttpInfoPK(m_monitor.getMonitorId());
			}
			// HTTP監視情報を設定
			m_requestUrl = m_http.getRequestUrl().trim();
			m_httpTimeout = m_http.getTimeout().intValue();
		}
	}

	/* (非 Javadoc)
	 * ノード用メッセージを取得
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#getMessage(int)
	 */
	@Override
	public String getMessage(int id) {

		String message = super.getMessage(id);
		if(message == null || "".equals(message)){
			return m_unKnownMessage;
		}
		return message;
	}

	/* (非 Javadoc)
	 * ノード用オリジナルメッセージを取得
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#getMessageOrg(int)
	 */
	@Override
	public String getMessageOrg(int id) {
		return m_messageOrg;
	}

	@Override
	protected String makeJobOrgMessage(String orgMsg, String msg) {
		if (m_monitor == null || m_monitor.getHttpCheckInfo() == null) {
			return "";
		}
		String[] args = {String.valueOf(m_monitor.getHttpCheckInfo().getTimeout())};
		return MessageConstant.MESSAGE_JOB_MONITOR_ORGMSG_HTTP.getMessage(args)
				+ "\n" + orgMsg;
	}
}
