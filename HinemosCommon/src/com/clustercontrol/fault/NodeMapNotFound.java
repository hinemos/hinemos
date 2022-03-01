/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * NodeMapが存在しない場合に利用するException
 * @version 3.2.0
 */
public class NodeMapNotFound extends HinemosNotFound {

	private static final long serialVersionUID = -4012154669374250146L;

	/**
	 * NodeMapNotFoundコンストラクタ
	 */
	public NodeMapNotFound() {
		super();
	}

	/**
	 * NodeMapNotFoundコンストラクタ
	 * @param messages
	 */
	public NodeMapNotFound(String messages) {
		super(messages);
	}

	/**
	 * NodeMapNotFoundコンストラクタ
	 * @param e
	 */
	public NodeMapNotFound(Throwable e) {
		super(e);
	}

	/**
	 * NodeMapNotFoundコンストラクタ
	 * @param messages
	 * @param e
	 */
	public NodeMapNotFound(String messages, Throwable e) {
		super(messages, e);
	}

}
