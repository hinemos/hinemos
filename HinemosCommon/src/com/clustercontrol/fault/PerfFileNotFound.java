/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * 性能CSVファイルが存在しない場合に利用するException
 * @version 3.2.0
 */
public class PerfFileNotFound extends HinemosNotFound {

	private static final long serialVersionUID = -9189072636555316265L;

	/**
	 * コンストラクタ
	 */
	public PerfFileNotFound() {
		super();
	}

	/**
	 * コンストラクタ
	 * @param messages
	 */
	public PerfFileNotFound(String messages) {
		super(messages);
	}

	/**
	 * コンストラクタ
	 * @param e
	 */
	public PerfFileNotFound(Throwable e) {
		super(e);
	}

	/**
	 * コンストラクタ
	 * @param messages
	 * @param e
	 */
	public PerfFileNotFound(String messages, Throwable e) {
		super(messages, e);
	}
}
