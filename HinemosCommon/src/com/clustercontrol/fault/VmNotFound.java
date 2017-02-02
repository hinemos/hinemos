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
 * Vm関連のレコードが存在しない場合に利用するException
 * @version 3.2.0
 */
public class VmNotFound extends HinemosException {

	private static final long serialVersionUID = 1322162894275102404L;

	/**
	 * VmNotFoundコンストラクタ
	 */
	public VmNotFound() {
		super();
	}

	/**
	 * VmNotFoundコンストラクタ
	 * @param messages
	 */
	public VmNotFound(String messages) {
		super(messages);
	}

	/**
	 * VmNotFoundコンストラクタ
	 * @param e
	 */
	public VmNotFound(Throwable e) {
		super(e);
	}

	/**
	 * VmNotFoundコンストラクタ
	 * @param messages
	 * @param e
	 */
	public VmNotFound(String messages, Throwable e) {
		super(messages, e);
	}

}
