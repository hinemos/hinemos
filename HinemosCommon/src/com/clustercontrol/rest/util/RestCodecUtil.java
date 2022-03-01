/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Restで使用する文字のエンコード/デコード用ユーティリティクラス
 * 
 * 以下の用途を想定
 * URLのパラメータに特殊文字を使用する場合
 * 
 */

public class RestCodecUtil {

	private static Log m_log = LogFactory.getLog(RestCodecUtil.class);
	
	private static Charset charset = StandardCharsets.UTF_8;

	/**
	 *  コンストラクタ
	 * 
	 */
	public RestCodecUtil(){
	}
	
	/**
	 * 文字列をエンコードする
	 * 
	 * @param string エンコードする文字列
	 * @return encodedString エンコード後の文字列
	 * 
	 */
	public static String stringEncode(String string) {
		byte[]encodedBytes = Base64.encodeBase64URLSafe(string.getBytes(charset));
		String encodedString = new String(encodedBytes, charset); 
		
		m_log.debug("string = " + string + " , encodedString = " + encodedString);
		return encodedString;
	}
	
	/**
	 * 文字列をデコードする
	 * 
	 * @param string デコードする文字列
	 * @return encodedString デコード後の文字列
	 * 
	 */
	public static String stringDecode(String string) {
		byte[] decodedBytes = Base64.decodeBase64(string);
		String decodedString = new String(decodedBytes, charset);
		
		m_log.debug("string = " + string + " , decodedString = " + decodedString);
		return decodedString;
	}

}
