/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.fault;

/**
 * NodeMapで、マップ上の表示要素についてアクセス権限がない場合に利用するException
 * 
 */
public class NodeMapElementNoPrivilege extends NodeMapException {

	private static final long serialVersionUID = -1346672924169215356L;

	/**
	 * コンストラクタ
	 */
	public NodeMapElementNoPrivilege() {
		super();
	}

	/**
	 * コンストラクタ
	 * @param messages
	 */
	public NodeMapElementNoPrivilege(String messages) {
		super(messages);
	}

	/**
	 * コンストラクタ
	 * @param e
	 */
	public NodeMapElementNoPrivilege(Throwable e) {
		super(e);
	}

	/**
	 * コンストラクタ
	 * @param messages
	 * @param e
	 */
	public NodeMapElementNoPrivilege(String messages, Throwable e) {
		super(messages, e);
	}
}
