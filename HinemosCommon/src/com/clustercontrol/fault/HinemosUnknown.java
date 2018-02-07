/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * Hinemos独自のErrorの場合に利用するException
 * @version 3.2.0
 */
public class HinemosUnknown extends HinemosException {

	private static final long serialVersionUID = 1300450541890103758L;

	/**
	 * HinemosUnknownコンストラクタ
	 */
	public HinemosUnknown() {
		super();
	}

	/**
	 * HinemosUnknownコンストラクタ
	 * @param messages
	 */
	public HinemosUnknown(String messages) {
		super(messages);
	}

	/**
	 * HinemosUnknownコンストラクタ
	 * @param e
	 */
	public HinemosUnknown(Throwable e) {
		super(e);
	}

	/**
	 * HinemosUnknownコンストラクタ
	 * @param messages
	 * @param e
	 */
	public HinemosUnknown(String messages, Throwable e) {
		super(messages, e);
	}
}
