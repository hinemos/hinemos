/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * 計算エラーが発生した場合に利用するException
 * @version 6.1.0
 */
public class HinemosArithmeticException extends HinemosException {

	private static final long serialVersionUID = 1L;

	/**
	 * HinemosArithmeticExceptionコンストラクタ
	 */
	public HinemosArithmeticException() {
		super();
	}

	/**
	 * HinemosArithmeticExceptionコンストラクタ
	 * @param messages
	 */
	public HinemosArithmeticException(String messages) {
		super(messages);
	}

	/**
	 * HinemosArithmeticExceptionコンストラクタ
	 * @param e
	 */
	public HinemosArithmeticException(Throwable e) {
		super(e);
	}

	/**
	 * HinemosArithmeticExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public HinemosArithmeticException(String messages, Throwable e) {
		super(messages, e);
	}
}
