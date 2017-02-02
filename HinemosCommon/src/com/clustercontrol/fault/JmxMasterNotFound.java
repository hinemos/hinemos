/*

Copyright (C) 2014 NTT DATA Corporation

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
 * JMX 監視項目マスタが存在しない場合に利用するException
 * @version 5.0.0
 * @since 5.0.0
 */
public class JmxMasterNotFound extends HinemosException {

	private static final long serialVersionUID = 8373432999993350125L;

	/**
	 * JmxMasterNotFoundコンストラクタ
	 */
	public JmxMasterNotFound() {
		super();
	}

	/**
	 * JmxMasterNotFoundコンストラクタ
	 * @param messages
	 */
	public JmxMasterNotFound(String messages) {
		super(messages);
	}

	/**
	 * JmxMasterNotFoundコンストラクタ
	 * @param e
	 */
	public JmxMasterNotFound(Throwable e) {
		super(e);
	}

	/**
	 * JmxMasterNotFoundコンストラクタ
	 * @param messages
	 * @param e
	 */
	public JmxMasterNotFound(String messages, Throwable e) {
		super(messages, e);
	}
}
