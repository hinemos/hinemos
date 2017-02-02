/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.monitor.bean;

/**
 * イベント情報の確認状態の定義を定数として格納するクラス<BR>
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class HttpAuthTypeConstant {
	/** ベーシック認証(種別) */
	public static final String BASIC = "BASIC";

	/** ダイジェスト認証(種別) */
	public static final String DIGEST = "DIGEST";

	/** NTLM認証(種別) */
	public static final String NTLM = "NTLM";
}