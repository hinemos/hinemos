/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * オブジェクト権限エラーの場合に利用するRuntimeException
 * 
 * HinemosEntityManagerクラスのfindメソッド、find_ORメソッド実行時、
 * commit時に発生する。
 * 
 * findメソッド実行時は、
 * findメソッドを呼び出しているQueryUtilクラスのメソッドにてcatchしてInvalidRole等でthrowすること。
 * commit時は、 呼び出し元にてcatchすること。
 * 
 */
public class ObjectPrivilege_InvalidRole extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * コンストラクタ
	 */
	public ObjectPrivilege_InvalidRole() {
		super();
	}

	/**
	 * コンストラクタ
	 * @param messages
	 */
	public ObjectPrivilege_InvalidRole(String messages) {
		super(messages);
	}

	/**
	 * コンストラクタ
	 * @param e
	 */
	public ObjectPrivilege_InvalidRole(Throwable e) {
		super(e);
	}

	/**
	 * コンストラクタ
	 * @param messages
	 * @param e
	 */
	public ObjectPrivilege_InvalidRole(String messages, Throwable e) {
		super(messages, e);
	}
}
