/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.log.monitoring;

import java.io.Serializable;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.impl.DefaultLogEventFactory;

import com.clustercontrol.log.CustomLogAppender;
import com.clustercontrol.log.internal.InternalLogManager;
import com.clustercontrol.logging.LoggingConfigurator;
import com.clustercontrol.logging.constant.LoggingConstant;
import com.clustercontrol.logging.constant.PropertyConstant;
import com.clustercontrol.logging.property.LoggingProperty;
import com.clustercontrol.logging.util.StringValidUtil;

/**
 * 監視ログのAppenderを構成するクラスです<BR>
 */
@Plugin(name = LoggingConstant.MONITOR_APPENDER_NAME, category = "Core", elementType = "appender", printObject = true)
public class MonitoringLogAppender extends CustomLogAppender {

	// アプリケーションログ監視の有効無効
	private static boolean enableLogApp;

	protected MonitoringLogAppender(final String name, final Layout<? extends Serializable> layout, final Filter filter,
			final RollingFileManager manager, final boolean ignoreExceptions, final boolean immediateFlush) {
		super(name, layout, filter, ignoreExceptions, immediateFlush, null, manager);
		LoggingProperty prop = LoggingConfigurator.getProperty();
		// 監視、収集どちらかが有効な場合、アプリケーションログ監視の出力処理を行う
		if (Boolean.valueOf(prop.getProperty(PropertyConstant.LOG_APP_MONITOR))
				|| Boolean.valueOf(prop.getProperty(PropertyConstant.LOG_APP_COLLECT))) {
			enableLogApp = true;
		}
	}

	@PluginBuilderFactory
	public static Builder newBuilder() {
		return new Builder();
	}

	public static class Builder extends CustomLogAppender.Builder<MonitoringLogAppender> {

		@Override
		public MonitoringLogAppender build() {
			final RollingFileManager manager = RollingFileManager.getFileManager(fileName, filePattern, append,
					bufferedIO, policy, strategy, advertiseURI, layout, bufferSize, immediateFlush, createOnDemand,
					filePermissions, fileOwner, fileGroup, config);

			manager.initialize();

			return new MonitoringLogAppender(name, layout, filter, manager, ignoreExceptions, immediateFlush);
		}
	}

	@Override
	public void append(LogEvent arg0) {
		// ローテーションが必要かどうかチェック
		getManager().checkRollover(arg0);

		if (arg0.getMessage() instanceof MonitoringLogMessage) {
			// HinemosMessageの場合はそのまま
			super.append(arg0);
		} else {
			if (enableLogApp) {
				MonitoringLogMessage monitorLogMessage = new MonitoringLogMessage(MonitoringType.LOG_APP);
				String message = StringValidUtil.ignoreInvalidString(arg0.getMessage().getFormattedMessage());
				monitorLogMessage.setOriginalMessage(arg0.getLevel().toString() + " " + message);
				DefaultLogEventFactory factory = DefaultLogEventFactory.getInstance();
				LogEvent logEvent = factory.createEvent(arg0.getLoggerName(), arg0.getMarker(), arg0.getLoggerFqcn(),
						arg0.getLevel(), monitorLogMessage, null, arg0.getThrown());
				super.append(logEvent);
			}
		}
	}

	@Override
	protected void reconfigure() {
		if (!MonitoringLogManager.isRun()) {
			// Managerが停止している場合は再設定しない
			return;
		}
		try {
			// 監視ログのAppenderを再設定する
			MonitoringLogManager.configure();
		} catch (Throwable t) {
			InternalLogManager.getLogger(getClass()).error("reconfigure : failed. " + t.getMessage(), t);
		}
	}

}
