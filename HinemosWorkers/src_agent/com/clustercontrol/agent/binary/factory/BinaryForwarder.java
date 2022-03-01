/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.binary.factory;

import java.util.ArrayList;
import java.util.List;

import org.openapitools.client.model.AgtBinaryFileDTORequest;
import org.openapitools.client.model.AgtBinaryRecordDTORequest;
import org.openapitools.client.model.AgtBinaryResultDTORequest;
import org.openapitools.client.model.AgtMessageInfoRequest;
import org.openapitools.client.model.AgtMonitorInfoRequest;
import org.openapitools.client.model.AgtRunInstructionInfoRequest;
import org.openapitools.client.model.ForwardBinaryResultRequest;

import com.clustercontrol.agent.Agent;
import com.clustercontrol.agent.AgentBinaryRestClientWrapper;
import com.clustercontrol.agent.binary.BinaryMonitorConfig;
import com.clustercontrol.agent.util.AgentRequestId;
import com.clustercontrol.agent.util.BlockTransporter;
import com.clustercontrol.agent.util.BlockTransporter.TransportProcessor;

public class BinaryForwarder {

	/** 自身のインスタンス(クラス1に対してインスタンス1) */
	private static final BinaryForwarder _instance = new BinaryForwarder();

	/** 自身のインスタンス(クラス1に対してインスタンス1) */
	public static BinaryForwarder getInstance() {
		return _instance;
	}

	private final BlockTransporter<BinaryResult> transporter;

	/**
	 * コンストラクタ.
	 */
	private BinaryForwarder() {
		transporter = new BlockTransporter<>(
				"Binary", //name
				BinaryMonitorConfig.getBinForwaringQueueMaxSize(), // queueSize
				BinaryMonitorConfig.getBinForwaringTransportMaxSize(), // blockSize
				BinaryMonitorConfig.getBinForwaringTransportInterval(), // sizeThreshold
				BinaryMonitorConfig.getBinForwaringTransportIntervalMsec(), // timeThreshold
				BinaryMonitorConfig.getBinForwaringTransportMaxTry(), // maxTries
				new BinaryResultProcessor());
	}

	/**
	 * マネージャへの送信対象として追加.
	 * 
	 * @param binaryFile 監視バイナリファイル情報
	 * @param binaryRecords 監視バイナリレコード
	 * @param msgInfo sys_log情報
	 * @param monitorInfo 監視結果
	 * @param monitorStrValueInfo 監視対象文字列パターン
	 * @param runInstructionInfo ジョブ設定
	 */
	public void add(AgtBinaryFileDTORequest binaryFile, List<AgtBinaryRecordDTORequest> binaryRecords, AgtMessageInfoRequest msgInfo,
			AgtMonitorInfoRequest monitorInfo, AgtRunInstructionInfoRequest runInstructionInfo) {

		transporter.add(new BinaryResult(binaryFile, binaryRecords, msgInfo, monitorInfo, runInstructionInfo));
	}

	private static class BinaryResult {
		public final AgtBinaryFileDTORequest _binaryFile;
		public final List<AgtBinaryRecordDTORequest> _binaryRecords;
		public final AgtMessageInfoRequest _msgInfo;
		public final AgtMonitorInfoRequest _monitorInfo;
		public final AgtRunInstructionInfoRequest _runInstructionInfo;

		public BinaryResult(AgtBinaryFileDTORequest binaryFile, List<AgtBinaryRecordDTORequest> binaryRecords, AgtMessageInfoRequest msgInfo,
				AgtMonitorInfoRequest monitorInfo, AgtRunInstructionInfoRequest runInstructionInfo) {
			this._binaryFile = binaryFile;
			this._binaryRecords = binaryRecords;
			this._msgInfo = msgInfo;
			this._monitorInfo = monitorInfo;
			this._runInstructionInfo = runInstructionInfo;
		}

		@Override
		public String toString() {
			return "BinaryResult: monitorId=" + _monitorInfo.getMonitorId();
		}
	}

	private static class BinaryResultProcessor implements TransportProcessor<BinaryResult> {
		@Override
		public void accept(List<BinaryResult> results, AgentRequestId requestId) throws Exception {
			ForwardBinaryResultRequest request = new ForwardBinaryResultRequest();
			for (BinaryResult result : results) {
				AgtBinaryResultDTORequest dto = new AgtBinaryResultDTORequest();
				dto.setBinaryFile(result._binaryFile);
				dto.setBinaryRecords(new ArrayList<>(result._binaryRecords));
				dto.setMsgInfo(result._msgInfo);
				dto.setMonitorInfo(result._monitorInfo);
				dto.setRunInstructionInfo(result._runInstructionInfo);
				request.addResultListItem(dto);
			}
			request.setAgentInfo(Agent.getAgentInfoRequest());
			AgentBinaryRestClientWrapper.forwardBinaryResult(request, requestId.toRequestHeaderValue());
		}
	}

}
