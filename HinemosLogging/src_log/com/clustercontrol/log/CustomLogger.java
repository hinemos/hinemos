/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.DefaultLogEventFactory;
import org.apache.logging.log4j.message.Message;

/**
 * AppenderControlを利用してAppenderを呼び出すロガーの基底クラス
 */
public abstract class CustomLogger {
	protected Class<?> clazz;

	public CustomLogger(final Class<?> clazz) {
		this.clazz = clazz;
	}

	/**
	 * ログの出力
	 * 
	 * @param message
	 * @param level
	 * @param t
	 */
	abstract public void log(Message message, Level level, Throwable t);

	abstract protected boolean isRun();

	protected LogEvent createLogEvent(Message message, Level level, Throwable t) {
		DefaultLogEventFactory factory = DefaultLogEventFactory.getInstance();
		LogEvent logEvent = factory.createEvent(clazz.getName(), null, clazz.getName(), level, message, null, t);
		return logEvent;
	}
}
