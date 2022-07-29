/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.log.monitoring;

import java.io.FileNotFoundException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AppenderControl;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.message.Message;

import com.clustercontrol.log.CustomLogger;
import com.clustercontrol.log.internal.InternalLogManager;
import com.clustercontrol.logging.LoggingConfigurator;
import com.clustercontrol.logging.constant.LoggingConstant;
import com.clustercontrol.logging.constant.PropertyConstant;
import com.clustercontrol.logging.property.LoggingProperty;

/**
 * 監視ログのマネージャクラスです<BR>
 */
public class MonitoringLogManager {
	private static InternalLogManager.Logger internalLog = InternalLogManager.getLogger(MonitoringLogManager.class);

	private static AppenderControl monitorLogAppenderControl;
	private static MonitoringLogConfig monitoringLogConfig;

	private static boolean status = false;

	public static boolean isRun() {
		return status;
	}

	public static void init() throws FileNotFoundException {
		monitoringLogConfig = new MonitoringLogConfig();

		configure();

		status = true;
		internalLog.info("init : MonitorLogManager run");
	}

	/**
	 * AppenderおよびAppenderControlの設定を行う
	 * 
	 * @throws FileNotFoundException
	 */
	public static void configure() throws FileNotFoundException {
		internalLog.info("configure");
		LoggingProperty prop = LoggingConfigurator.getProperty();
		Level level = Level.valueOf(prop.getProperty(PropertyConstant.LOG_APP_LEVEL));
		monitorLogAppenderControl = monitoringLogConfig.createAppenderControl(level,
				monitoringLogConfig.createCustomAppender());
	}

	public static void stop(boolean stopAppender) {
		status = false;
		if (monitorLogAppenderControl != null) {
			monitorLogAppenderControl.stop();
		}

		if (stopAppender) {
			final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
			final Configuration config = ctx.getConfiguration();
			config.getRootLogger().removeAppender(LoggingConstant.MONITOR_APPENDER_NAME);
			if (config.getAppender(LoggingConstant.MONITOR_APPENDER_NAME) != null) {
				config.getAppenders().remove(LoggingConstant.MONITOR_APPENDER_NAME).stop();
			}
			ctx.updateLoggers();
		}

		monitorLogAppenderControl = null;
		monitoringLogConfig = null;
		internalLog.info("stop : MonitorLogManager stop");
	}

	public static Logger getLogger(final Class<?> clazz) {
		return new Logger(clazz);
	}

	public static class Logger extends CustomLogger {

		public Logger(final Class<?> clazz) {
			super(clazz);
		}

		public void logDlk(String message) {
			MonitoringLogMessage monitorLogMessage = new MonitoringLogMessage(MonitoringType.INT_DLK);
			monitorLogMessage.setOriginalMessage(message);
			log(monitorLogMessage);
		}

		public void logHpr(String message) {
			MonitoringLogMessage monitorLogMessage = new MonitoringLogMessage(MonitoringType.INT_HPR);
			monitorLogMessage.setOriginalMessage(message);
			log(monitorLogMessage);
		}

		public void logGcc(String message) {
			MonitoringLogMessage monitorLogMessage = new MonitoringLogMessage(MonitoringType.INT_GCC);
			monitorLogMessage.setOriginalMessage(message);
			log(monitorLogMessage);
		}

		public void logCpu(String message) {
			MonitoringLogMessage monitorLogMessage = new MonitoringLogMessage(MonitoringType.INT_CPU);
			monitorLogMessage.setOriginalMessage(message);
			log(monitorLogMessage);
		}

		private void log(MonitoringLogMessage message) {
			log(message, null, null);
		}

		@Override
		protected boolean isRun() {
			return status;
		}

		@Override
		public void log(Message message, Level level, Throwable t) {
			if (isRun() && monitorLogAppenderControl != null) {
				monitorLogAppenderControl.callAppender(createLogEvent(message, level, t));
			} else {
				internalLog.error("log : MonitorLogManager is not working");
			}
		}
	}
}
