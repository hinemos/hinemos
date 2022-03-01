/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * 存在しないURLへアクセスがあった場合に利用する例外
 */
public class UrlNotFound extends HinemosNotFound {

	private static final long serialVersionUID = -5140106939019160410L;

	/**
	 * UrlNotFoundコンストラクタ
	 */
	public UrlNotFound() {
		super();
	}

	/**
	 * UrlNotFoundコンストラクタ
	 * @param messages
	 */
	public UrlNotFound(String messages) {
		super(messages);
	}

	/**
	 * UrlNotFoundコンストラクタ
	 * @param e
	 */
	public UrlNotFound(Throwable e) {
		super(e);
	}

	/**
	 * UrlNotFoundコンストラクタ
	 * @param messages
	 * @param e
	 */
	public UrlNotFound(String messages, Throwable e) {
		super(messages, e);
	}
}
