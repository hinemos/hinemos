/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.log.control;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AppenderControl;
import org.apache.logging.log4j.core.config.Configuration;

import com.clustercontrol.log.internal.InternalLogManager;
import com.clustercontrol.logging.constant.LoggingConstant;

/**
 * 制御ログのマネージャクラスです<BR>
 */
public class ControlLogManager {
	private static InternalLogManager.Logger internalLog = InternalLogManager.getLogger(ControlLogManager.class);

	private static AppenderControl controlLogAppenderControl;

	private static boolean status = false;

	public static boolean isRun() {
		return status;
	}

	public static void init() {
		controlLogAppenderControl = ControlLogConfig.getInstance().getLogAppenderControl();
		status = true;
		internalLog.info("init : ControlLogManager run");
	}

	public static void stop() {
		controlLogAppenderControl.stop();

		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		final Configuration config = ctx.getConfiguration();
		config.getRootLogger().removeAppender(LoggingConstant.CONTROL_APPENDER_NAME);
		ctx.updateLoggers();

		status = false;
		internalLog.info("stop : ControlLogManager stop");
	}

	public static Logger getLogger(final Class<?> clazz) {
		return new Logger(clazz);
	}

	public static class Logger {
		private Class<?> clazz;

		public Logger(final Class<?> clazz) {
			this.clazz = clazz;
		}

		public void stop() {
			ControlLogMessage controlLogMessage = new ControlLogMessage(ControlCode.STOP);
			log(controlLogMessage, null);
		}

		public void warn(String message) {
			ControlLogMessage controlLogMessage = new ControlLogMessage(ControlCode.WARNING);
			controlLogMessage.setOriginalMessage(message);
			log(controlLogMessage, null);
		}

		public void error(String message) {
			ControlLogMessage controlLogMessage = new ControlLogMessage(ControlCode.ERROR);
			controlLogMessage.setOriginalMessage(message);
			log(controlLogMessage, null);
		}

		public void initError(String message, AppenderControl appenderControl) {
			ControlLogMessage controlLogMessage = new ControlLogMessage(ControlCode.ERROR);
			controlLogMessage.setOriginalMessage(message);
			appenderControl.callAppender(ControlLogAppender.createLogEvent(controlLogMessage, clazz, null));
		}

		public void info(String message) {
			ControlLogMessage controlLogMessage = new ControlLogMessage(ControlCode.INFORM);
			controlLogMessage.setOriginalMessage(message);
			log(controlLogMessage, null);
		}

		public void log(ControlLogMessage message, Level level) {
			if (status) {
				controlLogAppenderControl.callAppender(ControlLogAppender.createLogEvent(message, clazz, level));
			} else {
				internalLog.error("log : ControlLogManager is not working");
			}
		}

	}

}