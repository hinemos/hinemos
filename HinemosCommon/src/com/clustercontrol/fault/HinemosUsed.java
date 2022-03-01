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
public class HinemosUsed extends HinemosException {

	private static final long serialVersionUID = -6113235984469782169L;

	/**
	 * HinemosUsedコンストラクタ
	 */
	public HinemosUsed() {
		super();
	}

	/**
	 * HinemosUsedコンストラクタ
	 * @param messages
	 */
	public HinemosUsed(String messages) {
		super(messages);
	}

	/**
	 * HinemosUsedコンストラクタ
	 * @param e
	 */
	public HinemosUsed(Throwable e) {
		super(e);
	}

	/**
	 * HinemosUsedコンストラクタ
	 * @param messages
	 * @param e
	 */
	public HinemosUsed(String messages, Throwable e) {
		super(messages, e);
	}


}
