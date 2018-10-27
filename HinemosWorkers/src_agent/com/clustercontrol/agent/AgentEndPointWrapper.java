/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.activation.DataHandler;
import javax.xml.ws.WebServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.EndpointManager.EndpointSetting;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.util.StringBinder;
import com.clustercontrol.util.XMLUtil;
import com.clustercontrol.ws.agent.AgentEndpoint;
import com.clustercontrol.ws.agent.AgentOutputBasicInfo;
import com.clustercontrol.ws.agent.CustomInvalid_Exception;
import com.clustercontrol.ws.agent.FacilityNotFound_Exception;
import com.clustercontrol.ws.agent.HashMapInfo;
import com.clustercontrol.ws.agent.HinemosTopicInfo;
import com.clustercontrol.ws.agent.HinemosUnknown_Exception;
import com.clustercontrol.ws.agent.InvalidRole_Exception;
import com.clustercontrol.ws.agent.InvalidUserPass_Exception;
import com.clustercontrol.ws.agent.JobInfoNotFound_Exception;
import com.clustercontrol.ws.agent.JobMasterNotFound_Exception;
import com.clustercontrol.ws.agent.JobSessionDuplicate_Exception;
import com.clustercontrol.ws.agent.MonitorNotFound_Exception;
import com.clustercontrol.ws.agent.OutputBasicInfo;
import com.clustercontrol.ws.jobmanagement.JobFileCheck;
import com.clustercontrol.ws.jobmanagement.RunResultInfo;
import com.clustercontrol.ws.monitor.CommandExecuteDTO;
import com.clustercontrol.ws.monitor.MonitorInfo;

/**
 * Hinemosマネージャとの通信をするクラス。
 * HAのような複数マネージャ対応のため、このクラスを実装する。
 * 
 * Hinemosマネージャと通信できない場合は、WebServiceExceptionがthrowされる。
 * WebServiceExeptionが出力された場合は、もう一台のマネージャと通信する。
 */
public class AgentEndPointWrapper {

	//ロガー
	private static Log m_log = LogFactory.getLog(AgentEndPointWrapper.class);

	// for trace
	private static ConcurrentHashMap<String, Long> counterMap ;
	static {
		counterMap = new ConcurrentHashMap<String, Long>();
	}
	
	/** Invalidな文字を置換する場合の置換文字のキー */
	private static final String MESSAGE_REPLACE_METHOD_KEY = "common.invalid.char.replace";
	/** Invalidな文字を置換する場合の置換文字のキー */
	private static final String MESSAGE_REPLACE_CHAR_KEY = "common.invalid.char.replace.to";

	static {
		boolean invalidCharReplace = Boolean.parseBoolean(AgentProperties.getProperty(MESSAGE_REPLACE_METHOD_KEY, "false"));
		XMLUtil.setReplace(invalidCharReplace);
		StringBinder.setReplace(invalidCharReplace);

		String replaceCharString = AgentProperties.getProperty(MESSAGE_REPLACE_CHAR_KEY, " ");
		if(replaceCharString != null){
			XMLUtil.setReplaceChar(replaceCharString);
			StringBinder.setReplaceChar(replaceCharString);
		}
	}

