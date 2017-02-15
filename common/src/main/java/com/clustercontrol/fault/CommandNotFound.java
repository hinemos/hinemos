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
 * facilityIDが存在しない場合に利用するException
 * @version 3.2.0
 */
public class CommandNotFound extends HinemosException {

	private static final long serialVersionUID = -435935118433877282L;

	/**
	 * FacilityNotFoundExceptionコンストラクタ
	 */
	public CommandNotFound() {
		super();
	}

	/**
	 * FacilityNotFoundExceptionコンストラクタ
	 * @param messages
	 */
	public CommandNotFound(String messages) {
		super(messages);
	}

	/**
	 * FacilityNotFoundExceptionコンストラクタ
	 * @param e
	 */
	public CommandNotFound(Throwable e) {
		super(e);
	}

	/**
	 * FacilityNotFoundExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public CommandNotFound(String messages, Throwable e) {
		super(messages, e);
	}
}
