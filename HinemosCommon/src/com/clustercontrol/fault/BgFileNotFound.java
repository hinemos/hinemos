/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * ノードマップで背景画像が存在しない場合に利用するException
 * @version 3.2.0
 */
public class BgFileNotFound extends HinemosNotFound {

	private static final long serialVersionUID = -9189072636555316265L;

	/**
	 * コンストラクタ
	 */
	public BgFileNotFound() {
		super();
	}

	/**
	 * コンストラクタ
	 * @param messages
	 */
	public BgFileNotFound(String messages) {
		super(messages);
	}

	/**
	 * コンストラクタ
	 * @param e
	 */
	public BgFileNotFound(Throwable e) {
		super(e);
	}

	/**
	 * コンストラクタ
	 * @param messages
	 * @param e
	 */
	public BgFileNotFound(String messages, Throwable e) {
		super(messages, e);
	}
}
