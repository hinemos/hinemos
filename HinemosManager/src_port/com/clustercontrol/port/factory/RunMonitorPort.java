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

package com.clustercontrol.port.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.factory.RunMonitorNumericValueType;
import com.clustercontrol.port.bean.PortRunCountConstant;
import com.clustercontrol.port.bean.PortRunIntervalConstant;
import com.clustercontrol.port.bean.ProtocolConstant;
import com.clustercontrol.port.model.MonitorProtocolMstEntity;
import com.clustercontrol.port.model.PortCheckInfo;
import com.clustercontrol.port.protocol.ReachAddressProtocol;
import com.clustercontrol.port.util.QueryUtil;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.MessageConstant;

/**
 * port監視クラス
 * 
 * @version 4.0.0
 * @since 2.4.0
 */
public class RunMonitorPort extends RunMonitorNumericValueType {

	private static Log m_log = LogFactory.getLog( RunMonitorPort.class );

	/** port監視情報 */
	private PortCheckInfo m_port = null;

	/** ポート番号 */
	private int m_portNo;

	/** 試行回数 */
	private int m_runCount = PortRunCountConstant.TYPE_COUNT_01;

	/** 試行間隔（ミリ秒） */
	private int m_runInterval = PortRunIntervalConstant.TYPE_SEC_01;

	/** タイムアウト（ミリ秒） */
	private int m_portTimeout;

	/** サービスID */
	private String m_serviceId;

	/** サービスプロトコル情報 */
	private MonitorProtocolMstEntity m_protocol = null;

	/** メッセージ */
	private String m_message = null;

	/** オリジナルメッセージ */
	private String m_messageOrg = null;

	/** 応答時間（ミリ秒） */
	private long m_response = 0;

	// port実行
	private ReachAddressProtocol m_reachability = null;

	/**
	 * 
	 * コンストラクタ
	 * 
	 */
	public RunMonitorPort() {
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
		return new RunMonitorPort();
	}

