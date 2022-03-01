/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.rpa;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.AgtCalendarInfoResponse;
import org.openapitools.client.model.AgtMonitorInfoResponse;
import org.openapitools.client.model.AgtRpaLogFileCheckInfoResponse;

import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitorInfoWrapper;

public class RpaMonitorInfoWrapper extends AbstractFileMonitorInfoWrapper {
	private static Log log = LogFactory.getLog(RpaMonitorInfoWrapper.class);

	public final AgtMonitorInfoResponse monitorInfo;
	
	public RpaMonitorInfoWrapper(AgtMonitorInfoResponse monitorInfo) {
		this.monitorInfo = monitorInfo;
	}

	public static List<RpaMonitorInfoWrapper> createMonitorInfoList(
			List<AgtMonitorInfoResponse> monitorList) {
		List<RpaMonitorInfoWrapper> wrapperList = new ArrayList<>();

		String fileName = "";
		for (AgtMonitorInfoResponse info : monitorList) {
			AgtRpaLogFileCheckInfoResponse check = info.getRpaLogFileCheckInfo();
			fileName += "[" + check.getDirectory() + "," + check.getFileName() + "]";

			wrapperList.add(new RpaMonitorInfoWrapper(info));

			// 監視設定の対象ディレクトリ存在チェック
			File directory = new File(check.getDirectory());
			log.debug("createMonitorInfoWrapper: Directory monitorId = " + info.getMonitorId() + ", directoryStr = "
					+ check.getDirectory() + ", exists = " + directory.isDirectory());
		}
		log.info("createMonitorInfoWrapper: Targets = " + fileName);

		return wrapperList;
	}

	@Override
	public String getId() {
		return monitorInfo.getMonitorId();
	}

	@Override
	public String getDirectory() {
		return monitorInfo.getRpaLogFileCheckInfo().getDirectory();
	}

	@Override
	public String getFileName() {
		return monitorInfo.getRpaLogFileCheckInfo().getFileName();
	}

	@Override
	public String getFileEncoding() {
		return monitorInfo.getRpaLogFileCheckInfo().getFileEncoding();
	}

	@Override
	public String getFileReturnCode() {
		// RPAログファイル監視はCRLF固定
		return "CRLF";
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
	@Deprecated
	public Integer getMaxStringLength() {
		// 使用しない
		return null;
	}

	@Override
	@Deprecated
	public String getStartRegexString() {
		// 使用しない
		return null;
	}

	@Override
	@Deprecated
	public String getEndRegexString() {
		// 使用しない
		return null;
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
	public boolean isMonitorJob() {
		// RPAログファイル監視は監視ジョブに対応しない
		return false;
	}

	public AgtMonitorInfoResponse getMonitorInfo() {
		return monitorInfo;
	}
	
	/**
	 * 監視文字列を整形する
	 * 
	 * @param line
	 *            監視文字列
	 * @param logfilMessageLength
	 *            オリジナルメッセージのサイズ上限
	 * @return 整形後監視文字列
	 */
	public String formatLine(String line, int logfilMessageLength) {
		// RPAログファイル監視はCRLF固定
		String separator = getFileReturnCode();

		// ファイル改行コードが残ってしまうので、ここで削除する。
		line = line.replace(separator, "");

		// 長さが上限値を超える場合は切り捨てる
		if (line.length() > logfilMessageLength) {
			if (log.isDebugEnabled()) {
				log.debug("log line is too long");
			}
			line = line.substring(0, logfilMessageLength);
		}
		return line;
	}
}
