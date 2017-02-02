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

package com.clustercontrol.nodemap;


/**
 * NodeMapEJBプロジェクトでは、想定内のエラーはNodeMapExceptionを利用する。
 * com.clustercontrolのException群と統合する予定。
 * @since 1.0.0
 */
public class NodeMapException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4082023747036586553L;

	/**
	 * NodeMapExceptionコンストラクタ
	 * @param messages
	 */
	public NodeMapException(String messages) {
		super(messages);
	}

	/**
	 * NodeMapExceptionコンストラクタ
	 * @param e
	 */
	public NodeMapException(Throwable e) {
		super(e);
	}

	/**
	 * NodeMapExceptionコンストラクタ
	 * @param messages
	 * @param e
	 */
	public NodeMapException(String messages, Throwable e) {
		super(messages, e);
	}
}
