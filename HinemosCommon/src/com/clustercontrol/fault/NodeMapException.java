/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * NodeMapEJBプロジェクトでは、想定内のエラーはNodeMapExceptionを利用する。
 * com.clustercontrolのException群と統合する予定。
 * @since 1.0.0
 */
public class NodeMapException extends HinemosException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4082023747036586553L;

	/**
	 * NodeMapExceptionコンストラクタ
	 */
	public NodeMapException() {
		super();
	}
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
