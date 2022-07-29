/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.fault;

/**
 * settingIDが重複している場合に利用するException
 * 
 * @version 6.2.0
 */
public class NodeConfigSettingDuplicate extends HinemosDuplicate {

	private static final long serialVersionUID = -5612525221540116629L;
	private String m_settingId = null;

	/**
	 * NodeConfigSettingDuplicateコンストラクタ
	 */
	public NodeConfigSettingDuplicate() {
		super();
	}

	/**
	 * NodeConfigSettingDuplicateコンストラクタ
	 * @param messages
	 */
	public NodeConfigSettingDuplicate(String messages) {
		super(messages);
	}

	/**
	 * NodeConfigSettingDuplicateコンストラクタ
	 * @param e
	 */
	public NodeConfigSettingDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * NodeConfigSettingDuplicateコンストラクタ
	 * @param messages
	 * @param e
	 */
	public NodeConfigSettingDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

	public String getSettingId() {
		return m_settingId;
	}

	public void setSettingId(String settingId) {
		m_settingId = settingId;
	}
}
