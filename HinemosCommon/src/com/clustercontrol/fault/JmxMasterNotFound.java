/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * JMX 監視項目マスタが存在しない場合に利用するException
 * @version 5.0.0
 * @since 5.0.0
 */
public class JmxMasterNotFound extends HinemosException {

	private static final long serialVersionUID = 8373432999993350125L;

	/**
	 * JmxMasterNotFoundコンストラクタ
	 */
	public JmxMasterNotFound() {
		super();
	}

	/**
	 * JmxMasterNotFoundコンストラクタ
	 * @param messages
	 */
	public JmxMasterNotFound(String messages) {
		super(messages);
	}

	/**
	 * JmxMasterNotFoundコンストラクタ
	 * @param e
	 */
	public JmxMasterNotFound(Throwable e) {
		super(e);
	}

	/**
	 * JmxMasterNotFoundコンストラクタ
	 * @param messages
	 * @param e
	 */
	public JmxMasterNotFound(String messages, Throwable e) {
		super(messages, e);
	}
}
