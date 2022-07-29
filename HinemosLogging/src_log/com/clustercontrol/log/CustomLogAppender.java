/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.log;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.util.Constants;

import com.clustercontrol.log.internal.InternalLogManager;
import com.clustercontrol.logging.LoggingConfigurator;

/**
 * HinemosLogging用のカスタムAppender
 * @see org.apache.logging.log4j.core.appender.RollingFileAppender
 */
public abstract class CustomLogAppender extends AbstractOutputStreamAppender<RollingFileManager> {

	protected CustomLogAppender(String name, Layout<? extends Serializable> layout, Filter filter,
			boolean ignoreExceptions, boolean immediateFlush, Property[] properties, RollingFileManager manager) {
		super(name, layout, filter, ignoreExceptions, immediateFlush, properties, manager);
	}

	public abstract static class Builder<T> implements org.apache.logging.log4j.core.util.Builder<T> {

		@PluginBuilderAttribute
		protected boolean append = true;

		@PluginBuilderAttribute
		protected boolean bufferedIO = true;

		@PluginBuilderAttribute
		protected Integer bufferSize = Constants.ENCODER_BYTE_BUFFER_SIZE;

		@PluginBuilderAttribute
		protected boolean createOnDemand;

		@PluginElement("Filter")
		protected Filter filter;

		@PluginBuilderAttribute
		protected String fileName;

		@PluginBuilderAttribute
		protected String filePattern;

		@PluginBuilderAttribute
		protected boolean immediateFlush = true;

		@PluginElement("Layout")
		protected Layout<? extends Serializable> layout;

		@PluginBuilderAttribute
		protected String name;

		@PluginElement("Policy")
		protected TriggeringPolicy policy;

		@PluginElement("Strategy")
		protected RolloverStrategy strategy;

		@PluginBuilderAttribute
		protected boolean ignoreExceptions = true;

		@PluginBuilderAttribute
		protected String filePermissions;

		@PluginBuilderAttribute
		protected String fileOwner;

		@PluginBuilderAttribute
		protected String fileGroup;

		@PluginBuilderAttribute
		protected String advertiseURI;

		@PluginConfiguration
		protected Configuration config;

		public Builder<T> setAppend(boolean append) {
			this.append = append;
			return this;
		}

		public Builder<T> setBufferedIO(boolean bufferedIO) {
			this.bufferedIO = bufferedIO;
			return this;
		}

		public Builder<T> setBufferSize(Integer bufferSize) {
			this.bufferSize = bufferSize;
			return this;
		}

		public Builder<T> setCreateOnDemand(boolean createOnDemand) {
			this.createOnDemand = createOnDemand;
			return this;
		}

		public Builder<T> setFilter(Filter filter) {
			this.filter = filter;
			return this;
		}

		public Builder<T> setFileName(String fileName) {
			this.fileName = fileName;
			return this;
		}

		public Builder<T> setFilePattern(String filePattern) {
			this.filePattern = filePattern;
			return this;
		}

		public Builder<T> setImmediateFlush(boolean immediateFlush) {
			this.immediateFlush = immediateFlush;
			return this;
		}

		public Builder<T> setLayout(Layout<? extends Serializable> layout) {
			this.layout = layout;
			return this;
		}

		public Builder<T> setName(String name) {
			this.name = name;
			return this;
		}

		public Builder<T> setPolicy(TriggeringPolicy policy) {
			this.policy = policy;
			return this;
		}

		public Builder<T> setStrategy(RolloverStrategy strategy) {
			this.strategy = strategy;
			return this;
		}

		public Builder<T> setIgnoreExceptions(boolean ignoreExceptions) {
			this.ignoreExceptions = ignoreExceptions;
			return this;
		}

		public Builder<T> setFilePermissions(String filePermissions) {
			this.filePermissions = filePermissions;
			return this;
		}

		public Builder<T> setFileOwner(String fileOwner) {
			this.fileOwner = fileOwner;
			return this;
		}

		public Builder<T> setFileGroup(String fileGroup) {
			this.fileGroup = fileGroup;
			return this;
		}

		public Builder<T> setAdvertiseURI(String advertiseURI) {
			this.advertiseURI = advertiseURI;
			return this;
		}

		public Builder<T> setConfig(Configuration config) {
			this.config = config;
			return this;
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
		InternalLogManager.getLogger(CustomLogAppender.class).info(toString() + " : stop.");

		if (((LoggerContext) LogManager.getContext(false)).isStopping()) {
			// LoggerContextが停止中の場合、HinemosLoggingの停止処理を呼び出す
			// Appenderの停止順は保証されていないため、全てのAppenderの停止時に呼び出すが、
			// 呼び出し先では最初の呼び出しのみが有効となる。
			LoggingConfigurator.stop(false);
		} else {
			// 停止中ではない場合はConfigurationの再構成とみなし、HinemosLogging独自設定の再設定を行う
			reconfigure();
		}

		return super.stop(timeout, timeUnit);
	}

	/**
	 * HinemosLogging独自設定の再設定
	 */
	protected abstract void reconfigure();
}
