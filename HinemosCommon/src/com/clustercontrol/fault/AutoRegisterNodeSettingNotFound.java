/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * ノード自動登録に関する設定が存在しない場合に利用するException
 * 
 * @version 6.2.0
 * @since 6.2.0
 */
public class AutoRegisterNodeSettingNotFound extends HinemosNotFound {

	private static final long serialVersionUID = -2799194595006299333L;
	private String m_priority = null;

	/**
	 * コンストラクタ
	 */
	public AutoRegisterNodeSettingNotFound() {
		super();
	}

	/**
	 * コンストラクタ
	 * 
	 * @param messages
	 */
	public AutoRegisterNodeSettingNotFound(String messages) {
		super(messages);
	}

	/**
	 * コンストラクタ
	 * 
	 * @param e
	 */
	public AutoRegisterNodeSettingNotFound(Throwable e) {
		super(e);
	}

	/**
	 * コンストラクタ
	 * 
	 * @param messages
	 * @param e
	 */
	public AutoRegisterNodeSettingNotFound(String messages, Throwable e) {
		super(messages, e);
	}

	public String getPriority() {
		return m_priority;
	}

	public void setPriority(String m_priority) {
		this.m_priority = m_priority;
	}

}