	/**
	 * port数を取得
	 * 
	 * @param facilityId
	 *            ファシリティID
	 * @return 値取得に成功した場合、true
	 * @throws FacilityNotFound
	 */
	@Override
	public boolean collect(String facilityId) throws FacilityNotFound, HinemosUnknown {

		if (m_now != null) {
			m_nodeDate = m_now.getTime();
		}
		m_message = "";
		m_messageOrg = "";
		m_response = 0;

		if (m_reachability == null) {
			try {
				// m_serviceIdを基にDBよりクラス名を得る
				m_protocol = QueryUtil.getMonitorProtocolMstPK(m_serviceId);

				String protocolClassName = "";
				// クラス名の取得
				if (m_protocol.getClassName() != null)
					protocolClassName = m_protocol.getClassName();

				// そのクラスのインスタンスを生成する
				Class<?> cls = Class.forName(protocolClassName);
				m_reachability = (ReachAddressProtocol) cls.newInstance();
				m_reachability.setPortNo(m_portNo);
				m_reachability.setSentCount(m_runCount);
				m_reachability.setSentInterval(m_runInterval);
				m_reachability.setTimeout(m_portTimeout);
			} catch (MonitorNotFound e) {
				m_log.debug(e.getMessage(), e);
			} catch (java.lang.ClassNotFoundException e) {
				m_log.info("collect() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
			} catch (java.lang.InstantiationException e) {
				m_log.info("collect() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
			} catch (IllegalAccessException e) {
				m_log.info("collect() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
			}

			if (m_reachability == null)
				throw new HinemosUnknown("ReachAddressProtocol is null, serviceId : " + m_serviceId);
		}

		// ノードの属性取得
		NodeInfo info = new RepositoryControllerBean().getNode(facilityId);

		String ipNetworkNumber = info.getAvailableIpAddress();
		
		String nodeName = info.getNodeName();

		boolean result = m_reachability.isReachable(ipNetworkNumber, nodeName);
		m_message = m_reachability.getMessage();
		m_messageOrg = m_reachability.getMessageOrg();
		if (result) {
			m_response = m_reachability.getResponse();
			m_value = m_response;
		}

		return result;
	}

	/*
	 * (非 Javadoc) port監視情報を設定
	 * 
	 * @see com.clustercontrol.monitor.run.factory.OperationNumericValueInfo#setMonitorAdditionInfo()
	 */
	@Override
	protected void setCheckInfo() throws MonitorNotFound {

		// port監視情報を取得
		if (!m_isMonitorJob) {
			m_port = QueryUtil.getMonitorPortInfoPK(m_monitorId);
		} else {
			m_port = QueryUtil.getMonitorPortInfoPK(m_monitor.getMonitorId());
		}

		// port監視情報を設定
		if (m_port.getPortNo() != null)
			m_portNo = m_port.getPortNo().intValue();
		if (m_port.getRunCount() != null)
			m_runCount = m_port.getRunCount().intValue();
		if (m_port.getRunInterval() != null)
			m_runInterval = m_port.getRunInterval().intValue();
		if (m_port.getTimeout() != null)
			m_portTimeout = m_port.getTimeout().intValue();
		if (m_port.getMonitorProtocolMstEntity() != null
				&& m_port.getMonitorProtocolMstEntity().getServiceId() != null)
			m_serviceId = m_port.getMonitorProtocolMstEntity().getServiceId();
	}

	/*
	 * (非 Javadoc) 判定結果を取得
	 * 
	 * @see com.clustercontrol.monitor.run.factory.RunMonitorNumericValueType#getCheckResult(boolean)
	 */
	@Override
	public int getCheckResult(boolean ret) {
		return super.getCheckResult(ret);
	}

	/*
	 * (非 Javadoc) ノード用メッセージを取得
	 * 
	 * @see com.clustercontrol.monitor.run.factory.OperationMonitor#getMessage(int)
	 */
	@Override
	public String getMessage(int id) {
		return m_message;
	}

	/*
	 * (非 Javadoc) ノード用オリジナルメッセージを取得
	 * 
	 * @see com.clustercontrol.monitor.run.factory.OperationMonitor#getMessageOrg(int)
	 */
	@Override
	public String getMessageOrg(int id) {
		return m_messageOrg;
	}

	@Override
	protected String makeJobOrgMessage(String orgMsg, String msg) {
		if (m_monitor == null || m_monitor.getPortCheckInfo() == null) {
			return "";
		}
		String type = m_monitor.getPortCheckInfo().getServiceId();
		String typeStr = "";
		if (type.equals(ProtocolConstant.TYPE_PROTOCOL_TCP)) {
			typeStr = MessageConstant.TCP_CONNECT_ONLY.getMessage();
		} else {
			String subTypeStr = "";
			if (type.equals(ProtocolConstant.TYPE_PROTOCOL_FTP)) {
				subTypeStr = MessageConstant.PROTOCOL_FTP.getMessage();
			} else if (type.equals(ProtocolConstant.TYPE_PROTOCOL_SMTP)) {
				subTypeStr = MessageConstant.PROTOCOL_SMTP.getMessage();
			} else if (type.equals(ProtocolConstant.TYPE_PROTOCOL_SMTPS)) {
				subTypeStr = MessageConstant.PROTOCOL_SMTPS.getMessage();
			} else if (type.equals(ProtocolConstant.TYPE_PROTOCOL_POP3)) {
				subTypeStr = MessageConstant.PROTOCOL_POP3.getMessage();
			} else if (type.equals(ProtocolConstant.TYPE_PROTOCOL_POP3S)) {
				subTypeStr = MessageConstant.PROTOCOL_POP3S.getMessage();
			} else if (type.equals(ProtocolConstant.TYPE_PROTOCOL_IMAP)) {
				subTypeStr = MessageConstant.PROTOCOL_IMAP.getMessage();
			} else if (type.equals(ProtocolConstant.TYPE_PROTOCOL_IMAPS)) {
				subTypeStr = MessageConstant.PROTOCOL_IMAPS.getMessage();
			} else if (type.equals(ProtocolConstant.TYPE_PROTOCOL_NTP)) {
				subTypeStr = MessageConstant.PROTOCOL_NTP.getMessage();
			} else if (type.equals(ProtocolConstant.TYPE_PROTOCOL_DNS)) {
				subTypeStr = MessageConstant.PROTOCOL_DNS.getMessage();
			}
			typeStr = MessageConstant.SERVICE_PROTOCOL.getMessage(new String[]{subTypeStr});
		}
		String[] args = {
				typeStr,
				String.valueOf(m_monitor.getPortCheckInfo().getPortNo()),
				String.valueOf(m_monitor.getPortCheckInfo().getRunCount()),
				String.valueOf(m_monitor.getPortCheckInfo().getRunInterval()),
				String.valueOf(m_monitor.getPortCheckInfo().getTimeout())};
		return MessageConstant.MESSAGE_JOB_MONITOR_ORGMSG_PORT.getMessage(args)
				+ "\n" + orgMsg;
	}
}