/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa.util;

/**
 * ログイン結果を定義するEnum
 *
 */
public enum LoginResultEnum {
	/** 成功 */
	SUCCESS,
	/** キャンセル */
	CANCELL,
	/** ログインエラー */
	LOGIN_ERROR,

	/** 不明 */
	UNKNOWN
}