	public static boolean jobResult(RunResultInfo resultInfo)
			throws HinemosUnknown_Exception, JobInfoNotFound_Exception,
			InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting endpointSetting : EndpointManager.getAgentEndpoint()) {
			try {
				AgentEndpoint endpoint = (AgentEndpoint) endpointSetting.getEndpoint();

				// Ignore Invalid XML Chars
				resultInfo.setMessage(XMLUtil.ignoreInvalidString(resultInfo.getMessage()));
				resultInfo.setErrorMessage(XMLUtil.ignoreInvalidString(resultInfo.getErrorMessage()));

				return endpoint.jobResult(resultInfo);
			} catch (WebServiceException e) {
				wse = e;
				m_log.info("jobResult " + e.getMessage());
				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
	}

	public static void sendMessage(AgentOutputBasicInfo info)
			throws FacilityNotFound_Exception, HinemosUnknown_Exception,
			InvalidRole_Exception, InvalidUserPass_Exception {
		info.setAgentInfo(Agent.getAgentInfo());
		WebServiceException wse = null;
		for (EndpointSetting endpointSetting : EndpointManager.getAgentEndpoint()) {
			try {
				AgentEndpoint endpoint = (AgentEndpoint) endpointSetting.getEndpoint();

				// Ignore Invalid XML Chars
				OutputBasicInfo outputInfo = info.getOutputBasicInfo();
				outputInfo.setMessage(XMLUtil.ignoreInvalidString(outputInfo.getMessage()));
				outputInfo.setMessageOrg(XMLUtil.ignoreInvalidString(outputInfo.getMessageOrg()));

				endpoint.sendMessage(info);
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.info("WebServiceException " + e.getMessage());
				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
	}

	public static HinemosTopicInfo getHinemosTopic()
			throws InvalidRole_Exception, InvalidUserPass_Exception, HinemosUnknown_Exception {
		
		//for #2478、#2479
		if(m_log.isTraceEnabled()){
			String threadName = Thread.currentThread().getName();
			Long counter = counterMap.get(threadName);
			if(counter == null){
				counter = Long.valueOf(0);
			}
			counter = counter.longValue() + (long)1;
			counterMap.put(threadName, counter);
			m_log.trace("getHinemosTopic() for debug : threadName = " + threadName + ", counter = " + counter);
		}
		
		WebServiceException wse = null;
		for (EndpointSetting endpointSetting : EndpointManager.getAgentEndpoint()) {
			try {
				AgentEndpoint endpoint = (AgentEndpoint) endpointSetting.getEndpoint();
				return endpoint.getHinemosTopic(Agent.getAgentInfo());
			} catch (WebServiceException e) {
				wse = e;
				m_log.info("WebServiceException " + e.getMessage());
				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
	}

	public static ArrayList<CommandExecuteDTO> getCommandExecuteDTOs()
			throws HinemosUnknown_Exception, CustomInvalid_Exception,
			InvalidRole_Exception, InvalidUserPass_Exception {
		// Local Variables
		WebServiceException wse = null;
		ArrayList<CommandExecuteDTO> wsDTO = null;

		// MAIN
		for (EndpointSetting endpointSetting : EndpointManager.getAgentEndpoint()) {
			try {
				AgentEndpoint endpoint = (AgentEndpoint) endpointSetting.getEndpoint();
				// 監視ジョブ以外
				wsDTO = (ArrayList<CommandExecuteDTO>)endpoint.getCommandExecuteDTO(Agent.getAgentInfo());
				// 監視ジョブ
				wsDTO.addAll((ArrayList<CommandExecuteDTO>)endpoint.getCommandExecuteDTOForMonitorJob(Agent.getAgentInfo()));
				return wsDTO;
			} catch (WebServiceException e) {
				wse = e;
				m_log.info("communication failure to manager...", e);
				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
	}

//	public static void putCommandResultDTO(CommandResultDTO result)
//			throws HinemosUnknown, InvalidRole_Exception,
//			InvalidUserPass_Exception, HinemosUnknown_Exception {
//		// Local Variables
//		WebServiceException wse = null;
//
//		// MAIN
//		for (EndpointSetting endpointSetting : EndpointManager.getAgentEndpoint()) {
//			try {
//				AgentEndpoint endpoint = (AgentEndpoint) endpointSetting.getEndpoint();
//
//				// Ignore Invalid XML Chars
//				result.setStderr(XMLUtil.ignoreInvalidString(result.getStderr()));
//				result.setStdout(XMLUtil.ignoreInvalidString(result.getStdout()));
//
//				endpoint.putCommandResultDTO(result);
//				return;
//			} catch (WebServiceException e) {
//				wse = e;
//				m_log.info("WebServiceException " + e.getMessage());
//				EndpointManager.changeEndpoint();
//			}
//		}
//		throw wse;
//	}
//	
//	public static void forwardCustomResult(List<CommandResultDTO> resultList) 
//			throws HinemosUnknown, InvalidRole_Exception, InvalidUserPass_Exception, HinemosUnknown_Exception {
//		// Local Variables
//		WebServiceException wse = null;
//		
//		// MAIN
//		for (EndpointSetting endpointSetting : EndpointManager.getAgentEndpoint()) {
//			try {
//				AgentEndpoint endpoint = (AgentEndpoint) endpointSetting.getEndpoint();
//		
//				// Ignore Invalid XML Chars
//				for (CommandResultDTO result : resultList) {
//					result.setStderr(XMLUtil.ignoreInvalidString(result.getStderr()));
//					result.setStdout(XMLUtil.ignoreInvalidString(result.getStdout()));
//				}
//		
//				endpoint.forwardCustomResult(resultList);
//				return;
//			} catch (WebServiceException e) {
//				wse = e;
//				m_log.info("WebServiceException " + e.getMessage());
//				EndpointManager.changeEndpoint();
//			}
//		}
//		throw wse;
//		
//	}
//	
//	public static void forwardLogfileResult(List<LogfileResultDTO> resultList) 
//			throws HinemosUnknown, InvalidRole_Exception, InvalidUserPass_Exception, HinemosUnknown_Exception {
//		// Local Variables
//		WebServiceException wse = null;
//		
//		// MAIN
//		for (EndpointSetting endpointSetting : EndpointManager.getAgentEndpoint()) {
//			try {
//				AgentEndpoint endpoint = (AgentEndpoint) endpointSetting.getEndpoint();
//		
//				// Ignore Invalid XML Chars
//				for (LogfileResultDTO result : resultList) {
//					result.setMessage(XMLUtil.ignoreInvalidString(result.getMessage()));
//					result.getMsgInfo().setMessage(XMLUtil.ignoreInvalidString(result.getMsgInfo().getMessage()));
//				}
//		
//				endpoint.forwardLogfileResult(resultList, Agent.getAgentInfo());
//				return;
//			} catch (WebServiceException e) {
//				wse = e;
//				m_log.info("WebServiceException " + e.getMessage());
//				EndpointManager.changeEndpoint();
//			}
//		}
//		throw wse;
//	}
//	
//	public static void forwardWinEventResult(List<WinEventResultDTO> resultList) 
//			throws HinemosUnknown, InvalidRole_Exception, InvalidUserPass_Exception, HinemosUnknown_Exception {
//		// Local Variables
//		WebServiceException wse = null;
//		
//		// MAIN
//		for (EndpointSetting endpointSetting : EndpointManager.getAgentEndpoint()) {
//			try {
//				AgentEndpoint endpoint = (AgentEndpoint) endpointSetting.getEndpoint();
//		
//				// Ignore Invalid XML Chars
//				for (WinEventResultDTO result : resultList) {
//					result.setMessage(XMLUtil.ignoreInvalidString(result.getMessage()));
//					result.getMsgInfo().setMessage(XMLUtil.ignoreInvalidString(result.getMsgInfo().getMessage()));
//				}
//		
//				endpoint.forwardWinEventResult(resultList, Agent.getAgentInfo());
//				return;
//			} catch (WebServiceException e) {
//				wse = e;
//				m_log.info("WebServiceException " + e.getMessage());
//				EndpointManager.changeEndpoint();
//			}
//		}
//		throw wse;
//	}

	public static ArrayList<MonitorInfo> getMonitorLogfile()
			throws HinemosUnknown_Exception, InvalidRole_Exception,
			InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting endpointSetting : EndpointManager.getAgentEndpoint()) {
			try {
				AgentEndpoint endpoint = (AgentEndpoint) endpointSetting.getEndpoint();
				return (ArrayList<MonitorInfo>) endpoint.getMonitorLogfile(Agent.getAgentInfo());
			} catch (WebServiceException e) {
				wse = e;
				m_log.info("WebServiceException " + e.getMessage());
				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
	}

	public static HashMapInfo getMonitorJobLogfile()
			throws HinemosUnknown_Exception, InvalidRole_Exception,
			InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting endpointSetting : EndpointManager.getAgentEndpoint()) {
			try {
				AgentEndpoint endpoint = (AgentEndpoint) endpointSetting.getEndpoint();
				return endpoint.getMonitorJobMap(HinemosModuleConstant.MONITOR_LOGFILE, Agent.getAgentInfo());
			} catch (WebServiceException e) {
				wse = e;
				m_log.info("WebServiceException " + e.getMessage());
				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
	}

	public static HashMapInfo getMonitorJobWinEvent()
			throws HinemosUnknown_Exception, InvalidRole_Exception,
			InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting endpointSetting : EndpointManager.getAgentEndpoint()) {
			try {
				AgentEndpoint endpoint = (AgentEndpoint) endpointSetting.getEndpoint();
				return endpoint.getMonitorJobMap(HinemosModuleConstant.MONITOR_WINEVENT, Agent.getAgentInfo());
			} catch (WebServiceException e) {
				wse = e;
				m_log.info("WebServiceException " + e.getMessage());
				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
	}

	public static ArrayList<JobFileCheck> getFileCheckForAgent()
			throws HinemosUnknown_Exception, InvalidRole_Exception,
			InvalidUserPass_Exception, MonitorNotFound_Exception, JobMasterNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting endpointSetting : EndpointManager.getAgentEndpoint()) {
			try {
				AgentEndpoint endpoint = (AgentEndpoint) endpointSetting.getEndpoint();
				return (ArrayList<JobFileCheck>) endpoint.getFileCheckForAgent(Agent.getAgentInfo());
			} catch (WebServiceException e) {
				wse = e;
				m_log.info("WebServiceException " + e.getMessage());
				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
	}

	public static String jobFileCheckResult(JobFileCheck jobFileCheck)
			throws HinemosUnknown_Exception, InvalidRole_Exception,
			InvalidUserPass_Exception, MonitorNotFound_Exception, JobMasterNotFound_Exception, FacilityNotFound_Exception, JobInfoNotFound_Exception, JobSessionDuplicate_Exception {
		WebServiceException wse = null;
		for (EndpointSetting endpointSetting : EndpointManager.getAgentEndpoint()) {
			try {
				AgentEndpoint endpoint = (AgentEndpoint) endpointSetting.getEndpoint();
				return endpoint.jobFileCheckResult(jobFileCheck, Agent.getAgentInfo());
			} catch (WebServiceException e) {
				wse = e;
				m_log.info("WebServiceException " + e.getMessage());
				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
	}

	public static ArrayList<MonitorInfo> getMonitorWinEvent()
			throws HinemosUnknown_Exception, InvalidRole_Exception,
			InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting endpointSetting : EndpointManager.getAgentEndpoint()) {
			try {
				AgentEndpoint endpoint = (AgentEndpoint) endpointSetting.getEndpoint();
				return (ArrayList<MonitorInfo>) endpoint.getMonitorWinEvent(Agent.getAgentInfo());
			} catch (WebServiceException e) {
				wse = e;
				m_log.info("WebServiceException " + e.getMessage());
				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
	}

	public static void deleteAgent()
			throws InvalidRole_Exception, InvalidUserPass_Exception,
			HinemosUnknown_Exception {
		WebServiceException wse = null;
		for (EndpointSetting endpointSetting : EndpointManager.getAgentEndpoint()) {
			try {
				AgentEndpoint endpoint = (AgentEndpoint) endpointSetting.getEndpoint();
				endpoint.deleteAgent(Agent.getAgentInfo());
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.info("WebServiceException " + e.getMessage());
				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
	}

	public static DataHandler downloadModule(String filePath)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, IOException {
		WebServiceException wse = null;
		for (EndpointSetting endpointSetting : EndpointManager.getAgentEndpoint()) {
			try {
				AgentEndpoint endpoint = (AgentEndpoint) endpointSetting.getEndpoint();
				return endpoint.downloadModule(filePath);
			} catch (WebServiceException e) {
				wse = e;
				m_log.info("WebServiceException " + e.getMessage());
				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
	}

	public static HashMap<String, String> getAgentLibMap()
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception  {
		WebServiceException wse = null;
		for (EndpointSetting endpointSetting : EndpointManager.getAgentEndpoint()) {
			try {
				AgentEndpoint endpoint = (AgentEndpoint) endpointSetting.getEndpoint();
				List<String> list = endpoint.getAgentLibMap(Agent.getAgentInfo());
				Iterator<String> itr = list.iterator();
				HashMap<String, String> ret = new HashMap<String, String>();
				while (itr.hasNext()) {
					ret.put(itr.next(), itr.next());
				}
				return ret;
			} catch (WebServiceException e) {
				wse = e;
				m_log.info("WebServiceException " + e.getMessage());
				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
	}

	public static void setAgentLibMd5(HashMap<String, String> agentLibMd5)
			throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;

		ArrayList<String> agentLibMd5List = new ArrayList<String> ();
		for (Entry<String, String> entry : agentLibMd5.entrySet()) {
			agentLibMd5List.add(entry.getKey());
			agentLibMd5List.add(entry.getValue());
		}
		for (EndpointSetting endpointSetting : EndpointManager.getAgentEndpoint()) {
			try {
				AgentEndpoint endpoint = (AgentEndpoint) endpointSetting.getEndpoint();
				endpoint.setAgentLibMd5(agentLibMd5List, Agent.getAgentInfo());
				return;
			} catch (WebServiceException e) {
				wse = e;
				m_log.info("WebServiceException " + e.getMessage());
				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
	}
	
	public static List<String> getScript(String sessionId, String jobunitId, String jobId)
			throws HinemosUnknown_Exception, JobInfoNotFound_Exception,
			InvalidRole_Exception, InvalidUserPass_Exception {
		WebServiceException wse = null;
		for (EndpointSetting endpointSetting : EndpointManager.getAgentEndpoint()) {
			try {
				AgentEndpoint endpoint = (AgentEndpoint) endpointSetting.getEndpoint();
				return endpoint.getScript(sessionId, jobunitId, jobId);
			} catch (WebServiceException e) {
				wse = e;
				m_log.info("getScript " + e.getMessage());
				EndpointManager.changeEndpoint();
			}
		}
		throw wse;
	}
}
