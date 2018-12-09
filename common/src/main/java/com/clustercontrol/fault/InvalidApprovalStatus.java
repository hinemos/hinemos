/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * 承認可能な状態ではない場合に利用するException
 * @version 6.0.0
 */
public class InvalidApprovalStatus extends HinemosException {

	private static final long serialVersionUID = -1L;

	/**
	 * InvalidApprovalStatusのコンストラクタ
	 */
	public InvalidApprovalStatus() {
		super();
	}

	/**
	 * InvalidApprovalStatusのコンストラクタ
	 * @param messages
	 */
	public InvalidApprovalStatus(String messages) {
		super(messages);
	}

	/**
	 * InvalidApprovalStatusのコンストラクタ
	 * @param e
	 */
	public InvalidApprovalStatus(Throwable e) {
		super(e);
	}

	/**
	 * InvalidApprovalStatusのコンストラクタ
	 * @param messages
	 * @param e
	 */
	public InvalidApprovalStatus(String messages, Throwable e) {
		super(messages, e);
	}
}
