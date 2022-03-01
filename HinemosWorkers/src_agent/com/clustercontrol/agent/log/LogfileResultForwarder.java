/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.log;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtLogfileResultDTORequest;
import org.openapitools.client.model.AgtMessageInfoRequest;
import org.openapitools.client.model.AgtMonitorInfoResponse;
import org.openapitools.client.model.AgtMonitorStringValueInfoResponse;
import org.openapitools.client.model.AgtRunInstructionInfoResponse;
import org.openapitools.client.model.ForwardLogfileResultRequest;

import com.clustercontrol.agent.Agent;
import com.clustercontrol.agent.AgentHubRestClientWrapper;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.agent.util.AgentRequestId;
import com.clustercontrol.agent.util.BlockTransporter;
import com.clustercontrol.agent.util.BlockTransporter.TransportProcessor;
import com.clustercontrol.agent.util.MonitorResult;
import com.clustercontrol.fault.HinemosUnknown;

public class LogfileResultForwarder {

	private static Log log = LogFactory.getLog(LogfileResultForwarder.class);

	private static final LogfileResultForwarder _instance = new LogfileResultForwarder();

	public static LogfileResultForwarder getInstance() {
		return _instance;
	}

	private final BlockTransporter<MonitorResult> transporter;

	private LogfileResultForwarder() {
		int _queueMaxSize;
		int _transportMaxTries;
		int _transportMaxSize;
		int _transportIntervalSize;
		long _transportIntervalMSec;

		{
			String key = "monitor.logfile.forwarding.queue.maxsize";
			int valueDefault = 5000;
			String str = AgentProperties.getProperty(key);
			int value = valueDefault;
			try {
				value = Integer.parseInt(str);
				if (value != -1 && value < 1) {
					throw new NumberFormatException();
				}
			} catch (NumberFormatException e) {
				value = valueDefault;
			} finally {
				log.info(key + " uses value \"" + value + "\". (configuration = \"" + str + "\")");
			}
			_queueMaxSize = value;
		}

		{
			String key = "monitor.logfile.forwarding.transport.maxsize";
			int valueDefault = 100;
			String str = AgentProperties.getProperty(key);
			int value = valueDefault;
			try {
				value = Integer.parseInt(str);
				if (value != -1 && value < 1) {
					throw new NumberFormatException();
				}
			} catch (NumberFormatException e) {
				value = valueDefault;
			} finally {
				log.info(key + " uses value \"" + value + "\". (configuration = \"" + str + "\")");
			}
			_transportMaxSize = value;
		}

		{
			String key = "monitor.logfile.forwarding.transport.maxtries";
			int valueDefault = 900;
			String str = AgentProperties.getProperty(key);
			int value = valueDefault;
			try {
				value = Integer.parseInt(str);
				if (value != -1 && value < 1) {
					throw new NumberFormatException();
				}
			} catch (NumberFormatException e) {
				value = valueDefault;
			} finally {
				log.info(key + " uses value \"" + value + "\". (configuration = \"" + str + "\")");
			}
			_transportMaxTries = value;
		}

		{
			String key = "monitor.logfile.forwarding.transport.interval.size";
			int valueDefault = 15;
			String str = AgentProperties.getProperty(key);
			int value = valueDefault;
			try {
				value = Integer.parseInt(str);
				if (value != -1 && value < 1) {
					throw new NumberFormatException();
				}
			} catch (NumberFormatException e) {
				value = valueDefault;
			} finally {
				log.info(key + " uses value \"" + value + "\". (configuration = \"" + str + "\")");
			}
			_transportIntervalSize = value;
		}

		{
			String key = "monitor.logfile.forwarding.transport.interval.msec";
			long valueDefault = 1000L;
			String str = AgentProperties.getProperty(key);
			long value = valueDefault;
			try {
				value = Long.parseLong(str);
				if (value != -1 && value < 1) {
					throw new NumberFormatException();
				}
			} catch (NumberFormatException e) {
				value = valueDefault;
			} finally {
				log.info(key + " uses value \"" + value + "\". (configuration = \"" + str + "\")");
			}
			_transportIntervalMSec = value;
		}

		transporter = new BlockTransporter<>(
				"Logfile", //name
				_queueMaxSize, // queueSize
				_transportMaxSize, // blockSize
				_transportIntervalSize, // sizeThreshold
				_transportIntervalMSec, // timeThreshold
				_transportMaxTries, // maxTries
				new LogfileResultProcessor());
	}

	public void add(String message, AgtMessageInfoRequest msgInfo, AgtMonitorInfoResponse monitorInfo,
			AgtMonitorStringValueInfoResponse monitorStrValueInfo, AgtRunInstructionInfoResponse runInstructionInfo) {
		MonitorResult result;
		try {
			result = new MonitorResult(message, msgInfo, monitorInfo, monitorStrValueInfo, runInstructionInfo);
		} catch (HinemosUnknown e) {
			log.error("add: Failed to convert result. message=" + message, e);
			return;
		}

		transporter.add(result);
	}

	private static class LogfileResultProcessor implements TransportProcessor<MonitorResult> {
		@Override
		public void accept(List<MonitorResult> results, AgentRequestId requestId) throws Exception {
			ForwardLogfileResultRequest request = new ForwardLogfileResultRequest();
			for (MonitorResult result : results) {
				AgtLogfileResultDTORequest dto = new AgtLogfileResultDTORequest();
				dto.setMessage(result.message);
				dto.setMsgInfo(result.msgInfoReq);
				dto.setMonitorInfo(result.monitorInfoReq);
				dto.setMonitorStrValueInfo(result.monitorStrValueInfoReq);
				dto.setRunInstructionInfo(result.runInstructionInfoReq);
				request.addResultListItem(dto);
			}
			request.setAgentInfo(Agent.getAgentInfoRequest());
			AgentHubRestClientWrapper.forwardLogfileResult(request, requestId.toRequestHeaderValue());
		}
	}

}
