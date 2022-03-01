/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.log.control;

import org.apache.logging.log4j.message.Message;

/**
 * 制御ログのメッセージクラスです<BR>
 */
public class ControlLogMessage implements Message {

	private static final long serialVersionUID = 1L;

	private final ControlCode code;

	private String originalMessage;

	public ControlLogMessage(ControlCode code) {
		this.code = code;
	}

	@Override
	public String getFormat() {
		return null;
	}

	@Override
	public String getFormattedMessage() {
		if (this.originalMessage == null) {
			return code.getString();
		} else {
			return code.getString() + " " + this.originalMessage;
		}
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
