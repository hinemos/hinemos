/*

Copyright (C) 2010 NTT DATA Corporation

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
 * Hinemos独自のErrorの場合に利用するException
 * @version 3.2.0
 */
public class SnmpResponseError extends HinemosException {

	private static final long serialVersionUID = 6157131613222270776L;

	/**
	 * HinemosUnknownコンストラクタ
	 */
	public SnmpResponseError() {
		super();
	}

	/**
	 * HinemosUnknownコンストラクタ
	 * @param messages
	 */
	public SnmpResponseError(String messages) {
		super(messages);
	}

	/**
	 * HinemosUnknownコンストラクタ
	 * @param e
	 */
	public SnmpResponseError(Throwable e) {
		super(e);
	}

	/**
	 * HinemosUnknownコンストラクタ
	 * @param messages
	 * @param e
	 */
	public SnmpResponseError(String messages, Throwable e) {
		super(messages, e);
	}
}
