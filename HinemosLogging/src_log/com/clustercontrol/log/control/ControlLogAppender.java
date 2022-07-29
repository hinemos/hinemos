/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.log.control;

import java.io.Serializable;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;

import com.clustercontrol.log.CustomLogAppender;
import com.clustercontrol.log.internal.InternalLogManager;
import com.clustercontrol.logging.constant.LoggingConstant;

/**
 * 制御ログのAppenderを構成するクラスです<BR>
 */
@Plugin(name = LoggingConstant.CONTROL_APPENDER_NAME, category = "Core", elementType = "appender", printObject = true)
public final class ControlLogAppender extends CustomLogAppender {

	private ControlLogAppender(final String name, final Layout<? extends Serializable> layout, final Filter filter,
			final RollingFileManager manager, final boolean ignoreExceptions, final boolean immediateFlush) {
		super(name, layout, filter, ignoreExceptions, immediateFlush, null, manager);
	}

	@PluginBuilderFactory
	public static Builder newBuilder() {
		return new Builder();
	}

	public static class Builder extends CustomLogAppender.Builder<ControlLogAppender> {

		@Override
		public ControlLogAppender build() {
			final RollingFileManager manager = RollingFileManager.getFileManager(fileName, filePattern, append,
					bufferedIO, policy, strategy, advertiseURI, layout, bufferSize, immediateFlush, createOnDemand,
					filePermissions, fileOwner, fileGroup, config);

			manager.initialize();

			return new ControlLogAppender(name, layout, filter, manager, ignoreExceptions, immediateFlush);
		}
	}

	@Override
	protected void reconfigure() {
		if (!ControlLogManager.isRun()) {
			// Managerが停止している場合は再設定しない
			return;
		}
		try {
			// 制御ログのAppenderを再設定する
			ControlLogManager.configure();
		} catch (Throwable t) {
			InternalLogManager.getLogger(getClass()).error("reconfigure : failed. " + t.getMessage(), t);
		}
	}

}