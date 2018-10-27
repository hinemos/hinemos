/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.EndpointManager.EndpointSetting;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.util.XMLUtil;
import com.clustercontrol.ws.agent.AgentEndpoint;
import com.clustercontrol.ws.agent.HashMapInfo;
import com.clustercontrol.ws.agentbinary.AgentBinaryEndpoint;
import com.clustercontrol.ws.agentbinary.HinemosUnknown_Exception;
import com.clustercontrol.ws.agentbinary.InvalidRole_Exception;
import com.clustercontrol.ws.agentbinary.InvalidUserPass_Exception;
import com.clustercontrol.ws.agentbinary.MonitorNotFound_Exception;
import com.clustercontrol.ws.monitor.BinaryResultDTO;
import com.clustercontrol.ws.monitor.MonitorInfo;

/**
 * バイナリ用マネージャー通信クラス.<br>
 * <br>
 * Hinemosマネージャとの通信をするクラス.<br>
 * HAのような複数マネージャ対応のため、このクラスを実装する.<br>
 * <br>
 * マネージャのみ最新バージョンの場合でも動作させることを考慮して.<br>
 * 既存の通信クラスとは別クラスとして作成.<br>
 * <br>
 * Hinemosマネージャと通信できない場合は、WebServiceExceptionがthrowされる.<br>
 * WebServiceExeptionが出力された場合は、もう一台のマネージャと通信する.<br>
 * 
 * @since 6.1.0
 * @version 6.1.0
 */
public class AgentBinaryEndPointWrapper {

	// ロガー
	private static Log m_log = LogFactory.getLog(AgentBinaryEndPointWrapper.class);

	/**
	 * バイナリ監視設定取得,
	 * 
	 */
	public static ArrayList<MonitorInfo> getMonitorBinary() throws HinemosUnknown_Exception, InvalidRole_Exception,
			InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting endpointSetting : EndpointManager.getAgenBinaryEndpoint()) {
			try {
				AgentBinaryEndpoint endpoint = (AgentBinaryEndpoint) endpointSetting.getEndpoint();
				return (ArrayList<MonitorInfo>) endpoint.getMonitorBinary(Agent.getAgentInfo());
			} catch (WebServiceException e) {
				wse = e;
				m_log.info("WebServiceException " + e.getMessage());
				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
	}

	/**
	 * バイナリファイル監視ジョブ設定取得,
	 * 
	 * @throws com.clustercontrol.ws.agent.MonitorNotFound_Exception
	 * @throws com.clustercontrol.ws.agent.InvalidUserPass_Exception
	 * @throws com.clustercontrol.ws.agent.InvalidRole_Exception
	 * @throws com.clustercontrol.ws.agent.HinemosUnknown_Exception
	 * 
	 */
	public static HashMapInfo getMonitorJobBinaryFile() throws com.clustercontrol.ws.agent.HinemosUnknown_Exception,
			com.clustercontrol.ws.agent.InvalidRole_Exception, com.clustercontrol.ws.agent.InvalidUserPass_Exception,
			com.clustercontrol.ws.agent.MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting endpointSetting : EndpointManager.getAgentEndpoint()) {
			try {
				AgentEndpoint endpoint = (AgentEndpoint) endpointSetting.getEndpoint();
				return endpoint.getMonitorJobMap(HinemosModuleConstant.MONITOR_BINARYFILE_BIN, Agent.getAgentInfo());
			} catch (WebServiceException e) {
				wse = e;
				m_log.info("WebServiceException " + e.getMessage());
				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
	}

	/**
	 * パケットキャプチャ監視ジョブ設定取得,
	 * 
	 * @throws com.clustercontrol.ws.agent.MonitorNotFound_Exception
	 * @throws com.clustercontrol.ws.agent.InvalidUserPass_Exception
	 * @throws com.clustercontrol.ws.agent.InvalidRole_Exception
	 * @throws com.clustercontrol.ws.agent.HinemosUnknown_Exception
	 * 
	 */
	public static HashMapInfo getMonitorJobPcap() throws com.clustercontrol.ws.agent.HinemosUnknown_Exception,
			com.clustercontrol.ws.agent.InvalidRole_Exception, com.clustercontrol.ws.agent.InvalidUserPass_Exception,
			com.clustercontrol.ws.agent.MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting endpointSetting : EndpointManager.getAgentEndpoint()) {
			try {
				AgentEndpoint endpoint = (AgentEndpoint) endpointSetting.getEndpoint();
				return endpoint.getMonitorJobMap(HinemosModuleConstant.MONITOR_PCAP_BIN, Agent.getAgentInfo());
			} catch (WebServiceException e) {
				wse = e;
				m_log.info("WebServiceException " + e.getMessage());
				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
	}

	/**
	 * マネージャーにバイナリデータ送信<br>
	 * <br>
	 * バイナリデータはBase64エンコードの上xml埋め込み.<br>
	 * 
	 * @param resultList
	 *            バイナリ監視結果DTO
	 * @throws HinemosUnknown_Exception
	 * @throws InvalidRole_Exception
	 * @throws InvalidUserPass_Exception
	 * 
	 */
	public static void forwardBinaryResult(List<BinaryResultDTO> resultList)
			throws HinemosUnknown, InvalidRole_Exception, InvalidUserPass_Exception, HinemosUnknown_Exception {
		// Local Variables
		WebServiceException wse = null;

		// MAIN
		for (EndpointSetting endpointSetting : EndpointManager.getAgenBinaryEndpoint()) {
			try {
				AgentBinaryEndpoint endpoint = (AgentBinaryEndpoint) endpointSetting.getEndpoint();

				// Ignore Invalid XML Chars
				for (BinaryResultDTO result : resultList) {
					result.getMsgInfo().setMessage(XMLUtil.ignoreInvalidString(result.getMsgInfo().getMessage()));
				}
				endpoint.forwardBinaryResult(resultList, Agent.getAgentInfo());
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.info("WebServiceException " + e.getMessage());
				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
	}
}
