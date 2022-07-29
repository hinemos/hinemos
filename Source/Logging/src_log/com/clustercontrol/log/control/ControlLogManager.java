/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.log.control;

import java.io.FileNotFoundException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AppenderControl;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.message.Message;

import com.clustercontrol.log.CustomLogger;
import com.clustercontrol.log.internal.InternalLogManager;
import com.clustercontrol.logging.constant.LoggingConstant;
import com.clustercontrol.logging.exception.LoggingInitializeException;
import com.clustercontrol.logging.exception.LoggingPropertyException;

/**
 * 制御ログのマネージャクラスです<BR>
 */
public class ControlLogManager {
	private static InternalLogManager.Logger internalLog = InternalLogManager.getLogger(ControlLogManager.class);

	private static AppenderControl controlLogAppenderControl;
	private static ControlLogConfig controlLogConfig;

	private static boolean status = false;

	public static boolean isRun() {
		return status;
	}

	public static void init() throws LoggingInitializeException {
		controlLogConfig = new ControlLogConfig();
		try {

			try {
				controlLogConfig.generateFileHeader();
			} catch (LoggingPropertyException e1) {
				internalLog.error(e1.getMessage(), e1);
				// 初期化時のエラーのためAppenderControlは保持せず、Exceptionをthrowする。
				// そのため、直接コントロールを渡して出力する。
				getLogger(ControlLogManager.class).initError(e1.getMessage(),
						new AppenderControl(controlLogConfig.createCustomAppender(), null, null));
				throw new LoggingInitializeException(e1);
			}

			// ヘッダー生成後にAppenderを設定
			configure();

		} catch (FileNotFoundException e) {
			internalLog.error(e.getMessage(), e);
			// 制御ログの設定が完了していないのでErrorは出力しない
			throw new LoggingInitializeException(e);
		}
		status = true;
		internalLog.info("init : ControlLogManager run");
	}

	/**
	 * AppenderおよびAppenderControlの設定を行う
	 * 
	 * @throws FileNotFoundException
	 */
	public static void configure() throws FileNotFoundException {
		internalLog.info("configure");
		controlLogAppenderControl = controlLogConfig.createAppenderControl(null,
				controlLogConfig.createCustomAppender());
	}

	public static void stop(boolean stopAppender) {
		status = false;
		if (controlLogAppenderControl != null) {
			controlLogAppenderControl.stop();
		}

		if (stopAppender) {
			final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
			final Configuration config = ctx.getConfiguration();
			if (config.getAppender(LoggingConstant.CONTROL_APPENDER_NAME) != null) {
				config.getAppenders().remove(LoggingConstant.CONTROL_APPENDER_NAME).stop();
				ctx.updateLoggers();
			}
		}

		controlLogAppenderControl = null;
		controlLogConfig = null;
		internalLog.info("stop : ControlLogManager stop");
	}

	public static Logger getLogger(final Class<?> clazz) {
		return new Logger(clazz);
	}

	public static class Logger extends CustomLogger {

		public Logger(final Class<?> clazz) {
			super(clazz);
		}

		public void stop() {
			ControlLogMessage controlLogMessage = new ControlLogMessage(ControlCode.STOP);
			log(controlLogMessage);
		}

		public void warn(String message) {
			ControlLogMessage controlLogMessage = new ControlLogMessage(ControlCode.WARNING);
			controlLogMessage.setOriginalMessage(message);
			log(controlLogMessage);
		}

		public void error(String message) {
			ControlLogMessage controlLogMessage = new ControlLogMessage(ControlCode.ERROR);
			controlLogMessage.setOriginalMessage(message);
			log(controlLogMessage);
		}

		public void initError(String message, AppenderControl appenderControl) {
			ControlLogMessage controlLogMessage = new ControlLogMessage(ControlCode.ERROR);
			controlLogMessage.setOriginalMessage(message);
			appenderControl.callAppender(createLogEvent(controlLogMessage, null, null));
		}

		public void info(String message) {
			ControlLogMessage controlLogMessage = new ControlLogMessage(ControlCode.INFORM);
			controlLogMessage.setOriginalMessage(message);
			log(controlLogMessage);
		}

		private void log(ControlLogMessage message) {
			log(message, null, null);
		}

		@Override
		protected boolean isRun() {
			return status;
		}

		@Override
		public void log(Message message, Level level, Throwable t) {
			if (isRun() && controlLogAppenderControl != null) {
				controlLogAppenderControl.callAppender(createLogEvent(message, level, t));
			} else {
				internalLog.error("log : ControlLogManager is not working");
			}
		}

	}

}