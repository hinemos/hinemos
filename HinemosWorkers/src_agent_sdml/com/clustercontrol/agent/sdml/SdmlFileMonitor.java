/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.sdml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitor;
import com.clustercontrol.agent.util.filemonitor.AbstractFileMonitorManager;
import com.clustercontrol.agent.util.filemonitor.AbstractReadingStatus;
import com.clustercontrol.agent.util.filemonitor.FileMonitorConfig;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.util.MessageConstant;

public class SdmlFileMonitor extends AbstractFileMonitor<SdmlFileMonitorInfoWrapper> {

	private static Log log = LogFactory.getLog(SdmlFileMonitor.class);

	private static final String SDML_VERSION_FORMAT = "^SDMLControlLog Version:(\\d+\\.\\d+)$";
	private static final List<String> SDML_SUPPORTED_VERSION;

	private static final String TIMESTAMP_FORMAT = "\\d{4}-\\d{2}-\\d{2}T\\d{2}\\:\\d{2}\\:\\d{2},\\d{3}[+-]\\d{2}\\:\\d{2}";
	private static final String TIMESTAMP_SDF_FORMAT = "yyyy-MM-dd'T'HH:mm:ss,SSSXXX";
	private static final String HOST_NAME_FORMAT = "[!-~]{1,255}";
	private static final String APPLICATION_ID_FORMAT = "[0-9A-Za-z-_.@]{1,64}";
	private static final String PROCESS_ID_FORMAT = "[0-9]{1,6}";
	private static final String CONTROL_CODE_FORAMAT = "[A-Za-z_]+";
	private static final String MESSAGE_FORMAT = ".*";
	private static final String SDML_V1_CONTROL_LOG_FORMAT = String.format("^(%s) (%s) (%s) (%s) (%s)( %s){0,1}$",
			TIMESTAMP_FORMAT, HOST_NAME_FORMAT, APPLICATION_ID_FORMAT, PROCESS_ID_FORMAT, CONTROL_CODE_FORAMAT,
			MESSAGE_FORMAT);

	// バージョンチェック済みフラグ
	private boolean versionChecked = false;

	// 正常バージョンフラグ
	private boolean validVersion = false;

	// フォーマットエラーメッセージ送信済みフラグ
	private boolean formatErrorSent = false;

	static {
		List<String> supportedVersion = new ArrayList<>();
		supportedVersion.add("1.0");
		SDML_SUPPORTED_VERSION = Collections.unmodifiableList(supportedVersion);
	}

	public SdmlFileMonitor(AbstractFileMonitorManager<SdmlFileMonitorInfoWrapper> fileMonitorManager,
			SdmlFileMonitorInfoWrapper monitorInfo, AbstractReadingStatus<SdmlFileMonitorInfoWrapper> status,
			FileMonitorConfig fileMonitorConfig) {
		super(fileMonitorManager, monitorInfo, status, fileMonitorConfig);
	}

