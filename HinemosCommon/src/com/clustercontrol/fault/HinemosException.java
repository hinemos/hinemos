/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * このExceptionは直接利用しないこと。
 * 本来はabstractにすべきだが、jaxbの仕様により、abstractは付けない。
 * 
 * 本クラスを継承する場合、コンストラクタには必ず
 * ・引数 無し コンストラクタ（com.clustercontrol.rest.util.RestLoggerで利用）
 * ・引数 String messages コンストラクタ（com.clustercontrol.util.ExceptionUtilで利用）
 * を実装する事
 * 
 * @version 3.2.0
 */
public class HinemosException extends Exception {

	private static final long serialVersionUID = 150339242320506932L;

	/**
	 * HinemosExceptionコンストラクタ
	 */
	public HinemosException() {
		super();
	}

	/**
	 * HinemosExceptionコンストラクタ
	 * @param messages
	 */
	public HinemosException(String messages) {
		super(messages);
	}

	/**
	 * HinemosExceptionコンストラクタ
	 * @param e
	 */
	public HinemosException(Throwable e) {
		super(e);
	}

	/**
	 * HinemosExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public HinemosException(String messages, Throwable e) {
		super(messages, e);
	}

}
