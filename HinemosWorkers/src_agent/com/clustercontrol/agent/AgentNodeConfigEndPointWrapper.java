/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
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
import com.clustercontrol.agent.repository.NodeConfigResult;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.ws.agentnodeconfig.AgentNodeConfigEndpoint;
import com.clustercontrol.ws.agentnodeconfig.FacilityNotFound_Exception;
import com.clustercontrol.ws.agentnodeconfig.HinemosDbTimeout_Exception;
import com.clustercontrol.ws.agentnodeconfig.HinemosUnknown_Exception;
import com.clustercontrol.ws.agentnodeconfig.InvalidRole_Exception;
import com.clustercontrol.ws.agentnodeconfig.InvalidSetting_Exception;
import com.clustercontrol.ws.agentnodeconfig.InvalidUserPass_Exception;
import com.clustercontrol.ws.agentnodeconfig.NodeConfigSettingDuplicate_Exception;
import com.clustercontrol.ws.agentnodeconfig.NodeConfigSettingNotFound_Exception;
import com.clustercontrol.ws.agentnodeconfig.NodeHistoryRegistered_Exception;
import com.clustercontrol.ws.agentnodeconfig.NodeNetworkInterfaceInfo;
import com.clustercontrol.ws.repository.AutoRegisterResult;
import com.clustercontrol.ws.repository.NodeConfigRunCollectInfo;
import com.clustercontrol.ws.repository.NodeConfigSetting;

/**
 * CMDB用マネージャー通信クラス.<br>
 * <br>
 * Hinemosマネージャとの通信をするクラス.<br>
 * HA(ミッションクリティカルオプション)のような複数マネージャ対応のため、このクラスを実装する.<br>
 * <br>
 * マネージャのみ最新バージョンの場合でも動作させることを考慮して既存の通信クラスとは別クラスとして作成.<br>
 * <br>
 * Hinemosマネージャと通信できない場合は、WebServiceExceptionがthrowされる.<br>
 * WebServiceExeptionが出力された場合は、もう一台のマネージャと通信する.<br>
 * 
 * @since 6.2.0
 * @version 6.2.0
 */
public class AgentNodeConfigEndPointWrapper {

	// ロガー
	private static Log m_log = LogFactory.getLog(AgentNodeConfigEndPointWrapper.class);

