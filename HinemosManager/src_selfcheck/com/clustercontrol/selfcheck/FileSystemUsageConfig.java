/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.selfcheck;

/**
 * セルフチェック機能におけるファイルシステム使用率の監視定義クラス
 */
public class FileSystemUsageConfig {

	// 監視対象とするファイルシステムのマウントポイント
	public final String mountPoint;

	// ファイルシステム使用率の通知閾値
	public final int percentThreshold;

	public FileSystemUsageConfig(String mountPoint, int percentThreshold) {
		this.mountPoint = mountPoint;
		this.percentThreshold = percentThreshold;
	}

}
