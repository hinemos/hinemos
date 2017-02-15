/*

Copyright (C) since 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.accesscontrol.factory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.accesscontrol.model.UserInfo;
import com.clustercontrol.accesscontrol.util.QueryUtil;
import com.clustercontrol.accesscontrol.util.UserRoleCache;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.UserNotFound;
import com.clustercontrol.util.HinemosTime;

/**
 * ユーザ情報を検索するファクトリクラス<BR>
 *
 * @version 1.0.0
 * @since 3.2.0
 */
public class LoginUserSelector {

	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(LoginUserSelector.class);


	/**
	 * 指定のユーザ情報を取得する。<BR>
	 * 
	 * @return ユーザ情報
	 * @throws HinemosUnknown
	 */
	public static UserInfo getUserInfo(String userId) throws HinemosUnknown, UserNotFound {

		UserInfo info = null;

		if(userId != null && userId.compareTo("") != 0){
			try {
				// ユーザを取得
				info = QueryUtil.getUserPK(userId);
			} catch (UserNotFound e) {
				throw e;
			} catch (Exception e) {
				m_log.warn("getUserInfo() failure to get user. : userId = " + userId, e);
				throw new HinemosUnknown(e.getMessage() + " : failure to get user.", e);
			}
		}
		return info;
	}

	/**
	 * ユーザ名とパスワードからUserInfoを取得する。
	 * @param userid
	 * @param password
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public static void getUserInfoByPassword(String userId, String password, ArrayList<SystemPrivilegeInfo> systemPrivilegeList)
			throws InvalidUserPass, InvalidRole, HinemosUnknown {

		UserInfo info = null;
		boolean invalidUserFlag = true;

		try {
			info = QueryUtil.getUserPK(userId);
		} catch (UserNotFound e) {
			// なにもしない
		} catch (Exception e) {
			m_log.warn("getUserInfoByPassword() " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
		if (info != null && info.getPassword().equals(hash(password))) {
			invalidUserFlag = false;
		}
		if (invalidUserFlag) {
			String message = "user(" + userId + ")/password is invalid combination";
			m_log.info("getUserInfoByPassword() " + message);
			throw new InvalidUserPass(message);
		}

		// ユーザが保持するシステム権限情報を取得する
		for (SystemPrivilegeInfo systemPrivilegeInfo : systemPrivilegeList) {
			if (!UserRoleCache.isSystemPrivilege(userId, systemPrivilegeInfo)) {
				String message = "need-role " + list2String(systemPrivilegeList);
				m_log.info("getUserInfoByPassword() " + message);
				throw new InvalidRole(message);
			}
		}
	}

	private static String hash(String password) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			m_log.info("hash() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			return null;
		}
		return Base64.encodeBase64String(md.digest(password.getBytes()));
	}

	/**
	 * 処理速度を測定するためのサンプルスクリプト
	 * @param args
	 */
	public static void main (String args[]) {
		int maxN = 1000000;
		String str = null;
		System.out.println("start (" + maxN + " loops) : " + HinemosTime.getDateString());
		for (int i = 0; i < maxN; i ++ ) {
			str = hash("HINEMOS_AGENT");
		}
		System.out.println("hash : " + str);
		System.out.println("end   (" + maxN + " loops) : " + HinemosTime.getDateString());
	}

	/**
	 * リストを文字列に変換する関数。
	 * debug用。
	 * @param list
	 * @return
	 */
	private static String list2String (Collection<SystemPrivilegeInfo> list) {
		if (list == null) {
			return "";
		}
		
		StringBuilder ret = new StringBuilder();
		for (SystemPrivilegeInfo s : list) {
			ret.append(s.getSystemFunction());
			ret.append(s.getSystemPrivilege());
		}
		return ret.toString();
	}

	/**
	 * ユーザ一覧情報を取得する。<BR>
	 * 
	 * @return ユーザ情報のリスト
	 * @throws UserNotFound
	 * @throws HinemosUnknown
	 */
	public static ArrayList<UserInfo> getUserInfoList() {
		m_log.debug("getting all user...");

		// 全ユーザを取得
		List<UserInfo> users = QueryUtil.getAllShowUser();

		m_log.debug("successful in getting all user.");

		if(users == null || users.isEmpty()){
			return new ArrayList<>();
		} else {
			return new ArrayList<UserInfo>(users);
		}
	}
}