	/**
	 * [Node Config] ノード自動登録.
	 * 
	 * @param platform
	 *            AgentのOS(OS別スコープ登録用).
	 * @param nodeNifList
	 *            AgentのNIF情報リスト(MACアドレスと主キー必須)
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidSetting_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 * @throws HinemosDbTimeout_Exception
	 */
	public static AutoRegisterResult registerNode(String platform, List<NodeNetworkInterfaceInfo> nodeNifList)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidSetting_Exception, InvalidUserPass_Exception,
			HinemosDbTimeout_Exception {

		WebServiceException wse = null;
		for (EndpointSetting endpointSetting : EndpointManager.getAgenNodeConfigEndpoint()) {
			try {
				AgentNodeConfigEndpoint endpoint = (AgentNodeConfigEndpoint) endpointSetting.getEndpoint();
				return (AutoRegisterResult) endpoint.registerNode(platform, nodeNifList);
			} catch (WebServiceException e) {
				wse = e;
				m_log.info("WebServiceException " + e.getMessage());
				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
	}
	
	/**
	 * 構成情報取得
	 * 
	 */
	public static List<NodeConfigSetting> getNodeConfigSetting() throws HinemosUnknown_Exception,
			InvalidRole_Exception, InvalidSetting_Exception, InvalidUserPass_Exception, FacilityNotFound_Exception, NodeConfigSettingNotFound_Exception {
		
		WebServiceException wse = null;
		for (EndpointSetting endpointSetting : EndpointManager.getAgenNodeConfigEndpoint()) {
			try {
				AgentNodeConfigEndpoint endpoint = (AgentNodeConfigEndpoint) endpointSetting.getEndpoint();
				return endpoint.getNodeConfigSetting(Agent.getAgentInfo());
			} catch (WebServiceException e) {
				wse = e;
				m_log.info("WebServiceException : " + e.getMessage());
				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
	}
	
	
	/**
	 * 構成情報送信
	 * @param resultList
	 * @throws HinemosUnknown
	 * @throws InvalidRole_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static List<NodeConfigResult> forwardNodeConfigSettingResult(List<NodeConfigResult> resultList) {
		// Local Variables
		WebServiceException wse = null;
		List<NodeConfigResult> failedList = new ArrayList<NodeConfigResult>();
		
		// MAIN
		for (EndpointSetting endpointSetting : EndpointManager.getAgenNodeConfigEndpoint()) {
			try {
				AgentNodeConfigEndpoint endpoint = (AgentNodeConfigEndpoint) endpointSetting.getEndpoint();
					for (NodeConfigResult result : resultList) {
						// 送信時に失敗しても次の結果を送信してやる.
						try {
							endpoint.registerNodeConfigInfo(result.getAquireDate(), result.getNodeInfo());
							continue;
						} catch (FacilityNotFound_Exception e) {
							m_log.warn("FacilityNotFound_Exception : " + e.getMessage());
							failedList.add(result);
						} catch (InvalidSetting_Exception e) {
							// 再送させない(エージェントから送ってる情報がおかしい、Manager側でInternalError通知されてる).
							m_log.warn("InvalidSetting_Exception : " + e.getMessage());
						} catch (NodeConfigSettingDuplicate_Exception e) {
							m_log.warn("NodeConfigSettingDuplicate_Exception : " + e.getMessage());
							failedList.add(result);
						} catch (NodeConfigSettingNotFound_Exception e) {
							m_log.warn("NodeConfigSettingNotFound_Exception : " + e.getMessage());
							failedList.add(result);
						} catch (HinemosUnknown_Exception e) {
							m_log.warn("HinemosUnknown_Exception : " + e.getMessage());
							failedList.add(result);
						} catch (InvalidRole_Exception e) {
							m_log.warn("InvalidRole_Exception : " + e.getMessage());
							failedList.add(result);
						} catch (InvalidUserPass_Exception e) {
							m_log.warn("InvalidUserPass_Exception : " + e.getMessage());
							failedList.add(result);
						} catch (NodeHistoryRegistered_Exception e) {
							// 再送させない(SocketTimeOutException等で接続中断されたがManagerには登録完了している状態).
							m_log.warn("NodeHistoryRegistered_Exception : " + e.getMessage());
						} catch (Exception e) {
							m_log.warn("Exception : " + e.getMessage());
							failedList.add(result);
						}
					}
				return failedList;
			} catch (WebServiceException e) {
				wse = e;
				m_log.info("WebServiceException : " + e.getMessage());
				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
		
	}

	/**
	 * [Node Config] 即時実行情報の取得
	 * 
	 * @throws InvalidUserPass_Exception
	 * @throws FacilityNotFound_Exception 
	 * @throws InvalidRole_Exceptio
	 * @throws HinemosUnknown_Exceptio
	 * 
	 */
	public static NodeConfigRunCollectInfo getNodeConfigRunCollectInfo()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, FacilityNotFound_Exception {

		WebServiceException wse = null;
		for (EndpointSetting endpointSetting : EndpointManager.getAgenNodeConfigEndpoint()) {
			try {
				AgentNodeConfigEndpoint endpoint = (AgentNodeConfigEndpoint) endpointSetting.getEndpoint();
				return endpoint.getNodeConfigRunCollectInfo(Agent.getAgentInfo());
			} catch (WebServiceException e) {
				wse = e;
				m_log.info("WebServiceException : " + e.getMessage());
				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
	}

	/**
	 * [Node Config] 即時実行の中止.
	 * 
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 * @throws FacilityNotFound_Exception 
	 */
	public static void stopNodeConfigRunCollect()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, FacilityNotFound_Exception {

		WebServiceException wse = null;
		for (EndpointSetting endpointSetting : EndpointManager.getAgenNodeConfigEndpoint()) {
			try {
				AgentNodeConfigEndpoint endpoint = (AgentNodeConfigEndpoint) endpointSetting.getEndpoint();
				endpoint.stopNodeConfigRunCollect(Agent.getAgentInfo());
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.info("WebServiceException : " + e.getMessage());
				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
	}
}
