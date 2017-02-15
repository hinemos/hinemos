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
 * ジョブ定義が不正な場合に利用するException
 * @version 3.2.0
 */
public class JobInvalid extends HinemosException {

	private static final long serialVersionUID = -7692063951801951629L;

	/**
	 * JobInvalidExceptionコンストラクタ
	 */
	public JobInvalid() {
		super();
	}

	/**
	 * JobInvalidExceptionコンストラクタ
	 * @param messages
	 */
	public JobInvalid(String messages) {
		super(messages);
	}

	/**
	 * JobInvalidExceptionコンストラクタ
	 * @param e
	 */
	public JobInvalid(Throwable e) {
		super(e);
	}

	/**
	 * JobInvalidExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public JobInvalid(String messages, Throwable e) {
		super(messages, e);
	}
}
