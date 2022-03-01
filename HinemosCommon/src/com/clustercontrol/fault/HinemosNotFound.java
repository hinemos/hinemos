/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.fault;

/**
 * このExceptionは直接利用しないこと。
 * 本来はabstractにすべきだが、HinemosExceptionのつくりを踏襲して、abstractは付けない。
 */
public class HinemosNotFound extends HinemosException {

	private static final long serialVersionUID = 444013476105968953L;

	/**
	 * HinemosNotFoundコンストラクタ
	 */
	public HinemosNotFound() {
		super();
	}

	/**
	 * HinemosNotFoundコンストラクタ
	 * @param messages
	 */
	public HinemosNotFound(String messages) {
		super(messages);
	}

	/**
	 * HinemosNotFoundコンストラクタ
	 * @param e
	 */
	public HinemosNotFound(Throwable e) {
		super(e);
	}

	/**
	 * HinemosNotFoundコンストラクタ
	 * @param messages
	 * @param e
	 */
	public HinemosNotFound(String messages, Throwable e) {
		super(messages, e);
	}


}
