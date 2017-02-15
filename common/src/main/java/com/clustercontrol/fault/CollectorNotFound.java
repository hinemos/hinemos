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
 * Collectorが存在しない場合に利用するException
 * @version 3.2.0
 */
public class CollectorNotFound extends HinemosException {

	private static final long serialVersionUID = 8373432999993350125L;

	/**
	 * CollectorNotFoundコンストラクタ
	 */
	public CollectorNotFound() {
		super();
	}

	/**
	 * CollectorNotFoundコンストラクタ
	 * @param messages
	 */
	public CollectorNotFound(String messages) {
		super(messages);
	}

	/**
	 * CollectorNotFoundコンストラクタ
	 * @param e
	 */
	public CollectorNotFound(Throwable e) {
		super(e);
	}

	/**
	 * CollectorNotFoundコンストラクタ
	 * @param messages
	 * @param e
	 */
	public CollectorNotFound(String messages, Throwable e) {
		super(messages, e);
	}

}
