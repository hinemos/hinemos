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
 * このExceptionは直接利用しないこと。
 * 本来はabstractにすべきだが、jaxbの仕様により、abstractは付けない。
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
