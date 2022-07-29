/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * ノードの構成情報履歴がすでに存在している場合に利用するException
 * 
 * @version 6.2.0
 */
public class NodeHistoryRegistered extends HinemosException {

	private static final long serialVersionUID = -2799194595006299333L;

	/**
	 * NodeHistoryRegisteredコンストラクタ
	 */
	public NodeHistoryRegistered() {
		super();
	}

	/**
	 * NodeHistoryRegisteredコンストラクタ
	 * 
	 * @param messages
	 */
	public NodeHistoryRegistered(String messages) {
		super(messages);
	}

	/**
	 * NodeHistoryRegisteredコンストラクタ
	 * 
	 * @param e
	 */
	public NodeHistoryRegistered(Throwable e) {
		super(e);
	}

}
