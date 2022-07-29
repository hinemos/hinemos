/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.log.control;

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
import com.clustercontrol.log.internal.InternalLogManager;
import com.clustercontrol.logging.LoggingConfigurator;
import com.clustercontrol.logging.constant.LoggingConstant;
import com.clustercontrol.logging.constant.PropertyConstant;
import com.clustercontrol.logging.exception.LoggingPropertyException;
import com.clustercontrol.logging.property.LoggingProperty;

/**
 * 制御ログの設定クラスです<BR>
 */
public class ControlLogConfig extends CustomLogConfig {
	private InternalLogManager.Logger internalLog = InternalLogManager.getLogger(ControlLogConfig.class);

	private String directoty;
	private String filename;
	private String header;

	private static final String LINE_SEPARATOR = "\n";

	@Override
	protected String getDirectory() throws FileNotFoundException {
		if (directoty == null) {
			LoggingProperty prop = LoggingConfigurator.getProperty();
			// ログ固有のパスを取得
			String dirPath = prop.getProperty(PropertyConstant.CONTROL_LOG_FILE_PATH);
			if (dirPath == null) {
				// ログ固有のパスが取得できない場合、共通パスを使用
				dirPath = prop.getProperty(PropertyConstant.LOG_FILE_PATH);
			}
			if (!new File(dirPath).exists()) {
				throw new FileNotFoundException("\"ControlLog Path is Not Found. path=" + dirPath + "\"");
			}
			directoty = dirPath;
		}
		return directoty;
	}

	@Override
	protected String getFilename() {
		if (filename == null) {
			filename = LoggingConfigurator.getProperty().getProperty(PropertyConstant.CONTROL_LOG_FILE_NAME);
		}
		return filename;
	}

	@Override
	protected String getLogPattern() {
		return "%d{yyyy-MM-dd'T'HH:mm:ss,SSSXXX} "
				+ String.format("%s %s %s ",
						LoggingConfigurator.getHostname(),
						LoggingConfigurator.getProperty().getProperty(PropertyConstant.APP_ID),
						LoggingConfigurator.getProcessInfo().getPid())
				+ "%m";
	}

	@Override
	protected String getFileGeneration() {
		return LoggingConfigurator.getProperty().getProperty(PropertyConstant.CONTROL_LOG_FILE_GENERATION);
	}

	@Override
	protected String getFileSize() {
		return LoggingConfigurator.getProperty().getProperty(PropertyConstant.CONTROL_LOG_FILE_SIZE);
	}

	@Override
	public AppenderControl createAppenderControl(Level level, Appender appender) {
		return new AppenderControl(appender, level, null);
	}

	@Override
	public Appender createCustomAppender() throws FileNotFoundException {
		if (header == null) {
			internalLog.error("createCustomAppender : header is null. Must be generated first.");
			return null;
		}

		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		final Configuration config = ctx.getConfiguration();

		String pattern = getLogPattern();
		Layout<? extends Serializable> layout = PatternLayout.newBuilder()
				.withConfiguration(config)
				.withPattern(pattern + LINE_SEPARATOR)
				.withHeader(header + LINE_SEPARATOR)
				.withCharset(Charset.forName(LoggingConstant.CONTROL_APPENDER_CHARSET))
				.build();

		TriggeringPolicy policies = CompositeTriggeringPolicy.createPolicy(
				LoggingOnStartupTriggeringPolicy.createPolicy(0),
				SizeBasedTriggeringPolicy.createPolicy(getFileSize()));

		RolloverStrategy strategy = DefaultRolloverStrategy.newBuilder()
				.withMax(getFileGeneration())
				.build();

		String filePath = getFilePath();
		Appender appender = ControlLogAppender.newBuilder()
				.setName(LoggingConstant.CONTROL_APPENDER_NAME)
				.setFileName(filePath)
				.setFilePattern(filePath + ".%i")
				.setLayout(layout)
				.setPolicy(policies)
				.setStrategy(strategy)
				.build();
		appender.start();

		config.addAppender(appender);
		ctx.updateLoggers();

		return appender;
	}

	/**
	 * 制御ログのヘッダーの生成<BR>
	 * {@link ControlLogConfig#createCustomAppender}を呼び出す前に実行する必要があります。<br>
	 * 
	 * @throws LoggingPropertyException
	 */
	public void generateFileHeader() throws LoggingPropertyException {
		internalLog.info("generateFileHeader : start");
		ControlLogHeader controlLogHeader = new ControlLogHeader();
		try {
			// HinemosLogging設定のPropertyから必要情報の設定を行う
			controlLogHeader.setProperty(LoggingConfigurator.getProperty(), LoggingConfigurator.getProcessInfo());

			internalLog.info("generateFileHeader : complete");
		} finally {
			// 必要情報の設定に途中で失敗した場合でもヘッダーは生成する
			header = controlLogHeader.toFormatString(getLogPattern(), LINE_SEPARATOR);
		}
	}
}
