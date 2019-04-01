/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * 構成情報検索で条件が存在しない場合に利用するException
 * 
 * @version 6.2.0
 */
public class NodeConfigFilterNotFound extends HinemosException {

	private static final long serialVersionUID = 836935854066835493L;

	/**
	 * NodeConfigFilterNotFoundコンストラクタ
	 */
	public NodeConfigFilterNotFound() {
		super();
	}

	/**
	 * NodeConfigFilterNotFoundコンストラクタ
	 * @param messages
	 */
	public NodeConfigFilterNotFound(String messages) {
		super(messages);
	}

	/**
	 * NodeConfigFilterNotFoundコンストラクタ
	 * @param e
	 */
	public NodeConfigFilterNotFound(Throwable e) {
		super(e);
	}

	/**
	 * NodeConfigFilterNotFoundコンストラクタ
	 * @param messages
	 * @param e
	 */
	public NodeConfigFilterNotFound(String messages, Throwable e) {
		super(messages, e);
	}
}
