/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.fault;

/**
 * applicationIDが重複している場合に利用するException
 *
 */
public class SdmlControlSettingDuplicate extends HinemosDuplicate {

	private static final long serialVersionUID = -659644818233709097L;
	private String applicationId = null;

	/**
	 * コンストラクタ
	 */
	public SdmlControlSettingDuplicate() {
		super();
	}

	/**
	 * コンストラクタ
	 * @param messages
	 */
	public SdmlControlSettingDuplicate(String messages) {
		super(messages);
	}

	/**
	 * コンストラクタ
	 * @param e
	 */
	public SdmlControlSettingDuplicate(Throwable e) {
		super(e);
	}

	/**
	 * コンストラクタ
	 * @param messages
	 * @param e
	 */
	public SdmlControlSettingDuplicate(String messages, Throwable e) {
		super(messages, e);
	}

	public String getApplicationId() {
		return this.applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}
}
