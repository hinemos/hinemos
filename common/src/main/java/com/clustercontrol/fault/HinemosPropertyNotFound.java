/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
