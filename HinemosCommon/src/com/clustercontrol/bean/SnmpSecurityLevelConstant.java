/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.bean;


/**
 * SNMPセキュリティレベルの定義クラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class SnmpSecurityLevelConstant {
	/**  認証なし・暗号化なし */
	public static final String  NOAUTH_NOPRIV = "noauth_nopriv";

	/**  認証あり・暗号化なし */
	public static final String  AUTH_NOPRIV = "auth_nopriv";

	/**  認証あり・暗号化あり */
	public static final String  AUTH_PRIV = "auth_priv";

}