/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * ジョブ定義が不正な場合に利用するException
 * @version 3.2.0
 */
public class JobInvalid extends HinemosInvalid {

	private static final long serialVersionUID = -7692063951801951629L;

	/**
	 * JobInvalidExceptionコンストラクタ
	 */
	public JobInvalid() {
		super();
	}

	/**
	 * JobInvalidExceptionコンストラクタ
	 * @param messages
	 */
	public JobInvalid(String messages) {
		super(messages);
	}

	/**
	 * JobInvalidExceptionコンストラクタ
	 * @param e
	 */
	public JobInvalid(Throwable e) {
		super(e);
	}

	/**
	 * JobInvalidExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public JobInvalid(String messages, Throwable e) {
		super(messages, e);
	}
}
