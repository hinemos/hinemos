/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.agent.rpa.scenariojob;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.clustercontrol.agent.log.MonitorInfoWrapper;
import com.clustercontrol.agent.util.MonitorStringUtil;
import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitor;
import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitorManager;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatus;
import com.clustercontrol.agent.util.filemonitor.FileMonitorConfig;
import com.clustercontrol.util.MessageConstant;

public class ScenarioLogfileMonitor extends AbstractFileMonitor<MonitorInfoWrapper> {

	/** FileNotFoundによるメッセージリスト (RPAシナリオジョブ向け) <ファイルパス,メッセージ> */
	private Map<String, String> messageListForFileNotFound = new ConcurrentHashMap<String, String>();

	/** FileOpenSecurityによるメッセージリスト (RPAシナリオジョブ向け) <ファイルパス,メッセージ> */
	private Map<String, String> messageListForFileOpenSecurity = new ConcurrentHashMap<String, String>();

	/** FileOpenIOによるメッセージリスト (RPAシナリオジョブ向け) <ファイルパス,メッセージ> */
	private Map<String, String> messageListForFileOpenIO = new ConcurrentHashMap<String, String>();

	/** FileReadIOによるメッセージリスト (RPAシナリオジョブ向け) <ファイルパス,メッセージ> */
	private Map<String, String> messageListForFileReadIO = new ConcurrentHashMap<String, String>();

	/** ファイル最大サイズ超過によるメッセージリスト (RPAシナリオジョブ向け) <ファイルパス,メッセージ> */
	private Map<String, String> messageListForFileSize = new ConcurrentHashMap<String, String>();

	public ScenarioLogfileMonitor(AbstractFileMonitorManager<MonitorInfoWrapper> fileMonitorManager,
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
		String[] args = { getFilePath() };
		String message = MessageConstant.MESSAGE_LOG_FILE_NOT_FOUND.getMessage() + ","
				+ MessageConstant.MESSAGE_LOG_FILE.getMessage(args);
		messageListForFileNotFound.put(getFilePath(), message);
	}

	@Override
	protected void sendMessageByFileOpenSecurityException(SecurityException e) {
		String[] args = { getFilePath() };
		String message = MessageConstant.MESSAGE_LOG_FAILED_TO_READ_FILE.getMessage() + ","
				+ MessageConstant.MESSAGE_LOG_FILE.getMessage(args) + "\n" + e.getMessage();
		messageListForFileOpenSecurity.put(getFilePath(), message);
	}

	@Override
	protected void sendMessageByFileOpenIOException(IOException e) {
		String[] args = { getFilePath() };
		String message = MessageConstant.MESSAGE_LOG_FAILED_TO_READ_FILE.getMessage() + ","
				+ MessageConstant.MESSAGE_LOG_FILE.getMessage(args) + "\n" + e.getMessage();
		messageListForFileOpenIO.put(getFilePath(), message);
	}

	@Override
	protected void sendMessageByFileReadIOException(IOException e) {
		String[] args = { getFilePath() };
		String message = MessageConstant.MESSAGE_LOG_FAILED_TO_READ_FILE.getMessage() + ","
				+ MessageConstant.MESSAGE_LOG_FILE.getMessage(args) + "\n" + e.getMessage();
		messageListForFileReadIO.put(getFilePath(), message);
	}

	@Override
	protected void sendMessageByFileSizeOver(long fileSize) {
		String[] args1 = { getFilePath() };
		String[] args2 = { String.valueOf(fileSize) };
		String message = MessageConstant.MESSAGE_LOG_FILE_SIZE_EXCEEDED_UPPER_BOUND.getMessage() + ","
				+ MessageConstant.MESSAGE_LOG_FILE.getMessage(args1) + ", "
				+ MessageConstant.MESSAGE_LOG_FILE_SIZE_BYTE.getMessage(args2);
		messageListForFileSize.put(getFilePath(), message);
	}

	public StringBuilder getErrorMessageBuilder() {
		StringBuilder ret = new StringBuilder();
		seMessageBuilder(messageListForFileNotFound, ret);
		seMessageBuilder(messageListForFileOpenSecurity, ret);
		seMessageBuilder(messageListForFileOpenIO, ret);
		seMessageBuilder(messageListForFileReadIO, ret);
		seMessageBuilder(messageListForFileSize, ret);
		return ret;
	}

	public String getMonitorFilePath() {
		return getFilePath();
	}

	private void seMessageBuilder(Map<String, String> orgMap, StringBuilder targetBuilder) {
		for (Map.Entry<String, String> rec : orgMap.entrySet()) {
			if (targetBuilder.length() > 0) {
				targetBuilder.append("\n");
			}
			targetBuilder.append(rec.getValue());
		}
	}
}
