/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hinemosagent.factory;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.hinemosagent.util.AgentConnectUtil;
import com.clustercontrol.monitor.run.bean.TruthConstant;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.factory.RunMonitorTruthValueType;
import com.clustercontrol.util.MessageConstant;

/**
 * Hinemos Agent監視を実行するクラス<BR>
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class RunMonitorAgent extends RunMonitorTruthValueType {

	private static Log m_log = LogFactory.getLog( RunMonitorAgent.class );

	/** メッセージ */
	private String m_message = null;

	/**
	 * コンストラクタ
	 * 
	 */
	public RunMonitorAgent() {
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
		return new RunMonitorAgent();
	}

	/**
	 * Hinemos エージェントをチェック
	 * 
	 * @param facilityId ファシリティID
	 * @return 値取得に成功した場合、true
	 */
	@Override
	public boolean collect(String facilityId) {
		boolean duplication = false;

		// 監視開始時刻を設定
		if (m_now != null) {
			m_nodeDate = m_now.getTime();
		}

		//値を初期化
		m_message = "";

		m_value = AgentConnectUtil.isValidAgent(facilityId);
		m_log.debug("checkAgent facilityId=" + facilityId + ", " + m_value);

		// TODO 同一のfacilityIdのノードチェック機構
		if(!duplication){
			if(m_value){
				//OK
				m_message = MessageConstant.MESSAGE_AGENT_IS_AVAILABLE.getMessage();
			} else {
				//NG
				m_message = MessageConstant.MESSAGE_AGENT_IS_NOT_AVAILABLE.getMessage();
			}
		} else {
			//同一ファシリティのAgentの重複
			String[] args = {facilityId};
			m_message = MessageConstant.MESSAGE_MULTIPLE_AGENTS_STARTED_SAME_FACILITY.getMessage(args);
		}

		return true;
	}

	/* (non-Javadoc)
	 * Hinemos Agent監視情報を設定
	 * @see com.clustercontrol.monitor.run.factory.OperationNumericValueInfo#setMonitorAdditionInfo()
	 */
	@Override
	protected void setCheckInfo() {
	}

	/* (non-Javadoc)
	 * ノード用メッセージを取得
	 * @see com.clustercontrol.monitor.run.factory.OperationMonitor#getMessage(int)
	 */
	@Override
	public String getMessage(int id) {
		return m_message;
	}

	/* (non-Javadoc)
	 * ノード用オリジナルメッセージを取得
	 * @see com.clustercontrol.monitor.run.factory.OperationMonitor#getMessageOrg(int)
	 */
	@Override
	public String getMessageOrg(int id) {
		return null;
	}

	@Override
	protected String makeJobOrgMessage(String orgMsg, String msg) {
		if (m_monitorRunResultInfo != null
				&& m_monitorRunResultInfo.getCheckResult() == TruthConstant.TYPE_TRUE) {
			//OK
			return MessageConstant.MESSAGE_AGENT_IS_AVAILABLE.getMessage();
		} else {
			//NG
			return MessageConstant.MESSAGE_AGENT_IS_NOT_AVAILABLE.getMessage();
		}
	}
}
