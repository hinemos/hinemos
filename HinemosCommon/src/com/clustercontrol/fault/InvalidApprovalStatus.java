/*

Copyright (C) 2016 NTT DATA Corporation

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
 * 承認可能な状態ではない場合に利用するException
 * @version 6.0.0
 */
public class InvalidApprovalStatus extends HinemosException {

	private static final long serialVersionUID = -1L;

	/**
	 * InvalidApprovalStatusのコンストラクタ
	 */
	public InvalidApprovalStatus() {
		super();
	}

	/**
	 * InvalidApprovalStatusのコンストラクタ
	 * @param messages
	 */
	public InvalidApprovalStatus(String messages) {
		super(messages);
	}

	/**
	 * InvalidApprovalStatusのコンストラクタ
	 * @param e
	 */
	public InvalidApprovalStatus(Throwable e) {
		super(e);
	}

	/**
	 * InvalidApprovalStatusのコンストラクタ
	 * @param messages
	 * @param e
	 */
	public InvalidApprovalStatus(String messages, Throwable e) {
		super(messages, e);
	}
}
