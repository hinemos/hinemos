/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.log.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.config.AppenderControl;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.clustercontrol.log.CustomLogConfig;
import com.clustercontrol.logging.LoggingConfigurator;
import com.clustercontrol.logging.constant.LoggingConstant;
import com.clustercontrol.logging.constant.PropertyConstant;
import com.clustercontrol.logging.property.LoggingProperty;

/**
 * Hinemosロギングログの設定クラスです<BR>
 */
public class InternalLogConfig extends CustomLogConfig {
	private String directoty;
	private String filename;

	@Override
	protected String getDirectory() throws FileNotFoundException {
		if (directoty == null) {
			LoggingProperty prop = LoggingConfigurator.getProperty();
			// ログ固有のパスを取得
			String dirPath = prop.getProperty(PropertyConstant.INTERNAL_LOG_FILE_PATH);
			if (dirPath == null) {
				// ログ固有のパスが取得できない場合、共通パスを使用
				dirPath = prop.getProperty(PropertyConstant.LOG_FILE_PATH);
			}
			if (!new File(dirPath).exists()) {
				throw new FileNotFoundException("\"InternalLog Path is Not Found. path=" + dirPath + "\"");
			}
			directoty = dirPath;
		}
		return directoty;
	}

	@Override
	protected String getFilename() {
		if (filename == null) {
			filename = LoggingConfigurator.getProperty().getProperty(PropertyConstant.INTERNAL_LOG_FILE_NAME);
		}
		return filename;
	}

	@Override
	protected String getLogPattern() {
		return "%d %-5p [%c] (%t) %m";
	}

	@Override
	protected String getFileGeneration() {
		return LoggingConfigurator.getProperty().getProperty(PropertyConstant.INTERNAL_LOG_FILE_GENERATION);
	}

	@Override
	protected String getFileSize() {
		return LoggingConfigurator.getProperty().getProperty(PropertyConstant.INTERNAL_LOG_FILE_SIZE);
	}

	@Override
	public AppenderControl createAppenderControl(Level level, Appender appender) {
		return new AppenderControl(appender, level, null);
	}

	@Override
	public Appender createCustomAppender() throws FileNotFoundException {
		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		final Configuration config = ctx.getConfiguration();

		Layout<? extends Serializable> layout = PatternLayout.newBuilder()
				.withConfiguration(config)
				.withPattern(getLogPattern() + "%n")
				.build();

		TriggeringPolicy policy = SizeBasedTriggeringPolicy
				.createPolicy(getFileSize());

		RolloverStrategy strategy = DefaultRolloverStrategy.newBuilder()
				.withMax(getFileGeneration())
				.build();

		String filePath = getFilePath();
		Appender appender = InternalLogAppender.newBuilder()
				.setName(LoggingConstant.INTERNAL_APPENDER_NAME)
				.setFileName(filePath)
				.setFilePattern(filePath + ".%i")
				.setLayout(layout)
				.setPolicy(policy)
				.setStrategy(strategy)
				.build();
		appender.start();

		config.addAppender(appender);
		ctx.updateLoggers();

		return appender;
	}

}
