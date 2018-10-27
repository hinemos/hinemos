/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.fault;

public class InquiryTargetNotDownloadable extends HinemosException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4477541039576179801L;


	private String logFormatId = null;

	/**
	 * InquiryContentNotFoundコンストラクタ
	 */
	public InquiryTargetNotDownloadable() {
		super();
	}

	/**
	 * InquiryContentNotFoundコンストラクタ
	 * @param messages
	 */
	public InquiryTargetNotDownloadable(String messages) {
		super(messages);
	}

	/**
	 * InquiryContentNotFoundコンストラクタ
	 * @param e
	 */
	public InquiryTargetNotDownloadable(Throwable e) {
		super(e);
	}

	/**
	 * InquiryContentNotFoundコンストラクタ
	 * @param messages
	 * @param e
	 */
	public InquiryTargetNotDownloadable(String messages, Throwable e) {
		super(messages, e);
	}

	public String getLogFormatId() {
		return logFormatId;
	}

	public void setLogFormatId(String logFormatId) {
		this.logFormatId = logFormatId;
	}

}
