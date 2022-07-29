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
	
	private static final int maxLengthForStatus = 4096;

	/**
	 * 引数strがnullか空文字の場合にtrueを返します.
	 * @param str 判定対象 
	 * @return nullか空文字の場合にtrueを返します.
	 */
	public static boolean isNullOrEmpty(String str) {
		return Objects.isNull(str) || Objects.equals(str, "");
	}

	/**
	 * 引数str が maxLengthForStatus を超えていた場合に 末尾を削ります。
	 * @param str 対象 
	 * @return  処理結果
	 */
	public static String cutTailForStatus(String str ) {
		if(str.length() > maxLengthForStatus){
			str = str.substring(0, maxLengthForStatus - 3) + "...";
		}
		return str;
	}
	
	
}
