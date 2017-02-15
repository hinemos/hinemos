/*

Copyright (C) 2011 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 文字列内の変数を置換するUtilityクラス<br/>
 * 
 */
public class StringBinder {

	private static Log log = LogFactory.getLog(StringBinder.class);

	private Map<String, String> param = new HashMap<String, String>();
	private static final String _postfixOriginal = ":original";

	private static String replaceChar = "?";
	private static boolean replace = false;

	/**
	 * 置換のマッピングを引数としたコンストラクタ
	 * @param param マッピング
	 */
	public StringBinder(Map<String, String> param) {
		if (param != null) {
			this.param.putAll(param);
		}
	}

	/**
	 * 
	 * @param replaceChar
	 */
	public static void setReplaceChar(String replaceChar) {
		log.info("setReplaceChar() StringBinder Invalid Chars replace to [" + replaceChar + "]");
		StringBinder.replaceChar = replaceChar;
	}

	/**
	 * 
	 * @param replace
	 */
	public static void setReplace(boolean replace) {
		log.info("setReplace() StringBinder Invalid Chars replace is " + replace);
		StringBinder.replace = replace;
	}

	/**
	 * 変数#[KEY]をVALUEに置き換えた文字列を返す。
	 * 
	 * @param str 変数が含まれる文字列
	 * @param replace 特殊文字(ascii 0x00-0x20,0x7f)を空白に置換するか\x01のような表記にするか
	 *         4.0と同じ動作にする場合はfalse。
	 * @return 変数が置換された文字列
	 */
	public String bindParam(String str) {
		// local variables

		// main
		if (str == null) {
			log.debug("str is null");
			return null;
		}
		if (param == null) {
			log.debug("replacement parameter(s) is not defined.");
			return str;
		}
		for (Map.Entry<String, String> entry : param.entrySet()) {
			if (entry.getValue() == null) {
				log.debug("value is not defined, 0-length string will be used. : key = " + entry.getKey());
				str = str.replace("#[" + entry.getKey() + "]", "");
				str = str.replace("#[" + entry.getKey() + _postfixOriginal + "]", "");
			} else {
				if (log.isTraceEnabled()) log.trace("replacing : string = " + str + ", key = " + entry.getKey() + ", value = " + entry.getValue());
				str = str.replace("#[" + entry.getKey() + "]", escapeStr(entry.getValue()));		// default : escape string
				str = str.replace("#[" + entry.getKey() + _postfixOriginal + "]", entry.getValue());	// not escape when :original
				if (log.isTraceEnabled()) log.trace("replaced : string = " + str);
			}
		}

		return str;
	}

	/**
	 * escape special code (ctrl, quote(", ', `), back slash)
	 * @param str escaped string
	 * @return
	 */
	private String escapeStr(String str) {
		// local variables
		String ret = null;

		// main
		if (str != null) {
			// escape \ to \\
			str = str.replace("\\", "\\\\");
			// escape " to \"
			str = str.replace("\"", "\\\"");
			// escape ' to \'
			str = str.replace("'", "\\'");
			// escape ` to \`
			str = str.replace("`", "\\`");
			// escape control to \xXX (0 - 1F & 7F)
			for (byte ascii = 0; ascii < 0x20; ascii++) {
				byte[] byteCode = { ascii };
				String strReplace = new String(byteCode);
				if (replace) {
					str = str.replace(strReplace, replaceChar);
				} else {
					str = str.replace(strReplace, "\\x" + Integer.toHexString(ascii).toUpperCase());
				}
			}
			byte[] byteCode = { 0x7F };
			String strReplace = new String(byteCode);
			if (replace) {
				str = str.replace(strReplace, replaceChar);
			} else {
				str = str.replace(strReplace, "\\x" + Integer.toHexString(0x7F).toUpperCase());
			}

			ret = str;
		}

		if (log.isDebugEnabled())
			log.debug("escaped string. (original = " + str + ", escaped = " + ret + ")");

		return ret;
	}

	public static void main(String[] args) {

		String str = "foo #[PARAM] bar #[ESCAPE] #[NOTFOUND] foo";

		Map<String, String> param = new HashMap<String, String>();
		param.put("PARAM", "foofoo");
		byte[] byteCode = { 0x10 };

		param.put("ESCAPE", "foo 'bar' \"foo\" `echo aaa` \\ bar" +
				" [" + new String(byteCode) + "], [" + new String(byteCode) + "]");

		StringBinder binder = new StringBinder(param);
		System.out.println("PARAM : " + param);
		System.out.println("ORIGINAL : " + str);
		System.out.println("BINDED   : " + binder.bindParam(str));
		StringBinder.setReplace(true);
		System.out.println("BINDED   : " + binder.bindParam(str));
		StringBinder.setReplaceChar("$");
		StringBinder.setReplace(true);
		System.out.println("BINDED   : " + binder.bindParam(str));
	}

}
