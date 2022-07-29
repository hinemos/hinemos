/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.log.monitoring;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.nio.charset.Charset;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.rolling.CompositeTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.config.AppenderControl;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.clustercontrol.log.CustomLogConfig;
import com.clustercontrol.log.LoggingOnStartupTriggeringPolicy;
import com.clustercontrol.logging.LoggingConfigurator;
import com.clustercontrol.logging.constant.LoggingConstant;
import com.clustercontrol.logging.constant.PropertyConstant;
import com.clustercontrol.logging.property.LoggingProperty;

/**
 * 監視ログの設定クラスです<BR>
 */
public class MonitoringLogConfig  extends CustomLogConfig {

	private String directoty;
	private String filename;

	private static final String LINE_SEPARATOR = "\n";

	@Override
	protected String getDirectory() throws FileNotFoundException {
		if (directoty == null) {
			LoggingProperty prop = LoggingConfigurator.getProperty();
			// ログ固有のパスを取得
			String dirPath = prop.getProperty(PropertyConstant.MON_LOG_FILE_PATH);
			if (dirPath == null) {
				// ログ固有のパスが取得できない場合、共通パスを使用
				dirPath = prop.getProperty(PropertyConstant.LOG_FILE_PATH);
			}
			if (!new File(dirPath).exists()) {
				throw new FileNotFoundException("\"MonitoringLog Path is Not Found. path=" + dirPath + "\"");
			}
			directoty = dirPath;
		}
		return directoty;
	}

	@Override
	protected String getFilename() {
		if (filename == null) {
			filename = LoggingConfigurator.getProperty().getProperty(PropertyConstant.MON_LOG_FILE_NAME);
		}
		return filename;
	}

	@Override
	protected String getLogPattern() {
		return "%d{yyyy-MM-dd'T'HH:mm:ss,SSSXXX} %m";
	}

	@Override
	protected String getFileGeneration() {
		return LoggingConfigurator.getProperty().getProperty(PropertyConstant.MON_LOG_FILE_GENERATION);
	}

	@Override
	protected String getFileSize() {
		return LoggingConfigurator.getProperty().getProperty(PropertyConstant.MON_LOG_FILE_SIZE);
	}

	@Override
	public AppenderControl createAppenderControl(Level level, Appender appender) {
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration config = ctx.getConfiguration();
		// rootLoggerにMonitoringLogAppenderを追加
		config.getRootLogger().addAppender(appender, level, null);
		// rootLoggerに追加してからupdate
		ctx.updateLoggers();

		return new AppenderControl(appender, level, null);
	}

	@Override
	public Appender createCustomAppender() throws FileNotFoundException {
		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		final Configuration config = ctx.getConfiguration();

		Layout<? extends Serializable> layout = PatternLayout.newBuilder()
				.withConfiguration(config)
				.withPattern(getLogPattern() + LINE_SEPARATOR)
				.withCharset(Charset.forName(LoggingConstant.MONITOR_APPENDER_CHARSET))
				.build();

		TriggeringPolicy policies = CompositeTriggeringPolicy.createPolicy(
				LoggingOnStartupTriggeringPolicy.createPolicy(0),
				SizeBasedTriggeringPolicy.createPolicy(getFileSize()));

		RolloverStrategy strategy = DefaultRolloverStrategy.newBuilder()
				.withMax(getFileGeneration())
				.build();

		String filePath = getFilePath();
		Appender appender = MonitoringLogAppender.newBuilder()
				.setName(LoggingConstant.MONITOR_APPENDER_NAME)
				.setFileName(filePath)
				.setFilePattern(filePath + ".%i")
				.setLayout(layout)
				.setPolicy(policies)
				.setStrategy(strategy)
				.build();
		appender.start();

		config.addAppender(appender);

		return appender;
	}

}
