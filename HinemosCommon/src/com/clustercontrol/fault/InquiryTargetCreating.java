/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.fault;

public class InquiryTargetCreating extends HinemosException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4477541039576179801L;


	private String logFormatId = null;

	/**
	 * InquiryContentNotFoundコンストラクタ
	 */
	public InquiryTargetCreating() {
		super();
	}

	/**
	 * InquiryContentNotFoundコンストラクタ
	 * @param messages
	 */
	public InquiryTargetCreating(String messages) {
		super(messages);
	}

	/**
	 * InquiryContentNotFoundコンストラクタ
	 * @param e
	 */
	public InquiryTargetCreating(Throwable e) {
		super(e);
	}

	/**
	 * InquiryContentNotFoundコンストラクタ
	 * @param messages
	 * @param e
	 */
	public InquiryTargetCreating(String messages, Throwable e) {
		super(messages, e);
	}

	public String getLogFormatId() {
		return logFormatId;
	}

	public void setLogFormatId(String logFormatId) {
		this.logFormatId = logFormatId;
	}

}
