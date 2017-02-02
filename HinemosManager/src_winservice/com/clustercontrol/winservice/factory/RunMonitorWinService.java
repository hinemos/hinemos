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

package com.clustercontrol.winservice.factory;

import intel.management.wsman.WsmanException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.factory.RunMonitorTruthValueType;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.winservice.model.WinServiceCheckInfo;
import com.clustercontrol.winservice.util.QueryUtil;
import com.clustercontrol.winservice.util.RequestWinRM;

/**
 * Windowsサービス監視クラス
 * 
 * @version 4.0.0
 * @since 4.0.0
 */
public class RunMonitorWinService extends RunMonitorTruthValueType {

	private static Log m_log = LogFactory.getLog( RunMonitorWinService.class );

	/** Windowsサービス監視情報 */
	private WinServiceCheckInfo m_winService = null;

	/** Windowsサービス名 */
	private String m_serviceName;

	private RequestWinRM m_request ;

	/** メッセージ */
	private String m_message = null;

	/** オリジナルメッセージ */
	private String m_messageOrg = null;

	/**
	 * コンストラクタ
	 * 
	 */
	public RunMonitorWinService() {
		super();
	}

	/**
	 * 
	 */
	@Override
	public boolean collect(String facilityId){

		m_value = false;

		try{
			// ノードの属性取得
			NodeInfo info = nodeInfo.get(facilityId);
			if(info == null){
				m_log.info("collect() targe NodeInfo is Null facilityId = " + facilityId);
				return false;
			}

			String ipAddress = info.getAvailableIpAddress();

			String user = info.getWinrmUser();
			String userPassword = info.getWinrmUserPassword();
			int port = info.getWinrmPort();
			String protocol = info.getWinrmProtocol();
			int timeout = info.getWinrmTimeout();
			int retries = info.getWinrmRetries();

			m_request = new RequestWinRM(m_serviceName);
			m_value = m_request.polling(
					ipAddress,
					user,
					userPassword,
					port,
					protocol,
					timeout, retries);

			m_message = m_request.getMessage();
			m_messageOrg = m_request.getMessageOrg();
			m_nodeDate = m_request.getDate();

			return true;
		} catch (WsmanException | HinemosUnknown e) {
			// 不明
			if (!m_isMonitorJob) {
				// 監視ジョブ以外
				m_message = "unknown error . facilityId = " + m_facilityId;
				m_messageOrg = "unknown error . facilityId = " + m_facilityId + ". " + e.getMessage();
			} else {
				// 監視ジョブ
				m_message = "unknown error . facilityId = " + facilityId;
				m_messageOrg = "unknown error . facilityId = " + facilityId + ". " + e.getMessage();
			}
			m_nodeDate = HinemosTime.currentTimeMillis();
			String message = "collect() facilityId = " + facilityId + ", " +
					e.getMessage() + ", class=" + e.getClass().getName();
			if (e instanceof WsmanException) {
				m_log.warn(message);
			} else {
				m_log.debug(message);
			}
			return false;
		} catch (Exception e) {
			// 不明
			if (!m_isMonitorJob) {
				// 監視ジョブ以外
				m_message = "unknown error . facilityId = " + m_facilityId;
				m_messageOrg = "unknown error . facilityId = " + m_facilityId + ". " + e.getMessage();
			} else {
				// 監視ジョブ
				m_message = "unknown error . facilityId = " + facilityId;
				m_messageOrg = "unknown error . facilityId = " + facilityId + ". " + e.getMessage();
			}
			m_nodeDate = HinemosTime.currentTimeMillis();

			m_log.warn("collect() facilityId = " + facilityId, e);

			return false;
		}
	}


	/**
	 * Windowsサービス監視情報を設定
	 */
	@Override
	protected void setCheckInfo() throws MonitorNotFound {
		// Windowsサービス監視情報を取得
		if (!m_isMonitorJob) {
			m_winService = QueryUtil.getMonitorWinserviceInfoPK(m_monitorId);
		} else {
			m_winService = QueryUtil.getMonitorWinserviceInfoPK(m_monitor.getMonitorId());
		}

		// Windowsサービス監視情報を設定
		if(m_winService.getServiceName() != null){
			m_serviceName = m_winService.getServiceName();
		}
	}

	@Override
	protected RunMonitor createMonitorInstance() {
		return new RunMonitorWinService();
	}

	@Override
	public String getMessageOrg(int key) {
		return m_messageOrg;
	}

	@Override
	public String getMessage(int key) {
		return m_message;
	}

	@Override
	protected String makeJobOrgMessage(String orgMsg, String msg) {
		return orgMsg;
	}
}
