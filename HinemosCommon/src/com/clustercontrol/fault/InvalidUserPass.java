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
 * ユーザとパスワードの組み合わせが間違っている場合に利用するException
 * @version 3.2.0
 */
public class InvalidUserPass extends HinemosException {

	private static final long serialVersionUID = -6401349857743376125L;

	/**
	 * コンストラクタ
	 */
	public InvalidUserPass() {
		super();
	}

	/**
	 * コンストラクタ
	 * @param messages
	 */
	public InvalidUserPass(String messages) {
		super(messages);
	}

	/**
	 * コンストラクタ
	 * @param e
	 */
	public InvalidUserPass(Throwable e) {
		super(e);
	}

	/**
	 * コンストラクタ
	 * @param messages
	 * @param e
	 */
	public InvalidUserPass(String messages, Throwable e) {
		super(messages, e);
	}
}
