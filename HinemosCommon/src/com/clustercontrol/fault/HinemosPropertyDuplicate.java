/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * keyが重複している場合に利用するException
 * @version 5.0.0
 */
public class HinemosPropertyDuplicate extends HinemosDuplicate {

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
