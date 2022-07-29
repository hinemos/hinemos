/*
 * Copyright (c) 2021 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.log;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.clustercontrol.agent.util.MonitorStringUtil;
import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitor;
import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitorManager;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatus;
import com.clustercontrol.agent.util.filemonitor.FileMonitorConfig;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.util.MessageConstant;

public class LogfileMonitor extends AbstractFileMonitor<MonitorInfoWrapper> {

	public LogfileMonitor(AbstractFileMonitorManager<MonitorInfoWrapper> fileMonitorManager,
			MonitorInfoWrapper monitorInfo, AbstractReadingStatus<MonitorInfoWrapper> status,
			FileMonitorConfig fileMonitorConfig) {
		super(fileMonitorManager, monitorInfo, status, fileMonitorConfig);
	}

	@Override
	protected void patternMatchAndSendManager(String line) {
		MonitorStringUtil.patternMatch(m_wrapper.formatLine(line, fileMonitorConfig.getFilMessageLength()),
				m_wrapper.getMonitorInfo(), m_wrapper.getRunInstructionInfo(), getFilePath());
	}

	@Override
	protected void sendMessageByFileOpenFileNotFoundException(FileNotFoundException e) {
		// message.log.agent.1=ログファイル「{0}」
		// message.log.agent.2=ログファイルがありませんでした
		String[] args = { getFilePath() };
		sendMessage(PriorityConstant.TYPE_INFO, MessageConstant.AGENT.getMessage(),
				MessageConstant.MESSAGE_LOG_FILE_NOT_FOUND.getMessage(),
				MessageConstant.MESSAGE_LOG_FILE.getMessage(args));
	}

	@Override
	protected void sendMessageByFileOpenSecurityException(SecurityException e) {
		// message.log.agent.1=ログファイル「{0}」
		// message.log.agent.4=ファイルの読み込みに失敗しました
		String[] args = { getFilePath() };
		sendMessage(PriorityConstant.TYPE_WARNING, MessageConstant.AGENT.getMessage(),
				MessageConstant.MESSAGE_LOG_FAILED_TO_READ_FILE.getMessage(),
				MessageConstant.MESSAGE_LOG_FILE.getMessage(args) + "\n" + e.getMessage());
	}

	@Override
	protected void sendMessageByFileOpenIOException(IOException e) {
		// message.log.agent.1=ログファイル「{0}」
		// message.log.agent.4=ファイルの読み込みに失敗しました
		String[] args = { getFilePath() };
		sendMessage(PriorityConstant.TYPE_INFO, MessageConstant.AGENT.getMessage(),
				MessageConstant.MESSAGE_LOG_FAILED_TO_READ_FILE.getMessage(),
				MessageConstant.MESSAGE_LOG_FILE.getMessage(args));
	}

	@Override
	protected void sendMessageByFileReadIOException(IOException e) {
		String[] args = { getFilePath() };
		// message.log.agent.1=ログファイル「{0}」
		// message.log.agent.4=ファイルの読み込みに失敗しました
		sendMessage(PriorityConstant.TYPE_WARNING, MessageConstant.AGENT.getMessage(),
				MessageConstant.MESSAGE_LOG_FAILED_TO_READ_FILE.getMessage(),
				MessageConstant.MESSAGE_LOG_FILE.getMessage(args) + "\n" + e.getMessage());
	}

	@Override
	protected void sendMessageByFileSizeOver(long fileSize) {
		// message.log.agent.1=ログファイル「{0}」
		// message.log.agent.3=ファイルサイズが上限を超えました
		// message.log.agent.5=ファイルサイズ「{0} byte」
		String[] args1 = { getFilePath() };
		String[] args2 = { String.valueOf(fileSize) };
		sendMessage(PriorityConstant.TYPE_INFO, MessageConstant.AGENT.getMessage(),
				MessageConstant.MESSAGE_LOG_FILE_SIZE_EXCEEDED_UPPER_BOUND.getMessage(),
				MessageConstant.MESSAGE_LOG_FILE.getMessage(args1) + ", "
						+ MessageConstant.MESSAGE_LOG_FILE_SIZE_BYTE.getMessage(args2));
	}
}
