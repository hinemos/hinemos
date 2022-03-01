/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * 不正なJSONを受信した際に発生する例外
 * 
 */
public class RequestJsonInvalidException extends HinemosInvalid {

	private static final long serialVersionUID = 1300450541890103758L;

	/**
	 * HinemosUnknownコンストラクタ
	 */
	public RequestJsonInvalidException() {
		super();
	}

	/**
	 * RequestJsonInvalidExceptionコンストラクタ
	 * 
	 * @param messages
	 */
	public RequestJsonInvalidException(String messages) {
		super(messages);
	}

	/**
	 * RequestJsonInvalidExceptionコンストラクタ
	 * 
	 * @param e
	 */
	public RequestJsonInvalidException(Throwable e) {
		super(e);
	}

	/**
	 * RequestJsonInvalidExceptionコンストラクタ
	 * 
	 * @param messages
	 * @param e
	 */
	public RequestJsonInvalidException(String messages, Throwable e) {
		super(messages, e);
	}
}
