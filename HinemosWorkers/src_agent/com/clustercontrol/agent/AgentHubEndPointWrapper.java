package com.clustercontrol.agent;

import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.EndpointManager.EndpointSetting;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.util.XMLUtil;
import com.clustercontrol.ws.agenthub.AgentHubEndpoint;
import com.clustercontrol.ws.agenthub.HinemosUnknown_Exception;
import com.clustercontrol.ws.agenthub.InvalidRole_Exception;
import com.clustercontrol.ws.agenthub.InvalidUserPass_Exception;
import com.clustercontrol.ws.agenthub.WinEventResultDTO;
import com.clustercontrol.ws.monitor.CommandResultDTO;
import com.clustercontrol.ws.monitor.LogfileResultDTO;

/**
 * Hinemosマネージャとの通信をするクラス。
 * HAのような複数マネージャ対応のため、このクラスを実装する。
 * 
 * Hinemosマネージャと通信できない場合は、WebServiceExceptionがthrowされる。
 * WebServiceExeptionが出力された場合は、もう一台のマネージャと通信する。
 */
public class AgentHubEndPointWrapper {

	//ロガー
	private static Log m_log = LogFactory.getLog(AgentHubEndPointWrapper.class);

	public static void forwardCustomResult(List<CommandResultDTO> resultList) 
			throws HinemosUnknown, InvalidRole_Exception, InvalidUserPass_Exception, HinemosUnknown_Exception {
		// Local Variables
		WebServiceException wse = null;
		
		// MAIN
		for (EndpointSetting endpointSetting : EndpointManager.getAgentHubEndpoint()) {
			try {
				AgentHubEndpoint endpoint = (AgentHubEndpoint) endpointSetting.getEndpoint();
		
				// Ignore Invalid XML Chars
				for (CommandResultDTO result : resultList) {
					result.setStderr(XMLUtil.ignoreInvalidString(result.getStderr()));
					result.setStdout(XMLUtil.ignoreInvalidString(result.getStdout()));
				}
		
				endpoint.forwardCustomResult(resultList);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.info("WebServiceException " + e.getMessage());
				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
		
	}
	
	public static void forwardLogfileResult(List<LogfileResultDTO> resultList) 
			throws HinemosUnknown, InvalidRole_Exception, InvalidUserPass_Exception, HinemosUnknown_Exception {
		// Local Variables
		WebServiceException wse = null;
		
		// MAIN
		for (EndpointSetting endpointSetting : EndpointManager.getAgentHubEndpoint()) {
			try {
				AgentHubEndpoint endpoint = (AgentHubEndpoint) endpointSetting.getEndpoint();
		
				// Ignore Invalid XML Chars
				for (LogfileResultDTO result : resultList) {
					result.setMessage(XMLUtil.ignoreInvalidString(result.getMessage()));
					result.getMsgInfo().setMessage(XMLUtil.ignoreInvalidString(result.getMsgInfo().getMessage()));
				}
		
				endpoint.forwardLogfileResult(resultList, Agent.getAgentInfo());
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.info("WebServiceException " + e.getMessage());
				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public static void forwardWinEventResult(List<WinEventResultDTO> resultList) 
			throws HinemosUnknown, InvalidRole_Exception, InvalidUserPass_Exception, HinemosUnknown_Exception {
		// Local Variables
		WebServiceException wse = null;
		
		// MAIN
		for (EndpointSetting endpointSetting : EndpointManager.getAgentHubEndpoint()) {
			try {
				AgentHubEndpoint endpoint = (AgentHubEndpoint) endpointSetting.getEndpoint();
		
				// Ignore Invalid XML Chars
				for (WinEventResultDTO result : resultList) {
					result.setMessage(XMLUtil.ignoreInvalidString(result.getMessage()));
					result.getMsgInfo().setMessage(XMLUtil.ignoreInvalidString(result.getMsgInfo().getMessage()));
				}
		
				endpoint.forwardWinEventResult(resultList, Agent.getAgentInfo());
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
