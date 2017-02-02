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
 * keyが重複している場合に利用するException
 * @version 5.0.0
 */
public class HinemosPropertyDuplicate extends HinemosException {

	private static final long serialVersionUID = -5612525221540116629L;
	private String m_key = null;

	/**
	 * コンストラクタ
	 */
	public HinemosPropertyDuplicate() {
		super();
	}

	/**
	 * コンストラクタ
	 * @param messages
	 */
	public HinemosPropertyDuplicate(String messages) {
		super(messages);
	}

	/**
	 * コンストラクタ
	 * @param e
	 */
	public HinemosPropertyDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * コンストラクタ
	 * @param messages
	 * @param e
	 */
	public HinemosPropertyDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * キーを取得します。
	 * @return m_key
	 */
	public String getKey() {
		return m_key;
	}

	/**
	 * キーをセットします。
	 * @param key
	 */
	public void setKey(String key) {
		m_key = key;
	}
}
