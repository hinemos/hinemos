/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.custom;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtCustomResultDTORequest;
import org.openapitools.client.model.ForwardCustomResultRequest;

import com.clustercontrol.agent.AgentHubRestClientWrapper;
import com.clustercontrol.agent.util.AgentProperties;
import com.clustercontrol.agent.util.AgentRequestId;
import com.clustercontrol.agent.util.BlockTransporter;
import com.clustercontrol.agent.util.BlockTransporter.TransportProcessor;

/**
 * カスタム監視によるコマンドの実行結果をマネージャへ送信するクラス.
 * 
 */
public class CustomResultForwarder {
	private static Log log = LogFactory.getLog(CustomResultForwarder.class);
	
	private static final CustomResultForwarder _instance = new CustomResultForwarder();

	public static CustomResultForwarder getInstance() {
		return _instance;
	}

	private final BlockTransporter<AgtCustomResultDTORequest> transporter;
	
	private CustomResultForwarder() {
		int _queueMaxSize;
		int _transportMaxSize;
		int _transportMaxTries;
		int _transportIntervalSize;
		long _transportIntervalMSec;

		{
			String key = "monitor.custom.forwarding.queue.maxsize";
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
			String key = "monitor.custom.forwarding.transport.maxsize";
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
			String key = "monitor.custom.forwarding.transport.maxtries";
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
			String key = "monitor.custom.forwarding.transport.interval.size";
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
			String key = "monitor.custom.forwarding.transport.interval.msec";
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
				"Custom", //name
				_queueMaxSize, // queueSize
				_transportMaxSize, // blockSize
				_transportIntervalSize, // sizeThreshold
				_transportIntervalMSec, // timeThreshold
				_transportMaxTries, // maxTries
				new CustomResultProcessor());
	}
	
	public void add(AgtCustomResultDTORequest result) {
		transporter.add(result);
	}

	private static class CustomResultProcessor implements TransportProcessor<AgtCustomResultDTORequest> {
		@Override
		public void accept(List<AgtCustomResultDTORequest> results, AgentRequestId requestId) throws Exception {
			ForwardCustomResultRequest request = new ForwardCustomResultRequest();
			for (AgtCustomResultDTORequest result : results) {
				request.addResultListItem(result);
			}
			AgentHubRestClientWrapper.forwardCustomResult(request, requestId.toRequestHeaderValue());
		}
	}

}
