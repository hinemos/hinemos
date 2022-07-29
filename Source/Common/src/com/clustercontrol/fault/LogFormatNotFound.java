/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

public class LogFormatNotFound extends HinemosNotFound {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4477541039576179801L;


	private String logFormatId = null;

	/**
	 * LogformatNotFoundコンストラクタ
	 */
	public LogFormatNotFound() {
		super();
	}

	/**
	 * LogformatNotFoundコンストラクタ
	 * @param messages
	 */
	public LogFormatNotFound(String messages) {
		super(messages);
	}

	/**
	 * LogformatNotFoundコンストラクタ
	 * @param e
	 */
	public LogFormatNotFound(Throwable e) {
		super(e);
	}

	/**
	 * LogformatNotFoundコンストラクタ
	 * @param messages
	 * @param e
	 */
	public LogFormatNotFound(String messages, Throwable e) {
		super(messages, e);
	}

	public String getLogFormatId() {
		return logFormatId;
	}

	public void setLogFormatId(String logFormatId) {
		this.logFormatId = logFormatId;
	}

}
