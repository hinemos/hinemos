/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.log.control;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.impl.DefaultLogEventFactory;
import org.apache.logging.log4j.message.Message;

import com.clustercontrol.logging.LoggingConfigurator;
import com.clustercontrol.logging.constant.LoggingConstant;

/**
 * 制御ログのAppenderを構成するクラスです<BR>
 */
@Plugin(name = LoggingConstant.CONTROL_APPENDER_NAME, category = "Core", elementType = "appender", printObject = true)
public final class ControlLogAppender extends AbstractOutputStreamAppender<RollingFileManager> {

	private static final int DEFAULT_BUFFER_SIZE = 8192;

	private ControlLogAppender(final String name, final Layout<? extends Serializable> layout, final Filter filter,
			final RollingFileManager manager, final boolean ignoreExceptions, final boolean immediateFlush) {
		super(name, layout, filter, ignoreExceptions, immediateFlush, null, manager);
	}

	@PluginBuilderFactory
	public static Builder newBuilder() {
		return new Builder();
	}

	public static class Builder implements org.apache.logging.log4j.core.util.Builder<ControlLogAppender> {

		@PluginBuilderAttribute
		private boolean append;

		@PluginBuilderAttribute
		private boolean bufferedIO = true;

		@PluginBuilderAttribute
		private Integer bufferSize = DEFAULT_BUFFER_SIZE;

		@PluginBuilderAttribute
		private boolean createOnDemand = true;

		@PluginElement("Filter")
		private Filter filter;

		@PluginBuilderAttribute
		private String fileName;

		@PluginBuilderAttribute
		private String filePattern;

		@PluginBuilderAttribute
		private boolean immediateFlush = true;

		@PluginElement("Layout")
		private Layout<? extends Serializable> layout;

		@PluginBuilderAttribute
		private String name;

		@PluginElement("Policy")
		private TriggeringPolicy policy;

		@PluginElement("Strategy")
		private RolloverStrategy strategy;

		@PluginBuilderAttribute
		private boolean ignoreExceptions = true;

		@PluginBuilderAttribute
		private String filePermissions;

		@PluginBuilderAttribute
		private String fileOwner;

		@PluginBuilderAttribute
		private String fileGroup;

		@PluginBuilderAttribute
		private String advertiseURI;

		@PluginConfiguration
		private Configuration config;

		public Builder setAppend(boolean append) {
			this.append = append;
			return this;
		}

		public Builder setBufferedIO(boolean bufferedIO) {
			this.bufferedIO = bufferedIO;
			return this;
		}

		public Builder setBufferSize(Integer bufferSize) {
			this.bufferSize = bufferSize;
			return this;
		}

		public Builder setCreateOnDemand(boolean createOnDemand) {
			this.createOnDemand = createOnDemand;
			return this;
		}

		public Builder setFilter(Filter filter) {
			this.filter = filter;
			return this;
		}

		public Builder setFileName(String fileName) {
			this.fileName = fileName;
			return this;
		}

		public Builder setFilePattern(String filePattern) {
			this.filePattern = filePattern;
			return this;
		}

		public Builder setImmediateFlush(boolean immediateFlush) {
			this.immediateFlush = immediateFlush;
			return this;
		}

		public Builder setLayout(Layout<? extends Serializable> layout) {
			this.layout = layout;
			return this;
		}

		public Builder setName(String name) {
			this.name = name;
			return this;
		}

		public Builder setPolicy(TriggeringPolicy policy) {
			this.policy = policy;
			return this;
		}

		public Builder setStrategy(RolloverStrategy strategy) {
			this.strategy = strategy;
			return this;
		}

		public Builder setIgnoreExceptions(boolean ignoreExceptions) {
			this.ignoreExceptions = ignoreExceptions;
			return this;
		}

		public Builder setFilePermissions(String filePermissions) {
			this.filePermissions = filePermissions;
			return this;
		}

		public Builder setFileOwner(String fileOwner) {
			this.fileOwner = fileOwner;
			return this;
		}

		public Builder setFileGroup(String fileGroup) {
			this.fileGroup = fileGroup;
			return this;
		}

		public Builder setAdvertiseURI(String advertiseURI) {
			this.advertiseURI = advertiseURI;
			return this;
		}

		public Builder setConfig(Configuration config) {
			this.config = config;
			return this;
		}

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
	public void append(LogEvent arg0) {
		// ローテーションが必要かどうかチェック
		getManager().checkRollover(arg0);
		super.append(arg0);
	}

	@Override
	public void start() {
		super.start();
	}

	@Override
	public boolean stop(long timeout, TimeUnit timeUnit) {
		// Webアプリケーション以外の場合Appenderのstopで終了処理
		if (!LoggingConfigurator.isServlet()) {
			LoggingConfigurator.stop();
		}

		return super.stop(timeout, timeUnit);
	}

	public static LogEvent createLogEvent(Message message, Class<?> clazz, Level level) {
		DefaultLogEventFactory factory = DefaultLogEventFactory.getInstance();
		LogEvent logEvent = factory.createEvent(clazz.getName(), null, clazz.getName(), level, message, null, null);
		return logEvent;
	}
}