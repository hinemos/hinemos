/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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