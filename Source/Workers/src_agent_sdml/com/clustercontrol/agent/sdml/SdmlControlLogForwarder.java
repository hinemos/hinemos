/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.sdml;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtSdmlControlLogDTORequest;
import org.openapitools.client.model.ForwardSdmlControlLogRequest;

import com.clustercontrol.agent.Agent;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.agent.util.AgentRequestId;
import com.clustercontrol.agent.util.BlockTransporter;
import com.clustercontrol.agent.util.BlockTransporter.TransportProcessor;

public class SdmlControlLogForwarder {

	private static Log log = LogFactory.getLog(SdmlControlLogForwarder.class);

	private static final SdmlControlLogForwarder _instance = new SdmlControlLogForwarder();

	public static SdmlControlLogForwarder getInstance() {
		return _instance;
	}

	private final BlockTransporter<AgtSdmlControlLogDTORequest> transporter;

	private SdmlControlLogForwarder() {
		int _queueMaxSize;
		int _transportMaxSize;
		int _transportIntervalSize;
		long _transportIntervalMSec;
		long _transportIntervalMaxMSec;

		{
			String key = "sdml.log.reader.forwarding.queue.maxsize";
			int valueDefault = 5000;
			String str = AgentProperties.getProperty(key, String.valueOf(valueDefault));
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
			String key = "sdml.log.reader.forwarding.transport.maxsize";
			int valueDefault = 100;
			String str = AgentProperties.getProperty(key, String.valueOf(valueDefault));
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
			String key = "sdml.log.reader.forwarding.transport.interval.size";
			int valueDefault = 15;
			String str = AgentProperties.getProperty(key, String.valueOf(valueDefault));
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
			String key = "sdml.log.reader.forwarding.transport.interval.msec";
			long valueDefault = 1000L;
			String str = AgentProperties.getProperty(key, String.valueOf(valueDefault));
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

		{
			String key = "sdml.log.reader.forwarding.transport.interval.max.msec";
			long valueDefault = 90000L;
			String str = AgentProperties.getProperty(key, String.valueOf(valueDefault));
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
			_transportIntervalMaxMSec = value;
		}

		transporter = new BlockTransporter<>("SdmlCtl", // name
				_queueMaxSize, // queueSize
				_transportMaxSize, // blockSize
				_transportIntervalSize, // sizeThreshold
				_transportIntervalMSec, // timeThreshold
				_transportIntervalMaxMSec, // maxTimeThreshold
				new SdmlControlLogSendProcessor());
	}

	public void add(Long time, String hostname, String applicationId, String pid, String controlCode, String message,
			String orgLogLine) {
		AgtSdmlControlLogDTORequest result = new AgtSdmlControlLogDTORequest();
		result.setTime(time);
		result.setHostname(hostname);
		result.setApplicationId(applicationId);
		result.setPid(pid);
		result.setControlCode(controlCode);
		result.setMessage(message);
		result.setOrgLogLine(orgLogLine);

		transporter.add(result);
	}

	private static class SdmlControlLogSendProcessor implements TransportProcessor<AgtSdmlControlLogDTORequest> {
		@Override
		public void accept(List<AgtSdmlControlLogDTORequest> results, AgentRequestId requestId) throws Exception {
			ForwardSdmlControlLogRequest request = new ForwardSdmlControlLogRequest();
			request.setLogList(results);
			request.setAgentInfo(Agent.getAgentInfoRequest());
			SdmlAgentRestClientWrapper.forwardSdmlControlLog(request, requestId.toRequestHeaderValue());
		}
	}

}
