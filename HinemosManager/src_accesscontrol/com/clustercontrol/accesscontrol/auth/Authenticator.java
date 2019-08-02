/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.auth;

import com.clustercontrol.accesscontrol.model.UserInfo;

/**
 * ユーザ認証処理を実装するクラスのためのインターフェイスです。
 * 
 */
public interface Authenticator {

	/**
	 * メッセージ表示などで使用するための名称を返します。
	 */
	String getSimpleName();

	/**
	 * 外部認証ならtrueを返します。
	 */
	boolean isExternal();

	/**
	 * ユーザIDとパスワードでユーザ認証を実行します。
	 * <p>
	 * 複数スレッドから同時に実行される可能性があります。
	 * 
	 * @param params 認証情報。
	 * @param userInfo HinemosのDBから取得したユーザ情報。
	 * @return 認証成功の場合はtrue、資格情報に誤りがある場合はfalse。
	 * @throws Exception 資格情報の誤り以外のエラーが発生した場合には例外を投げます。
	 */
	boolean execute(AuthenticationParams params, UserInfo userInfo) throws Exception;

	/**
	 * 終了処理を行います。
	 */
	void terminate();
}