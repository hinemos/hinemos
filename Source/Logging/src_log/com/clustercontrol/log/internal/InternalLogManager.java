/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.log.internal;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AppenderControl;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;

import com.clustercontrol.log.CustomLogger;
import com.clustercontrol.logging.LoggingConfigurator;
import com.clustercontrol.logging.constant.LoggingConstant;
import com.clustercontrol.logging.constant.PropertyConstant;
import com.clustercontrol.logging.exception.LoggingPropertyException;
import com.clustercontrol.logging.property.LoggingProperty;

/**
 * Hinemosロギングログのマネージャクラスです<BR>
 */
public class InternalLogManager {
	private static volatile AppenderControl rootAppenderControl;
	private static volatile Map<String, AppenderControl> appenderControlMap;

	private static InternalLogConfig internalLogConfig;

	private static boolean status = false;

	public static boolean isRun() {
		return status;
	}

	public static void init() throws FileNotFoundException {
		internalLogConfig = new InternalLogConfig();

		configure();

		Logger internalLog = getLogger(InternalLogManager.class);
		status = true;
		internalLog.info("init : InternalLogManager run");
	}

	/**
	 * AppenderおよびAppenderControlの設定を行う
	 * 
	 * @throws FileNotFoundException
	 */
	public static void configure() throws FileNotFoundException {
		LoggingProperty prop = LoggingConfigurator.getProperty();

		Level level = Level.valueOf(prop.getProperty(PropertyConstant.INTERNAL_LOG_ROOT_LOGGER));
		Appender appender = internalLogConfig.createCustomAppender();
		rootAppenderControl = internalLogConfig.createAppenderControl(level, appender);

		appenderControlMap = null;
		for (String key : prop.getInternalLogLoggers()) {
			if (!(prop.getProperty(key) == null)) {
				level = Level.valueOf(prop.getProperty(key));
				String pakName = key.replace("hinemos.logging.log.logger.", "");
				if (appenderControlMap == null) {
					appenderControlMap = new ConcurrentHashMap<>();
				}
				appenderControlMap.put(pakName, internalLogConfig.createAppenderControl(level, appender));
			}
		}
		getLogger(InternalLogManager.class).info("configure");
	}

	public static void stop(boolean stopAppender) {
		Logger internalLog = getLogger(InternalLogManager.class);
		internalLog.info("stop : InternalLogManager stop");
		status = false;

		if (rootAppenderControl != null) {
			rootAppenderControl.stop();
		}
		if (appenderControlMap != null) {
			for (Entry<String, AppenderControl> entry : appenderControlMap.entrySet()) {
				entry.getValue().stop();
			}
		}

		if (stopAppender) {
			final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
			final Configuration config = ctx.getConfiguration();
			if (config.getAppender(LoggingConstant.INTERNAL_APPENDER_NAME) != null) {
				config.getAppenders().remove(LoggingConstant.INTERNAL_APPENDER_NAME).stop();
				ctx.updateLoggers();
			}
		}

		rootAppenderControl = null;
		appenderControlMap = null;
		internalLogConfig = null;
	}

	public static Logger getLogger(final Class<?> clazz) {
		return new Logger(clazz);
	}

	private static AppenderControl selectAppenderControl(Class<?> clazz) {
		if (appenderControlMap != null) {
			for (Entry<String, AppenderControl> entry : appenderControlMap.entrySet()) {
				if (clazz.getName().startsWith(entry.getKey())) {
					return entry.getValue();
				}
			}
		}
		return rootAppenderControl;
	}

	public static class Logger extends CustomLogger {

		public Logger(final Class<?> clazz) {
			super(clazz);
		}

		// 各ログを動作ログに出力
		public void trace(String message) {
			log(message, Level.TRACE, null);
		}

		public void debug(String message) {
			log(message, Level.DEBUG, null);
		}

		public void info(String message) {
			log(message, Level.INFO, null);
		}

		public void info(String name, String value, LoggingPropertyException e) {
			String message = String.format("%s : value = %s : ", name, value);
			log(message + e.getClass().getSimpleName() + ", " + e.getMessage(), Level.INFO, null);
		}

		public void warn(String message) {
			log(message, Level.WARN, null);
		}

		public void warn(String message, Throwable t) {
			log(message, Level.WARN, t);
		}

		public void error(String message) {
			log(message, Level.ERROR, null);
		}

		public void error(String message, Throwable t) {
			log(message, Level.ERROR, t);
		}

		public void fatal(String message) {
			log(message, Level.FATAL, null);
		}

		private void log(String message, Level level, Throwable t) {
			log(new SimpleMessage(message), level, t);
		}

		@Override
		protected boolean isRun() {
			return status;
		}

		@Override
		public void log(Message message, Level level, Throwable t) {
			AppenderControl appenderControl = selectAppenderControl(clazz);
			if (isRun() && appenderControl != null) {
				appenderControl.callAppender(createLogEvent(message, level, t));
			} else {
				// ログの出力先がないため何もしない
			}
		}
	}
}
