/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * ノードの構成情報履歴が存在しない場合に利用するException
 * 
 * @version 6.2.0
 */
public class NodeHistoryNotFound extends HinemosException {

	private static final long serialVersionUID = -2799194595006299333L;

	/**
	 * NodeHistoryNotFoundコンストラクタ
	 */
	public NodeHistoryNotFound() {
		super();
	}

	/**
	 * NodeHistoryNotFoundコンストラクタ
	 * 
	 * @param messages
	 */
	public NodeHistoryNotFound(String messages) {
		super(messages);
	}

	/**
	 * NodeHistoryNotFoundコンストラクタ
	 * 
	 * @param e
	 */
	public NodeHistoryNotFound(Throwable e) {
		super(e);
	}

}
