/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.fault;

import com.clustercontrol.fault.HinemosException;;


/**
 * プロパティに値が存在しない場合に利用するException
 * @version 5.0.0
 */
public class ReportingPropertyNotFound extends HinemosException {

	private static final long serialVersionUID = 3178697208871226266L;
	private String m_key = null;

	/**
	 * コンストラクタ
	 */
	public ReportingPropertyNotFound() {
		super();
	}

	/**
	 * コンストラクタ
	 * @param messages
	 * @param e
	 */
	public ReportingPropertyNotFound(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * コンストラクタ
	 * @param messages
	 */
	public ReportingPropertyNotFound(String messages) {
		super(messages);
	}

	/**
	 * コンストラクタ
	 * @param e
	 */
	public ReportingPropertyNotFound(Throwable e) {
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
