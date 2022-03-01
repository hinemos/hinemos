/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.message.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.clustercontrol.commons.util.HinemosPropertyCommon;

/**
 * メッセージフィルタマネージャとの認証トークンを管理するクラス
 */
public class TokenManager {

	/** Bearer認証用トークンリスト */
	private static List<String> tokenList = new ArrayList<>();

	/** 最新のトークン一覧文字列を保持する（更新有無の判定に使用） */
	private static String currentTokens = "";

	private static final Object lock = new Object();

	/**
	 * トークンリストを取得します。
	 */
	public static List<String> getTokenList() {
		synchronized (lock) {

			// Hinemosプロパティと保持トークンに差異がある場合は取り込み
			if (!currentTokens.equals(HinemosPropertyCommon.notify_message_webapi_auth_token.getStringValue())) {
				currentTokens = HinemosPropertyCommon.notify_message_webapi_auth_token.getStringValue();
				tokenList = new ArrayList<>(Arrays.asList(currentTokens.split(",")));
			}

			// 戻り値はクローンを返す
			return new ArrayList<>(tokenList);
		}
	}

	/**
	 * 認証に成功したトークンを通知し、トークンリストの先頭になるよう並び替えします。
	 * 
	 * @param token
	 */
	public static void notifySuccessToken(String token) {
		synchronized (lock) {
			if (tokenList.contains(token)) {
				tokenList.remove(token);
				tokenList.add(0, token);
			}
		}
	}
}
