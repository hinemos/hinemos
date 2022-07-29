/*
 * Copyright (c) 2020 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * 文字列を扱うUtilクラス
 *
 */
public class StringUtil {

	/**
	 * byte配列が指定された文字コードかどうかをチェックするメソッド
	 * @param src byte配列
	 * @param encode チェックしたい文字コード
	 * @return true 文字コードが一致した場合
	 *          false 一致しなかった場合
	 */
	public static boolean checkEncode(byte[] src, Charset charset){

		//JISチェック
		for (byte b : src){
			if (b == 0x1B){
				//JISが指定されていた場合
				if(charset.name().equals("JIS") || charset.name().equals("ISO-2022-JP")){
					return true;
				}else{
					//JIS以外が指定されていた場合
					return false;
				}
			}
		}
		
		//JIS以外の場合
		byte[] tmp = new String(src, charset).getBytes(charset);
		return Arrays.equals(tmp, src);
	}
	
	/**
	 * String オブジェクトが null もしくは empty を確認するメソッド
	 * @param str 文字列
	 * @return true nullもしくはemptyの場合
	 *          false 上記以外の場合
	 */
	public static boolean isNullOrEmpty(String str) {
		return str == null || str.isEmpty();
	}
}
