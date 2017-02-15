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
 * 適切なroleを持っていない場合に利用するException
 * @version 3.2.0
 */
public class InvalidRole extends HinemosException {

	private static final long serialVersionUID = 1636867491172325635L;

	/**
	 * コンストラクタ
	 */
	public InvalidRole() {
		super();
	}

	/**
	 * コンストラクタ
	 * @param messages
	 */
	public InvalidRole(String messages) {
		super(messages);
	}

	/**
	 * コンストラクタ
	 * @param e
	 */
	public InvalidRole(Throwable e) {
		super(e);
	}

	/**
	 * コンストラクタ
	 * @param messages
	 * @param e
	 */
	public InvalidRole(String messages, Throwable e) {
		super(messages, e);
	}
}
