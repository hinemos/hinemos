/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.fault;

/**
 * SDML制御設定が存在しない場合に利用するException
 *
 */
public class SdmlControlSettingNotFound extends HinemosNotFound {

	private static final long serialVersionUID = -7047984499006260024L;
	private String applicationId = null;

	/**
	 * コンストラクタ
	 */
	public SdmlControlSettingNotFound() {
		super();
	}

	/**
	 * コンストラクタ
	 * @param messages
	 * @param e
	 */
	public SdmlControlSettingNotFound(String messages, Throwable e) {
		super(messages, e);
	}

	/**
	 * コンストラクタ
	 * @param messages
	 */
	public SdmlControlSettingNotFound(String messages) {
		super(messages);
	}

	/**
	 * コンストラクタ
	 * @param e
	 */
	public SdmlControlSettingNotFound(Throwable e) {
		super(e);
	}

	public String getApplicationId() {
		return this.applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}
}
