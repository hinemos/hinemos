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
public class HinemosInvalid extends HinemosException {

	private static final long serialVersionUID = -8451492735110902680L;

	/**
	 * HinemosInvalidコンストラクタ
	 */
	public HinemosInvalid() {
		super();
	}

	/**
	 * HinemosInvalidコンストラクタ
	 * @param messages
	 */
	public HinemosInvalid(String messages) {
		super(messages);
	}

	/**
	 * HinemosInvalidコンストラクタ
	 * @param e
	 */
	public HinemosInvalid(Throwable e) {
		super(e);
	}

	/**
	 * HinemosInvalidコンストラクタ
	 * @param messages
	 * @param e
	 */
	public HinemosInvalid(String messages, Throwable e) {
		super(messages, e);
	}


}
