/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.text.StringEscapeUtils;

/**
 * 文字列内の変数を置換するUtilityクラス<br/>
 * 
 */
public class StringBinder {

	private static Log log = LogFactory.getLog(StringBinder.class);

	private Map<String, String> param = new HashMap<String, String>();
	private static final String _postfixOriginal = ":original";
	private static final String _postfixQuoteSh = ":quoteSh";
	private static final String _postfixEscapeCmd = ":escapeCmd";
	private static final String _postfixEscapeJson = ":escapeJson";
	private static final String _postfixSimplePost = ":simplePost";
	private static final String[] _specialPostfix = {
		_postfixOriginal,
		_postfixQuoteSh,
		_postfixEscapeCmd,
		_postfixEscapeCmd,
		_postfixSimplePost
	};

	private static String replaceChar = "?";
	private static boolean replace = false;
	
	/** 入れ子のない #[...] にマッチする
	* 「((?!#\\[).)*?」を分解すると、
	* -----
	*  (
	*    (?!#\\[.*]). -> #[...] じゃなくればどんな文字でもよい
	*  )*? -> 上記の最短一致
	* -----
	*/
	private static Pattern pattern = Pattern.compile("#\\[((?!#\\[.*]).)*?\\]", Pattern.DOTALL);

	/**
	 * 置換対象の文字列内の置換キーをリストで返却
	 * @param str 置換対象の文字列
	 * @param maxReplaceWord 文字列内に含まれる置換対象のキーワードの最大数
	 * 			(無限ループ防止用、基本はHinemosプロパティから取得)
	 */
	public static ArrayList<String> getKeyList(String str, int maxReplaceWord){
		ArrayList<String> keyList = new ArrayList<String>();
		if (str == null || str.isEmpty()) {
			return keyList;
		}
		
		log.debug("getKeyList() : original string=[" + str + "], maxReplaceWord=[" + maxReplaceWord + "]");
		Matcher matcher = pattern.matcher(str);
		Pattern onlyKeyPattern = Pattern.compile("#\\[(.*)]", Pattern.DOTALL);
		StringBuilder keyListStr = new StringBuilder();
		boolean notTopStr = false;
		for (int i = 0; i < maxReplaceWord; i++) {
			if (matcher.find()) {
				String keyWhole = matcher.group(0);
				Matcher onlyKeymatcher = onlyKeyPattern.matcher(keyWhole);
				if (onlyKeymatcher.find()) {
					String onlyKey = onlyKeymatcher.group(1);
					keyList.add(onlyKey);
					if(notTopStr){
						keyListStr.append(", ");
					}
					keyListStr.append(onlyKey);
					notTopStr = true;
				}
			} else {
				break;
			}
		}
		log.debug("getKeyList() : keyList=[" + keyListStr.toString() + "]");
		
		keyList = new ArrayList<String>(new HashSet<>(keyList));
		return keyList;
	}

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
				str = str.replace("#[" + entry.getKey() + _postfixQuoteSh + "]", "");
				str = str.replace("#[" + entry.getKey() + _postfixEscapeCmd + "]", "");
				str = str.replace("#[" + entry.getKey() + _postfixEscapeJson + "]", "");
				str = str.replace("#[" + entry.getKey() + _postfixSimplePost + "]", "");
			} else {
				if (log.isTraceEnabled()) log.trace("replacing : string = " + str + ", key = " + entry.getKey() + ", value = " + entry.getValue());
				str = str.replace("#[" + entry.getKey() + "]", escapeStr(entry.getValue()));		// default : escape string
				str = str.replace("#[" + entry.getKey() + _postfixOriginal + "]", entry.getValue());	// not escape when :original
				str = str.replace("#[" + entry.getKey() + _postfixQuoteSh + "]", quoteSh((entry.getValue()))); // Quote for Bash when :quoteSh
				str = str.replace("#[" + entry.getKey() + _postfixEscapeCmd + "]", escapeCmd(entry.getValue())); // Escape for Command Prompt when :escapeCmd
				str = str.replace("#[" + entry.getKey() + _postfixEscapeJson + "]", escapeJson(entry.getValue())); // Escape for Json value when :escapeJosn
				str = str.replace("#[" + entry.getKey() + _postfixSimplePost + "]", urlEncode(entry.getValue())); // URLEncode when :simplePost
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
	public static String escapeStr(String str) {
		// local variables
		String ret = null;
		String originStr = str;

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
			log.debug("escaped string. (original = " + originStr + ", escaped = " + ret + ")");

		return ret;
	}
	
	/**
	 * single quote for Bash.
	 * @param str quoted string
	 * @return
	 */
	private static String quoteSh(String str) {
		// local variables
		String ret = null;
		
		// main
		if (str != null) {
			// ' を '"'"' に置換する
			ret = str.replace("'", "'\"'\"'");
			// 文字列全体をシングルクォートする。
			ret = "'" + ret + "'";

			// プロパティcommon.invalid.char.replace(this.replace)がTrueの場合は制御文字を置換する。
			if(replace) {
				// 制御文字 (0 - 1F & 7F)を16進数文字(\xXX)に置換する。
				for (byte ascii = 0; ascii < 0x20; ascii++) {
					byte[] byteCode = { ascii };
					String strReplace = new String(byteCode);
					ret = ret.replace(strReplace, replaceChar);
				}

				byte[] byteCode = { 0x7F };
				String strReplace = new String(byteCode);
				ret = ret.replace(strReplace, replaceChar);
				
			}
		}
		
		if (log.isDebugEnabled()) {
			log.debug("quoted string. (original = " + str + ", quoted = " + ret + ")");
		}

		return ret;
	}

	/**
	 * escape for Command Prompt.
	 * @param str escaped string
	 * @return
	 */
	private static String escapeCmd(String str) {
		// local variables
		String ret = null;
		String strReplace = null;
		
		// main
		if (str != null) {
			// "を""にエスケープする。
			ret = str.replace("\"", "\"\"");
			// コマンドプロンプトの特殊文字を^でエスケープする。
			// "は上のエスケープと合わせて^"^"となる。
			ret = ret.replaceAll("(%|\\^|&|<|>|\\||\")", "^$0");
			// \は\\にエスケープする。
			ret = ret.replace("\\", "\\\\");
			// 制御文字 (0 - 1F & 7F)を16進数文字(\xXX)に置換する。
			for (byte ascii = 0; ascii < 0x20; ascii++) {
				byte[] byteCode = { ascii };
				strReplace = new String(byteCode);
				if (replace) {
					ret = ret.replace(strReplace, replaceChar);
				} else {
					ret = ret.replace(strReplace, String.format("\\x%02X", ascii));
				}
			}
			
			byte[] byteCode = { 0x7F };
			strReplace = new String(byteCode);
			if (replace) {
				ret = ret.replace(strReplace, replaceChar);
			} else {
				ret = ret.replace(strReplace, String.format("\\x%02X", 0x7F));
			}
			
			// 空白文字(スペース, 全角スペース)を16進数文字に置換する。
			// 制御文字ではないのでプロパティcommon.invalid.char.replace(this.replace)の影響は受けない。
			String space = " ";
			String fullWidthSpace = "　";
			
			ret = ret.replace(space, String.format("\\x%02X", 0x20));
			ret = ret.replace(fullWidthSpace, String.format("\\x%02X", 0x81) + String.format("\\x%02X", 0x40));

		}

		if (log.isDebugEnabled()) {
			log.debug("escaped string. (original = " + str + ", escaped = " + ret + ")");
		}

		return ret;
	}

	/**
	 * escape for Json value
	 * @param str escaped string
	 * @return
	 */
	private static String escapeJson(String str) {
		//org.apache.commons.text.StringEscapeUtil で変換
		return StringEscapeUtils.escapeJson(str);
	}
	
	/**
	 * URLEncode.
	 * @param str src string
	 * @return Encoded String
	 */
	private static String urlEncode(String str) {
		//org.apache.commons.codec.net.URLCodec で変換
		URLCodec codec = new URLCodec("UTF-8");
		try {
			return codec.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			//エンコードできない場合は 先頭にエラー内容を付加して 返す
			return  UnsupportedEncodingException.class.getSimpleName() + " " + e.getMessage()  + " "+ str;
		}
	}
	/**
	 * escape(quote) for Shell
	 * @param str escaped string
	 * @param opt escape option ":quoteSh" or ":escapeCmd"
	 * @return
	 */
	public static String escapeShell(String str, String opt) {
		String ret = null;

		if (opt.equals(_postfixQuoteSh)) {
			ret = quoteSh(str);
		} else if (opt.equals(_postfixEscapeCmd)) {
			ret = escapeCmd(str);
		} else {
			log.warn("escapeShell(): invalid escape option\"" + opt + "\", return null.");
		}

		return ret;
	}

	public String replace(String input) {
		log.debug("input : " + input);

		String ret = input;
		int n = 100; // 無限ループを防ぐために、ループ回数を100に制限しておく。
		try {
			for(int i = 0; i < n; i++) {
				StringBuffer sb = new StringBuffer();
				Matcher m = pattern.matcher(ret);
				while(m.find()) {
					log.debug("m.group() : " + m.group());
					m.appendReplacement(sb, Matcher.quoteReplacement(bindParam(m.group())));
				}
				m.appendTail(sb);

				// 前回と置換結果が同じ場合、これ以上置換できないのでループを抜ける
				if(sb.toString().equals(ret)) {
					log.debug("no more replaced");
					break;
				}
				ret = sb.toString();
			}
		} catch (RuntimeException e) {
			// 少なくともappendReplacementでIllegalArgumentExceptionが発生する場合がある。
			log.warn("replace() : " + e.getMessage()+", input="+input+", ret="+ret, e);
		}

		log.debug("ret : " + ret);
		return ret;
	}
	
	/**
	 * split paramId:specialPostfix(:original, :quoteSh, :escapeCmd)
	 * @param str "paramId:postfix"
	 * @return String[] {"paramId", ":postfix"} (if postfix isn't specialPostfix , will be null)
	 */
	public static String[] splitPostfix(String paramId) {
		String[] ret = {null, null};
		int paramIdLegnth = paramId.length();
		for (String postfixStr: _specialPostfix) {
			if (paramId.endsWith(postfixStr) && paramIdLegnth - postfixStr.length() != 0) {
				ret[0] = paramId.substring(0,paramIdLegnth - postfixStr.length());
				ret[1] = postfixStr;
			}			
		}
		return ret;
	}
	
	/**
	 * return whether paramIdList contains paramId or also paramId with specialPostfix
	 * 
	 * @param paramIdList
	 * @param paramId
	 * @return whether paramIdList contains paramId
	 */
	public static boolean containsParam(List<String> paramIdList, String paramId) {
		if (paramIdList.contains(paramId)) {
			return true;
		}
		
		for (String postfixStr: _specialPostfix) {
			if (paramIdList.contains(paramId + postfixStr)) {
				return true;
			}
		}

		return false;
	}

	public static void main(String[] args) {
		
		
		String str = "foo #[PARAM] bar #[ESCAPE] #[NOTFOUND] foo bar";

		Map<String, String> param = new HashMap<String, String>();
		param.put("PARAM", "foofoo");
		byte[] byteCode = { 0x10 };

		param.put("ESCAPE", "foo 'bar' \"foo\" `echo aaa` \\ bar $ bar" +
				" [" + new String(byteCode) + "], [" + new String(byteCode) + "]");

		StringBinder binder = new StringBinder(param);
		System.out.println("PARAM : " + param);
		System.out.println("ORIGINAL : " + str);
		System.out.println("BINDED   : " + binder.bindParam(str));
		StringBinder.setReplace(true);
		System.out.println("BINDED   : " + binder.bindParam(str));
		StringBinder.setReplaceChar("?");
		StringBinder.setReplace(true);
		System.out.println("BINDED   : " + binder.bindParam(str));
		
		Map<String, String> param2 = new HashMap<>();
		param2.put("FOO", "foo");
		param2.put("BAR", "$[bar]");
		param2.put("HOGE:uga", "hoge");
		param2.put("HOGE", "zzz");
		param2.put("UGA", "uga");
		StringBinder binder2 = new StringBinder(param2);

		String input = "#[HOGE]";
		System.out.println(binder2.replace(input));
		input = "#[HOGE:#[UGA]]";
		System.out.println(binder2.replace(input));
		input = "#[FOO] AAA#[HOGE:#[UGA]]BBB #[BAR]";
		System.out.println(binder2.replace(input));
		input = "#[FOO] AAA#[HOGE:#[]]BBB #[BAR]";
		System.out.println(binder2.replace(input));
		input = "#[FOO] AAA#[HOGE:#[UGA]BBB #[BAR]";
		System.out.println(binder2.replace(input));
		input = "#[FOO] AAA#[HOGE:#[AHE]]BBB #[BAR]";
		System.out.println(binder2.replace(input));
		input = "#[FOO] AAA#[HOGE:#[UGA]]BBB #[AHE]";
		System.out.println(binder2.replace(input));
		
		
		System.out.println("Test 4");
		str = "foo #[PARAM] bar #[ESCAPE] #[NOTFOUND] foo bar";
		Map<String, String> param4 = new HashMap<String, String>();
		param4.put("PARAM", "foofoo");
		byte[] byteCode2 = { 0x10 };

		param4.put("ESCAPE", "foo 'bar' \"foo\" `echo aaa` \\ bar $ bar" +
				" [" + new String(byteCode2) + "], [" + new String(byteCode2) + "]");

		StringBinder binder4 = new StringBinder(param4);
		System.out.println("BINDED   : " + binder4.bindParam(str));

		
		// Test 3
		str = "echo \"message:#[MESSAGE]; original message:#[ORIGINAL_MESSAGE]\"";
		StringBinder.setReplace(false);
		param = new HashMap<String, String>();
		param.put("MESSAGE", "This's message");
		param.put("ORIGINAL_MESSAGE", "This is \"message\".(\r\n) (\n) (`) ($) \\ \n(original)");

		StringBinder binder3 = new StringBinder(param);
		System.out.println(binder3.replace(str));
		System.out.println(binder3.bindParam(str));
		
		System.out.println("old test------------------------------------------");
		System.out.println("test getKeyList()------------------------------------------");
		String originalString = "";
		System.out.println(String.format("originalString=[%s]", originalString));
		ArrayList<String> resultList = getKeyList(originalString, 1000);
		if(resultList == null || resultList.isEmpty()){
			System.out.println("result is empty.");
			return;
		}
		int i = 0;
		for(String result : resultList){
			System.out.println(String.format("result[%d]=[%s]", i, result));
		}

	}

}
