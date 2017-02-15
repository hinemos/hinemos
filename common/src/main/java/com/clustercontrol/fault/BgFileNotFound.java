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
 * ノードマップで背景画像が存在しない場合に利用するException
 * @version 3.2.0
 */
public class BgFileNotFound extends HinemosException {

	private static final long serialVersionUID = -9189072636555316265L;

	/**
	 * コンストラクタ
	 */
	public BgFileNotFound() {
		super();
	}

	/**
	 * コンストラクタ
	 * @param messages
	 */
	public BgFileNotFound(String messages) {
		super(messages);
	}

	/**
	 * コンストラクタ
	 * @param e
	 */
	public BgFileNotFound(Throwable e) {
		super(e);
	}

	/**
	 * コンストラクタ
	 * @param messages
	 * @param e
	 */
	public BgFileNotFound(String messages, Throwable e) {
		super(messages, e);
	}
}
