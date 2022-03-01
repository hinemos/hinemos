/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.log.internal;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AppenderControl;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.impl.DefaultLogEventFactory;
import org.apache.logging.log4j.message.SimpleMessage;

import com.clustercontrol.logging.constant.LoggingConstant;
import com.clustercontrol.logging.exception.LoggingPropertyException;

/**
 * Hinemosロギングログのマネージャクラスです<BR>
 */
public class InternalLogManager {
	// rootlogger,loggerの複数を扱う
	private static Map<String, AppenderControl> appenderControlMap = new ConcurrentHashMap<>();

	private static boolean status = false;

	public static boolean isRun() {
		return status;
	}

	public static void init() {
		appenderControlMap = InternalLogConfig.getInstance().getLogAppenderControl();
		Logger internalLog = getLogger(InternalLogManager.class);
		status = true;
		internalLog.info("init : InternalLogManager run");
	}

	public static void stop() {
		Logger internalLog = getLogger(InternalLogManager.class);
		internalLog.info("stop : InternalLogManager stop");
		for (Entry<String, AppenderControl> entry : appenderControlMap.entrySet()) {
			entry.getValue().stop();
		}

		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		final Configuration config = ctx.getConfiguration();
		config.getRootLogger().removeAppender(LoggingConstant.INTERNAL_APPENDER_NAME);
		ctx.updateLoggers();
		status = false;
	}

	public static Logger getLogger(final Class<?> clazz) {
		return new Logger(clazz);
	}

	public String internalMessage(String name, String value, LoggingPropertyException e) {
		String message = String.format("%s, value = %s : ", name, value);
		return message + e.getClass().getSimpleName() + ", " + e.getMessage();
	}

	public static class Logger {
		private Class<?> clazz;
		private AppenderControl appenderControl;

		public Logger(final Class<?> clazz) {
			this.clazz = clazz;
			for (Entry<String, AppenderControl> entry : appenderControlMap.entrySet()) {
				if (this.clazz.getName().startsWith(entry.getKey())) {
					appenderControl = entry.getValue();
				}
			}
			if (appenderControl == null) {
				appenderControl = appenderControlMap.get("ROOT");
			}
		}

		// 各ログを動作ログに出力
		public void debug(String message) {
			log(message, Level.DEBUG);
		}

		public void info(String message) {
			log(message, Level.INFO);
		}

		public void info(String name, String value, LoggingPropertyException e) {
			String message = String.format("%s : value = %s : ", name, value);
			log(message + e.getClass().getSimpleName() + ", " + e.getMessage(), Level.INFO);
		}

		public void warn(String message) {
			log(message, Level.WARN);
		}

		public void warn(String message, Exception e) {
			log(message, Level.WARN, e);
		}

		public void error(String message) {
			log(message, Level.ERROR);
		}

		public void error(String message, Exception e) {
			log(message, Level.ERROR, e);
		}

		public void log(String message, Level level) {
			appenderControl.callAppender(createLogEvent(message, level, null));
		}

		public void log(String message, Level level, Exception e) {
			appenderControl.callAppender(createLogEvent(message, level, e));
		}

		private LogEvent createLogEvent(String message, Level level, Exception e) {
			DefaultLogEventFactory factory = DefaultLogEventFactory.getInstance();
			LogEvent logEvent = factory.createEvent(this.clazz.getName(), null, this.clazz.getName(), level,
					new SimpleMessage(message), null, e);
			return logEvent;
		}

	}
}
