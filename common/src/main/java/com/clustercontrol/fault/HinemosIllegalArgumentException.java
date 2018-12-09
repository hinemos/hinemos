/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * 計算処理で必要な情報が不足している場合に利用するException
 * @version 6.1.0
 */
public class HinemosIllegalArgumentException extends HinemosException {

	private static final long serialVersionUID = 1L;

	/**
	 * HinemosIllegalArgumentExceptionコンストラクタ
	 */
	public HinemosIllegalArgumentException() {
		super();
	}

	/**
	 * HinemosIllegalArgumentExceptionコンストラクタ
	 * @param messages
	 */
	public HinemosIllegalArgumentException(String messages) {
		super(messages);
	}

	/**
	 * HinemosIllegalArgumentExceptionコンストラクタ
	 * @param e
	 */
	public HinemosIllegalArgumentException(Throwable e) {
		super(e);
	}

	/**
	 * HinemosIllegalArgumentExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public HinemosIllegalArgumentException(String messages, Throwable e) {
		super(messages, e);
	}
}
