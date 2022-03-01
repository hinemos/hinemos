/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtCalendarInfoResponse;
import org.openapitools.client.model.AgtLogfileCheckInfoResponse;
import org.openapitools.client.model.AgtMonitorInfoResponse;
import org.openapitools.client.model.AgtRunInstructionInfoRequest;
import org.openapitools.client.model.AgtRunInstructionInfoResponse;

import com.clustercontrol.agent.util.RestAgentBeanUtil;
import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitorInfoWrapper;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.logfile.bean.LogfileLineSeparatorConstant;

/**
 * 監視設定と監視結果をまとめるためのクラス
 * 
 */
public class MonitorInfoWrapper extends AbstractFileMonitorInfoWrapper {
	private static Log log = LogFactory.getLog(MonitorInfoWrapper.class);

	public final AgtMonitorInfoResponse monitorInfo;
	public final AgtRunInstructionInfoResponse runInstructionInfo;
	public final AgtRunInstructionInfoRequest runInstructionInfoReq;
	
	public MonitorInfoWrapper(AgtMonitorInfoResponse monitorInfo, AgtRunInstructionInfoResponse runInstructionInfo) {
		this.monitorInfo = monitorInfo;
		this.runInstructionInfo = runInstructionInfo;

		if (runInstructionInfo == null) {
			this.runInstructionInfoReq = null;
		} else {
			this.runInstructionInfoReq = new AgtRunInstructionInfoRequest();
			try {
				RestAgentBeanUtil.convertBean(this.runInstructionInfo, this.runInstructionInfoReq);
			} catch (HinemosUnknown never) {
				throw new RuntimeException(never);
			}
		}
	}

	public static List<MonitorInfoWrapper> createMonitorInfoList(
			List<AgtMonitorInfoResponse> monitorList,
			Map<AgtRunInstructionInfoResponse, AgtMonitorInfoResponse> monitorMap) {
		List<MonitorInfoWrapper> wrapperList = new ArrayList<>();

		String fileName = "";
		for (AgtMonitorInfoResponse info : monitorList) {
			AgtLogfileCheckInfoResponse check = info.getLogfileCheckInfo();
			fileName += "[" + check.getDirectory() + "," + check.getFileName() + "]";

			wrapperList.add(new MonitorInfoWrapper(info, null));

			// 監視設定の対象ディレクトリ存在チェック
			File directory = new File(check.getDirectory());
			log.debug("createMonitorInfoList: Directory monitorId = " + info.getMonitorId() + ", directoryStr = "
					+ check.getDirectory() + ", exists = " + directory.isDirectory());
		}
		log.info("createMonitorInfoList: Targets = " + fileName);

		fileName = "";
		for (Entry<AgtRunInstructionInfoResponse, AgtMonitorInfoResponse> entry : monitorMap.entrySet()) {
			AgtLogfileCheckInfoResponse check = entry.getValue().getLogfileCheckInfo();
			fileName += "[" + check.getDirectory() + "," + check.getFileName() + "]";

			wrapperList.add(new MonitorInfoWrapper(entry.getValue(), entry.getKey()));

			// 監視設定の対象ディレクトリ存在チェック
			File directory = new File(check.getDirectory());
			log.debug("createMonitorInfoList: Directory(Job) monitorId = " + check.getMonitorId() + ", directoryStr = "
					+ check.getDirectory() + ", exists = " + directory.isDirectory());

		}
		log.info("createMonitorInfoList: Targets(Job) = " + fileName);

		return wrapperList;
	}

	@Override
	public String getId() {
		if (runInstructionInfo == null) {
			return monitorInfo.getMonitorId();
		} else {
			return runInstructionInfo.getSessionId() + runInstructionInfo.getJobunitId()
				+ runInstructionInfo.getJobId() + runInstructionInfo.getFacilityId() + monitorInfo.getMonitorId();
		}
	}

	@Override
	public String getDirectory() {
		return monitorInfo.getLogfileCheckInfo().getDirectory();
	}

	@Override
	public String getFileName() {
		return monitorInfo.getLogfileCheckInfo().getFileName();
	}

	@Override
	public String getFileEncoding() {
		return monitorInfo.getLogfileCheckInfo().getFileEncoding();
	}

	@Override
	public String getFileReturnCode() {
		return monitorInfo.getLogfileCheckInfo().getFileReturnCode();
	}

	@Override
	public Long getRegDate() {
		return monitorInfo.getRegDate();
	}

	@Override
	public Long getUpdateDate() {
		return monitorInfo.getUpdateDate();
	}

	@Override
	public Integer getMaxStringLength() {
		return monitorInfo.getLogfileCheckInfo().getMaxBytes();
	}

	@Override
	public String getStartRegexString() {
		return monitorInfo.getLogfileCheckInfo().getPatternHead();
	}

	@Override
	public String getEndRegexString() {
		return monitorInfo.getLogfileCheckInfo().getPatternTail();
	}

	@Override
	public String getCalendarId() {
		return monitorInfo.getCalendarId();
	}

	@Override
	public AgtCalendarInfoResponse getCalendar() {
		return monitorInfo.getCalendar();
	}

	@Override
	public Boolean getMonitorFlg() {
		return monitorInfo.getMonitorFlg();
	}

	@Override
	public Boolean getCollectorFlg() {
		return monitorInfo.getCollectorFlg();
	}

	@Override
	public boolean isMonitorJob(){
		return runInstructionInfo != null;
	}
	
	public AgtMonitorInfoResponse getMonitorInfo() {
		return monitorInfo;
	}

	public AgtRunInstructionInfoResponse getRunInstructionInfo() {
		return runInstructionInfo;
	}

	public AgtRunInstructionInfoRequest getRunInstructionInfoReq() {
		return runInstructionInfoReq;
	}

	/**
	 * 監視文字列を整形する。
	 * 改行コードを取り除き、指定長以降の部分は切り捨てる。
	 * 
	 * @param line
	 *            監視文字列
	 * @param logfilMessageLength
	 *            オリジナルメッセージのサイズ上限。監視設定の最大読み取り文字数が小さい場合はその値となる。
	 * @return 整形後監視文字列
	 */
	public String formatLine(String line, int logfilMessageLength) {
		log.debug("formatLine(line: " + line + ", logfilMessageLength: " + logfilMessageLength + ") start.");

		String separator = "\n";
		switch (getFileReturnCode()) {
		case LogfileLineSeparatorConstant.LF:
			separator = "\n";
			break;
		case LogfileLineSeparatorConstant.CR:
			separator = "\r";
			break;
		case LogfileLineSeparatorConstant.CRLF:
			separator = "\r\n";
			break;
		default:
			log.warn("ReturnCode:" + getFileReturnCode());
		}

		// ファイル改行コードが残ってしまうので、ここで削除する。
		line = line.replace(separator, "");

		// 上限値
		int limit = logfilMessageLength;
		Integer maxBytes = monitorInfo.getLogfileCheckInfo().getMaxBytes();
		if (maxBytes != null && maxBytes < logfilMessageLength) {
			// 監視設定で設定された最大読み取り文字数の方が小さい場合はそれを設定
			limit = maxBytes;
		}
		// 長さが上限値を超える場合は切り捨てる
		if (line.length() > limit) {
			log.info("log line is too long, so truncate after the limit: " + limit);
			line = line.substring(0, limit);
		}

		log.debug("formatLine() end. line: " + line);
		return line;
	}
}