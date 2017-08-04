/*

Copyright (C) 2017 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.platform;

import com.clustercontrol.util.CommandCreator.PlatformType;

/**
 * 環境差分のある値や処理を格納するクラス（rhel）<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class PlatformPertial {
	
	// コマンド実行時の自身のプラットフォーム種別
	private static final PlatformType SELF_PLATFORM_TYPE = PlatformType.UNIX;

	public static PlatformType getPlatformType() {
		return SELF_PLATFORM_TYPE;
	}
	
	public static void setupHostname() {
		// do nothing. windows only.
	}
}