/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.log;

import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.rolling.AbstractTriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import com.clustercontrol.logging.LoggingConfigurator;

/**
 * Hinemos Loggingの起動時にローテーションするためのカスタムTriggeringPolicy
 * 
 * @See org.apache.logging.log4j.core.appender.rolling.OnStartupTriggeringPolicy
 */
@Plugin(name = "LoggingOnStartupTriggeringPolicy", category = Core.CATEGORY_NAME, printObject = true)
public class LoggingOnStartupTriggeringPolicy extends AbstractTriggeringPolicy {

	private final long minSize;

	private LoggingOnStartupTriggeringPolicy(final long minSize) {
		this.minSize = minSize;
	}

	/**
	 * Provide the RollingFileManager to the policy.
	 * @param manager The RollingFileManager.
	 */
	@Override
	public void initialize(final RollingFileManager manager) {
		final long startupTime = LoggingConfigurator.getStartupTime();
		LOGGER.debug("LoggingOnStartupTriggeringPolicy filetime {}, startuptime {}, filesize {}", manager.getFileTime(),
				startupTime, manager.getFileSize());
		// 実行環境によってはミリ秒まで取得されない可能性があるので比較の精度を落とす
		if ((manager.getFileTime() / 1000) < (startupTime / 1000) && manager.getFileSize() >= minSize) {
			LOGGER.debug("Initiating rollover at startup");
			if (minSize == 0) {
				manager.setRenameEmptyFiles(true);
			}
			manager.skipFooter(true);
			manager.rollover();
			manager.skipFooter(false);
		}
	}

	/**
	 * Determine if a rollover should be triggered.
	 * @param event A reference to the current event.
	 * @return 
	 */
	@Override
	public boolean isTriggeringEvent(final LogEvent event) {
		return false;
	}

	@Override
	public String toString() {
		return "LoggingOnStartupTriggeringPolicy";
	}

	@PluginFactory
	public static LoggingOnStartupTriggeringPolicy createPolicy(
			@PluginAttribute(value = "minSize", defaultLong = 1) final long minSize) {
		return new LoggingOnStartupTriggeringPolicy(minSize);
	}
}
