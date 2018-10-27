/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

public class LogTransferNotFound extends HinemosException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5075836476018252464L;
	private String transferId = null;

	/**
	 * LogTransferNotFoundコンストラクタ
	 */
	public LogTransferNotFound() {
		super();
	}

	/**
	 * LogTransferNotFoundコンストラクタ
	 * @param messages
	 */
	public LogTransferNotFound(String messages) {
		super(messages);
	}

	/**
	 * LogTransferNotFoundコンストラクタ
	 * @param e
	 */
	public LogTransferNotFound(Throwable e) {
		super(e);
	}

	/**
	 * LogTransferNotFoundコンストラクタ
	 * @param messages
	 * @param e
	 */
	public LogTransferNotFound(String messages, Throwable e) {
		super(messages, e);
	}

	public String getLogTransferId() {
		return transferId;
	}

	public void setLogTransferId(String transferId) {
		this.transferId = transferId;
	}
}
