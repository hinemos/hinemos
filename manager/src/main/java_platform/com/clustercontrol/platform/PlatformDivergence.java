/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.platform;

import com.clustercontrol.util.CommandCreator.PlatformType;

/**
 * 環境差分のある値や処理を格納するクラス（rhel）<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class PlatformDivergence {
	
	// コマンド実行時の自身のプラットフォーム種別
	private static final PlatformType SELF_PLATFORM_TYPE = PlatformType.UNIX;

	public static PlatformType getPlatformType() {
		return SELF_PLATFORM_TYPE;
	}
	
	public static void setupHostname() {
		// do nothing. windows only.
	}
}