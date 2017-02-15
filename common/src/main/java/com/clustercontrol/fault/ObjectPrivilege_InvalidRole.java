/*

Copyright (C) 2011 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
