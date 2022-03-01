/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * このExceptionは直接利用しないこと。
 * 本来はabstractにすべきだが、HinemosExceptionのつくりを踏襲して、abstractは付けない。
 */
public class HinemosDuplicate extends HinemosException {

	private static final long serialVersionUID = -5612525221540116629L;
	private String m_key = null;

	/**
	 * コンストラクタ
	 */
	public HinemosDuplicate() {
		super();
	}

	/**
	 * コンストラクタ
	 * @param messages
	 */
	public HinemosDuplicate(String messages) {
		super(messages);
	}

	/**
	 * コンストラクタ
	 * @param e
	 */
	public HinemosDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * コンストラクタ
	 * @param messages
	 * @param e
	 */
	public HinemosDuplicate(String messages, Throwable e) {
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