	@Override
	protected void patternMatchAndSendManager(String line) {
		Pattern sdmlVersionPattern = Pattern.compile(SDML_VERSION_FORMAT);
		Matcher sdmlVersionMatcher = sdmlVersionPattern.matcher(line);
		if (sdmlVersionMatcher.find()) {
			versionChecked = true;
			String version = sdmlVersionMatcher.group(1);
			if (SDML_SUPPORTED_VERSION.contains(version)) {
				validVersion = true;
			} else {
				validVersion = false;
				sendMessage(PriorityConstant.TYPE_WARNING, m_wrapper.getApplication(),
						MessageConstant.SDML_MSG_LOG_READER_UNSUPPORTED_VERSION.getMessage(),
						MessageConstant.SDML_MSG_FILE_PATH.getMessage(getFilePath()) + "\n"
								+ MessageConstant.SDML_MSG_VERSION.getMessage(version));
			}
			return;
		}

		// バージョン未チェックでバージョン情報以外の行が来たらフォーマットエラー
		if (!versionChecked) {
			// 1行ごとにフォーマットエラー通知を送ると大量になるので1度のみ送る
			if (!formatErrorSent) {
				formatErrorSent = true;
				sendMessage(PriorityConstant.TYPE_WARNING, m_wrapper.getApplication(),
						MessageConstant.SDML_MSG_LOG_READER_INVALID_LOG.getMessage(),
						MessageConstant.SDML_MSG_LOG.getMessage(line));
			}
			return;
		}

		// 正常バージョンのみ処理（異常バージョン時はバージョンチェック時にメッセージ送信しているので通知もしない）
		if (validVersion) {
			Pattern controlLogPattern = Pattern.compile(SDML_V1_CONTROL_LOG_FORMAT);
			Matcher controlLogMatcher = controlLogPattern.matcher(line);
			if (controlLogMatcher.find()) {
				try {
					String timestamp = controlLogMatcher.group(1);
					Long time = new SimpleDateFormat(TIMESTAMP_SDF_FORMAT).parse(timestamp).getTime();
					String hostname = controlLogMatcher.group(2);
					String applicationId = controlLogMatcher.group(3);
					String pid = controlLogMatcher.group(4);
					String controlCode = controlLogMatcher.group(5);
					String message = controlLogMatcher.group(6);
					
					if(message != null && message.length() >= 1){
						message = message.substring(1); // 先頭のスペースを取り除く
					}

					if (applicationId == null || !applicationId.equals(m_wrapper.getId())) {
						// アプリケーションIDが異なるログは送信せずに破棄する
						// パターンに誤りがなければnullは通常ありえないが念のため除外
						log.warn("patternMatchAndSendManager(): Application ID in the log is different. Control Setting="
								+ m_wrapper.getId() + ", Log=" + line);
						return;
					}

					SdmlControlLogForwarder.getInstance().add(time, hostname, applicationId, pid, controlCode, message,
							line);
				} catch (ParseException e) {
					// 正規表現でフォーマットチェックしているので通常ここには到達しない
					sendMessage(PriorityConstant.TYPE_WARNING, m_wrapper.getApplication(),
							MessageConstant.SDML_MSG_LOG_READER_INVALID_LOG.getMessage(),
							MessageConstant.SDML_MSG_LOG.getMessage(line));
				}
			} else {
				// 正常バージョンが確認出来ている状態でのフォーマットエラーは1行ごとに何度でも通知する
				sendMessage(PriorityConstant.TYPE_WARNING, m_wrapper.getApplication(),
						MessageConstant.SDML_MSG_LOG_READER_INVALID_LOG.getMessage(),
						MessageConstant.SDML_MSG_LOG.getMessage(line));
			}
		}
	}

	@Override
	protected void sendMessageByFileOpenFileNotFoundException(FileNotFoundException e) {
		sendMessage(PriorityConstant.TYPE_CRITICAL, m_wrapper.getApplication(),
				MessageConstant.SDML_MSG_LOG_READER_FILE_OPEN_FAILED.getMessage(),
				MessageConstant.SDML_MSG_FILE_PATH.getMessage(getFilePath()) + "\n" + e.getMessage());
	}

	@Override
	protected void sendMessageByFileOpenSecurityException(SecurityException e) {
		sendMessage(PriorityConstant.TYPE_CRITICAL, m_wrapper.getApplication(),
				MessageConstant.SDML_MSG_LOG_READER_FILE_OPEN_FAILED.getMessage(),
				MessageConstant.SDML_MSG_FILE_PATH.getMessage(getFilePath()) + "\n" + e.getMessage());
	}

	@Override
	protected void sendMessageByFileOpenIOException(IOException e) {
		sendMessage(PriorityConstant.TYPE_CRITICAL, m_wrapper.getApplication(),
				MessageConstant.SDML_MSG_LOG_READER_FILE_OPEN_FAILED.getMessage(),
				MessageConstant.SDML_MSG_FILE_PATH.getMessage(getFilePath()) + "\n" + e.getMessage());
	}

	@Override
	protected void sendMessageByFileReadIOException(IOException e) {
		sendMessage(PriorityConstant.TYPE_WARNING, m_wrapper.getApplication(),
				MessageConstant.SDML_MSG_LOG_READER_FILE_OPEN_FAILED.getMessage(),
				MessageConstant.SDML_MSG_FILE_PATH.getMessage(getFilePath()));
	}

	@Override
	protected void sendMessageByFileSizeOver(long fileSize) {
		sendMessage(PriorityConstant.TYPE_WARNING, m_wrapper.getApplication(),
				MessageConstant.SDML_MSG_LOG_READER_FILE_SIZE_OVER.getMessage(),
				MessageConstant.SDML_MSG_FILE_PATH.getMessage(getFilePath()) + "\n"
						+ MessageConstant.SDML_MSG_FILE_SIZE.getMessage(String.valueOf(fileSize) + " byte"));
	}
}
