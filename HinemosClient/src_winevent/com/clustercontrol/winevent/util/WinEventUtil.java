/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.winevent.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.clustercontrol.winevent.bean.WinEventConstant;

public class WinEventUtil {

	// ----- static フィールド ----- //
	private static Map<String, Long> keywordMap = new ConcurrentHashMap<String, Long>();

	static {
		keywordMap.put(WinEventConstant.AUDIT_FAILURE_STRING, WinEventConstant.AUDIT_FAILURE_LONG);
		keywordMap.put(WinEventConstant.AUDIT_FAILURE_STRING_OLD, WinEventConstant.AUDIT_FAILURE_LONG);	// EventLogEntryType 用

		keywordMap.put(WinEventConstant.AUDIT_SUCCESS_STRING, WinEventConstant.AUDIT_SUCCESS_LONG);
		keywordMap.put(WinEventConstant.AUDIT_SUCCESS_STRING_OLD, WinEventConstant.AUDIT_SUCCESS_LONG);	// EventLogEntryType 用

		keywordMap.put(WinEventConstant.CLASSIC_STRING, WinEventConstant.CLASSIC_LONG);
		keywordMap.put(WinEventConstant.CORRELATION_HINT_STRING, WinEventConstant.CORRELATION_HINT_LONG);
		keywordMap.put(WinEventConstant.RESPONSE_TIME_STRING, WinEventConstant.RESPONSE_TIME_LONG);
		keywordMap.put(WinEventConstant.SQM_STRING, WinEventConstant.SQM_LONG);
		keywordMap.put(WinEventConstant.WDI_CONTEXT_STRING, WinEventConstant.WDI_CONTEXT_LONG);
		keywordMap.put(WinEventConstant.WDI_DIAG_STRING, WinEventConstant.WDI_DIAG_LONG);
	}

	/**
	 * Keywordのlong値をString値に変換する
	 * 
	 * @param keywordLong Keywordsのlong値
	 * @return KeyWordのString値
	 */
	public static String getKeywordString(long keywordLong){
		// valueからkeyを検索する
		for (Map.Entry<String, Long> entry : keywordMap.entrySet()) {
			if(keywordLong == entry.getValue().longValue()){
				return entry.getKey();
			}
		}

		// keyにvalueが見つからない場合
		return null;
	}

	/**
	 * KeywordのString値をlong値に変換する
	 * 
	 * @param keywordString KeywordsのString値
	 * @return Keywordsのlong値
	 */
	public static long getKeywordLong(String keywordString){
		return keywordMap.get(keywordString);
	}

	/**
	 * KeywordのString値が定義済みかどうかを返す
	 * 
	 * @return KeywordのString値が定義済みかどうか
	 */
	public static boolean containsKeywordString(String keywordString){
		return keywordMap.containsKey(keywordString);
	}
}
