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
 * 適切なタイムゾーンではない場合に利用するException
 * @version 5.1.0
 */
public class InvalidTimezone extends HinemosException {

	private static final long serialVersionUID = 1636867491172325635L;

	/**
	 * コンストラクタ
	 */
	public InvalidTimezone() {
		super();
	}

	/**
	 * コンストラクタ
	 * @param messages
	 */
	public InvalidTimezone(String messages) {
		super(messages);
	}

	/**
	 * コンストラクタ
	 * @param e
	 */
	public InvalidTimezone(Throwable e) {
		super(e);
	}

	/**
	 * コンストラクタ
	 * @param messages
	 * @param e
	 */
	public InvalidTimezone(String messages, Throwable e) {
		super(messages, e);
	}
}