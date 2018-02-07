/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.util;

import java.util.Objects;

/**
 * 文字列ユーティリティクラス
 * @version 6.1.0
 * @since 6.0.0
 */
public class StringUtil {

	/**
	 * 引数strがnullか空文字の場合にtrueを返します.
	 * @param str 判定対象 
	 * @return nullか空文字の場合にtrueを返します.
	 */
	public static boolean isNullOrEmpty(String str) {
		return Objects.isNull(str) || Objects.equals(str, "");
	}
}
