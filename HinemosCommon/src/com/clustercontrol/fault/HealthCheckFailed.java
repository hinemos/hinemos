/*
 * Copyright (c) 2020 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * ヘルスチェックに失敗した場合に利用するException
 * @version 6.2.2
 */
public class HealthCheckFailed extends HinemosException {

	private static final long serialVersionUID = 1300450541890103758L;

	/**
	 * HealthCheckFailedコンストラクタ
	 */
	public HealthCheckFailed() {
		super();
	}

	/**
	 * HealthCheckFailedコンストラクタ
	 * @param messages
	 */
	public HealthCheckFailed(String messages) {
		super(messages);
	}

	/**
	 * HealthCheckFailedコンストラクタ
	 * @param e
	 */
	public HealthCheckFailed(Throwable e) {
		super(e);
	}

	/**
	 * HealthCheckFailedコンストラクタ
	 * @param messages
	 * @param e
	 */
	public HealthCheckFailed(String messages, Throwable e) {
		super(messages, e);
	}
}
