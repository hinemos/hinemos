/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.log.monitoring;

import java.io.File;
import java.io.Serializable;

import org.apache.logging.log4j.Level;
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
import com.clustercontrol.logging.constant.LoggingConstant;
import com.clustercontrol.logging.constant.PropertyConstant;
import com.clustercontrol.logging.property.LoggingProperty;

/**
 * 監視ログの設定クラスです<BR>
 */
public class MonitoringLogConfig implements LoggingConfig {
	private static final String PATTERN = "%d{yyyy-MM-dd'T'HH:mm:ss,SSSXXX} %m %n";
	private static final MonitoringLogConfig INSTANCE;
	private static InternalLogManager.Logger internalLog = InternalLogManager.getLogger(MonitoringLogConfig.class);

	// 現在出力している監視ログ（絶対パス）
	private static String currentMonitorLogPath;
	private static Appender monitorLogAppender;
	private static AppenderControl monitorLogAppenderControl;

	static {
		try {
			INSTANCE = new MonitoringLogConfig();
		} catch (Exception e) {
			internalLog.error(e.getMessage(), e);
			throw new ExceptionInInitializerError(e);
		}
	}

	public static MonitoringLogConfig getInstance() {
		return INSTANCE;
	}

	public String getCurrentLogPath() {
		return currentMonitorLogPath;
	}

	public AppenderControl getLogAppenderControl() {
		return monitorLogAppenderControl;
	}

	private MonitoringLogConfig() throws Exception {
		LoggingProperty prop = LoggingProperty.getInstance();

		// このプロセスが出力しているログのファイル名を設定
		String dirPath = prop.getProperty(PropertyConstant.MON_LOG_FILE_PATH);
		if (dirPath == null) {
			// ログ固有のパスが取得できない場合、共通パスを使用
			dirPath = prop.getProperty(PropertyConstant.LOG_FILE_PATH);
		}
		if (!new File(dirPath).exists()) {
			throw new Exception("\"MonitoringLog Path is Not Found. path=" + dirPath + "\"");
		}
		currentMonitorLogPath = String.format("%s%s", dirPath, prop.getProperty(PropertyConstant.MON_LOG_FILE_NAME));
		config();
		internalLog.debug("MonitoringLogConfig : MonitorLogConfig complete.");
	}

	private void config() {
		LoggingProperty prop = LoggingProperty.getInstance();

		final Level level = Level.valueOf(prop.getProperty(PropertyConstant.LOG_APP_LEVEL));
		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		final Configuration config = ctx.getConfiguration();
		Layout<? extends Serializable> layout = PatternLayout.newBuilder().withConfiguration(config)
				.withPattern(PATTERN).build();

		TriggeringPolicy policies = CompositeTriggeringPolicy.createPolicy(OnStartupTriggeringPolicy.createPolicy(0),
				SizeBasedTriggeringPolicy.createPolicy(prop.getProperty(PropertyConstant.MON_LOG_FILE_SIZE)));

		RolloverStrategy strategy = DefaultRolloverStrategy.newBuilder()
				.withMax(prop.getProperty(PropertyConstant.MON_LOG_FILE_GENERATION)).build();

		monitorLogAppender = MonitoringLogAppender.newBuilder().setName(LoggingConstant.MONITOR_APPENDER_NAME)
				.setFileName(currentMonitorLogPath).setFilePattern(currentMonitorLogPath + ".%i.%d{yyyyMMdd}")
				.setAppend(false).setLayout(layout).setPolicy(policies).setStrategy(strategy).build();

		monitorLogAppender.start();

		config.addAppender(monitorLogAppender);
		// rootLoggerにMonitoringLogAppenderを追加
		config.getRootLogger().addAppender(monitorLogAppender, level, null);
		ctx.updateLoggers();

		monitorLogAppenderControl = new AppenderControl(monitorLogAppender, level, null);

	}
}
