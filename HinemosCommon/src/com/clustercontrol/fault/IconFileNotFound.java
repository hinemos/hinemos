/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * ノードマップでアイコンファイルが存在しない場合に利用するException
 * @version 3.2.0
 */
public class IconFileNotFound extends HinemosException {

	private static final long serialVersionUID = 5241727385182864490L;

	/**
	 * コンストラクタ
	 */
	public IconFileNotFound() {
		super();
	}

	/**
	 * コンストラクタ
	 * @param messages
	 */
	public IconFileNotFound(String messages) {
		super(messages);
	}

	/**
	 * コンストラクタ
	 * @param e
	 */
	public IconFileNotFound(Throwable e) {
		super(e);
	}

	/**
	 * コンストラクタ
	 * @param messages
	 * @param e
	 */
	public IconFileNotFound(String messages, Throwable e) {
		super(messages, e);
	}
}
