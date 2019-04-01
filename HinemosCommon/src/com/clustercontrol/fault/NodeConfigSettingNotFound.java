/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * NodeConfigSettingInfoが存在しない場合に利用するException
 * 
 * @version 6.2.0
 */
public class NodeConfigSettingNotFound extends HinemosException {

	private static final long serialVersionUID = -2799194595006299333L;
	private String m_settingId = null;

	/**
	 * NodeConfigSettingNotFoundコンストラクタ
	 */
	public NodeConfigSettingNotFound() {
		super();
	}

	/**
NodeConfigSettingNotFoundコンストラクタ
	 * @param messages
	 */
	public NodeConfigSettingNotFound(String messages) {
		super(messages);
	}

	/**
	 * NodeConfigSettingNotFoundコンストラクタ
	 * @param e
	 */
	public NodeConfigSettingNotFound(Throwable e) {
		super(e);
	}

	/**
	 * NodeConfigSettingNotFoundコンストラクタ
	 * @param messages
	 * @param e
	 */
	public NodeConfigSettingNotFound(String messages, Throwable e) {
		super(messages, e);
	}

	public String getSettingId() {
		return m_settingId;
	}

	public void setSettingId(String settingId) {
		m_settingId = settingId;
	}

}
