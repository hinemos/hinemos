/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.log.control;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.rolling.CompositeTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.OnStartupTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.config.AppenderControl;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.clustercontrol.log.internal.InternalLogManager;
import com.clustercontrol.log.util.LoggingConfig;
import com.clustercontrol.logging.LoggingConfigurator;
import com.clustercontrol.logging.constant.LoggingConstant;
import com.clustercontrol.logging.constant.PropertyConstant;
import com.clustercontrol.logging.exception.LoggingPropertyException;
import com.clustercontrol.logging.property.LoggingProperty;
import com.clustercontrol.logging.util.ProcessInfo;

/**
 * 制御ログの設定クラスです<BR>
 */
public class ControlLogConfig implements LoggingConfig {
	private static final String PREFIX = String.format("%s %s %s ", LoggingConfigurator.getHostname(),
			LoggingProperty.getInstance().getProperty(PropertyConstant.APP_ID), ProcessInfo.getInstance().getPid());
	private static final String PATTERN = "%d{yyyy-MM-dd'T'HH:mm:ss,SSSXXX} " + PREFIX + "%m";
	private static final ControlLogConfig INSTANCE;
	private static ControlLogManager.Logger controlLog = ControlLogManager.getLogger(ControlLogConfig.class);
	private static InternalLogManager.Logger internalLog = InternalLogManager.getLogger(ControlLogConfig.class);

	// 現在出力している制御ログ（絶対パス）
	private static String currentControlLogPath;
	private static Appender controlLogAppender;
	private static AppenderControl controlLogAppenderControl;

	static {
		try {
			INSTANCE = new ControlLogConfig();
		} catch (LoggingPropertyException | TimeoutException e) {
			internalLog.error(e.getMessage(), e);
			// 初期化時のエラーのためマネージャ経由でLog出力できない
			// 直接コントロールを渡して出力する。
			controlLog.initError(e.getMessage(), controlLogAppenderControl);
			throw new ExceptionInInitializerError(e);
		} catch (Exception e) {
			internalLog.error(e.getMessage(), e);
			throw new ExceptionInInitializerError(e);
		}
	}

	public static ControlLogConfig getInstance() {
		return INSTANCE;
	}

	public String getCurrentLogPath() {
		return currentControlLogPath;
	}

	public AppenderControl getLogAppenderControl() {
		return controlLogAppenderControl;
	}

	private ControlLogConfig() throws Exception {
		LoggingProperty prop = LoggingProperty.getInstance();

		// このプロセスが出力しているログのファイル名を設定
		String dirPath = prop.getProperty(PropertyConstant.CONTROL_LOG_FILE_PATH);
		if (dirPath == null) {
			// ログ固有のパスが取得できない場合、共通パスを使用
			dirPath = prop.getProperty(PropertyConstant.LOG_FILE_PATH);
		}
		if (!new File(dirPath).exists()) {
			throw new Exception("\"ControlLog Path is Not Found. path=" + dirPath + "\"");
		}
		currentControlLogPath = String.format("%s%s", dirPath,
				prop.getProperty(PropertyConstant.CONTROL_LOG_FILE_NAME));
		config();
		internalLog.debug("ControlLogConfig : ControlLogConfig complete.");
	}

	/**
	 * 制御ログファイルの詳細を設定します。
	 */
	private void config() throws Exception {
		LoggingProperty prop = LoggingProperty.getInstance();
		// 制御コード[Initialize]～[Start]のヘッダー
		ControlLogHeader controlLogHeader = new ControlLogHeader();

		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		final Configuration config = ctx.getConfiguration();

		// 制御ログにErrorを出力するため、Exceptionが発生し処理がヘッダー項目生成が途中でもアぺンダーを起動
		try {
			internalLog.info("config : initlialize_set run");
			controlLogHeader.setProperty(prop);
			internalLog.info("config : initlialize_set end");
		} catch (LoggingPropertyException e) {
			throw e;
		} finally {
			String strLogHeader = controlLogHeader.toFormatString(PATTERN);
			Layout<? extends Serializable> layout = PatternLayout.newBuilder().withConfiguration(config)
					.withPattern(PATTERN + "%n").withHeader(strLogHeader + "%n").build();

			TriggeringPolicy policies = CompositeTriggeringPolicy.createPolicy(
					OnStartupTriggeringPolicy.createPolicy(0),
					SizeBasedTriggeringPolicy.createPolicy(prop.getProperty(PropertyConstant.CONTROL_LOG_FILE_SIZE)));

			RolloverStrategy strategy = DefaultRolloverStrategy.newBuilder()
					.withMax(prop.getProperty(PropertyConstant.CONTROL_LOG_FILE_GENERATION)).build();

			controlLogAppender = ControlLogAppender.newBuilder().setName(LoggingConstant.CONTROL_APPENDER_NAME)
					.setFileName(currentControlLogPath).setFilePattern(currentControlLogPath + ".%i.%d{yyyyMMdd}")
					.setAppend(false).setLayout(layout).setPolicy(policies).setStrategy(strategy).build();

			controlLogAppender.start();

			config.addAppender(controlLogAppender);
			ctx.updateLoggers();

			controlLogAppenderControl = new AppenderControl(controlLogAppender, null, null);
		}
	}
}
