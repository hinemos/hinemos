/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.hub.bean;


/**
 * Quartz関連の定義を定数として格納するクラスです。
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class QuartzConstant {

	/**
	 *  メンテナンスのグループ。<BR>
	 *  メンテナンス機能のスケジュール実行を呼び出すQuartzのメンテナンスのグループ名です。
	 */
	public static final String GROUP_NAME = "HUBTransfer";

	/**
	 *  Quartzから呼び出すメソッド名。<BR>
	 *  メンテナンス機能のスケジュール実行を行う Session Bean のメソッド名です。
	 */
	public static final String METHOD_NAME = "scheduleRunTransfer";
}
