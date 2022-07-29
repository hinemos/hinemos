/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.logging;

import java.io.IOException;
import java.io.InputStream;

import com.clustercontrol.log.internal.InternalLogManager;
import com.clustercontrol.logging.exception.LoggingPropertyException;
import com.clustercontrol.logging.monitor.InternalMonitor;
import com.clustercontrol.logging.property.LoggingProperty;
import com.clustercontrol.logging.util.ProcessInfo;

/**
 * Hinemos Loggingで使用するインスタンスの管理クラス<br>
 * ここで管理されるインスタンスは起動時に生成され、停止時に適切に破棄される必要があります<br>
 */
public class LoggingInstanceManager {

	private LoggingProperty loggingProperty;
	private ProcessInfo processInfo;
	private InternalMonitor internalMonitor;

	public LoggingInstanceManager() {
	}

	/**
	 * HinemosLoggingの設定ファイルの初期化
	 * 
	 * @param in
	 * @throws IOException
	 * @throws LoggingPropertyException
	 */
	public void initializeLoggingPropety(InputStream in) throws IOException, LoggingPropertyException {
		loggingProperty = new LoggingProperty();
		loggingProperty.loadProperty(in);
	}

	public LoggingProperty getLoggingProperty() {
		return loggingProperty;
	}

	/**
	 * プロセス情報の初期化
	 */
	public void initializeProcessInfo() {
		processInfo = new ProcessInfo();
	}

	public ProcessInfo getProcessInfo() {
		return processInfo;
	}

	/**
	 * プロセス内部監視の開始
	 */
	public void startInternalMonitor() {
		if (loggingProperty == null) {
			InternalLogManager.getLogger(LoggingInstanceManager.class)
					.error("startInternalMonitor : LoggingProperty is not initialized.");
			return;
		}
		internalMonitor = new InternalMonitor(loggingProperty);
		internalMonitor.start();
	}

	/**
	 * プロセス内部監視の停止
	 */
	public void stopInternalMonitor() {
		if (internalMonitor == null) {
			InternalLogManager.getLogger(LoggingInstanceManager.class).error("stopInternalMonitor : not initialized.");
			return;
		}
		internalMonitor.stop();
	}
}
