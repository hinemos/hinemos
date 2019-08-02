/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.factory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.model.UserInfo;
import com.clustercontrol.accesscontrol.util.QueryUtil;
import com.clustercontrol.fault.HinemosUnknown;
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
