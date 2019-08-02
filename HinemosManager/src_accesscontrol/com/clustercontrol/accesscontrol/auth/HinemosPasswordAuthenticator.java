/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.auth;

import java.security.MessageDigest;

import org.apache.commons.codec.binary.Base64;

import com.clustercontrol.accesscontrol.model.UserInfo;

/**
 * Hinemos標準のパスワード認証(内部DBに保存されたパスワードを使用した認証)を行います。
 */
public class HinemosPasswordAuthenticator implements Authenticator {

	@Override
	public String getSimpleName() {
		return "InternalPassword";
	}

	@Override
	public boolean isExternal() {
		return false;
	}

	@Override
	public boolean execute(AuthenticationParams params, UserInfo userInfo) throws Exception {
		MessageDigest md = MessageDigest.getInstance("MD5");
		String hashedPassword = Base64.encodeBase64String(md.digest(params.getPassword().getBytes()));
		return hashedPassword.equals(userInfo.getPassword());
	}

	@Override
	public void terminate() {
		// NOP
	}

}