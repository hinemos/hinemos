/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.log.monitoring;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AppenderControl;
import org.apache.logging.log4j.core.config.Configuration;

import com.clustercontrol.log.internal.InternalLogManager;
import com.clustercontrol.logging.constant.LoggingConstant;

/**
 * 監視ログのマネージャクラスです<BR>
 */
public class MonitoringLogManager {
	private static InternalLogManager.Logger internalLog = InternalLogManager.getLogger(MonitoringLogManager.class);

	private static AppenderControl monitorLogAppenderControl;

	private static boolean status = false;

	public static boolean isRun() {
		return status;
	}

	public static void init() {
		monitorLogAppenderControl = MonitoringLogConfig.getInstance().getLogAppenderControl();
		status = true;
		internalLog.info("init : MonitorLogManager run");
	}

	public static void stop() {
		monitorLogAppenderControl.stop();

		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		final Configuration config = ctx.getConfiguration();
		config.getRootLogger().removeAppender(LoggingConstant.MONITOR_APPENDER_NAME);
		ctx.updateLoggers();
		status = false;

		internalLog.info("stop : MonitorLogManager stop");
	}

	public static Logger getLogger(final Class<?> clazz) {
		return new Logger(clazz);
	}

	public static class Logger {
		private Class<?> clazz;

		public Logger(final Class<?> clazz) {
			this.clazz = clazz;
		}

		public void logDlk(String message) {
			MonitoringLogMessage monitorLogMessage = new MonitoringLogMessage(MonitoringType.INT_DLK);
			monitorLogMessage.setOriginalMessage(message);
			log(monitorLogMessage, null);
		}

		public void logHpr(String message) {
			MonitoringLogMessage monitorLogMessage = new MonitoringLogMessage(MonitoringType.INT_HPR);
			monitorLogMessage.setOriginalMessage(message);
			log(monitorLogMessage, null);
		}

		public void logGcc(String message) {
			MonitoringLogMessage monitorLogMessage = new MonitoringLogMessage(MonitoringType.INT_GCC);
			monitorLogMessage.setOriginalMessage(message);
			log(monitorLogMessage, null);
		}

		public void logCpu(String message) {
			MonitoringLogMessage monitorLogMessage = new MonitoringLogMessage(MonitoringType.INT_CPU);
			monitorLogMessage.setOriginalMessage(message);
			log(monitorLogMessage, null);
		}

		public void log(MonitoringLogMessage message, Level level) {
			if (status) {
				monitorLogAppenderControl.callAppender(MonitoringLogAppender.createLogEvent(message, clazz, level));
			} else {
				internalLog.error("log : MonitorLogManager is not working");
			}
		}

	}
}
