/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.snmp.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.factory.RunMonitorStringValueType;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.snmp.model.SnmpCheckInfo;
import com.clustercontrol.snmp.util.QueryUtil;
import com.clustercontrol.snmp.util.RequestSnmp4j;
import com.clustercontrol.util.MessageConstant;

/**
 * SNMP監視 文字列監視を実行するファクトリークラス<BR>
 *
 * @version 4.0.0
 * @since 2.1.0
 */
public class RunMonitorSnmpString extends RunMonitorStringValueType {

	private static Log m_log = LogFactory.getLog( RunMonitorSnmpString.class );

	/** SNMP監視情報 */
	private SnmpCheckInfo m_snmp = null;

	/** OID */
	private String m_snmpOid = null;

	/** オリジナルメッセージ */
	private String m_messageOrg = null;

	/** メッセージ */
	private String m_message = "";

	/**
	 * コンストラクタ
	 * 
	 */
	public RunMonitorSnmpString() {
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
		return new RunMonitorSnmpString();
	}

	/**
	 * OID値を取得
	 * 
	 * @param facilityId ファシリティID
	 * @return 値取得に成功した場合、true
	 * @throws HinemosUnknown
	 */
	@Override
	public boolean collect(String facilityId) throws HinemosUnknown {

		if (m_now != null) {
			m_nodeDate = m_now.getTime();
		}
		m_value = null;

		// メッセージを設定
		m_message = "";
		m_messageOrg = MessageConstant.OID.getMessage() + " : " + m_snmpOid;

		NodeInfo info = null;
		try {
			// ノードの属性取得
			info = new RepositoryControllerBean().getNode(facilityId);
		}
		catch(FacilityNotFound e){
			m_message = MessageConstant.MESSAGE_COULD_NOT_GET_NODE_ATTRIBUTES.getMessage();
			m_messageOrg = m_messageOrg + " (" + e.getMessage() + ")";
			return false;
		}

		// SNMP値取得
		RequestSnmp4j m_request = new RequestSnmp4j();

		m_log.debug("version=" + info.getSnmpVersion());
		boolean result = false;
		try {
			result = m_request.polling(
					info.getAvailableIpAddress(),
					info.getSnmpCommunity(),
					info.getSnmpPort(),
					m_snmpOid,
					info.getSnmpVersion(),
					info.getSnmpTimeout(),
					info.getSnmpRetryCount(),
					info.getSnmpSecurityLevel(),
					info.getSnmpUser(),
					info.getSnmpAuthPassword(),
					info.getSnmpPrivPassword(),
					info.getSnmpAuthProtocol(),
					info.getSnmpPrivProtocol()
					);
		} catch (Exception e) {
			m_message = MessageConstant.MESSAGE_COULD_NOT_GET_NODE_ATTRIBUTES.getMessage();
			m_messageOrg = m_message + ", " + e.getMessage() + " (" + e.getClass().getName() + ")";
			if (e instanceof NumberFormatException) {
				m_log.warn(m_messageOrg);
			} else {
				m_log.warn(m_messageOrg, e);
			}
			return false;
		}

		if(result){

			m_value = m_request.getValue();
			m_nodeDate = m_request.getDate();

			m_messageOrg = m_messageOrg + ", " + MessageConstant.SELECT_VALUE.getMessage() + " : " + m_value;
		}
		else{
			m_message = m_request.getMessage();
		}
		return result;
	}

	/* (非 Javadoc)
	 * SNMP監視情報を設定
	 * @see com.clustercontrol.monitor.run.factory.OperationNumericValueInfo#setMonitorAdditionInfo()
	 */
	@Override
	protected void setCheckInfo() throws MonitorNotFound {

		// SNMP監視情報を取得
		if (!m_isMonitorJob) {
			m_snmp = QueryUtil.getMonitorSnmpInfoPK(m_monitorId);
		} else {
			m_snmp = QueryUtil.getMonitorSnmpInfoPK(m_monitor.getMonitorId());
		}

		// SNMP監視情報を設定
		m_snmpOid = m_snmp.getSnmpOid().trim();
	}

	/* (非 Javadoc)
	 * ノード用メッセージを取得
	 * @see com.clustercontrol.monitor.run.factory.OperationMonitor#getMessage(int)
	 */
	@Override
	public String getMessage(int id) {

		String message = super.getMessage(id);
		if(message == null || "".equals(message)){
			return m_message;
		}
		return message;
	}

	/* (非 Javadoc)
	 * ノード用オリジナルメッセージを取得
	 * @see com.clustercontrol.monitor.run.factory.OperationMonitor#getMessageOrg(int)
	 */
	@Override
	public String getMessageOrg(int id) {
		return m_messageOrg;
	}

	@Override
	protected String makeJobOrgMessage(String orgMsg, String msg) {
		String rtnmsg = "";
		if (m_monitorRunResultInfo != null
				&& m_monitorRunResultInfo.getCheckResult() != -1) {
			//OK
			rtnmsg = orgMsg;
		} else {
			//NG
			rtnmsg = msg;
		}
		return rtnmsg;
	}
}
