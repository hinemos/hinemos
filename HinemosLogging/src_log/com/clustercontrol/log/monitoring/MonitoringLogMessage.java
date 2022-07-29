/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.log.monitoring;

import org.apache.logging.log4j.message.Message;

/**
 * 監視ログのメッセージクラスです<BR>
 */
public class MonitoringLogMessage implements Message {

	private static final long serialVersionUID = 1L;

	private final MonitoringType type;

	private String originalMessage;

	public MonitoringLogMessage(MonitoringType type) {
		this.type = type;
	}

	@Override
	public String getFormat() {
		return null;
	}

	@Override
	public String getFormattedMessage() {
		return type + " " + this.originalMessage;
	}

	@Override
	public Object[] getParameters() {
		return null;
	}

	@Override
	public Throwable getThrowable() {
		return null;
	}

	public void setOriginalMessage(String originalMessage) {
		this.originalMessage = originalMessage;
	}
}
