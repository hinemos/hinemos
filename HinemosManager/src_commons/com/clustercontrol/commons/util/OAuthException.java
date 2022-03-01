/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import com.clustercontrol.fault.HinemosException;

/**
 * OAuth認証時に利用するException
 */
public class OAuthException extends HinemosException {

	private static final long serialVersionUID = -4025269791079786280L;

	/**
	 * OAuthExceptionコンストラクタ
	 */
	public OAuthException() {
		super();
	}

	/**
	 * OAuthExceptionコンストラクタ
	 * @param messages
	 */
	public OAuthException(String messages) {
		super(messages);
	}

	/**
	 * OAuthExceptionコンストラクタ
	 * @param e
	 */
	public OAuthException(Throwable e) {
		super(e);
	}

	/**
	 * OAuthExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public OAuthException(String messages, Throwable e) {
		super(messages, e);
	}
}
