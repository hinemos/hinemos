/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.util;

import org.openapitools.client.model.AgtMessageInfoRequest;
import org.openapitools.client.model.AgtMonitorInfoRequest;
import org.openapitools.client.model.AgtMonitorInfoResponse;
import org.openapitools.client.model.AgtMonitorStringValueInfoRequest;
import org.openapitools.client.model.AgtMonitorStringValueInfoResponse;
import org.openapitools.client.model.AgtRunInstructionInfoRequest;
import org.openapitools.client.model.AgtRunInstructionInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;

/**
 * ログファイル監視、Windowsイベント監視の監視結果を保持すると同時に、REST API の DTO 変換を行います。
 */
public class MonitorResult {
	public final String message;
	public final String logMessage;

	public final AgtMonitorInfoResponse monitorInfoRsp;
	public final AgtMonitorStringValueInfoResponse monitorStrValueInfoRsp;
	public final AgtRunInstructionInfoResponse runInstructionInfoRsp;

	public final AgtMessageInfoRequest msgInfoReq;
	public final AgtMonitorInfoRequest monitorInfoReq;
	public final AgtMonitorStringValueInfoRequest monitorStrValueInfoReq;
	public final AgtRunInstructionInfoRequest runInstructionInfoReq;

	public MonitorResult(String message, AgtMessageInfoRequest msgInfo, AgtMonitorInfoResponse monitorInfo,
			AgtMonitorStringValueInfoResponse monitorStrValueInfo, AgtRunInstructionInfoResponse runInstructionInfo)
			throws HinemosUnknown {

		this.message = message;
		this.msgInfoReq = msgInfo;
		this.monitorInfoRsp = monitorInfo;
		this.monitorStrValueInfoRsp = monitorStrValueInfo;
		this.runInstructionInfoRsp = runInstructionInfo;

		monitorInfoReq = (monitorInfo == null) ? null : new AgtMonitorInfoRequest();
		RestAgentBeanUtil.convertBean(monitorInfo, monitorInfoReq);

		monitorStrValueInfoReq = (monitorStrValueInfo == null) ? null : new AgtMonitorStringValueInfoRequest();
		RestAgentBeanUtil.convertBean(monitorStrValueInfo, monitorStrValueInfoReq);

		runInstructionInfoReq = (runInstructionInfo == null) ? null : new AgtRunInstructionInfoRequest();
		RestAgentBeanUtil.convertBean(runInstructionInfo, runInstructionInfoReq);
		
		String monitorId = "";
		String monitorTypeId = "";
		String generationDate = "";
		if(monitorInfo != null){
			monitorId = monitorInfo.getMonitorId();
			monitorTypeId = monitorInfo.getMonitorTypeId();
		}
		if(msgInfo != null){
			generationDate = String.valueOf(msgInfo.getGenerationDate());
		}
		
		this.logMessage = String.format("MonitorResult: monitorId=%s, monitorTypeId=%s, generationDate=%s", monitorId,
				monitorTypeId, generationDate);
	}
	
	@Override
	public String toString() {
		return this.logMessage;
	}
}
