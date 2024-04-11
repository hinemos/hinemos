/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.cloud.log.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import org.apache.log4j.Logger;

import com.clustercontrol.agent.log.MonitorInfoWrapper;
import com.clustercontrol.agent.util.MonitorStringUtil;
import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitor;
import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitorManager;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatus;
import com.clustercontrol.agent.util.filemonitor.FileMonitorConfig;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

public class CloudLogfileMonitor extends AbstractFileMonitor<MonitorInfoWrapper> {
	Logger log = Logger.getLogger(this.getClass());
	String logStreamName = "";
	boolean hasNotifyDateNotFound = false;
	String orgMsgForDateNotFound = "";
	
	public CloudLogfileMonitor(AbstractFileMonitorManager<MonitorInfoWrapper> fileMonitorManager,
			MonitorInfoWrapper monitorInfo, AbstractReadingStatus<MonitorInfoWrapper> status,
			FileMonitorConfig fileMonitorConfig) {
		super(fileMonitorManager, monitorInfo, status, fileMonitorConfig);
	}

	public void setLogStreamName(String logStreamName) {
		this.logStreamName = logStreamName;
	}

	public String getLogStreamName() {
		return this.logStreamName;
	}

	public String getFilePathForStream() {
		return getFilePath();
	}

	public void clearCarryover() {
		this.status.setCarryOver("");
	}

	public String getCarryover() {
		return this.status.getCarryover();
	}
	
	/**
	 * 監視設定IDを取得
	 * 監視ジョブの場合は監視ジョブに紐づくIDが返却される
	 * @return
	 */
	public String getMonitorId() {
		return m_wrapper.getId();
	}
	
	/**
	 * ファイル監視で取得した文字列から日時情報を抽出して監視を行います。
	 */
	@Override
	protected void patternMatchAndSendManager(String line) {
		// 日時情報のみを取り出して監視
		String monLine = line;
		try {
			monLine = line.substring(line.indexOf(",") + 1, line.length());
		} catch (Exception e) {
			// ここに到達した場合は何らかのロジカルエラー
			// 最低限ログはロストしないよう、分割前のログを監視
			log.error("patternMatchAndSendManager: Format error. Org line: " + line, e);
		}

		// 日時情報の取得
		// 取得できなかった場合は現在時刻を使用
		long date = HinemosTime.currentTimeMillis();
		try {
			date = Long.parseLong(line.substring(0, line.indexOf(",")));
		} catch (Exception e) {
			log.warn("patternMatchAndSendManager: Date not found for line: " + monLine);

			if (log.isDebugEnabled()) {
				log.debug("patternMatchAndSendManager: Date not found for org line: " + line, e);
			}

			if (!hasNotifyDateNotFound) {
				if (!logStreamName.isEmpty()) {
					orgMsgForDateNotFound = "LogStream: " + logStreamName + "\n" + "Log Line: " + monLine;
				} else {
					orgMsgForDateNotFound = "Log Line: " + monLine;
				}
				hasNotifyDateNotFound = true;
			}
		}

		Date orgDate = new Date(date);

		MonitorStringUtil.patternMatch(m_wrapper.formatLine(monLine, fileMonitorConfig.getFilMessageLength()),
				m_wrapper.getMonitorInfo(), m_wrapper.getRunInstructionInfo(), orgDate, logStreamName);

	}

	public String getOrgMessageForDateNotFound() {
		return this.orgMsgForDateNotFound;
	}

	public void resetDateNotFoundNotify() {
		this.orgMsgForDateNotFound = "";
		hasNotifyDateNotFound = false;
	}

	@Override
	protected void sendMessageByFileOpenFileNotFoundException(FileNotFoundException e) {
		String[] args = { getFilePath() };
		sendMessage(PriorityConstant.TYPE_WARNING, MessageConstant.AGENT.getMessage(),
				MessageConstant.CLOUDLOG_MSG_TMP_FILE_OPEN_FAILED.getMessage(),
				MessageConstant.CLOUDLOG_TMP_FILE.getMessage(args));
	}

	@Override
	protected void sendMessageByFileOpenSecurityException(SecurityException e) {
		String[] args = { getFilePath() };
		sendMessage(PriorityConstant.TYPE_WARNING, MessageConstant.AGENT.getMessage(),
				MessageConstant.CLOUDLOG_MSG_TMP_FILE_OPEN_FAILED.getMessage(),
				MessageConstant.CLOUDLOG_TMP_FILE.getMessage(args) + "\n" + e.getMessage());
	}

	@Override
	protected void sendMessageByFileOpenIOException(IOException e) {
		String[] args = { getFilePath() };
		sendMessage(PriorityConstant.TYPE_WARNING, MessageConstant.AGENT.getMessage(),
				MessageConstant.CLOUDLOG_MSG_TMP_FILE_OPEN_FAILED.getMessage(),
				MessageConstant.CLOUDLOG_TMP_FILE.getMessage(args));
	}

	@Override
	protected void sendMessageByFileReadIOException(IOException e) {
		String[] args = { getFilePath() };
		sendMessage(PriorityConstant.TYPE_WARNING, MessageConstant.AGENT.getMessage(),
				MessageConstant.CLOUDLOG_MSG_TMP_FILE_OPEN_FAILED.getMessage(),
				MessageConstant.CLOUDLOG_TMP_FILE.getMessage(args) + "\n" + e.getMessage());
	}

	@Override
	protected void sendMessageByFileSizeOver(long fileSize) {
		// message.log.agent.1=ログファイル「{0}」
		// message.log.agent.3=ファイルサイズが上限を超えました
		// message.log.agent.5=ファイルサイズ「{0} byte」
		String[] args1 = { getFilePath() };
		String[] args2 = { String.valueOf(fileSize) };
		sendMessage(PriorityConstant.TYPE_WARNING, MessageConstant.AGENT.getMessage(),
				MessageConstant.MESSAGE_CLOUD_LOG_TMP_FILE_SIZE_EXCEEDED_UPPER_BOUND.getMessage(),
				MessageConstant.CLOUDLOG_TMP_FILE.getMessage(args1) + ", "
						+ MessageConstant.MESSAGE_LOG_FILE_SIZE_BYTE.getMessage(args2));
	}
	
}
