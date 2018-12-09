/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap;


/**
 * NodeMapEJBプロジェクトでは、想定内のエラーはNodeMapExceptionを利用する。
 * com.clustercontrolのException群と統合する予定。
 * @since 1.0.0
 */
public class NodeMapException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4082023747036586553L;

	/**
	 * NodeMapExceptionコンストラクタ
	 * @param messages
	 */
	public NodeMapException(String messages) {
		super(messages);
	}

	/**
	 * NodeMapExceptionコンストラクタ
	 * @param e
	 */
	public NodeMapException(Throwable e) {
		super(e);
	}

	/**
	 * NodeMapExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public NodeMapException(String messages, Throwable e) {
		super(messages, e);
	}
}
