/*
 * Copyright (c) 2024 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import com.clustercontrol.fault.InvalidSetting;

/**
 * クラウドサービスモードで通常のInvalidSettingとは区別する必要がある例外に利用するException
 */
public class InvalidSettingByCloudServiceMode extends InvalidSetting {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8399394566987141361L;

	/**
	 * InvalidSettingByCloudServiceModeのコンストラクタ
	 */
	public InvalidSettingByCloudServiceMode() {
		super();
	}

	/**
	 * InvalidSettingByCloudServiceModeのコンストラクタ
	 * 
	 * @param messages
	 */
	public InvalidSettingByCloudServiceMode(String messages) {
		super(messages);
	}

	/**
	 * InvalidSettingByCloudServiceModeのコンストラクタ
	 * 
	 * @param e
	 */
	public InvalidSettingByCloudServiceMode(Throwable e) {
		super(e);
	}

	/**
	 * InvalidSettingByCloudServiceModeのコンストラクタ
	 * 
	 * @param messages
	 * @param e
	 */
	public InvalidSettingByCloudServiceMode(String messages, Throwable e) {
		super(messages, e);
	}

}
