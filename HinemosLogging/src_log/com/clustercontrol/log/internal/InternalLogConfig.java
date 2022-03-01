/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.log.internal;

import java.io.File;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.config.AppenderControl;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.clustercontrol.log.util.LoggingConfig;
import com.clustercontrol.logging.constant.LoggingConstant;
import com.clustercontrol.logging.constant.PropertyConstant;
import com.clustercontrol.logging.property.LoggingProperty;

/**
 * Hinemosロギングログの設定クラスです<BR>
 */
public class InternalLogConfig implements LoggingConfig {
	private static final InternalLogConfig INSTANCE;

	// 現在出力しているHinemosロギング内部ログ(絶対パス）
	private static String currentInternalPath;
	// rootlogger,loggerの複数を扱う
	private static Map<String, AppenderControl> internalLogAppenderControl = new ConcurrentHashMap<>();;

	static {
		try {
			INSTANCE = new InternalLogConfig();
		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	public static InternalLogConfig getInstance() {
		return INSTANCE;
	}

	public String getCurrentLogPath() {
		return currentInternalPath;
	}

	public Map<String, AppenderControl> getLogAppenderControl() {
		return internalLogAppenderControl;
	}

	private InternalLogConfig() throws Exception {
		LoggingProperty prop = LoggingProperty.getInstance();

		// このプロセスが出力しているログのファイル名を設定
		String dirPath = prop.getProperty(PropertyConstant.INTERNAL_LOG_FILE_PATH);
		if (dirPath == null) {
			// ログ固有のパスが取得できない場合、共通パスを使用
			dirPath = prop.getProperty(PropertyConstant.LOG_FILE_PATH);
		}
		if (!new File(dirPath).exists()) {
			throw new Exception("\"InternalLog Path is Not Found. path=" + dirPath + "\"");
		}
		currentInternalPath = dirPath + prop.getProperty(PropertyConstant.INTERNAL_LOG_FILE_NAME);
		config();
	}

	// Hinemosロギングの動作ログを設定
	private static void config() {
		LoggingProperty prop = LoggingProperty.getInstance();

		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		final Configuration config = ctx.getConfiguration();

		Layout<? extends Serializable> layout = PatternLayout.newBuilder().withConfiguration(config)
				.withPattern("%d %-5p [%c] (%t)%M %m%n").build();

		TriggeringPolicy policy = SizeBasedTriggeringPolicy
				.createPolicy(prop.getProperty(PropertyConstant.INTERNAL_LOG_FILE_SIZE));

		RolloverStrategy strategy = DefaultRolloverStrategy.newBuilder()
				.withMax(prop.getProperty(PropertyConstant.INTERNAL_LOG_FILE_GENERATION)).build();

		Appender internalLogAppender = RollingFileAppender.newBuilder().setName(LoggingConstant.INTERNAL_APPENDER_NAME)
				.withFileName(currentInternalPath).withFilePattern(currentInternalPath + ".%i.%d{yyyyMMdd}")
				.setLayout(layout).withPolicy(policy).withStrategy(strategy).build();

		internalLogAppender.start();
		config.addAppender(internalLogAppender);

		Level rootLevel = Level.valueOf(prop.getProperty(PropertyConstant.INTERNAL_LOG_ROOT_LOGGER).toUpperCase());
		internalLogAppenderControl.put("ROOT", new AppenderControl(internalLogAppender, rootLevel, null));

		for (String key : PropertyConstant.INTERNAL_LOG_LOGGERS) {
			if (!(prop.getProperty(key) == null)) {
				Level level = Level.valueOf(prop.getProperty(key).toUpperCase());
				String pakName = key.replace("hinemos.logging.log.logger.", "");
				internalLogAppenderControl.put(pakName, new AppenderControl(internalLogAppender, level, null));
			}
		}

	}

}
