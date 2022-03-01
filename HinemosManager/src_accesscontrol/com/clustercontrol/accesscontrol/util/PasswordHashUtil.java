/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.accesscontrol.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base64;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.HinemosUnknown;

/**
 * ユーザのログインパスワードをDBに保存する際のハッシュ関数を取得するクラス
 */
public class PasswordHashUtil {

	private enum Hash {
		MD5("MD5"), SHA256("SHA-256");

		private final String text;

		Hash(String text) {
			this.text = text;
		}

		private String getString() {
			return this.text;
		}

		private static boolean isMember(String s) {
			for (Hash h : Hash.values()) {
				if (h.getString().equals(s)) {
					return true;
				}
			}
			return false;
		}
	}

	// access.user.password.hash の設定値が有効な値の場合、そのまま返す。
	// 無効な値の場合はデフォルトの SHA-256 を返す。
	public static String getPasswordHash() {
		String hash = HinemosPropertyCommon.access_user_password_hash.getStringValue();
		if (Hash.isMember(hash)) {
			return hash;
		}
		return "SHA-256";
	}
	
	// getPasswordHash を用いて パスワードをハッシュ化する
	public static String encodePassword(String password) throws HinemosUnknown {
		try {
			MessageDigest md = MessageDigest.getInstance(getPasswordHash());
			String hashedPassword = Base64.encodeBase64String(md.digest(password.getBytes()));
			return hashedPassword;
		}catch(	NoSuchAlgorithmException e){
			//getPasswordHashは MD5かSHA-256しか返さないので ここにくることは 通常ありえない
			throw new HinemosUnknown(e.getMessage(),e);
		}
	}
}
