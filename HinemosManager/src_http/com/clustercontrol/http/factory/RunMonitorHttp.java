/*

 Copyright (C) 2006 NTT DATA Corporation

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
import java.text.NumberFormat;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.http.model.HttpCheckInfo;
import com.clustercontrol.http.util.GetHttpResponse;
import com.clustercontrol.http.util.QueryUtil;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.factory.RunMonitorNumericValueType;
import com.clustercontrol.repository.util.RepositoryUtil;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.StringBinder;

/**
 * HTTP監視 数値監視を実行するクラス<BR>
 *
 * @version 4.0.0
 * @since 2.1.0
 */
public class RunMonitorHttp extends RunMonitorNumericValueType {

	private static Log m_log = LogFactory.getLog( RunMonitorHttp.class );

	//	/** ノード ネットワーク情報 */
	//	private static final ArrayList<String> m_attributeList = new ArrayList<String>();
	//	static{
	//		m_attributeList.add(FacilityAttributeConstant.IPNETWORKNUMBER);
	//		m_attributeList.add(FacilityAttributeConstant.IPNETWORKNUMBERV6);
	//	}

	/** HTTP監視情報 */
	private HttpCheckInfo m_http = null;

	/** URL */
	private String m_requestUrl = null;

	/** タイムアウト */
	private int m_httpTimeout;

	/** メッセージ */
	private String m_message = "";

	/** オリジナルメッセージ */
	private String m_messageOrg = null;

	/**
	 * コンストラクタ
	 * 
	 */
	public RunMonitorHttp() {
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
		return new RunMonitorHttp();
	}


	/**
	 * HTTP 応答時間（ミリ秒）を取得
	 * 
	 * @param facilityId ファシリティID
	 * @return 値取得に成功した場合、true
	 */
	@Override
	public boolean collect(String facilityId) {

		if (m_now != null){
			m_nodeDate = m_now.getTime();
		}
		m_message = "";
		m_messageOrg = "";

		;

		String url = m_requestUrl;
		// 変数を置換したURLの生成
		if (nodeInfo != null && nodeInfo.containsKey(facilityId)) {
			Map<String, String> nodeParameter = RepositoryUtil.createNodeParameter(nodeInfo.get(facilityId));
			StringBinder strbinder = new StringBinder(nodeParameter);
			url = strbinder.bindParam(m_requestUrl);
			if (m_log.isTraceEnabled()) m_log.trace("http request. (nodeInfo = " + nodeInfo + ", facilityId = " + facilityId + ", url = " + url + ")");
		}

		boolean result = false;
		try (GetHttpResponse m_request = GetHttpResponse.custom()
				.setConnectTimeout(m_httpTimeout)
				.setRequestTimeout(m_httpTimeout)
				.setNeedAuthSSLCert(! HinemosPropertyUtil.getHinemosPropertyBool("monitor.http.ssl.trustall", true))
				.build()) {
			result = m_request.execute(url);
			if(result){
				m_value = (double) m_request.getResponseTime();

				m_message = MessageConstant.RESPONSE_TIME_MILLI_SEC.getMessage() + " : " + NumberFormat.getNumberInstance().format(m_value);

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
				m_message = MessageConstant.MESSAGE_COULD_NOT_GET_VALUE_HTTP.getMessage();

				StringBuffer response = new StringBuffer();
				response.append(m_request.getErrorMessage());
				response.append("\n");
				response.append("\n" + MessageConstant.REQUEST_URL.getMessage() + " : " + url);
				response.append("\n" + MessageConstant.STATUS_CODE.getMessage() + " : " + m_request.getStatusCode());
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
		return m_message;
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
				+ "\n" + orgMsg
				+ "\n" + msg;
	}
}
