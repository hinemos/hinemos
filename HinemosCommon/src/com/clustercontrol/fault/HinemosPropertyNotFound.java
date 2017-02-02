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
 * 共通情報が存在しない場合に利用するException
 * @version 5.0.0
 */
public class HinemosPropertyNotFound extends HinemosException {

	private static final long serialVersionUID = 3178697208871226266L;
	private String m_key = null;

	/**
	 * コンストラクタ
	 */
	public HinemosPropertyNotFound() {
		super();
	}

	/**
	 * コンストラクタ
	 * @param messages
	 * @param e
	 */
	public HinemosPropertyNotFound(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * コンストラクタ
	 * @param messages
	 */
	public HinemosPropertyNotFound(String messages) {
		super(messages);
	}

	/**
	 * コンストラクタ
	 * @param e
	 */
	public HinemosPropertyNotFound(Throwable e) {
		super(e);
	}

	/**
	 * キーを返します。
	 * @return キー
	 */
	public String getKey() {
		return m_key;
	}

	/**
	 * キーを設定します。
	 * @param key キー
	 */
	public void setKey(String key) {
		m_key = key;
	}




}
