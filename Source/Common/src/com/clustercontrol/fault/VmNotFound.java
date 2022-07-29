/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * Vm関連のレコードが存在しない場合に利用するException
 * @version 3.2.0
 */
public class VmNotFound extends HinemosNotFound {

	private static final long serialVersionUID = 1322162894275102404L;

	/**
	 * VmNotFoundコンストラクタ
	 */
	public VmNotFound() {
		super();
	}

	/**
	 * VmNotFoundコンストラクタ
	 * @param messages
	 */
	public VmNotFound(String messages) {
		super(messages);
	}

	/**
	 * VmNotFoundコンストラクタ
	 * @param e
	 */
	public VmNotFound(Throwable e) {
		super(e);
	}

	/**
	 * VmNotFoundコンストラクタ
	 * @param messages
	 * @param e
	 */
	public VmNotFound(String messages, Throwable e) {
		super(messages, e);
	}

}
