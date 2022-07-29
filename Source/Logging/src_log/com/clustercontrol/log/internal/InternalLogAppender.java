/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.log.internal;

import java.io.Serializable;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;

import com.clustercontrol.log.CustomLogAppender;
import com.clustercontrol.logging.constant.LoggingConstant;

/**
 * HinemosLogging内部ログのカスタムAppender<BR>
 */
@Plugin(name = LoggingConstant.INTERNAL_APPENDER_NAME, category = "Core", elementType = "appender", printObject = true)
public final class InternalLogAppender extends CustomLogAppender {

	private InternalLogAppender(final String name, final Layout<? extends Serializable> layout, final Filter filter,
			final RollingFileManager manager, final boolean ignoreExceptions, final boolean immediateFlush) {
		super(name, layout, filter, ignoreExceptions, immediateFlush, null, manager);
	}

	@PluginBuilderFactory
	public static Builder newBuilder() {
		return new Builder();
	}

	public static class Builder extends CustomLogAppender.Builder<InternalLogAppender> {

		@Override
		public InternalLogAppender build() {
			final RollingFileManager manager = RollingFileManager.getFileManager(fileName, filePattern, append,
					bufferedIO, policy, strategy, advertiseURI, layout, bufferSize, immediateFlush, createOnDemand,
					filePermissions, fileOwner, fileGroup, config);

			manager.initialize();

			return new InternalLogAppender(name, layout, filter, manager, ignoreExceptions, immediateFlush);
		}
	}

	@Override
	protected void reconfigure() {
		if (!InternalLogManager.isRun()) {
			// Managerが停止している場合は再設定しない
			return;
		}
		try {
			// 内部ログのAppenderを再設定する
			InternalLogManager.configure();
		} catch (Throwable t) {
			InternalLogManager.getLogger(getClass()).error("reconfigure : failed. " + t.getMessage(), t);
		}
	}

}