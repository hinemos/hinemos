/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.rpa;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.clustercontrol.agent.util.MonitorStringUtil;
import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitor;
import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitorManager;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatus;
import com.clustercontrol.agent.util.filemonitor.FileMonitorConfig;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.util.MessageConstant;

public class RpaLogfileMonitor extends AbstractFileMonitor<RpaMonitorInfoWrapper> {

	public RpaLogfileMonitor(AbstractFileMonitorManager<RpaMonitorInfoWrapper> fileMonitorManager,
			RpaMonitorInfoWrapper monitorInfo, AbstractReadingStatus<RpaMonitorInfoWrapper> status,
			FileMonitorConfig fileMonitorConfig) {
		super(fileMonitorManager, monitorInfo, status, fileMonitorConfig);
	}

	@Override
	protected void patternMatchAndSendManager(String line) {
		MonitorStringUtil.patternMatch(m_wrapper.formatLine(line, fileMonitorConfig.getFilMessageLength()),
				m_wrapper.getMonitorInfo(), null, getFilePath());
	}

	@Override
	protected void sendMessageByFileOpenFileNotFoundException(FileNotFoundException e) {
		// MESSAGE_LOG_FILE=ログファイル「{0}」
		// MESSAGE_LOG_FILE_NOT_FOUND=ログファイルがありませんでした
		String[] args = { getFilePath() };
		sendMessage(PriorityConstant.TYPE_INFO, MessageConstant.AGENT.getMessage(),
				MessageConstant.MESSAGE_LOG_FILE_NOT_FOUND.getMessage(),
				MessageConstant.MESSAGE_LOG_FILE.getMessage(args));
	}

	@Override
	protected void sendMessageByFileOpenSecurityException(SecurityException e) {
		// MESSAGE_LOG_FILE=ログファイル「{0}」
		// MESSAGE_LOG_FAILED_TO_READ_FILE=ファイルの読み込みに失敗しました
		String[] args = { getFilePath() };
		sendMessage(PriorityConstant.TYPE_WARNING, MessageConstant.AGENT.getMessage(),
				MessageConstant.MESSAGE_LOG_FAILED_TO_READ_FILE.getMessage(),
				MessageConstant.MESSAGE_LOG_FILE.getMessage(args) + "\n" + e.getMessage());
	}

	@Override
	protected void sendMessageByFileOpenIOException(IOException e) {
		// MESSAGE_LOG_FILE=ログファイル「{0}」
		// MESSAGE_LOG_FAILED_TO_READ_FILE=ファイルの読み込みに失敗しました
		String[] args = { getFilePath() };
		sendMessage(PriorityConstant.TYPE_INFO, MessageConstant.AGENT.getMessage(),
				MessageConstant.MESSAGE_LOG_FAILED_TO_READ_FILE.getMessage(),
				MessageConstant.MESSAGE_LOG_FILE.getMessage(args));
	}

	@Override
	protected void sendMessageByFileReadIOException(IOException e) {
		String[] args = { getFilePath() };
		// MESSAGE_LOG_FILE=ログファイル「{0}」
		// MESSAGE_LOG_FAILED_TO_READ_FILE=ファイルの読み込みに失敗しました
		sendMessage(PriorityConstant.TYPE_WARNING, MessageConstant.AGENT.getMessage(),
				MessageConstant.MESSAGE_LOG_FAILED_TO_READ_FILE.getMessage(),
				MessageConstant.MESSAGE_LOG_FILE.getMessage(args) + "\n" + e.getMessage());
	}

	@Override
	protected void sendMessageByFileSizeOver(long fileSize) {
		// MESSAGE_LOG_FILE=ログファイル「{0}」
		// MESSAGE_LOG_FILE_SIZE_EXCEEDED_UPPER_BOUND=ファイルサイズが上限を超えました
		// MESSAGE_LOG_FILE_SIZE_BYTE=ファイルサイズ「{0} byte」
		String[] args1 = { getFilePath() };
		String[] args2 = { String.valueOf(fileSize) };
		sendMessage(PriorityConstant.TYPE_INFO, MessageConstant.AGENT.getMessage(),
				MessageConstant.MESSAGE_LOG_FILE_SIZE_EXCEEDED_UPPER_BOUND.getMessage(),
				MessageConstant.MESSAGE_LOG_FILE.getMessage(args1) + ", "
						+ MessageConstant.MESSAGE_LOG_FILE_SIZE_BYTE.getMessage(args2));
	}
}
